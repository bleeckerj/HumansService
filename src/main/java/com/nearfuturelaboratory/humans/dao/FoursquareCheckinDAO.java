package com.nearfuturelaboratory.humans.dao;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;

import com.mongodb.Mongo;
import com.nearfuturelaboratory.humans.foursquare.entities.FoursquareCheckin;
import com.nearfuturelaboratory.humans.foursquare.entities.FoursquareUser;
import com.nearfuturelaboratory.humans.util.MongoUtil;

public class FoursquareCheckinDAO extends BasicDAO<FoursquareCheckin, ObjectId> {

	public FoursquareCheckinDAO() {
		super(MongoUtil.getMongo(), new Morphia(), "foursquare");
	}

	protected FoursquareCheckinDAO(Mongo aMongo, Morphia aMorphia, String aDbName) {
		super(aMongo, aMorphia, aDbName);
	}

	/**
	 * A user by userid, most recent first
	 * @param aUsername
	 * @return
	 */
	public List<FoursquareCheckin> findByExactUserID( String aUserID ) {
		//Pattern regExp = Pattern.compile(aUsername + ".*", Pattern.CASE_INSENSITIVE);
		return this.getDatastore().find(this.getEntityClass()).filter("user_id", aUserID).asList();
	}
	
	public FoursquareCheckin findLatestCheckin( String aUserID ) {
		return this.getDatastore().find(this.getEntityClass()).filter("user_id", aUserID).order("-createdAt").limit(1).get();
	}

    public long getStatusCountForUserID(String aUserID) {
        long result;
        result = this.getDatastore().find(this.getEntityClass()).filter("user_id", aUserID).countAll();
        return result;
    }

}
