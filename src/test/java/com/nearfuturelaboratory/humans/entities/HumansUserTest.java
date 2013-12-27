package com.nearfuturelaboratory.humans.entities;

//import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bson.types.ObjectId;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mongodb.morphia.Key;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.nearfuturelaboratory.humans.core.MinimalSocialServiceUser;
import com.nearfuturelaboratory.humans.dao.HumansUserDAO;
import com.nearfuturelaboratory.util.Constants;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HumansUserTest {
	final static Logger logger = Logger.getLogger(com.nearfuturelaboratory.humans.entities.HumanTest.class);
	static HumansUserDAO test_dao;

	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Logger.getRootLogger().setLevel(Level.OFF);

		try {
			Constants.load("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/src/main/webapp/WEB-INF/lib/dev.app.properties");
			PropertyConfigurator.configureAndWatch("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/src/main/webapp/WEB-INF/lib/static-logger.properties");
			test_dao = new HumansUserDAO("humans-test");
			test_dao.getCollection().drop();
			//logger.debug("Hey Ho!");
		} catch(Exception e) {
			e.printStackTrace();
		}

	}
	
//	@Test
//	public void testHumansUser() {
//		try {
//		HumansUser user = new HumansUser("darthjulian", "darthjulian");
//		assertThat(user.isValidUser(), is(true));
//		} catch(InvalidUserException iue) {
//			fail(iue.toString());
//			
//		}
//	}

	
	
	//	protected Human getTestHuman()
	//	{
	//		return human;
	//	}

	@Test
	public void removeServiceUser() {
		ServiceEntry service_entry = new ServiceEntry("id_", "username_", "service_");
		ServiceUser service_user = new ServiceUser("id__", "username__", "name__", "image_url__", service_entry);
		ObjectId aId = new ObjectId(new Date(), 1);
		service_user.setId(aId);
		Human human = new Human();
		human.setName("test");
		human.addServiceUser(service_user);

		HumansUser user = new HumansUser();
		user.addHuman(human);
		boolean result;
		assertThat(user.getAllHumans(), hasSize(1));
		assertThat(human.getServiceUsers(), hasSize(1));
		result = user.removeServiceUserById(aId.toString());
		assertThat(result, is(true));
		assertThat(human.getServiceUsers(), hasSize(0));

	}




	/**
	 * This should also remove any human that relies on a specific OnBehalfOf service
	 */
	@Test
	public void testRemoveServiceBy() {
		ServiceEntry on_behalf_of = new ServiceEntry("1", "me", "twitter");
		ServiceUser service_user = new ServiceUser("101", "a_username", "Bill Bullox", "http://blah.com", on_behalf_of);
		Human human = new Human();
		human.setName("removeServiceFromUser-test");
		human.addServiceUser(service_user);

		HumansUser user = new HumansUser();
		user.setUsername("darthjulian");
		user.addHuman(human);

		user.addService(on_behalf_of);
		test_dao.save(user);

		HumansUser load_user = test_dao.findOneByUsername("darthjulian");
		assertThat(load_user, notNullValue());

		assertThat(load_user.getServicesForServiceName("twitter"), hasSize(1));

		load_user.removeServiceBy("twitter", "1");

		assertThat(load_user.getServicesForServiceName("twitter"), hasSize(0));

		test_dao.save(load_user);

	}


	@Test
	public void removeHumanById() {
		Human human = new Human();
		human.setName("test");
		ObjectId aId = new ObjectId(new Date(), 1);
		human.setId(aId.toString());
		HumansUser user = new HumansUser();
		user.addHuman(human);
		boolean result;

		assertThat(user.getAllHumans(), hasSize(1));
		assertThat(user.getAllHumans(), hasItem(human));

		result = user.removeHumanById(aId.toString());

		assertThat(result, is(true));
		assertThat(user.getAllHumans(), hasSize(0));
	}

	@Test
	public void updateServiceUserById() {
		ServiceEntry service_entry = new ServiceEntry("id_", "username_", "service_");
		ServiceUser service_user = new ServiceUser("id__", "username__", "name__", "image_url__", service_entry);
		ObjectId aId = new ObjectId(new Date(), 1);
		service_user.setId(aId);
		Human human = new Human();
		human.setName("updateServiceUserById-test");
		human.addServiceUser(service_user);

		HumansUser user = new HumansUser();

		user.addHuman(human);

		user.save(test_dao);

		ServiceEntry new_service_entry = new ServiceEntry("new_id", "new_username", "faafaa");

		service_user.setUsername("Boo Boo Boo");
		service_user.setService("faafaa");
		service_user.setOnBehalfOf(new_service_entry);

		boolean result = user.updateServiceUserById(service_user, aId.toString());

		assertThat(result, is(true));
		result = user.containsServiceUserById(aId.toString());
		assertThat(result, is(true));

		assertThat(user.getServiceUsersForAllHumans(), hasSize(1));
		assertThat(user.getServiceUsersForAllHumans(), hasItem(service_user));
		assertThat(user.getServiceUsersForAllHumansByServiceName("faafaa"), hasItem(service_user));
		assertThat(user.getServiceUsersForServiceName("faafaa"), hasItem(service_user));
		assertThat(user.getServiceUserById(aId.toString()), notNullValue());
		assertThat(user.getServiceUserById(aId.toString()).getService(), equalTo("faafaa"));
		assertThat(user.getServiceUserById(aId.toString()).getUsername(), equalTo("Boo Boo Boo"));
		assertThat(user.getServiceUserById(aId.toString()).getOnBehalfOf(), equalTo(new_service_entry));
		user.save(test_dao);

	}


	@Test
	public void addServiceUserToHuman() {
		ServiceEntry service_entry = new ServiceEntry("service_entry_id_", "service_entry_username_", "service_entry_service_");
		ServiceUser service_user = new ServiceUser("id__", "username__", "name__", "image_url__", service_entry);

		Human human = new Human();
		human.setName("addServiceUserToHuman");
		human.addServiceUser(service_user);
		HumansUser user = new HumansUser();
		user.addHuman(human);
		user.save(test_dao);

		ServiceEntry new_service_entry = new ServiceEntry("new_id", "new_username", "faafaa");
		service_user = new ServiceUser();
		service_user.setUsername("Foo Foo Foo");
		service_user.setService("fiddlefaddle");
		service_user.setOnBehalfOf(new_service_entry);


		String human_id = user.getHumanByName("addServiceUserToHuman").getId();

		boolean result = user.addServiceUserToHuman(service_user, human_id);

		assertThat(result, is(true));
		user.save(test_dao);

		Human new_human = user.getHumanByID(human_id);
		assertThat(new_human.getName(), equalTo("addServiceUserToHuman"));

	}

	@Test
	public void addService() {
		ServiceEntry service_entry = new ServiceEntry("service_entry_id_", "service_entry_username_", "service_entry_service_");
		ServiceUser service_user = new ServiceUser("service_user_id", "service_user_username__", "service_user_name__", "service_user_image_url__", service_entry);

		Human human = new Human();
		human.setName("addService");
		human.addServiceUser(service_user);
		HumansUser user = new HumansUser();
		user.addHuman(human);
		user.setUsername("test-username");
		user.save(test_dao);

		ServiceEntry twitter = new ServiceEntry("185383", "darthjulian", "twitter");
		user.addService(twitter);
		user.save(test_dao);


		ServiceEntry instagram = new ServiceEntry("instagram_id", "instagram_username", "instagram");
		user.addService(instagram);
		user.save(test_dao);


		HumansUser loaded_user = test_dao.findOneByUsername("test-username");
		assertThat(loaded_user, notNullValue());
		assertThat(loaded_user.getAllHumans(), hasSize(1));
		assertThat(loaded_user.getAllHumans(), hasItem(human));
		assertThat(loaded_user.getHumanByName("addService"), notNullValue());
		assertThat(loaded_user.getServices(), hasSize(2));
		assertThat(loaded_user.getServices(), hasItem(twitter));
		assertThat(loaded_user.getServices(), hasItem(instagram));

	}

	@Test
	public void testRemoveService()
	{
		HumansUserDAO dao = new HumansUserDAO();
		HumansUser user = dao.findOneByUsername("darthjulian");
		
		Human h = new Human();
		ServiceEntry service = new ServiceEntry("faa", "foo", "fam"); //services.get(0);
		
		user.addService(service);
		
		h.addServiceUser(new ServiceUser("bing", "bang", "fam", "furl", service));

		user.addHuman(h);
		// make sure we have some services to remove
		assertThat(user.getServices().isEmpty(), is(false));

		user.save();

		
		//List<ServiceEntry> services = user.getServicesForServiceName(service.getServiceName());
		

		List<ServiceUser> service_users = user.getServiceUsersForAllHumansByServiceName(service.getServiceName());
		int count = service_users.size();
		System.out.println(service_users);
		assertThat(service_users.isEmpty(), is(false));
		assertThat(count, greaterThan(1));
		
		boolean result = user.removeServiceBy(service.getServiceName(), service.getServiceUserID());
		assertThat(result, equalTo(true));
//
//		
		service_users = user.getServiceUsersForAllHumansByServiceName(service.getServiceName());
		
		assertThat(service_users.size(), lessThan(count));
		
		//HumansUserDAO foo = new HumansUserDAO("test");
		user.save();
		
		//fail("Not implemented yet");
	}
	
	@Test
	public void removeServiceBy()
	{
		HumansUser user = new HumansUser();
		user.setUsername("removeServiceBy");
		ServiceEntry twitter = new ServiceEntry("185383", "darthjulian", "twitter");
		user.addService(twitter);
		user.save(test_dao);

		HumansUser loaded_user = test_dao.findOneByUsername("removeServiceBy");

		assertThat(loaded_user.getServices(), hasSize(1));

		boolean result = user.removeServiceBy("twitter", "185383");

		user.save(test_dao);

		loaded_user = test_dao.findOneByUsername("removeServiceBy");
		assertThat(result, is(true));
		assertThat(loaded_user, notNullValue());
		assertThat(loaded_user.getServices(), hasSize(0));

	}
	
	
	@Test
	public void testGetJsonStatusForHuman() {
		
	}
	
	
	@Test
	public void testGetStatusForHuman() {
		fail("Unimplemnted. Get to it.");
	}

	@Ignore
	public void test_getServicesFor() {
		HumansUserDAO dao = new HumansUserDAO();
		HumansUser user = dao.findOneByUsername("darthjulian");
		logger.debug(user.getServicesForServiceName("twitter"));
	}

	@Ignore
	public void test_getServices() {
		HumansUserDAO dao = new HumansUserDAO();
		HumansUser user = dao.findOneByUsername("darthjulian");
		//dao.save(user);
		List<ServiceEntry> service = user.getServices();
		Iterator<ServiceEntry> iter = service.iterator();
		while(iter.hasNext()) {
			ServiceEntry e = iter.next();
			logger.debug(e);
		}

		logger.debug(service);
		//		fail("Not yet implemented");
	}

	@Ignore
	public void test_addService() {
		HumansUserDAO dao = new HumansUserDAO();
		HumansUser user = dao.findOneByUsername("darthjulian");

		user.addService("1", "test", "noservice");
		Key k = dao.save(user);
		logger.debug(k);

	}
	
	@Test
	public void testGetServiceUsersRelyingOn() {
		
		fail("Not yet implemented");
	}

	@Test
	public void testRemoveServiceUsersRelyingOn() {
		HumansUserDAO dao = new HumansUserDAO();
		HumansUser user = dao.findOneByUsername("darthjulian");
		
		Human h = new Human();
		ServiceEntry service = new ServiceEntry("faaalalalal", "foo", "fam"); //services.get(0);
		
		user.addService(service);
		
		ServiceUser dependent_service_user_1 = new ServiceUser("bing", "bang", "fam", "furl", service);
		h.addServiceUser(dependent_service_user_1);

		ServiceUser dependent_service_user_2 = new ServiceUser("989881a9@", "jango", "fam", "http://foo.fab.com/989881a9@", service);
		h.addServiceUser(dependent_service_user_2);
		
		user.addHuman(h);
		// make sure we have some services to remove
		assertThat(user.getServices().isEmpty(), is(false));
		assertThat(user.getServices(), hasItem(service));

		assertThat(user.getServiceUsersRelyingOn(service), hasSize(2));
		assertThat(user.getServiceUsersRelyingOn(service), hasItems(dependent_service_user_1, dependent_service_user_2));

		user.removeServiceUsersRelyingOn(service);
		
		
		user.removeServiceBy(service.getServiceName(), service.getServiceUserID());
		
		assertThat(user.getServiceUsersRelyingOn(service), hasSize(0));
		
		assertThat(user.getServicesForServiceName(service.getServiceName()), hasSize(0));
		
		assertThat(user.getServices(), not(hasItem(service)));

	}
	
