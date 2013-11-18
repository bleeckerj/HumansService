package com.nearfuturelaboratory.humans.instagram.entities;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.PostLoad;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Transient;
import org.mongodb.morphia.annotations.Version;
import org.mongodb.morphia.utils.IndexDirection;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nearfuturelaboratory.humans.dao.InstagramUserDAO;
import com.nearfuturelaboratory.humans.service.status.ServiceStatus;


@Entity(value = "status", noClassnameStored = true)
public class InstagramStatus /*extends BaseEntity*/ extends ServiceStatus {
	final static Logger logger = Logger.getLogger(com.nearfuturelaboratory.humans.instagram.entities.InstagramStatus.class);

	//	@Indexed(value = IndexDirection.ASC, name = "uniq_status_id", unique = true, dropDups = true)
	//	@Id
	//	@Property("status_id")
	
	@Version
	@Property ("version")
	private Long version;

	
	@Id
	protected String id;
	@Indexed(value = IndexDirection.ASC, name = "created_time", unique = false, dropDups = false)
	protected Long created_time;

	@Embedded
	protected Map<String, Image> images;

	@Embedded
	protected Caption caption;

	@Embedded
	protected User user;

	/**
	 * This user (above) that comes back from the server during service requests for status
	 * doesn't seem to contain data for some of the adornment fields, like bio and website
	 * Not that these are critical, but..we can @PostLoad these from the user database, as the
	 * user with this status should (must?) exist in our instagram.user collection
	 */
	@Transient
	protected InstagramUser transient_instagram_user;
	
	protected List<String> tags;

	protected Map<String, String>location;


	protected String link;

	protected String user_has_liked;
	protected String type;

	protected Likes likes;

	protected Comments comments;
	protected String filter;
	protected String attribution;
	//protected List<UserInPhoto> users_in_photo;

	public static final String STANDARD_RESOLUTION = "standard_resolution";
	public static final String THUMBNAIL_RESOLUTION = "thumbnail_resolution";
	public static final String LOW_RESOLUTION = "low_resolution";

	@PostLoad void postLoad() {
		InstagramUserDAO dao = new InstagramUserDAO();
		transient_instagram_user = dao.findByExactUserID(user.id);
	}
	
	protected Date lastUpdated;

	

	@PrePersist void prePersist() {
		lastUpdated = new Date();
	}
	

	public Date getLastUpdated() {
		if(lastUpdated == null) lastUpdated = new Date();
		return lastUpdated;
	}


	public String getId() {
		return id;
	}
	public void setId(String aId) {
		id = aId;
	}
	public String getStatusId() {
		return id;
	}
	public void setStatusId(String aId) {
		id = aId;
	}

	//	public String getStandardResolution() {
	//		String result = null;
	//		if(images != null) {
	//			Image image = images.get(STANDARD_RESOLUTION);
	//			result = image.url;
	//		}
	//		return result;
	//	}
	//	
	public Map<String, Image> getImages() {
		return images;
	}
	public void setImages(Map<String, Image> aImages) {
		images = aImages;
	}
	public Caption getCaption() {
		return caption;
	}
	public void setCaption(Caption aCaption) {
		caption = aCaption;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User aUser) {
		user = aUser;
	}
	public Long getCreated_time() {
		return created_time;
	}
	public void setCreated_time(Long  aCreated_time) {
		created_time = aCreated_time;
	}
	public List<String> getTags() {
		return tags;
	}
	public void setTags(List<String> aTags) {
		tags = aTags;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String aLink) {
		link = aLink;
	}
	public String getType() {
		return type;
	}
	public void setType(String aType) {
		type = aType;
	}
	public String getAttribution() {
		return attribution;
	}
	public void setAttribution(String aAttribution) {
		attribution = aAttribution;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long aVersion) {
		version = aVersion;
	}


	public JsonObject getStatusJSON() {
		JsonObject obj = new Gson().toJsonTree(this, this.getClass()).getAsJsonObject();
		obj.addProperty("service", "instagram");
		return obj;
	}


	public long getCreated() {
		return this.created_time*1000L;
	}


//	@Override
//	public int compareTo(ServiceStatus aO) {
//		Date otherDate = aO.getCreatedDate();
//		//otherDate.setTime(aO.getCreated()*1000l);
//		return otherDate.compareTo(this.getCreatedDate());
//
//	}

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
		InstagramStatus other = (InstagramStatus) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}

class UserInPhoto {
	Position position; // y, x
	BriefUser user;
}

class Position {
	Double x, y;
}


class Comments {
	Integer count;
	List<CommentData> data;
}

class CommentData {
	String id;
	String text;
	Long created_time;
	BriefUser from;
}


class Location {
	Double latitude, longitude;
	String name;
	public Double getLatitude() {
		return latitude;
	}
	public void setLatitude(Double aLatitude) {
		latitude = aLatitude;
	}
	public Double getLongitude() {
		return longitude;
	}
	public void setLongitude(Double aLongitude) {
		longitude = aLongitude;
	}
	public String getName() {
		return name;
	}
	public void setName(String aName) {
		name = aName;
	}

}


class Likes {
	Integer count;
	List<Liker> data;

}

class Liker {
	String id;
	String profile_picture;
	String username;
	String full_name;
}

class BriefUser {
	String id;
	String profile_picture;
	String username;
	String full_name;
}

class User {
	@Indexed(value = IndexDirection.ASC, name="user.id", unique = false, dropDups = false)
	String id;
	String profile_picture;
	@Indexed(value = IndexDirection.ASC, name="user.username", unique = false, dropDups = false)
	String username;
	String bio;
	String website;
	String full_name;
	@Override
	public String toString() {
		return "User [id=" + id + ", profile_picture=" + profile_picture
				+ ", username=" + username + ", bio=" + bio + ", website="
				+ website + ", full_name=" + full_name + "]";
	}
}


class Image {
	//Map<String, Map<String, String>> images;
	Integer height, width;
	String url;
}

class Caption {

	String id;
	String text;
	Map<String, String> from;
	protected Long created_time;
	//String created_time;

	protected String getId() {
		return id;
	}
	protected void setId(String aId) {
		id = aId;
	}
	protected String getText() {
		return text;
	}
	protected void setText(String aText) {
		text = aText;
	}
	protected Map<String, String> getFrom() {
		return from;
	}
	protected void setFrom(Map<String, String> aFrom) {
		from = aFrom;
	}
	protected Long getCreated_time() {
		return created_time;
	}
	protected void setCreated_time(Long aCreated_time) {
		created_time = aCreated_time;
	}


}