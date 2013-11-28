package com.nearfuturelaboratory.humans.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;

import com.mongodb.Mongo;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramFriend;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramStatus;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramUserBriefly;
import com.nearfuturelaboratory.humans.util.MongoUtil;

public class InstagramFriendsDAO extends BasicDAO<InstagramFriend, ObjectId> {
	final static Logger logger = Logger.getLogger(com.nearfuturelaboratory.humans.dao.InstagramFriendsDAO.class);
	protected static Mongo mongo;

	public InstagramFriendsDAO(/*String aUserId*/) {
		super(MongoUtil.getMongo(), new Morphia(), "instagram");
	}

	public InstagramFriendsDAO(Mongo mongo, Morphia morphia, String dbname) {
		super(mongo, morphia, dbname);
	}
	
	public List<InstagramFriend> findFollowsByExactUserID(String aUserID) {
		List<InstagramFriend> result = new ArrayList<InstagramFriend>();
		result = this.getDs().find(this.getEntityClass()).filter("user_id", aUserID).order("friend_username").asList();
		return result;
	}
	
	public InstagramFriend findFollowsByFriendIDForUserID(String aFriendID, String aUserID) {
		InstagramFriend result = this.getDs().find(this.getEntityClass()).filter("friend_id", aFriendID).filter("user_id", aUserID).get();
		return result;
		
	}

	public InstagramFriend findMostRecentFriendsForUserID(String aUserID) {
		InstagramFriend result = this.getDs().find(this.getEntityClass()).filter("user_id", aUserID).order("-lastUpdated").get();
		return result;

	}
	
	public InstagramFriend findOldestFriendsForUserID(String aUserID) {
		InstagramFriend result = this.getDs().find(this.getEntityClass()).filter("user_id", aUserID).order("lastUpdated").get();
		return result;

	}

}
