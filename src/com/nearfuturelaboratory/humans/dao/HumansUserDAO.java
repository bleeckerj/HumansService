package com.nearfuturelaboratory.humans.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Criteria;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.QueryResults;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;

import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.WriteConcern;
import com.nearfuturelaboratory.humans.config.MongoDB;
import com.nearfuturelaboratory.humans.entities.ServiceUser;
import com.nearfuturelaboratory.humans.entities.HumansUser;
import com.nearfuturelaboratory.humans.entities.Human;
import com.nearfuturelaboratory.humans.util.MongoUtil;
import com.nearfuturelaboratory.util.Constants;

import java.util.regex.Pattern;

public class HumansUserDAO extends BasicDAO<HumansUser, ObjectId> {

	final static Logger logger = Logger.getLogger(com.nearfuturelaboratory.humans.dao.HumansUserDAO.class);
	protected static Mongo mongo;
//	private Morphia morphia;
//	private HumanUserDAO humanUserDao;
//	private final String dbname = "humans";
//	private Datastore datastore;

	public HumansUserDAO() {
		super(MongoUtil.getMongo(), new Morphia(), "humans");
	}
	
	public HumansUserDAO(Mongo mongo, Morphia morphia, String dbname) {
		super(mongo, morphia, dbname);
		//initiate();
	}
	
	
	public List<HumansUser> findByUsername( String aUsername ) {
	    Pattern regExp = Pattern.compile(aUsername + ".*", Pattern.CASE_INSENSITIVE);
	    //ds.find(entityClazz).filter("username", regExp);
	    
	    return this.getDs().find(getEntityClazz()).filter("username", regExp).order("username").asList();// .sort("username").asList();
	}
	
	
	public List<HumansUser> getAllHumansUsers() {
		return getDs().find(getEntityClazz()).order("username").asList();
	}
	
	public List<String>getAllHumansUsers_Usernames() {
		List<String> result = new ArrayList<String>();
		List<HumansUser> all = getAllHumansUsers();
		for(HumansUser h : all) {
			result.add(h.getUsername());
		}
		return result;
	}
	
//	public boolean doesUsernameExist(String aUsername) {
//		boolean result = false;
//		HumansUser h = findOneByUsername(aUsername);
//		
//	}
	
	public HumansUser findOneByUsername(String aUsername) {		
		Query<HumansUser> q = this.createQuery().field("username").equal(aUsername);
		return this.findOne(q);
	}

	public HumansUser getHumansUser(String aUsername, String aPassword) {
		HumansUser h = null;
		h = findOneByUsername(aUsername);
		if(h != null && h.verifyPassword(aPassword)) {
			return h;
		} else {
			return null;
		}	
	}
	
