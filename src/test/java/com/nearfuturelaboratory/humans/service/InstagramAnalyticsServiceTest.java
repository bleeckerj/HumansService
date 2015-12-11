package com.nearfuturelaboratory.humans.service;

import com.google.gson.*;
import com.jayway.jsonpath.JsonPath;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramFriend;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramUser;
import com.nearfuturelaboratory.humans.util.MongoUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.joda.time.*;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.nearfuturelaboratory.util.Constants;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.eq;

/**
 * Created by julian on 11/23/15.
 */
public class InstagramAnalyticsServiceTest {
    static InstagramAnalyticsService instagram;
    final static Logger logger = LogManager.getLogger(com.nearfuturelaboratory.humans.service.InstagramAnalyticsServiceTest.class);
    static int SEVEN_DAYS_BACK = 7;
    static int ONE_DAY_BACK = 1;
    static int MONTHS_BACK = 1;


    @Before
    public void setUp() throws Exception {
        try {
            Constants.load("/Users/julian/Documents/workspace/HumansService/src/main/webapp/WEB-INF/lib/dev.app.properties");
            //PropertyConfigurator.configureAndWatch("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/WebContent/WEB-INF/lib/static-logger.properties");
            //logger.debug("Hey Ho!");
            instagram = InstagramAnalyticsService.createServiceOnBehalfOfUsername("darthjulian");

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() throws Exception {

    }

    @Ignore
    public void testCreateServiceOnBehalfOfUsername() throws Exception {

    }

    @Test
    public void testUsersSearch() throws Exception {
        List<InstagramUser> users =  instagram.serviceUserSearch("darthjulian");
        InstagramUser user = null;
        if(users.size() > 0) {
            user = users.get(0);
            //instagram.captureInstagramUserAnalytics(user);
            //List<InstagramStatus> status = instagram.serviceRequestStatusForUserIDToMonthsAgo(user.getUserID(), 3);
            //logger.debug(status);


        }
    }
//TODO Mark
    // this generates sorted sort of things
    @Test
    public void test_getSortedListByEngagementAnalytic() {
        List<Document> sorted = instagram.getSortedListByEngagementAnalytic("avg-likes");
        List<Document> clipped = instagram.getListOfInstagramAnalyticsDocumentsByDayOfYearFromDocumentList(sorted, 338);

        String sortedClippedJson = new Gson().toJson(clipped);
        logger.debug("done");
    }

    @Test
    public void weird() {
        MongoDatabase db = MongoUtil.getMongo().getDatabase("instagram-analytics");
        FindIterable<Document> docs = db.getCollection("_superdomestik_417717129").find(eq("snapshot-day-of-year", "333"));

        Document foo = docs.first();
        docs.forEach((Block<Document>) document -> {
            String json = document.toJson();
            logger.debug(json);
        });
    }

    @Test
    public void getAllCollectionsForADay() {
        MongoDatabase db = MongoUtil.getMongo().getDatabase("instagram-analytics");
        db.getCollection("restaurants").find(eq("borough", "Manhattan"));



    }

    @Test
    public void test_getAllStatusForDateOrdered() {
        DateTimeFormatter fmt_short = DateTimeFormat.forPattern("MMddYY-HHmmss");
        //DateTime now = new DateTime(ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))));
        Gson gson = new Gson();
        JsonObject o = new JsonObject();
        JsonArray a = new JsonArray();
        List<Document> docs = instagram.getAllStatusForDateOrdered();
        docs.forEach((Consumer<? super Document>) d -> {

//            DateTime dt = new DateTime(new Date(d.getInteger("created_time").longValue()*1000));
//            logger.debug(fmt_short.print(dt));
//            logger.debug(d.getString("link")+", "+new Date(d.getInteger("created_time").longValue()*1000));

            a.add(new JsonParser().parse(d.toJson()));
        });
        //logger.debug(docs.size());
         o.add("status",a);
    }

    @Test
    public void test_getAnalyticsDocumentsByUsername() throws Exception {
        // start with a username_userid
        ArrayList<Document> result = new ArrayList<>();
        MongoDatabase db = MongoUtil.getMongo().getDatabase("instagram-analytics");
        InstagramUser user = instagram.localUserSearch("rapha");
        String coll_name = user.getUsername()+"_"+user.getUserID();
        FindIterable<Document> docs_i = db.getCollection(coll_name).find(eq("snapshot-day-of-year", "338"));
        docs_i.forEach((Block<Document>) document -> {
           result.add(document);
        });

    }


    @Test
    public void test_getListOfInstagramAnalyticsDocumentsAndSort() throws Exception {
        List<String> collectionNames = instagram.getListOfInstagramAnalyticsCollections();
        List<Document> docs = instagram.getListOfInstagramAnalyticsDocumentsByDayOfYear(collectionNames, 333);
        //logger.debug(docs);
       // String json = docs.get(0).toJson();
        //Number n = (Number)JsonPath.read(json, "$.analytics.engagement-analytics-meta.max-likes");

        Collections.sort(docs,new Comparator<Document>() {
            public int compare(Document d1, Document d2) {
                Double n1 = JsonPath.read( d1.toJson(), "$.analytics.engagement-analytics-meta.avg-comments");
                Double n2 = JsonPath.read( d2.toJson(), "$.analytics.engagement-analytics-meta.avg-comments");
                return n2.compareTo(n1);
            }
        });
       // logger.debug(docs);

    }

    //TODO Pick here to start
    // main test..despite captureInstagramUserAnalytics deprecated
    @Test
    public void test_captureInstagramUserAnalytics() throws Exception {
        //
        BufferedReader reader = new BufferedReader(new FileReader("/Users/julian/Documents/workspace/HumansService/Omata-ListOfInstagramAccounts.txt"));
        // BufferedWriter writer = new BufferedWriter(new FileWriter("/Users/julian/Documents/workspace/HumansService/Omata-ListOfInstagramAccounts-Output.txt"));
        ArrayList<String> listOfUserNames = new ArrayList<String>();
        for(String s = reader.readLine(); s != null && s.length() > 0; s = reader.readLine()) {
            if(listOfUserNames.contains(s) == true) {
                continue;
            }
            listOfUserNames.add(s);
        }


        // Find the people we @omata.la follow to be sure
        InstagramUser me = InstagramAnalyticsService.staticGetLocalUserBasicForUsername("omata.la");
        List<InstagramUser> follows = instagram.getInstagramUserFriendsAsList(me);
        follows.forEach((instagramUser) -> {
            String a_follow = instagramUser.getUsername();
            if(listOfUserNames.contains(a_follow) == false) {
                listOfUserNames.add(a_follow);
            }
        });

        // Find the people that @rhnewman follows also
        InstagramUser rhys = InstagramAnalyticsService.staticGetLocalUserBasicForUsername("rhnewman");
        follows = instagram.getInstagramUserFriendsAsList(rhys);
        follows.forEach((instagramUser) -> {
            String a_follow = instagramUser.getUsername();
            if(listOfUserNames.contains(a_follow) == false) {
                listOfUserNames.add(a_follow);
            }
        });

        // Find the people that @hellofosta follows also
        InstagramUser fosta = InstagramAnalyticsService.staticGetLocalUserBasicForUsername("hellofosta");
        follows = instagram.getInstagramUserFriendsAsList(fosta);
        follows.forEach((instagramUser) -> {
            String a_follow = instagramUser.getUsername();
            if(listOfUserNames.contains(a_follow) == false) {
                listOfUserNames.add(a_follow);
            }
        });



        //Gson gson = new Gson();
        //JsonElement inputListJson = new JsonArray();
        //JsonElement element = gson.toJsonTree(listOfUserNames);

        for(String userName : listOfUserNames) {
            logger.info("Capture Instagram analytics for ["+userName+"]..");
            InstagramUser user_from_db = InstagramAnalyticsService.getLocalUserBasicForUsername(userName);
            DateTime now = new DateTime(ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))));
            now.minusMonths(1);
            if(user_from_db != null) {

                instagram.captureInstagramUserAnalytics(user_from_db, ONE_DAY_BACK);


                instagram.captureInstagramUserAnalytics(user_from_db, SEVEN_DAYS_BACK);

                int month_days = Days.daysBetween(new LocalDate(now.minusMonths(MONTHS_BACK)), new LocalDate(now)).getDays();

                instagram.captureInstagramUserAnalytics(user_from_db, month_days);

                //if(instagram.localUserBasicIsFreshForUserID(user_from_db.getUserID())) {
                 InstagramUser user = instagram.serviceRequestUserBasicForUserID(user_from_db.getUserID());
                // save user basic just general in the user collection
                 instagram.saveUserBasicForAnalytics(user);
                // save user basic just general in the user collection
                instagram.saveUserBasic(user);
                //}
//                instagram.saveUserBasicForAnalytics(user_from_db);
                //continue;
            } else {
                //InstagramUser user = instagram.localUserSearch(userName);
                List<InstagramUser> users = instagram.serviceUserSearch(userName);
                if(users != null) {
                    users.removeIf(p -> (p.getUsername().equalsIgnoreCase(userName) == false));
                    if(users.size() == 1) {
                        // get the analytics
                        instagram.captureInstagramUserAnalytics(user_from_db, ONE_DAY_BACK);

                        instagram.captureInstagramUserAnalytics(users.get(0), SEVEN_DAYS_BACK);

                        int month_days = Days.daysBetween(new LocalDate(now.minusMonths(MONTHS_BACK)), new LocalDate(now)).getDays();
                        instagram.captureInstagramUserAnalytics(users.get(0), month_days);

                        // save user basic snapshot for analytics
                        instagram.saveUserBasicForAnalytics(users.get(0));
                        // save user basic just general in the user collection
                        instagram.saveUserBasic(users.get(0));
                    }
                }
            }
        }
    }


    @Test
    public void test_getListOfInstagramAnalyticsCollections() {
        List<String> collectionNames = instagram.getListOfInstagramAnalyticsCollections();
        collectionNames.forEach((name -> {
          logger.debug(name);

        }));
    }

    @Test
    public void test_serviceRequestFriendsAsUsers() throws Exception {
        List<InstagramUser> users = instagram.serviceUserSearch("darthjulian");
        InstagramUser user = null;
        List<InstagramUser> friends = null;
        if (users.size() > 0) {
            user = users.get(0);
            /*
            if(instagram.localFriendsIsFresh(user.getUserID())) {
                instagram.getLocalFriendsFor(user.getUserID());
            } else {
               friends = instagram.serviceRequestFriendsAsUsers(user.getUserID());
            }*/
            friends = instagram.serviceRequestFriendsAsUsers(user.getUserID());
//            logger.debug(friends);
//            instagram.getInstagramUsersForTopOfListAnalyticsJson(friends);
        }
        Gson gson = new Gson();
        String s = gson.toJson(friends);

        logger.debug(s);
    }

    @Test
    public void test_saveRootUserFriends() throws Exception {
        List<InstagramUser> users = instagram.serviceUserSearch("theradavist");
        InstagramUser user = null;
        List<InstagramUser> friends = null;
        if (users.size() > 0) {
            user = users.get(0);

            instagram.saveRootUserFriends(user);
        }


    }

    @Test
    public void test_saveUserBasicForAnalytics() {
        InstagramUser user = InstagramAnalyticsService.staticGetLocalUserBasicForUsername("darthjulian");
        instagram.saveUserBasicForAnalytics(user);

    }

    @Test
    public void test_serviceRequestFollowersAsUsers() throws Exception {
        List<InstagramUser> users = instagram.serviceUserSearch("rhnewman");
        InstagramUser user = null;
        List<InstagramUser> followers = null;
        if (users.size() > 0) {
            user = users.get(0);
            followers = instagram.serviceRequestFollowersAsUsers(user.getUserID());
            JsonElement result = instagram.getInstagramUsersForTopOfListAnalyticsJson(followers);
            logger.debug(result);
//            instagram.getInstagramUsersForTopOfListAnalyticsJson(friends);
        }

    }

    @Test
    public void test_getInstagramUserFriends() throws Exception {
        List<InstagramUser> users = instagram.serviceUserSearch("rhnewman");
        InstagramUser user = null;
        List<InstagramUser> followers = null;
        if (users.size() > 0) {
            user = users.get(0);
            JsonElement e = instagram.getInstagramUserFriendsAsJson(user);
            if(e.isJsonArray()) {
                // it's an array..
            }
        }


    }

    @Test
    public void test_getInstagramUserStatusAndAnalytics() throws Exception {
        List<InstagramUser> users = instagram.serviceUserSearch("rhnewman");
        InstagramUser user = null;
        if (users.size() > 0) {
            user = users.get(0);
            JsonElement e = instagram.getInstagramUserStatusAndAnalytics(user);
            JsonObject obj = instagram.getRootUserMeta(user);
            //obj.add("status", e);
            logger.debug("done");
            //logger.debug(obj);
        }
    }

    @Test
    public void foo() {
        DateTime now = new DateTime(ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))));
        now.minusMonths(1);
        int days = Days.daysBetween(new LocalDate(now), new LocalDate(now.minusMonths(1))).getDays();
        logger.debug("days "+days);
    }


    @Test
    public void test_getInstagramUserFriendsAsList() throws Exception {
        List<InstagramUser> users = instagram.serviceUserSearch("omata.la");
        try {
            InstagramUser user = null;
            List<InstagramFriend> friends = null;
            if (users.size() > 0) {
                user = users.get(0);
                friends = instagram.serviceRequestFriends(user.getUserID(), true);
                logger.debug("");
            }

        }catch(Exception e) {
            logger.warn(e);
        }
    }
}