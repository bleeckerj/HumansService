package com.nearfuturelaboratory.humans.test;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.nearfuturelaboratory.humans.service.TwitterService;
import com.nearfuturelaboratory.humans.twitter.entities.TwitterFriend;
import com.nearfuturelaboratory.humans.twitter.entities.TwitterStatus;
import com.nearfuturelaboratory.util.Constants;

public class TwitterServiceTest {
	static TwitterService twitter;
	final static Logger logger = Logger.getLogger(com.nearfuturelaboratory.humans.test.TwitterServiceTest.class);

	public static void main(String[] args) throws Exception {
		TwitterServiceTest test = new TwitterServiceTest();
		TwitterServiceTest.setUpBeforeClass();
		test.test_serviceRequestFollows();
		logger.debug("Done");
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	
		try {
			Constants.load("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/WebContent/WEB-INF/lib/dev.app.properties");
			PropertyConfigurator.configureAndWatch("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/WebContent/WEB-INF/lib/static-logger.properties");
			logger.debug("Hey Ho!");
			twitter = TwitterService.createTwitterServiceOnBehalfOfUsername("darthjulian");
			logger.debug(twitter);
		} catch(Exception e) {
			e.printStackTrace();
		}

	}

	@Ignore
	public void test_serviceRequestUserBasic() {
		try {
		twitter.serviceRequestUserBasic();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Ignore
	public void test_getLargeImageURL() {
		List<TwitterFriend>follows = twitter.getFriends();
		for(TwitterFriend friend : follows) {
			logger.debug(friend.getLargeImageURL());
		}
	}
	
	@Ignore
	public void test_serviceRequestStatus() {
		try {
			twitter.serviceRequestStatus();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Ignore
	public void test_getMostRecentStatus() {
		try {
			TwitterStatus status = twitter.getMostRecentStatus();
			logger.debug(status);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void test_serviceRequestFollows() {
		try {
			twitter.serviceRequestFollows();
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("",e);
		}
	}
	
	@Test
	public void test_getFollows() {
		List<TwitterFriend> friends = twitter.getFriends();
		for(TwitterFriend friend : friends) {
			logger.debug(friend);
			break;
		}
	}
	
	@Ignore
	public void test_localFollowsIsFresh() {
		twitter.localFriendsIsFresh();
	}

}
