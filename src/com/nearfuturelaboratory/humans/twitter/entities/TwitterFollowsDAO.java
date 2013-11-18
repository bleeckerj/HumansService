package com.nearfuturelaboratory.humans.twitter.entities;

import java.util.ArrayList;
import java.util.List;

import org.mongodb.morphia.dao.BasicDAO;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;

import com.mongodb.Mongo;
import com.mongodb.WriteResult;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramFollows;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramUserBriefly;
import com.nearfuturelaboratory.humans.util.MongoUtil;

public class TwitterFollowsDAO extends BasicDAO<TwitterFollows, ObjectId> {
	
	public TwitterFollowsDAO(/*String aUserId*/) {
		super(MongoUtil.getMongo(), new Morphia(), "twitter");
	}

	public TwitterFollowsDAO(Mongo mongo, Morphia morphia, String dbname) {
		super(mongo, morphia, dbname);
	}
	
	public List<TwitterFollows> findFollowsByExactUserID(String aUserID) {
		List<TwitterFollows> result = new ArrayList<TwitterFollows>();
		result = this.getDs().find(this.getEntityClass()).filter("follower_id", aUserID).asList();
		return result;
	}
	
	public TwitterFollows findFollowsByUserIDFollowsID(String aUserID, String aFollowsID) {
		return this.getDs().find(this.getEntityClass()).filter("follower_id", aFollowsID).filter("user._id", aUserID).get();
	}
	//TODO do we error check here?
	public WriteResult deleteByFollowerID(String aFollowerID) {
		Query<TwitterFollows> q = getDs().createQuery(this.getEntityClass()).field("follower_id").equal(aFollowerID);
		WriteResult result = getDs().delete(q);
		return result;
	}

}
