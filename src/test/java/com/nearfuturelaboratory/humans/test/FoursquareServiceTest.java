package com.nearfuturelaboratory.humans.test;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
//import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.nearfuturelaboratory.humans.dao.FoursquareFriendDAO;
import com.nearfuturelaboratory.humans.foursquare.entities.FoursquareFriend;
import com.nearfuturelaboratory.humans.service.FoursquareService;
import com.nearfuturelaboratory.humans.service.InstagramService;
import com.nearfuturelaboratory.util.Constants;

public class FoursquareServiceTest {
	static FoursquareService foursquare;
	final static Logger logger = LogManager.getLogger("com.nearfuturelaboratory.humans.test.Test");

	public static void main(String[] args) throws Exception {
		FoursquareServiceTest test = new FoursquareServiceTest();
		FoursquareServiceTest.setUpBeforeClass();
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		try {
			Constants.load("/Users/julian/Documents/workspace/HumansService/src/main/webapp/WEB-INF/lib/dev.app.properties");
			//PropertyConfigurator.configureAndWatch("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/src/main/webapp/WEB-INF/lib/static-logger.properties");
			foursquare = FoursquareService.createFoursquareServiceOnBehalfOfUserID("91181");

			//logger.debug("Hey Ho!");
		} catch(Exception e) {
			e.printStackTrace();
		}

	}

    @Test
    public void testgetStatusCountForUserID() throws Exception {

    }

    @Test
	public void test_serviceRequestUserBasic() {
		try {
		foursquare.serviceRequestUserBasic();
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
//		fail("Not yet implemented");
	}
	
	@Test
	public void test_ServiceRequestCheckins() {
		try {
			foursquare.serviceRequestCheckins();
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Test
	public void test_getFriends() {
		try {
			FoursquareFriend f;
			FoursquareFriendDAO followsDAO = new FoursquareFriendDAO();
			followsDAO.ensureIndexes();
			foursquare.serviceRequestFriends();

			List<FoursquareFriend> friends = foursquare.getFriends();
			
			f = friends.get(0);
			logger.debug(f);
			//			followsDAO.updateLastUpdated(f);
//			
//			followsDAO.save(f);

//			for(FoursquareFriend friend : friends) {
//				//logger.debug(friend);
//				followsDAO.createUpdateOperations();
//				followsDAO.save(friend);
//			}
			
		} catch(Exception e) {
			logger.error("",e);
			e.printStackTrace();
		}
	}
	
	// I don't know how you test these..
	
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
