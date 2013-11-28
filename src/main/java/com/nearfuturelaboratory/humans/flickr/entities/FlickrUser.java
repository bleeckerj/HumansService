package com.nearfuturelaboratory.humans.flickr.entities;

import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Version;

@Entity(value="user", noClassnameStored = true)
public class FlickrUser {
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


	final static Logger logger = Logger.getLogger(com.nearfuturelaboratory.humans.flickr.entities.FlickrUser.class);
	
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
