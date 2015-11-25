package com.nearfuturelaboratory.humans.service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.jayway.jsonpath.JsonPath;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
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
import org.joda.time.*;
import org.joda.time.chrono.CopticChronology;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.Morphia;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by julian on 11/23/15.
 */
public class InstagramAnalyticsService extends InstagramService {

    private static final String USER_SEARCH_URL = "https://api.instagram.com/v1/users/search?q=%s&count=%s";


//    private OAuthService service;
//    private Token accessToken;

    final static Logger logger = LogManager.getLogger(com.nearfuturelaboratory.humans.service.InstagramAnalyticsService.class);

    public InstagramAnalyticsService() {
        db = MongoUtil.getMongo().getDB("instagram-analytics-service");

        statusDAO = new InstagramStatusDAO();
        statusDAO.ensureIndexes();

        userDAO = new InstagramUserDAO("instagram-analytics-service");
        userDAO.ensureIndexes();

        followsDAO = new InstagramFriendsDAO();
        followsDAO.ensureIndexes();

//    tokenDAO = new ServiceTokenDAO("instagram");
//    tokenDAO.ensureIndexes();

        gson = new Gson();
    }

    //TODO Change this super ridiculous constructor. Should all be factory methods like above.
    public InstagramAnalyticsService(Token aAccessToken) {
        super(aAccessToken);
        accessToken = aAccessToken;
        service = new ServiceBuilder()
                .provider(InstagramApi.class)
                .apiKey(Constants.getString("INSTAGRAM_API_KEY"))
                .apiSecret(Constants.getString("INSTAGRAM_API_SECRET"))
                .callback(Constants.getString("INSTAGRAM_CALLBACK_URL"))
                .scope("basic,likes")
                .build();
        //		user = this.serviceRequestUserBasic();
        db = MongoUtil.getMongo().getDB("instagram-analytics-service");

        statusDAO = new InstagramStatusDAO();
        statusDAO.ensureIndexes();

        userDAO = new InstagramUserDAO("instagram-analytics-service");
        userDAO.ensureIndexes();

        followsDAO = new InstagramFriendsDAO();
        followsDAO.ensureIndexes();
    }

    public static InstagramAnalyticsService createServiceOnBehalfOfUsername(String aUsername) throws BadAccessTokenException {
        InstagramAnalyticsService result;
        Token token;
        InstagramUser user = InstagramAnalyticsService.getLocalUserBasicForUsername(aUsername);
        if (user == null) {
            logger.warn("null token. trying to find one by username");
            token = InstagramService.deserializeToken(aUsername);
            result = new InstagramAnalyticsService(token);
            user = result.serviceRequestUserBasic();

        } else {
            token = InstagramAnalyticsService.deserializeToken(user);
            result = new InstagramAnalyticsService(token);
        }
        if (token == null) {
            throw new BadAccessTokenException("The access token for Instagram User " + aUsername + " is null. It probably does not exist.");
        }

        result.user = user;
        return result;
    }


    public List<InstagramUser> usersSearch(String aUsername) throws BadAccessTokenException {
        JSONObject aUser;
        List<InstagramUser> result = new ArrayList<InstagramUser>();
        InstagramUser iuser;
        String userSearchURL = String.format(USER_SEARCH_URL, aUsername, "1");
        OAuthRequest request = new OAuthRequest(Verb.GET, userSearchURL);
        service.signRequest(accessToken, request);
        Response response = request.send();
        String s = response.getBody();
        Object obj = JSONValue.parse(s);
        JSONObject map = (JSONObject) obj;
        JSONArray a = (JSONArray) map.get("data");
        //JSONObject pagination = (JSONObject) map.get("pagination");
        try {
            aUser = (JSONObject) a.get(0);

            //saveUserBasicJson(aUser);
            iuser = gson.fromJson(aUser.toString(),
                    com.nearfuturelaboratory.humans.instagram.entities.InstagramUser.class);
            iuser = this.serviceRequestUserBasicForUserID(iuser.getUserID());
            //return iuser;
            //saveUserBasicJson(iuser);
            //saveRootUser(iuser);
        } catch (Exception e) {
            logger.error("Bad response for " + aUsername + " " + this.getThisUser() + " " + response.getBody(), e);
            if (response.getCode() == 400 && response.getBody().contains("OAuthAccessTokenException")) {
                throw new BadAccessTokenException("Bad response for " + aUsername + " " + this.getThisUser() + " " + response.getBody());
            }
            return null;
        }

        result.add(iuser);

        return result;
    }


    protected Key<InstagramUser> saveUserBasicJson(InstagramUser aUser) {
//        com.nearfuturelaboratory.humans.instagram.entities.InstagramUser iuser = gson.fromJson(aUserJson.toString(),
//                com.nearfuturelaboratory.humans.instagram.entities.InstagramUser.class);
        return userDAO.save(aUser);


    }


