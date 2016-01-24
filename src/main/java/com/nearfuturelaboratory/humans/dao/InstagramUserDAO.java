package com.nearfuturelaboratory.humans.dao;

import java.util.List;
import java.util.regex.Pattern;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;

import com.mongodb.Mongo;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramUser;
import com.nearfuturelaboratory.humans.util.MongoUtil;
import org.mongodb.morphia.query.Query;

public class InstagramUserDAO extends BasicDAO<InstagramUser, ObjectId> {

	public InstagramUserDAO() {
		super(MongoUtil.getMongo(), new Morphia(), "instagram");
	}

    public InstagramUserDAO(String dbName) {
        super(MongoUtil.getMongo(), new Morphia(), dbName);
    }

	public InstagramUserDAO(MongoClient aMongo, Morphia aMorphia, String aDbName) {
		super(aMongo, aMorphia, aDbName);
	}
	
	/**
	 * A user by username, most recent first
	 * @param aUsername
	 * @return
	 */
	public InstagramUser findByExactUsername( String aUsername ) {

		//Pattern regExp = Pattern.compile(aUsername + ".*", Pattern.CASE_INSENSITIVE);

		//Query<InstagramUser> query = createQuery().field("username").equal(regExp).limit(1);//Replace `id` with what ever name you use in UserData for '_id'
		//Object user = query.get();
		//return query.get();
		Pattern regExp = Pattern.compile("^" + aUsername + "$", Pattern.CASE_INSENSITIVE);//Pattern.compile(aUsername, Pattern.CASE_INSENSITIVE);

		return this.getDatastore().find(this.getEntityClass()).filter("username", regExp).limit(1).get();
	}
	/**
	 * InstagramUser's key is the userid
	 * @param aUserID
	 * @return
	 */
	public InstagramUser findByExactUserID(String aUserID) {
			return this.getDatastore().find(this.getEntityClass()).filter("_id", aUserID).limit(1).get();
	}

	public InstagramUser findByExactUsernameCaseInsensitive(String aUsername) {
		//Pattern regExp = Pattern.compile(".*"+aUsername + ".*", Pattern.CASE_INSENSITIVE);
		Pattern regExp = Pattern.compile("^" + aUsername + "$", Pattern.CASE_INSENSITIVE);//Pattern.compile(aUsername, Pattern.CASE_INSENSITIVE);

		//return this.getDatastore().find(getEntityClass()).filter("username", regExp).order("username").asList();// .sort("username").asList();

		return this.getDatastore().find(this.getEntityClass()).filter("username",  regExp).limit(1).get();
	}

	public List<InstagramUser> findByUsernameCaseInsensitive(String aUsername) {
		Pattern regExp = Pattern.compile("^" + aUsername + "$", Pattern.CASE_INSENSITIVE);//Pattern.compile(".*"+aUsername + ".*", Pattern.CASE_INSENSITIVE);
		//Pattern regExp = Pattern.compile(aUsername, Pattern.CASE_INSENSITIVE);
		return this.getDatastore().find(this.getEntityClass()).filter("username",  regExp).asList();
	}

}
