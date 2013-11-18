package com.nearfuturelaboratory.humans.test;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.nearfuturelaboratory.humans.foursquare.entities.FoursquareFriend;
import com.nearfuturelaboratory.humans.service.FoursquareService;
import com.nearfuturelaboratory.humans.service.InstagramService;
import com.nearfuturelaboratory.util.Constants;

public class FoursquareServiceTest {
	static FoursquareService foursquare;
	final static Logger logger = Logger.getLogger("com.nearfuturelaboratory.humans.test.Test");

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		try {
			Constants.load("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/WebContent/WEB-INF/lib/dev.app.properties");
			PropertyConfigurator.configureAndWatch("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/WebContent/WEB-INF/lib/static-logger.properties");
			foursquare = FoursquareService.createFoursquareServiceOnBehalfOfUserID("41");

			//logger.debug("Hey Ho!");
		} catch(Exception e) {
			e.printStackTrace();
		}

	}

	@Ignore
	public void test_serviceRequestUserBasic() {
		try {
		foursquare.serviceRequestUserBasic();
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
//		fail("Not yet implemented");
	}
	
	@Ignore
	public void test_ServiceRequestCheckins() {
		try {
			foursquare.serviceRequestCheckins();
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Ignore
	public void test_GetFollows() {
		try {
			foursquare.serviceRequestFollows();
			List<FoursquareFriend> friends = foursquare.getFriends();
			for(FoursquareFriend friend : friends) {
				logger.debug(friend);
			}
			
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Ignore
	public void test_GetLatestCheckin() {
		try {
			logger.debug(foursquare.getLatestCheckin());
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Ignore
	public void test_ServiceRequestLatestCheckins() {
		try {
			foursquare.serviceRequestLatestCheckins();
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}

}
