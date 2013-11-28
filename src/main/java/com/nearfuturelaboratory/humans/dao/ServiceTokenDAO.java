package com.nearfuturelaboratory.humans.dao;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;

import com.mongodb.Mongo;
import com.nearfuturelaboratory.humans.entities.ServiceToken;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramUser;
import com.nearfuturelaboratory.humans.util.MongoUtil;

public class ServiceTokenDAO extends BasicDAO<ServiceToken, ObjectId> {

	public ServiceTokenDAO(String aServiceName) {
		super(MongoUtil.getMongo(), new Morphia(), aServiceName);
	}
	
	
	protected ServiceTokenDAO(Mongo aMongo, Morphia aMorphia, String aDbName) {
		super(aMongo, aMorphia, aDbName);
	}

	/**
	 * A list of status by username, most recent first
	 * @param aUsername
	 * @return
	 */
	public ServiceToken findByExactUserId( String aUserId ) {
		//Pattern regExp = Pattern.compile(aUsername + ".*", Pattern.CASE_INSENSITIVE);
		ServiceToken result =  this.getDatastore().find(this.getEntityClass()).filter("user_id", aUserId).limit(1).get();
//		if(result == null) {
//			result = new ServiceToken();
//		}
		return result;
	}
 
	public ServiceToken findByExactUsername( String aUsername ) {
		//Pattern regExp = Pattern.compile(aUsername + ".*", Pattern.CASE_INSENSITIVE);
		ServiceToken result =  this.getDatastore().find(this.getEntityClass()).filter("username", aUsername).limit(1).get();
//		if(result == null) {
//			result = new ServiceToken();
//		}
		return result;

	}

	public ServiceToken findByExactUserID(String aUserID) {
		ServiceToken result =  this.getDatastore().find(this.getEntityClass()).filter("user_id", aUserID).limit(1).get();
//		if(result == null) {
//			result = new ServiceToken();
//		}
		return result;

	}
 
}
