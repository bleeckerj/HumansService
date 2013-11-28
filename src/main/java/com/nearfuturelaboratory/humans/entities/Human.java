package com.nearfuturelaboratory.humans.entities;

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.selectUnique;
import static org.hamcrest.CoreMatchers.equalTo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.utils.IndexDirection;

import com.nearfuturelaboratory.humans.entities.ServiceUser;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramUser;
import com.nearfuturelaboratory.humans.service.InstagramService;


@Entity(value="humans",noClassnameStored = true)
// compound index
//@Indexes(@Index(name = "nameAndID", value = "name, id", unique = true))
public class Human  /*extends BaseEntity*/ {
	
	// uniqueness applies across the collection, even in an embedded document
	// would need a compound index otherwise, cf http://joegornick.com/2012/10/25/mongodb-unique-indexes-on-single-embedded-documents/
	//@Indexed(value = IndexDirection.ASC, name="name"/*, unique = true*/)
	protected String name;
	
	//@Indexed(value = IndexDirection.ASC, name = "humanid", unique = true/*, sparse = true, dropDups = true*/)

	@Id 
	@Property("humanid")
	protected ObjectId humanid;
	
	@Embedded("serviceUsers")
	protected List<ServiceUser> serviceUsers = new ArrayList<ServiceUser>();
	
	@PrePersist void prePersist() {
		if(humanid == null) {
			humanid = new ObjectId();
		}
	}

	
	public String getName() {
		return name;
	}
	public void setName(String aName) {
		name = aName;
	}
	public List<ServiceUser> getServiceUsers() {
		return serviceUsers;
	}
	
	@Deprecated
	public void setServiceUsers(List<ServiceUser> aServiceUsers) {
		serviceUsers = aServiceUsers;
	}
	
	public boolean addServiceUser(ServiceUser aServiceUser) {
		boolean result;
		if(serviceUsers.contains(aServiceUser)) {
			result = false;
		} else {
			result = serviceUsers.add(aServiceUser);
		}
		return result;
	}
	
	public boolean removeServiceUser(ServiceUser aServiceUser) {
		boolean result;
		result = serviceUsers.remove(aServiceUser);
		return result;
	}

	public boolean removeServiceUserById(String aServiceUserId) {
		boolean result;
		ServiceUser serviceUser = selectUnique(serviceUsers, having(on(ServiceUser.class).getId(), equalTo(new ObjectId(aServiceUserId))));
		result = serviceUsers.remove(serviceUser);
		return result;
	}
	
	public ServiceUser getServiceUserById(String aServiceUserId) {
		ServiceUser serviceUser = selectUnique(serviceUsers, having(on(ServiceUser.class).getId(), equalTo(new ObjectId(aServiceUserId))));
		return serviceUser;
	}
	
	public String getId() {
		return humanid.toString();
	}
	
	/**
	 * Really only for testing
	 * @param aId
	 */
	public void setId(String aId) {
		humanid = new ObjectId(aId);
	}
	
	protected ObjectId getHumanid() {
		return humanid;
	}
	protected void setHumanid(ObjectId aHumanid) {
		humanid = aHumanid;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Human [name=" + name + ", humanid=" + humanid
				+ ", serviceUsers=" + serviceUsers + "]";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Human other = (Human) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	

	public ServiceUser fixImageUrls(ServiceUser aServiceUser) {
		ServiceUser result = aServiceUser;
		ServiceEntry se = aServiceUser.getOnBehalfOf();
		if(aServiceUser.getService().equalsIgnoreCase("instagram")) {
			try {
			InstagramService instagram = InstagramService.createServiceOnBehalfOfUsername(se.getServiceUsername());
			InstagramUser u = instagram.serviceRequestUserBasicForUserID(aServiceUser.getServiceID());
			aServiceUser.setImageURL(u.getImageURL());
			result = aServiceUser;
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
}
