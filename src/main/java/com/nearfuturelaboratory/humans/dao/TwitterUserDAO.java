package com.nearfuturelaboratory.humans.dao;

import java.util.List;
import java.util.regex.Pattern;

import com.mongodb.MongoClient;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;

import com.mongodb.Mongo;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramUser;
import com.nearfuturelaboratory.humans.twitter.entities.TwitterUser;
import com.nearfuturelaboratory.humans.util.MongoUtil;

public class TwitterUserDAO extends BasicDAO<TwitterUser, ObjectId> {
	
	public TwitterUserDAO() {
		super(MongoUtil.getMongo(), new Morphia(), "twitter");

	}

	protected TwitterUserDAO(MongoClient aMongo, Morphia aMorphia, String aDbName) {
		super(aMongo, aMorphia, aDbName);
	}
	/**
	 * A user by username, most recent first
	 * @param aUsername
	 * @return
	 */
	public TwitterUser findByExactUsername( String screen_name ) {
		//Pattern regExp = Pattern.compile(aUsername + ".*", Pattern.CASE_INSENSITIVE);
		return this.getDatastore().find(this.getEntityClass()).filter("screen_name", screen_name).limit(1).get();
	}
	/**
	 * TwitterUser's key is the userid
	 * @param aUserID
	 * @return
	 */
	public TwitterUser findByExactUserID(String user_id) {
			return this.getDatastore().find(this.getEntityClass()).filter("_id", user_id).limit(1).get();
	}

	public List<TwitterUser> findByUsername(String screen_name) {
		Pattern regExp = Pattern.compile(".*"+screen_name + ".*", Pattern.CASE_INSENSITIVE);
		return this.getDatastore().find(this.getEntityClass()).filter("screen_name",  screen_name).asList();
	}

}
