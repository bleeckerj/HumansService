package com.nearfuturelaboratory.humans.dao;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.nearfuturelaboratory.humans.tumblr.entities.TumblrTextPost;
import com.nearfuturelaboratory.humans.util.MongoUtil;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;

/**
 * Created by julian on 4/4/14.
 */
public class TumblrTextPostDAO extends BasicDAO<TumblrTextPost, ObjectId> {

    public TumblrTextPostDAO() {
        super(MongoUtil.getMongo(), new Morphia(), "tumblr");
    }


    protected TumblrTextPostDAO(MongoClient mongo, Morphia morphia, String dbName) {
        super(mongo, morphia, dbName);
    }
}
