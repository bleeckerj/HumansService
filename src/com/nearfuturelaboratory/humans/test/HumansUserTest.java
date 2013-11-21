package com.nearfuturelaboratory.humans.test;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mongodb.morphia.Key;

import com.nearfuturelaboratory.humans.dao.HumansUserDAO;
import com.nearfuturelaboratory.humans.entities.HumansUser;
import com.nearfuturelaboratory.humans.entities.ServiceEntry;
import com.nearfuturelaboratory.util.Constants;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HumansUserTest {
	final static Logger logger = Logger.getLogger(com.nearfuturelaboratory.humans.test.HumansUserTest.class);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		try {
			Constants.load("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/WebContent/WEB-INF/lib/dev.app.properties");
			PropertyConfigurator.configureAndWatch("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/WebContent/WEB-INF/lib/static-logger.properties");
			//logger.debug("Hey Ho!");
		} catch(Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void test_getServices() {
		HumansUserDAO dao = new HumansUserDAO();
		HumansUser user = dao.findOneByUsername("darthjulian");
		//dao.save(user);
		List<ServiceEntry> service = user.getServices();
		Iterator<ServiceEntry> iter = service.iterator();
		while(iter.hasNext()) {
			ServiceEntry e = iter.next();
			logger.debug(e);
		}

		logger.debug(service);
		//		fail("Not yet implemented");
	}

	@Test
	public void test_addService() {
		HumansUserDAO dao = new HumansUserDAO();
		HumansUser user = dao.findOneByUsername("darthjulian");

		user.addService("1", "test", "noservice");
		Key k = dao.save(user);
		logger.debug(k);
		
	}
	
	@Test
	public void test_removeService() {
		HumansUserDAO dao = new HumansUserDAO();
		HumansUser user = dao.findOneByUsername("darthjulian");

		user.removeService("1", "test", "noservice");
		Key k = dao.save(user);
		logger.debug(k);
		
	}
	
	@Test
	public void getFollows() {
		HumansUserDAO dao = new HumansUserDAO();
		HumansUser user = dao.findOneByUsername("darthjulian");
		List<Object> result = user.getFriends();
		logger.debug(result);
	}

}
