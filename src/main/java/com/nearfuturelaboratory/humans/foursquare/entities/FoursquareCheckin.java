package com.nearfuturelaboratory.humans.foursquare.entities;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Transient;
import org.mongodb.morphia.annotations.Version;
import org.mongodb.morphia.utils.IndexDirection;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nearfuturelaboratory.humans.service.status.ServiceStatus;

@Entity(value="checkin", noClassnameStored = true)
@Indexes( @Index(name="checkins_index", value="_id, user_id", unique=true/*, dropDups=true*/) )

public class FoursquareCheckin extends ServiceStatus {
	final static Logger logger = Logger.getLogger(com.nearfuturelaboratory.humans.foursquare.entities.FoursquareCheckin.class);
	@Version
	@Property ("version")
	private Long version;
	protected Date lastUpdated;

	@Transient
	protected String service="instagram";

	
	@Id
	protected String id;
	@Indexed(value=IndexDirection.ASC, name="user_id", unique=false, dropDups=false)
	protected String user_id;
	protected String username;
	
	protected String type;
	protected String timeZoneOffset;
	@Indexed(value=IndexDirection.ASC, name="created_at", unique=false, dropDups=false)
	protected Long createdAt;
	@Property("private")
	protected String _private;
	protected String shout;
	protected Map<String, String>source;
	@Embedded
	protected FoursquareFriend user;
	@Embedded
	protected FoursquareVenue venue;
	protected FoursquareLocation location;
	protected FoursquareEvent event;
	protected FoursquarePhoto photo;
	protected FoursquareUnit comment;
//	protected FoursquareLikes likes;
//	protected FoursquareUnit overlaps;
	@PrePersist void prePersist() {
		lastUpdated = new Date();
	}
	public String getUserID() {
		return user_id;
	}
	public void setUserID(String aUserID) {
		user_id = aUserID;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String aUsername) {
		username = aUsername;
	}
	
	public Date getCreatedAtDate() {
		return new java.util.Date(createdAt*1000l);
	}
	
	public Date getLastUpdated() {
		return lastUpdated;
	}
	public String getId() {
		return id;
	}
	public String getUser_id() {
		return user_id;
	}
	public String getType() {
		return type;
	}
	public String getTimeZoneOffset() {
		return timeZoneOffset;
	}
	public Long getCreatedAt() {
		return createdAt;
	}
	public String get_private() {
		return _private;
	}
	public String getShout() {
		return shout;
	}
	public Map<String, String> getSource() {
		return source;
	}
	public FoursquareFriend getUser() {
		return user;
	}
	public FoursquareVenue getVenue() {
		return venue;
	}
	public FoursquareLocation getLocation() {
		return location;
	}
	public FoursquareEvent getEvent() {
		return event;
	}
	public FoursquarePhoto getPhoto() {
		return photo;
	}
	public FoursquareUnit getComment() {
		return comment;
	}
	@Override
	public String toString() {
		return "FoursquareCheckin [id=" + id + ", user_id=" + user_id
				+ ", username=" + username + ", type=" + type
				+ ", timeZoneOffset=" + timeZoneOffset + ", createdAt="
				+ createdAt + " ("+getCreatedAtDate()+"), _private=" + _private + ", shout=" + shout
				+ ", source=" + source + ", user=" + user + ", venue=" + venue
				+ ", location=" + location + ", event=" + event + ", photo="
				+ photo + ", comment=" + comment + "]";
	}
	@Override
	public JsonObject getStatusJSON() {
		JsonObject obj = new Gson().toJsonTree(this, this.getClass()).getAsJsonObject();
		obj.addProperty("service", "foursquare");
		return obj;
	}
	@Override
	public long getCreated() {
		return this.createdAt * 1000L;
	}
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
		FoursquareCheckin other = (FoursquareCheckin) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}


}

class FoursquareUnit {
	Integer count;
	String name;
	String type;
	List<FoursquareItem> items;
	public Integer getCount() {
		return count;
	}
	public List<FoursquareItem> getItems() {
		return items;
	}
}

class FoursquareEvent {
	String id;
	String name;
	public String getId() {
		return id;
	}
	public String getName() {
		return name;
	}
}
