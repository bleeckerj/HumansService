package com.nearfuturelaboratory.humans.instagram.entities;

import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.PostLoad;
import org.mongodb.morphia.annotations.PostPersist;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Version;

import com.nearfuturelaboratory.humans.core.MinimalSocialServiceUser;
import com.nearfuturelaboratory.humans.entities.BaseEntity;
import com.nearfuturelaboratory.humans.entities.ServiceEntry;


@Entity(value="friend", noClassnameStored = true)
@Indexes( @Index(name="friends_index", value="user_id, friend_id", unique=true) )
public class InstagramFriend extends MinimalSocialServiceUser {
	
	@Id 
	@Property("id")
	protected ObjectId id;
	
	@Version
	@Property ("version")
	private Long version;
	
	public ObjectId getId() {
		return id;
	}
	
	public Long getVersion() {
		return version;
	}
	
	public void setVersion(Long version) {
		this.version = version;
	}

	
	protected Date lastUpdated;
	
	@PrePersist void prePersist() {
		lastUpdated = new Date();
		if(onBehalfOf == null) {
			onBehalfOf = new ServiceEntry(user.getId(), user.getUsername(), "instagram");
		}
	}

	@PostLoad void postLoad() {
		if(onBehalfOf == null) {
			onBehalfOf = new ServiceEntry(user.getId(), user.getUsername(), "instagram");
		}
	}

	
	//TODO get these naming conventions sorted out. This is the user who is your friend
	@Reference
	protected InstagramUser friend;
	
	@Reference
	protected InstagramUser user;
	
	protected String user_id;
	protected String friend_id;
	protected String friend_username;
	
	protected InstagramFriend() {
		super();
	}
	
	/**
	 * @return the user
	 */
	public InstagramUser getUser() {
		return user;
	}

	/**
	 * @param aUser the user to set
	 */
	public void setUser(InstagramUser aUser) {
		user = aUser;
	}

	/**
	 * @return the user_id
	 */
	public String getUser_id() {
		return user_id;
	}

	/**
	 * @param aUser_id the user_id to set
	 */
	public void setUser_id(String aUser_id) {
		user_id = aUser_id;
	}

	/**
	 * @return the user
	 */
	public InstagramUser getFollower() {
		return user;
	}

	/**
	 * @param aFollower the user to set
	 */
	public void setFollower(InstagramUser aFollower) {
		user = aFollower;
	}

	/**
	 * @return the lastUpdated
	 */
	public Date getLastUpdated() {
		return lastUpdated;
	}

	public InstagramFriend(InstagramUser aInstagramFriend) {
		super();
		friend = aInstagramFriend;
	}

	/**
	 * @return the friend
	 */
	public InstagramUser getFriend() {
		return friend;
	}

	/**
	 * @param aFriend the friend to set
	 */
	public void setFriend(InstagramUser aFriend) {
		friend = aFriend;
	}


	public String getFollower_id() {
		return user_id;
	}

	public void setFollower_id(String aFollower_id) {
		user_id = aFollower_id;
	}

	/**
	 * @return the friend_id
	 */
	public String getFriend_id() {
		return friend_id;
	}

	/**
	 * @param aFriend_id the friend_id to set
	 */
	public void setFriend_id(String aFriend_id) {
		friend_id = aFriend_id;
	}

	/**
	 * @return the friend_username
	 */
	public String getFriend_username() {
		return friend_username;
	}

	/**
	 * @param aFriend_username the friend_username to set
	 */
	public void setFriend_username(String aFriend_username) {
		friend_username = aFriend_username;
	}

	@Override
	public String toString() {
		return "InstagramFriend [lastUpdated=" + lastUpdated
				+ ", friend=" + friend + ", user=" + user
				+ ", user_id=" + user_id + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((user_id == null) ? 0 : user_id.hashCode());
		result = prime * result
				+ ((friend_id == null) ? 0 : friend_id.hashCode());
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
		InstagramFriend other = (InstagramFriend) obj;
		if (user_id == null) {
			if (other.user_id != null)
				return false;
		} else if (!user_id.equals(other.user_id))
			return false;
		if (friend_id == null) {
			if (other.friend_id != null)
				return false;
		} else if (!friend_id.equals(other.friend_id))
			return false;
		return true;
	}

	@Override
	public String getImageURL() {
		return this.getFriend().getProfile_picture();
	}

	@Override
	public String getUserID() {
		return this.getFriend().getId();
	}

	@Override
	public String getUsername() {
		return this.getFriend().getUsername();
	}

	@Override
	public String getServiceName() {
		return "instagram";
	}

	@Override
	public String getLargeImageURL() {
		return getImageURL();
	}
	
	public String getFirstName() {
		return getUser().getFull_name();
	}
	
	public String getLastName() {
		return getUser().getFull_name();
	}
	
//	public void setOnBehalfOf(ServiceEntry entry) {
//		onBehalfOf = entry;
//	}
//	
//	public ServiceEntry getOnBehalfOf() {
//		return onBehalfOf;
//	}

}

