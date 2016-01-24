package com.nearfuturelaboratory.humans.service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.jayway.jsonpath.JsonPath;
import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.result.DeleteResult;
import com.nearfuturelaboratory.humans.exception.UsernameNotFoundException;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramFriend;
import org.bson.Document;
import com.mongodb.Block;
import com.mongodb.util.JSON;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Indexes.descending;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.orderBy;
import static java.util.Arrays.asList;

import com.nearfuturelaboratory.humans.dao.InstagramFriendsDAO;
import com.nearfuturelaboratory.humans.dao.InstagramStatusDAO;
import com.nearfuturelaboratory.humans.dao.InstagramUserDAO;
import com.nearfuturelaboratory.humans.exception.BadAccessTokenException;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramStatus;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramUser;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramUserBriefly;
import com.nearfuturelaboratory.humans.serviceapi.InstagramApi;
import com.nearfuturelaboratory.humans.util.MongoUtil;
import com.nearfuturelaboratory.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.*;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mongodb.morphia.Morphia;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
//import org.w3c.dom.Document;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by julian on 11/23/15.
 */
public class InstagramAnalyticsService extends InstagramService {

    private static final String USER_SEARCH_URL = "https://api.instagram.com/v1/users/search?q=%s&count=%s";
    //protected static final DateTime now = new DateTime(ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))));

    public static String ONE_DAY = "P1D";
    public static Period ONE_DAY_P = new Period().withDays(1);
    public static String SEVEN_DAYS = "P7D";
    public static String ONE_MONTH = "P1M";
    public static String TWO_DAYS = "P2D";
    public static String THREE_DAYS = "P3D";
    public static Period THREE_DAY_P = new Period().withDays(3);
    public static String FIVE_DAYS = "P5D";
    public static String TEN_DAYS = "P10D";
    public static String FIFTEEN_DAYS = "P15D";
    public static DateTimeFormatter short_date_fmt = DateTimeFormat.forPattern("MMddyy");


//    private OAuthService service;
//    private Token accessToken;

    final static Logger logger = LogManager.getLogger(com.nearfuturelaboratory.humans.service.InstagramAnalyticsService.class);

    public InstagramAnalyticsService() {
        db = MongoUtil.getMongo().getDB("instagram-analytics");
        database = MongoUtil.getMongo().getDatabase("instagram-analytics");

        statusDAO = new InstagramStatusDAO();
        statusDAO.ensureIndexes();

        userDAO = new InstagramUserDAO("instagram-analytics");
        userDAO.ensureIndexes();

        followsDAO = new InstagramFriendsDAO("instagram-analytics");
        followsDAO.ensureIndexes();

//    tokenDAO = new ServiceTokenDAO("instagram");
//    tokenDAO.ensureIndexes();

        gson = new Gson();
        super.setDBName("instagram-analytics");
    }

    public void setDBClient(MongoClient _client) {
        client = _client;
        database = client.getDatabase("instagram-analytics");
        db = client.getDB("instagram-analytics");

        super.statusDAO = new InstagramStatusDAO(client, new Morphia(), "instagram");
        super.statusDAO.ensureIndexes();

        /*
        userDAO = new InstagramUserDAO(client, new Morphia(), "instagram-analytics");
        userDAO.ensureIndexes();

        followsDAO = new InstagramFriendsDAO(client, new Morphia(), "instagram-analytics");
        followsDAO.ensureIndexes();
        */
        gson = new Gson();
        //super.client = _client;
        //super.setDBName("instagram-analytics");
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
        client = MongoUtil.getMongo();
        db = client.getDB("instagram-analytics");
        database = client.getDatabase("instagram-analytics");

        statusDAO = new InstagramStatusDAO();
        statusDAO.ensureIndexes();

        userDAO = new InstagramUserDAO("instagram-analytics");
        userDAO.ensureIndexes();

        followsDAO = new InstagramFriendsDAO("instagram-analytics");
        followsDAO.ensureIndexes();
    }

    static InstagramAnalyticsService analyticsService = null;

    public static InstagramAnalyticsService createServiceOnBehalfOfUsername(String aUsername) throws BadAccessTokenException {
        if(analyticsService != null) {
            return analyticsService;
        }
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
        analyticsService = result;
        return result;
    }

    public List<Document> getAllStatusForDateOrdered(DateTime date)
    {
        //DateTimeFormatter f = DateTimeFormat.forPattern("MMddyy");
        //List<InstagramStatus> statusOrdered = new ArrayList<>();
        List<String> collectionNames = this.getListOfInstagramAnalyticsCollections();
        collectionNames.removeIf(s -> s.contains("_snapshot_counts") || s.equalsIgnoreCase("user") || s.contains("system.indexes"));
        //List<Document> docs = this.getListOfInstagramAnalyticsDocumentsByDayOfYear(collectionNames, 333);
        List<Document> docs = getListOfInstagramAnalyticsDocumentsByDate(collectionNames, short_date_fmt.print(date), ONE_DAY_P);
        List<Document> result = new ArrayList<>();
        docs.forEach((Consumer<? super Document>) d -> {
            //logger.debug(d.get("analytics"));
            Document x = (Document)d.get("analytics");
            ArrayList<Document> a;
            a = (ArrayList<Document>)x.get("status-json");
            result.addAll(a);
           // logger.debug(result.size());
        });
      // List<Document> r = result.subList(0,8);
        Collections.sort(result, (o1, o2) -> Integer.compare(o1.getInteger("created_time"), o2.getInteger("created_time")));

        result.stream().filter(distinctByKey(p -> p.getString("id")));


        return result;
    }


