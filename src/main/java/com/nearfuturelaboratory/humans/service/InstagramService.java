package com.nearfuturelaboratory.humans.service;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.jayway.jsonpath.JsonPath;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.nearfuturelaboratory.humans.dao.InstagramFriendsDAO;
import com.nearfuturelaboratory.humans.dao.InstagramStatusDAO;
import com.nearfuturelaboratory.humans.dao.InstagramUserDAO;
import com.nearfuturelaboratory.humans.dao.ServiceTokenDAO;
import com.nearfuturelaboratory.humans.entities.ServiceToken;
import com.nearfuturelaboratory.humans.exception.BadAccessTokenException;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramFriend;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramStatus;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramUser;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramUserBriefly;
import com.nearfuturelaboratory.humans.serviceapi.InstagramApi;
import com.nearfuturelaboratory.humans.util.MongoUtil;
import com.nearfuturelaboratory.util.Constants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.chrono.ISOChronology;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mongodb.morphia.Key;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import java.util.*;

//import com.nearfuturelaboratory.humans.service.status.InstagramStatus;

public class InstagramService /*implements AbstractService*/ {

    protected static final String FOLLOWS_URL = "https://api.instagram.com/v1/users/%s/follows";
    protected static final String FOLLOWED_BY_URL = "https://api.instagram.com/v1/users/%s/followed-by";
    protected static final String STATUS_URL = "https://api.instagram.com/v1/users/%s/media/recent";
    protected static final String STATUS_BY_MEDIAID_URL = "https://api.instagram.com/v1/media/%s";
    protected static final String LIKE_STATUS_BY_MEDIAID_URL = "https://api.instagram.com/v1/media/%s/likes";
    protected static final String USER_URL = "https://api.instagram.com/v1/users/%s";
    protected static final String TAG_NAME = "https://api.instagram.com/v1/tags/%s";
    protected static final String TAG_RECENT_MEDIA = "https://api.instagram.com/v1/tags/%s/media/recent";
    protected static final String TAG_SEARCH = "https://api.instagram.com/v1/tags/search?q=%s";

    protected OAuthService service;

    protected Token accessToken;

    //protected JSONObject user;
    protected InstagramUser user;

    protected DB db;
    protected MongoDatabase database; // new
    protected MongoClient client;

    protected InstagramStatusDAO statusDAO;
    protected InstagramUserDAO userDAO;
    protected InstagramFriendsDAO followsDAO;
    protected ServiceTokenDAO tokenDAO;

    protected Gson gson;

    final static Logger logger = LogManager.getLogger(com.nearfuturelaboratory.humans.service.InstagramService.class);

    public InstagramService() {
        //TODO Gross..
        client = MongoUtil.getMongo();
        db = client.getDB("instagram");
        database = client.getDatabase("instagram");
        statusDAO = new InstagramStatusDAO();
        statusDAO.ensureIndexes();

        userDAO = new InstagramUserDAO();
        userDAO.ensureIndexes();

        followsDAO = new InstagramFriendsDAO();
        followsDAO.ensureIndexes();

        tokenDAO = new ServiceTokenDAO("instagram");
        tokenDAO.ensureIndexes();

        gson = new Gson();

    }


    protected void setDBName(String dbName) {
        db = client.getDB(dbName);
        database = client.getDatabase(dbName);

        statusDAO = new InstagramStatusDAO();
        statusDAO.ensureIndexes();

        userDAO = new InstagramUserDAO();
        userDAO.ensureIndexes();

        followsDAO = new InstagramFriendsDAO();
        followsDAO.ensureIndexes();

        tokenDAO = new ServiceTokenDAO(dbName);
        tokenDAO.ensureIndexes();

        gson = new Gson();

    }

    //TODO Change this super ridiculous constructor. Should all be factory methods like above.
    public InstagramService(Token aAccessToken) {
        this();
        accessToken = aAccessToken;
        service = new ServiceBuilder()
                .provider(InstagramApi.class)
                .apiKey(Constants.getString("INSTAGRAM_API_KEY"))
                .apiSecret(Constants.getString("INSTAGRAM_API_SECRET"))
                .callback(Constants.getString("INSTAGRAM_CALLBACK_URL"))
                .scope("basic,likes")
                .build();

        //		user = this.serviceRequestUserBasic();

    }

    protected void setAccessToken(Token aAccessToken) {
        accessToken = aAccessToken;
        service = new ServiceBuilder()
                .provider(InstagramApi.class)
                .apiKey(Constants.getString("INSTAGRAM_API_KEY"))
                .apiSecret(Constants.getString("INSTAGRAM_API_SECRET"))
                .callback(Constants.getString("INSTAGRAM_CALLBACK_URL"))
                .scope("basic,likes")
                .build();

    }

    @Deprecated
    public void initServiceOnBehalfOfUsername(String aUsername) throws BadAccessTokenException {
        accessToken = InstagramService.deserializeToken(aUsername);
        setAccessToken(accessToken);
        if (accessToken == null) {
            throw new BadAccessTokenException("The access token for Instagram User " + aUsername + " is null. It probably does not exist.");
        } else {
            user = InstagramService.getLocalUserBasicForUsername(aUsername);
            if (user == null) {
                user = serviceRequestUserBasic();
            }
        }
    }

    // on behalf of a specific instagram username

