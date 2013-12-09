package com.nearfuturelaboratory.humans.entities;
import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.Gson;
import com.nearfuturelaboratory.humans.core.MinimalSocialServiceUser;


/**
 * For our serviceUsers sub-document within our HumansUser thing
 * @author julian
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@Entity(value="serviceUsers",noClassnameStored = true)
public class ServiceUser extends MinimalSocialServiceUser {


	@Version
	@Property ("version")
	private Long version;
	protected Date lastUpdated;

	@Id
	protected ObjectId id;

	protected String username;
	protected String serviceID;
	protected String service;
	//	@Embedded
	//	protected ServiceEntry onBehalfOf;

	public ServiceUser() {
		super();
	}

	public ServiceUser(String aServiceID, String aUsername, String aServiceName, String aImageURL, ServiceEntry onBehalfOf) {
		setUsername(aUsername);
		setServiceID(aServiceID);
		setService(aServiceName);
		this.setImageURL(aImageURL);
		this.setOnBehalfOf(onBehalfOf);
	}

	/**
	 * This is the service icon/avatar image for this user for this service
	 */
	protected String imageURL;
	@PrePersist void prePersist() {
		lastUpdated = new Date();
		if(id == null) {
			id = new ObjectId();
		}

	}

	/**
	 * @return the id
	 */
	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId aId) {
		id = aId;
	}

	public String getUsername() {
		return username;
	}
	public void setUsername(String aUsername) {
		username = aUsername;
	}
	public String getService() {
		return service;
	}
	public void setService(String aService) {
		service = aService;
	}
	/**
	 * @return the serviceID
	 */
	public String getServiceID() {
		return serviceID;
	}



	/**
	 * @param aServiceID the serviceID to set
	 */
	public void setServiceID(String aServiceID) {
		serviceID = aServiceID;
	}

	public ServiceEntry getOnBehalfOf() {
		return onBehalfOf;
	}

	public String getOnBehalfOfUsername() {
		return this.onBehalfOf.getServiceUsername();
	}

	public String getOnBehalfOfUserId() {
		return this.onBehalfOf.getServiceUserID();
	}

	public void setOnBehalfOf(String aServiceUserID, String aServiceUsername, String aServiceName) {
		ServiceEntry e = new ServiceEntry();
		e.setServiceName(aServiceName);
		e.setServiceUserID(aServiceUserID);
		e.setServiceUsername(aServiceUsername);
		onBehalfOf = e;  //new OnBehalfOf(aServiceUserID, aServiceUsername);
	}
	public String getImageURL() {
		return imageURL;
	}
	public void setImageURL(String aImageURL) {
		imageURL = aImageURL;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ServiceUser [username=" + username + ", serviceID=" + serviceID
				+ ", service=" + service + ", imageURL=" + imageURL + "]";
	}

	//	@Override
	//	public String toString() {
	//		//String o = new Gson().toJson(this).toString();
	//		//System.out.println("o = "+o);
	//		//return o;
	//		//return new Gson().toJsonTree(this, this.getClass()).getAsString();
	//	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((service == null) ? 0 : service.hashCode());
		result = prime * result
				+ ((serviceID == null) ? 0 : serviceID.hashCode());
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
		ServiceUser other = (ServiceUser) obj;
		if (service == null) {
			if (other.service != null)
				return false;
		} else if (!service.equals(other.service))
			return false;
		if (serviceID == null) {
			if (other.serviceID != null)
				return false;
		} else if (!serviceID.equals(other.serviceID))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

	@Override
	public String getUserID() {
		return this.getServiceID();
	}

	@Override
	public String getServiceName() {
		return "-";
	}

	@Override
	public String getLargeImageURL() {
		return "-";
	}

	public String getFirstName() {
		return null;
	}

	public String getLastName() {
		return null;
	}


}

class OnBehalfOf  {


	//@Transient protected Pair<String, String> pair;
	@Property("serviceUserID") protected String serviceUserID;
	@Property("serviceUsername") protected String serviceUsername;

	protected OnBehalfOf() {}

	public OnBehalfOf(String aServiceUserID, String aServiceUsername) {
		serviceUserID = aServiceUserID;
		serviceUsername = aServiceUsername;
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

	@Override
	public String toString() {
		return "OnBehalfOf [serviceUserID=" + serviceUserID
				+ ", serviceUsername=" + serviceUsername + "]";
	}

}