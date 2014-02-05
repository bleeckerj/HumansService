package com.nearfuturelaboratory.humans.entities;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Version;

//@Entity("users")
public class BaseEntity {

	@Id 
	@Property("id")
	protected ObjectId id;
	
	@Version
	@Property ("version")
	private Long version;
	
	public BaseEntity() {
		
	}
	
	public ObjectId getId() {
		return id;
	}
	
	public Long getVersion() {
		return version;
	}
	
	public void setVersion(Long version) {
		this.version = version;
	}

}
