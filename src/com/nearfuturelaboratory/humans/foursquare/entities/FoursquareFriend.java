package com.nearfuturelaboratory.humans.foursquare.entities;

import java.util.Date;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Version;
import org.mongodb.morphia.annotations.Entity;


@Entity(value="friend", noClassnameStored = true)
//@Indexes( @Index(name="friend_index", value="user_id, friend_id, friend.id", unique=true/*, dropDups=true*/) )
@Indexes( @Index(name="friend_index", value="user_id, friend_id", unique=true/*, dropDups=true*/) )
public class FoursquareFriend {
	
	@Version
	@Property ("version")
	private Long version;
	protected Date lastUpdated;

	@PrePersist void prePersist() {
		lastUpdated = new Date();
	}

	
	@Id
	String id;
	
	
	protected String friend_id;
	// this leads to weird concurrent modification exceptions when I update..weird
	@Reference
	protected FoursquareUser friend;
	protected String user_id;
	
	public Long getVersion() {
		return version;
	}
	public void setVersion(Long aVersion) {
		version = aVersion;
	}
	public Date getLastUpdated() {
		return lastUpdated;
	}
	public void setLastUpdated(Date aLastUpdated) {
		lastUpdated = aLastUpdated;
	}
	public String getId() {
		return id;
	}
	public void setId(String aId) {
		id = aId;
	}

	public String getUser_id() {
		return user_id;
	}
	public void setUser_id(String aUser_id) {
		user_id = aUser_id;
	}
	public FoursquareUser getFriend() {
		return friend;
	}
	public void setFriend(FoursquareUser aFriend) {
		friend = aFriend;
	}
	public String getFriend_id() {
		return friend_id;
	}
	public void setFriend_id(String aFriend_id) {
		friend_id = aFriend_id;
	}
//	public FoursquareUser getFriend() {
//		return friend;
//	}
//	public void setFriend(FoursquareUser aFriend) {
//		friend = aFriend;
//	}
	@Override
	public String toString() {
		return "FoursquareFriend [version=" + version + ", lastUpdated="
				+ lastUpdated + ", id=" + id + ", friend_id=" + friend_id
				+ ", friend=" + friend + ", user_id=" + user_id + "]";
	}
	
	
}

//class __FoursquareCompactUser  {
//	
////	@Version
////	@Property ("version")
////	private Long version;
////	protected Date lastUpdated;
//
////	@PrePersist void prePersist() {
////		lastUpdated = new Date();
////	}
//
//	
//
//	String id;
//	String firstName;
//	String lastName;
//	FoursquarePhoto photo;
//	String homeCity;
//	
//	public String getId() {
//		return id;
//	}
//	public String getFirstName() {
//		return firstName;
//	}
//	public void setFirstName(String aFirstName) {
//		firstName = aFirstName;
//	}
//	public String getLastName() {
//		return lastName;
//	}
//	public void setLastName(String aLastName) {
//		lastName = aLastName;
//	}
//	public FoursquarePhoto getPhoto() {
//		return photo;
//	}
//	public void setPhoto(FoursquarePhoto aPhoto) {
//		photo = aPhoto;
//	}
//}
//
