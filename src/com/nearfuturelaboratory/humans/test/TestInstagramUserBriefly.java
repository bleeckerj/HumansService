package com.nearfuturelaboratory.humans.test;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.mongodb.morphia.Morphia;

import com.mongodb.Mongo;
import com.nearfuturelaboratory.humans.dao.InstagramFollowsDAO;
import com.nearfuturelaboratory.humans.service.InstagramService;
import com.nearfuturelaboratory.humans.util.MongoUtil;
import com.nearfuturelaboratory.util.Constants;

public class TestInstagramUserBriefly {
	final static Logger logger = Logger.getLogger(com.nearfuturelaboratory.humans.test.TestInstagramUserBriefly.class);

	public static void main(String[] args) {
		try {
			Constants.load("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/WebContent/WEB-INF/lib/dev.app.properties");
			PropertyConfigurator.configureAndWatch("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/WebContent/WEB-INF/lib/static-logger.properties");
			logger.debug("Hey Ho!");
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		InstagramFollowsDAO dao = new InstagramFollowsDAO();
		InstagramService instagram = InstagramService.createInstagramServiceOnBehalfOfUsername("darthjulian");
		instagram.getFollows();

	}

}
