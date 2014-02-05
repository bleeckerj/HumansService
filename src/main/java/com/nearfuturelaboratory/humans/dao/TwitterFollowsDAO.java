package com.nearfuturelaboratory.humans.dao;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;

import com.mongodb.Mongo;
import com.mongodb.WriteResult;
import com.nearfuturelaboratory.humans.twitter.entities.TwitterFriend;
import com.nearfuturelaboratory.humans.util.MongoUtil;

public class TwitterFollowsDAO extends BasicDAO<TwitterFriend, ObjectId> {
	
	public TwitterFollowsDAO(/*String aUserId*/) {
		super(MongoUtil.getMongo(), new Morphia(), "twitter");
	}

	public TwitterFollowsDAO(Mongo mongo, Morphia morphia, String dbname) {
		super(mongo, morphia, dbname);
	}
	
	public List<TwitterFriend> findFollowsByExactUserID(String aUserID) {
		List<TwitterFriend> result = new ArrayList<TwitterFriend>();
		result = this.getDatastore().find(this.getEntityClass()).filter("follower_id", aUserID).order("-lastUpdated").asList();
		return result;
	}
	
	public TwitterFriend findNewestFriendByExactUserID(String aUserID) {
		//List<TwitterFriend> result = new ArrayList<TwitterFriend>();
		TwitterFriend result = null;
		result = this.getDatastore().find(this.getEntityClass()).filter("follower_id", aUserID).order("-lastUpdated").get();
		return result;
	}	
	
	public TwitterFriend findOldestFriendByExactUserID(String aUserID) {
		//List<TwitterFriend> result = new ArrayList<TwitterFriend>();
		TwitterFriend result = null;
		result = this.getDatastore().find(this.getEntityClass()).filter("follower_id", aUserID).order("lastUpdated").get();
		return result;
	}	
	
	public TwitterFriend findFollowsByUserIDFollowsID(String aUserID, String aFollowsID) {
		return this.getDatastore().find(this.getEntityClass()).filter("follower_id", aFollowsID).filter("friend_id", aUserID).get();
	}
		
	
	//TODO do we error check here?
	public WriteResult deleteByFollowerID(String aFollowerID) {
		Query<TwitterFriend> q = getDatastore().createQuery(this.getEntityClass()).field("follower_id").equal(aFollowerID);
		WriteResult result = getDatastore().delete(q);
		return result;
	}

}
