package com.nearfuturelaboratory.humans.dao;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;

import com.nearfuturelaboratory.humans.foursquare.entities.FoursquareFriend;
import com.mongodb.Mongo;
import com.nearfuturelaboratory.humans.util.MongoUtil;

public class FoursquareFollowsDAO extends BasicDAO<FoursquareFriend, ObjectId> {
	
	public FoursquareFollowsDAO() {
		super(MongoUtil.getMongo(), new Morphia(), "foursquare");
	}

	protected FoursquareFollowsDAO(Mongo aMongo, Morphia aMorphia, String aDbName) {
		super(aMongo, aMorphia, aDbName);
	}

	public FoursquareFriend findForUserIDFriendID(String aUserID, String aFriendID) {
		return this.getDs().find(this.getEntityClass()).filter("user_id", aUserID).filter("friend_id", aFriendID).get();

	}
	
	public List<FoursquareFriend> findForUserID(String aUserID) {
		return this.getDs().find(this.getEntityClass()).filter("user_id", aUserID).asList();

	}
	
//	public FoursquareFriend updateForFriend(String aUserID, Foursquare)

}
