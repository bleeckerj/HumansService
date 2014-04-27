package com.nearfuturelaboratory.humans.util;

import java.net.UnknownHostException;

import com.nearfuturelaboratory.util.Constants;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;

public class MongoUtil {

	final static Logger logger = LogManager.getLogger(com.nearfuturelaboratory.humans.util.MongoUtil.class);


	private static final int port = Constants.getInt("MONGO_PORT", 27017);
	private static final String host = Constants.getString("MONGO_IP", "localhost");
	private static MongoClient mongo = null;

	public static MongoClient getMongo() {
		if (mongo == null) {
			try {
				
				MongoClientOptions mco = new MongoClientOptions.Builder()
			    .connectionsPerHost(10)
			    .threadsAllowedToBlockForConnectionMultiplier(10)
			    .build();
			//MongoClient client = new MongoClient(addresses, mco);
				ServerAddress address = new ServerAddress(host, port);
				mongo = new MongoClient(address, mco);
				logger.info("New Mongo created with [" + host + "] and ["
						+ port + "] and ["+mco.toString()+"]");
			} catch (UnknownHostException e) {
				logger.error(e.getMessage());
			} catch(MongoException e) {
                logger.error(e.getMessage());

            }
		}
		return mongo;
	}
	

    public static MongoClient getMongo(String _host, int _port) {
//        if (mongo == null) {
            try {

                MongoClientOptions mco = new MongoClientOptions.Builder()
                        .connectionsPerHost(10)
                        .threadsAllowedToBlockForConnectionMultiplier(10)
                        .build();
                //MongoClient client = new MongoClient(addresses, mco);
                ServerAddress address = new ServerAddress(_host, _port);
                mongo = new MongoClient(address, mco);
                logger.info("New Mongo created with [" + _host + "] and ["
                        + _port + "] and ["+mco.toString()+"]");
            } catch (UnknownHostException e) {
                logger.error(e.getMessage());
            } catch(MongoException e) {
                logger.error(e.getMessage());

            }
 //       }
        return mongo;

    }

	public static DB getStatusCacheDB() {
		return getMongo().getDB("status_cache");
	}
}