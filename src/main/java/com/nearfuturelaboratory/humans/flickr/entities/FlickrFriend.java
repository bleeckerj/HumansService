package com.nearfuturelaboratory.humans.flickr.entities;

import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.bson.types.ObjectId;
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
@Indexes( @Index(name="friend_index", value="user_id, friend_id", unique=true/*, dropDups=true*/) )
public class FlickrFriend extends MinimalSocialServiceUser {

	final static Logger logger = LogManager.getLogger(com.nearfuturelaboratory.humans.flickr.entities.FlickrFriend.class);

	@Version
	@Property ("version")
	private Long version;
	protected Date lastUpdated;

	@Id
	protected ObjectId id;

	@Property("friend_id")
	protected String nsid;
	protected String username;
	protected String iconserver;
	protected String iconfarm;
	protected Integer ignored;
	protected String realname;
//	protected Integer friend;
//	protected Integer family;
	protected String path_alias;
	protected String location;

	//      { "nsid": "43511287@N06", "username": "*Elybeth", "iconserver": "3337", "iconfarm": 4, "ignored": 0, "realname": "Elisabetta Pisano", "friend": 0, "family": 0, 
	// "path_alias": "elybeth", "location": "Milano, Italia" },
	protected String user_id;

//	@Reference
//	protected FlickrUser friend;
	
	@Reference
	protected FlickrUser user;

	@PrePersist void prePersist() {
		lastUpdated = new Date();
		if(onBehalfOf == null) {
			onBehalfOf = new ServiceEntry(user.getId(), user.getUsername(), "flickr");

		}
	}
	
	@PostLoad void postLoad() {
		if(onBehalfOf == null) {
			onBehalfOf = new ServiceEntry(user.getId(), user.getUsername(), "flickr");
		}		
	}


	public String getFriendID() {
		return getNsid();
	}

	/**
	 * @return the friend
	 */
//	public FlickrUser getFriend() {
//		return friend;
//	}
//
//	/**
//	 * @param aFriend the friend to set
//	 */
//	public void setFriend(FlickrUser aFriend) {
//		friend = aFriend;
//	}

	public FlickrUser getUser() {
		return user;
	}

	public void setUser(FlickrUser aUser) {
		user = aUser;
		setUser_id(user.getId());
	}

	public static Logger getLogger() {
		return logger;
	}

	public Long getVersion() {
		return version;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public String getNsid() {
		return nsid;
	}

	public String getUsername() {
		return username;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId aId) {
		id = aId;
	}

	public String getIconserver() {
		return iconserver;
	}

	public void setIconserver(String aIconserver) {
		iconserver = aIconserver;
	}

	public String getIconfarm() {
		return iconfarm;
	}

	public void setIconfarm(String aIconfarm) {
		iconfarm = aIconfarm;
	}

	public Integer getIgnored() {
		return ignored;
	}

	public void setIgnored(Integer aIgnored) {
		ignored = aIgnored;
	}

	public String getRealname() {
		return realname;
	}

	public void setRealname(String aRealname) {
		realname = aRealname;
	}


	public String getPath_alias() {
		return path_alias;
	}

	public void setPath_alias(String aPath_alias) {
		path_alias = aPath_alias;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String aLocation) {
		location = aLocation;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String aUser_id) {
		user_id = aUser_id;
	}

	public void setVersion(Long aVersion) {
		version = aVersion;
	}

	public void setLastUpdated(Date aLastUpdated) {
		lastUpdated = aLastUpdated;
	}

	public void setNsid(String aNsid) {
		nsid = aNsid;
	}

	public void setUsername(String aUsername) {
		username = aUsername;
	}

//	public Integer getFriend() {
//		return friend;
//	}
//
//	public void setFriend(Integer aFriend) {
//		friend = aFriend;
//	}
//
//	public Integer getFamily() {
//		return family;
//	}
//
//	public void setFamily(Integer aFamily) {
//		family = aFamily;
//	}



	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nsid == null) ? 0 : nsid.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
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
		FlickrFriend other = (FlickrFriend) obj;
		if (nsid == null) {
			if (other.nsid != null)
				return false;
		} else if (!nsid.equals(other.nsid))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FlickrFriend [lastUpdated=" + lastUpdated + ", id=" + id
				+ ", nsid=" + nsid + ", username=" + username + ", iconserver="
				+ iconserver + ", iconfarm=" + iconfarm + ", ignored="
				+ ignored + ", realname=" + realname + ", path_alias="
				+ path_alias + ", location=" + location + ", user_id="
				+ user_id + ", user=" + user + "]";
	}

	/**
	 * MinimalSocialServiceUser methods
	 * 
	 */


	@Override
	public String getImageURL() {
		String result;
		Integer i = Integer.parseInt(iconserver);
		if(i != null && i.intValue() > 0) {
			result = "http://farm"+iconfarm+".staticflickr.com/"+iconserver+"/buddyicons/"+nsid+".jpg";
		} else {
			result = "http://www.flickr.com/images/buddyicon.gif";
		}
		return result;
	}


	@Override
	public String getUserID() {
		return this.getNsid();
	}


	@Override
	public String getServiceName() {
		return "flickr";
	}


	@Override
	public String getLargeImageURL() {
		return getImageURL();
	}
	
	
	public String getFirstName() {
		return this.getRealname();
	}
	
	public String getLastName() {
		return this.getRealname();
	}

//	public void setOnBehalfOf(ServiceEntry entry) {
//		onBehalfOf = entry;
//	}
//	
//	public ServiceEntry getOnBehalfOf() {
//		return onBehalfOf;
//	}
	
}
