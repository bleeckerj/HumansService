package com.nearfuturelaboratory.humans.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramStatus;
import com.nearfuturelaboratory.humans.service.InstagramService;
import com.nearfuturelaboratory.humans.util.MongoUtil;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;


public class InstagramStatusDAO extends BasicDAO<InstagramStatus, ObjectId> {

	final static Logger logger = LogManager.getLogger(com.nearfuturelaboratory.humans.dao.InstagramStatusDAO.class);
	protected static Mongo mongo;

	public InstagramStatusDAO(/*String aUserId*/) {
		super(MongoUtil.getMongo(), new Morphia(), "instagram");
	}

	public InstagramStatusDAO(String dbName) {
        super(MongoUtil.getMongo(), new Morphia(), dbName);
    }

	public InstagramStatusDAO(MongoClient mongo, Morphia morphia, String dbname) {
		super(mongo, morphia, dbname);
	}

	/**
	 * A list of status by username, most recent first
	 * @param aUsername
	 * @return
	 */
	public List<InstagramStatus> findByExactUsername( String aUsername ) {
		//Pattern regExp = Pattern.compile(aUsername + ".*", Pattern.CASE_INSENSITIVE);
		return this.getDatastore().find(this.getEntityClass()).filter("user.username", aUsername).order("-created_time").asList();
	}
	/**
	 * A list of status by user.id, most recent first
	 * @param aUserID
	 * @return
	 */
	public List<InstagramStatus> findByExactUserID(String aUserID) {
		//Pattern regExp = Pattern.compile(aUserID + ".*", Pattern.CASE_INSENSITIVE);
		return this.getDatastore().find(this.getEntityClass()).filter("user.id", aUserID).order("-created_time").asList();

	}

	public List<InstagramStatus> findStatusByExactUserIDToDaysAgo(String aUserID, int aDaysAgo) {
		Calendar ago = Calendar.getInstance();
		ago.add(Calendar.DAY_OF_MONTH, -1 * aDaysAgo);
		//ago.getTimeInMillis();

		Query<InstagramStatus> query = this.getDatastore().createQuery(InstagramStatus.class);
		query.filter("user.id ==", aUserID);
		query.filter("created_time >=", new Long(ago.getTimeInMillis()/1000L));
		query.order("-created_time");
		Iterable<InstagramStatus> result = query.fetch();
		return query.asList();
	}

	public List<InstagramStatus> findStatusByExactUserIDToMonthsAgo(String aUserID, int aMonthsAgo) {
		Calendar ago = Calendar.getInstance();
		ago.add(Calendar.MONTH, -1 * aMonthsAgo);
		//ago.getTimeInMillis();

		Query<InstagramStatus> query = this.getDatastore().createQuery(InstagramStatus.class);
		query.filter("user.id ==", aUserID);
		query.filter("created_time >=", new Long(ago.getTimeInMillis()/1000L));
		query.order("-created_time");
		Iterable<InstagramStatus> result = query.fetch();
		return query.asList();
	}

	public InstagramStatus findMostRecentStatusByExactUserID(String aUserID) {
		InstagramStatus result = null;
		Query<InstagramStatus> q = this.getDatastore().find(this.getEntityClass()).filter("user.id", aUserID).order("-created_time").limit(1);
		result = q.get();
		return result;
	}

    public InstagramStatus findOldestStatusByExactUserID(String aUserID) {
        InstagramStatus result = null;
        Query<InstagramStatus> q = this.getDatastore().find(this.getEntityClass()).filter("user.id", aUserID).order("created_time").limit(1);
        result = q.get();
        return result;
    }


    public long getStatusCountForUserID(String aUserID) {
        long result;
        result = this.getDatastore().find(this.getEntityClass()).filter("user.id", aUserID).countAll();
        return result;
    }
}
