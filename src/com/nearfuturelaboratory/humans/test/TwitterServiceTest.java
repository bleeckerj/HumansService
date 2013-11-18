package com.nearfuturelaboratory.humans.test;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;

import com.nearfuturelaboratory.humans.service.TwitterService;
import com.nearfuturelaboratory.humans.twitter.entities.TwitterStatus;
import com.nearfuturelaboratory.util.Constants;

public class TwitterServiceTest {
	static TwitterService twitter;
	final static Logger logger = Logger.getLogger("com.nearfuturelaboratory.humans.test.Test");

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	
		try {
			Constants.load("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/WebContent/WEB-INF/lib/dev.app.properties");
			PropertyConfigurator.configureAndWatch("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/WebContent/WEB-INF/lib/static-logger.properties");
			//logger.debug("Hey Ho!");
			twitter = TwitterService.createTwitterServiceOnBehalfOfUsername("nicolasnova");

		} catch(Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testServiceRequestUserBasic() {
		try {
		twitter.serviceRequestUserBasic();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testServiceRequestStatus() {
		try {
			twitter.serviceRequestStatus();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void test_getMostRecentStatus() {
		try {
			TwitterStatus status = twitter.getMostRecentStatus();
			logger.debug(status);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testServiceRequestFollows() {
		try {
			twitter.serviceRequestFollows();
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
	}

}