    /**
     * @param aUsername an instagram username which of course must be a user of the application
     * @return
     * @throws BadAccessTokenException
     */
    public static InstagramService createServiceOnBehalfOfUsername(String aUsername) throws BadAccessTokenException {
        InstagramService result;
        Token token;
        InstagramUser user = InstagramService.getLocalUserBasicForUsername(aUsername);
        if (user == null) {
            logger.warn("null token. trying to find one by username");
            token = InstagramService.deserializeToken(aUsername);
            result = new InstagramService(token);
            user = result.serviceRequestUserBasic();

        } else {
            token = InstagramService.deserializeToken(user);
            result = new InstagramService(token);
        }
        if (token == null) {
            throw new BadAccessTokenException("The access token for Instagram User " + aUsername + " is null. It probably does not exist.");
        }

        result.user = user;
        return result;
    }

    protected static InstagramUser staticGetLocalUserBasicForUserID(String aUserID) {
        InstagramUserDAO dao = new InstagramUserDAO();
        dao.ensureIndexes();
        InstagramUser result = dao.findByExactUserID(aUserID);
        return result;
    }

    /**
     * This will go to the service and get "self" for whoever's accessToken we have
     */
    public InstagramUser serviceRequestUserBasic() throws BadAccessTokenException {
        this.user = this.serviceRequestUserBasicForUserID("self");
        return user;
    }


    public InstagramUser getThisUser() {
        return user;
    }

    /**
     * Request from Instagram the basic user info for a particular user id
     * and save it.
     *
     * @param aUserID
     * @return
     */
    public InstagramUser serviceRequestUserBasicForUserID(String aUserID) throws BadAccessTokenException {
        //JSONObject result = __serviceRequestUserBasicForUserID(aUserID);
        JSONObject aUser;
        String userURL = String.format(USER_URL, aUserID);
        OAuthRequest request = new OAuthRequest(Verb.GET, userURL);
        service.signRequest(accessToken, request);
        Response response = request.send();
        // TODO Error check..
        String s = response.getBody();
        Object obj = JSONValue.parse(s);
        try {
            aUser = (JSONObject) ((JSONObject) obj).get("data");
            com.nearfuturelaboratory.humans.instagram.entities.InstagramUser iuser = gson.fromJson(aUser.toString(),
                    com.nearfuturelaboratory.humans.instagram.entities.InstagramUser.class);
            saveUserBasicJson(aUser);
            return iuser;
        } catch (Exception e) {
            //logger.warn("Maybe a private user when doing analytics?");
            //logger.warn(e);
            if (response.getCode() == 400 && response.getBody().contains("OAuthAccessTokenException")) {
                logger.warn("Bad response for Instagram User ID " + aUserID + " via " + this.getThisUser() + " " + response.getBody(), e);
                throw new BadAccessTokenException("Bad response for " + aUserID + " " + this.getThisUser() + " " + response.getBody());
            } else if (response.getCode() == 400 && response.getBody().contains("APINotAllowedError")) {
                logger.warn("APINotAllowedError. You've either been blocked by a user or are running analytics against someone else's account.");
                logger.warn(response.getBody());
            } else {
                // logger.warn(e);
            }
            return null;
        }


    }


    protected InstagramUser getLocalUserBasicForUserID(String aUserID) {
        InstagramUser result = userDAO.findByExactUserID(aUserID);
        return result;
    }

    protected com.nearfuturelaboratory.humans.instagram.entities.InstagramStatus getMostRecentStatusForUserID(String aUserID) {
        return statusDAO.findMostRecentStatusByExactUserID(aUserID);
    }


