package com.nearfuturelaboratory.humans.dao;

import com.mongodb.MongoClient;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;

import com.mongodb.Mongo;
import com.nearfuturelaboratory.humans.flickr.entities.FlickrUser;
import com.nearfuturelaboratory.humans.util.MongoUtil;

public class FlickrUserDAO extends BasicDAO<FlickrUser, ObjectId> {
	
	public FlickrUserDAO() {
		super(MongoUtil.getMongo(), new Morphia(), "flickr");
	}

	protected FlickrUserDAO(MongoClient aMongo, Morphia aMorphia, String aDbName) {
		super(aMongo, aMorphia, aDbName);
	}
	
	public FlickrUser findByExactUserID( String aUserID ) {
		return this.getDatastore().find(this.getEntityClass()).filter("id", aUserID).limit(1).get();

	}

}