    public static <T> Predicate<T> distinctByKey(Function<? super T,Object> keyExtractor) {
        Map<Object,Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    protected static InstagramUser staticGetLocalUserBasicForUserID(String aUserID) {
        InstagramUserDAO dao = new InstagramUserDAO("instagram-analytics");
        dao.ensureIndexes();
        InstagramUser result = dao.findByExactUserID(aUserID);
        return result;
    }

    public static InstagramUser staticGetLocalUserBasicForUsername(String aUsername) {
        InstagramUserDAO dao = new InstagramUserDAO("instagram-analytics");
        dao.ensureIndexes();
        InstagramUser result = dao.findByExactUsername(aUsername);
        return result;
    }

//    public InstagramUser getUserByUserID(String aUserID) {
//
//    }

    public InstagramUser localUserSearch(String aUsername) {
        InstagramUserDAO dao = new InstagramUserDAO("instagram-analytics");
        return dao.findByExactUsername(aUsername);
    }



    public List<InstagramUser> serviceUserSearch(String aUsername) throws BadAccessTokenException {
        JSONObject aUser;
        List<InstagramUser> result = new ArrayList<>();
        InstagramUser iuser;

        //TODO Should query the database first

        String userSearchURL = String.format(USER_SEARCH_URL, aUsername, "5");
        OAuthRequest request = new OAuthRequest(Verb.GET, userSearchURL);
        service.signRequest(accessToken, request);
        Response response = request.send();
        String s = response.getBody();
        Object obj = JSONValue.parse(s);
        JSONObject map = (JSONObject) obj;
        JSONArray a = (JSONArray) map.get("data");
        //JSONObject pagination = (JSONObject) map.get("pagination");
        int i;
        for(i=0; i<a.size(); i++) {
            JSONObject o = (JSONObject)a.get(i);
            if( ((String)o.get("username")).equalsIgnoreCase(aUsername)) {
                break;
            }
        }
        if(i >= a.size()) {
            return null;
        }
        try {
            aUser = (JSONObject) a.get(i);

            //saveUserBasicJson(aUser);
            iuser = gson.fromJson(aUser.toString(),
                    com.nearfuturelaboratory.humans.instagram.entities.InstagramUser.class);
            iuser = this.serviceRequestUserBasicForUserID(iuser.getUserID());
            //return iuser;
            //saveUserBasicJson(iuser);
            //captureInstagramUserAnalytics(iuser);
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





    protected JsonObject getRootUserMeta(InstagramUser aUser, DateTime snapshotDate) {

//        DateTimeFormatter fmt_short = DateTimeFormat.forPattern("MMddYY-HHmm");
//        JsonObject rootUserJson = new JsonObject();
        DateTime now = new DateTime(ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))));
        JsonObject rootUserMeta = new JsonObject();
        rootUserMeta.addProperty("username", aUser.getUsername());
        rootUserMeta.addProperty("userid", aUser.getUserID());
        rootUserMeta.addProperty("snapshot-date", short_date_fmt.print(snapshotDate));//DateTimeFormat.forPattern("MMddYY").print(snapshotDate));
        rootUserMeta.addProperty("snapshot-time", DateTimeFormat.forPattern("HHmm").print(snapshotDate));
        rootUserMeta.addProperty("snapshot-week-of-year", DateTimeFormat.forPattern("ww").print(snapshotDate));
        rootUserMeta.addProperty("snapshot-day-of-year", DateTimeFormat.forPattern("D").print(snapshotDate));
        rootUserMeta.addProperty("snapshot-year", DateTimeFormat.forPattern("yyyy").print(snapshotDate));
        rootUserMeta.addProperty("snapshot-time-millis", snapshotDate.getMillis());
        rootUserMeta.addProperty("snapshot-run-date", DateTimeFormat.forPattern("MMddYY").print(now));
        rootUserMeta.addProperty("snapshot-run-time", DateTimeFormat.forPattern("HHmm").print(now));
        rootUserMeta.addProperty("snapshot-run-week-of-year", DateTimeFormat.forPattern("ww").print(now));
        rootUserMeta.addProperty("snapshot-run-day-of-year", DateTimeFormat.forPattern("D").print(now));
        rootUserMeta.addProperty("snapshot-run-year", DateTimeFormat.forPattern("yyyy").print(now));
        rootUserMeta.addProperty("snapshot-run-time-millis", now.getMillis());




//        rootUserMeta.addProperty("counts", aUser.getCounts())
        //rootUserMeta.addProperty("other-meta", "something");

//        String s = gson.toJson(aUser);
//        JsonElement e = gson.toJsonTree(aUser);
//        rootUserMeta.add("user-meta", e.getAsJsonObject());
        return rootUserMeta;
        //return e.getAsJsonObject();

    }
    // one method
    @Deprecated
    protected JsonElement getInstagramUserStatusAndAnalytics(InstagramUser aUser) {
        JsonElement e;
        List<InstagramStatus> status = this.serviceRequestStatusForUserIDToMonthsAgo(aUser.getUserID(), 3);
        //Object statusJson = (Object)JSON.serialize(status);
        JsonElement status_analytics = getInstagramUserStatusAnalyticsAsJson(status);

        return status_analytics.getAsJsonObject();
    }

    // another list
    protected JsonElement getInstagramUsersForTopOfListAnalyticsJson(List<InstagramUser> friends) {
        // track users with the biggest follows in a list

        //Collections.sort(l_top_mentions, new Comparator<Map.Entry<String, Number>>()
        Collections.sort(friends, (o1, o2) -> Integer.compare(Integer.parseInt(o2.getFollowedByCount()),
                Integer.parseInt(o1.getFollowedByCount())));
        JsonArray a_top_friends = new JsonArray();
        int count = 1;
        for(InstagramUser temp : friends){
            JsonObject j = new JsonObject();
                    j.addProperty(temp.getFollowedByCount(), String.format("http://instagram.com/%1s", temp.getUsername()));

            a_top_friends.add(j);
            count++;
            if(count > 100) {
                break;
            }


        }

        return a_top_friends;
    }



    // another method
    protected JsonElement getInstagramUserFriendsAsJson(InstagramUser aUser)
    {
        JsonElement result = null;
            List<InstagramUser>follows = this.getInstagramUserFriendsAsList(aUser);
            Gson gson = new Gson();
            result = gson.toJsonTree(follows, new TypeToken<List<InstagramUser>>() {}.getType());

        return result;
    }

    public List<InstagramUser> getInstagramUserFriendsAsList(InstagramUser aUser) {


        if(this.localFriendsIsFresh(aUser.getUserID())) {
            List<InstagramFriend> friends = this.getLocalFriendsFor(aUser.getUserID());
            logger.debug("gots " + friends.size() + " people they follow");
            List<InstagramUser>result = new ArrayList<>();
            friends.forEach(instagramFriend -> {
                if (instagramFriend != null) {
                    result.add(instagramFriend.getFriend());
                }
            });
            return result;
        }
        else {
            try {
                List<InstagramUser> foo = this.serviceRequestFriendsAsUsers(aUser, 100);
                return foo;
            } catch (BadAccessTokenException e) {
                logger.warn(e);
                return new ArrayList<>();
            }

        }


    }

    /**
     * Get a list of dates for posts we have for this username
     * @param aUsername
     * @return
     */
    public List<DateTime> getPostDatesFor(String aUsername) {
        List<DateTime> result = new ArrayList<>();
        List<String> uniques = new ArrayList<>();
        List<String> urls = new ArrayList<>();
        List<Document> docs = this.getListOfInstagramAnalyticsDocumentsByUsername(aUsername);
        for(int i=0; i<docs.size(); i++) {
            Integer size = JsonPath.read(docs.get(i).toJson(), "$.analytics.status-json.length()");
            for(int j=0; j<size; j++) {
                Integer f = JsonPath.read(docs.get(i).toJson(), "$.analytics.status-json["+j+"].created_time");
                DateTime d = new DateTime().withMillis(f * 1000l);
                String url = JsonPath.read(docs.get(i).toJson(), "$.analytics.status-json["+j+"].link");
                String id = JsonPath.read(docs.get(i).toJson(), "$.analytics.status-json["+j+"].id");
                if(uniques.contains(id) == false) {
                    urls.add(url);
                    uniques.add(id);
                    result.add(d);
                }
            }
        }

        Collections.sort(result, new Comparator<DateTime>() {
            public int compare(DateTime d1, DateTime d2) {
                return d1.compareTo(d2);
            }
        });

        return result;
    }

    public List<Document> getSortedAnalyticsFor(List<Document> docs, String aKeySuffix, String aDate, Period aPeriod) {
        Collections.sort(docs,new Comparator<Document>() {
            public int compare(Document d1, Document d2) {
                Object f = JsonPath.read(d1.toJson(), "$.analytics.engagement-analytics-meta."+aKeySuffix);

                Double n1 = JsonPath.read( d1.toJson(), "$.analytics.engagement-analytics-meta."+aKeySuffix);
                Double n2 = JsonPath.read( d2.toJson(), "$.analytics.engagement-analytics-meta."+aKeySuffix);
                //logger.debug(n1+" "+n2);
                return n2.compareTo(n1);
            }
        });

        return docs;
    }

    public JsonElement getRankingByEngagementAnalyticFor(List<Document> docs, String aUsername, String aKeySuffix, String aDate, Period aPeriod) {
        int index = 0;

        Collections.sort(docs,new Comparator<Document>() {
            public int compare(Document d1, Document d2) {
                Object f = JsonPath.read(d1.toJson(), "$.analytics.engagement-analytics-meta."+aKeySuffix);

                Double n1 = JsonPath.read( d1.toJson(), "$.analytics.engagement-analytics-meta."+aKeySuffix);
                Double n2 = JsonPath.read( d2.toJson(), "$.analytics.engagement-analytics-meta."+aKeySuffix);
                //logger.debug(n1+" "+n2);
                return n2.compareTo(n1);
            }
        });


        for(int i = 0; i<docs.size(); i++) {
            if (aUsername.equalsIgnoreCase(docs.get(i).getString("username"))) {
                // mark the index
                index = i;
                break;
            }
        }

        JsonObject result = new JsonObject();
        result.addProperty("username", aUsername);
        result.addProperty("rank", index);
        result.addProperty("of", docs.size());
        result.addProperty("by", aKeySuffix);
        result.addProperty("date", aDate);
        result.addProperty("period", aPeriod.toString());
        return result;
    }


