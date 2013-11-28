package com.nearfuturelaboratory.humans.entities;

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.select;
import static ch.lambdaj.Lambda.selectFirst;
import static ch.lambdaj.Lambda.selectUnique;
import static ch.lambdaj.function.matcher.AndMatcher.and;
import static org.hamcrest.CoreMatchers.equalTo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.utils.IndexDirection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nearfuturelaboratory.humans.core.MinimalSocialServiceUser;
import com.nearfuturelaboratory.humans.dao.HumansUserDAO;
import com.nearfuturelaboratory.humans.exception.BadAccessTokenException;
import com.nearfuturelaboratory.humans.flickr.entities.FlickrFriend;
import com.nearfuturelaboratory.humans.foursquare.entities.FoursquareFriend;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramFriend;
import com.nearfuturelaboratory.humans.service.FlickrService;
import com.nearfuturelaboratory.humans.service.FoursquareService;
import com.nearfuturelaboratory.humans.service.InstagramService;
import com.nearfuturelaboratory.humans.service.TwitterService;
import com.nearfuturelaboratory.humans.service.status.ServiceStatus;
import com.nearfuturelaboratory.humans.twitter.entities.TwitterFriend;
import com.nearfuturelaboratory.humans.twitter.entities.TwitterStatus;
import com.nearfuturelaboratory.humans.util.MyObjectIdSerializer;


@Entity(value="users",noClassnameStored = true)
public class HumansUser extends BaseEntity {

	final static Logger logger = Logger.getLogger(com.nearfuturelaboratory.humans.entities.HumansUser.class);

	@Indexed(value = IndexDirection.ASC, name = "username", unique = true, dropDups = true)
	private String username;
	private String password;
	protected String email;

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


	//	public static HumansUser(String aUsername, String aPassword) {
	//		setUsername(aUsername);
	//		setPassword(aPassword);
	//	}

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

		for(Human human : getHumans()) {
			service_user = human.getServiceUserById(aServiceUserId);
			if(service_user != null) {
				container = human;
				result = container.removeServiceUserById(aServiceUserId);
				break;
			} else {
				continue;
			}
		}
		return result;
		//		ServiceUser service_user = selectFirst(
		//											flatten(
		//													extract(this.getHumans(), on(Human.class).getServiceUsers())),
		//                having(on(ServiceUser.class).getId(), equalTo(new ObjectId(aServiceUserId))));
		//		
		//		return result;
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

	public boolean addServiceUserToHuman(ServiceUser aServiceUser, String aHumanId) {
		boolean result = false;
		Human human = getHumanByID(aHumanId);
		if(human != null) {
			result = human.addServiceUser(aServiceUser);
		}
		return result;
	}

	public boolean addService(ServiceEntry aService) {
		boolean result = false;

		if(services.contains(aService)) {
			logger.info(this+" already contains "+aService);
			result = false;
		} else {
			result = services.add(aService);
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


	public List<ServiceUser> getServiceUsersForAllHumansByService(String aService) {
		List<ServiceUser> result = new ArrayList<ServiceUser>();
		List<ServiceUser> all = getServiceUsersForAllHumans();
		for(int i=0; i<all.size(); i++) {
			ServiceUser su = all.get(i);
			if(su.getService().equalsIgnoreCase(aService)) {
				result.add(su);
			}
		}
		return result;

	}


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
			obj.addProperty("serviceID", friend.getUserID());
			obj.addProperty("username", friend.getUsername());
			obj.addProperty("service", friend.getServiceName());
			obj.addProperty("imageURL", friend.getImageURL());
			obj.addProperty("largeImageURL", friend.getLargeImageURL());

			OnBehalfOf o = new OnBehalfOf();
			o.setServiceUserID(friend.getOnBehalfOf().getServiceUserID());
			o.setServiceUsername(friend.getOnBehalfOf().getServiceUsername());
			obj.add("onBehalfOf", new JsonParser().parse(gson.toJson(o)));


			//			obj.addProperty("on_behalf_of_username", friend.getOnBehalfOf().getServiceUsername());
			//			obj.addProperty("on_behalf_of_userid", friend.getOnBehalfOf().getServiceUserID());
			//obj.a ("on_behalf_of", friend.getOnBehalfOf());
			//friend_json. (gson.toJson(obj));
			//logger.debug(gson.toJson(obj));
			f.add(obj);

		}

		//		List<JsonObject> byUsername = sort(f, on(JsonObject.class).get("user_id"));

		Collections.sort(f, new Comparator<JsonObject>()
				{
			public int compare(JsonObject o1, JsonObject o2)
			{
				//				JsonObject j1 = (JsonObject)o1;
				//				JsonObject j2 = (JsonObject)o2;
				String u1 = o1.get("username").toString();
				String u2 = o2.get("username").toString();
				int v = u1.compareTo(u2);
				return v;
			}
				});
		for(JsonObject o : f) {
			result.add(o);
		}
		return result;
	}