    protected JsonObject getRootUser(InstagramUser aUser) {

        DateTime now = new DateTime(ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))));
        DateTimeFormatter fmt_short = DateTimeFormat.forPattern("MMddYY-HHmm");
        JsonObject rootUserJson = new JsonObject();

        JsonObject rootUserMeta = new JsonObject();
        rootUserMeta.addProperty("snapshot-date", DateTimeFormat.forPattern("MM/dd/yy HH:mm:ss").print(now));
        rootUserMeta.addProperty("snapshot-week-of-year", DateTimeFormat.forPattern("ww-yyyy").print(now));
        //rootUserMeta.addProperty("other-meta", "something");

        String s = gson.toJson(aUser);
        JsonElement e = gson.toJsonTree(aUser);
        rootUserJson.add(aUser.getUsername(), e.getAsJsonObject());

        return rootUserJson;

    }

    protected JsonElement getInstagramUserStatus(InstagramUser aUser) {
        JsonElement e;
        List<InstagramStatus> status = this.serviceRequestStatusForUserIDToMonthsAgo(aUser.getUserID(), 3);
        //Object statusJson = (Object)JSON.serialize(status);
        JsonElement status_analytics = parseInstagramUserStatusAnalytics(status);
        JsonObject f = new JsonObject();
        f.add("status-analytics", status_analytics);

        e = gson.toJsonTree(status);
        f.add("status", e);
//        f.getAsJsonObject();
        return f.getAsJsonObject();
    }

    protected JsonElement parseInstagramUserStatusAnalytics(List<InstagramStatus> aStatusList) {
        // average caption size
        float avg_caption_length = 0;
        // mentions per caption

        // average likes
        float avg_likes = 0;
        float likes_count = 0;
        int max_likes_count = 0;
        int min_likes_count = Integer.MAX_VALUE;

        // average comments
        float avg_comments = 0;
        float comments_count = 0;
        int max_comments_count = 0;
        int min_comments_count = Integer.MAX_VALUE;

        float avg_mentions = 0;
        float mentions_count = 0;

        float avg_tags = 0;
        float tags_count = 0;

        // top 20 tags
        //ArrayList<String> top_tags = new ArrayList<String>(20);
        List<Map.Entry<String, Number>> l_top_tags;// = new ArrayList<>(m_top_tags.entrySet());

        // top 20 most mentions
        //ArrayList<String> top_mentions = new ArrayList<String>(20);
        List<Map.Entry<String, Number>> l_top_mentions;

        // top 20 likers
        ArrayList<String> top_likers = new ArrayList<String>(20);
        // top 20 commenters
        ArrayList<String> top_commenters = new ArrayList<String>(20);


        JsonArray result = new JsonArray();



        Map<String, Number> m_top_tags = new HashMap<String, Number>();
        Map<String, Number> m_top_mentions = new HashMap<String, Number>();

        for(InstagramStatus tmp : aStatusList) {
            // caption size

            // likes count
            likes_count+=tmp.getLikes();

            // max likes
            if(tmp.getLikes() > max_likes_count) {
                max_likes_count = tmp.getLikes();
            }
            // min likes
            if(tmp.getLikes() < min_likes_count) {
                min_likes_count = tmp.getLikes();
            }

            // comments count
            comments_count+=tmp.getCommentsCount();
            // max comments
            if(tmp.getCommentsCount() > max_comments_count) {
                max_comments_count = tmp.getCommentsCount();
            }
            // min comments
            if(tmp.getCommentsCount() < min_comments_count) {
                min_comments_count = tmp.getCommentsCount();
            }

            // track top tags
            List<String> tags = tmp.getTags();
            for(String tags_tmp : tags) {
                tags_tmp = new String("#"+tags_tmp);
                if(m_top_tags.containsKey(tags_tmp)) {
                    Number count = m_top_tags.get(tags_tmp);
                    int new_count = count.intValue();
                    new_count++;
                    m_top_tags.remove(tags_tmp);
                    m_top_tags.put(tags_tmp, new Integer(new_count));

                } else {
                    m_top_tags.put(tags_tmp, 1);
                }
            }

            // now capture mentions in the caption text
            String caption = tmp.getCaptionText();

            Pattern pattern = Pattern.compile("(\\@\\S+)");
            Matcher matcher = pattern.matcher(caption);

           // int v = matcher.groupCount();
            int match_count = 0;
            while(matcher.find()) {
                match_count++;
                String username_mentioned = matcher.group(1);
                if(m_top_mentions.containsKey(username_mentioned)) {
                    Number count = m_top_mentions.get(username_mentioned);
                    int new_count = count.intValue();
                    new_count++;
                    m_top_mentions.remove(username_mentioned);
                    m_top_mentions.put(username_mentioned, new_count);
                } else {
                    m_top_mentions.put(username_mentioned, 1);
                }
                //logger.debug(s);
            }

            //matcher.group();
            // mentions count
            mentions_count+=match_count;

            // tags count
            tags_count+=m_top_tags.size();

        }
        JsonObject o = new JsonObject();
        JsonObject c = new JsonObject();
        // likes average first
        avg_likes = likes_count/aStatusList.size();
        o.addProperty("avg-likes", new Float(avg_likes));
        o.addProperty("min-likes", new Integer(min_likes_count));
        o.addProperty("max-likes", new Integer(max_likes_count));

        // comments count average
        avg_comments = comments_count/aStatusList.size();
        o.addProperty("avg-comments", new Float(avg_comments));
        o.addProperty("min-comments", new Integer(min_comments_count));
        o.addProperty("max-comments", new Integer(max_comments_count));

        // mentions average
        avg_mentions = mentions_count/aStatusList.size();
        o.addProperty("avg-mentions", new Float(avg_mentions));

        // tags average
        avg_tags = tags_count/aStatusList.size();
        o.addProperty("avg-tags", new Float(avg_tags));
        c.add("engagement-meta", o);


        l_top_tags = new ArrayList<>(m_top_tags.entrySet());
        Collections.sort(l_top_tags, new Comparator<Map.Entry<String, Number>>() {
            @Override
            public int compare(Map.Entry<String, Number> o1, Map.Entry<String, Number> o2) {
                //return (o1.getValue())(o2.getValue());
                return Integer.compare(o2.getValue().intValue(), o1.getValue().intValue());
            }
        });
        // a_top_tags is an array of top tags sorted
        JsonArray a_top_tags = new JsonArray();
        for(Map.Entry<String, Number> temp : l_top_tags){
            JsonObject j = new JsonObject();
            j.addProperty(temp.getKey(), temp.getValue());
            a_top_tags.add(j);
        }
        c.add("top-tags", a_top_tags);



        l_top_mentions = new ArrayList<>(m_top_mentions.entrySet());
        Collections.sort(l_top_mentions, new Comparator<Map.Entry<String, Number>>() {
            @Override
            public int compare(Map.Entry<String, Number> o1, Map.Entry<String, Number> o2) {
                return Integer.compare(o2.getValue().intValue(), o1.getValue().intValue());
            }
        });
        JsonArray a_top_mentions = new JsonArray();
        for(Map.Entry<String, Number> temp : l_top_mentions) {
            JsonObject j = new JsonObject();
            j.addProperty(temp.getKey(), temp.getValue());
            a_top_mentions.add(j);
        }

        c.add("top-mentions", a_top_mentions);
        result.add(c);

//        //JsonElement j_top_tags = gson.toJson(l_top_tags);
//        String s = gson.toJson(l_top_tags);
//        JsonElement x = gson.fromJson(s, JsonElement.class);
//        //result.add(j_top_tags);
        return result;
    }


    @Deprecated
    protected void saveRootUser(InstagramUser aUser) {
        DB foo = MongoUtil.getMongo().getDB("instagram-analytics");
        DBCollection rootUser = foo.getCollection("root_users");
        //rootUser.drop();
        DateTime now = new DateTime(ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))));
        DateTimeFormatter fmt_short = DateTimeFormat.forPattern("MMddYY-HHmm");

        rootUser = rootUser.getCollection(aUser.getUsername() + "_" + fmt_short.print(now)+"_" + aUser.getUserID());
        rootUser.drop();

        JsonObject rootUserJson = new JsonObject();


        JsonObject rootUserMeta = new JsonObject();
        rootUserMeta.addProperty("snapshot-date", DateTimeFormat.forPattern("MM/dd/yy HH:mm:ss").print(now));
        rootUserMeta.addProperty("snapshot-week-of-year", DateTimeFormat.forPattern("ww-yyyy").print(now));
        //rootUserMeta.addProperty("other-meta", "something");

        String s = gson.toJson(aUser);
        JsonElement e = gson.toJsonTree(aUser);
        rootUserJson.add(aUser.getUsername(), e.getAsJsonObject());

        // get all status and capture engagement metrics
        JsonObject bar = new JsonObject();
        bar.addProperty("foo", "bar");
        bar.addProperty("comments-per-post", 15);
        bar.addProperty("likes-per-post", 300);
        rootUserJson.add("engagement-meta", bar);

        List<InstagramStatus> status = this.serviceRequestStatusForUserIDToMonthsAgo(user.getUserID(), 3);
        //Object statusJson = (Object)JSON.serialize(status);
        e = gson.toJsonTree(status);
        rootUserJson.add("status", e);

        JsonObject baz = new JsonObject();
        JsonElement x = new JsonPrimitive("Someone Else");
        //       baz.addProperty("key-follower", "foomanchu");
        baz.add("key-follower", x);

        rootUserJson.add("key-followers", baz);


        DBObject obj = (DBObject) JSON.parse(rootUserJson.toString());

        rootUser.save(obj);

        //rootUser = rootUser.getCollection("bar");
//        String x = bar.toString();
//        rootUser.save((DBObject) JSON.parse(x));
    }
}
