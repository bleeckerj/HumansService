package com.nearfuturelaboratory.humans.twitter.entities;

import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
//import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.annotations.Index;
//import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.PostLoad;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Version;

import com.nearfuturelaboratory.humans.entities.MinimalSocialServiceUser;
import com.nearfuturelaboratory.humans.entities.ServiceEntry;

@Entity(value="friend",noClassnameStored = true)
@Indexes( @Index(name="friends_index", value="follower_id, friend_id", unique=true/*, dropDups=true*/) )
public class TwitterFriend extends MinimalSocialServiceUser {

	@Version
	@Property ("version")
	private Long version;
	protected Date lastUpdated;
	@Id 
	@Property("id")
	protected ObjectId id;
	
	@PrePersist void prePersist() {
		lastUpdated = new Date();
		if(onBehalfOf == null) {
			 onBehalfOf = new ServiceEntry(this.follower.getUserID(), this.follower.getUsername(), "twitter");
		}
	}

	@PostLoad void postLoad() {
		if(onBehalfOf == null) {
			 onBehalfOf = new ServiceEntry(this.follower.getUserID(), this.follower.getUsername(), "twitter");
		}
	}
	

	@Reference
	protected TwitterUser friend;
	@Reference
	protected TwitterUser follower;
	
	protected String follower_id;
	protected String friend_id;

	public TwitterFriend() {
		super();
	}

	public TwitterFriend(TwitterUser aTwitterUserBriefly) {
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
		return this.getFriend().getId();
	}

	@Override
	public String getUsername() {
		return getFriend().getScreen_name();
	}

	@Override
	public String getServiceName() {
		return "twitter";
	}

	
	public String getFirstName() {
		return this.getFriend().getName();
	}
	
	public String getLastName() {
		return this.getFriend().getName();
	}

	public String getFullName() {
        return this.getFriend().getName();
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

//	public void setOnBehalfOf(ServiceEntry entry) {
//		onBehalfOf = entry;
//	}
//	
//	public ServiceEntry getOnBehalfOf() {
//		return onBehalfOf;
//	}
//
	
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
    public String getIdStr() { return id.toString(); }

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
		return "TwitterFriend [version=" + version + ", lastUpdated="
				+ lastUpdated + ", id=" + id + ", friend=" + friend
				+ ", follower=" + follower + ", follower_id=" + follower_id
				+ ", friend_id=" + friend_id + ", onBehalfOf=" + onBehalfOf
				+ "]";
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
		TwitterFriend other = (TwitterFriend) obj;
		if (friend == null) {
			if (other.friend != null)
				return false;
		} else if (!friend.equals(other.friend))
			return false;
		return true;
	}

}
