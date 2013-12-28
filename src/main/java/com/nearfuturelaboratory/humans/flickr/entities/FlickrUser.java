package com.nearfuturelaboratory.humans.flickr.entities;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Version;

import com.nearfuturelaboratory.humans.core.MinimalSocialServiceUser;

@Entity(value="user", noClassnameStored = true)
public class FlickrUser extends MinimalSocialServiceUser {
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		FlickrUser other = (FlickrUser) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}


	final static Logger logger = LogManager.getLogger(com.nearfuturelaboratory.humans.flickr.entities.FlickrUser.class);
	
	@Version
	@Property ("version")
	private Long version;
	protected Date lastUpdated;

	@Id
	protected String id;
	
	protected Map<String, String> username;
	protected Map<String, String> realname;
	protected Map<String, String> location;
	protected Map<String, String> timezone;
	protected String nsid;
	protected int iconserver;
	protected int iconfarm;
	protected String path_alias;
	/**
	 * firstdatetaken: {
	 *  _content: "1904-01-01 00:00:00:
	 *  },
	 *  firstdate : {
	 *  _content: "1104731550"
	 *  },
	 *  count : {
	 *  _content: "11529"
	 *  },
	 *  views: {
	 *  _content: "196451"
	 *  }
	 */
	protected Map<String, Map<String, String>> photos;
//	protected Map<String, String> photosurl;
	
	
	@PrePersist void prePersist() {
		lastUpdated = new Date();
	}


	public String getId() {
		return id;
	}


	public void setId(String aId) {
		id = aId;
	}


	public Long getVersion() {
		return version;
	}


	public Date getLastUpdated() {
		return lastUpdated;
	}


	public String getUsername() {
		String result = null;
		if(username != null) {
			result = username.get("_content");
		}
		return result;
	}


	@Override
	public String getImageURL() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getUserID() {
		return this.getId();
	}


	@Override
	public String getFirstName() {
		return this.realname.get("_content");
	}


	@Override
	public String getLastName() {
		return this.realname.get("_content");
	}


	@Override
	public String getServiceName() {
		return "flickr";
	}


	@Override
	public String getLargeImageURL() {
		String str = "http://farm%s.staticflickr.com/%s/buddyicons/%s.jpg";
		String urlStr = String.format(str, iconfarm, iconserver, nsid);
		logger.debug("url string for buddy icon is "+urlStr);
		return null;
	}


//	public String getRealname() {
//		return realname;
//	}
//
//
//	public String getLocation() {
//		return location;
//	}


//	public String getTimezone() {
//		return timezone;
//	}


	
	
}
