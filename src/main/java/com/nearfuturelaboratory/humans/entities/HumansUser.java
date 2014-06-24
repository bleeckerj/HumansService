package com.nearfuturelaboratory.humans.entities;

import com.google.gson.*;
import com.mongodb.*;
import com.mongodb.util.JSON;
import com.nearfuturelaboratory.humans.dao.HumansUserDAO;
import com.nearfuturelaboratory.humans.exception.BadAccessTokenException;
import com.nearfuturelaboratory.humans.flickr.entities.FlickrFriend;
import com.nearfuturelaboratory.humans.flickr.entities.FlickrUser;
import com.nearfuturelaboratory.humans.foursquare.entities.FoursquareFriend;
import com.nearfuturelaboratory.humans.foursquare.entities.FoursquareUser;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramFriend;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramUser;
import com.nearfuturelaboratory.humans.service.FlickrService;
import com.nearfuturelaboratory.humans.service.FoursquareService;
import com.nearfuturelaboratory.humans.service.InstagramService;
import com.nearfuturelaboratory.humans.service.TwitterService;
import com.nearfuturelaboratory.humans.service.status.ServiceStatus;
import com.nearfuturelaboratory.humans.twitter.entities.TwitterFriend;
import com.nearfuturelaboratory.humans.twitter.entities.TwitterUser;
import com.nearfuturelaboratory.humans.twitter.entities.generated.TwitterStatus;
import com.nearfuturelaboratory.humans.util.MongoUtil;
import com.nearfuturelaboratory.humans.util.MyObjectIdSerializer;
import com.nearfuturelaboratory.util.Constants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.jasypt.util.binary.BasicBinaryEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.Period;
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.utils.IndexDirection;
import org.scribe.exceptions.OAuthConnectionException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

import static ch.lambdaj.Lambda.*;
import static ch.lambdaj.function.matcher.AndMatcher.and;
import static org.hamcrest.CoreMatchers.equalTo;

@Entity(value="users",noClassnameStored = true)
public class HumansUser extends BaseEntity {

    final static Logger logger = LogManager.getLogger(com.nearfuturelaboratory.humans.entities.HumansUser.class);

    @Indexed(value = IndexDirection.ASC, name = "username", unique = true, dropDups = true)
    private String username;
    private String password;
    //protected String email;
    protected Boolean emailVerified = false;
    // @Indexed(name="access_token", unique = true, sparse = true)
    protected String access_token;

    protected byte[] access_token_bytes;

    protected Boolean isAdmin = false;
    protected Boolean isSuperuser = false;

    @Transient
    String email;
    byte[] email_bytes;

    @Embedded("humans")
    protected List<Human> humans = new ArrayList<Human>();

    //	@Embedded("humans.serviceUsers")
    //	protected List<ServiceUser> serviceUsers = new ArrayList<ServiceUser>();

    // Services are a list of a Service Name mapped to a List of Pair<ServiceID, ServiceUsername>
    // Service entry looks like "flickr" : {["66854529@N00","JulianBleecker"],["858291847@N11","Near Future Laboratory"]}
    //	protected List<Map<String, List<ServiceEntry>>> services = new ArrayList<Map<String, List<ServiceEntry>>>();
    //
    @Embedded("services")
    protected List<ServiceEntry> services = new ArrayList<ServiceEntry>();
    //protected List<Map<String,List<ServiceEntry>>> services;

