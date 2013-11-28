package com.nearfuturelaboratory.humans.foursquare.entities;

import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.annotations.PostLoad;
import org.mongodb.morphia.annotations.PostPersist;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Version;
import org.mongodb.morphia.annotations.Entity;

import com.nearfuturelaboratory.humans.core.MinimalSocialServiceUser;
import com.nearfuturelaboratory.humans.entities.ServiceEntry;


@Entity(value="friend", noClassnameStored = true)
//@Indexes( @Index(name="friend_index", value="user_id, friend_id, friend.id", unique=true/*, dropDups=true*/) )
@Indexes( @Index(name="friend_index", value="user_id, friend_id", unique=true/*, dropDups=true*/) )
public class FoursquareFriend extends MinimalSocialServiceUser {

	@Version
	@Property ("version")
	private Long version;
	protected Date lastUpdated;

	@PrePersist void prePersist() {
		lastUpdated = new Date();
		if(onBehalfOf == null) {
			onBehalfOf = new ServiceEntry(user.getId(), user.getUsername(), "foursquare");

		}
	}

	@PostLoad void postLoad() {
		if(onBehalfOf == null) {
			onBehalfOf = new ServiceEntry(user.getId(), user.getUsername(), "foursquare");
		}
	}

	@Id
	ObjectId id;


	protected String friend_id;
	// this leads to weird concurrent modification exceptions when I update..weird
	@Reference
	protected FoursquareUser friend;
	
	@Reference
	protected FoursquareUser user;
	
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

	/**
	 * @return the id
	 */
	public ObjectId getId() {
		return id;
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
	/**
	 * @return the user
	 */
	public FoursquareUser getUser() {
		return user;
	}
	/**
	 * @param aUser the user to set
	 */
	public void setUser(FoursquareUser aUser) {
		user = aUser;
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

	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FoursquareFriend [lastUpdated=" + lastUpdated + ", id=" + id
				+ ", friend_id=" + friend_id + ", friend=" + friend + ", user="
				+ user + ", user_id=" + user_id + ", onBehalfOf=" + onBehalfOf
				+ ", getLargeImageURL()=" + getLargeImageURL() + "]";
	}

	@Override
	public String getImageURL() {
		return getFriend().get72SquarePhoto();
	}
	@Override
	public String getUserID() {
		return this.getFriend_id();
	}
	@Override
	public String getUsername() {
		return this.getFriend().getFirstName();
	}
	@Override
	public String getServiceName() {
		return "foursquare";
	}
	@Override
	public String getLargeImageURL() {
		return getFriend().getSquarePhoto(120);
	}

	
	public String getFirstName() {
		return friend.firstName;
	}
	
	public String getLastName() {
		return friend.lastName;
	}
//	public void setOnBehalfOf(ServiceEntry entry) {
//		onBehalfOf = entry;
//	}
//	
//	public ServiceEntry getOnBehalfOf() {
//		return onBehalfOf;
//	}


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
