package com.nearfuturelaboratory.humans.service;

import java.util.Date;
import java.util.List;

import com.google.gson.JsonElement;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramUser;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
//import org.apache.log4j.PropertyConfigurator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.nearfuturelaboratory.humans.instagram.entities.InstagramFriend;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramStatus;
import com.nearfuturelaboratory.humans.service.InstagramService;
import com.nearfuturelaboratory.util.Constants;

public class InstagramServiceTest {
	static InstagramService instagram;
	final static Logger logger = LogManager.getLogger(com.nearfuturelaboratory.humans.service.InstagramServiceTest.class);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	
		try {
			Constants.load("/Users/julian/Documents/workspace/HumansService/src/main/webapp/WEB-INF/lib/dev.app.properties");
			//PropertyConfigurator.configureAndWatch("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/WebContent/WEB-INF/lib/static-logger.properties");
			//logger.debug("Hey Ho!");
			instagram = InstagramService.createServiceOnBehalfOfUsername("darthjulian");

		} catch(Exception e) {
			e.printStackTrace();
		}

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Ignore
	public void test_freshenStatus() {
		instagram.freshenStatus();
	}

    @Test
    public void serviceRequestStatus()
    {
        List<InstagramStatus> status = instagram.serviceRequestStatus();
        logger.debug(status);
    }

	@Test
	public void serviceRequestFollowersAsUsers()
	{
		instagram.serviceRequestFollowersAsUsers();
	}


    @Test
    public void serviceRequestStatusForUserID()
    {
        String userid = instagram.getThisUser().getId();
        List<InstagramStatus> status = instagram.serviceRequestStatusForUserID(userid);
        logger.debug(status);
    }

	@Ignore
	public void test() {
		//Token t = instagram.deserializeToken(aUser)
		List<InstagramStatus> status = instagram.getStatus();
		for(InstagramStatus s : status) {
			logger.debug(s.getStatusJSON().toString());
		}
		//fail("Not yet implemented");
	}
	
	@Test
	public void test_getFollows() {
		List<InstagramFriend> result = instagram.getFriends();
		for(InstagramFriend f : result) {
			logger.debug(f.getFriend().getUsername()+" "+f.getOnBehalfOf().getServiceUsername() + " "+f.getOnBehalfOf().getServiceName());
		}
		instagram.getFriends();
//		instagram.getFollows("11394571");
	}

    @Test
    public void test_serviceRequestStatusByMediaID() {
        List<InstagramStatus> result = instagram.serviceRequestStatusByMediaID("734865045431656146_48029998");
        logger.debug(result);
    }

    @Test
    public void test_serviceLikeStatusByMediaID() {
        JsonElement element = instagram.serviceLikeStatusByMediaID("729014530937658462_1342246");
        logger.debug(element);
    }


    @Test
    public void test_serviceRequestStatusForUserIDFromMonthsAgo() {
        List<InstagramStatus> result = instagram.serviceRequestStatusForUserIDFromMonthsAgo(instagram.getThisUser().getUserID(), 12);
        logger.debug(result);
    }

	@Test
	public void test_getStatusForUserID() {
		List<InstagramStatus> status = instagram.getStatusForUserID("10282731");

		logger.debug("Hello");
	}

	@Test
	public void test_getLocalStatusByExactUserIDToMonthsAgo() {
		List<InstagramStatus> status = instagram.getLocalStatusByExactUserIDToMonthsAgo("10282731", 3);
		Date a = null;
		Date b = null;
		if(status != null) {
			a = status.get(0).getCreatedDate();
			b = status.get(status.size()-1).getCreatedDate();

		}
		logger.debug("Hello");
	}

	@Test
	public void test_serviceRequestUserBasicForUserID() {
		try {
			instagram.serviceRequestUserBasicForUserID("696578");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

    @Ignore
	public void test_serviceRequestFollows() {
		try {
		instagram.serviceRequestFriends();
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
	}

	@Test
	public void test_localRequestFriendsFo() {
		List<InstagramFriend> list = instagram.getLocalFriendsFor("696578");
		logger.debug("");
	}

	@Test
	public void test_getLocalUserBasicForUsername() {
		InstagramUser user = instagram.getLocalUserBasicForUsername("foobarbaz");
		logger.debug(user);
	}

}