    @PrePersist
    void prePersist() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(email);
            email_bytes =this.EncryptByteArray(baos.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(access_token);
            access_token_bytes =this.EncryptByteArray(baos.toByteArray());
            //logger.debug((this.getUsername()+"access_token_bytes="+email_bytes));

        } catch(Exception e) {
            logger.error(e);
        }

    }

    @PostLoad
    void postLoad() {
        try {
            email_bytes = this.DecryptByteArray(email_bytes);
            ObjectInputStream ois = new ObjectInputStream(
                    new ByteArrayInputStream(email_bytes));
            email = (String) ois.readObject();

            //this.updateYouman();

        } catch (Exception e) {
            // e.printStackTrace();

            logger.error(
                    "The email " + email + " address for " + this.getUsername() + " probably do not represent an encrypted String. You may need to encrypt " + this.getUsername() + "'s email address.");
        }
        try {
            access_token_bytes = this.DecryptByteArray(access_token_bytes);
            ObjectInputStream ois = new ObjectInputStream(
                    new ByteArrayInputStream(access_token_bytes));
            access_token = (String) ois.readObject();
        } catch (Exception e) {
            // e.printStackTrace();

            logger.error(
                    "The access_token " + access_token + " for " + this.getUsername() + " probably do not represent an encrypted String. You may need to encrypt " + this.getUsername() + "'s access_token.");
        }
    }

    protected byte[] EncryptByteArray(byte[] array) throws Exception {
        BasicBinaryEncryptor binaryEncryptor = new BasicBinaryEncryptor();
        binaryEncryptor.setPassword(Constants.getString("EMAIL_PW"));
        byte[] myEncryptedBytes = binaryEncryptor.encrypt(array);
        return myEncryptedBytes;
    }

    protected byte[] DecryptByteArray(byte[] array) throws Exception {
        BasicBinaryEncryptor binaryEncryptor = new BasicBinaryEncryptor();
        binaryEncryptor.setPassword(Constants.getString("EMAIL_PW"));
        // byte[] myEncryptedBytes = binaryEncryptor.encrypt(array);
        byte[] plainBytes = binaryEncryptor.decrypt(array);
		/*
		 * Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
		 * aes.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getBytes(),
		 * "AES")); byte[] ciphertext = aes.doFinal(array); return ciphertext;
		 */
        return plainBytes;
    }

    public HumansUser() {
        super();
    }

    public void updateYouman()
    {
        //boolean has_modified = false;
        Human youman = getYouman();

        List<ServiceUser> service_users = youman.getServiceUsers();
        List<ServiceEntry> humans_services = new ArrayList<ServiceEntry>(this.getServices());
        List<ServiceUser> humans_service_users_faux  = new ArrayList<ServiceUser>();

        try {
            for (ServiceEntry service_entry : humans_services) {
                ServiceUser fromServiceEntry = makeServiceUserFromServiceEntry(service_entry);
                humans_service_users_faux.add(fromServiceEntry);
            }
        } catch (Exception e) {
            logger.warn(e);
            logger.warn("Really weird. I don't know what could be concurrently modified here.", e);
        }

        Collection<ServiceUser> addToYouman =  CollectionUtils.subtract(humans_service_users_faux, service_users);
        Collection<ServiceUser> removeFromYouman = CollectionUtils.subtract(service_users, humans_service_users_faux);

        if(addToYouman.size() > 0 || removeFromYouman.size() > 0) {


            this.removeHuman(youman);
            for (ServiceUser service_user : addToYouman) {
                youman.addServiceUser(service_user);
            }
            for (ServiceUser service_user : removeFromYouman) {
                youman.removeServiceUser(service_user);
            }

            if (youman.getServiceUsers() != null && youman.getServiceUsers().size() > 0) {
                this.addHuman(youman);
            } else {
                if (youman.getId() != null) {
                    this.removeHuman(youman);
                }
            }
        }
    }

    protected void youmanHygeine() {
        // for some reason i've noticed that the
    }


    /**
     * Create a Youman
     * @return
     */
    protected Human createYouman()
    {
        Human human = new Human();
        human.setName(this.getUsername());
        human.setYouMan(true);
        for(ServiceEntry serviceEntry : services) {
            ServiceUser service_user = makeServiceUserFromServiceEntry(serviceEntry);
            human.addServiceUser(service_user);
        }
        //this.save();
        return human;
    }

    /**
     * return the mirror of this HumansUser based on all of their linked Services
     * @return
     */
    protected Human getYouman()
    {
        Human result = null;
        for(Human human : humans) {
            if(human.isYouMan()) {
                result = human;
                break;
            }
        }
        if(result == null) {
            // woops..doesn't have one so make it!
            result = createYouman();
            this.addHuman(result);
        }
        return result;
    }


    protected ServiceUser makeServiceUserFromServiceEntry(ServiceEntry aServiceEntry) {
        ServiceUser result = new ServiceUser();
        //result.setServiceEntry(aServiceEntry);
        result.setOnBehalfOf(aServiceEntry);
        result.setUsername(aServiceEntry.getServiceUsername());
        result.setServiceName(aServiceEntry.getServiceName());
        result.setServiceUserID(aServiceEntry.getServiceUserID());
        result.setImageURL(getImageURLFor(aServiceEntry));
        return result;
    }


    protected String getImageURLFor(ServiceEntry aServiceEntry) {
        String result = "";
        ServiceEntry se = aServiceEntry;
        if(aServiceEntry.getServiceName().equalsIgnoreCase("instagram")) {
            try {
                InstagramService instagram = InstagramService.createServiceOnBehalfOfUsername(se.getServiceUsername());
                InstagramUser u = instagram.serviceRequestUserBasicForUserID(se.getServiceUserID());
                result = u.getLargeImageURL();
            } catch(Exception e) {
                //e.printStackTrace();
                logger.error(e.getMessage());
            }
        }
        if(aServiceEntry.getServiceName().equalsIgnoreCase("twitter")) {
            try {
                TwitterService service = TwitterService.createTwitterServiceOnBehalfOfUsername(se.getServiceUsername());
                TwitterUser u = service.serviceRequestUserBasicForUserID(se.getServiceUserID());
                result = u.getImageURL();
            } catch(Exception e) {
                e.printStackTrace();
                logger.error(e.getMessage());

            }
        }
        if(aServiceEntry.getServiceName().equalsIgnoreCase("foursquare")) {
            try {
                FoursquareService service = FoursquareService.createFoursquareServiceOnBehalfOfUserID(se.getServiceUserID());
                FoursquareUser u = service.serviceRequestUserBasicForUserID(se.getServiceUserID());
               result = u.getImageURL();
            } catch(Exception e) {
                e.printStackTrace();
                logger.error(e.getMessage());

            }
        }
        if(aServiceEntry.getServiceName().equalsIgnoreCase("flickr")) {
            try {
                FlickrService service = FlickrService.createFlickrServiceOnBehalfOfUserID(se.getServiceUserID());
                FlickrUser u = service.serviceRequestUserBasicForUserID(se.getServiceUserID());
                result = u.getImageURL();
            } catch(Exception e) {
                e.printStackTrace();
                logger.error(e.getMessage());

            }

        }

        return result;
    }


    public static boolean doesUsernameExist(String aUsername) {
        HumansUserDAO dao = new HumansUserDAO();
        return dao.doesUsernameExist(aUsername);
    }

    /**
     * Get all the humans users in the canonical datastore
     *
     * @return a List of HumansUser
     */
    public static List<HumansUser> getAllHumansUsers() {
        HumansUserDAO dao = new HumansUserDAO();
        return dao.getDatastore().find(dao.getEntityClass()).order("username").asList();
    }

    public boolean isValidUser() {
        boolean result = false;
        if (verifyPassword(getPassword()) == false) {
            //userJSON = new JSONObject();
            logger.info("Bad login. Returning empty user");
        } else {
            result = true;
        }
        return result;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String aUsername) {
        username = aUsername;
    }

    public String getPassword() {
        return password;
    }

    /**
     * This will encrypt the clear password and set the property with that encrypted password
     * @param aClearPassword
     */
    public void setPassword(String aClearPassword) {
        StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
        String encryptedPassword = passwordEncryptor
                .encryptPassword(aClearPassword);

        password = encryptedPassword;
    }


    public boolean verifyPassword(String aPassword) {
        StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
        boolean result = false;
        if (passwordEncryptor.checkPassword(aPassword, this.getPassword())) {
            // correct!
            result = true;
        } else {
            // bad login!
            result = false;
            logger.warn("Bad password attempt for " + this.getUsername()
                    + " " + aPassword);
        }
        return result;
    }


    public void setAccessToken(String aToken) {
        this.access_token = aToken;
    }

    public String getAccessToken() {
        return this.access_token;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String aEmail) {
        email = aEmail;
    }
    public List<Human> getHumans() {
        return humans;
    }

    public void setHumans(List<Human> aHumans) {
        humans = aHumans;
    }

    public boolean addHuman(Human aHuman) {
        boolean result = false;
        if(humans.contains(aHuman)) {
            humans.remove(aHuman);
            result = humans.add(aHuman);
        } else {
            result = humans.add(aHuman);
        }
        return result;
    }

    public void removeHuman(Human aHuman) {
        humans.remove(aHuman);
        if(aHuman.getId() != null) {
            removeCacheForHuman(aHuman);
        }
    }

    /**
     * Remove a Human by ID
     * @param aHumanId
     * @return true if it was removed, false otherwise.
     */
    public boolean removeHumanById(String aHumanId) {
        boolean result;
        // trying weird lambdaj syntax
        Human human = selectUnique(this.getHumans(), having(on(Human.class).getHumanid(), equalTo(new ObjectId(aHumanId))));
        result = humans.remove(human);
        if(result) {
            removeCacheForHuman(human);
        }
        return result;
    }

    /**
     * Update a human by id.
     * @param aUpdatedHuman
     * @param aHumanId
     * @return true if it was updated, false otherwise
     */
    public boolean updateHumanById(Human aUpdatedHuman, String aHumanId)
    {
        boolean result = false;
        if(removeHumanById(aHumanId)) {
            aUpdatedHuman.setId(aHumanId);
            result = this.addHuman(aUpdatedHuman);
        }
        return result;
    }


    /**
     *
     * @param aUpdatedServiceUser
     * @param aServiceUserId
     * @return
     */
    public boolean updateServiceUserById(ServiceUser aUpdatedServiceUser, String aServiceUserId)
    {
        boolean result = false;
        boolean removed = false;

        Human container = null;
        ServiceUser service_user = null;

        for(Human human : getHumans()) {
            service_user = human.getServiceUserById(aServiceUserId);
            if(service_user != null) {
                container = human;
                removed = container.removeServiceUserById(aServiceUserId);
                break;
            } else {
                continue;
            }
        }

        if(removed && container != null) {
            aUpdatedServiceUser.setId(new ObjectId(aServiceUserId));
            result = container.addServiceUser(aUpdatedServiceUser);
        }


        return result;
    }

    /**
     * Removes a ServiceUser with a specific ID.
     * @param aServiceUserId
     * @return true if it was removed, false if it did not, which could happen if there's an error or if the ServiceUser wasn't removed because it doesn't exist in this HumansUser's Humans
     */
    public boolean removeServiceUserById(String aServiceUserId) {
        boolean result = false;
        Human container;
        ServiceUser service_user;
        List<Human> empty_humans = new ArrayList<Human>();

        for(Human human : getHumans()) {
            service_user = human.getServiceUserById(aServiceUserId);
            if(service_user != null) {
                container = human;
                result = container.removeServiceUserById(aServiceUserId);
                if(result == true) {
                    removeCacheForHuman(human);
                }
                if(container.getServiceUsers().size() < 1) {
                    empty_humans.add(container);
                }
                break;
            } else {
                continue;
            }
        }

        if(result) {
            this.save();
        }
        return result;
      }

    /**
     * Returns true if this Human has a ServiceUser with a specific Id
     *
     * @param aServiceUserId is a String version of the ObjectId of the ServiceUser
     * @return
     */
    public boolean containsServiceUserById(String aServiceUserId) {
        boolean result = false;
        //Human container;
        ServiceUser service_user;

        for(Human human : getHumans()) {
            service_user = human.getServiceUserById(aServiceUserId);
            if(service_user != null) {
                result = true;
                break;
            } else {
                continue;
            }
        }
        return result;

    }

    public ServiceUser getServiceUserById(String aServiceUserId) {
        ServiceUser service_user = null;

        for(Human human : getHumans()) {
            service_user = human.getServiceUserById(aServiceUserId);
            if(service_user != null) {
                break;
            } else {
                continue;
            }
        }
        return service_user;

    }

    public boolean addServiceUserToHuman(ServiceUser aServiceUser, Human aHuman) {
        boolean result = false;
        if(aHuman != null) {
            result = aHuman.addServiceUser(aServiceUser);
            if(result) {
                this.removeCacheForHuman(aHuman);
            }
        }
        return result;
    }


    public List<Human> getAllHumans() {
        return this.humans;

    }

    public Human getHumanByID(String aID) {
        Human human = selectUnique(humans, having(on(Human.class).getHumanid(), equalTo(new ObjectId(aID))));
        return human;
    }

    public Human getHumanByName(String aHumanName) {
        Human result = null;
        List<Human> humans = getAllHumans();
        for(Human human : humans) {
            if(human.getName() != null && human.getName().equalsIgnoreCase(aHumanName)) {
                result = human;
                break;
            }
        }
        return result;
    }


    public List<ServiceUser> getServiceUsersForAllHumansByServiceName(String aService) {
        List<ServiceUser> result = new ArrayList<ServiceUser>();
        List<ServiceUser> all = getServiceUsersForAllHumans();
        for(int i=0; i<all.size(); i++) {
            ServiceUser su = all.get(i);
            if(su.getServiceName().equalsIgnoreCase(aService)) {
                result.add(su);
            }
        }
        return result;

    }

    /**
     * Get Friends as Json
     * @return
     */
    public JsonArray getFriendsAsJson() {
        Gson gson = new GsonBuilder().registerTypeAdapter(ObjectId.class, new MyObjectIdSerializer()).create();
        //JSONObject result = new JSONObject();
        //JsonArray friend_json = new JsonArray();
        //Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        List<JsonObject> f = new ArrayList<JsonObject>();
        JsonArray result = new JsonArray();
        List<MinimalSocialServiceUser> friends = getFriends();
        for(MinimalSocialServiceUser friend : friends) {

            JsonObject obj = new JsonObject();
            try {
                obj.addProperty("id", friend.getIdStr());
            } catch(Exception e) {
                logger.error("error", e);
            }
            obj.addProperty("serviceUserID", friend.getUserID());
            obj.addProperty("username", friend.getUsername());
            obj.addProperty("serviceName", friend.getServiceName());
            obj.addProperty("imageURL", friend.getImageURL());
            obj.addProperty("largeImageURL", friend.getLargeImageURL());
            obj.addProperty("fullname", friend.getFullName());
            obj.addProperty("lastUpdated", String.valueOf(friend.getLastUpdated().getTime()));

            JsonObject onBehalfOf = new JsonObject();
            onBehalfOf.addProperty("serviceName", friend.getServiceName());
            onBehalfOf.addProperty("serviceUserID", friend.getOnBehalfOf().getServiceUserID());
            onBehalfOf.addProperty("serviceUsername", friend.getOnBehalfOf().getServiceUsername());
            obj.add("onBehalfOf", onBehalfOf);
            f.add(obj);

        }

        for(JsonObject o : f) {
            result.add(o);
        }

        // now cache it..

        return result;
    }


    //TODO There should be a way to get friends cached in the local database. Each service has a friend collection
    public List<MinimalSocialServiceUser> getFriends() {
        List<MinimalSocialServiceUser> friends = new ArrayList<MinimalSocialServiceUser>();
        for(ServiceEntry service_entry : getServices()) {
            //logger.debug(service_entry);
            if(service_entry.getServiceName().equalsIgnoreCase("flickr")) {
                FlickrService flickr;
                try {
                    flickr = FlickrService.createFlickrServiceOnBehalfOfUserID(service_entry.getServiceUserID());
                    for(FlickrFriend f : flickr.getFriends()) {
                        friends.add(f);
                    }
                } catch (BadAccessTokenException | org.scribe.exceptions.OAuthConnectionException e) {
                    logger.warn(e+" we will use what we can from flickr.friend collection in the db");
                }
            }
            if(service_entry.getServiceName().equalsIgnoreCase("instagram")) {
                InstagramService instagram = new InstagramService();
                try {
                    instagram = InstagramService.createServiceOnBehalfOfUsername(service_entry.getServiceUsername());
                    for(InstagramFriend f : instagram.getFriends()) {
                        //f.setOnBehalfOf(service_entry);
                        friends.add(f);
                    }
                } catch (BadAccessTokenException | org.scribe.exceptions.OAuthConnectionException e) {
                    logger.warn(e+" we will use what we can from instagram.friend collection in the db");
                }
            }
            if(service_entry.getServiceName().equalsIgnoreCase("twitter")) {
                try {
                    TwitterService twitter = TwitterService.createTwitterServiceOnBehalfOfUsername(service_entry.getServiceUsername());
                    for(TwitterFriend f : twitter.getFriends() ) {
                        friends.add(f);
                    }
                } catch(BadAccessTokenException | OAuthConnectionException e) {
                    logger.warn(e+" we will use what we can from twitter.friend collection in the db");
                }
            }
            if(service_entry.getServiceName().equalsIgnoreCase("foursquare")) {
                FoursquareService foursquare = null;
                try {
                    foursquare = FoursquareService.createFoursquareServiceOnBehalfOfUserID(service_entry.getServiceUserID());
                    for(FoursquareFriend f : foursquare.getFriends() ) {
                        friends.add(f);
                    }
                } catch (org.scribe.exceptions.OAuthConnectionException e) {
                    logger.warn(e+" we will use what we can from foursquare.friend collection in the db");
                    //FoursquareFriendDAO dao = new FoursquareFriendDAO();
                    if(foursquare != null) {
                        for(FoursquareFriend f : foursquare.getFriends() ) {
                            friends.add(f);
                        }
                    }

                } catch(BadAccessTokenException e) {
                    logger.warn(e+" for userid="+service_entry.getServiceUserID()+" username="+service_entry.getServiceUsername()+" user="+this);
                }
            }
        }
        return friends;

    }

    /**
     * Get the friends according to the Social Service idiom for people I pay attention to/follow.
     * This would not necessarily be people who follow me in the case where I do not follow them back.
     * @return
     */
    public List<MinimalSocialServiceUser> serviceRequestFriends() {
        List<MinimalSocialServiceUser> friends = new ArrayList<MinimalSocialServiceUser>();

        for(ServiceEntry service_entry : getServices()) {
            if(service_entry.getServiceName().equalsIgnoreCase("flickr")) {
                FlickrService flickr;
                try {
                    flickr = FlickrService.createFlickrServiceOnBehalfOfUserID(service_entry.getServiceUserID());
                    if(flickr.localFriendsIsFresh() == false) {
                        flickr.serviceRequestFriends();
                    }
                    friends.addAll(flickr.getFriends());
                } catch (BadAccessTokenException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    logger.error("",e);
                }
            }
            if(service_entry.getServiceName().equalsIgnoreCase("instagram")) {
                InstagramService instagram;
                try {
                    instagram = InstagramService.createServiceOnBehalfOfUsername(service_entry.getServiceUsername());
                    if(instagram.localFriendsIsFresh() == false) {
                        instagram.serviceRequestFriends();
                    }
                    friends.addAll(instagram.getFriends());

                } catch (BadAccessTokenException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    logger.error("",e);
                }
            }
            if(service_entry.getServiceName().equalsIgnoreCase("twitter")) {
                TwitterService twitter;
                try {
                    twitter = TwitterService.createTwitterServiceOnBehalfOfUsername(service_entry.getServiceUsername());
                    if(twitter.localFriendsIsFresh() == false) {
                        twitter.serviceRequestFollows();
                    }
                    friends.addAll(twitter.getFriends());
                } catch(BadAccessTokenException e) {
                    // TODO Auto-generated catch block
                    logger.warn("",e);
                }
            }
            if(service_entry.getServiceName().equalsIgnoreCase("foursquare")) {
                try {
                    FoursquareService foursquare = FoursquareService.createFoursquareServiceOnBehalfOfUserID(service_entry.getServiceUserID());
                    if(foursquare.localFriendsIsFresh() == false) {
                        foursquare.serviceRequestFriends();
                    }
                    friends.addAll(foursquare.getFriends());
                } catch (BadAccessTokenException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    logger.error("",e);
                }
            }

        }

        return friends;
    }


    // Fri Nov 29 23:48:16 PST 2013 "EE MMM dd HH:mm:ss z YYYY"
    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss")
            .registerTypeAdapter(ObjectId.class, new MyObjectIdSerializer())
            .addSerializationExclusionStrategy(new StatusCacheJsonExclusionStrategy())
            .create();
    //private static DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");


    protected boolean isCachedStatusStale(Human aHuman) {
        boolean result = true;
        DB cache_db = MongoUtil.getStatusCacheDB();
        DBCollection cache = cache_db.getCollection("status_cache_"+this.getId()+"_"+aHuman.getId());

        BasicDBObject query = new BasicDBObject("key", this.getId()+"_"+aHuman.getId());
        DBCursor cursor = cache.find(query);
        DBObject key = null;
        if(cursor.hasNext()) {
            key = cursor.next();
        }
        if(key != null) {
            //logger.debug("Does this ever change format?? "+key.get("lastUpdated")+" from:"+cache.getName());

            Date d = (Date)key.get("lastUpdated");//HumansUser.format.parse( first.get("lastUpdated").toString() );
            long then = d.getTime();
            long now = new Date().getTime();
            long diff = now - then;
            //logger.debug(d+" "+diff+" "+Constants.getLong("STATUS_STALE_TIME"));
            if (diff < Constants.getLong("STATUS_STALE_TIME")) {
                result = false;
            }

        }

        return result;
    }