	public void saveHumansUser(HumansUser aHuman) {
		
	}
	
	
	

// some test methods below..leave for a bit just in case i get confused again
/*	public static void main(String[] args) {
		try {
			Constants.load("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/WebContent/WEB-INF/lib/dev.app.properties");
			PropertyConfigurator.configureAndWatch("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/WebContent/WEB-INF/lib/static-logger.properties");
			logger.debug("Hey Ho!");
		} catch(Exception e) {
			e.printStackTrace();
		}

		mongo = MongoUtil.getMongo();
		Morphia morphia = new Morphia();
		
		HumansUserDAO daoTest = new HumansUserDAO(mongo, morphia, "humans");
		//test.initiate();
		
		
		HumansUser user = new HumansUser();
		user.addService("twitter", "darthjulian", "99993333");
		user.setEmail("bleeckerj@gmail.com");
		user.setUsername("darthjulian");
		user.setPassword("darthjulian");
		Human human = new Human();
		human.setName("WTF");
		
		ServiceUser service_user = new ServiceUser();
		service_user.setOnBehalfOf("WTF-test");
		service_user.setService("twitter");
		service_user.setServiceID("abcdefghijklmnop");
		service_user.setUsername("someoneelse");

		human.setServiceUsers(new ArrayList<ServiceUser>(Arrays.asList(service_user)));
		user.setHumans(new ArrayList<Human>(Arrays.asList(human)));
		
		
		//test.datastore.delete(HumansUser.class, new ObjectId("5274592e03645b1a9df9fdf7"));
		//test.datastore.save(user);

		//user.setEmail("foo@bar.com");
		Key<HumansUser> k;
		try {
			k = daoTest.save(user);
		} catch(Exception e) {
			
		}
		user.setEmail("bar@baz.com");
		
		UpdateOperations<HumansUser> fops = daoTest.datastore.createUpdateOperations(HumansUser.class).set("email", "bingo@bongo.net");
		Query<HumansUser> updateQuery = daoTest.datastore.createQuery(HumansUser.class).field("username").equal(user.getUsername());
		UpdateResults<HumansUser> ur = daoTest.update(updateQuery, fops);
		
		Object id = ur.getNewId();
		
		
		user.removeHuman(human);
		daoTest.datastore.merge(user);
		daoTest.update(updateQuery, fops);


		service_user = new ServiceUser();
		service_user.setOnBehalfOf("Tony-The-Tiger");
		service_user.setService("memento");
		service_user.setServiceID("594949494");
		service_user.setUsername("skrumpy");
		List<ServiceUser> l = new ArrayList();
		l.add(service_user);
		
		human = new Human();
		human.setName("Skrumpy");
		human.setServiceUsers(l);
		
		fops = daoTest.datastore.createUpdateOperations(HumansUser.class).add("humans", human);
		daoTest.update(updateQuery, fops);
		
		
		
		
		updateQuery = daoTest.datastore.createQuery(HumansUser.class).field("username").equal(user.getUsername()).field("humans.serviceUsers.username").equal("Skrumpy");
		fops = daoTest.datastore.createUpdateOperations(HumansUser.class).set("humans.serviceUsers.username", "foobar");
		daoTest.update(updateQuery, fops);
		//daoTest.datastore.delete(user);
		//test.datastore.delete(test.datastore.createQuery(HumansUser.class).filter("username", user.getUsername()));
		//daoTest.datastore.save(user);
	
		
		DBCollection collection = mongo.getDB("humans").getCollection("users");
		//collection.update(q, o)
		//test.datastore.merge(user);
		
		//UpdateOperations<HumansUser> op = test.datastore.createUpdateOperations(HumansUser.class);
//		Query<HumansUser> q_1 = test.datastore.find(HumansUser.class, "username", "darthjulian"); 
//		test.datastore.update(q_1, test.datastore.createUpdateOperations(HumansUser.class), true, WriteConcern.NORMAL);
		
		
		Query<HumansUser> query_5 =  daoTest.datastore.find(HumansUser.class).filter("humans.serviceUsers.username", "hellofosta").filter("humans.serviceUsers.service", "instagram");
		Iterable<HumansUser> iter_5 = query_5.fetch();
		for (HumansUser h : iter_5) {
			logger.debug(h.getUsername()+" "+h.getHumans());
		}
		//logger.debug(first.getServices());
		//ops = datastore.createUpdateOperations(Person.class).removeAll("addresses", new Address("Los Angeles"));

		// Just learning crap..
		//
		// Change the name of a Human for a specific HumansUser with a specific username
		// equivalent of..
		// db.users.update({"humans.serviceUsers.username" : "Anna_Simonse", "username" : "darthjulian", "humans.name" : "HalfNoise"}, {$set : {"humans.$.name" : "MosNoise"}})
		Query<HumansUser> a_query = daoTest.datastore.createQuery(HumansUser.class);
		a_query.and(new Criteria[]{daoTest.datastore.createQuery(HumansUser.class).criteria("username").equal("darthjulian")});
		a_query.and(new Criteria[]{daoTest.datastore.createQuery(HumansUser.class).criteria("humans.name").equal("Slavin")});
		a_query.and(new Criteria[]{daoTest.datastore.createQuery(HumansUser.class).criteria("humans.serviceUsers.username").equal("Anna_Simonse")});



		// Should only be one hopefully, although I don't know how to enforce humans.name uniqueness at this stage
		HumansUser a = a_query.get();
		logger.debug(a);
		// Ha! This works now — with this weird positional operator — you have to disableValidation
		//
		UpdateOperations<HumansUser> ops = daoTest.datastore.createUpdateOperations(HumansUser.class).disableValidation().set("humans.$.name", "HalfNoise").enableValidation();
		daoTest.datastore.update(a_query, ops);



		Query<HumansUser> b_query = daoTest.datastore.createQuery(HumansUser.class).field("username").equal("foolian");
		UpdateOperations<HumansUser> b_ops = daoTest.datastore.createUpdateOperations(HumansUser.class).set("username", "darthjulian");
		daoTest.datastore.update(b_query, b_ops);



		HumansUser first = query_5.get();
		Iterator<Human> iter = first.getHumans().iterator();
		while(iter.hasNext()) {
			Human s = iter.next();

			logger.debug(s.getName());
			for(ServiceUser su : s.getServiceUsers()) {
				logger.debug(su.getOnBehalfOf()+" "+su.getUsername()+" "+su.getService());
			}			
		}
		Query<HumansUser> query = daoTest.datastore.find(HumansUser.class).filter("username", "darthjulian").field("humans.serviceUsers.onBehalfOf").equal("41-Julian_Bleecker");
		//q.field("humans.serviceUses.service").equals("twitter");
		Iterator<HumansUser> iterator = query.fetch().iterator();
		while(iterator.hasNext()) {
			HumansUser hu = iterator.next();

			Iterator<Human> hu_iter = hu.getHumans().iterator();
			while(hu_iter.hasNext()) {
				Human h = hu_iter.next();
				logger.debug("Human="+h);
				ArrayList<ServiceUser> copy = (ArrayList<ServiceUser>)h.getServiceUsers();
				copy = (ArrayList<ServiceUser>)copy.clone();
				for(ServiceUser su : h.getServiceUsers()) {
					if(su.getOnBehalfOf().equals("41-Julian_Bleecker")) {
						logger.debug("here's one "+su);
						copy.remove(su);
					}
				}
				h.setServiceUsers(copy);
				daoTest.datastore.save(hu);


			}

			logger.debug("onBehalfOf is here ="+hu);
		}
		logger.debug("================================");
		DBCollection results = query.getCollection();


		Query<HumansUser> q = daoTest.datastore.createQuery(HumansUser.class).field("humans.serviceUsers.onBehalfOf").equal("41-Julian_Bleecker");
		//q.field("humans.serviceUsers.name").equal("Slavin");

		iterator = q.fetch().iterator();
		while(iterator.hasNext()) {
			HumansUser hu = iterator.next();
			List<Human> sus = hu.getHumans();
			Iterator<Human> f = sus.iterator();
			while(f.hasNext()) {
				Human s = f.next();
				for(ServiceUser su : s.getServiceUsers()) {
					logger.debug("*** " +su.getService()+" "+su.getUsername()+" "+s.getName());

				}
			}

		}

		Query<HumansUser> query_2 = daoTest.datastore.find(HumansUser.class).field("humans.name").equal("Slavin");
		Iterator<HumansUser> iterator_2 = query_2.fetch().iterator();
		if(iterator_2 != null) {
			logger.debug("Slavin exists as a name of a Human somewhere");
		}
		while(iterator_2.hasNext()) {
			HumansUser hu = iterator_2.next();
			logger.debug("now="+hu.getUsername());
			for(Human h : hu.getHumans()) {
				logger.debug(h.getServiceUsers()+" "+h.getName());
			}

		}

		Query<Human> query_4 = daoTest.datastore.find(Human.class).field("name").equal("Slavin");
		Iterator<Human> iterator_4 = query_4.fetch().iterator();
		while(iterator_4.hasNext()) {
			Human hu = iterator_4.next();
			logger.debug("query="+hu.getName()+" "+hu.getId());
			for(ServiceUser s : hu.getServiceUsers()) {
				logger.debug(s.getOnBehalfOf()+" "+s.getService());
			}

		}


		Query<ServiceUser> query_3 = daoTest.datastore.find(ServiceUser.class).field("onBehalfOf").equal("41-Julian_Bleecker");
		Iterator<ServiceUser> iterator_3 = query_3.fetch().iterator();
		while(iterator_3.hasNext()) {
			ServiceUser su = iterator_3.next();
			logger.debug("foo="+su.getOnBehalfOf());
		}

		logger.debug(daoTest.humanUserDao.count());

		//Query<Human> query_5 = test.datastore.createQuery(kind)



	}*/

}
