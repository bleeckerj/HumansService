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
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Transient;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.utils.IndexDirection;

import com.mongodb.Mongo;
import com.nearfuturelaboratory.humans.entities.ServiceUser;
import com.nearfuturelaboratory.util.Pair;


@Entity("users")
public class HumansUser extends BaseEntity {

	final static Logger logger = Logger.getLogger(com.nearfuturelaboratory.humans.entities.HumansUser.class);

	//static HumansUserDAO dao = new HumansUserDAO();

	//@Id protected ObjectId id;
	@Indexed(value = IndexDirection.ASC, name = "username", unique = true, dropDups = true)
	private String username;
	private String password;
	protected String email;
	//	@Transient 	protected static Mongo mongo;
	//	@Transient protected Datastore datastore;
	//	@Transient private Morphia morphia;
	//	@Transient private final String dbname = "humans";


	@Embedded("humans")
	protected List<Human> humans = new ArrayList<Human>();

//	@Embedded("humans.serviceUsers")
//	protected List<ServiceUser> serviceUsers = new ArrayList<ServiceUser>();

	// Services are a list of a Service Name mapped to a List of Pair<ServiceID, ServiceUsername>
	// Service entry looks like "flickr" : {["66854529@N00","JulianBleecker"],["858291847@N11","Near Future Laboratory"]}
	@Embedded("services")
	protected List<Map<String, List<ServiceEntry>>> services = new ArrayList<Map<String, List<ServiceEntry>>>();


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

	//	public void getFollows() {
	//		List<String>services = getServicesAssigned();
	//		for(String serviceName : services) {
	//			List<String> serviceUsers = this.getService
	//		}
	//	}

	public List<String> getServiceNamesAssigned() {
		List<String> result = new ArrayList<String>();
		List<Map<String, List<ServiceEntry>>> services = this.getServices();

		for(Map<String, List<ServiceEntry>> service : services) {
			//logger.debug(service.toString());
			logger.debug(service.entrySet());
			for ( String key : service.keySet() ) {
				if(result.contains(key) == false) {
					result.add( key );
				}
			}
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
	@Deprecated
	public List<String> getServicesAssigned() {
		return getServiceNamesAssigned();
	}


	/**
	 * 
	 * @param ServiceName
	 *            typically lowercase name of a service, eg twitter, instagram,
	 *            flickr, foursquare
	 * @return a List<String> of the accounts for that service assigned/attached
	 *         on behalf of this humans user in this "coded" format of
	 *         id-username which is a good key to useful files.
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
						logger.warn("WTF?");
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

	public List<Map<String, List<ServiceEntry>>> getServices() {
		return services;
	}
	public void setServices(List<Map<String, List<ServiceEntry>>> aServices) {
		services = aServices;
	}

	/**
	 * 
	 * @param aServiceTypeName
	 * @param aServiceUsername
	 * @param aServiceUserID
	 * 
	 * * @deprecated use {@link addService(String, String, String)} instead. 
	 */
	@Deprecated
	public void addServiceForHuman(@NonNull String aServiceTypeName, @NonNull String aServiceUsername, @NonNull String aServiceUserID) {
		this.addService(aServiceTypeName, aServiceUsername, aServiceUserID);
	}

	public void addService(@NonNull String aServiceTypeName, @NonNull String aServiceUsername, @NonNull String aServiceUserID) {

		if(services == null) {
			services = new ArrayList<Map<String, List<ServiceEntry>>>();
		}
		if(aServiceTypeName == null || aServiceUsername == null || aServiceUserID == null) {
			logger.warn("Bailing cause I found null values when adding a Service "+aServiceTypeName+", "+aServiceUsername+", "+aServiceUserID);
			return;
		}
		// aiyee. find the entries for the specific service
		Iterator<Map<String, List<ServiceEntry>>> services_iterator = services.iterator();
		//for(Map<String, List<ServiceEntry>> service : services){
		while(services_iterator.hasNext()) {
			Map<String, List<ServiceEntry>>service = services_iterator.next();
			if(service.containsKey(aServiceTypeName.toLowerCase())) {

				//
				// the Pair is first the serviceUserID and then the serviceUsername
				//
				List<ServiceEntry> servicesForKey = service.get(aServiceTypeName.toLowerCase());
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
		map.put(aServiceTypeName, list);
		services.add(map);

	}
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId aId) {
		id = aId;
	}

}

class ServiceEntry  {

	//@Transient protected Pair<String, String> pair;
	@Property("serviceUserID") protected String serviceUserID;
	@Property("serviceUsername") protected String serviceUsername;
	@Property("serviceName") protected String serviceName;

	protected ServiceEntry() {}



	public ServiceEntry(String aServiceUserID, String aServiceUsername, String aServiceName) {
		serviceUserID = aServiceUserID;
		serviceUsername = aServiceUsername;
		serviceName = aServiceName;
	} 

	public String getServiceUserID() {
		return serviceUserID;
	}

	public String getServiceUsername() {
		return serviceUsername;
	}

	public void setServiceUserID(String aServiceUserID) {
		serviceUserID = aServiceUserID;
	}

	public void setServiceUsername(String aServiceUsername) {
		serviceUsername = aServiceUsername;
	}



	protected String getServiceName() {
		return serviceName;
	}



	protected void setServiceName(String aServiceName) {
		serviceName = aServiceName;
	}



	@Override
	public String toString() {
		return "ServiceEntry [serviceUserID=" + serviceUserID
				+ ", serviceUsername=" + serviceUsername + ", serviceName="
				+ serviceName + "]";
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((serviceName == null) ? 0 : serviceName.hashCode());
		result = prime * result
				+ ((serviceUserID == null) ? 0 : serviceUserID.hashCode());
		result = prime * result
				+ ((serviceUsername == null) ? 0 : serviceUsername.hashCode());
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServiceEntry other = (ServiceEntry) obj;
		if (serviceName == null) {
			if (other.serviceName != null)
				return false;
		} else if (!serviceName.equals(other.serviceName))
			return false;
		if (serviceUserID == null) {
			if (other.serviceUserID != null)
				return false;
		} else if (!serviceUserID.equals(other.serviceUserID))
			return false;
		if (serviceUsername == null) {
			if (other.serviceUsername != null)
				return false;
		} else if (!serviceUsername.equals(other.serviceUsername))
			return false;
		return true;
	}
}