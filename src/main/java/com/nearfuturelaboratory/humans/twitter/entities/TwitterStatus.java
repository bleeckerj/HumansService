package com.nearfuturelaboratory.humans.twitter.entities;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Transient;
import org.mongodb.morphia.annotations.Version;
import org.mongodb.morphia.utils.IndexDirection;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nearfuturelaboratory.humans.service.status.ServiceStatus;


@Entity(value="status", noClassnameStored = true)
public class TwitterStatus extends ServiceStatus {
	final static Logger logger = Logger.getLogger(com.nearfuturelaboratory.humans.twitter.entities.TwitterStatus.class);
	//protected String id;
	@Version
	@Property ("version")
	private Long version;

	@Id
	@Property("status_id")
	protected String id;
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getStatusJSON().toString();
	}

	//protected String id_str;
	@Indexed(value = IndexDirection.ASC, name = "created_at", unique = false, dropDups = false)
	protected Date created_at;
	protected String text;
	protected String screen_name;

	@Embedded
	protected Coordinates coordinates;

	@Transient
	protected String service="twitter";
	
//	@Embedded
//	protected UserEntities entities;

	protected String favorite_count;
	protected boolean favorited;
	protected String in_reply_to_screen_name;
	protected String in_reply_to_status_id;
	protected String in_reply_to_status_id_str;
	protected String in_reply_to_user_id;
	protected String in_reply_to_user_id_str;
	protected String lang;

	@Embedded
	protected Places place;

	protected boolean possibly_sensitive;
	//protected Map scopes;
	protected String retweet_count;
	protected boolean retweeted;
	protected String source;
	protected boolean truncated;
	@Embedded
	protected TwitterUser user;
	protected boolean withheld_copyright;
	protected List<String>withheld_in_countries;
	protected String withheld_scope;
	
	protected Date lastUpdated;


	@PrePersist void prePersist() {
		lastUpdated = new Date();
	}

	public Date getLastUpdated() {
		if(lastUpdated == null) lastUpdated = new Date();
		return lastUpdated;
	}

	
	public Long getVersion() {
		return version;
	}
	public void setVersion(Long aVersion) {
		version = aVersion;
	}
	public String getId_str() {
		return user.getId_str();
	}
	public Date getCreated_at() {
		return created_at;
	}
	public void setCreated_at(Date aCreated_at) {
		created_at = aCreated_at;
	}
	public String getText() {
		return text;
	}
	public void setText(String aText) {
		text = aText;
	}
/**
	 * @return the screen_name
	 */
	public String getScreen_name() {
		return screen_name;
	}

	/**
	 * @param aScreen_name the screen_name to set
	 */
	public void setScreen_name(String aScreen_name) {
		screen_name = aScreen_name;
	}

	/**
	 * @return the user
	 */
	public TwitterUser getUser() {
		return user;
	}

	/**
	 * @param aUser the user to set
	 */
	public void setUser(TwitterUser aUser) {
		user = aUser;
	}

	//	public Coordinates getCoordinates() {
