package com.nearfuturelaboratory.humans.service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.nearfuturelaboratory.util.Constants;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;

/**
 * Created by julian on 11/23/15.
 */
public class InstagramAnalyticsServiceTest {
    static InstagramAnalyticsService instagram;
    final static Logger logger = LogManager.getLogger(com.nearfuturelaboratory.humans.service.InstagramAnalyticsServiceTest.class);


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
        List<InstagramUser> users =  instagram.usersSearch("darthjulian");
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
    public void test_getAnalyticsDocumentsByUsername() throws Exception {
        // start with a username_userid
        ArrayList<Document> result = new ArrayList<>();
        MongoDatabase db = MongoUtil.getMongo().getDatabase("instagram-analytics");
        InstagramUser user = instagram.userSearch("rapha");
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
        //Gson gson = new Gson();
        //JsonElement inputListJson = new JsonArray();
        //JsonElement element = gson.toJsonTree(listOfUserNames);

        for(String userName : listOfUserNames) {
            logger.info("Capture Instagram analytics for ["+userName+"]..");
            InstagramUser user_from_db = InstagramAnalyticsService.getLocalUserBasicForUsername(userName);
            if(user_from_db != null) {
                instagram.captureInstagramUserAnalytics(user_from_db, 7);
                instagram.saveUserBasicForAnalytics(user_from_db);
                continue;
            }

            List<InstagramUser> users = instagram.usersSearch(userName);
            if(users == null || users.size() < 1) {
                logger.warn("Couldn't find username = "+userName+". Skipping..");

            } else {
                InstagramUser user = null;
                if (users.size() > 0) {
                    user = users.get(0);
                    instagram.captureInstagramUserAnalytics(user, 7);
                    instagram.saveUserBasicForAnalytics(user);
                    //logger.debug(obj);
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
        List<InstagramUser> users = instagram.usersSearch("darthjulian");
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
        List<InstagramUser> users = instagram.usersSearch("theradavist");
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
        List<InstagramUser> users = instagram.usersSearch("rhnewman");
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
        List<InstagramUser> users = instagram.usersSearch("rhnewman");
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
        List<InstagramUser> users = instagram.usersSearch("rhnewman");
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
    public void test_getInstagramUserFriendsAsList() throws Exception {
        List<InstagramUser> users = instagram.usersSearch("omata.la");
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