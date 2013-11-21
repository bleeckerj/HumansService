package com.nearfuturelaboratory.humans.twitter.entities;

import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
//import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.annotations.Index;
//import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Version;

import com.nearfuturelaboratory.humans.core.MinimalSocialServiceUser;
import com.nearfuturelaboratory.humans.entities.BaseEntity;

@Entity(value="friends",noClassnameStored = true)
@Indexes( @Index(name="friends_index", value="follower_id, friend_id", unique=true/*, dropDups=true*/) )
public class TwitterFollows /*extends BaseEntity*/ implements MinimalSocialServiceUser {

	@Version
	@Property ("version")
	private Long version;
	protected Date lastUpdated;
	@Id 
	@Property("id")
	protected ObjectId id;

	@PrePersist void prePersist() {
		lastUpdated = new Date();
	}


	@Reference
	protected TwitterUser friend;
	@Reference
	protected TwitterUser follower;
	
	protected String follower_id;
	protected String friend_id;

	public TwitterFollows() {
		super();
	}

	public TwitterFollows(TwitterUser aTwitterUserBriefly) {
		super();
		friend = aTwitterUserBriefly;
	}



	/**
	 * @return the friend
	 */
	public TwitterUser getFriend() {
		return friend;
	}

	/**
	 * @param aFriend the friend to set
	 */
	public void setFriend(TwitterUser aFriend) {
		friend = aFriend;
	}

	/**
	 * @return the follower
	 */
	public TwitterUser getFollower() {
		return follower;
	}

	/**
	 * @param aFollower the follower to set
	 */
	public void setFollower(TwitterUser aFollower) {
		follower = aFollower;
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

	@Override
	public String getImageURL() {
		return this.getFriend().getProfile_image_url();
	}

	@Override
	public String getUserID() {
		return this.getFollower_id();
	}

	@Override
	public String getUsername() {
		return getFriend().getScreen_name();
	}

	@Override
	public String getServiceName() {
		return "twitter";
	}

	@Override
	public String getLargeImageURL() {
		String i = getImageURL();
		int x = i.lastIndexOf('_');
		int y = i.lastIndexOf('.');
		String root = i.substring(0, x);
		String suffix = i.substring(y, i.length());
		String result = root+"_bigger"+suffix;
		return result;
	}

	/**
	 * @return the lastUpdated
	 */
	public Date getLastUpdated() {
		return lastUpdated;
	}

	/**
	 * @param aLastUpdated the lastUpdated to set
	 */
	public void setLastUpdated(Date aLastUpdated) {
		lastUpdated = aLastUpdated;
	}

	/**
	 * @return the id
	 */
	public ObjectId getId() {
		return id;
	}

	/**
	 * @param aId the id to set
	 */
	public void setId(ObjectId aId) {
		id = aId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TwitterFollows [friend=" + friend + ", follower_id="
				+ follower_id + ", getImageURL()=" + getImageURL()
				+ ", getUsername()=" + getUsername() + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((friend == null) ? 0 : friend.hashCode());
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
		TwitterFollows other = (TwitterFollows) obj;
		if (friend == null) {
			if (other.friend != null)
				return false;
		} else if (!friend.equals(other.friend))
			return false;
		return true;
	}

}
