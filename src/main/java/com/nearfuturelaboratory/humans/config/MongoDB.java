package com.nearfuturelaboratory.humans.config;

//import java.util.logging.Logger;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

//import at.ac.tuwien.ec.mongouk2011.entities.BaseEntity;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;

/**
 * MongoDB providing the database connection.
 */
public class MongoDB {
	private static final Logger logger = LogManager.getLogger(MongoDB.class.getName());
	private static final MongoDB INSTANCE = new MongoDB();

	//private final Datastore datastore = null;
	public static final String DB_NAME = "humans";
	private final MongoClient mongoClient;

	private MongoDB() {
		try {
			mongoClient = new MongoClient("127.0.0.1", 27017);
			mongoClient.setWriteConcern(WriteConcern.SAFE);
			
			//			datastore = new Morphia().mapPackage(BaseEntity.class.getPackage().getName())
			//					.createDatastore(mongoClient, DB_NAME);
			//			datastore.ensureIndexes();
			logger.info("Connection to database '" + DB_NAME + "' initialized");
		} catch (Exception e) {
			logger.error(e);
			throw new RuntimeException("Error initializing MongoDB", e);
		}
	}

	
	public static MongoDB instance() {
		return INSTANCE;
	}

	public Datastore getDatastore(String dbName) 
	{ 
		Datastore ds; // with authentication? 
		ds = new Morphia(). createDatastore(mongoClient,dbName);
		/*, properties.getProperty("username"), properties.getProperty("password").toCharArray()); } else { ds = new Morphia(). createDatastore(mongoClient,dbName); }*/
		return ds; 
	}

}