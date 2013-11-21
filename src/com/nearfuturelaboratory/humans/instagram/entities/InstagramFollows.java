package com.nearfuturelaboratory.humans.instagram.entities;

import java.util.Date;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

import com.nearfuturelaboratory.humans.core.MinimalSocialServiceUser;
import com.nearfuturelaboratory.humans.entities.BaseEntity;


@Entity(value="friends", noClassnameStored = true)
@Indexes( @Index(name="friends_index", value="follower_id, friend_id", unique=true) )
public class InstagramFollows extends BaseEntity implements MinimalSocialServiceUser {
	
	protected Date lastUpdated;
	@PrePersist void prePersist() {
		lastUpdated = new Date();
	}

	//TODO get these naming conventions sorted out. This is the user who is your friend
	@Reference
	protected InstagramUser friend;
	
	@Reference
	protected InstagramUser follower;
	
	protected String follower_id;
	protected String friend_id;
	protected String friend_username;
	
	protected InstagramFollows() {
		super();
	}
	
	/**
	 * @return the follower
	 */
	public InstagramUser getFollower() {
		return follower;
	}

	/**
	 * @param aFollower the follower to set
	 */
	public void setFollower(InstagramUser aFollower) {
		follower = aFollower;
	}

	/**
	 * @return the lastUpdated
	 */
	public Date getLastUpdated() {
		return lastUpdated;
	}

	public InstagramFollows(InstagramUser aInstagramFriend) {
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
		return follower_id;
	}

	public void setFollower_id(String aFollower_id) {
		follower_id = aFollower_id;
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
		return "InstagramFollows [lastUpdated=" + lastUpdated
				+ ", friend=" + friend + ", follower=" + follower
				+ ", follower_id=" + follower_id + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((follower_id == null) ? 0 : follower_id.hashCode());
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
		InstagramFollows other = (InstagramFollows) obj;
		if (follower_id == null) {
			if (other.follower_id != null)
				return false;
		} else if (!follower_id.equals(other.follower_id))
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
		return this.getFollower().getProfile_picture();
	}

	@Override
	public String getUserID() {
		return this.getFollower().getId();
	}

	@Override
	public String getUsername() {
		return this.getFollower().getUsername();
	}

	@Override
	public String getServiceName() {
		return "instagram";
	}

	@Override
	public String getLargeImageURL() {
		return getImageURL();
	}
	
}