/*
    public List<ServiceStatus> getStatusForAllHumans() {

        return getStatusForAllHumans(false);
    }
*/

    /**
     * Called by ScheduledStatusFetcher to refresh the status for humans..
     */
    public void refreshStatusForAllHumans()
    {
        List<Human> humans = getAllHumans();
        for(Human human : humans) {
//            if(human.isYouMan()) {
//                this.updateYouman();
//            }
            serviceRefreshStatusForHuman(human);
            refreshCache(human);
        }
        this.updateYouman();
        Human h = this.getYouman();
        serviceRefreshStatusForHuman(h);
        refreshCache(h);

    }

    public List<ServiceStatus> getStatusForAllHumans(boolean loadIfStale) {
        List<ServiceStatus> allStatus = new ArrayList<ServiceStatus>();
        List<Human> humans = getAllHumans();
        for(Human human : humans) {
            allStatus.addAll(getStatusForHuman(human, loadIfStale));
        }
        return allStatus;

    }

//TODO There's no way any DB classes/methods/expectations should be in this class at all..

    public int getJsonCachedStatusCountForHuman(Human aHuman) {
        int result;
        String cache_name = "status_cache_"+this.getId()+"_"+aHuman.getId();
        DB cache_db = MongoUtil.getStatusCacheDB();
        if(cache_db.collectionExists(cache_name)) {
            DBCollection cache = cache_db.getCollection(cache_name);

            //DBCursor cursor;
            // don't count the "key" document
            long result_long = cache.count()-1;
            if (result_long < Integer.MIN_VALUE || result_long > Integer.MAX_VALUE) {
                logger.error("You've got too much status for "+aHuman+" "+result_long +" cannot be cast to int without changing its value.");
                throw new IllegalArgumentException
                        (result_long + " cannot be cast to int without changing its value.");
            }
            result = (int) result_long;

        } else {
            result = getStatusCountForHuman(aHuman);
        }
        //DBCursor cursor = cache.find(  );
        return result;
    }


    public int getJsonCachedStatusPageCountForHuman(Human aHuman, int aPageSize) {
        long count = getJsonCachedStatusCountForHuman(aHuman);
        return ((int)count + aPageSize - 1) / aPageSize;
    }

    public JsonArray getJsonCachedStatusForHuman(Human aHuman, int aPage) {
        String cache_name = "status_cache_"+this.getId()+"_"+aHuman.getId();

        DB cache_db = MongoUtil.getStatusCacheDB();
        JsonArray result = new JsonArray();
        if(cache_db.collectionExists(cache_name) && this.isCachedStatusStale(aHuman) == false) {
            result = getJsonStatusFromCache(aHuman, aPage);
        } else
        if(cache_db.collectionExists(cache_name) && this.isCachedStatusStale(aHuman) == true) {
            ///// let the scheduled background guy deal with refreshing the cache..
            ///// but maybe there's a way to goad it to do it sooner?
            ////refreshCache(aHuman);
            result = getJsonStatusFromCache(aHuman, aPage);
        } else
        if(cache_db.collectionExists(cache_name) == false) {
            logger.warn("No cache available for "+aHuman.getName() +" for "+this.getUsername()+" - taking the long way 'round");
            ////////////////refreshCache(aHuman);
            ////////////////result = getJsonStatusFromCache(aHuman, aPage);

        }
        if(result.size() < 1) {
            logger.warn("Why didn't we get any status for "+aHuman.getName()+" "+cache_name+" page="+aPage);
            logger.warn("cache exists="+cache_db.collectionExists(cache_name) + " cache status stale="+this.isCachedStatusStale(aHuman));
            logger.warn("number of pages is probably "+getJsonCachedStatusPageCountForHuman(aHuman, Constants.getInt("STATUS_CHUNK_SIZE", 25))+" Perhaps we're out of range?");
        }
        return result;
    }


    protected void removeCacheForHuman(Human aHuman)
    {
        if(this.getId() != null && aHuman.getId() != null) {
            String cache_name = "status_cache_" + this.getId() + "_" + aHuman.getId();

            DB cache_db = MongoUtil.getStatusCacheDB();

            if (cache_db.collectionExists(cache_name)) {
                DBCollection cache = cache_db.getCollection(cache_name);
                cache.drop();
                logger.info("removing cache for " + aHuman.getName() + " / " + aHuman.getId());
            }
        }
    }



    public void refreshCache(Human aHuman)
    {
        logger.info("refreshing cache for "+aHuman.getName() + " / "+aHuman.getId());
        List<ServiceStatus> statuses = getStatusForHuman(aHuman, false);

        cacheStatusForHuman(aHuman, statuses);
    }

    /**
     * Returns a count of status in a human cache greater than (after) a given timestamp
     * @param aHuman
     * @param aTimestamp
     * @return
     */
    public int getStatusCountFromCacheAfterTimestamp(Human aHuman, long aTimestamp)
    {
        String cache_name = "status_cache_"+this.getId()+"_"+aHuman.getId();

        DB cache_db = MongoUtil.getStatusCacheDB();

        DBCollection cache = cache_db.getCollection(cache_name);

        BasicDBObject query = new BasicDBObject("created", new BasicDBObject("$gt", aTimestamp));

        DBCursor cursor = cache.find(query);

        return cursor.size();

    }

    /**
     *
     * @param aHuman
     * @param aTimestamp milliseconds after that we're checking for status. this'll try to normalize to milliseconds in case you send seconds
     *                   if you send anything else, bad things..
     * @return
     */
    public JsonObject getStatusDetailsFromCacheAfterTimestamp(Human aHuman, long aTimestamp)
    {
        String cache_name = "status_cache_"+this.getId()+"_"+aHuman.getId();
        DB cache_db = MongoUtil.getStatusCacheDB();
        DBCollection cache = cache_db.getCollection(cache_name);
        BasicDBObject query = new BasicDBObject("created", new BasicDBObject("$gt", aTimestamp));
        DBCursor cursor = cache.find(query);
        int max = cursor.count();
        // List<DBObject> objects = cursor.toArray();

        return getStatusDetailsFromDBCursor(cursor, max);
    }


    public JsonObject getStatusDetailsFromCache(Human aHuman) {
        String cache_name = "status_cache_"+this.getId()+"_"+aHuman.getId();

        DB cache_db = MongoUtil.getStatusCacheDB();

        DBCollection cache = cache_db.getCollection(cache_name);
        DBCursor cursor = cache.find();
        int max = cursor.count();
        return getStatusDetailsFromCache(aHuman, max);
    }

    public JsonObject getStatusDetailsFromCache(Human aHuman, int aMaxIndex) {
        String cache_name = "status_cache_"+this.getId()+"_"+aHuman.getId();

        DB cache_db = MongoUtil.getStatusCacheDB();

        DBCollection cache = cache_db.getCollection(cache_name);

        DBObject query = new BasicDBObject("cache_header", new BasicDBObject("$exists", false));

        DBCursor cursor = cache.find(query);

        JsonObject period_details = this.getStatusDetailsFromDBCursor(cursor, aMaxIndex);
        period_details.addProperty("human", aHuman.toString());

        return period_details;
    }


