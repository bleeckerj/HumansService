package com.nearfuturelaboratory.humans.entities;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;


@Entity(value="service_entry", noClassnameStored = true)
public
class ServiceEntry {

	//@Transient protected Pair<String, String> pair;
	
	@Property("serviceUserID") protected String serviceUserID;
	@Property("serviceUsername") protected String serviceUsername;
	@Property("serviceName") protected String serviceName;

	protected ServiceEntry() {}


	/**
	 * 
	 * @param aServiceUserID
	 * @param aServiceUsername
	 * @param aServiceName
	 */
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



	public String getServiceName() {
		return serviceName;
	}



	public void setServiceName(String aServiceName) {
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