    public Key<InstagramUser> saveUserBasic(InstagramUser aUser) {
        try {
            return userDAO.save(aUser);
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    protected Key<InstagramUser> saveUserBasicJson(JSONObject aUserJson) {
        try {
            com.nearfuturelaboratory.humans.instagram.entities.InstagramUser iuser = gson.fromJson(aUserJson.toString(),
                    com.nearfuturelaboratory.humans.instagram.entities.InstagramUser.class);

            return userDAO.save(iuser);
        } catch (Exception e) {
            logger.error(e);
            return null;
        }

    }

    protected String getMostRecentStatusID(String aUserID) {
        String result = null;
        com.nearfuturelaboratory.humans.instagram.entities.InstagramStatus most_recent = getMostRecentStatusForUserID(aUserID);
        if (most_recent != null) {
            result = most_recent.getStatusId();
        }
        return result;
    }

    public List<InstagramStatus> getLocalStatusByExactUserIDToMonthsAgo(String aUserID, int aMonthsAgo) {
        return statusDAO.findStatusByExactUserIDToMonthsAgo(aUserID, aMonthsAgo);
    }

    public InstagramStatus getMostRecentStatus() {
        return statusDAO.findMostRecentStatusByExactUserID(this.getThisUser()
                .getId());
    }

    public InstagramStatus getOldestStatus() {
        return statusDAO.findOldestStatusByExactUserID(this.getThisUser().getId());
    }


    /**
     * Weird method to see if I can easily change the type of the created_time field to Long
     */
    public void freshenStatus() {
        List<InstagramStatus> status = getStatusForUserID(this.getThisUser().getId());
        for (InstagramStatus item : status) {
            statusDAO.save(item);
        }
    }

    public List<InstagramStatus> getStatus() {
        return getStatusForUserID(this.getThisUser().getId());
    }

    public List<InstagramStatus> getStatusForUserID(String aUserID) {
        return statusDAO.findByExactUserID(aUserID);
    }

    //public List<InstagramStatus> getStatus

    /**
     * Given a minimum (most recent) status ID, get all the ones after it.
     * The way the Instagram endpoint works, this basically also includes the status with the aMinID, if it exists
     *
     * @param aUserID
     * @param aMinID
     * @return
     */
    public List<InstagramStatus> serviceRequestStatusForUserIDAfterMinID(String aUserID, String aMinID) {
        if (aUserID == null || aUserID.equalsIgnoreCase("self")) {
            aUserID = (String) user.getId();
        }
        String statusURL = String.format(STATUS_URL, aUserID);
        OAuthRequest request = new OAuthRequest(Verb.GET, statusURL);
        request.addQuerystringParameter("min_id", aMinID);
        service.signRequest(accessToken, request);
        Response response = request.send();
        String s = response.getBody();
        Object jsonResponse = JSONValue.parse(s);
        @SuppressWarnings("unused")
        JSONObject status = (JSONObject) jsonResponse;
        JSONArray full_data = (JSONArray) status.get("data");
        // Save to Mongo
        return this.saveStatusJson(full_data);

    }

    /**
     * For aUserID get one chunk of status from some number of months ago
     *
     * @param aUserID
     * @param aMonthsAgo
     */
    //TODO Alter this method to be more efficient by using both max_timestamp as well as max_id or something
    public List<InstagramStatus> serviceRequestStatusForUserIDFromMonthsAgo(String aUserID, int aMonthsAgo) {
        Calendar ago = Calendar.getInstance();
        ago.add(Calendar.MONTH, -1 * aMonthsAgo);
        //long year_ago = ago.getTimeInMillis();
        if (aUserID == null || aUserID.equalsIgnoreCase("self")) {
            aUserID = (String) user.getId();
        }
        String statusURL = String.format(STATUS_URL, aUserID);
        OAuthRequest request = new OAuthRequest(Verb.GET, statusURL + "?max_timestamp=" + String.valueOf(ago.getTimeInMillis() / 1000l));
        request.addQuerystringParameter("count", "40");
        //request.addQuerystringParameter("MIN_TIMESTAMP", String.valueOf(ago.getTimeInMillis()));
        service.signRequest(accessToken, request);
        Response response = request.send();
        String s = response.getBody();
        //TODO Error checking
        Object jsonResponse = JSONValue.parse(s);

        JSONObject status = (JSONObject) jsonResponse;
        List<InstagramStatus> result = new ArrayList<InstagramStatus>();
        if (status != null) {
            JSONArray full_data = (JSONArray) status.get("data");
//        if(full_data != null && full_data.size() > 0) {
//            JSONObject oldest = (JSONObject) full_data.get(full_data.size() - 1);
//            long oldest_time = Long.parseLong(oldest.get("created_time").toString());
//            Calendar oldest_cal = Calendar.getInstance();
//            oldest_cal.setTimeInMillis(oldest_time * 1000l);
//        } else {
//            logger.warn("Got back no status from Instagram for "+aUserID+" for "+this.getThisUser().getUsername());
//        }
            result = saveStatusJson(full_data);
        } else {
            logger.warn("Got weird status result for " + this.getThisUser().getUsername() + " for " + aUserID);
            logger.warn(status);

        }
        return result;

    }

    public List<InstagramStatus> serviceRequestStatus() {
        return serviceRequestStatusForUserID(this.getThisUser().getId());
    }

    public List<InstagramStatus> serviceRequestStatusForUserID(String aUserID) {
        if (aUserID.equalsIgnoreCase("self")) {
            aUserID = this.getThisUser().getId();
        }
        //long startTime = System.nanoTime();
        String aMinID = this.getMostRecentStatusID(aUserID);
        //long endTime = System.nanoTime();
        if (aMinID == null) {
            return this.serviceRequestStatusForUserIDToMonthsAgo(aUserID, 1);
        } else {

            return this.serviceRequestStatusForUserIDAfterMinID(aUserID, aMinID);
        }
        //serviceRequestStatusForUserIDToMonthsAgo(aUserID, 0);
    }

    /**
     * For aUserID get status from most recent until some number of months ago
     *
     * @param aUserID
     * @param aBackMonthsAgo
     */
    public List<InstagramStatus> serviceRequestStatusForUserIDToMonthsAgo(String aUserID, int aBackMonthsAgo) {
        Calendar ago = Calendar.getInstance();
        ago.add(Calendar.MONTH, -1 * aBackMonthsAgo);
        //long year_ago = ago.getTimeInMillis();
        if (aUserID == null || aUserID.equalsIgnoreCase("self")) {
            aUserID = (String) user.getId();
        }
        String statusURL = String.format(STATUS_URL, aUserID);
        OAuthRequest request = new OAuthRequest(Verb.GET, statusURL);
        request.addQuerystringParameter("count", "50");
        service.signRequest(accessToken, request);
        Response response = request.send();
        //TODO Nasty..
        if (response.getCode() != 200) {
            logger.warn(aUserID + " / " + this.getThisUser().getId() + " response.getCode()= " + response.getCode());
            logger.warn(response.getMessage());
            return new ArrayList<InstagramStatus>();
        }

        String s = response.getBody();
        Object jsonResponse = JSONValue.parse(s);

        JSONObject status = (JSONObject) jsonResponse;
        JSONArray full_data = (JSONArray) status.get("data");
        //TODO Nasty..
        if (full_data.size() < 1) {
            InstagramUser user = this.getLocalUserBasicForUserID(aUserID);
            logger.warn("For " + user.getUsername() + " no data found");
//            logger.warn("For " + this.getThisUser().getUsername() + " no data found for " + this.getThisUser().getUsername() + " / " + this.getThisUser().getId());
//            logger.warn("onBehalfOf " + this.getThisUser().getOnBehalfOf());
//            logger.warn("Perhaps " + this.getThisUser().getUsername() + " is not authorized somehow to see this users status??");
            return new ArrayList<InstagramStatus>();
        }
        JSONObject oldest = (JSONObject) full_data.get(full_data.size() - 1);
        long oldest_time = Long.parseLong(oldest.get("created_time").toString());
        Calendar oldest_cal = Calendar.getInstance();
        oldest_cal.setTimeInMillis(oldest_time);
        String next_url;
        JSONObject obj = JsonPath.read(status, "pagination");
        if (obj.size() > 1) {
            next_url = JsonPath.read(status, "pagination.next_url");
        } else {
            next_url = null;
        }
        //String next_url = JsonPath.read(status, "pagination.next_url");

        do {
            if (next_url == null) {
                break;
            }
            request = new OAuthRequest(Verb.GET, next_url);
            service.signRequest(accessToken, request);
            response = request.send();
            s = response.getBody();
            jsonResponse = JSONValue.parse(s);
            status = (JSONObject) jsonResponse;
            JSONArray latest_data = (JSONArray) status.get("data");
            //channelSearchEnum[] enums = gson.fromJson(yourJson, channelSearchEnum[].class);
            full_data.addAll(latest_data);
            oldest = (JSONObject) latest_data.get(latest_data.size() - 1);
            oldest_time = Long.parseLong(oldest.get("created_time").toString());
            oldest_cal.setTimeInMillis(oldest_time * 1000);
            JSONObject p_obj = JsonPath.read(status, "pagination");
            if (p_obj.size() > 1) {
                next_url = JsonPath.read(status, "pagination.next_url");
            } else {
                next_url = null;
            }
        } while (oldest_cal.compareTo(ago) > 0 && next_url != null);
        JSONArray x = full_data;
        List<InstagramStatus> y = new ArrayList<InstagramStatus>();
        try {
            y = saveStatusJson(full_data);
        } catch (Exception e) {
            logger.warn("Woops", e);
            logger.warn("status", status);
            // logger.warn("full data is "+full_data);
        }

        return y;
    }

    public List<InstagramStatus> serviceRequestStatusByMediaID(String aMediaID) {

        String statusURL = String.format(STATUS_BY_MEDIAID_URL, aMediaID);
        OAuthRequest request = new OAuthRequest(Verb.GET, statusURL);
        service.signRequest(accessToken, request);
        Response response = request.send();
        if (response.getCode() != 200) {
            logger.warn(this.getThisUser().getUsername() + " / " + this.getThisUser().getId() + " response.getCode()= " + response.getCode());
            return new ArrayList<InstagramStatus>();
        }

        String s = response.getBody();
        Object jsonResponse = JSONValue.parse(s);

        JSONObject response_obj = (JSONObject) jsonResponse;
        JSONObject status_data = (JSONObject) response_obj.get("data");
        //JSONObject status = (JSONObject)status_data.get("value");
        //TODO Nasty..
        JSONArray array_of_one = new JSONArray();
        array_of_one.add(status_data);
        return saveStatusJson(array_of_one);
    }


    JsonElement serviceRequestTagsDataByTagName(String aTagName) {
        String statusURL = String.format(TAG_NAME, aTagName);
        OAuthRequest request = new OAuthRequest(Verb.GET, statusURL);
        service.signRequest(accessToken, request);
        Response response = request.send();
        if (response.getCode() != 200) {
            logger.warn(this.getThisUser().getUsername() + " / " + this.getThisUser().getId() + " response.getCode()= " + response.getCode());
        }
        String s = response.getBody();
        Gson gson = new Gson();
        JsonElement element = gson.fromJson(s, JsonElement.class);
        return element;
    }

    ArrayList<JSONObject> serviceRequestMediaRecentsByTag(String aTagName, int hours, int minutes) {
        ArrayList<JSONObject> allMediaRecents = new ArrayList<JSONObject>();

        String statusURL = String.format(TAG_RECENT_MEDIA, aTagName);
        OAuthRequest request = new OAuthRequest(Verb.GET, statusURL);
        service.signRequest(accessToken, request);
        Response response = request.send();
        if (response.getCode() != 200) {
            logger.warn(this.getThisUser().getUsername() + " / " + this.getThisUser().getId() + " response.getCode()= " + response.getCode());
        }

        String s = response.getBody();
        Object obj = JSONValue.parse(s);
        JSONObject map = (JSONObject) obj;

        JSONObject pagination = (JSONObject) map.get("pagination");

        if (pagination == null) {
            logger.warn("No pagination for " + this + " " + this.getThisUser());
        }

        DateTime earliest = new DateTime()
                .withChronology(ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))))
                .minusHours(hours)
                .minusMinutes(minutes);
        //.withSecondOfMinute(0);

