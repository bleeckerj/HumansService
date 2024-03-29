package com.nearfuturelaboratory.humans.service;

import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import com.nearfuturelaboratory.humans.dao.FoursquareCheckinDAO;
import com.nearfuturelaboratory.humans.dao.FoursquareFriendDAO;
import com.nearfuturelaboratory.humans.dao.FoursquareUserDAO;
import com.nearfuturelaboratory.humans.dao.ServiceTokenDAO;
import com.nearfuturelaboratory.humans.entities.ServiceEntry;
import com.nearfuturelaboratory.humans.entities.ServiceToken;
import com.nearfuturelaboratory.humans.exception.BadAccessTokenException;
import com.nearfuturelaboratory.humans.foursquare.entities.FoursquareCheckin;
import com.nearfuturelaboratory.humans.foursquare.entities.FoursquareFriend;
import com.nearfuturelaboratory.humans.foursquare.entities.FoursquareUser;
import com.nearfuturelaboratory.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.Foursquare2Api;
import org.scribe.exceptions.OAuthConnectionException;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import java.util.Date;
import java.util.List;

public class FoursquareService {

    private static final String FOLLOWS_URL = "https://api.foursquare.com/v2/users/%s/friends/?oauth_token=%s&v=20131006";
    private static final String CHECKINS_URL = "https://api.foursquare.com/v2/users/self/checkins/?oauth_token=%s&v=20131006&sort=newestfirst";
    private static final String USER_URL = "https://api.foursquare.com/v2/users/%s?oauth_token=%s&v=20131006";

    final static Logger logger = LogManager.getLogger(com.nearfuturelaboratory.humans.service.FoursquareService.class);
    private static String apiKey = Constants.getString("FOURSQUARE_API_KEY");//"MKGJ3OZYTDNZAI5ZMROF3PAMAUND0ZO2HYRTZYXHIIR5TW1Q";
    private static String apiSecret = Constants.getString("FOURSQUARE_API_SECRET");//"2G0DUIFCFAWBH1WPIYBUDQMESKRLFLGY5PHXY0BJNBE1MMN4";
    private static String callbackURL = Constants.getString("FOURSQUARE_CALLBACK_URL");
    private Token accessToken;
    protected FoursquareUser user;
    private OAuthService service;

    @Override
    public String toString() {
        return "FoursquareService{" +
                "user=" + user +
                '}';
    }

    protected FoursquareCheckinDAO checkinDAO;
    protected FoursquareUserDAO userDAO;
    protected FoursquareFriendDAO followsDAO;
    protected ServiceTokenDAO tokenDAO;
    protected Gson gson;

    public FoursquareService() {
        //db = MongoUtil.getMongo().getDB("foursquare");

        checkinDAO = new FoursquareCheckinDAO();
        checkinDAO.ensureIndexes();

        userDAO = new FoursquareUserDAO();
        userDAO.ensureIndexes();

        followsDAO = new FoursquareFriendDAO();
        followsDAO.ensureIndexes();

        tokenDAO = new ServiceTokenDAO("foursquare");
        tokenDAO.ensureIndexes();

        //gson = new GsonBuilder().setDateFormat("EEE MMM dd HH:mm:ss Z yyyy").serializeNulls().create();
        gson = new Gson();
    }

    public static FoursquareService createFoursquareServiceOnBehalfOfUserID(String aUserID)  throws BadAccessTokenException
    {
        FoursquareService result;//  = new FoursquareService(token);
        Token token = null;
        FoursquareUser user = getLocalUserBasicForUserID(aUserID);
        if(user == null) {
            logger.warn("null token. trying to find one by userid "
                    + aUserID);
            token = FoursquareService.deserializeTokenByUserID(aUserID);
            result = new FoursquareService(token);
            user = result.serviceRequestUserBasic();
            if(user == null) {
                throw new BadAccessTokenException("The user for Foursquare User "+aUserID+" is null. The access token probably does not exist. accesstoken="+token);
            }
        } else {
            token = FoursquareService.deserializeToken(user);
            result = new FoursquareService(token);
        }
        if(token == null) {
            throw new BadAccessTokenException(user+" The access token for Foursquare User "+aUserID+" is null. It probably does not exist.");

        }
        result.user = user;
        /*
        FoursquareUser offline_user = result.serviceRequestUserBasic();

        if(offline_user == null) {
            result.user = user;
        } else {
            result.user = offline_user;
        }
        */

        return result;
    }


