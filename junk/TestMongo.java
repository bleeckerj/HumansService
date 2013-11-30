package com.nearfuturelaboratory.humans.test;

import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.ServerAddress;
import com.nearfuturelaboratory.humans.entities.Human;
import com.nearfuturelaboratory.humans.entities.HumansUser;
import com.nearfuturelaboratory.humans.entities.ServiceUser;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.TestMapping.BaseEntity;
import org.mongodb.morphia.query.UpdateOperations;


public class TestMongo {

	private  Datastore ds = null;
	//private static final TestMongo INSTANCE = new TestMongo();


	public TestMongo() {
		//To directly connect to a single MongoDB server (note that this will not auto-discover the primary even
		//if it's a member of a replica set:
		//		MongoClient mongoClient = new MongoClient();
		//		//or
		//		MongoClient mongoClient = new MongoClient( "localhost" );
		//		//or
		//		MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
		//or, to connect to a replica set, with auto-discovery of the primary, supply a seed list of members

		MongoClient mongoClient = null;
		try {
			mongoClient = new MongoClient( "localhost" , 27017 );

			//			mongoClient = new MongoClient(Arrays.asList(new ServerAddress("localhost", 27017),
			//			                                   new ServerAddress("localhost", 27018),
			//			                                   new ServerAddress("localhost", 27019)));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		DB db;
		
		db = mongoClient.getDB("instagram");
		DBCollection instagram_status = db.getCollection("status");
		DBObject doc = instagram_status.findOne();
		
		db = mongoClient.getDB( "humans" );
		Set<String> colls = db.getCollectionNames();

		for (String s : colls) {
			System.out.println(s);
		}
		
		
		
		//		datastore = new Morphia().mapPackage(BaseEntity.class.getPackage().getName())
		//				.createDatastore(mongoClient, "humans");
		//		datastore.ensureIndexes();


//		DBCollection users_collection = db.getCollection("users");
//		DBObject doc = users_collection.findOne();
//		DBObject query = new DBObject();
//		query.put(", arg1)
//		query.put("$pull", new DBObject("services.serviceUsers", )
//		users_collection.update(doc, o);
		
		
		//		DBObject doc = users_collection.findOne();
//		System.out.println(doc);
//		Datastore ds;
//
//		Person person = new Person();
//		person.name = "Julian";
//		person.height = new Integer("66");
//
//		ds = new Morphia().createDatastore(mongoClient, "bar");
//		ds.save(person);
//
//		person.name = "Foolian";
//
//		ds.merge(person);
//
//		ds.save(person);
//
		

		
		HumansUser user = new HumansUser();
		//List allHumansUsers = user.getAllHumansUsers();
		
		
		user.addService("twitter", "darthjulian", "99993333");
		user.setEmail("bleeckerj@gmail.com");
		user.setUsername("darthjulian");
		user.setPassword("darthjulian");
		Human human = new Human();
		human.setName("WTF");

		ServiceUser service_user = new ServiceUser();
		service_user.setOnBehalfOf("WTF-test", "WTF-test-id");
		service_user.setService("twitter");
		service_user.setServiceID("abcdefghijklmnop");
		service_user.setUsername("someoneelse");

		human.setServiceUsers(new ArrayList<ServiceUser>(Arrays.asList(service_user)));
		user.setHumans(new ArrayList<Human>(Arrays.asList(human)));

		ds = new Morphia().createDatastore(mongoClient, "humans");
		ds.ensureIndexes(HumansUser.class);
		
//		UpdateOperations<HumansUser> ops = ds.createUpdateOperations(HumansUser.class).add("serviceUsers", service_user)
//		ds.update(queryToFindMe(), ops);
		
		try {
		ds.save(user);
		} catch(Exception e) {
			e.printStackTrace();
		}
		HumansUser h = ds.createQuery(HumansUser.class).field("username").equal(user.getUsername()).get();
		
		if(h == null) {
			ds.save(user);
		} else {
			//for(HumansUser h : aList) {
				System.out.println(h.getUsername()+" "+h.getId());
				// how to add a new ServiceUser
				ServiceUser su = new ServiceUser();
				su.setService("flickr");
				su.setServiceID("9181918@");
				su.setUsername("Fancy");
				su.setOnBehalfOf("WTF-test", "WTF-test-id");
				// now add to human
				human.addServiceUser(su);
				// add to the human we retrieved from the database - that has the _id field and shit
				h.addHuman(human);
				
				h.addService("flickr", "juan", "88888@$#");
				
				ds.merge(h);

				h = ds.createQuery(HumansUser.class).field("humans.serviceUsers.serviceID").equal("abcdefghijklmnop").get();	
				
				
		}


	}

	public static void main(String[] args) {
		TestMongo test = new TestMongo();


	}

}
