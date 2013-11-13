package com.nearfuturelaboratory.humans.entities;

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


@Entity("humans")
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
	public void setServiceUsers(List<ServiceUser> aServiceUsers) {
		serviceUsers = aServiceUsers;
	}
	
	public void addServiceUser(ServiceUser aServiceUser) {
		if(serviceUsers.contains(aServiceUser)) {
			return;
		} else {
			serviceUsers.add(aServiceUser);
		}
	}
	
	public void removeServiceUser(ServiceUser aServiceUser) {
		serviceUsers.remove(aServiceUser);
	}

	
	protected ObjectId getHumanid() {
		return humanid;
	}
	protected void setHumanid(ObjectId aHumanid) {
		humanid = aHumanid;
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
	

}
