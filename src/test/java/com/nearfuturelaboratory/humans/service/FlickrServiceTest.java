package com.nearfuturelaboratory.humans.service;

import org.apache.log4j.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.fail;

import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.nearfuturelaboratory.humans.dao.FlickrUserDAO;
import com.nearfuturelaboratory.humans.exception.BadAccessTokenException;
import com.nearfuturelaboratory.humans.flickr.entities.FlickrUser;
import com.nearfuturelaboratory.humans.service.FlickrService;
import com.nearfuturelaboratory.util.Constants;

import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;


public class FlickrServiceTest {
	static FlickrService flickr;
	final static Logger logger = Logger.getLogger("com.nearfuturelaboratory.humans.test.Test");

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		try {
			Constants.load("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/src/main/webapp/WEB-INF/lib/dev.app.properties");
			PropertyConfigurator.configureAndWatch("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/src/main/webapp/WEB-INF/lib/static-logger.properties");
			flickr = FlickrService.createFlickrServiceOnBehalfOfUserID("66854529@N00");

			//logger.debug("Hey Ho!");
		} catch(Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testServiceRequestUserBasic()
	{
		try {
			FlickrUser user = flickr.serviceRequestUserBasic();
//			FlickrUserDAO dao = new FlickrUserDAO();
//			dao.save(user);
			assertThat("user ids do not match", user.getId(), equalTo(flickr.getThisUser().getId()));
		} catch(BadAccessTokenException bate) {
			logger.error(bate);
			bate.printStackTrace();
			fail(bate.getMessage());
		}

	}

	
	
	@Ignore
	public void test_serviceRequestFriends() {
		try {
			flickr.serviceRequestFriends();
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}

	@Ignore
	public void test_serviceRequestStatus() {
		try {
			flickr.serviceRequestStatus();
		}catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}

}
