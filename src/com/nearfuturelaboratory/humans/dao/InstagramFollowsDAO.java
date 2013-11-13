package com.nearfuturelaboratory.humans.dao;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;

import com.mongodb.Mongo;
import com.nearfuturelaboratory.humans.entities.InstagramFollows;
import com.nearfuturelaboratory.humans.entities.InstagramUserBriefly;
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

}
