package com.nearfuturelaboratory.humans.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
//import org.apache.log4j.PropertyConfigurator;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
//import org.mongodb.morphia.Key;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;
//import org.mongodb.morphia.query.Criteria;
import org.mongodb.morphia.query.Query;
//import org.mongodb.morphia.query.QueryResults;
//import org.mongodb.morphia.query.UpdateOperations;
//import org.mongodb.morphia.query.UpdateResults;
//
//import com.mongodb.DBCollection;
import com.mongodb.Mongo;
//import com.nearfuturelaboratory.humans.config.MongoDB;
//import com.nearfuturelaboratory.humans.entities.ServiceUser;
import com.nearfuturelaboratory.humans.entities.HumansUser;
//import com.nearfuturelaboratory.humans.entities.Human;
import com.nearfuturelaboratory.humans.util.MongoUtil;
//import com.nearfuturelaboratory.util.Constants;

import java.util.regex.Pattern;

public class HumansUserDAO extends BasicDAO<HumansUser, ObjectId> {

	final static Logger logger = LogManager.getLogger(com.nearfuturelaboratory.humans.dao.HumansUserDAO.class);
	protected static Mongo mongo;

	public HumansUserDAO() {
		super(MongoUtil.getMongo(), new Morphia(), "humans");
		this.ensureIndexes();
	}
	
	public HumansUserDAO(String dbname) {
		super(MongoUtil.getMongo(), new Morphia(), dbname);
		this.ensureIndexes();

	}

    //TODO For testing only..
	public HumansUserDAO(Mongo mongo, Morphia morphia, String dbname) {
		super(mongo, morphia, dbname);
		this.ensureIndexes();

		//initiate();
	}
	
	
	public List<HumansUser> findByUsername( String aUsername ) {
	    Pattern regExp = Pattern.compile(aUsername + ".*", Pattern.CASE_INSENSITIVE);
	    //ds.find(entityClazz).filter("username", regExp);
	    return this.getDatastore().find(getEntityClass()).filter("username", regExp).order("username").asList();// .sort("username").asList();
	}
	
	
	public List<HumansUser> getAllHumansUsers() {
		return getDatastore().find(this.getEntityClass()).order("username").asList();
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
		Query<HumansUser> q = this.getDatastore().createQuery(HumansUser.class).
				filter("humans._id", new ObjectId(aHumanID));
		return findOne(q);
	}
	
	public boolean doesUsernameExist(String aUsername) {
		boolean result = false;
		HumansUser h = findOneByUsername(aUsername);
		if(h != null) {
            result = true;
        }
        return result;
	}
	
	public HumansUser findOneByUsername(String aUsername) {		
		Query<HumansUser> q = this.createQuery().field("username").equal(aUsername);
		return this.findOne(q);
	}
	
	public HumansUser findOneByUsernameAndAccessToken(String aUsername, String aAccessToken) {
		Query<HumansUser> q = this.createQuery().field("username").equal(aUsername).field("access_token").equal(aAccessToken);
		return this.findOne(q);
	}
	
	public HumansUser findOneByAccessToken(String aAccessToken) {
		Query<HumansUser> q = this.createQuery().field("access_token").equal(aAccessToken);
		return this.findOne(q);
	}
	
    public HumansUser findOneByID(String aID) {
        Query<HumansUser> q = this.createQuery().field("_id").equal(new ObjectId(aID));
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