    public FoursquareService(Token aAccessToken) {
        this();
        try {
            this.accessToken = aAccessToken;
            service = new ServiceBuilder()
                    .provider(Foursquare2Api.class)
                    .apiKey(apiKey)
                    .apiSecret(apiSecret)
                    .callback(callbackURL)
                    .build();
        } catch(Exception e) {
            logger.warn("Uh Oh..", e);
            logger.warn(apiKey);
            logger.warn(apiSecret);
            logger.warn(callbackURL);

        }

    }

    private static FoursquareUser getLocalUserBasicForUserID(String aUserID) {
        FoursquareUser result = null;
        FoursquareUserDAO dao = new FoursquareUserDAO();
        result = dao.findByExactUserID(aUserID);
        return result;
    }


    public FoursquareUser serviceRequestUserBasicForUserID(String aUserID) {
        // break up aCodedUsername
        //String id = parts[0];
        FoursquareUser fuser = null;
        try {
            logger.debug(USER_URL+" "+aUserID+" "+accessToken);
            String userURL = String.format(USER_URL, aUserID, accessToken.getToken());
            OAuthRequest request = new OAuthRequest(Verb.GET, userURL );
            service.signRequest(accessToken, request);
            Response response = request.send();
            //TODO Error checking
            String s = response.getBody();
            JSONObject obj = (JSONObject)JSONValue.parse(s);
            JSONObject respJSON = (JSONObject) obj.get("response");
            JSONObject respUser = (JSONObject)respJSON.get("user");

            fuser = gson.fromJson(respUser.toJSONString(), FoursquareUser.class);

            userDAO.save(fuser);
        } catch(OAuthConnectionException e) {
            // perhaps we're offline?
            logger.warn("We may be offline");
            fuser = userDAO.findByExactUserID(aUserID);
            if(fuser == null) {
                logger.warn("And in this case we do not have a db version of FoursquareUser="+aUserID+" and "+this.getThisUser());
            }
        }
        return fuser;
    }




    /**
     * This will go to the service and get "self" for whoever's accessToken we have
     * @return
     */
    public FoursquareUser serviceRequestUserBasic() {
        //logger.debug("user here is = "+this.getThisUser());
        this.user = serviceRequestUserBasicForUserID("self");
        return this.getThisUser();
    }


    public boolean localServiceStatusIsFresh() {
        boolean result = false;
        if(this.getLatestCheckin() == null) {
            return false;
        }
        Date d = this.getLatestCheckin().getLastUpdated();
        long then = d.getTime();
        long now = new Date().getTime();
        long diff = now - then;
        if(diff < Constants.getLong("STATUS_STALE_TIME")) {
            result = true;
        }
        return result;
    }

    public boolean localUserBasicIsFreshForUserID(String aUserID) {
        boolean result = false;
        FoursquareUser user = FoursquareService.getLocalUserBasicForUserID(aUserID);
        if(user == null) return false;

        Date d = user.getLastUpdated();

        long then = d.getTime();
        long now = new Date().getTime();
        long diff = now - then;
        if(diff < Constants.getLong("USER_BASIC_STALE_TIME")) {
            result = true;
        }
        return result;
    }

    protected FoursquareUser getLocalUserBasicForUserID1(String aUserID) {
        FoursquareUser result = null;
        result = userDAO.findByExactUserID(aUserID);
        return result;
    }

    public FoursquareCheckin getMostRecentCheckin() {
        return checkinDAO.findMostRecentCheckin(this.getThisUser()
                .getId());
    }

    public FoursquareCheckin getOldestCheckin() {
        return checkinDAO.findOldestCheckin(this.getThisUser().getId());
    }


    /*	public void getLatestCheckins() {
        // get the last check in? get the ones since them somehow? or..who cares?
    }
     */
    public List<FoursquareCheckin> getCheckins() {
        return checkinDAO.findByExactUserID(this.getThisUser().getId());
    }

    public List<FoursquareCheckin> getCheckinsForUserID(String aUserID) {
        return checkinDAO.findByExactUserID(aUserID);
    }

    public void serviceRequestCheckins() {
        serviceRequestCheckins(0);
    }

    public void serviceRequestLatestCheckins() {
        FoursquareCheckin lastLocalCheckin = checkinDAO.findMostRecentCheckin(user.getId());

        if(lastLocalCheckin != null) {
            Long lastLocalCheckinTime = lastLocalCheckin.getCreatedAt();
            serviceRequestCheckins(lastLocalCheckinTime);
        } else {
            serviceRequestCheckins(-1);
        }

    }


