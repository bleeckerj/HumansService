package com.nearfuturelaboratory.humans.util;

import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;

public class MongoUtil {

	final static Logger logger = Logger.getLogger(com.nearfuturelaboratory.humans.util.MongoUtil.class);


	private static final int port = 27017;
	private static final String host = "localhost";
	private static MongoClient mongo = null;

	public static MongoClient getMongo() {
		if (mongo == null) {
			try {
				mongo = new MongoClient(host, port);
				logger.debug("New Mongo created with [" + host + "] and ["
						+ port + "]");
			} catch (UnknownHostException | MongoException e) {
				logger.error(e.getMessage());
			}
		}
		return mongo;
	}
	
	
	public static DB getStatusCacheDB() {
		return getMongo().getDB("status_cache");
	}
}