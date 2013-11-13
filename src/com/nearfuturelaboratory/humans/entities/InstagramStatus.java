package com.nearfuturelaboratory.humans.entities;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.apache.log4j.Logger;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.utils.IndexDirection;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.nearfuturelaboratory.humans.entities.BaseEntity;

@Entity("status")
public class InstagramStatus /*extends BaseEntity*/ {
	final static Logger logger = Logger.getLogger("com.nearfuturelaboratory.humans.entities.InstagramStatus.class");

	//	@Indexed(value = IndexDirection.ASC, name = "uniq_status_id", unique = true, dropDups = true)
	//	@Id
	//	@Property("status_id")
	@Id
	protected String id;
	@Indexed(value = IndexDirection.ASC, name = "created_time", unique = false, dropDups = false)
	protected String created_time;

	@Embedded
	protected Map<String, Image> images;

	@Embedded
	protected Caption caption;

	@Embedded
	protected User user;

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
	public String getCreated_time() {
		return created_time;
	}
	public void setCreated_time(String  aCreated_time) {
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
	String created_time;
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
	protected String created_time;
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
	protected String getCreated_time() {
		return created_time;
	}
	protected void setCreated_time(String aCreated_time) {
		created_time = aCreated_time;
	}


}