    public long getStatusCountForUserID(String aUserID)
    {
        long result = checkinDAO.getStatusCountForUserID(aUserID);
        return result;
    }
    /**
     *  You can only get checkins for "self", not for anyone
     */
    protected void serviceRequestCheckins(long afterTimeStamp)
    {
        //List<JSONObject> checkinsAll;

        JSONArray checkinsAll;
        String checkinsURL = String.format(CHECKINS_URL, accessToken.getToken());
        OAuthRequest request = new OAuthRequest(Verb.GET, checkinsURL );
        request.addQuerystringParameter("limit", "250");
        if(afterTimeStamp > -1) {
            request.addQuerystringParameter("afterTimestamp", String.valueOf(afterTimeStamp));
        }
        service.signRequest(accessToken, request);
        Response response = request.send();
        //TODO Error checking
        String s = response.getBody();
        if(response.getCode() != 200 || s == null || s.isEmpty()) {
            logger.warn("weird response " + response);
            logger.warn("response code "+response.getCode()+" is not 200 for "+this.getThisUser()+" "+this.getThisUser().getUserID());
            return;
        }
        JSONObject obj = (JSONObject)JSONValue.parse(s);
        JSONObject checkins = JsonPath.read(obj, "response.checkins");
        //TODO no error checking..fix that
        int total_checkins = Integer.parseInt(JsonPath.read(checkins, "count").toString());
        JSONArray items = (JSONArray)checkins.get("items");
        int items_count = items.size();


        checkinsAll = JsonPath.read(obj, "response.checkins.items");
        Date last = new Date();
        last.setTime(afterTimeStamp);
        if(items_count < 250) {
            logger.info("For user_id "+this.getThisUser().getId()+" found "+checkinsAll.size()+" checkins since " + last);
            saveCheckins(checkinsAll);
        } else {
            //TODO only get latest checkins??
            // this is good code - it just gets all a users checkins, so i need to think about how to use this/put it
            int offset = 0;
            while(offset < total_checkins) {
                offset+=items.size();
                request = new OAuthRequest(Verb.GET, checkinsURL );
                request.addQuerystringParameter("limit", "250");
                request.addQuerystringParameter("offset", String.valueOf(offset));
                service.signRequest(accessToken, request);
                response = request.send();
                s = response.getBody();
                obj = (JSONObject)JSONValue.parse(s);
                try {
                    items = JsonPath.read(obj, "response.checkins.items");
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error(e);
                    logger.error(obj);
                }
                if(items != null) {
                    checkinsAll.addAll(items);
                }
                if(items != null && items.size() < 1) {
                    break;
                }
            }

            logger.info("For user_id "+this.getThisUser().getId()+" found "+checkinsAll.size()+" checkins since " + last);
            logger.warn("You probably only want to get latest checkins..");
            saveCheckins(checkinsAll);
        }
    }


    public void saveCheckins(JSONArray data) {
        for(int i=0; i<data.size(); i++) {
            String s = data.get(i).toString();
            JSONObject o = (JSONObject)JSONValue.parse(s);
            FoursquareCheckin checkin = gson.fromJson(o.toJSONString(), FoursquareCheckin.class);
            checkin.setUserID(this.getThisUser().getId());
            checkin.setUser(this.getThisUser());

            ServiceEntry serviceEntry = new ServiceEntry(this.user.getUserID(), this.user.getUsername(), this.user.getServiceName());
            checkin.setOnBehalfOf(serviceEntry);

            checkinDAO.save(checkin);
        }
    }

    public void serviceRequestFriends() {
        List<JSONObject> follows;
        String userURL = String.format(FOLLOWS_URL, "self", accessToken.getToken());
        OAuthRequest request = new OAuthRequest(Verb.GET, userURL );
        request.addQuerystringParameter("limit", "500");
        service.signRequest(accessToken, request);
        Response response = request.send();
        String s = response.getBody();
        JSONObject obj = (JSONObject)JSONValue.parse(s);
        JSONObject friends = JsonPath.read(obj, "response.friends");
        //TODO no error checking..fix that


        int count = Integer.parseInt(JsonPath.read(friends, "count").toString());
        JSONArray items = (JSONArray)friends.get("items");
        int items_count = items.size();

        follows = JsonPath.read(obj, "response.friends.items");
        int offset = 0;
        while(offset < count) {
            offset+=follows.size();
            request = new OAuthRequest(Verb.GET, userURL );
            request.addQuerystringParameter("limit", "500");
            request.addQuerystringParameter("offset", String.valueOf(offset));
            service.signRequest(accessToken, request);
            response = request.send();
            s = response.getBody();
            obj = (JSONObject)JSONValue.parse(s);
            items = JsonPath.read(obj, "response.friends.items");
            if(items != null) {
                follows.addAll(items);
            }
        }
        saveFollowsJson(follows, this.getThisUser().getId());
    }

