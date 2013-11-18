package com.nearfuturelaboratory.humans.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.QueryResults;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.nearfuturelaboratory.humans.dao.HumansUserDAO;
import com.nearfuturelaboratory.humans.dao.InstagramStatusDAO;
import com.nearfuturelaboratory.humans.entities.Human;
import com.nearfuturelaboratory.humans.entities.HumansUser;
//import com.nearfuturelaboratory.humans.entities.InstagramStatus;
import com.nearfuturelaboratory.humans.service.status.InstagramStatus;
import com.nearfuturelaboratory.humans.entities.ServiceUser;
import com.nearfuturelaboratory.humans.util.MongoUtil;
import com.nearfuturelaboratory.util.Constants;

public class TestHumansDAO {
	final static Logger logger = Logger.getLogger(com.nearfuturelaboratory.humans.test.TestHumansDAO.class);

	public static void main(String[] args) {
		try {
			Constants.load("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/WebContent/WEB-INF/lib/dev.app.properties");
			PropertyConfigurator.configureAndWatch("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/WebContent/WEB-INF/lib/static-logger.properties");
			logger.debug("Hey Ho!");
		} catch(Exception e) {
			e.printStackTrace();
		}

		Mongo mongo = MongoUtil.getMongo();
		Morphia morphia = new Morphia();
		Gson gson = new Gson();

		
		
		DB db = mongo.getDB("instagram");
		DBCollection coll = db.getCollection("instagram.status.1342246");
		DBCursor cursor = coll.find();
		try {
			while(cursor.hasNext()) {
				DBObject o = (DBObject) cursor.next();
				try {
					//InstagramStatus i = gson.fromJson(o.toString(), InstagramStatus.class);
					//					logger.debug(i.getCaptionText()+" "+i.getImageURL_StandardResolution()+" ");

					com.nearfuturelaboratory.humans.instagram.entities.InstagramStatus i = gson.fromJson(o.toString(), com.nearfuturelaboratory.humans.instagram.entities.InstagramStatus.class);
					logger.debug(i+" "+i.getUser());
				} catch(Exception e) {
					e.printStackTrace();
					logger.error(e);
					logger.info(o);
				}
			}
		} finally {
			cursor.close();
		}

		//		InstagramStatusDAO instagramStatusDao = new InstagramStatusDAO("1342246");
		//		QueryResults<InstagramStatus> status = instagramStatusDao.find();
		//		for(InstagramStatus i : status) {
		//			logger.debug(i+" "+i.getId());
		//		}

		HumansUserDAO daoTest = new HumansUserDAO();//new HumansUserDAO(mongo, morphia, "humans");
		try {
			daoTest.ensureIndexes();
		}catch(Exception e) {
			logger.error(e);
		}

		List<HumansUser> results = daoTest.findByUsername("nicolas");
		//daoTest.delete(results.get(0));
		HumansUser one = results.get(0);

		List<HumansUser> l = daoTest.getAllHumansUsers();



		HumansUser h = daoTest.findOneByUsername("darthjulian");
		h.getServiceNamesAssigned();
		h.getServices();

		HumansUser user = new HumansUser();
		user.addService("twitter", "darthjulian", "99993333");
		user.setEmail("bleeckerj@gmail.com");
		user.setUsername("test");
		user.setPassword("test");
		Human human = new Human();
		human.setName("Test Human");

		ServiceUser service_user = new ServiceUser();
		service_user.setOnBehalfOf("testOnBehalfOf", "anIdForTest");
		service_user.setService("twitter");
		service_user.setServiceID("abcdefghijklmnop");
		service_user.setUsername("someoneelse");
		service_user.setImageURL("https://irs0.4sqi.net/img/user/height48/54DBU2G2JAMKZTLI.jpg");

		human.setServiceUsers(new ArrayList<ServiceUser>(Arrays.asList(service_user)));


		user.setHumans(new ArrayList<Human>(Arrays.asList(human)));

		user.addService("flickr", "test-flickr-user", "0987654321@1");
		user.addService("twitter", "test-twitter-user", "1898763729");

		Key<HumansUser>k = null;
		try {
			k = daoTest.save(user);
		} catch(Exception e) {
			logger.warn(e);
		}

		logger.debug(k);

		List<HumansUser> r = daoTest.findByUsername("test");
		daoTest.delete(results.get(0));


		//		
		//		Key k;
		results = daoTest.findByUsername("darthjulian");
		for(HumansUser result : results) {
			logger.debug(result);
			result.setUsername("darthmoolian");

			result.addService("flickr", "darthjulian", "74747474747");
			logger.debug(result.getAllHumans());

			k = daoTest.save(result);
			logger.debug(k);

			result.setUsername("darthjulian");
			k = daoTest.save(result);
			logger.debug(k);
		}


		results = daoTest.find().asList();
		for(HumansUser result : results) {
			logger.debug(result.getUsername());
			daoTest.save(result);
		}



		//daoTest.save(entity, WriteConcern.)

	}



}
