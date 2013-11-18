package com.nearfuturelaboratory.humans.foursquare.entities;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Version;
import org.mongodb.morphia.annotations.Entity;

@Entity(value="user", noClassnameStored = true)
public class FoursquareUser {
	final static Logger logger = Logger.getLogger("com.nearfuturelaboratory.humans.entities.FousquareUser.class");
	@Version
	@Property ("version")
	private Long version;
	protected Date lastUpdated;

	@Id
	protected String id;
	protected String firstName;
	protected String lastName;
	@Embedded
	protected FoursquarePhoto photo;
	protected String relationship;
	@Embedded
	protected FoursquareFriends friends;
	protected String type;
	protected String homeCity;
	protected String gender;
	
	protected Map<String, String>contact;
	protected String bio;
	
	protected Map<String, String> tips;
	
	@Embedded
	protected FoursquareList lists;
	
	@Embedded
	protected FoursquareCheckinsGroup checkins;
	
//	@Embedded
//	protected FoursquareMayorshipsGroup mayorships
	
	@PrePersist void prePersist() {
		lastUpdated = new Date();
	}

	public Date getLastUpdated() {
		if(lastUpdated == null) lastUpdated = new Date();
		return lastUpdated;
	}

	
	
	public Long getVersion() {
		return version;
	}
	public void setVersion(Long aVersion) {
		version = aVersion;
	}
	public String getId() {
		return id;
	}
	public void setId(String aId) {
		id = aId;
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
	
	public String getSquarePhoto(int side) {
		return photo.getSquare(side);
	}
	
	public String get60SquarePhoto() {
		return this.getSquarePhoto(60);
	}
	
	public void setPhoto(FoursquarePhoto aPhoto) {
		photo = aPhoto;
	}
	public String getRelationship() {
		return relationship;
	}
	public void setRelationship(String aRelationship) {
		relationship = aRelationship;
	}
//	public Integer getFriends() {
//		return friends;
//	}
//	public void setFriends(Integer aFriends) {
//		friends = aFriends;
//	}
	public String getType() {
		return type;
	}
	public void setType(String aType) {
		type = aType;
	}
	public String getHomeCity() {
		return homeCity;
	}
	public void setHomeCity(String aHomeCity) {
		homeCity = aHomeCity;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String aGender) {
		gender = aGender;
	}
//	public Map<String, String> getContact() {
//		return contact;
//	}
//	public void setContact(Map<String, String> aContact) {
//		contact = aContact;
//	}
	public String getBio() {
		return bio;
	}
	public void setBio(String aBio) {
		bio = aBio;
	}

	@Override
	public String toString() {
		return "FoursquareUser [version=" + version + ", lastUpdated="
				+ lastUpdated + ", id=" + id + ", firstName=" + firstName
				+ ", lastName=" + lastName + ", photo=" + photo
				+ ", 60sq=" + this.get60SquarePhoto()
				+ ", relationship=" + relationship + ", friends=" + friends
				+ ", type=" + type + ", homeCity=" + homeCity + ", gender="
				+ gender + ", contact=" + contact + ", bio=" + bio + ", tips="
				+ tips + ", lists=" + lists + ", checkins=" + checkins + "]";
	}
	
//	public Map<String, String> getTips() {
//		return tips;
//	}
//	public void setTips(Map<String, String> aTips) {
//		tips = aTips;
//	}
//	public FoursquareList getLists() {
//		return lists;
//	}
//	public void setLists(FoursquareList aLists) {
//		lists = aLists;
//	}
	
}

class FoursquareFriends {
	Integer count;
	List<FoursquareFriendGroups>groups;
}

class FoursquareFriendGroups {
	String type;
	String name;
	Integer count;
	List<FoursquareUser> items;
}

class FoursquareCheckinsGroup {
	Integer count;
	List<FoursquareCheckin> items;
}
class FoursquareList {
	protected Integer count;
//	@Embedded
	protected List<CompactList> groups;
	
}

class CompactList {
	protected Integer count;
	protected String type;
	protected String name;
	@Embedded
	protected List<FoursquareItem> items;
}

class FoursquareItem {
	protected String id;
	protected FoursquareCompactUser user;	
	protected FoursquarePhoto photo;
	protected CompactVenue venue;
	//protected CompactTip tip;
	protected String note;
	protected Date careatedAt;
	protected List<CompactList>listed;
	public String getId() {
		return id;
	}
	public void setId(String aId) {
		id = aId;
	}
	public FoursquareCompactUser getUser() {
		return user;
	}
	public void setUser(FoursquareCompactUser aUser) {
		user = aUser;
	}
	public FoursquarePhoto getPhoto() {
		return photo;
	}
	public void setPhoto(FoursquarePhoto aPhoto) {
		photo = aPhoto;
	}
	public CompactVenue getVenue() {
		return venue;
	}
	public void setVenue(CompactVenue aVenue) {
		venue = aVenue;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String aNote) {
		note = aNote;
	}
	public Date getCareatedAt() {
		return careatedAt;
	}
	public void setCareatedAt(Date aCareatedAt) {
		careatedAt = aCareatedAt;
	}
	public List<CompactList> getListed() {
		return listed;
	}
	public void setListed(List<CompactList> aListed) {
		listed = aListed;
	}
}

class CompactVenue {
	protected String id;
	protected String name;
	protected Map<String, String>contact;
	protected FoursquareLocation location;
	protected List<FoursquareCategory> categories;
	protected boolean verified;
	protected Map<String, String>stats;
	protected String url;
	protected Map<String, String>hours;
	protected Map<String, String>menu;
	protected Map<String, String>price;
	protected String description;
	protected Long cratedAt;
	protected FoursquareCompactUser mayor;
	protected String shortUrl;
	protected String canonicalUrl;
	protected List<FoursquarePhoto> photos;
	protected Integer likes;
}

class FoursquareLists {
	protected String id;
	protected String name;
	protected String description;
	protected FoursquareCompactUser user;
	protected boolean following;
	protected CompactList followers;
	protected boolean editable;
	protected boolean collaborative;
	protected CompactList collaborators;
	protected String canonicalUrl;
	protected FoursquarePhoto photo;
	protected Integer venueCount;
	protected Integer visitedCount;
	protected CompactList listItems;
	
}