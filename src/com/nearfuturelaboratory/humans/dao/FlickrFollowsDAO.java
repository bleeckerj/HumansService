package com.nearfuturelaboratory.humans.dao;

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
	
	protected FlickrFollowsDAO(Mongo aMongo, Morphia aMorphia, String aDbName) {
		super(aMongo, aMorphia, aDbName);
		// TODO Auto-generated constructor stub
	}
	
	public FlickrFriend findByFriendIDUserID(String aFriendID, String aUserID) {
		return this.getDs().find(this.getEntityClass()).filter("friend_id", aFriendID).filter("user_id", aUserID).get();
	}

}
