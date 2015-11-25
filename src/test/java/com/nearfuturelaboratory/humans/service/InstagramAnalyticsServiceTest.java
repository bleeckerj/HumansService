package com.nearfuturelaboratory.humans.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramUser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramFriend;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramStatus;
import com.nearfuturelaboratory.humans.service.InstagramService;
import com.nearfuturelaboratory.util.Constants;

import java.util.List;

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
        List<InstagramUser> users =  instagram.usersSearch("theradavist");
        InstagramUser user = null;
        if(users.size() > 0) {
            user = users.get(0);
            instagram.saveRootUser(user);
            //List<InstagramStatus> status = instagram.serviceRequestStatusForUserIDToMonthsAgo(user.getUserID(), 3);
            //logger.debug(status);


        }
    }

    @Test
    public void test_getRootUser() throws Exception {
        List<InstagramUser> users = instagram.usersSearch("theradavist");
        InstagramUser user = null;
        if (users.size() > 0) {
            user = users.get(0);
            JsonObject obj = instagram.getRootUser(user);
            logger.debug(obj);
        }
    }

    @Test
    public void test_getInstagramUserStatus() throws Exception {
        List<InstagramUser> users = instagram.usersSearch("theradavist");
        InstagramUser user = null;
        if (users.size() > 0) {
            user = users.get(0);
            JsonElement e = instagram.getInstagramUserStatus(user);
            JsonObject obj = instagram.getRootUser(user);
            obj.add("status", e);
            logger.debug(obj);
        }
    }
}