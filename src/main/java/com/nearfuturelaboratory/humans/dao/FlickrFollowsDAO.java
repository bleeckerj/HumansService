package com.nearfuturelaboratory.humans.dao;

import java.util.List;

import com.mongodb.MongoClient;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;

import com.mongodb.Mongo;
import com.nearfuturelaboratory.humans.flickr.entities.FlickrFriend;
import com.nearfuturelaboratory.humans.util.MongoUtil;

public class FlickrFollowsDAO extends BasicDAO<FlickrFriend, ObjectId> {

	public FlickrFollowsDAO() {
		this(MongoUtil.getMongo(), new Morphia(), "flickr");
	}
	
	protected FlickrFollowsDAO(MongoClient aMongo, Morphia aMorphia, String aDbName) {
		super(aMongo, aMorphia, aDbName);
		// TODO Auto-generated constructor stub
	}
	
	public FlickrFriend findByFriendIDUserID(String aFriendID, String aUserID) {
		return this.getDatastore().find(this.getEntityClass()).filter("friend_id", aFriendID).filter("user_id", aUserID).get();
	}

	public List<FlickrFriend> findByUserID(String aUserID) {
		return this.getDatastore().find(this.getEntityClass()).filter("user_id", aUserID).order("username").asList();
	}

	/**
	 * For a specific user, find the most recent friend (by lastUpdate) in the database
	 * @param aUserID
	 * @return
	 */
	public FlickrFriend findNewestFriendByExactUserID(String aUserID) {
		return this.getDatastore().find(this.getEntityClass()).filter("user_id", aUserID).order("lastUpdated").get();

	}
	
	/**
	 * For a specific user, find the oldest friend (by lastUpdate) in the database
	 * @param aUserID
	 * @return
	 */
	public FlickrFriend findOldestFriendByExactUserID(String aUserID) {
		FlickrFriend friend= this.getDatastore().find(this.getEntityClass()).filter("user_id", aUserID).order("-lastUpdated").get();
		return friend;
	}

	
}
