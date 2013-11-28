package com.nearfuturelaboratory.humans.entities;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;

import org.mongodb.morphia.annotations.*;

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
