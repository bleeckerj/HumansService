package com.nearfuturelaboratory.humans.test;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.nearfuturelaboratory.humans.service.InstagramService;
import com.nearfuturelaboratory.util.Constants;

public class InstagramServiceTest {
	static InstagramService instagram;
	final static Logger logger = Logger.getLogger("com.nearfuturelaboratory.humans.test.Test");

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	
		try {
			Constants.load("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/WebContent/WEB-INF/lib/dev.app.properties");
			PropertyConfigurator.configureAndWatch("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/WebContent/WEB-INF/lib/static-logger.properties");
			//logger.debug("Hey Ho!");
		} catch(Exception e) {
			e.printStackTrace();
		}
		instagram = InstagramService.createInstagramServiceOnBehalfOfUsername("darthjulian");

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void test() {
		//Token t = instagram.deserializeToken(aUser)
		instagram.serviceRequestStatusForUserID("self");
		
		//fail("Not yet implemented");
	}
	
	@Test
	public void getFollows() {
		instagram.getFollows("294198486");
//		instagram.getFollows();
//		instagram.getFollows("11394571");
	}
	
	@Test
	public void requestFollows() {
		
	}
}