    public JsonElement getRankingByEngagementAnalyticFor(String aUsername, String aKeySuffix, String aDate, Period aPeriod) {

        List<Document>docs = this.getListOfInstagramAnalyticsDocumentsByDate(
                this.getListOfInstagramAnalyticsCollections(),
                aDate,
                aPeriod);

        return this.getRankingByEngagementAnalyticFor(docs, aUsername, aKeySuffix, aDate, aPeriod);

    }


    /**
     *
     * @param aAnalytic is the last string (xxx) in the Json path under analytics.engagement-analygics-meta.xxxx
     * @return a list, sorted large to small of that analytic
     */
    protected List<Document> getSortedListByEngagementAnalytic(String aAnalytic) {

        DateTime now = new DateTime(ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))));
        List<String> collectionNames = this.getListOfInstagramAnalyticsCollections();
        List<Document> docs = this.getListOfInstagramAnalyticsDocumentsByDayOfYear(collectionNames,
                                                                        Integer.parseInt(DateTimeFormat.forPattern("D").print(now)));
        try {
            Collections.sort(docs, new Comparator<Document>() {
                public int compare(Document d1, Document d2) {
                    try {
                        Double n1, n2;
                        Object foo = JsonPath.read(d1.toJson(), "$.analytics.engagement-analytics-meta." + aAnalytic);
                        if (foo.getClass() == Double.class) {
                            n1 = new Double((Double) JsonPath.read(d1.toJson(), "$.analytics.engagement-analytics-meta." + aAnalytic));
                            n2 = new Double((Double) JsonPath.read(d2.toJson(), "$.analytics.engagement-analytics-meta." + aAnalytic));
                        } else {
                            n1 = Double.parseDouble(JsonPath.read(d1.toJson(), "$.analytics.engagement-analytics-meta." + aAnalytic));
                            n2 = Double.parseDouble(JsonPath.read(d2.toJson(), "$.analytics.engagement-analytics-meta." + aAnalytic));
                        }
                        return n2.compareTo(n1);
                    } catch (NumberFormatException nfe) {
                        String s1 = JsonPath.read(d1.toJson(), "$.analytics.engagement-analytics-meta." + aAnalytic);
                        String s2 = JsonPath.read(d2.toJson(), "$.analytics.engagement-analytics-meta." + aAnalytic);
                        return s2.compareTo(s1);
                    } catch (ClassCastException cce) {
                        String a = d1.toJson();
                        String b = d2.toJson();
                        logger.debug("Eh? " + a);
                        return a.compareTo(b);
                    }
                    //return n2.compareTo(n1);
                }

            });
        } catch(java.lang.IllegalArgumentException iae) {
            logger.debug(iae);

        }
        return docs;

    }

    // THROW A STATUS HERE AND WE GET AN ELEMENT WE CAN USE TO UPDATE IN THE DATABASE
    /**
     * With a bunch of status, crunch and create some analytics..
     *
     * @param aStatusList
     * @return
     */
    protected JsonElement getInstagramUserStatusAnalyticsAsJson(List<InstagramStatus> aStatusList) {
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

//        // top 20 likers
//        ArrayList<String> top_likers = new ArrayList<String>(20);
//        // top 20 commenters
//        ArrayList<String> top_commenters = new ArrayList<String>(20);


        JsonArray result = new JsonArray();



        Map<String, Number> m_top_tags = new HashMap<String, Number>();
        Map<String, Number> m_top_mentions = new HashMap<String, Number>();

        Collections.sort(aStatusList, new Comparator<InstagramStatus>() {
            @Override
            public int compare(InstagramStatus o1, InstagramStatus o2) {
                return o2.getCreatedDate().compareTo(o1.getCreatedDate());
            }
        });


        for(InstagramStatus tmp : aStatusList) {
            // caption size

            // likes count
            likes_count += tmp.getLikes();

            // max likes
            if (tmp.getLikes() > max_likes_count) {
                max_likes_count = tmp.getLikes();
            }
            // min likes
            if (tmp.getLikes() < min_likes_count) {
                min_likes_count = tmp.getLikes();
            }
            // pace? posts per hour? per day? per week?

            // comments count
            comments_count += tmp.getCommentsCount();
            // max comments
            if (tmp.getCommentsCount() > max_comments_count) {
                max_comments_count = tmp.getCommentsCount();
            }
            // min comments
            if (tmp.getCommentsCount() < min_comments_count) {
                min_comments_count = tmp.getCommentsCount();
            }

            // likes count over time

            // comments count over time

            // track top tags
            List<String> tags = tmp.getTags();
            if(tags != null) {
                for (String tags_tmp : tags) {
                    tags_tmp = new String("#" + encodeKey(tags_tmp));
                    if (m_top_tags.containsKey(tags_tmp)) {
                        Number count = m_top_tags.get(tags_tmp);
                        int new_count = count.intValue();
                        new_count++;
                        m_top_tags.remove(tags_tmp);
                        m_top_tags.put(tags_tmp, new Integer(new_count));

                    } else {
                        m_top_tags.put(tags_tmp, 1);
                    }
                }
            }
            // now capture user (@) mentions in the caption text
            String caption = tmp.getCaptionText();
            if (caption != null) {
                Pattern pattern = Pattern.compile("(\\@\\S+)");
                Matcher matcher = pattern.matcher(caption);

                // int v = matcher.groupCount();
                int match_count = 0;
                while (matcher.find()) {
                    match_count++;
                    String username_mentioned = encodeKey(matcher.group(1));
                    if (m_top_mentions.containsKey(username_mentioned)) {
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
                mentions_count += match_count;

                // tags count
                tags_count += (tags == null ?  0 : tags.size());

            }
        }
        JsonObject o = new JsonObject();
        JsonObject c = new JsonObject();
        // number of posts in sample
        // time range of sample
        // posts per day, avg
        // posts per week, avg
        InstagramStatus _last;
        InstagramStatus _first;
        long _first_time;
        long _last_time;
        DateTime now = new DateTime(ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))));

        if(aStatusList != null && aStatusList.size() > 0) {
            _last = aStatusList.get(0);
            _first = aStatusList.get(aStatusList.size() - 1);


            _last_time = _last.getCreated_time().longValue();
            _first_time = _first.getCreated_time().longValue();

            // make sure we're getting milliseconds
            if (Math.log10(_first_time) < 10) {
                _first_time *= 1000L;
            }

            if (Math.log10(_last_time) < 10) {
                _last_time *= 1000L;
            }
        } else {
            _last_time = _first_time = now.getMillis();
        }

        Calendar first_cal = Calendar.getInstance();
        first_cal.setTimeInMillis(_first_time);

        Calendar last_cal = Calendar.getInstance();
        last_cal.setTimeInMillis(_last_time);

        Period period = new Period(_first_time, _last_time);
