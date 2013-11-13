package com.nearfuturelaboratory.humans.core;

import com.google.gson.Gson;

/**
 * This represents the service facet of someone on a service you follow and
 * you want to be part of a Human. E.g. darthjulian on twitter, or nicolasnova on instagram
 * It needs this "onBehalfOf" attribute in order to know what tokens to use to retrieve status, and otherwise
 * operate on it.
 * 
 * @author julian
 *
 */
public class ServiceUser {
	protected String username;
	protected String serviceID;
	protected String service;
	protected String onBehalfOf;
	protected String imageURL;

	public String getCodedUsername() {
		return serviceID+"-"+username;
	}

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

	public String getOnBehalfOf() {
		return onBehalfOf;
	}

	public void setOnBehalfOf(String aOnBehalfOf) {
		onBehalfOf = aOnBehalfOf;
	}


	public void setImageURL(String aImageURL) {
		imageURL = aImageURL;
		
	}
	
	public String getImageURL() {
		return imageURL;
	}
//TODO add method to get the actual status from local?	
	
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
	
	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}


}