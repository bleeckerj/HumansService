package com.nearfuturelaboratory.humans.entities;

//import static org.hamcrest.CoreMatchers.is;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.nearfuturelaboratory.humans.dao.HumansUserDAO;
import com.nearfuturelaboratory.humans.util.MongoUtil;
import com.nearfuturelaboratory.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.Morphia;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.fail;

//import org.apache.log4j.Level;
//import org.apache.log4j.PropertyConfigurator;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HumansUserTest {

    static {
//        Properties props = System.getProperties();
//        props.list(System.out);
//        System.out.println(props.getProperty("user.dir"));
//        System.out.println("");
        //System.setProperty(XMLConfigurationFactory.CONFIGURATION_FILE_PROPERTY, "/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/src/test/conf/log4j2.xml");


    }

    final static Logger logger = LogManager.getLogger(HumansUserTest.class);
    static HumansUserDAO test_dao;
    static HumansUserDAO dev_dao;
    static HumansUserDAO remote_dao;


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        //Logger.getRootLogger().setLevel(Level.OFF);

        try {
            Constants.load("/Users/julian/Documents/workspace/HumansService/src/main/webapp/WEB-INF/lib/dev.app.properties");
            //PropertyConfigurator.configureAndWatch("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/src/main/webapp/WEB-INF/lib/static-logger.properties");

            test_dao = new HumansUserDAO("humans-test");
            test_dao.getCollection().drop();

            dev_dao = new HumansUserDAO("humans");

            // gotta set up port forwarding..only way through
            /**
             * /usr/bin/ssh -o CheckHostIP=no -o TCPKeepAlive=yes -o StrictHostKeyChecking=no -o ServerAliveInterval=120 -o ServerAliveCountMax=100 -i /Users/julian/.ec2/AMZ-EC2.pem ec2-user@ec2-54-193-78-211.us-west-1.compute.amazonaws.com -N -L 29017:127.0.0.1:27017
             */
            MongoClient mongo = MongoUtil.getMongo("localhost", 27017);

            remote_dao = new HumansUserDAO(mongo, new Morphia(), "humans");


            logger.debug("Hey Ho!");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void changeUserPassword() {
        HumansUser user = remote_dao.findOneByUsername("maryb");
        if (user != null) {
            user.setPassword("boogaloo");
            user.save();
        }
        logger.info("User is now " + user);
    }

    @Test
    public void updateYouman() {
        HumansUser user = dev_dao.getHumansUser("darthjulian", "darthjulian");
        user.updateYouman();
        assertThat(user.getYouman(), is(notNullValue()));
        user.save();
        assertThat(user.getYouman(), is(notNullValue()));
        Human youman = user.getYouman();
        assertThat(youman.getServiceUsers(), is(not(empty())));
        for (ServiceUser service_user : youman.getServiceUsers()) {
            String service_user_id = service_user.getServiceUserID();
            ServiceUser youman_service_user = youman.getServiceUserByServiceUserId(service_user_id);
            assertThat(youman_service_user, is(equalTo(service_user)));
        }

    }


    @Test
    public void getHumansServiceUsersCount() {
        ServiceEntry service_entry = new ServiceEntry("id_", "username_", "service_");
        ServiceUser service_user = new ServiceUser("id__", "username__", "name__", "image_url__", service_entry);
        ObjectId aId = new ObjectId(new Date(), 1);
        service_user.setId(aId);
        Human human = new Human();
        human.setName("test");
        human.addServiceUser(service_user);



        HumansUser user = new HumansUser();
        user.addHuman(human);

        assertThat(user.getHumansServiceUsersCount(), is(1));

        aId = new ObjectId(new Date(), 2);
        service_entry = new ServiceEntry("id_2", "username_2", "service_");
        service_user = new ServiceUser("id__2", "username__2", "name__2", "image_url__2", service_entry);
        service_user.setId(aId);
        human.addServiceUser(service_user);

        assertThat(user.getHumansServiceUsersCount(), is(2));

        human.removeServiceUserById(aId.toString());

        assertThat(user.getHumansServiceUsersCount(), is(1));

        human.removeAllServiceUsers();
        assertThat(user.getHumansServiceUsersCount(), is(0));


    }

    @Test
    public void removeServiceUserById() {
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
        service_user.setServiceName("faafaa");
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
        assertThat(user.getServiceUserById(aId.toString()).getServiceName(), equalTo("faafaa"));
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
        service_user.setServiceName("fiddlefaddle");
        service_user.setOnBehalfOf(new_service_entry);


        String human_id = user.getHumanByName("addServiceUserToHuman").getId();

        boolean result = user.addServiceUserToHuman(service_user, human);

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
    public void testRemoveService() {
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
        assertThat(count, equalTo(1));

        boolean result = user.removeServiceBy(service.getServiceName(), service.getServiceUserID());
        assertThat(result, equalTo(true));
//
//		
        service_users = user.getServiceUsersForAllHumansByServiceName(service.getServiceName());

        assertThat(service_users.size(), equalTo(0));

        //HumansUserDAO foo = new HumansUserDAO("test");
        user.save();

        //fail("Not implemented yet");
    }

    @Test
    public void removeServiceBy() {
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
        fail("Unimplemnted. Get to it.");

    }


    @Test
    public void testGetStatusForHuman() {
        fail("Unimplemnted. Get to it.");
    }


    @Ignore
    public void test_getServices() {
        HumansUserDAO dao = new HumansUserDAO();
        HumansUser user = dao.findOneByUsername("darthjulian");
        //dao.save(user);
        List<ServiceEntry> service = user.getServices();
        Iterator<ServiceEntry> iter = service.iterator();
        while (iter.hasNext()) {
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

    @Test
    public void getJsonCachedStatusForHuman() {
        HumansUser user = dev_dao.findOneByUsername("darthjulian");
        Human human = user.getHumanByName("anti");
        JsonArray jsonStatusForHuman = user.getJsonCachedStatusForHuman(human, -1);
        assertThat(jsonStatusForHuman.size(), greaterThan(0));
        int pages = user.getJsonCachedStatusPageCountForHuman(human, 10);
        int total = jsonStatusForHuman.size();

        assertThat(total / 10, lessThanOrEqualTo(pages));

/*

        for(int i=0; i<user.getJsonStatusPageCountForHuman(human, Constants.getInt("STATUS_CHUNK_SIZE", 25)); i++) {
            JsonArray page = user.getJsonStatusForHuman(human, i);
            Iterator<JsonElement> iter = page.iterator();
            assertThat(page.size(), greaterThan(0));
        }
*/
    }


    @Test
    public void getJsonStatusPageCountForHuman() {
        HumansUser user = dev_dao.findOneByUsername("nicolas");
        Human human = user.getHumanByName("fabien");
        int pages = user.getJsonCachedStatusPageCountForHuman(human, 25);
        int total = user.getJsonCachedStatusCountForHuman(human);
        assertThat(total / 25, lessThanOrEqualTo(pages));
        logger.debug("total=" + total + " pages=" + pages);


        assertThat(total, greaterThan(0));
        assertThat(pages, greaterThan(0));
        //logger.warn("HEllo");
    }


    @Test
    public void getTimeOfMostRecentStatusForHuman() {
        HumansUser user = dev_dao.findOneByUsername("nicolas");
        Human human = user.getHumanByName("fabien");
        long time = user.getTimeOfMostRecentStatusForHuman(human);
        Date d = new Date(time);
        //ServiceStatus status = user.getMostRecentStatusForHuman(human);
    }

    @Test
    public void getTimeOfOldestCachedStatusForHuman() {
        HumansUser user = dev_dao.findOneByUsername("darthjulian");
        Human human = user.getHumanByName("Nova");
        long time = user.getTimeOfOldestCachedStatusForHuman(human);
        Date d = new Date(time);
    }

    @Test
    public void getTimeOfMostRecentCachedStatusForHuman() {
        HumansUser user = dev_dao.findOneByUsername("darthjulian");
        Human human = user.getHumanByName("Nova");
        long time = user.getTimeOfOldestCachedStatusForHuman(human);
        Date d = new Date(time);

    }

    @Test
    public void getTimeOfOldestStatusForHuman() {
        HumansUser user = dev_dao.findOneByUsername("darthjulian");
        Human human = user.getHumanByName("Nova");
        long time = user.getTimeOfOldestStatusForHuman(human);
        Date d = new Date(time);

    }

    @Test
    public void getJsonStatusCountForHuman() {
        HumansUser user = dev_dao.findOneByUsername("nicolas");
        Human human = user.getHumanByName("fabien");
        long count = user.getJsonCachedStatusCountForHuman(human);
        int count_int = (int) count;
        JsonArray all_status = user.getJsonCachedStatusForHuman(human, -1);
        assertThat(all_status.size(), equalTo(count_int));
    }


    @Test
    public void __getJsonStatusCountForHuman() {
        HumansUser user = dev_dao.findOneByUsername("darthjulian");
        Human human = user.getHumanByName("Dawn Mike Ella");
        long count = user.getJsonCachedStatusCountForHuman(human);
        JsonArray all_status = user.getJsonCachedStatusForHuman(human, -1);
        assertThat(all_status.size(), equalTo((int) count));
    }

    @Ignore
    public void getFriends() {
        HumansUser user = test_dao.findOneByUsername("darthjulian");

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

    @Test
    public void test_getServicesForServiceName() {
        HumansUser user = new HumansUser();
        user.setUsername("shempy");
        ServiceEntry twitter = new ServiceEntry("1", "shempy", "twitter");
        ServiceEntry foursquare = new ServiceEntry("1", "twombly", "foursquare");
        user.addService(twitter);
        user.addService(foursquare);
        user.addService(new ServiceEntry("2", "twinkles", "twitter"));

        List<ServiceEntry> serviceEntries = user.getServicesForServiceName("twitter");
        assertThat(serviceEntries, notNullValue());
        assertThat(serviceEntries, hasSize(2));
        assertThat(serviceEntries, hasItem(twitter));
        assertThat(serviceEntries, not(hasItem(foursquare)));

        serviceEntries = user.getServicesForServiceName("foursquare");
        assertThat(serviceEntries, notNullValue());
        assertThat(serviceEntries, hasSize(1));
        assertThat(serviceEntries, hasItem(foursquare));

    }

    @Test
    public void serviceRefreshStatusForHuman() {
        // if(remote_dao != null) {
        HumansUser user = test_dao.findOneByUsername("darthjulian");
        //HumansUser user = remote_dao.findOneByUsername("darthjulian");
        Human human = user.getHumanByName("Twitter Yammer");

        List<Human> humans = user.getAllHumans();


        user.serviceRefreshStatusForHuman(human);
        //  }
    }

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
        } catch (Exception e) {
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
            while (iter.hasNext()) {
                JsonElement obj = iter.next();
                logger.debug(obj);
                break;
            }
        } catch (Exception e) {
            logger.debug("", e);
        }
    }

    @Test
    public void encryptThings() {
        List<HumansUser> users = remote_dao.getAllHumansUsers();
        //List<HumansUser> users = HumansUser.getAllHumansUsers();
        for (HumansUser user : users) {
            user.save(remote_dao);
        }
    }


    @Test
    public void getHumanByID() {
        try {
            HumansUser h = dev_dao.findOneByUsername("darthjulian");
            Human human = h.getHumanByID("52df74850364e4bd329f50d5");
            logger.debug(human);
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    @Test
    public void test_serviceRefreshStatusForHuman() {
        try {
            HumansUserDAO dao = new HumansUserDAO();
            //HumansUser user;

            List<HumansUser> humans = dao.getAllHumansUsers();
            for (HumansUser user : humans) {
                for (Human human : user.getAllHumans()) {
                    user.serviceRefreshStatusForHuman(human);
                }
            }
        } catch (Exception e) {
            logger.error("", e);
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    @Test
    public void refreshCacheForUser() {
        HumansUser user = remote_dao.findOneByUsername("nicolasnova");
        user.refreshStatusForAllHumans();
    }


    @Test
    public void refreshOneCache() {
        //HumansUser user = remote_dao.findOneByUsername("darthjulian");
        HumansUser user = dev_dao.findOneByUsername("darthjulian");
        Human human = user.getHumanByName("Twitter Yammer");

        user.refreshCache(human);
    }

    @Test
    public void refreshCache() {
        List<HumansUser> users = remote_dao.getAllHumansUsers();
        if (users.size() > 0) {
            HumansUser test_user = users.get(0);
            if (test_user.getAllHumans() != null && test_user.getAllHumans().size() > 0) {
                Human test_human = test_user.getAllHumans().get(0);

                test_user.refreshCache(test_human);

            }
        }
    }

//    @Test
//    public void getStatusDetailsFromCache()
//    {
//        List<HumansUser> users = dev_dao.getAllHumansUsers();
//        if(users.size() > 0) {
//            HumansUser test_user = users.get(0);
//            if(test_user.getAllHumans() != null && test_user.getAllHumans().size() > 0) {
//                Human test_human = test_user.getAllHumans().get(0);
//
//                test_user.refreshCache(test_human);
//
//                JsonObject result = test_user.getStatusDetailsFromCache(test_human);
//
//                String cache_name = "status_cache_"+test_user.getId()+"_"+test_human.getId();
//
//                DB cache_db = MongoUtil.getStatusCacheDB();
//
//                //DBCollection cache = cache_db.getCollection(cache_name);
//
//                //assertThat((int)cache.count(), is(equalTo(cache_count)));
//
//            } else {
//                fail("HumansUser has no humans!");
//            }
//
//
//        } else {
//            fail("No HumansUsers to run test.");
//        }
//
//    }

    @Test
    public void getStatusDetailsFromCache() {

        HumansUser user = dev_dao.findOneByUsername("darthjulian");
        Human human = user.getHumanByName("Twitter Yammer");
        JsonObject obj = user.getStatusDetailsFromCache(human);
        assertThat(obj, is(not(null)));
    }

    @Test
    public void getStatusCountFromOneCache() {
        HumansUser user = dev_dao.findOneByUsername("darthjulian");
        Human human = user.getHumanByName("Twitter Yammer");
        int cache_count = user.getStatusCountFromCache(human);
        String cache_name = "status_cache_" + user.getId() + "_" + human.getId();

        DB cache_db = MongoUtil.getStatusCacheDB();

        // the cache is structured such that it has a bunch of heterogenous service-specific status elements
        // and one element that's the cache header that contains some metadata about the cache
        DBCollection cache = cache_db.getCollection(cache_name);
        assertThat((int) cache.count() - 1, is(equalTo(cache_count)));
    }

    @Test
    public void getStatusCountFromCache() {
        List<HumansUser> users = dev_dao.getAllHumansUsers();
        if (users.size() > 0) {
            HumansUser test_user = users.get(0);
            if (test_user.getAllHumans() != null && test_user.getAllHumans().size() > 0) {
                Human test_human = test_user.getAllHumans().get(0);

                test_user.refreshCache(test_human);

                int cache_count = test_user.getStatusCountFromCache(test_human);

                String cache_name = "status_cache_" + test_user.getId() + "_" + test_human.getId();

                DB cache_db = MongoUtil.getStatusCacheDB();

                DBCollection cache = cache_db.getCollection(cache_name);

                assertThat((int) cache.count(), is(equalTo(cache_count)));

            } else {
                fail("HumansUser has no humans!");
            }


        } else {
            fail("No HumansUsers to run test.");
        }

    }

    @Test
    public void getStatusCountFromCacheAfterTimestamp() {

        List<HumansUser> users = dev_dao.getAllHumansUsers();
        if (users.size() > 0) {
            HumansUser test_user = users.get(0);
            if (test_user.getAllHumans() != null && test_user.getAllHumans().size() > 0) {
                Human test_human = test_user.getAllHumans().get(0);

                test_user.refreshCache(test_human);

                JsonArray status = test_user.getJsonStatusFromCache(test_human, -1);

                if (status.size() < 1 || status.size() < 10) {
                    fail("No/not enough status to test. user=" + test_user + " human=" + test_human);
                } else {

                    JsonObject nine = status.get(9).getAsJsonObject();
                    JsonObject zero = status.get(0).getAsJsonObject();
                    String x = String.valueOf(nine.get("created"));
                    long ago = Long.valueOf(String.valueOf(nine.get("created")));
                    int after_nine = test_user.getStatusCountFromCacheAfterTimestamp(test_human, ago);
                    assertThat(after_nine, is(equalTo(9)));
                }


            } else {
                fail("HumansUser has no humans!");
            }


        } else {
            fail("No HumansUsers to run test.");
        }

    }

    @Test
    public void testRefreshStatus() {
        try {
            HumansUserDAO dao = new HumansUserDAO();
            HumansUser user;

            user = dao.findOneByUsername("darthjulian");
            user.getStatusForAllHumans(true);
            user = dao.findOneByUsername("nicolas");
            user.getStatusForAllHumans(true);
            user = dao.findOneByUsername("fabien");
            user.getStatusForAllHumans(true);
            user = dao.findOneByUsername("grignani");
            user.getStatusForAllHumans(true);
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    @Test
    public void test_fixImageUrls() {
        try {
            HumansUserDAO dao = new HumansUserDAO();
            HumansUser user = dao.findOneByUsername("nicolas");
            List<Human> humans = user.getHumans();
            for (Human human : humans) {
                List<ServiceUser> service_users = human.getServiceUsers();
                List<ServiceUser> bad = new ArrayList<ServiceUser>();
                for (ServiceUser service_user : service_users) {
                    if (service_user.getImageURL() == null || service_user.getImageURL().length() < 1) {
                        bad.add(service_user);
                    }
                }
                for (ServiceUser service_user : bad) {
                    human.removeServiceUser(service_user);
                    ServiceUser alt = human.fixImageUrls(service_user);
                    human.addServiceUser(alt);
                }
            }
            user.save();

        } catch (Exception e) {
            logger.error("", e);
        }

    }

}
