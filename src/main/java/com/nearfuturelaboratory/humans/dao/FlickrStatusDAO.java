package com.nearfuturelaboratory.humans.dao;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;

import com.mongodb.Mongo;
import com.nearfuturelaboratory.humans.flickr.entities.FlickrStatus;
import com.nearfuturelaboratory.humans.twitter.entities.TwitterStatus;
import com.nearfuturelaboratory.humans.util.MongoUtil;

public class FlickrStatusDAO extends BasicDAO<FlickrStatus, ObjectId> {

	public FlickrStatusDAO() {
		super(MongoUtil.getMongo(), new Morphia(), "flickr");
	}
	
	protected FlickrStatusDAO(Mongo aMongo, Morphia aMorphia, String aDbName) {
		super(aMongo, aMorphia, aDbName);
	}
	
	public FlickrStatus findMostRecentStatusByExactUserID(String aUserID) {
		FlickrStatus result = null;
		Query<FlickrStatus> q = this.getDatastore().find(this.getEntityClass()).filter("owner", aUserID).order("-dateupload").limit(1);
		result = q.get();
		return result;
	}
	
	public FlickrStatus findOldestStatusByExactUserID(String aUserID) {
		FlickrStatus result = null;
		Query<FlickrStatus> q = this.getDatastore().find(this.getEntityClass()).filter("owner", aUserID).order("dateupload").limit(1);
		result = q.get();
		return result;
	}

}