/*        if(period.getDays() < 1) {
            period = period.plusDays(1);
        }*/
        Duration duration = new Duration(_first_time, _last_time);
        if(duration.getStandardDays() < 1) {

            duration = duration.withDurationAdded(DateTimeConstants.MILLIS_PER_DAY, +1);
        }
        o.addProperty("period-posts-count", new Integer(aStatusList.size()).doubleValue());
        o.addProperty("earliest-in-period", String.format("%1$tb %1$te,%1$tY %1$tH %1$tM", first_cal));
        o.addProperty("latest-in-period",String.format("%1$tb %1$te,%1$tY %1$tH %1$tM", last_cal));
        o.addProperty("earliest-in-period-date", String.format("%1$tm%1$td%1$ty", first_cal));
        o.addProperty("latest-in-period-date", String.format("%1$tm%1$td%1$ty", last_cal));

        o.addProperty("period-between-earliest-latest", period.toString());
        o.addProperty("period-days", Float.parseFloat( String.format("%1$d",duration.getStandardDays()) ));
        float rate = (float)((float)aStatusList.size()/duration.getStandardDays());
        if(rate == Float.NaN || aStatusList.size() == 0) {
            rate = 0;
            o.addProperty("rate-posts-per-day", Double.parseDouble( String.format("%1$g", 0.0) ));
            o.addProperty("rate-days-per-post", Double.parseDouble( String.format("%1$g", 0.0) ));
            o.addProperty("rate-posts-per-week", Double.parseDouble( String.format("%1$g", 0.0) ));


            // likes average first
            //avg_likes = 0;
            o.addProperty("avg-likes", 0.0);
            o.addProperty("min-likes", 0.0);
            o.addProperty("max-likes", 0.0);

            // comments count average
            //avg_comments = comments_count / aStatusList.size();
            o.addProperty("avg-comments",0.0);
            o.addProperty("min-comments",0.0);
            o.addProperty("max-comments", 0.0);

            // mentions average
            avg_mentions = mentions_count / aStatusList.size();
            o.addProperty("avg-mentions", 0.0);

            // tags average
           // avg_tags = tags_count / aStatusList.size();
            o.addProperty("avg-tags", 0.0);
        } else {
            o.addProperty("rate-posts-per-day", Double.parseDouble( String.format("%1$g", rate) ));
            o.addProperty("rate-days-per-post", Double.parseDouble( String.format("%1$g", 1 / rate) ));
            o.addProperty("rate-posts-per-week", Double.parseDouble( String.format("%1$g", rate * 7) ));

            // likes average first
            avg_likes = likes_count / aStatusList.size();
            o.addProperty("avg-likes", new Double(Math.floor(avg_likes)*100/100));
            o.addProperty("min-likes", new Double(min_likes_count));
            o.addProperty("max-likes", new Double(max_likes_count));

            // comments count average
            avg_comments = comments_count / aStatusList.size();
            o.addProperty("avg-comments", new Double(Math.floor(avg_comments)*100/100));
            o.addProperty("min-comments", new Double(min_comments_count));
            o.addProperty("max-comments", new Double(max_comments_count));

            // mentions average
            avg_mentions = mentions_count / aStatusList.size();
            o.addProperty("avg-mentions", new Double(Math.floor(avg_mentions)*100/100));

            // tags average
            avg_tags = tags_count / aStatusList.size();
            o.addProperty("avg-tags", new Double(Math.floor(avg_tags)*100/100));


        }
        c.add("engagement-analytics-meta", o);


        // now get the image urls for the period status (max 12?)
        JsonArray a_image_urls = new JsonArray();
        JsonArray a_status_json = new JsonArray();
        aStatusList.forEach((status) -> {
            //Object img = status.getImages().get("low_resolution");

            Gson gson = new Gson();
            Type t = new TypeToken<InstagramStatus>(){}.getType();
            String json = gson.toJson(status, t);

            Object images = JsonPath.read(json,"$.images");
            String images_json = gson.toJson(images);

            JsonParser parser = new JsonParser();

            JsonElement e = parser.parse(images_json);
            a_image_urls.add(e);


            a_status_json.add(status.getStatusJSON());

        });

        c.add("status-json", a_status_json);
        c.add("image-urls", a_image_urls);
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


        return c;
    }

    /**
     *
     * @param aUsername
     * @return
     */
    protected List<Document> getListOfInstagramAnalyticsDocumentsByUsername(String aUsername) {
        List<String> aCollectionNames = this.getListOfInstagramAnalyticsCollections();
       // MongoDatabase db = MongoUtil.getMongo().getDatabase("instagram-analytics");

        List<Document> result = new ArrayList<Document>();

        //ArrayList<String> unique = new ArrayList<>();
        aCollectionNames.forEach((s) -> {
            // should add an additional clause for the Period (eg range from P1D, P7D, P1M)
            FindIterable<Document> docs_i;// = db.getCollection(s).find(eq("snapshot-date", date));

            docs_i = database.getCollection(s).find(
                    new Document("username", aUsername).append("snapshot-coverage-period", ONE_DAY).append("$where", "this.analytics[\"status-json\"].length > 0"));

            docs_i.forEach((Block<Document>) document -> {
                //TODO
                // check that we don't include duplicates, and if there are duplicate docs with a username
                // caused by multiple runs against Instagram, get the latest one
                // And maybe flat that there are dupes so we can garden later, maybe..
               // if( unique.contains((document.getString("username"))) == false) {

                    result.add(document);
                   // unique.add(document.getString("username"));
                //}
            });
        });

        return result;
    }




    public List<Document> getListOfInstagramAnalyticsDocumentsByDate(List<String> aCollectionNames, String date, Period period) {
        //MongoDatabase db = MongoUtil.getMongo().getDatabase("instagram-analytics");
        List<Document> result = new ArrayList<Document>();

        ArrayList<String> unique = new ArrayList<>();
        aCollectionNames.forEach((s) -> {
            // should add an additional clause for the Period (eg range from P1D, P7D, P1M)
            FindIterable<Document> docs_i;// = db.getCollection(s).find(eq("snapshot-date", date));
            /*

            Basicaly query for documents of this Period length, on a date, that actually have posts in them.

            DateTimeFormatter formatter = DateTimeFormat.forPattern("MM dd,yyyy");
            */
            docs_i = database.getCollection(s).find(
                    new Document("period-coverage-end-date", date)
                            .append("snapshot-coverage-period", period.toString())
                            //.append("analytics.engagement-analytics-meta.period-posts-count", new Document("$gt", 0))
                        )
                    .sort(orderBy(descending("period-coverage-end-millis")));


            docs_i.forEach((Block<Document>) document -> {
                //TODO
                // check that we don't include duplicates, and if there are duplicate docs with a username
                // caused by multiple runs against Instagram, get the latest one
                // And maybe flag that there are dupes so we can garden later, maybe..
                if( unique.contains((document.getString("username"))) == false) {

                    result.add(document);
                    unique.add(document.getString("username"));
                }
            });
        });


        return result;
    }

    protected List<Document> getListOfInstagramAnalyticsDocumentsByDayOfYearFromDocumentList(List<Document> sourceList, int dayOfYear) {
        List<Document> result = new ArrayList<Document>();
        if(sourceList != null) {
            sourceList.forEach(document -> {
                //System.out.println(document);
                if (dayOfYear == Integer.parseInt(JsonPath.read(document.toJson(), "$.snapshot-day-of-year"))) {
                    logger.debug("including " + document);
                    result.add(document);
                }

            });
        }


        return result;
    }

    public List<Document> getListOfInstagramAnalyticsDocumentsByDayOfYear(List<String> aCollectionNames, int dayOfYear) {
        //MongoDatabase db = MongoUtil.getMongo().getDatabase("instagram-analytics");
        List<Document> result = new ArrayList<Document>();

        ArrayList<String> unique = new ArrayList<>();
        aCollectionNames.forEach((s) -> {
           FindIterable<Document> docs_i = database.getCollection(s).find(eq("snapshot-day-of-year", String.valueOf(dayOfYear)));
            docs_i.forEach((Block<Document>) document -> {
                if( unique.contains((document.getString("username"))) == false) {
                    result.add(document);
                    unique.add(document.getString("username"));
                }
            });
        });


        return result;
    }




    public ArrayList<Document> getSortedListOfUserCounts(String aUsername, DateTime date, int limit) throws UsernameNotFoundException {
        InstagramUser user = localUserSearch(aUsername);
        if(user == null) {
            throw new UsernameNotFoundException("No such user "+aUsername);
        }
        String coll_name = InstagramAnalyticsService.encodeKey(user.getUsername())+"_"+user.getUserID()+"_snapshot-counts";
        //FindIterable<Document> docs_i = db.getCollection(coll_name).find(eq("snapshot-day-of-year", DateTimeFormat.forPattern("D").print(yesterday)));
        FindIterable<Document> docs_i = database.getCollection(coll_name).find(
                new Document("snapshot-date", short_date_fmt.print(date))
        ).sort(orderBy(descending("snapshot-time-millis"))).limit(limit);
        ArrayList<Document> result = new ArrayList();
        docs_i.forEach((Block<Document>) document -> {
            if(document != null) {
                result.add(document);
            }
        });
        return result;
            //.sort(orderBy(descending("period-coverage-end-millis")));
    }

    public List<Document> getListOfEndemicCyclistsInstagramAnalytics() {
        List<Document> d_1 = new ArrayList<>();
        ArrayList<String> n_1 = getListOfEndemicCyclistsInstagramAnalyticsUsernames();

        n_1.forEach((Consumer<String>) name -> {
            List<Document> docs = this.getListOfInstagramAnalyticsDocumentsByUsername(name);
            if(docs != null) {
                docs.forEach((Consumer<Document>) doc -> {
                    d_1.add(doc);
                });
            }
        });

        ArrayList<String> n_2 = getListOfEndemicCylingInstagramAnalyticsUsernames();
        n_2.forEach((Consumer<String>) name -> {
            List<Document> docs = this.getListOfInstagramAnalyticsDocumentsByUsername(name);
            if(docs != null) {
                docs.forEach((Consumer<Document>) doc -> {
                    d_1.add(doc);
                });
            }
        });

        return d_1;
    }



    public List<String> getListOfEndemicAnalyticsCollectionNames() {
        List<String> collectionNames = new ArrayList<>();
        ArrayList a_1 = getListOfEndemicCyclistsInstagramAnalyticsUsernames();
        a_1.addAll(getListOfEndemicCylingInstagramAnalyticsUsernames());

        a_1.forEach((Consumer<String>) name -> {
            InstagramUser aUser = this.localUserSearch(name);
            if(aUser != null) {
                String cName = aUser.getUsername().toLowerCase() + "_" + aUser.getUserID();
                collectionNames.add(cName);
            }
        });
        return collectionNames;
    }

    public List<Document> getEndemicAnalyticsCollectionsByDate(DateTime aDate, Period period) {
       List<String> collectionNames = this.getListOfEndemicAnalyticsCollectionNames();
        String str_date = this.short_date_fmt.print(aDate);

        List<Document> docs = getListOfInstagramAnalyticsDocumentsByDate(collectionNames,str_date, period );
        return docs;
    }


    public ArrayList<String> getListOfEndemicCyclistsInstagramAnalyticsUsernames() {
        //MongoDatabase db = MongoUtil.getMongo().getDatabase("instagram-analytics");
        MongoDatabase endemics = MongoUtil.getMongo().getDatabase("instagram-analytics-grabbag");
        // look in the extra-follows-cycling collection
        MongoCollection<Document> collection = endemics.getCollection("extra-follows-cyclists");

        ArrayList<ArrayList> l = new ArrayList<>();
        ArrayList<String> usernames = new ArrayList<>();
        ArrayList<InstagramUser> users = new ArrayList<>();

        MongoIterable iterator = collection.find();
        MongoCursor<Document> cursor = iterator.iterator();
        while(cursor.hasNext()) {
            l = (ArrayList)cursor.next().get("cyclists-endemic");
        }


        l.forEach((Consumer<ArrayList>) list -> {
            list.forEach((Consumer<String>) name -> {
                usernames.add(name.toLowerCase());
            });
        });





//
        return usernames;
    }


    public ArrayList<String> getListOfEndemicCylingInstagramAnalyticsUsernames() {
        //MongoDatabase db = MongoUtil.getMongo().getDatabase("instagram-analytics");
        MongoDatabase endemics = MongoUtil.getMongo().getDatabase("instagram-analytics-grabbag");
        // look in the extra-follows-cycling collection
        MongoCollection<Document> collection = endemics.getCollection("extra-follows-cycling");

        ArrayList<ArrayList> l = new ArrayList<>();
        ArrayList<String> usernames = new ArrayList<>();
        ArrayList<InstagramUser> users = new ArrayList<>();

        MongoIterable iterator = collection.find();
        MongoCursor<Document> cursor = iterator.iterator();
        while(cursor.hasNext()) {
            l = (ArrayList)cursor.next().get("cycling-endemic");
        }


        l.forEach((Consumer<ArrayList>) list -> {
            list.forEach((Consumer<String>) name -> {
                usernames.add(name.toLowerCase());
            });
        });

//
        return usernames;
    }



    public List<String> getListOfInstagramAnalyticsCollections() {
        //MongoDatabase db = MongoUtil.getMongo().getDatabase("instagram-analytics");
        MongoIterable<String> collections = database.listCollectionNames();

        ArrayList<String> collectionNames = new ArrayList<String>();


        collections.forEach((Block<String>) s -> {
            collectionNames.add(s);

        });

        collectionNames.remove("user");
        collectionNames.remove("system.indexes");
        collectionNames.removeIf(name->name.contains("_snapshot-counts"));
        return collectionNames;
    }


    protected void saveRootUserFriends(InstagramUser aUser) throws Exception {
        DB foo = client.getDB("instagram-analytics-friends");
        //MongoCollection<Document> rootUser = database.getCollection("root_users");
        DBCollection rootUser;// = db.getCollection("root_users");

        //rootUser.drop();
        DateTime now = new DateTime(ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))));
        DateTimeFormatter fmt_short = DateTimeFormat.forPattern("MMddYY");

        //Set s = db.getCollectionNames();
        //logger.debug(s);

        rootUser = foo.getCollection(aUser.getUsername() +"_" + fmt_short.print(now)+"_friends");
        rootUser.drop();

