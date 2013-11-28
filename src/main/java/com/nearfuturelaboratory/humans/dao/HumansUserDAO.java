package com.nearfuturelaboratory.humans.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Criteria;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.QueryResults;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;

import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.WriteConcern;
import com.nearfuturelaboratory.humans.config.MongoDB;
import com.nearfuturelaboratory.humans.entities.ServiceUser;
import com.nearfuturelaboratory.humans.entities.HumansUser;
import com.nearfuturelaboratory.humans.entities.Human;
import com.nearfuturelaboratory.humans.util.MongoUtil;
import com.nearfuturelaboratory.util.Constants;

import java.util.regex.Pattern;

public class HumansUserDAO extends BasicDAO<HumansUser, ObjectId> {

	final static Logger logger = Logger.getLogger(com.nearfuturelaboratory.humans.dao.HumansUserDAO.class);
	protected static Mongo mongo;

	public HumansUserDAO() {
		super(MongoUtil.getMongo(), new Morphia(), "humans");
	}
	
	public HumansUserDAO(String dbname) {
		super(MongoUtil.getMongo(), new Morphia(), dbname);
	}
	
	public HumansUserDAO(Mongo mongo, Morphia morphia, String dbname) {
		super(mongo, morphia, dbname);
		//initiate();
	}
	
	
	public List<HumansUser> findByUsername( String aUsername ) {
	    Pattern regExp = Pattern.compile(aUsername + ".*", Pattern.CASE_INSENSITIVE);
	    //ds.find(entityClazz).filter("username", regExp);
	    
	    return this.getDs().find(getEntityClazz()).filter("username", regExp).order("username").asList();// .sort("username").asList();
	}
	
	
	public List<HumansUser> getAllHumansUsers() {
		return getDs().find(getEntityClazz()).order("username").asList();
	}
	
	public List<String>getAllHumansUsers_Usernames() {
		List<String> result = new ArrayList<String>();
		List<HumansUser> all = getAllHumansUsers();
		for(HumansUser h : all) {
			result.add(h.getUsername());
		}
		return result;
	}
	/**
	 * Find a HumansUser given a specific ID of a human it contains
	 * 
	 * @param aHumanID
	 * @return
	 */
	public HumansUser findByHumanID(String aHumanID) {
		Query<HumansUser> q = this.getDs().createQuery(HumansUser.class).
				filter("humans._id", new ObjectId(aHumanID));
		return findOne(q);
	}
	
//	public boolean doesUsernameExist(String aUsername) {
//		boolean result = false;
//		HumansUser h = findOneByUsername(aUsername);
//		
//	}
	
	public HumansUser findOneByUsername(String aUsername) {		
		Query<HumansUser> q = this.createQuery().field("username").equal(aUsername);
		return this.findOne(q);
	}

	public HumansUser getHumansUser(String aUsername, String aPassword) {
		HumansUser h = null;
		h = findOneByUsername(aUsername);
		if(h != null && h.verifyPassword(aPassword)) {
			return h;
		} else {
			return null;
		}	
	}
}