//    protected JsonObject getStatusDetailsFromDBCursor(DBCursor cursor)
//    {
//        DBCursor noHeader = cursor.skip(1);
//
//        List<DBObject> objects = noHeader.toArray();
//        int _last = objects.size();
//        return getStatusDetailsFromDBCursor(cursor, _last);
//    }

    protected JsonObject getStatusDetailsFromDBCursor(DBCursor cursor, int aMaxIndex)
    {
        JsonObject result = new JsonObject();
        //result.addProperty("human", this.toString());
        int count = cursor.count();

        DBObject first = null;
        DBObject last = null;
        // DBCursor noHeader = cursor.skip(1);

        //List<DBObject> objects = noHeader.toArray();
        List<DBObject> objects = cursor.toArray();
        int _last = objects.size();
        if(aMaxIndex < objects.size()) {
            _last = aMaxIndex;
        }
        if(objects != null && objects.size() > 0) {
            first = objects.get(0);

            last = objects.get(_last -1);
        }

        Duration duration = null;
        Period period = null;
        long first_time = 0, last_time = 0;

        try {
            if(count > 0 && first != null && last != null) {
                Number first_n, last_n;
                first_n = (Number)first.get("created");
                first_time = first_n.longValue();

                last_n = (Number)last.get("created");
                last_time= last_n.longValue();

                // make sure we're getting milliseconds
                if(Math.log10(first_time) < 10) {
                    first_time *= 1000L;
                }

                if(Math.log10(last_time) < 10) {
                    last_time *= 1000L;
                }
//                if(first.get("created") instanceof Long) {
//                    first_time = (Long)first.get("created");
//                } else {
//                    first_time = ((Integer)first.get("created")).longValue()*1000L;
//                }
//                if(last.get("created") instanceof Long) {
//                    last_time = (Long)last.get("created");
//                } else {
//                    last_time = ((Integer)last.get("created")).longValue()*1000L;
//                }

                if(last_time == first_time) {
                    first_time = Instant.now().getMillis();
                }
                period = new Period(last_time, first_time);
                duration = new Duration(last_time, first_time);
                Period since = new Period(last_time, Instant.now().getMillis());

                result.addProperty("first.created", first_time);
                result.addProperty("last.created", last_time);

                result.addProperty("last.since.seconds", since.getSeconds());
                result.addProperty("last.since.minutes", since.getMinutes());
                result.addProperty("last.since.hours", since.getHours());
                result.addProperty("last.since.days", since.getDays());
                result.addProperty("last.since.weeks", since.getWeeks());
                result.addProperty("last.since.months", since.getMonths());
                result.addProperty("last.since.years", since.getYears());

                result.addProperty("period.seconds", period.getSeconds());
                result.addProperty("period.minutes", period.getMinutes());
                result.addProperty("period.hours", period.getHours());
                result.addProperty("period.days", period.getDays());
                result.addProperty("period.weeks", period.getWeeks());
                result.addProperty("period.months", period.getMonths());
                result.addProperty("period.years", period.getYears());
                result.addProperty("period", period.toString());

                // the duration betwix last_time and first_time should be roughly the same
                // as the period - so duration.hours should equal period.days * 24 + period.hours
                result.addProperty("duration.hours", duration.getStandardHours());
                result.addProperty("duration.days", duration.getStandardDays());
                result.addProperty("duration.minutes", duration.getStandardMinutes());
                result.addProperty("duration.seconds", duration.getStandardSeconds());
                result.addProperty("duration.khz", duration.getStandardSeconds()*1000L);
                result.addProperty("duration.mhz", duration.getStandardSeconds()*1000000L);
                //result.addProperty("time-span",)
            }
            //logger.warn("weird first="+first+" last="+last+" with "+this);

            Hashtable<String, Integer> service_counts = new Hashtable<String, Integer>();
            JsonObject counts = new JsonObject();
            int x = 0;
            int total = 0;
            //for(DBObject object : objects) {
            for(int i=0; i<_last; i++) {
                DBObject object = objects.get(i);
                if(object.containsField("service")) {
                    total++;
                    String service_name = object.get("service").toString();
                    if(service_counts.containsKey(service_name)) {
                        Integer countInteger = (Integer) service_counts.get(service_name);
                        x = countInteger.intValue();
                        x++;
                        service_counts.put(service_name, Integer.valueOf(x));
                    } else {
                        x = 1;
                        service_counts.put(service_name, Integer.valueOf(x));

                    }
                    counts.addProperty(service_name, Integer.valueOf(x));
                }
            }

            result.addProperty("service.count.total", total);

            if(service_counts.isEmpty() == false) {
                Iterator e = (Iterator) service_counts.keys();
                while(e.hasNext()) {
                    String s = (String)e.next();
                    Integer c = (Integer)service_counts.get(s);
                    result.addProperty("service."+s+".count", c);
                    if(duration != null) {
                        result.addProperty("service."+s+".khzp",  duration.getStandardSeconds()*1000L/(float)c.floatValue() );
                        result.addProperty("service."+s+".mhzp", duration.getStandardSeconds()*1000000L/(float)c.floatValue());
                        if(duration.getStandardHours() > 0) {
                            result.addProperty("service."+s+".pph", (float)c.floatValue() / duration.getStandardHours());
                        }
                        if(duration.getStandardDays() > 0) {
                            result.addProperty("service."+s+".ppd", (float)c.floatValue() / duration.getStandardDays());

                        }
                        result.addProperty("service."+s+".hzp", duration.getStandardSeconds()/(float)c.floatValue() );
                        if(period != null && period.getWeeks() > 0) {
                            result.addProperty("service."+s+".ppw", (float)c.floatValue() / period.getWeeks());
                        }
                    }
                }
            }
//            result.addProperty("service."+service_name)

        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(e);
            logger.error("Something's weird with the cache called " + cursor.getCollection() + ". Will drop. Maybe that'll fix it?");
            DBCollection collection = cursor.getCollection();
            collection.drop();

            result.addProperty("ex-message", e.toString());
        }
        return result;
    }


    public int getStatusCountFromCache(Human aHuman)
    {

        String cache_name = "status_cache_"+this.getId()+"_"+aHuman.getId();

        DB cache_db = MongoUtil.getStatusCacheDB();

        DBCollection cache = cache_db.getCollection(cache_name);

        DBObject query = new BasicDBObject("cache_header", new BasicDBObject("$exists", false));
        long result = cache.getCount(query);
        ///DBCursor cursor = cache.find(query);
        ///int count = cursor.count();

//        DBObject first = null;
//        DBObject last = null;
//        DBCursor noHeader = cache.find(query);//cursor.skip(1);
//
//        List<DBObject> objects = noHeader.toArray();
        return (int) result;
    }

    /**
     * Returns whatever may be cached for this Human, otherwise, if the cache doesn't exist
     * or the cache is empty, it returns an empty result.
     *
     * @param aHuman
     * @param aPage
     * @return JsonArray Returns whatever may be cached for this Human, otherwise, if the cache doesn't exist
     * or the cache is empty, it returns an empty result.
     */
    protected JsonArray getJsonStatusFromCache(Human aHuman, int aPage) {
        JsonArray result_array = new JsonArray();
        JsonParser parser = new JsonParser();
        String cache_name = "status_cache_"+this.getId()+"_"+aHuman.getId();

        DB cache_db = MongoUtil.getStatusCacheDB();

        DBCollection cache = cache_db.getCollection(cache_name);
        //DBCursor cursor = cache.find(  );
        DBCursor cursor;

        if(aPage > -1) {
            cursor = cache.find().skip(aPage*Constants.getInt("STATUS_CHUNK_SIZE", 25)).limit(Constants.getInt("STATUS_CHUNK_SIZE", 25));
        } else {
            cursor = cache.find(  );
        }
        //logger.debug("cache="+cache);

        while (cursor.hasNext() ) {
            DBObject obj = cursor.next();
            // the document that contains this 'key' field is the
            // key to the collection. we don't need it for status
            // it's used purely as metadata about the documents themselves.
            if (obj.containsField("cache_header")) {
                continue;
            }

            JsonElement elem = parser.parse(obj.toString());
            result_array.add(elem);
        }
        return result_array;
    }


    /**
     * Useful time stats good for display
     * @param aHuman
     * @return time in ms of the most recent status we have for a specific Human
     */
    protected long getTimeOfMostRecentStatusForHuman(Human aHuman)
    {
        List<ServiceStatus> status = new ArrayList<ServiceStatus>();
        List<ServiceUser> service_users = aHuman.getServiceUsers();
        for(ServiceUser service_user : service_users) {
            String service_name = service_user.getServiceName();

            try {
                if(service_name.equalsIgnoreCase("twitter")) {

                    TwitterService twitter = TwitterService.createTwitterServiceOnBehalfOfUsername(service_user.getOnBehalfOfUsername());
                    status.add(twitter.getMostRecentStatus());
                }
                if(service_name.equalsIgnoreCase("foursquare")) {

                    FoursquareService foursquare = FoursquareService.createFoursquareServiceOnBehalfOfUserID(service_user.getOnBehalfOfUserId());
                    status.add(foursquare.getMostRecentCheckin());
                }
                if(service_name.equalsIgnoreCase("flickr")) {

                    FlickrService flickr = FlickrService.createFlickrServiceOnBehalfOfUserID(service_user.getOnBehalfOfUserId());
                    status.add(flickr.getMostRecentStatus());
                }
                if(service_name.equalsIgnoreCase("instagram")) {

                    InstagramService instagram = InstagramService.createServiceOnBehalfOfUsername(service_user.getOnBehalfOfUsername());
                    status.add(instagram.getMostRecentStatus());
                }
            } catch (BadAccessTokenException e) {
                logger.warn(e);
            }
        }
        Collections.sort(status);
        if(status.size() > 0) {
            return status.get(0).getCreated();
        } else {
            logger.warn("No status to sort for most recent for "+this);
            return 0;
        }
    }

    /**
     * Useful time stats good for display
     * @param aHuman
     * @return time in ms of the oldest status we have for a specific Human
     */
    protected long getTimeOfOldestStatusForHuman(Human aHuman)
    {
        List<ServiceStatus> status = new ArrayList<ServiceStatus>();
        List<ServiceUser> service_users = aHuman.getServiceUsers();
        for(ServiceUser service_user : service_users) {
            String service_name = service_user.getServiceName();

            try {
                if(service_name.equalsIgnoreCase("twitter")) {

                    TwitterService twitter = TwitterService.createTwitterServiceOnBehalfOfUsername(service_user.getOnBehalfOfUsername());
                    status.add(twitter.getOldestStatus());
                }
                if(service_name.equalsIgnoreCase("foursquare")) {

                    FoursquareService foursquare = FoursquareService.createFoursquareServiceOnBehalfOfUserID(service_user.getOnBehalfOfUserId());
                    status.add(foursquare.getOldestCheckin());
                }
                if(service_name.equalsIgnoreCase("flickr")) {

                    FlickrService flickr = FlickrService.createFlickrServiceOnBehalfOfUserID(service_user.getOnBehalfOfUserId());
                    status.add(flickr.getOldestStatus());
                }
                if(service_name.equalsIgnoreCase("instagram")) {

                    InstagramService instagram = InstagramService.createServiceOnBehalfOfUsername(service_user.getOnBehalfOfUsername());
                    status.add(instagram.getOldestStatus());
                }
            } catch (BadAccessTokenException e) {
                logger.warn(e);
            }
        }
        Collections.sort(status);
        if(status.size() > 0) {
            return status.get(status.size()-1).getCreated();
        } else {
            logger.warn("No status to sort for oldest for "+this);
            return 0;
        }

    }


    /**
     * Useful time stats good for display
     * @param aHuman
     * @return time in ms of the oldest status IN THE CACHE ready to go we have for a specific Human
     */
    public long getTimeOfOldestCachedStatusForHuman(Human aHuman) {
        Integer result = new Integer(0);
        Object foo;
        JsonArray result_array = new JsonArray();
        JsonParser parser = new JsonParser();
        String cache_name = "status_cache_"+this.getId()+"_"+aHuman.getId();

        DB cache_db = MongoUtil.getStatusCacheDB();

        DBCollection cache = cache_db.getCollection(cache_name);
        //DBCursor cursor = cache.find(  );
        DBCursor cursor;
        DBObject obj = new BasicDBObject();
        obj.put( "created", 1 );
        cursor = cache.find().sort(obj);//.limit(1);
        DBObject key = null;
        while(cursor.hasNext()) {
            key = cursor.next();
            if(key.containsField("created")) {
                result = (Integer)key.get("created");
                break;
            }
        }

        return result.longValue()*1000;
    }

    /**
     * Useful time stats good for display
     * @param aHuman
     * @return time in ms of the most recent status IN THE CACHE ready to go we have for a specific Human
     */

    public long getTimeOfMostRecentCachedStatusForHuman(Human aHuman) {
        Integer result = new Integer(0);
        Object foo;
        JsonArray result_array = new JsonArray();
        JsonParser parser = new JsonParser();
        String cache_name = "status_cache_"+this.getId()+"_"+aHuman.getId();

        DB cache_db = MongoUtil.getStatusCacheDB();

        DBCollection cache = cache_db.getCollection(cache_name);
        //DBCursor cursor = cache.find(  );
        DBCursor cursor;
        DBObject obj = new BasicDBObject();
        obj.put( "created", -1 );
        cursor = cache.find().sort(obj);//.limit(1);
        DBObject key = null;
        while(cursor.hasNext()) {
            key = cursor.next();
            if(key.containsField("created")) {
                result = (Integer)key.get("created");
                break;
            }
        }

        return result.longValue()*1000;
    }