//        List<InstagramUser> users = this.serviceUserSearch("darthjulian");
//        InstagramUser user = null;
//        List<InstagramUser> friends = null;
//        if (users.size() > 0) {
//            user = users.get(0);
//            friends = this.serviceRequestFriendsAsUsers(user.getUserID());
////            logger.debug(friends);
//            this.getInstagramUsersForTopOfListAnalyticsJson(friends);
//        }

        InstagramUser user = InstagramAnalyticsService.staticGetLocalUserBasicForUsername(aUser.getUsername());
        List<InstagramUser> friends = this.getInstagramUserFriendsAsList(user);

        Gson gson = new Gson();
        JsonElement element = gson.toJsonTree(friends, new TypeToken<List<InstagramUser>>() {}.getType());

        JsonObject rootUserJson = new JsonObject();
        JsonArray o = element.getAsJsonArray();
        rootUserJson.add(aUser.getUsername()+"_friends", o);
        DBObject obj = (DBObject) JSON.parse(rootUserJson.toString());
        rootUser.save(obj);
    }

    public void deleteManyInstagramUserAnalytics(InstagramUser aUser, DateTime date, Period period) {

        //MongoDatabase db = MongoUtil.getMongo().getDatabase("instagram-analytics");


        MongoCollection rootUser = database.getCollection(encodeKey(aUser.getUsername()) + /*"_" + fmt_short.print(now)+*/"_" + aUser.getUserID());
        DateTimeFormatter fmt_short = DateTimeFormat.forPattern("MMddYY");

        rootUser.deleteMany(new Document().
                append("username", aUser.getUsername()).
                append("snapshot-date", fmt_short.print(date)).
                append("snapshot-coverage-period", period.toString()));

    }

    /**
     * This will capture back analytics for a user with the latest status at latest, going BACKWARDS an amount
     * specified by period. We always assume a midnight boundary, so this normalizes to the beginning of the earliest day.
     *
     * @param aUser
     * @param latestDateTime
     * @param period
     */
    public void captureInstagramUserAnalytics(InstagramUser aUser, DateTime latestDateTime, Period period) {
        long timeToRun = System.currentTimeMillis();
        //MongoDatabase foo = MongoUtil.getMongo().getDatabase("instagram-analytics");
        //DateTime now = new DateTime(ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))));
        DateTimeFormatter fmt_short = DateTimeFormat.forPattern("MMddYY");
        MongoCollection rootUser = database.getCollection(encodeKey(aUser.getUsername()) + "_" + aUser.getUserID());
       // DBCollection collection = db.getCollection(encodeKey(aUser.getUsername()) + /*"_" + fmt_short.print(now)+*/"_" + aUser.getUserID());

        JsonObject rootUserJson = new JsonObject();

        Date earliestDate = latestDateTime.minus(period).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).toDate();
        //DateTime earliest = latest.minus(period).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
        DateTime earliestDateTime = new DateTime(earliestDate);

        Date latestDate = latestDateTime.withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).withMillisOfSecond(999).toDate();
        Duration duration = new Duration(earliestDateTime, latestDateTime);


        rootUserJson = getRootUserMeta(aUser, latestDateTime);

        rootUserJson.addProperty("snapshot-coverage-period", period.toString());
        rootUserJson.addProperty("snapshot-coverage-duration-days", (double)duration.getStandardDays());
        rootUserJson.addProperty("snapshot-coverage-duration-hours", (double)duration.getStandardHours());
        rootUserJson.addProperty("snapshot-coverage-duration-millis", (double)duration.getMillis());
        rootUserJson.addProperty("period-coverage-start-date", fmt_short.print(earliestDateTime));
        rootUserJson.addProperty("period-coverage-end-date", fmt_short.print(latestDateTime));
        rootUserJson.addProperty("period-coverage-start-millis", (double) earliestDateTime.getMillis());
        rootUserJson.addProperty("period-coverage-end-millis", (double) latestDateTime.getMillis());
        rootUserJson.addProperty("period-coverage-start-day-of-year", (double) earliestDateTime.getDayOfYear());
        rootUserJson.addProperty("period-coverage-end-day-of-year", (double) latestDateTime.getDayOfYear());
        rootUserJson.addProperty("period-coverage-start-month", (double) earliestDateTime.getMonthOfYear());
        rootUserJson.addProperty("period-coverage-end-month", (double) latestDateTime.getMonthOfYear());
        rootUserJson.addProperty("period-coverage-start-week", (double) earliestDateTime.getWeekOfWeekyear());
        rootUserJson.addProperty("period-coverage-end-week", (double) latestDateTime.getWeekOfWeekyear());
        rootUserJson.addProperty("period-coverage-start-year", (double) earliestDateTime.getYear());
        rootUserJson.addProperty("period-coverage-end-year", (double) latestDateTime.getYear());

        int trailingMonths = Months.monthsBetween(
                latestDateTime,
                latestDateTime.minus(period)).getMonths();

        if(trailingMonths < 1) trailingMonths = 1;

        /*
         * Start with getting appropriate range of InstagramStatus
         */
        List<InstagramStatus> status = new ArrayList<InstagramStatus>();
        if (this.localServiceStatusIsNewStatus(aUser.getUserID())) {
            status = this.getLocalStatusByExactUserIDToMonthsAgo(aUser.getUserID(),trailingMonths);
            if(status == null) {
                logger.info("Null status all together. This'll punt.");
                status = new ArrayList<>();
            }
            if(status.size() < 1) {
                logger.info("Woops.  "+aUser.getUsername()+" status is stale all together. Maybe they're media lazy or media dead?");
                // something wrong here. the status is probably stuck or the account is just completely stale all together
                //status = this.serviceRequestStatusForUserIDToMonthsAgo(aUser.getUserID(), trailingMonths);
            } else {
                logger.info("==== NOT REQUESTING UNSTALE STATUS ====");
            }
        } else {
            logger.info("Making service request. "+aUser.getUsername()+" status is stale.");
            status = this.serviceRequestStatusForUserIDToMonthsAgo(aUser.getUserID(), trailingMonths);
        }

        //TODO HERE IS WHERE WE CAN UPDATE THE ANALYTICS ENTRIES FOR NEW LIKES
        //TODO IT IS THE CASE (I THINK..) THAT THE DB GETS UPDATED IN THE STATUS COLLECTION
        //TODO WHEN THE QUERIES FOR serviceRequestStatusForUserIDToMonthsAgo IS EXECUTED
        //TODO THE JsonElement RETURNED VIA getInstagramUserStatusAnalyticsAsJson CAN BE USED
        //TODO TO UPDATE ANY EXISTING ANALYTICS
        // here update status in the db to update likes and so forth..


        status.removeIf(s -> s.getCreatedDate().before(earliestDate));
        status.removeIf(s-> s.getCreatedDate().after(latestDate));
        //status.removeIf(s -> s.getCreatedDate().before(earlier.getMillis());
        logger.debug("Status size from "+fmt_short.print(latestDateTime)+" — "+fmt_short.print(earliestDateTime)+" "+period+" for "+aUser.getUsername()+"_"+aUser.getUserID()+" is "+status.size());


        rootUserJson.addProperty("posts-count-in-coverage-period", new Double(status.size()));
        /*
         * Now get the analytics..
         */
        JsonElement e = this.getInstagramUserStatusAnalyticsAsJson(status);
        rootUserJson.add("analytics", e);
        //rootUserArrayJson.add(this.getInstagramUserStatusAnalyticsAsJson(status));

        /*************
         List<InstagramUser> friends = this.getInstagramUserFriendsAsList(aUser);
         JsonElement top_friends = this.getInstagramUsersForTopOfListAnalyticsJson(friends);
         rootUserJson.add("top-friends", top_friends);
         */


        // this may be ridiculous to run with large numbers of followers
        /**************
         try {
         List<InstagramUser> followers = this.serviceRequestFollowersAsUsers(aUser.getUserID());
         JsonElement top_followers = this.getInstagramUsersForTopOfListAnalyticsJson(followers);
         rootUserJson.add("top-followers", top_followers);
         } catch(Exception exc) {
         logger.warn(exc);
         }
         */


        String y = rootUserJson.toString();
        timeToRun = System.currentTimeMillis()- timeToRun;
        rootUserJson.addProperty("snapshot-cpu-time", timeToRun);

        Document doc = Document.parse(rootUserJson.toString());


        /* delete what may already be there in the database covering this coverage period */
        String endDate = fmt_short.print(latestDateTime);
        DeleteResult result = rootUser.deleteMany(new Document("username", aUser.getUsername())
                .append("period-coverage-end-date", endDate)
                .append("snapshot-coverage-period", period.toString()));

        logger.debug(result);

        try {
            rootUser.insertOne(doc);
            logger.info("added "+period.toString()+" "+endDate+" for "+aUser.getUsername()+" with size "+status.size());
        } catch(Exception ex) {
            logger.warn("", ex);
        }


    }


    /**
     * Capture for a user based on trailing days..should be able to modify for a date range
     * @param aUser
     * @param aTrailingDays
     */
    @Deprecated
    public void captureInstagramUserAnalytics(InstagramUser aUser, int aTrailingDays) {


        MongoDatabase foo = client.getDatabase("instagram-analytics");

        DateTime now = new DateTime(ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))));

        MongoCollection rootUser;
        rootUser = foo.getCollection(encodeKey(aUser.getUsername()) + /*"_" + fmt_short.print(now)+*/"_" + aUser.getUserID());

        JsonObject rootUserJson = new JsonObject();

        rootUserJson = getRootUserMeta(aUser, now);

        Period cover = new Period(0, 0, 0, aTrailingDays, 0, 0, 0, 0);
        DateTime earlier = now.minus(cover).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);// new DateTime(early).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);

        rootUserJson.addProperty("snapshot-coverage-period", cover.toString());

        int trailingMonths = Months.monthsBetween(
                now,
                earlier).getMonths();

        if(trailingMonths < 1) trailingMonths = 1;

        /*
         * Start with getting appropriate range of InstagramStatus
         */
        List<InstagramStatus> status = new ArrayList<InstagramStatus>();
        if (this.localServiceStatusIsNewStatus(aUser.getUserID())) {
            status = this.getLocalStatusByExactUserIDToMonthsAgo(aUser.getUserID(),trailingMonths);
            if(status == null || status.size() < 1) {
                status = this.serviceRequestStatusForUserIDToMonthsAgo(aUser.getUserID(), trailingMonths);
            }
        } else {
            status = this.serviceRequestStatusForUserIDToMonthsAgo(aUser.getUserID(), trailingMonths);
        }

        status.removeIf(s -> s.getCreatedDate().before(earlier.toDate()));
        logger.debug("Status size for "+aUser.getUsername()+" is "+status.size());

        /*
         * Now get the analytics..
         */
        JsonElement e = this.getInstagramUserStatusAnalyticsAsJson(status);
        rootUserJson.add("analytics", e);

        /*************
        List<InstagramUser> friends = this.getInstagramUserFriendsAsList(aUser);
        JsonElement top_friends = this.getInstagramUsersForTopOfListAnalyticsJson(friends);
        rootUserJson.add("top-friends", top_friends);
        */


        // this may be ridiculous to run with large numbers of followers
        /**************
        try {
            List<InstagramUser> followers = this.serviceRequestFollowersAsUsers(aUser.getUserID());
            JsonElement top_followers = this.getInstagramUsersForTopOfListAnalyticsJson(followers);
            rootUserJson.add("top-followers", top_followers);
        } catch(Exception exc) {
            logger.warn(exc);
        }
        */


        String y = rootUserJson.toString();
        Document doc = Document.parse(rootUserJson.toString());


        rootUser.insertOne(doc);

    }





    /**
     * @param aUserID should be an InstagramUser??
     *
     */
    protected List<InstagramUser> serviceRequestFriendsAsUsers(InstagramUser aUser, long throttle_millis) throws BadAccessTokenException, NullPointerException {
        List<InstagramUser> result = new ArrayList<InstagramUser>();
        String aUserID = aUser.getUserID();
        if (aUserID == null || aUserID.equalsIgnoreCase("self")) {
            aUserID = user.getId();
            // if the user basic isn't fresh for self, then request it and reset ourselves
            if (this.localUserBasicIsFresh() == false) {
                user = serviceRequestUserBasic();
            }
            //aUser = user;
        } /*else {
            if (this.localUserBasicIsFreshForUserID(aUserID) == false) {
                // freshen this user
                InstagramUser tmpUser = this.serviceRequestUserBasicForUserID(aUserID);
                // save it for later?
                saveUserBasic(tmpUser);
               //saveUserBasicJson(null);
                //saveUserBasicForAnalytics(tmpUser);
            }
        }
        */
        //aUser = this.getLocalUserBasicForUserID(aUserID);

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
            try {
                Thread.sleep(throttle_millis);
            } catch (InterruptedException ie) {
                logger.warn("Problem throttling for " + aUser.getUsername(), ie);
            }
        }

            for(JSONObject j : allFollows) {
                InstagramUserBriefly iub = gson.fromJson(j.toString(), InstagramUserBriefly.class);
                InstagramUser friend = this.getLocalUserBasicForUserID(iub.getId());
                if(friend == null || this.localUserBasicIsFreshForUserID(iub.getId()) == false) {
                    friend = this.serviceRequestUserBasicForUserID(iub.getId());
                    if (friend == null) {
                        logger.warn(iub + " is maybe a private/blocked User from whichever user is authenticated in this transaction. (Are you running analytics on someone else's account?");
                        continue;
                    }
                    try {
                        Thread.sleep(throttle_millis);
                    } catch (InterruptedException ie) {
                        logger.warn("Problem throttling for " + friend.getUsername(), ie);
                    }
                }
                result.add(friend);



            }

        return result;
    }

    /**
     * This creates a snapshot of the user profile with their counts, etc., so we can track changes over time
     * This is NOT saving to the user collection in the db
     * To do that call saveUserBasic(InstagramUser) or saveUserBasicJson(JSON)
     * @param aUser
     */
    public void saveUserBasicForAnalytics(InstagramUser aUser) {
        Type t = new TypeToken<InstagramUser>(){}.getType();
        String json = gson.toJson(aUser, t);
        Document doc = Document.parse(json);

        MongoDatabase foo = client.getDatabase("instagram-analytics");

        DateTime now = new DateTime(ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))));
        DateTimeFormatter fmt_short = DateTimeFormat.forPattern("MMddYY");

        MongoCollection collection;
        collection = foo.getCollection(encodeKey(aUser.getUsername()) + "_" + aUser.getUserID()+"_snapshot-counts");


        doc.append("snapshot-date", DateTimeFormat.forPattern("MMddYY").print(now));
        doc.append("snapshot-time", DateTimeFormat.forPattern("HHmm").print(now));
        doc.append("snapshot-week-of-year", DateTimeFormat.forPattern("ww").print(now));
        doc.append("snapshot-day-of-year", DateTimeFormat.forPattern("D").print(now));
        doc.append("snapshot-year", DateTimeFormat.forPattern("yyyy").print(now));
        doc.append("snapshot-time-millis", now.getMillis());
       collection.insertOne(doc);
    }

    public InstagramUser serviceRequestUserBasicForUserID(String aUserID) throws BadAccessTokenException {
        //JSONObject result = __serviceRequestUserBasicForUserID(aUserID);
        JSONObject aUser;
        String userURL = String.format(USER_URL, aUserID);
        OAuthRequest request = new OAuthRequest(Verb.GET, userURL);
        service.signRequest(accessToken, request);
        Response response = request.send();
        // TODO Error check..
        if (response.getCode() == 400 && response.getHeaders().containsValue("HTTP/1.1 400 BAD REQUEST")) {
            logger.warn("Bad response for Instagram User ID " + aUserID + " via " + this.getThisUser() + " " + response.getBody());
            return null;
            //throw new BadAccessTokenException("Bad response for " + aUserID + " " + this.getThisUser() + " " + response.getBody());
        }
        String s = response.getBody();
        Object obj = JSONValue.parse(s);
        try {
            aUser = (JSONObject) ((JSONObject) obj).get("data");
            com.nearfuturelaboratory.humans.instagram.entities.InstagramUser iuser = gson.fromJson(aUser.toString(),
                    com.nearfuturelaboratory.humans.instagram.entities.InstagramUser.class);
            saveUserBasicJson(aUser);
            //saveUserBasicForAnalytics(iuser);
            logger.info("Got updated basic user and analytics user info for "+iuser.getUsername());
            return iuser;
        } catch (Exception e) {
            //logger.warn("Maybe a private user when doing analytics?");
            //logger.warn(e);
            if (response.getCode() == 400 && response.getBody().contains("OAuthAccessTokenException")) {
                logger.warn("Bad response for Instagram User ID " + aUserID + " via " + this.getThisUser() + " " + response.getBody(), e);
                throw new BadAccessTokenException("Bad response for " + aUserID + " " + this.getThisUser() + " " + response.getBody());
            } else
            if(response.getCode() == 400 && response.getBody().contains("APINotAllowedError")) {
                logger.warn("APINotAllowedError. You've either been blocked by a user or are running analytics against someone else's account.");
                logger.warn(response.getBody());
            } /*else {
                // logger.warn(e);
            }*/
            return null;
        }


    }