	public List<MinimalSocialServiceUser> getFriends() {
		List<MinimalSocialServiceUser> friends = new ArrayList<MinimalSocialServiceUser>();
		for(ServiceEntry service_entry : getServices()) {
			//logger.debug(service_entry);
			if(service_entry.getServiceName().equalsIgnoreCase("flickr")) {
				FlickrService flickr;
				try {
					flickr = FlickrService.createFlickrServiceOnBehalfOfUserID(service_entry.getServiceUserID());
					for(FlickrFriend f : flickr.getFriends()) {
						//f.setOnBehalfOf(service_entry);
						friends.add(f);
					}
					//friends.addAll(flickr.getFriends());
				} catch (BadAccessTokenException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.error("",e);
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
					//logger.debug("INSTAGRAM "+instagram.getFollows());

					//friends.addAll(instagram.getFollows());

				} catch (BadAccessTokenException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.error("",e);
				}
			}
			if(service_entry.getServiceName().equalsIgnoreCase("twitter")) {
				TwitterService twitter = TwitterService.createTwitterServiceOnBehalfOfUsername(service_entry.getServiceUsername());
				for(TwitterFriend f : twitter.getFriends() ) {
					//f.setOnBehalfOf(service_entry);
					friends.add(f);
				}

				//friends.addAll(twitter.getFriends());

			}
			if(service_entry.getServiceName().equalsIgnoreCase("foursquare")) {
				try {
					FoursquareService foursquare = FoursquareService.createFoursquareServiceOnBehalfOfUserID(service_entry.getServiceUserID());
					for(FoursquareFriend f : foursquare.getFriends() ) {
						//f.setOnBehalfOf(service_entry);
						friends.add(f);
					}

				} catch (BadAccessTokenException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.error("",e);
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
				TwitterService twitter = TwitterService.createTwitterServiceOnBehalfOfUsername(service_entry.getServiceUsername());
				if(twitter.localFriendsIsFresh() == false) {
					twitter.serviceRequestFollows();
				}
				friends.addAll(twitter.getFriends());
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


	public List<ServiceStatus> getStatusForAllHumans(boolean loadIfStale) {
		List<ServiceStatus> allStatus = new ArrayList<ServiceStatus>();
		List<Human> humans = getAllHumans();
		for(Human human : humans) {
			allStatus.addAll(getStatusForHuman(human, loadIfStale));
		}
		return allStatus;

	}

	public List<ServiceStatus> getStatusForAllHumans() {
		return getStatusForAllHumans(false);
	}

	public List<ServiceStatus> getStatusForHuman(Human aHuman, boolean loadIfStale) {
		List<ServiceStatus> result = new ArrayList<ServiceStatus>();

		List<ServiceUser> service_users = aHuman.getServiceUsers();
		for(ServiceUser service_user : service_users) {
			String service_name = service_user.getService();
			logger.debug(service_user);
			if(service_name.equalsIgnoreCase("twitter")) {
				TwitterService twitter = TwitterService.createTwitterServiceOnBehalfOfUsername(service_user.getOnBehalfOfUsername());
				if(loadIfStale && twitter.localServiceStatusIsFreshFor(service_user.getUserID()) == false) {
					List<TwitterStatus> status = twitter.serviceRequestStatusForUserID(service_user.getUserID());
					result.addAll(status);
				} else {
					result.addAll(twitter.getStatusForUserID(service_user.getUserID()));
				}
			}
			if(service_name.equalsIgnoreCase("instagram")) {
				InstagramService instagram = new InstagramService();
				try {
					logger.debug(service_user.getServiceID()+" "+service_user.getUsername());
					instagram.initServiceOnBehalfOfUsername(service_user.getOnBehalfOfUsername());
					if(loadIfStale && instagram.localServiceStatusIsFreshForUserID(service_user.getUserID()) == false) {
						result.addAll(instagram.serviceRequestStatusForUserID(service_user.getUserID()));
					} else {
						result.addAll(instagram.getStatusForUserID(service_user.getUserID()));
					}
				} catch (BadAccessTokenException e) {
					logger.error("", e);
					e.printStackTrace();
				}

			}
			if(service_name.equalsIgnoreCase("flickr")) {
				try {
					FlickrService flickr = FlickrService.createFlickrServiceOnBehalfOfUserID(service_user.getOnBehalfOfUserId());
					if(loadIfStale && flickr.localServiceStatusIsFreshForUserID(service_user.getUserID()) == false) {
						result.addAll(flickr.serviceRequestStatusForUserID(service_user.getUserID()));
					} else {
						result.addAll(flickr.getStatusForUserID(service_user.getUserID()));
					}
				} catch (BadAccessTokenException e) {
					logger.error("", e);
					e.printStackTrace();
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
					logger.error("", e);
					e.printStackTrace();
				}
			}

		}
		//		logger.debug("BEFORE");
		//		for(int i=0; i< result.size(); i++) {
		//			logger.debug(result.get(i).getClass()+" "+ result.get(i).getCreatedDate());
		//			
		//		}

		Collections.sort(result);

		//		logger.debug("AFTER");
		//		for(int i=0; i< result.size(); i++) {
		//			logger.debug(result.get(i).getClass()+" "+ result.get(i).getCreatedDate());
		//			
		//		}

		return result;
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
	 * Get all of the services this Humans User has assigned - twitter,
	 * instagram, flickr, etc., etc.
	 * 
	 * @return
	 * @deprecated use {@link getServiceNamesAssigned()}
	 */
	//	@Deprecated
	//	public List<String> getServicesAssigned() {
	//		return getServiceNamesAssigned();
	//	}


	/**
	 * 
	 * @param ServiceName
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
				if(serviceUser.service != null && serviceUser.service.equalsIgnoreCase(aServiceName)) {
					if(serviceUser.getOnBehalfOfUsername() == null) {
						logger.warn("WTF? "+this+" The username for serviceUser.getOnBehalfOf is null: "+serviceUser);
					}
					result.add(serviceUser);
				}
			}
		}
		return result;
	}


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

	/**
	 * 
	 * @param aServiceUserID
	 * @param aServiceUsername
	 * @param aServiceTypeName
	 * @return
	 */
	public boolean removeService(String aServiceUserID, String aServiceUsername, String aServiceTypeName) {
		ServiceEntry service_entry = new ServiceEntry(aServiceUserID, aServiceUsername, aServiceTypeName);
		boolean result = services.remove(service_entry);
		logger.debug("removing service entry "+service_entry);
		logger.debug("Removing service_entry "+service_entry.hashCode()+" from "+this+" result="+result);

		// delete service token??
		//		ServiceTokenDAO st_dao = new ServiceTokenDAO(aServiceTypeName);
		//		ServiceToken st = st_dao.findByExactUserId(aServiceUserID);
		//		st_dao.delete(st);

		// delete friends


		// delete status

		return result;
	}

	public boolean removeServiceBy(String aServiceName, String aServiceUserID) 
	{
		boolean result = false;
		ServiceEntry service_entry = selectFirst(services, 
				and(having(on(ServiceEntry.class).getServiceName(), equalTo(aServiceName)),
						having(on(ServiceEntry.class).getServiceUserID(), equalTo(aServiceUserID))));
		result = services.remove(service_entry);
		return result;
	}


	public void removeServiceUser(ServiceUser aServiceUser) {
		List<Human> allHumans = this.getAllHumans();
		for(Human human : allHumans) {
			human.removeServiceUser(aServiceUser);
		}
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
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "HumansUser [username=" + username + ", password=" + password
				+ ", email=" + email + ", humans=" + humans + ", services="
				+ services + "]";
	}

}