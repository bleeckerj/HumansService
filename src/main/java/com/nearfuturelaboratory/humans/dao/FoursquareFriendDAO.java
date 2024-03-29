package com.nearfuturelaboratory.humans.dao;

import java.util.List;

import com.mongodb.MongoClient;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateResults;

import com.nearfuturelaboratory.humans.foursquare.entities.FoursquareFriend;
import com.mongodb.Mongo;
import com.nearfuturelaboratory.humans.util.MongoUtil;

import org.mongodb.morphia.query.UpdateOperations;
import java.util.Date;

public class FoursquareFriendDAO extends BasicDAO<FoursquareFriend, ObjectId> {
	
	public FoursquareFriendDAO() {
		super(MongoUtil.getMongo(), new Morphia(), "foursquare");
	}

	protected FoursquareFriendDAO(MongoClient aMongo, Morphia aMorphia, String aDbName) {
		super(aMongo, aMorphia, aDbName);
	}

	public FoursquareFriend findForUserIDFriendID(String aUserID, String aFriendID) {
		return this.getDatastore().find(this.getEntityClass()).filter("user_id", aUserID).filter("friend_id", aFriendID).get();

	}

    public FoursquareFriend findByExactUserID(String aUserID) {
        return this.getDatastore().find(this.getEntityClass()).filter("user_id", aUserID).limit(1).get();

    }

	public List<FoursquareFriend> findForUserID(String aUserID) {
		return this.getDatastore().find(this.getEntityClass()).filter("user_id", aUserID).asList();

	}
	/**
	 * For a specific foursquare user id, find the friend that's the newest to be updated as by lastUpdated
	 * @param aUserID
	 * @return
	 */
	public FoursquareFriend findNewestFriendByExactUserID(String aUserID) {
		FoursquareFriend result = null;
		result = this.getDatastore().find(this.getEntityClass()).filter("user_id", aUserID).order("-lastUpdated").get();
		return result;
	}
	
	/**
	 * For a specific foursquare user id, find the friend that's the oldest to be updated as by lastUpdated
	 * @param aUserID
	 * @return
	 */
	
	public FoursquareFriend findOldestFriendByExactUserID(String aUserID) {
		FoursquareFriend result = null;
		result = this.getDatastore().find(this.getEntityClass()).filter("user_id", aUserID).order("lastUpdated").get();
		return result;
	}
	
	//TODO look into this more closely
	/**
	 * Here is an experiment to have an update rather than delete and save, maybe..
	 *
	 * @param aFriend
	 */
	public void updateLastUpdated(FoursquareFriend aFriend) {
		Query<FoursquareFriend> query = getDatastore().createQuery(this.getEntityClass()).field("_id").equal(aFriend.getId());
		UpdateOperations<FoursquareFriend> ops = getDatastore().createUpdateOperations(this.getEntityClass()).set("lastUpdated", new Date());

		UpdateResults results = getDatastore().update(query, ops);
		//TODO check for errors from results??
	}
	
//	public FoursquareFriend updateForFriend(String aUserID, Foursquare)

}
