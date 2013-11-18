package com.nearfuturelaboratory.humans.test;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.nearfuturelaboratory.humans.service.FlickrService;
import com.nearfuturelaboratory.humans.service.FoursquareService;
import com.nearfuturelaboratory.util.Constants;

public class FlickrServiceTest {
	static FlickrService flickr;
	final static Logger logger = Logger.getLogger("com.nearfuturelaboratory.humans.test.Test");

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		try {
			Constants.load("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/WebContent/WEB-INF/lib/dev.app.properties");
			PropertyConfigurator.configureAndWatch("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/WebContent/WEB-INF/lib/static-logger.properties");
			flickr = FlickrService.createFlickrServiceOnBehalfOfUserID("66854529@N00");

			//logger.debug("Hey Ho!");
		} catch(Exception e) {
			e.printStackTrace();
		}

	}

	@Ignore
	public void test_serviceGetFollows() {
		try {
			flickr.serviceRequestFollows();
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Test
	public void test_serviceRequestStatus() {
		try {
			flickr.serviceRequestStatus();
		}catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}

}
