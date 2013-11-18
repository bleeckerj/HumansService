package com.nearfuturelaboratory.humans.dao;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;

import com.mongodb.Mongo;
import com.nearfuturelaboratory.humans.foursquare.entities.FoursquareUser;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramUser;
import com.nearfuturelaboratory.humans.util.MongoUtil;

public class FoursquareUserDAO extends BasicDAO<FoursquareUser, ObjectId> {
	public FoursquareUserDAO() {
		super(MongoUtil.getMongo(), new Morphia(), "foursquare");
	}

	protected FoursquareUserDAO(Mongo aMongo, Morphia aMorphia, String aDbName) {
		super(aMongo, aMorphia, aDbName);
	}
	/**
	 * A user by userid, most recent first
	 * @param aUsername
	 * @return
	 */
	public FoursquareUser findByExactUserID( String aUserID ) {
		//Pattern regExp = Pattern.compile(aUsername + ".*", Pattern.CASE_INSENSITIVE);
		return this.getDs().find(this.getEntityClass()).filter("id", aUserID).limit(1).get();
	}

}