//	@Test
//	public void test_removeService() {
//		HumansUserDAO dao = new HumansUserDAO();
//		HumansUser user = dao.findOneByUsername("darthjulian");
//
//		user.removeService("11062822", "nearfuturelab", "twitter");
//		Key k = dao.save(user);
//		logger.debug(k);
//		
//		fail("Not implemented");
//
//	}

	@Ignore
	public void test_C_getFriends() {
		HumansUserDAO dao = new HumansUserDAO();
		HumansUser user = dao.findOneByUsername("darthjulian");
		List<MinimalSocialServiceUser> result = user.getFriends();
		//		for(MinimalSocialServiceUser friend : result) {
		//			logger.debug(friend);
		//		}

	}

	@Ignore
	public void test_A_serviceRequestFriends() {
		try {
			HumansUserDAO dao = new HumansUserDAO();
			HumansUser user = dao.findOneByUsername("darthjulian");
			user.serviceRequestFriends();
		} catch(Exception e) {
			logger.debug("", e);
		}

	}

	@Ignore
	public void test_B_getFriendsAsJson() {
		try {
			HumansUserDAO dao = new HumansUserDAO();
			HumansUser user = dao.findOneByUsername("darthjulian");
			JsonArray foo = user.getFriendsAsJson();
			//logger.debug(foo);
			Iterator<JsonElement> iter = foo.iterator();
			while(iter.hasNext()) {
				JsonElement obj = iter.next();
				logger.debug(obj);
				break;
			}
		} catch(Exception e) {
			logger.debug("", e);
		}
	}

	@Ignore
	public void test_getByHumanID() {
		try {
			HumansUserDAO dao = new HumansUserDAO();
			HumansUser h = dao.findOneByUsername("grignani");
			Human human = h.getHumanByID("52925b5403640e801a76a84c");
			logger.debug(human);
		} catch(Exception e) {
			logger.error("", e);
		}
	}

	@Test
	public void testRefreshStatus() {
		try {
			HumansUserDAO dao = new HumansUserDAO();
			HumansUser user = dao.findOneByUsername("darthjulian");
			user.getStatusForAllHumans(true);
			user = dao.findOneByUsername("fabien");
			user.getStatusForAllHumans(true);
			user = dao.findOneByUsername("nicolas");
			user.getStatusForAllHumans(true);
			user = dao.findOneByUsername("grignani");
			user.getStatusForAllHumans(true);
		} catch(Exception e) {
			logger.error("", e);
		}
	}

	@Test
	public void test_fixImageUrls() {
		try {
			HumansUserDAO dao = new HumansUserDAO();
			HumansUser user = dao.findOneByUsername("nicolas");
			List<Human> humans = user.getHumans();
			for(Human human : humans) {
				List<ServiceUser> service_users = human.getServiceUsers();
				List<ServiceUser> bad = new ArrayList<ServiceUser>();
				for(ServiceUser service_user : service_users) {
					if(service_user.getImageURL() == null || service_user.getImageURL().length() < 1) {
						bad.add(service_user);
					}
				}
				for(ServiceUser service_user : bad) {
					human.removeServiceUser(service_user);
					ServiceUser alt = human.fixImageUrls(service_user);
					human.addServiceUser(alt);
				}
			}
			user.save();

		} catch(Exception e) {
			logger.error("", e);
		}

	}

}
