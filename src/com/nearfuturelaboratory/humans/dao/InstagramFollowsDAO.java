package com.nearfuturelaboratory.humans.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;

import com.mongodb.Mongo;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramFollows;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramStatus;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramUserBriefly;
import com.nearfuturelaboratory.humans.util.MongoUtil;

public class InstagramFollowsDAO extends BasicDAO<InstagramFollows, ObjectId> {
	final static Logger logger = Logger.getLogger(com.nearfuturelaboratory.humans.dao.InstagramFollowsDAO.class);
	protected static Mongo mongo;

	public InstagramFollowsDAO(/*String aUserId*/) {
		super(MongoUtil.getMongo(), new Morphia(), "instagram");
	}

	public InstagramFollowsDAO(Mongo mongo, Morphia morphia, String dbname) {
		super(mongo, morphia, dbname);
	}
	
	public List<InstagramFollows> findFollowsByExactUserID(String aUserID) {
		List<InstagramFollows> result = new ArrayList<InstagramFollows>();
		result = this.getDs().find(this.getEntityClass()).filter("follower_id", aUserID).order("user_briefly.username").asList();
		return result;
	}
	
	public InstagramFollows findFollowsByUserIDFollowsID(String aUserID, String aFollowsID) {
		InstagramFollows result = this.getDs().find(this.getEntityClass()).filter("user_briefly.id", aUserID).filter("follower_id", aFollowsID).get();
		return result;
		
	}

}