/*
 "snapshot-coverage-period" : 1,
        "snapshot-coverage-duration-days" : 1,
        "snapshot-date" : 1,
        "snapshot-run-time" : 1,
        "snapshot-coverage-period" : 1,
        "posts-count-in-coverage-period" : 1,
        "snapshot-coverage-period" : 1,
        "snapshot-run-time-millis" : 1,
        "analytics.engagement-analytics-meta.avg-likes" : 1,
        "analytics.engagement-analytics-meta.avg-comments" : 1

 */

    public ArrayList<Document> gatherAggregatePeriodDataForPeriod(InstagramUser aUser, DateTime coveragePeriodEndDate, String aPeriod) {
        AggregateIterable<Document> iterable;

        String collection_name = aUser.getUsername()+"_"+aUser.getUserID();
        String str_date = short_date_fmt.print(coveragePeriodEndDate);
        iterable = database.getCollection(collection_name).aggregate(asList(
                new Document("$match", (
                        new Document("snapshot-coverage-period", new Document("$in",
                                asList(aPeriod))))
                        .append("period-coverage-end-date", str_date)
                ),
                new Document("$sort", new Document("snapshot-time-millis", 1)),
                new Document("$project", new Document("analytics.status-json", 1)
                        .append("snapshot-run-date", 1)

                )

                ));

        ArrayList<Document> result = new ArrayList();

        iterable.forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                Gson gson = new Gson();
                result.add(document);
            }
        });
        return result;
    }


    public ArrayList<Document> gatherAggregatePeriodData(InstagramUser aUser, DateTime coveragePeriodEndDate) {
        AggregateIterable<Document> iterable;

        String collection_name = aUser.getUsername()+"_"+aUser.getUserID();
        String str_date = short_date_fmt.print(coveragePeriodEndDate);
        iterable = database.getCollection(collection_name).aggregate(asList(
                new Document("$match", (
                        new Document("snapshot-coverage-period", new Document("$in",
                        asList("P1M","P15D","P10D","P7D","P5D","P3D","P2D","P1D"))))
                        .append("period-coverage-end-date", str_date)
                ),
                new Document("$sort", new Document("snapshot-coverage-duration-days", 1)),
                new Document("$project", new Document("snapshot-coverage-period", 1)
                        .append("snapshot-coverage-duration-days", 1)
                        .append("snapshot-date", 1)
                        .append("snapshot-run-time", 1)
                        .append("period-coverage-end-date", 1)
                        .append("period-coverage-start-date", 1)
                        .append("period-coverage-end-millis", 1)
                        .append("posts-count-in-coverage-period", 1)
                        .append("snapshot-run-time-millis", 1)
                        .append("analytics.engagement-analytics-meta.max-likes", 1)
                        .append("analytics.engagement-analytics-meta.max-comments", 1)
                        .append("analytics.engagement-analytics-meta.avg-likes", 1)
                        .append("analytics.engagement-analytics-meta.avg-comments", 1)
        )));

        ArrayList<Document> result = new ArrayList();

        iterable.forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                Gson gson = new Gson();
                result.add(document);
            }
        });
        return result;
    }

    /**
     *
     */
