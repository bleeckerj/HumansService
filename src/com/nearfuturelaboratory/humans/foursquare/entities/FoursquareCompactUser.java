package com.nearfuturelaboratory.humans.foursquare.entities;

import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Version;

@Entity(value="compactuser", noClassnameStored = true)
public class FoursquareCompactUser  {
	
//	@Version
//	@Property ("version")
//	private Long version;
//	protected Date lastUpdated;

//	@PrePersist void prePersist() {
//		lastUpdated = new Date();
//	}

	
	@Id
	String id;
	String firstName;
	String lastName;
	FoursquarePhoto photo;
	String homeCity;
	
	public String getId() {
		return id;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String aFirstName) {
		firstName = aFirstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String aLastName) {
		lastName = aLastName;
	}
	public FoursquarePhoto getPhoto() {
		return photo;
	}
	public void setPhoto(FoursquarePhoto aPhoto) {
		photo = aPhoto;
	}
}