/*
    public long getTimeOfNewestCachedStatusForHuman(Human aHuman) {
        return 0;

    }
*/


    protected int getStatusCountForHuman(Human aHuman)
    {
        int count = 0;
        List<ServiceUser> service_users = aHuman.getServiceUsers();
        for(ServiceUser service_user : service_users) {
            String service_name = service_user.getServiceName();

            try {
                if(service_name.equalsIgnoreCase("twitter")) {

                    TwitterService twitter = TwitterService.createTwitterServiceOnBehalfOfUsername(service_user.getOnBehalfOfUsername());
                    count += twitter.getStatusCountForUserID(service_user.getUserID());
                }
                if(service_name.equalsIgnoreCase("foursquare")) {

                    FoursquareService foursquare = FoursquareService.createFoursquareServiceOnBehalfOfUserID(service_user.getOnBehalfOfUserId());
                    count += foursquare.getStatusCountForUserID(service_user.getUserID());
                }
                if(service_name.equalsIgnoreCase("flickr")) {

                    FlickrService flickr = FlickrService.createFlickrServiceOnBehalfOfUserID(service_user.getOnBehalfOfUserId());
                    count += flickr.getStatusCountForUserID(service_user.getUserID());
                }
                if(service_name.equalsIgnoreCase("instagram")) {

                    InstagramService instagram = InstagramService.createServiceOnBehalfOfUsername(service_user.getOnBehalfOfUsername());
                    count += instagram.getStatusCountForUserID(service_user.getUserID());
                }
            } catch (BadAccessTokenException e) {
                logger.warn(e);
            }
        }
        return count;
    }

    /**
     * Just refresh status, don't return a list of it. For updating the database with latest status, basically.
     * @param aHuman
     */
    public void serviceRefreshStatusForHuman(Human aHuman)
    {
        // TODO whilst doing this may as well freshen the avatar imageURLs
        // sometimes they are stale (Twitter, which doesn't fix the URLs but wipes them out if the user changes them)
        // and sometimes they are missing all together if the Human was created in a wierd way, specifically on the
        // client which can create a Human without knowing the user's imageURL, such as when we create a Youman.
        
        // if this particular HumansUser has foursquare accounts we may as well
        // get their service ID and get latest checkins
        List<ServiceEntry> serviceEntries = getServicesForServiceName("foursquare");
        for(ServiceEntry service : serviceEntries) {

            try {
                FoursquareService foursquare = FoursquareService.createFoursquareServiceOnBehalfOfUserID(service.getServiceUserID());
                if(foursquare.localServiceStatusIsFresh() == false) {
                    foursquare.serviceRequestLatestCheckins();
                }
            } catch (BadAccessTokenException e) {
                logger.warn(e);
            } catch (NullPointerException e) {
                logger.error("huh.", e);
                //logger.info("service_user="+service_user);
                logger.error("humans_user="+this.getUsername());
            }

        }

        List<ServiceUser> service_users = aHuman.getServiceUsers();

        for(ServiceUser service_user : service_users) {
            logger.info("Human="+ aHuman.getName() +":"+this.getUsername()+" / refreshing status for " + service_user.getUsername()+" @ "+service_user.getServiceName() + " "+service_user.getUserID());
            if(service_user.getServiceName() == null) {
                logger.warn("Bad thing. While refreshing service status found a null serviceName in serviceUser. Skipping. human="+aHuman.getName()+" service_user="+service_user);

                continue;
            }
            String service_name = service_user.getServiceName();
            if(service_name.equalsIgnoreCase("twitter")) {
                try {
                    TwitterService twitter = TwitterService.createTwitterServiceOnBehalfOfUsername(service_user.getOnBehalfOfUsername());
                    if(twitter.localServiceStatusIsFreshFor(service_user.getUserID()) == false) {
                        twitter.serviceRequestStatusForUserID(service_user.getUserID());



                        twitter.serviceRequestUserBasicForUserID(service_user.getUserID());
                    }
                } catch (BadAccessTokenException e) {
                    logger.warn(e);
                } catch (NullPointerException e) {
                    logger.error("huh. this may happen. it happened i think when a user on a service was deleted on the service.", e);
                    logger.error("service_user="+service_user);
                    logger.error("humans_user="+this.getUsername());
                }
            }
            if(service_name.equalsIgnoreCase("instagram")) {
                try {
                    //logger.debug(service_user.getServiceUserID()+" "+service_user.getUsername());
                    InstagramService instagram = InstagramService.createServiceOnBehalfOfUsername(service_user.getOnBehalfOfUsername());
                    if(instagram.localServiceStatusIsFreshForUserID(service_user.getUserID()) == false) {
                        instagram.serviceRequestStatusForUserID(service_user.getUserID());

                        instagram.serviceRequestStatusForUserIDFromMonthsAgo(service_user.getUserID(), 12);


                    }
                } catch (BadAccessTokenException e) {
                    logger.warn(e);
                } catch (NullPointerException e) {
                    logger.error("huh. this may happen. it happened i think when a user on a service was deleted on the service.", e);
                    logger.error("service_user="+service_user);
                    logger.error("humans_user="+this.getUsername());
                }

            }
            if(service_name.equalsIgnoreCase("flickr")) {
                try {
                    FlickrService flickr = FlickrService.createFlickrServiceOnBehalfOfUserID(service_user.getOnBehalfOfUserId());
                    if(flickr.localServiceStatusIsFreshForUserID(service_user.getUserID()) == false) {
                        flickr.serviceRequestStatusForUserID(service_user.getUserID());

                        flickr.serviceRequestStatusForUserIDAfterMonthsAgo(service_user.getUserID(), 12);
                    }
                } catch (BadAccessTokenException e) {
                    logger.warn(e);
                } catch (NullPointerException e) {
                    logger.error("huh. this may happen. it happened i think when a user on a service was deleted on the service.", e);
                    logger.error("service_user="+service_user);
                    logger.error("humans_user="+this.getUsername());
                }
            }
        }

    }

    /**
     * This will gather the status for a Human ignorning the cache, if it exists,
     * and going to the services themselves if the status is stale.
     * @param aHuman
     * @param loadIfStale
     * @return
     */
    protected List<ServiceStatus> getStatusForHuman(Human aHuman, boolean loadIfStale) {
        List<ServiceStatus> result = new ArrayList<ServiceStatus>();

        List<ServiceUser> service_users = aHuman.getServiceUsers();
        for(ServiceUser service_user : service_users) {
            String service_name = service_user.getServiceName();
            logger.info("Gathering Status for " + service_user + " loadIfStale=" + (loadIfStale ? "YES" : "NO"));
            if(service_name == null) {
                logger.warn("Empty/null service_name. Migration issue. Fix if you want, but it's on a case-by-case basis. human="+aHuman+" service_user="+service_user);
                logger.warn("In the meantime, this human will not have any status refreshes cause we don't know what service it is for.");
                logger.warn("username="+this.getUsername()+" service_user="+service_user+" is not going to get a service update.");
                continue;
            }
            if(service_name.equalsIgnoreCase("twitter")) {
                try {
                    TwitterService twitter = TwitterService.createTwitterServiceOnBehalfOfUsername(service_user.getOnBehalfOfUsername());
                    if(loadIfStale && twitter.localServiceStatusIsFreshFor(service_user.getUserID()) == false) {
                        List<TwitterStatus> status = twitter.serviceRequestStatusForUserID(service_user.getUserID());
                        result.addAll(status);

                    } else {
                        result.addAll(twitter.getStatusForUserID(service_user.getUserID()));
                    }
                } catch (BadAccessTokenException e) {
                    logger.warn(e);
                }
            }
            if(service_name.equalsIgnoreCase("instagram")) {
                try {
                    InstagramService instagram = InstagramService.createServiceOnBehalfOfUsername(service_user.getOnBehalfOfUsername());
                    if(loadIfStale && instagram.localServiceStatusIsFreshForUserID(service_user.getUserID()) == false) {
                        result.addAll(instagram.serviceRequestStatusForUserID(service_user.getUserID()));
                    } else {
                        result.addAll(instagram.getStatusForUserID(service_user.getUserID()));
                    }
                } catch (BadAccessTokenException e) {
                    logger.warn(e);
                    //e.printStackTrace();
                }

            }
            if(service_name.equalsIgnoreCase("flickr")) {
                try {
                    FlickrService flickr = FlickrService.createFlickrServiceOnBehalfOfUserID(service_user.getOnBehalfOfUserId());

                    //FlickrUser user = flickr.getURLForBuddyIconForUser();

                    if(loadIfStale && flickr.localServiceStatusIsFreshForUserID(service_user.getUserID()) == false) {
                        result.addAll(flickr.serviceRequestStatusForUserID(service_user.getUserID()));
                    } else {
                        result.addAll(flickr.getStatusForUserID(service_user.getUserID()));
                    }
                } catch (BadAccessTokenException e) {
                    logger.warn(e);
                    //e.printStackTrace();
                }
            }
            if(service_name.equalsIgnoreCase("foursquare")) {
                try {
                    FoursquareService foursquare = FoursquareService.createFoursquareServiceOnBehalfOfUserID(service_user.getOnBehalfOfUserId());
                    // note that we'll only get checkins for the authenticated user onBehalfOf..
                    // foursquare doesn't allow me to get someone else's checkins..
                    if(loadIfStale && foursquare.localServiceStatusIsFresh() == false) {
                        foursquare.serviceRequestCheckins();
                    }
                    result.addAll(foursquare.getCheckinsForUserID(service_user.getUserID()));
                } catch (BadAccessTokenException e) {
                    logger.warn(e);
                    //e.printStackTrace();
                }
            }

        }

        Collections.sort(result);
        // HEY
        if(result != null && result.size() > 0) {
            // we do this explicitly via HumansUser.refreshCache() while doing our ScheduledStatusFetch don't assume we want to cache just cause
            // we've gathered status
            // cacheStatusForHuman(aHuman, result);
        } else {
            logger.warn("status for "+aHuman.getName()+"/"+aHuman.getId()+" is "+result);
        }

        return result;
    }

    /**
     * Refactor this to just do updates or upserts
     * Update the lastUpdated field but otherwise?
     * Thing is — if the human changes configuration (you add/remove a person) then the status should change as
     * well and if you don't delete it from the cache, you'll get bad elements in the cache...
     * Sigh..
     * @param aHuman
     * @param aListOfStatus
     */
    public void cacheStatusForHuman(Human aHuman, List<ServiceStatus> aListOfStatus) {
        DB cache_db = MongoUtil.getStatusCacheDB();
        String cache_final_name = "status_cache_"+this.getId()+"_"+aHuman.getId();
        String cache_temp_name = "tmp_"+cache_final_name;
        DBCollection cache = cache_db.getCollection(cache_temp_name);
        logger.info("writing cache, will rename and drop in a bit also " + cache);

        cache.drop();
        cache = cache_db.getCollection(cache_temp_name);

        // sort so that the newest items are at lower indices
        Collections.sort(aListOfStatus, new Comparator<ServiceStatus>()
        {
            public int compare(ServiceStatus o1, ServiceStatus o2)
            {
                //				JsonObject j1 = (JsonObject)o1;
                //				JsonObject j2 = (JsonObject)o2;
                long u1 = o1.getCreated();
                long u2 = o2.getCreated();
                return u1>u2 ? -1 : (u1==u2 ? 0 : 1);
            }
        });

        //TODO limit the size of the write to the cache to keep things snappy?
        int max_cache_document_count = Constants.getInt("MAX_CACHE_DOCUMENT_COUNT", 250);
        if(aListOfStatus.size() > max_cache_document_count) {
            aListOfStatus = aListOfStatus.subList(0, max_cache_document_count);
        }

        Date oldest = aListOfStatus.get(aListOfStatus.size()-1).getCreatedDate(); //oldestStatusInList(aListOfStatus);
        Date newest = aListOfStatus.get(0).getCreatedDate();

        // the document that contains this 'key' field is the
        // key to the collection. we don't need it for status
        // it's used purely as metadata about the documents themselves.
        for (ServiceStatus status : aListOfStatus) {
            try {
                DBObject obj = (DBObject) JSON.parse(status.getStatusJSON().toString());
                cache.save(obj);
            } catch (Error error) {
                logger.error(error);
                logger.error("status=" + status + " json=" + status.getStatusJSON());
            }
        }

        BasicDBObject meta = new BasicDBObject
                ("user", this.toString()).
                append("key", this.getId() + "_" + aHuman.getId()).
                append("human", aHuman.toString()).
                append("newest", newest).
                append("oldest", oldest).
                append("created", new Date().getTime()).
                append("cache_header", true).
                append("lastUpdated", new Date());


        cache.insert(meta);


//        logger.error("TEST TEST TEST TEST");
//        logger.warn("FOO FOO FOO FOO");
        logger.info("wrote "+aListOfStatus.size()+" items");
        logger.info("oldest-in-cache-doc "+ oldest);
        logger.info("newest-in-cache-doc "+ newest);
        logger.info("renaming cache now to "+cache_final_name);
        cache.rename(cache_final_name, true);
        logger.info("writing cache is done");
    }

    protected Date newestStatusInList(List<ServiceStatus> aListOfStatus)
    {
        // sort
        Collections.sort(aListOfStatus, new Comparator<ServiceStatus>()
        {
            public int compare(ServiceStatus o1, ServiceStatus o2)
            {
                //				JsonObject j1 = (JsonObject)o1;
                //				JsonObject j2 = (JsonObject)o2;
                long u1 = o1.getCreated();
                long u2 = o2.getCreated();
                return u1<u2 ? -1 : (u1==u2 ? 0 : 1);
            }
        });

        return aListOfStatus.get(aListOfStatus.size()-1).getCreatedDate();

    }

    protected Date oldestStatusInList(List<ServiceStatus> aListOfStatus)
    {
        // sort
        Collections.sort(aListOfStatus, new Comparator<ServiceStatus>()
        {
            public int compare(ServiceStatus o1, ServiceStatus o2)
            {
                //				JsonObject j1 = (JsonObject)o1;
                //				JsonObject j2 = (JsonObject)o2;
                long u1 = o1.getCreated();
                long u2 = o2.getCreated();
                return u1>u2 ? -1 : (u1==u2 ? 0 : 1);
            }
        });

        return aListOfStatus.get(aListOfStatus.size()-1).getCreatedDate();
    }

    protected List<String> getServiceNamesAssigned() {
        List<String> result = new ArrayList<String>();
        //List<ServiceEntry> services = this.getServices();

        for(ServiceEntry service_entry : getServices()) {
            //logger.debug(service.toString());
            result.add(service_entry.getServiceName());
        }
        return result;

    }

    /**
     *
     * @param aServiceName
     *            typically lowercase name of a service, eg twitter, instagram,
     *            flickr, foursquare
     * @return a List<String> of the accounts for that service assigned/attached
     *         on behalf of this humans user
     */
    public List<ServiceUser> getServiceUsersForServiceName(String aServiceName) {
        List<ServiceUser>result = new ArrayList<ServiceUser>();
        //List<String> aListResult = new ArrayList<String>();
        if (aServiceName != null) {
            //aServiceName = aServiceName.toLowerCase();
            List<ServiceUser> serviceUsers = this.getServiceUsersForAllHumans();
            for(ServiceUser serviceUser : serviceUsers) {
                if(serviceUser.serviceName != null && serviceUser.serviceName.equalsIgnoreCase(aServiceName)) {
                    if(serviceUser.getOnBehalfOfUsername() == null) {
                        logger.warn("WTF? "+this+" The username for serviceUser.getOnBehalfOf is null: "+serviceUser);
                    }
                    result.add(serviceUser);
                }
            }
        }
        return result;
    }

