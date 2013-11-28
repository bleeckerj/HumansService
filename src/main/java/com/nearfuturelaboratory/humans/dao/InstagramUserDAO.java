package com.nearfuturelaboratory.humans.dao;

import java.util.List;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;

import com.mongodb.Mongo;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramUser;
import com.nearfuturelaboratory.humans.util.MongoUtil;

public class InstagramUserDAO extends BasicDAO<InstagramUser, ObjectId> {

	public InstagramUserDAO() {
		super(MongoUtil.getMongo(), new Morphia(), "instagram");
	}

	protected InstagramUserDAO(Mongo aMongo, Morphia aMorphia, String aDbName) {
		super(aMongo, aMorphia, aDbName);
	}
	
	/**
	 * A user by username, most recent first
	 * @param aUsername
	 * @return
	 */
	public InstagramUser findByExactUsername( String aUsername ) {
		//Pattern regExp = Pattern.compile(aUsername + ".*", Pattern.CASE_INSENSITIVE);
		return this.getDs().find(this.getEntityClass()).filter("username", aUsername).limit(1).get();
	}
	/**
	 * InstagramUser's key is the userid
	 * @param aUserID
	 * @return
	 */
	public InstagramUser findByExactUserID(String aUserID) {
			return this.getDs().find(this.getEntityClass()).filter("_id", aUserID).limit(1).get();
	}

	public List<InstagramUser> findByUsername(String aUsername) {
		Pattern regExp = Pattern.compile(".*"+aUsername + ".*", Pattern.CASE_INSENSITIVE);
		return this.getDs().find(this.getEntityClass()).filter("username",  aUsername).asList();
	}

}
