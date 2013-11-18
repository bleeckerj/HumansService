package com.nearfuturelaboratory.humans.entities;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

import com.google.gson.Gson;


/**
 * For our serviceUsers sub-document within our HumansUser thing
 * @author julian
 *
 */
@Entity(value="serviceUsers",noClassnameStored = true)
public class ServiceUser extends BaseEntity {

	protected String username;
	protected String serviceID;
	protected String service;
	@Embedded
	protected OnBehalfOf onBehalfOf;
	/**
	 * This is the service icon/avatar image for this user for this service
	 */
	protected String imageURL;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String aUsername) {
		username = aUsername;
	}
	public String getServiceID() {
		return serviceID;
	}
	public void setServiceID(String aServiceID) {
		serviceID = aServiceID;
	}
	public String getService() {
		return service;
	}
	public void setService(String aService) {
		service = aService;
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

	@Override
	public String toString() {
		String o = new Gson().toJson(this).toString();
		//System.out.println("o = "+o);
		return o;
		//return new Gson().toJsonTree(this, this.getClass()).getAsString();
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((onBehalfOf == null) ? 0 : onBehalfOf.hashCode());
		result = prime * result + ((service == null) ? 0 : service.hashCode());
		result = prime * result
				+ ((serviceID == null) ? 0 : serviceID.hashCode());
		result = prime * result
				+ ((username == null) ? 0 : username.hashCode());
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
		ServiceUser other = (ServiceUser) obj;
		if (onBehalfOf == null) {
			if (other.onBehalfOf != null)
				return false;
		} else if (!onBehalfOf.equals(other.onBehalfOf))
			return false;
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