package com.nearfuturelaboratory.humans.test;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.nearfuturelaboratory.humans.instagram.entities.InstagramFollows;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramStatus;
import com.nearfuturelaboratory.humans.service.InstagramService;
import com.nearfuturelaboratory.util.Constants;

public class InstagramServiceTest {
	static InstagramService instagram;
	final static Logger logger = Logger.getLogger(com.nearfuturelaboratory.humans.test.InstagramServiceTest.class);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	
		try {
			Constants.load("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/WebContent/WEB-INF/lib/dev.app.properties");
			PropertyConfigurator.configureAndWatch("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/WebContent/WEB-INF/lib/static-logger.properties");
			//logger.debug("Hey Ho!");
			instagram = InstagramService.createInstagramServiceOnBehalfOfUsername("darthjulian");

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
	public void getFollows() {
		List<InstagramFollows> result = instagram.getFollows();
		for(InstagramFollows f : result) {
			logger.debug(f.getFriend().getUsername());
		}
		instagram.getFollows();
//		instagram.getFollows("11394571");
	}
	
	@Test
	public void test_serviceRequestFollows() {
		try {
		instagram.serviceRequestFollows();
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
	}
}