//		return coordinates;
//	}
//	public void setCoordinates(Coordinates aCoordinates) {
//		coordinates = aCoordinates;
//	}
//	public UserEntities getEntities() {
//		return entities;
//	}
//	public void setEntities(UserEntities aEntities) {
//		entities = aEntities;
//	}
	public String getFavorite_count() {
		return favorite_count;
	}
	public void setFavorite_count(String aFavorite_count) {
		favorite_count = aFavorite_count;
	}
	public boolean isFavorited() {
		return favorited;
	}
	public void setFavorited(boolean aFavorited) {
		favorited = aFavorited;
	}
	public String getIn_reply_to_screen_name() {
		return in_reply_to_screen_name;
	}
	public void setIn_reply_to_screen_name(String aIn_reply_to_screen_name) {
		in_reply_to_screen_name = aIn_reply_to_screen_name;
	}
	public String getIn_reply_to_status_id() {
		return in_reply_to_status_id;
	}
	public void setIn_reply_to_status_id(String aIn_reply_to_status_id) {
		in_reply_to_status_id = aIn_reply_to_status_id;
	}
	public String getIn_reply_to_status_id_str() {
		return in_reply_to_status_id_str;
	}
	public void setIn_reply_to_status_id_str(String aIn_reply_to_status_id_str) {
		in_reply_to_status_id_str = aIn_reply_to_status_id_str;
	}
	public String getIn_reply_to_user_id() {
		return in_reply_to_user_id;
	}
	public void setIn_reply_to_user_id(String aIn_reply_to_user_id) {
		in_reply_to_user_id = aIn_reply_to_user_id;
	}
	public String getIn_reply_to_user_id_str() {
		return in_reply_to_user_id_str;
	}
	public void setIn_reply_to_user_id_str(String aIn_reply_to_user_id_str) {
		in_reply_to_user_id_str = aIn_reply_to_user_id_str;
	}
	public String getLang() {
		return lang;
	}
	public void setLang(String aLang) {
		lang = aLang;
	}
	public Places getPlace() {
		return place;
	}
	public void setPlace(Places aPlace) {
		place = aPlace;
	}
	public boolean isPossibly_sensitive() {
		return possibly_sensitive;
	}
	public void setPossibly_sensitive(boolean aPossibly_sensitive) {
		possibly_sensitive = aPossibly_sensitive;
	}
	public String getRetweet_count() {
		return retweet_count;
	}
	public void setRetweet_count(String aRetweet_count) {
		retweet_count = aRetweet_count;
	}
	public boolean isRetweeted() {
		return retweeted;
	}
	public void setRetweeted(boolean aRetweeted) {
		retweeted = aRetweeted;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String aSource) {
		source = aSource;
	}
	public boolean isTruncated() {
		return truncated;
	}
	public void setTruncated(boolean aTruncated) {
		truncated = aTruncated;
	}
//	public TwitterUser getUser() {
//		return user;
//	}
//	public void setUser(TwitterUser aUser) {
//		user = aUser;
//	}
	public boolean isWithheld_copyright() {
		return withheld_copyright;
	}
	public void setWithheld_copyright(boolean aWithheld_copyright) {
		withheld_copyright = aWithheld_copyright;
	}
	public List<String> getWithheld_in_countries() {
		return withheld_in_countries;
	}
	public void setWithheld_in_countries(List<String> aWithheld_in_countries) {
		withheld_in_countries = aWithheld_in_countries;
	}
	public String getWithheld_scope() {
		return withheld_scope;
	}
	public void setWithheld_scope(String aWithheld_scope) {
		withheld_scope = aWithheld_scope;
	}

	public JsonObject getStatusJSON() {
		JsonObject obj = new Gson().toJsonTree(this, this.getClass()).getAsJsonObject();
		obj.addProperty("service", "twitter");
		return obj;
	}

	public long getCreated() {
		return this.getCreated_at().getTime();
	}

	
	
}


class Coordinates {
	List<Float> coordinates;
	String type;
	public List<Float> getCoordinates() {
		return coordinates;
	}
	public void setCoordinates(List<Float> aCoordinates) {
		coordinates = aCoordinates;
	}
	public String getType() {
		return type;
	}
	public void setType(String aType) {
		type = aType;
	}

	public Float getLat() {
		return coordinates.get(0);
	}
	
	public Float getLon() {
		return coordinates.get(1);
	}
	
}

class BoundingBox {
	protected List<List<List<Float>>> coordinates;
	protected String type;
	
}

class Places {
	//protected Map<String, String> attributes;
	
	protected BoundingBox bounding_box;
	
	protected String country;
	protected String country_code;
	protected String full_name;
	protected String id;
	protected String name;
	protected String place_type;
	protected String url;
	
//	public Map<String, String> getAttributes() {
//		return attributes;
//	}
//	public void setAttributes(Map<String, String> aAttributes) {
//		attributes = aAttributes;
//	}
//	public List<Coordinates> getBounding_box() {
//		return bounding_box;
//	}
//	public void setBounding_box(List<Coordinates> aBounding_box) {
//		bounding_box = aBounding_box;
//	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String aCountry) {
		country = aCountry;
	}
	public String getCountry_code() {
		return country_code;
	}
	public void setCountry_code(String aCountry_code) {
		country_code = aCountry_code;
	}
	public String getFull_name() {
		return full_name;
	}
	public void setFull_name(String aFull_name) {
		full_name = aFull_name;
	}
	public String getId() {
		return id;
	}
	public void setId(String aId) {
		id = aId;
	}
	public String getName() {
		return name;
	}
	public void setName(String aName) {
		name = aName;
	}
	public String getPlace_type() {
		return place_type;
	}
	public void setPlace_type(String aPlace_type) {
		place_type = aPlace_type;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String aUrl) {
		url = aUrl;
	}
}