        long current_earliest = new DateTime()
                .withChronology(ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))))
                .getMillis();// = allMediaRecents.get(allMediaRecents.size()-1);

        while ((pagination != null) &&
                current_earliest > earliest.getMillis()) {
            List<JSONObject> f = JsonPath.read(map, "data");
            if (f != null) {
                allMediaRecents.addAll(f);
                current_earliest = 1000 * Long.parseLong((String) allMediaRecents.get(allMediaRecents.size() - 1).get("created_time"));
            }

            String next_url = (String) pagination.get("next_url");
            if (next_url != null) {
                request = new OAuthRequest(Verb.GET, (String) next_url);
                response = request.send();
                s = response.getBody();
                //TODO Error checking
                if (s == null) {
                    logger.error("Null body in the response " + response);
                    break;
                }
                map = (JSONObject) JSONValue.parse(s);
                if (map == null) {
                    logger.error("No pagination in the get follows request " + response + " " + s);
                    break;
                }
                pagination = (JSONObject) map.get("pagination");
            } else {
                break;

            }
        }
        /*if(save) {
            logger.debug("Save follows for " + aUser.getUsername());
            saveFollowsJson(allFollows, aUserID*//*, aUser*//*);
        }*/
        return allMediaRecents;
    }

    JsonElement serviceLikeStatusByMediaID(String aMediaID) {
        String statusURL = String.format(LIKE_STATUS_BY_MEDIAID_URL, aMediaID);
        OAuthRequest request = new OAuthRequest(Verb.POST, statusURL);
        service.signRequest(accessToken, request);
        Response response = request.send();
        if (response.getCode() != 200) {
            logger.warn(this.getThisUser().getUsername() + " / " + this.getThisUser().getId() + " response.getCode()= " + response.getCode());
        }

        String s = response.getBody();
        Gson gson = new Gson();
        JsonElement element = gson.fromJson(s, JsonElement.class);
        return element;

    }


    @SuppressWarnings("unused")
    List<InstagramStatus> saveStatusJson(JSONArray data) {
        gson = new Gson();
        List<InstagramStatus> result = new ArrayList<InstagramStatus>();
        // the way with morphia + DAO model..
        if (data != null) {
            Iterator<JSONObject> iter = data.iterator();
            while (iter.hasNext()) {
                String i = iter.next().toString();
                //logger.debug(i);
                com.nearfuturelaboratory.humans.instagram.entities.InstagramStatus istatus = null;
                try {
                    istatus = gson.fromJson(i, com.nearfuturelaboratory.humans.instagram.entities.InstagramStatus.class);
                    //ServiceEntry serviceEntry = new ServiceEntry(this.user.getUserID(), this.user.getUsername(), this.user.getServiceName());
                    //istatus.setOnBehalfOf(serviceEntry);
                    result.add(istatus);
                    statusDAO.save(istatus);
                } catch (Exception e) {
                    logger.warn("Weird Instagram element. Not sure. Just skipping until more forensics on the snafu");
                    logger.debug(i);
                    logger.debug("", e);
                }
            }
        } else {
            logger.warn("No data found for " + this.getThisUser().getUsername() + " / " + this.getThisUser().getId());
        }
        return result;
    }


    public boolean localFriendsIsFresh() {
        boolean result = false;
        InstagramFriend friend = followsDAO.findOldestFriendsForUserID(this.getThisUser().getId());

        if (friend == null)
            return false;

        Date d = friend.getLastUpdated();//  .getFriend().getLastUpdated();

        long then = d.getTime();
        long now = new Date().getTime();
        long diff = now - then;
        if (diff < Constants.getLong("FOLLOWS_STALE_TIME")) {
            result = true;
        }

        return result;

    }

    public boolean localFriendsIsFresh(String aUserId) {
        boolean result = false;
        InstagramFriend friend = followsDAO.findOldestFriendsForUserID(aUserId);

        if (friend == null)
            return false;

        Date d = friend.getLastUpdated();//  .getFriend().getLastUpdated();

        long then = d.getTime();
        long now = new Date().getTime();
        long diff = now - then;
        if (diff < Constants.getLong("FOLLOWS_STALE_TIME")) {
            result = true;
        }

        return result;

    }

    public List<InstagramFriend> getFriends() {
        return followsDAO.findFollowsByExactUserID(this.getThisUser().getId());
    }

    protected List<InstagramFriend> getLocalFriendsFor(String aUserID) {
        return followsDAO.findFollowsByExactUserID(aUserID);
    }

    /**
     *
     *
     */
    public void serviceRequestFriends() {
        try {
            serviceRequestFriends(this.getThisUser().getId());
        } catch (BadAccessTokenException e) {
            logger.warn(e);
        } catch (NullPointerException e) {
            logger.warn(e);
        }

    }

    protected List<InstagramFriend> serviceRequestFriends(String aUserID) throws BadAccessTokenException, NullPointerException {
        return serviceRequestFriends(aUserID, true);
    }


    /**
     * @param aUserID
     */
    protected List<InstagramFriend> serviceRequestFriends(String aUserID, boolean save) throws BadAccessTokenException, NullPointerException {
        InstagramUser aUser;
        List<InstagramFriend> result = new ArrayList<InstagramFriend>();

        if (aUserID == null || aUserID.equalsIgnoreCase("self")) {
            aUserID = user.getId();
            // if the user basic isn't fresh for self, then request it and reset ourselves
            if (this.localUserBasicIsFresh() == false) {
                user = serviceRequestUserBasic();
            }
            aUser = user;
        } else {
            if (this.localUserBasicIsFreshForUserID(aUserID) == false) {
                this.serviceRequestUserBasicForUserID(aUserID);
            }
            aUser = this.getLocalUserBasicForUserID(aUserID);
        }

        aUser = this.getLocalUserBasicForUserID(aUserID);

        String followsURL = String.format(FOLLOWS_URL, aUserID);

        OAuthRequest request = new OAuthRequest(Verb.GET, followsURL);
        service.signRequest(accessToken, request);
        Response response = request.send();
        String s = response.getBody();
        Object obj = JSONValue.parse(s);
        JSONObject map = (JSONObject) obj;

        JSONObject pagination = (JSONObject) map.get("pagination");
        //JSONArray allFollows = new JSONArray();
        List<JSONObject> allFollows = new ArrayList<JSONObject>();
        if (pagination == null) {
            logger.warn("No pagination for " + this + " " + this.getThisUser());
        }
        while (pagination != null) {
            List<JSONObject> f = JsonPath.read(map, "data");
            if (f != null) {
                allFollows.addAll(f);
            }
            //allFollows.addAll(data);
            //logger.debug("Adding "+data.size());
            //logger.debug("All Follows now "+allFollows.size());
            String next_url = (String) pagination.get("next_url");
            if (next_url != null) {
                request = new OAuthRequest(Verb.GET, (String) next_url);
                response = request.send();
                s = response.getBody();
                //TODO Error checking
                if (s == null) {
                    logger.error("Null body in the response " + response);
                    break;
                }
                map = (JSONObject) JSONValue.parse(s);
                if (map == null) {
                    logger.error("No pagination in the get follows request " + response + " " + s);
                    break;
                }
                pagination = (JSONObject) map.get("pagination");
            } else {
                break;

            }
        }
//        do {
//            //			JSONArray data = (JSONArray)map.get("data");
//            }
//        } while (pagination != null);
        if (save) {
            logger.debug("Save friends that "+aUser.getUsername()+ "follows");
            saveFollowsJson(allFollows, aUserID/*, aUser*/);
        }
//        else {
//            for(JSONObject friend : allFollows) {
//                logger.debug(friend);
//                InstagramFriend f = new InstagramFriend();
//
//            }
//        }

        return followsDAO.findFollowsByExactUserID(aUserID);
//        return result;
    }


    public void serviceRequestFollowersAsUsers() {
        try {
            serviceRequestFollowersAsUsers(this.getThisUser().getId());
        } catch (BadAccessTokenException e) {
            logger.warn(e);
        } catch (NullPointerException e) {
            logger.warn(e);
        }
    }

    protected List<InstagramUserBriefly> serviceRequestFollowersAsUsersBriefly(String aUserID) throws BadAccessTokenException, NullPointerException {
        return serviceRequestFollowersAsUsersBriefly(aUserID, 0);
    }


    public List<InstagramUserBriefly> serviceRequestFollowersAsUsersBriefly(String aUserID, long throttle_millis) throws BadAccessTokenException, NullPointerException {
        InstagramUser aUser;
        List<InstagramUserBriefly> result = new ArrayList<>();

        if (aUserID == null || aUserID.equalsIgnoreCase("self")) {
            aUserID = user.getId();
            // if the user basic isn't fresh for self, then request it and reset ourselves
            if (this.localUserBasicIsFresh() == false) {
                user = serviceRequestUserBasic();
            }
            //aUser = user;
        } else {
            if (this.localUserBasicIsFreshForUserID(aUserID) == false) {
                this.serviceRequestUserBasicForUserID(aUserID);
            }
        }

        aUser = this.getLocalUserBasicForUserID(aUserID);
        int c = Integer.parseInt(aUser.getFollowedByCount());
        String followedByURL = String.format(FOLLOWED_BY_URL, aUserID);
        logger.info("Getting followers for " + aUser.getUsername() + " out of " + c);
        OAuthRequest request = new OAuthRequest(Verb.GET, followedByURL);
        service.signRequest(accessToken, request);
        Response response = request.send();
        String s = response.getBody();

        if (response.getCode() != 200) {
            logger.warn(aUserID + " / " + this.getThisUser().getId() + " response.getCode()= " + response.getCode());
            logger.warn(response.getMessage());
            //return new ArrayList<InstagramStatus>();
        }

        Object obj = JSONValue.parse(s);
        JSONObject map = (JSONObject) obj;

        JSONObject pagination = (JSONObject) map.get("pagination");
        //JSONArray allFollows = new JSONArray();
        List<JSONObject> allFollowers = new ArrayList<JSONObject>();
        if (pagination == null) {
            logger.warn("No pagination for " + this + " " + this.getThisUser());
        }
        while (pagination != null) {
            List<JSONObject> f = JsonPath.read(map, "data");
            if (f != null) {
                allFollowers.addAll(f);

                logger.debug("Now have " + allFollowers.size() + "/" + c + " (expected). Added " + f.size());
            }
            //allFollows.addAll(data);
            //logger.debug("Adding "+data.size());
            //logger.debug("All Follows now "+allFollows.size());
            String next_url = (String) pagination.get("next_url");
            if (next_url != null) {
                request = new OAuthRequest(Verb.GET, (String) next_url);
                response = request.send();
                s = response.getBody();
                //TODO Error checking
                if (s == null) {
                    logger.error("Null body in the response " + response);
                    break;
                }
                map = (JSONObject) JSONValue.parse(s);
                if (map == null) {
                    logger.error("No pagination in the get follows ('followed by') request " + response + " " + s);
                    break;
                }
                pagination = (JSONObject) map.get("pagination");
            } else {
                break;

            }
            try {
                Thread.sleep(throttle_millis);
            } catch (InterruptedException ie) {
                logger.warn("Problem throttling for " + aUser.getUsername(), ie);
            }
        }
        for (JSONObject j : allFollowers) {
            InstagramUserBriefly iub = gson.fromJson(j.toString(), InstagramUserBriefly.class);
            result.add(iub);
        }
        return result;
    }


    public List<InstagramUser> serviceRequestFollowersAsUsers(String aUserID) throws BadAccessTokenException, NullPointerException {
        {
            InstagramUser aUser;
            List<InstagramUser> result = new ArrayList<InstagramUser>();

            if (aUserID == null || aUserID.equalsIgnoreCase("self")) {
                aUserID = user.getId();
                // if the user basic isn't fresh for self, then request it and reset ourselves
                if (this.localUserBasicIsFresh() == false) {
                    user = serviceRequestUserBasic();
                }
                //aUser = user;
            } else {
                if (this.localUserBasicIsFreshForUserID(aUserID) == false) {
                    this.serviceRequestUserBasicForUserID(aUserID);
                }
            }

            aUser = this.getLocalUserBasicForUserID(aUserID);

            String followedByURL = String.format(FOLLOWED_BY_URL, aUserID);

            OAuthRequest request = new OAuthRequest(Verb.GET, followedByURL);
            service.signRequest(accessToken, request);
            Response response = request.send();
            String s = response.getBody();
            Object obj = JSONValue.parse(s);
            JSONObject map = (JSONObject) obj;

            JSONObject pagination = (JSONObject) map.get("pagination");
            //JSONArray allFollows = new JSONArray();
            List<JSONObject> allFollowers = new ArrayList<JSONObject>();
            if (pagination == null) {
                logger.warn("No pagination for " + this + " " + this.getThisUser());
            }
            while (pagination != null) {
                List<JSONObject> f = JsonPath.read(map, "data");
                if (f != null) {
                    allFollowers.addAll(f);
                }
                //allFollows.addAll(data);
                //logger.debug("Adding "+data.size());
                //logger.debug("All Follows now "+allFollows.size());
                String next_url = (String) pagination.get("next_url");
                if (next_url != null) {
                    request = new OAuthRequest(Verb.GET, (String) next_url);
                    response = request.send();
                    s = response.getBody();
                    //TODO Error checking
                    if (s == null) {
                        logger.error("Null body in the response " + response);
                        break;
                    }
                    map = (JSONObject) JSONValue.parse(s);
                    if (map == null) {
                        logger.error("No pagination in the get follows ('followed by') request " + response + " " + s);
                        break;
                    }
                    pagination = (JSONObject) map.get("pagination");
                } else {
                    break;

                }
            }
//        do {
//            //			JSONArray data = (JSONArray)map.get("data");
//            }
//        } while (pagination != null);
            //           logger.debug("Save followers for " + aUser.getUsername());
            //saveFollowsJson(allFollows, aUserID);
            //saveFollowersJson(allFollowers, aUserID);
            for (JSONObject j : allFollowers) {
                //logger.debug(j);
                InstagramUserBriefly iub = gson.fromJson(j.toString(), InstagramUserBriefly.class);
                InstagramUser friend = this.getLocalUserBasicForUserID(iub.getId());
                if (friend == null || this.localUserBasicIsFreshForUserID(iub.getId()) == false) {
                    friend = this.serviceRequestUserBasicForUserID(iub.getId());
                    if (friend == null) {
                        logger.warn(iub + " is maybe a private/blocked User from whichever user is authenticated in this transaction. (Are you running analytics on someone else's account?");
                        continue;
                    }
                }
                result.add(friend);


            }


            return result;
        }
    }

    //TODO we should delete all the follows first..then add them back?
    //TODO or find the ones we would be deleting in the overlap exclusion?
    //TODO InstagramService passes the id of the user to this method
    protected void saveFollowsJson(List<JSONObject> data, String follower_id) {
        try {
            List<InstagramFriend> new_friends = new ArrayList<InstagramFriend>();
            for (JSONObject j : data) {
                InstagramUserBriefly iub = gson.fromJson(j.toString(), InstagramUserBriefly.class);
                InstagramUser friend = this.getLocalUserBasicForUserID(iub.getId());
                // here's where we update a user if their local user basic is stale..
                if (friend == null || this.localUserBasicIsFreshForUserID(iub.getId()) == false) {
                    friend = this.serviceRequestUserBasicForUserID(iub.getId());
                    if (friend == null) {
                        logger.debug("Private/Blocked User from whichever user is authenticated in this transaction. (Are you running analytics on someone else's account?");
                        continue;
                    }
                    logger.debug("serviceRequestUserBasicForUserID(" + iub.getId() + ")");
                }
                ///ugh.
                InstagramFriend iuf = new InstagramFriend(friend);
                ///iuf.setFollower_id(getThisUser().getId());
                iuf.setFollower_id(follower_id);
                iuf.setFriend_id(iub.getId());
                iuf.setFriend_username(iub.getUsername());
                ///iuf.setFollower(this.getThisUser());
                iuf.setFollower(this.getLocalUserBasicForUserID(follower_id));
                InstagramFriend f = followsDAO.findFollowsByFriendIDForUserID(friend.getId(), follower_id);
                if (f != null) {
                    f.setFriend(friend);
                    f.setFollower(/*this.getThisUser()*/this.getLocalUserBasicForUserID(follower_id));
                    new_friends.add(f);
                } else {
                    new_friends.add(iuf);
                }
            }

            List<InstagramFriend> existing_friends = this.getLocalFriendsFor(follower_id);

            Collection<InstagramFriend> new_friends_to_save = CollectionUtils.subtract(new_friends, existing_friends);
            Collection<InstagramFriend> no_longer_friends = CollectionUtils.subtract(existing_friends, new_friends);

            for (InstagramFriend not_a_friend : no_longer_friends) {
                try {
                    followsDAO.delete(not_a_friend);
                } catch (Exception e) {
                    logger.warn(e);
                    continue;
                }
            }

            for (InstagramFriend is_a_friend : new_friends_to_save) {
                try {
                    followsDAO.save(is_a_friend);
                } catch (Exception e) {
                    logger.warn(e);
                    continue;
                }
            }

            //		Iterator<JSONObject> iter = data.iterator();
        } catch (BadAccessTokenException bate) {
            logger.error(bate);
        }
    }

    public static void serializeToken(Token aToken, InstagramUser aUser) {
        ServiceTokenDAO dao = new ServiceTokenDAO("instagram");
        ServiceToken tokenToSave = dao.findByExactUserID(aUser.getId()); //new ServiceToken();
        if (tokenToSave == null) {
            tokenToSave = new ServiceToken();
        }
        tokenToSave.setToken(aToken);
        tokenToSave.setUser_id(aUser.getId());
        tokenToSave.setUsername(aUser.getUsername());
        tokenToSave.setServicename("instagram");
        dao.save(tokenToSave);
    }

    protected static Token deserializeToken(String aUsername) {
        ServiceTokenDAO dao = new ServiceTokenDAO("instagram");
        ServiceToken st = dao.findByExactUsername(aUsername);
        return st.getToken();
    }


    public static Token deserializeToken(InstagramUser aUser) {
        //Token result = null;
        ServiceTokenDAO dao = new ServiceTokenDAO("instagram");
        ServiceToken serviceToken = dao.findByExactUserId(aUser.getId());
        return serviceToken.getToken();
    }

    public boolean localUserBasicIsFresh() {
        return this.localUserBasicIsFreshForUserID(this.user.getId());
    }

    protected boolean localUserBasicIsFreshForUserID(String aUserID) {
        boolean result = false;
        com.nearfuturelaboratory.humans.instagram.entities.InstagramUser user = this.getLocalUserBasicForUserID(aUserID);

        if (user == null) return false;

        Date d = user.getLastUpdated();

        long then = d.getTime();
        long now = new Date().getTime();
        long diff = now - then;
        if (diff < Constants.getLong("USER_BASIC_STALE_TIME")) {
            result = true;
        }

        return result;

    }

    public boolean localServiceStatusIsNewStatus(String aUserID) {
        boolean result = false;
        com.nearfuturelaboratory.humans.instagram.entities.InstagramStatus most_recent = null;
        DateTime now = new DateTime().withChronology(ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))));
        Date d;
        try {

            most_recent = this.getMostRecentStatusForUserID(aUserID);
            if (most_recent == null) return false;
            d = most_recent.getCreatedAt();

            long then = d.getTime();
            //long now = new Date().getTime();

            long diff = now.getMillis() - then;
            if (diff > Constants.getLong("STATUS_NEWNESS_TIME")) {
                // okay..but when was it last updated?
                DateTime t = new DateTime(most_recent.getLastUpdated());
                diff = now.getMillis() - t.getMillis();
                if (diff < Constants.getLong("STATUS_STALE_TIME")) {
                    result = true;

                }
                //result = true;
            } else {
                result = true;
            }
        } catch (NullPointerException npe) {
            logger.warn("", npe);
            logger.warn("Probably no status at all for userid=" + aUserID + " (" + most_recent + "), so no Farm Fresh Local Status Today");


        } finally {
            if (result == false) return false;
        }
        return result;

    }

    public boolean localServiceStatusIsFresh() {
        return localServiceStatusIsFreshForUserID(this.getThisUser().getId());
    }

    public boolean localServiceStatusIsFreshForUserID(String aUserID) {
        boolean result = false;
        com.nearfuturelaboratory.humans.instagram.entities.InstagramStatus most_recent = null;
        try {

            most_recent = this.getMostRecentStatusForUserID(aUserID);
            Date d = most_recent.getLastUpdated();

            long then = d.getTime();
            long now = new Date().getTime();

            long diff = now - then;
            if (diff < Constants.getLong("STATUS_STALE_TIME")) {
                result = true;
            }
        } catch (NullPointerException npe) {
            logger.warn(npe);
            logger.warn("Probably no status at all for userid=" + aUserID + " (" + most_recent + "), so no Farm Fresh Local Status Today");


        } finally {
            if (result == false) return false;
        }
        return result;
    }

//    public InstagramUser _getLocalUserBasicForUsername(String aUsername) {
//        InstagramUser user = userDAO.findByExactUsername(aUsername);
//        return user;
//    }


    public static InstagramUser getLocalUserBasicForUsername(String aUsername) {
        InstagramUserDAO dao = new InstagramUserDAO();
        InstagramUser user = dao.findByExactUsername(aUsername);
        return user;
    }

    public long getStatusCountForUserID(String userID) {
        long result = statusDAO.getStatusCountForUserID(userID);
        return result;
    }
}