//    public ArrayList<Document> getSortedListOfAggregatePeriodData() {
//
//    }

    /**
     * Gets all the usernames for users we want to run analytics upon.
     *
     * @return
     * @throws IOException
     */
    public ArrayList<String> gatherUsernamesForAnalytics() throws IOException {
        ArrayList<String> listOfUserNames = new ArrayList<String>();
        MongoCollection rootCollection;
        MongoDatabase db = client.getDatabase("instagram-analytics");
        //rootUser = foo.getCollection(encodeKey(aUser.getUsername()) + /*"_" + fmt_short.print(now)+*/"_" + aUser.getUserID());
        rootCollection = db.getCollection("base_analytics_users");
        FindIterable f = rootCollection.find();
        //ArrayList<String> list = new ArrayList<>();
        Document doc = (Document)f.first();
        listOfUserNames.addAll( doc.get("base_analytics_users", ArrayList.class));

        // Find the people we @omata_la follow to be sure
        InstagramUser me = InstagramAnalyticsService.staticGetLocalUserBasicForUsername("omata_la");
        List<InstagramUser> follows = this.getInstagramUserFriendsAsList(me);
        follows.forEach((instagramUser) -> {
            String a_follow = instagramUser.getUsername();
            if (listOfUserNames.contains(a_follow) == false) {
                logger.info("Adding ["+a_follow+"] via "+me.getUsername()+" to list..("+listOfUserNames.size()+")");

                listOfUserNames.add(a_follow);
            }
        });

        // now save this list?

        // Find the people @darthjulian follows to be sure
        InstagramUser darthjulian = InstagramAnalyticsService.staticGetLocalUserBasicForUsername("darthjulian");
        follows = this.getInstagramUserFriendsAsList(darthjulian);
        follows.forEach((instagramUser) -> {
            String a_follow = instagramUser.getUsername();
            if (listOfUserNames.contains(a_follow) == false) {
                logger.info("Adding ["+a_follow+"] via "+darthjulian.getUsername()+" to list..("+listOfUserNames.size()+")");

                listOfUserNames.add(a_follow);
            }
        });

        // Find the people that @rhnewman follows also
        InstagramUser rhys = InstagramAnalyticsService.staticGetLocalUserBasicForUsername("rhnewman");
        follows = this.getInstagramUserFriendsAsList(rhys);
        follows.forEach((instagramUser) -> {
            String a_follow = instagramUser.getUsername();
            if (listOfUserNames.contains(a_follow) == false) {
                logger.info("Adding ["+a_follow+"] via "+rhys.getUsername()+" to list..("+listOfUserNames.size()+")");

                listOfUserNames.add(a_follow);
            }
        });

        // Find the people that @hellofosta follows also
        InstagramUser fosta = InstagramAnalyticsService.staticGetLocalUserBasicForUsername("hellofosta");
        follows = this.getInstagramUserFriendsAsList(fosta);
        follows.forEach((instagramUser) -> {
            String a_follow = instagramUser.getUsername();
            if (listOfUserNames.contains(a_follow) == false) {
                logger.info("Adding ["+a_follow+"] via "+fosta.getUsername()+" to list..("+listOfUserNames.size()+")");

                listOfUserNames.add(a_follow);
            }
        });

        // Find the people that @stefanoblanco follows also
        InstagramUser stefanoblanco = InstagramAnalyticsService.staticGetLocalUserBasicForUsername("stefanoblanco");
        follows = this.getInstagramUserFriendsAsList(stefanoblanco);
        follows.forEach((instagramUser) -> {
            String a_follow = instagramUser.getUsername();
            if (listOfUserNames.contains(a_follow) == false) {
                logger.info("Adding ["+a_follow+"] via "+stefanoblanco.getUsername()+" to list..("+listOfUserNames.size()+")");

                listOfUserNames.add(a_follow);
            }
        });

        // Find the people that @vernor follows also
        InstagramUser vernor = InstagramAnalyticsService.staticGetLocalUserBasicForUsername("vernor");
        follows = this.getInstagramUserFriendsAsList(vernor);
        follows.forEach((instagramUser) -> {
            String a_follow = instagramUser.getUsername();
            if (!listOfUserNames.contains(a_follow)) {
                logger.info("Adding ["+a_follow+"] via "+vernor.getUsername()+" to list..("+listOfUserNames.size()+")");

                listOfUserNames.add(a_follow);
            }
        });

        // Find the people that @theradavist follows also
        InstagramUser theradavist = InstagramAnalyticsService.staticGetLocalUserBasicForUsername("theradavist");
        follows = this.getInstagramUserFriendsAsList(theradavist);
        follows.forEach((instagramUser) -> {
            String a_follow = instagramUser.getUsername();
            if (!listOfUserNames.contains(a_follow)) {
                logger.info("Adding ["+a_follow+"] via "+theradavist.getUsername()+" to list..("+listOfUserNames.size()+")");

                listOfUserNames.add(a_follow);
            }
        });


        // now go into the collections in instagram-analytics-grabbag
        // extra-follows-cycling
        // extra-follows-cyclists
        // and add those accounts
        getListOfEndemicCyclistsInstagramAnalyticsUsernames().forEach((username) -> {
            if (!listOfUserNames.contains(username)) {
                logger.info("Adding ["+username+"] via getListOfEndemicCyclistsInstagramAnalyticsUsernames to list..("+listOfUserNames.size()+")");

                listOfUserNames.add(username);
            }
        });

        getListOfEndemicCylingInstagramAnalyticsUsernames().forEach((username) -> {
            if (!listOfUserNames.contains(username)) {
                logger.info("Adding ["+username+"] via getListOfEndemicCylingInstagramAnalyticsUsernames to list..("+listOfUserNames.size()+")");

                listOfUserNames.add(username);
            }
        });



        return listOfUserNames;
    }


    public static InstagramUser getLocalUserBasicForUsername(String aUsername) {
        InstagramUserDAO dao = new InstagramUserDAO("instagram-analytics");

        InstagramUser user = dao.findByExactUsernameCaseInsensitive(aUsername);
        return user;
    }


    public static String encodeKey(String key) {
        return key.replace("\\", "\\\\").replace("$", "\\u0024").replace(".", "\\u002e");
    }

    public static String decodeKey(String key) {
        return key.replace("\\u002e", ".").replace("\\u0024", "$").replace("\\\\", "\\");
    }

}