    //TODO why is this a List<JSONObject> unlike the other services saveFollowsJson methods??
    //TODO we need to delete first..
    protected void saveFollowsJson(List<JSONObject> data, String follower_id) {
        //JSONArray result = new JSONArray();
        //List<FoursquareUser>friends1 = new ArrayList<FoursquareUser>();
        for(int i=0; i<data.size(); i++) {
            JSONObject o = (JSONObject)JSONValue.parse(data.get(i).toJSONString());
            FoursquareUser friend = gson.fromJson(o.toJSONString(), FoursquareUser.class);
            //			if(friendLocal == null) {
            userDAO.save(friend);
            //friends1.add(friend);
            //			}

            FoursquareFriend me = followsDAO.findForUserIDFriendID(this.getThisUser().getId(), friend.getId());
            if(me == null) {
                me = new FoursquareFriend();
                me.setUser_id(this.getThisUser().getId());
                me.setUser(this.getThisUser());

                me.setFriend_id(friend.getId());
                me.setFriend(friend);
                followsDAO.save(me);

            } else {
                me.setUser(this.getThisUser());
                followsDAO.updateLastUpdated(me);
            }
        }

    }

    public boolean localFriendsIsFresh() {
        boolean result = false;

        FoursquareFriend friend = followsDAO.findOldestFriendByExactUserID(this.getThisUser().getId());

        if (friend == null)
            return false;

        Date d = friend.getFriend().getLastUpdated();

        long then = d.getTime();
        long now = new Date().getTime();
        long diff = now - then;
        if (diff < Constants.getLong("FOLLOWS_STALE_TIME")) {
            result = true;
        }

        return result;

    }

    public List<FoursquareFriend> getFriends() {
        return followsDAO.findForUserID(this.getThisUser().getId());
    }

    public FoursquareUser getThisUser() {
        return user;
    }


    /**
     * Weird bootstrap method needed while migrating. We really should not have a token
     * if we don't have a local user in the database, but we did when we migrated Tokens from the
     * old filesystem database and the database had no users in it yet. Really, when you
     * save a new user you also save the user basic with the token
     *
     * @param aUserID
     */
    private static Token deserializeTokenByUserID(String aUserID) throws BadAccessTokenException {
        ServiceTokenDAO dao = new ServiceTokenDAO("foursquare");
        ServiceToken st = dao.findByExactUserID(aUserID);
        if(st == null) {
            FoursquareUserDAO userDAO = new FoursquareUserDAO();
            FoursquareUser user = userDAO.findByExactUserID(aUserID);
            logger.error("No Token for Foursquare userid:"+aUserID+" ("+user+") maybe a data error in the token.");

            throw new BadAccessTokenException("No Token for Foursquare userid:"+aUserID+" ("+user+") maybe a data error in the token.");
        }
        return st.getToken();
    }

    public static Token deserializeToken(FoursquareUser aUser) {
        //Token result = null;
        ServiceTokenDAO dao = new ServiceTokenDAO("foursquare");
        ServiceToken serviceToken = dao.findByExactUserId( aUser.getId() );
        if(serviceToken == null) {
            FoursquareUserDAO userDAO = new FoursquareUserDAO();
            logger.error("No Token for Foursquare user ("+aUser+") maybe a data error in the token.");

            return null;

        }
        return serviceToken.getToken();
    }

    public static void serializeToken(Token aToken, FoursquareUser aUser) {
        ServiceTokenDAO dao = new ServiceTokenDAO("foursquare");
        ServiceToken tokenToSave = dao.findByExactUserID(aUser.getId()); //new ServiceToken();
        if(tokenToSave == null) {
            tokenToSave = new ServiceToken();
        }
        tokenToSave.setToken(aToken);
        tokenToSave.setUser_id(aUser.getId());
        tokenToSave.setUsername(aUser.getFirstName()+"_"+aUser.getLastName());
        tokenToSave.setServicename("foursquare");
        dao.save(tokenToSave);
    }



    public FoursquareCheckin getLatestCheckin() {
        return checkinDAO.findMostRecentCheckin(user.getId());
    }

    public String getDerivedUsername() {
        return getThisUser().getFirstName()+"_"+getThisUser().getLastName();
    }




}
