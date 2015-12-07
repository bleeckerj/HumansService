package com.nearfuturelaboratory.humans.dao;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.nearfuturelaboratory.humans.twitter.entities.generated.TwitterStatus;
import com.nearfuturelaboratory.humans.util.MongoUtil;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;

import java.util.List;

public class TwitterStatusDAO extends BasicDAO<TwitterStatus, ObjectId> {


    public TwitterStatusDAO() {
        super(MongoUtil.getMongo(), new Morphia(), "twitter");
    }

    protected TwitterStatusDAO(MongoClient aMongo, Morphia aMorphia, String aDbName) {
        super(aMongo, aMorphia, aDbName);
    }

    /**
     * A list of status by username, most recent first
     *
     * @param screen_name
     * @return
     */
    public List<TwitterStatus> findByExactScreename(String screen_name) {
        //Pattern regExp = Pattern.compile(aUsername + ".*", Pattern.CASE_INSENSITIVE);
        return this.getDatastore().find(this.getEntityClass()).filter("screen_name", screen_name).order("-created_at").asList();
    }
//	/**
//	 * A list of status by user.id, most recent first
//	 * @param aUserID
//	 * @return
//	 */
//	public List<TwitterStatus> findByExactUserID(String aUserID) {
//		//Pattern regExp = Pattern.compile(aUserID + ".*", Pattern.CASE_INSENSITIVE);
//		return this.getDatastore().find(this.getEntityClass()).filter("user.id_str", aUserID).order("-created_at").asList();
//
//	}
//
//	public TwitterStatus findMostRecentStatusByExactUserID(String aUserID) {
//		TwitterStatus result = null;
//		Query<TwitterStatus> q = this.getDatastore().find(this.getEntityClass()).filter("user.id_str", aUserID).order("-created_at").limit(1);
//		result = q.get();
//		return result;
//	}
//
//	/**
//	 * Don't be confused this is the oldest status *WE have, not in the entire Twitter timeline
//	 * @param aUserID
//	 * @return
//	 */
//	public TwitterStatus findOldestStatusByExactUserID(String aUserID) {
//		TwitterStatus result = null;
//		Query<TwitterStatus> q = this.getDatastore().find(this.getEntityClass()).filter("user.id_str", aUserID).order("+created_at").limit(1);
//		result = q.get();
//		return result;
//
//	}

    /**
     * A list of status by user.id, most recent first
     *
     * @param aUserID
     * @return
     */
    public List<TwitterStatus> findByExactUserID(String aUserID) {
        //Pattern regExp = Pattern.compile(aUserID + ".*", Pattern.CASE_INSENSITIVE);
        return this.getDatastore().find(this.getEntityClass()).filter("user.id", aUserID).order("-created_at").asList();

    }

    public TwitterStatus findMostRecentStatusByExactUserID(String aUserID) {
        TwitterStatus result = null;
        Query<TwitterStatus> q = this.getDatastore().find(this.getEntityClass()).filter("user.id", aUserID).order("-created_at").limit(1);
        result = q.get();
        return result;
    }

    /**
     * Don't be confused this is the oldest status *WE have, not in the entire Twitter timeline
     *
     * @param aUserID
     * @return
     */
    public TwitterStatus findOldestStatusByExactUserID(String aUserID) {
        TwitterStatus result = null;
        Query<TwitterStatus> q = this.getDatastore().find(this.getEntityClass()).filter("user.id", aUserID).order("created_at").limit(1);
        result = q.get();
        return result;

    }

    public long getStatusCountForUserID(String aUserID) {
        long result;
        result = this.getDatastore().find(this.getEntityClass()).filter("user.id", aUserID).countAll();
        return result;
    }
}