//    public void removeServiceUser(ServiceUser aServiceUser) {
//        List<Human> allHumans = this.getAllHumans();
//        for(Human human : allHumans) {
//            human.removeServiceUser(aServiceUser);
//        }
//    }


    public List<ServiceUser> getServiceUsersForAllHumans() {
        List<ServiceUser> result = new ArrayList<ServiceUser>();
        List<Human> allHumans = this.getAllHumans();
        for(Human human : allHumans) {
            for(ServiceUser serviceUser : human.getServiceUsers()) {
                result.add(serviceUser);
            }
        }
        return result;
    }



    public boolean addService(ServiceEntry aService) {
        boolean result = false;

        if(services.contains(aService)) {
            logger.debug(this+" already contains "+aService);
            result = false;
        } else {
            result = services.add(aService);
        }
        return result;
    }

    /**
     *
     * @param aServiceEntry
     * @return whether it succeeded
     */
    public boolean removeService(ServiceEntry aServiceEntry) {
        return removeService(aServiceEntry.serviceUserID, aServiceEntry.getServiceUsername(), aServiceEntry.getServiceName());
    }

    /**
     *
     * @param aServiceUserID
     * @param aServiceUsername
     * @param aServiceName
     * @return whether it succeded
     */
    protected boolean removeService(String aServiceUserID, String aServiceUsername, String aServiceName) {
        ServiceEntry service_entry = new ServiceEntry(aServiceUserID, aServiceUsername, aServiceName);
        boolean result = services.remove(service_entry);
        //logger.debug("removing service entry "+service_entry);
        logger.debug("Removing service_entry "+service_entry.hashCode()+" from "+this+" result="+result);
        if(result) {
            // delete friends
            // go through all thus users humans?
            removeServiceUsersRelyingOn(service_entry);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public boolean removeServiceBy(String aServiceName, String aServiceUserID)
    {
        boolean result = false;
        ServiceEntry service_entry = selectFirst(services,
                and(having(on(ServiceEntry.class).getServiceName(), equalTo(aServiceName)),
                        having(on(ServiceEntry.class).getServiceUserID(), equalTo(aServiceUserID))));
        result = services.remove(service_entry);
        if(result) {
            removeServiceUsersRelyingOn(service_entry);
        }


        return result;
    }



    /**
     *
     * @param aServiceUserID
     * @param aServiceUsername
     * @param aServiceTypeName
     */
    public void addService(String aServiceUserID, String aServiceUsername, String aServiceTypeName) {
        ServiceEntry service_entry = new ServiceEntry(aServiceUserID, aServiceUsername, aServiceTypeName);
        if(aServiceUserID == null || aServiceUsername == null || aServiceTypeName == null) {
            logger.warn("While adding a service_entry, got an empty value for one of userid("+aServiceUserID+"), username("+aServiceUsername+") or service("+aServiceTypeName+") when attempting to add a service_entry "+this);
            return;
        }
        // check for dupes?
        if(services.contains(service_entry)) {
            services.remove(service_entry);
        }
        services.add(service_entry);
    }

    public List<ServiceUser> getServiceUsersRelyingOn(ServiceEntry onBehalfOf)
    {
        List<ServiceUser> results = new ArrayList<ServiceUser>();
        for(Human human : getAllHumans()) {
            results.addAll( human.getServiceUsersRelyingOn(onBehalfOf) );
        }
        return results;
    }

    /**
     * This you'll want to call when you remove a service because the "onBehalfOf" component will no longer exist
     *
     * @param onBehalfOf
     */
    protected void removeServiceUsersRelyingOn(ServiceEntry onBehalfOf) {
        if(onBehalfOf == null) {
            return;
        }
        logger.debug("removeServiceUsersRelyingOn ", onBehalfOf);
        List<ServiceUser> services_to_remove = new ArrayList<ServiceUser>();

        List<Human> humans = this.getAllHumans();
        for(Human human : humans) {
            boolean result = human.removeServiceUsersByServiceEntry(onBehalfOf);
            if(result) {
                this.removeCacheForHuman(human);
            }
        }

        Iterator<Human> iter = this.getAllHumans().iterator();
        while(iter.hasNext()) {
            Human human = iter.next();
            if(human.getServiceUsers().size() < 1) {
                iter.remove();
            }
        }
    }

    public List<ServiceEntry> getServicesForServiceName(String aServiceName) {
        List<ServiceEntry> result = select(this.getServices(), having(on(ServiceEntry.class).getServiceName(), equalTo(aServiceName)));

        return result;
    }


    public ObjectId getId() {
        return id;
    }
    public void setId(ObjectId aId) {
        id = aId;
    }
    public List<ServiceEntry> getServices() {
        return services;
    }

    public void save() {
        HumansUserDAO dao = new HumansUserDAO();
        dao.save(this);
    }

    /**
     * Useful for debugging on a test database
     * @param dao
     */
    public void save(HumansUserDAO dao) {
        dao.save(this);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        result = prime * result + ((humans == null) ? 0 : humans.hashCode());
        result = prime * result
                + ((password == null) ? 0 : password.hashCode());
        result = prime * result
                + ((services == null) ? 0 : services.hashCode());
        result = prime * result
                + ((username == null) ? 0 : username.hashCode());
        return result;
    }



    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HumansUser other = (HumansUser) obj;
        if (email == null) {
            if (other.email != null)
                return false;
        } else if (!email.equals(other.email))
            return false;
        if (humans == null) {
            if (other.humans != null)
                return false;
        } else if (!humans.equals(other.humans))
            return false;
        if (password == null) {
            if (other.password != null)
                return false;
        } else if (!password.equals(other.password))
            return false;
        if (services == null) {
            if (other.services != null)
                return false;
        } else if (!services.equals(other.services))
            return false;
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username))
            return false;
        return true;
    }



    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "HumansUser [username=" + username + ", password=******* email=" + email + ", humans=" + humans + ", services="
                + services + "]";
    }


}

class StatusCacheJsonExclusionStrategy implements ExclusionStrategy
{

    @Override
    public boolean shouldSkipField(FieldAttributes aField) {
        if ( (aField.getName().equals("_id"))) {
            return true;
        } else {
            return false;
        }


    }

    @Override
    public boolean shouldSkipClass(Class<?> aClazz) {
        if(aClazz.equals(ObjectId.class)) {
            return true;
        } else {
            return false;
        }
    }


}
