package com.nearfuturelaboratory.humans.service;

import com.nearfuturelaboratory.humans.twitter.entities.TwitterFriend;
import com.nearfuturelaboratory.humans.twitter.entities.generated.TwitterStatus;
import com.nearfuturelaboratory.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hamcrest.Matchers;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileReader;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

//import org.apache.log4j.PropertyConfigurator;

public class TwitterServiceTest {
    static TwitterService twitter;
    final static Logger logger = LogManager.getLogger(com.nearfuturelaboratory.humans.service.TwitterServiceTest.class);

    public static void main(String[] args) throws Exception {
        TwitterServiceTest test = new TwitterServiceTest();
        TwitterServiceTest.setUpBeforeClass();
        test.test_serviceRequestFollows();
        logger.debug("Done");
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        try {
            Constants.load("/Users/julian/Documents/workspace/HumansService/src/main/webapp/WEB-INF/lib/dev.app.properties");
            //PropertyConfigurator.configureAndWatch("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/src/main/webapp/WEB-INF/lib/static-logger.properties");
            logger.debug("Hey Ho Test!");
            twitter = TwitterService.createTwitterServiceOnBehalfOfUsername("darthjulian");
            logger.debug(twitter);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void getMostRecentStatus() {
        TwitterStatus status = twitter.getMostRecentStatus();
        assertThat(status, notNullValue());
        assertThat(status.getIdStr(), notNullValue());
    }


    @Test
    public void serviceRequestStatusForUserID() {
        List<TwitterStatus> status = twitter.serviceRequestStatusForUserID("6825792");
        assertThat(status, Matchers.notNullValue());
        assertThat(status, hasSize(200));
        assertThat(status, everyItem(isA(TwitterStatus.class)));
        //assertThat(status, everyItem(is(instanceOf(TwitterStatus.class))));


    }

    @Test
    public void serviceRequestStatusForUserIDAndSinceID() {
        List<TwitterStatus> status = twitter.serviceRequestStatusForUserIDAndSinceID("185383", null);
        assertThat(status, Matchers.notNullValue());
        //assertThat(status, hasSize(200));
        assertThat(status, everyItem(isA(TwitterStatus.class)));


    }


    @Ignore
    public void test_serviceRequestUserBasic() {
        try {
            twitter.serviceRequestUserBasic();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Ignore
    public void test_getLargeImageURL() {
        List<TwitterFriend> follows = twitter.getFriends();
        for (TwitterFriend friend : follows) {
            logger.debug(friend.getLargeImageURL());
        }
    }

    @Ignore
    public void test_serviceRequestStatus() {
        try {
            twitter.serviceRequestStatus();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test_getMostRecentStatus() {
        try {
            TwitterStatus status = twitter.getMostRecentStatus();
            logger.debug(status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Ignore
    public void test_serviceRequestFollows() {
//        List<TwitterFriend> follows = twitter.serviceRequestFollows();
//
//        assertThat(follows, notNullValue());

    }

    @Test
    public void test_getFollows() {
        List<TwitterFriend> friends = twitter.getFriends();
        for (TwitterFriend friend : friends) {
            logger.debug(friend);
            break;
        }
    }

    @Ignore
    public void test_localFollowsIsFresh() {
        twitter.localFriendsIsFresh();
    }

    @Test
    public void testSaveStatusJson() throws Exception {

        FileReader reader = new FileReader("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/src/main/resources/json/TwitterStatus.json");
        JSONArray status = (JSONArray) JSONValue.parse(reader);
        //JSONArray array = new JSONArray();
        //array.add(status);

        //TwitterService offline = new TwitterService();

        List<TwitterStatus> result = twitter.saveStatusJson(status);

        assertThat(result, notNullValue());
        assertThat(result, hasSize(1));
        assertTrue("They're supposed to be equal", result.equals(status.get(0)));

    }
}
