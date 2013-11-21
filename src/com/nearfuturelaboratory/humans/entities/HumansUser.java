package com.nearfuturelaboratory.humans.entities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.eclipse.jdt.annotation.NonNull;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.PostLoad;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Transient;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.utils.IndexDirection;

import com.mongodb.Mongo;
import com.nearfuturelaboratory.humans.core.MinimalSocialServiceUser;
import com.nearfuturelaboratory.humans.entities.ServiceUser;
import com.nearfuturelaboratory.humans.exception.BadAccessTokenException;
import com.nearfuturelaboratory.humans.service.FlickrService;
import com.nearfuturelaboratory.humans.service.FoursquareService;
import com.nearfuturelaboratory.humans.service.InstagramService;
import com.nearfuturelaboratory.humans.service.TwitterService;
import com.nearfuturelaboratory.util.Pair;


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
	protected List<ServiceEntry> services;
	//protected List<Map<String,List<ServiceEntry>>> services;


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

	public void addHuman(Human aHuman) {
		if(humans.contains(aHuman)) {
			humans.remove(aHuman);
			humans.add(aHuman);
		} else {
			humans.add(aHuman);
		}
	}

	public void removeHuman(Human aHuman) {
		humans.remove(aHuman);
	}

	public List<Human> getAllHumans() {
		return this.humans;

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

	public List<MinimalSocialServiceUser> getFriends() {
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
					instagram = InstagramService.createInstagramServiceOnBehalfOfUsername(service_entry.getServiceUsername());
					if(instagram.localFriendsIsFresh() == false) {
						instagram.serviceRequestFollows();
					}
					friends.addAll(instagram.getFollows());

				} catch (BadAccessTokenException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.error("",e);
				}
			}
			if(service_entry.getServiceName().equalsIgnoreCase("twitter")) {
				TwitterService twitter = TwitterService.createTwitterServiceOnBehalfOfUsername(service_entry.getServiceUsername());
				if(twitter.localFollowsIsFresh() == false) {
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


	public void removeServiceUser(ServiceUser aServiceUser) {
		List<Human> allHumans = this.getAllHumans();
		for(Human human : allHumans) {
			human.removeServiceUser(aServiceUser);
		}
	}

	public void addService(@NonNull String aServiceUserID, @NonNull String aServiceUsername, @NonNull String aServiceTypeName) {
		ServiceEntry service_entry = new ServiceEntry(aServiceUserID, aServiceUsername, aServiceTypeName);
		if(aServiceUserID == null || aServiceUsername == null || aServiceTypeName == null) {
			logger.warn("While adding a service_entry, got an empty value for one of userid("+aServiceUserID+"), username("+aServiceUsername+") or service("+aServiceTypeName+") when attempting to add a service_entry "+this);
			return;		
		}
		// check for dupes?
		if(services.contains(service_entry)) {
			logger.info("Attempted to add an already existing ServiceEntry to "+this);
		} else {
			services.add(service_entry);
		}
	}

	public boolean removeService(@NonNull String aServiceUserID, @NonNull String aServiceUsername, @NonNull String aServiceTypeName) {
		ServiceEntry service_entry = new ServiceEntry(aServiceUserID, aServiceUsername, aServiceTypeName);
		boolean result = services.remove(service_entry);
		logger.debug("Removing service_entry "+service_entry+" from "+this+" result="+result);
		return result;
	}

	//	public List<HumansUser>getAllHumansUsers() {
	//		Query<HumansUser> q = datastore.createQuery(HumansUser.class);
	//		List<HumansUser> humans_users = q.asList();
	//		return humans_users;
	//	}
	//	
	//	public List<String>getAllHumansUsers_Usernames() {
	//		List<String>results = new ArrayList<String>();
	//		List<HumansUser>humans_users = getAllHumansUsers();
	//		for(HumansUser human : humans_users) {
	//			results.add(human.getUsername());
	//		}
	//		return results;
	//		
	//	}

	//	public List<Map<String, List<ServiceEntry>>> getServices() {
	//		return services;
	//	}
	//	public void setServices(List<Map<String, List<ServiceEntry>>> aServices) {
	//		services = aServices;
	//	}

	//	/**
	//	 * @return the services
	//	 */
	//	public List<Map<String, List<ServiceEntry>>> getServices() {
	//		return services;
	//	}
	//	/**
	//	 * @param aServices the services to set
	//	 */
	//	public void setServices(List<Map<String, List<ServiceEntry>>> aServices) {
	//		services = aServices;
	//	}
	//	/**
	//	 * 
	//	 * @param aServiceTypeName
	//	 * @param aServiceUsername
	//	 * @param aServiceUserID
	//	 * 
	//	 * * @deprecated use {@link addService(String, String, String)} instead. 
	//	 */
	//	@Deprecated
	//	public void addServiceForHuman(@NonNull String aServiceTypeName, @NonNull String aServiceUsername, @NonNull String aServiceUserID) {
	//		this.addService(aServiceTypeName, aServiceUsername, aServiceUserID);
	//	}
	//
	/*
	public void addService(@NonNull String aServiceTypeName, @NonNull String aServiceUsername, @NonNull String aServiceUserID) {

//		if(services == null) {
//			services = new ArrayList<Map<String, List<ServiceEntry>>>();
//		}

		if(services == null) {
			//services = new ArrayList<Map<String,List<ServiceEntry>>>();
		}

		if(aServiceTypeName == null || aServiceUsername == null || aServiceUserID == null) {
			logger.warn("Bailing cause I found null values when adding a Service "+aServiceTypeName+", "+aServiceUsername+", "+aServiceUserID);
			return;
		}
		// aiyee. find the entries for the specific service
		//Iterator<Map<String, List<ServiceEntry>>> services_iterator = services.iterator();
		Iterator<Map<String, List<ServiceEntry>>> services_iterator = services.iterator();

		//for(Map<String, List<ServiceEntry>> service : services){
		while(services_iterator.hasNext()) {
			//Map<String, List<ServiceEntry>>service = services_iterator.next();
			Map<String, List<ServiceEntry>> service = services_iterator.next();
			if(service.containsKey(aServiceTypeName.toLowerCase())) {
			//if(service.getServiceName().equalsIgnoreCase(aServiceTypeName)) {
				//
				// the Pair is first the serviceUserID and then the serviceUsername
				//
				List<ServiceEntry> servicesForKey = service.get(aServiceTypeName.toLowerCase());
				//List<ServiceEntry> servicesForKey = service.getServiceEntries();
				if(servicesForKey == null) {
					services_iterator.remove();
					//services.remove(service);
					continue;
				}
				Iterator<ServiceEntry> iter = servicesForKey.iterator();
				while(iter.hasNext()) {
					ServiceEntry serviceForKey = iter.next();
					// if there's either a userid or username that's the same as what we're trying to put in..remove it..no duplicates, right?
					if(serviceForKey.getServiceUserID().equals(aServiceUserID) || serviceForKey.getServiceUsername().equals(aServiceUsername)) {
						// how to remove without concurrent modification error..
						iter.remove();
						services_iterator.remove();
						logger.info("For "+this.getUsername() +" there was already an entry for either this userid ["+aServiceUserID+"] or this username ["+aServiceUsername+"]");
					}


				}
			// add it..
				//servicesForKey.add(new ServiceEntry(aServiceUserID, aServiceUsername));

			} else {
			}
		}
		ServiceEntry entry = new ServiceEntry(aServiceUserID, aServiceUsername, aServiceTypeName);
		List<ServiceEntry> list = new ArrayList<ServiceEntry>();
		list.add(entry);
		Map<String, List<ServiceEntry>> map = new HashMap<String, List<ServiceEntry>>();
		//Service newService = new Service();
//		newService.setServiceName(aServiceTypeName);
//		newService.setServiceEntries(list);
		map.put(aServiceTypeName, list);
		//services.add(newService);

	}
	 */	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId aId) {
		id = aId;
	}
	public List<ServiceEntry> getServices() {
		return services;
	}

}