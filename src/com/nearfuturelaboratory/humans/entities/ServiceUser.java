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

import com.google.gson.Gson;


/**
 * For our serviceUsers sub-document within our HumansUser thing
 * @author julian
 *
 */
@Entity(value="serviceUsers",noClassnameStored = true)
public class ServiceUser {


	@Version
	@Property ("version")
	private Long version;
//	protected Date lastUpdated;

	@Id
	protected ObjectId id;

	protected String username;
	protected String serviceID;
	protected String service;
	@Embedded
	protected OnBehalfOf onBehalfOf;
	/**
	 * This is the service icon/avatar image for this user for this service
	 */
	protected String imageURL;
	@PrePersist void prePersist() {
//		lastUpdated = new Date();
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

	public OnBehalfOf getOnBehalfOf() {
		return onBehalfOf;
	}
	
	public String getOnBehalfOfUsername() {
		return this.onBehalfOf.getServiceUsername();
	}
	
	public String getOnBehalfOfUserId() {
		return this.onBehalfOf.getServiceUserID();
	}
	
	public void setOnBehalfOf(String aServiceUserID, String aServiceUsername) {
		onBehalfOf = new OnBehalfOf(aServiceUserID, aServiceUsername);
	}
	public String getImageURL() {
		return imageURL;
	}
	public void setImageURL(String aImageURL) {
		imageURL = aImageURL;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	@Override
	public String toString() {
		String o = new Gson().toJson(this).toString();
		//System.out.println("o = "+o);
		return o;
		//return new Gson().toJsonTree(this, this.getClass()).getAsString();
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