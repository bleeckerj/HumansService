package com.nearfuturelaboratory.humans.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.util.Date;


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
	protected String serviceUserID;
	protected String serviceName;
	//	@Embedded
	//	protected ServiceEntry onBehalfOf;

	public ServiceUser() {
		super();
	}

	public ServiceUser(String aServiceUserID, String aUsername, String aServiceName, String aImageURL, ServiceEntry onBehalfOf) {
		setUsername(aUsername);
		setServiceUserID(aServiceUserID);
		setServiceName(aServiceName);
		this.setImageURL(aImageURL);
		this.setOnBehalfOf(onBehalfOf);
	}

    public Date getLastUpdated() {
        return lastUpdated;
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
	public void setServiceName(String aServiceName) {
		serviceName = aServiceName;
	}
	/**
	 * @return the serviceUserID
	 */
	public String getServiceUserID() {
		return serviceUserID;
	}



	/**
	 * @param aServiceUserID the serviceUserID to set
	 */
	public void setServiceUserID(String aServiceUserID) {
		serviceUserID = aServiceUserID;
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
		return "ServiceUser [username=" + username + ", serviceUserID=" + serviceUserID
				+ ", serviceName=" + serviceName + ", imageURL=" + imageURL + "]";
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
		result = prime * result + ((serviceName == null) ? 0 : serviceName.hashCode());
		result = prime * result
				+ ((serviceUserID == null) ? 0 : serviceUserID.hashCode());
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
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

	@Override
	public String getUserID() {
		return this.getServiceUserID();
	}

	@Override
	public String getServiceName() {
		return this.serviceName;
	}

	@Override
	public String getLargeImageURL() {
		return "-";
	}

//TODO eh?? Are these handled in the super classes?
	public String getFirstName() {
		return null;
	}

	public String getLastName() {
		return null;
	}

    public String getFullName() {
        return null;
    }

}

class OnBehalfOf  {


	//@Transient protected Pair<String, String> pair;
	@Property("serviceUserID") protected String serviceUserID;
	@Property("serviceUsername") protected String serviceUsername;
    @Property("service") protected String serviceName;

	protected OnBehalfOf() {}


    public OnBehalfOf(String aServiceUserID, String aServiceUsername, String aService) {
		serviceUserID = aServiceUserID;
		serviceUsername = aServiceUsername;
        serviceName = aService;
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

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }


    @Override
	public String toString() {
		return "OnBehalfOf [serviceUserID=" + serviceUserID
				+ ", serviceUsername=" + serviceUsername + ", service=" + serviceName+"]";
	}


}