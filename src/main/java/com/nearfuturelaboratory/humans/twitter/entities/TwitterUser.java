package com.nearfuturelaboratory.humans.twitter.entities;

import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Version;

import com.nearfuturelaboratory.humans.entities.MinimalSocialServiceUser;

@Entity(value="user", noClassnameStored = true)
public class TwitterUser extends MinimalSocialServiceUser 
{
	final static Logger logger = LogManager.getLogger(com.nearfuturelaboratory.humans.twitter.entities.TwitterUser.class);
	@Version
	@Property ("version")
	private Long version;
	protected Date lastUpdated;

	@Id
	protected String id;
	
	protected boolean contributors_enabled;
	protected Date created_at;
	protected boolean default_profile;
	protected boolean default_profile_image;
	protected String description;
	
//	@Embedded
//	protected UserEntities entities;
	
	protected Integer favourites_count;
	protected boolean follow_request_sent;
	protected boolean following;
	protected Integer followers_count;
	protected Integer friends_count;
	protected boolean geo_enabled;
	protected String id_str;
	protected boolean is_translator;
	protected String lang;
	protected Integer listed_count;
	protected String location;
	protected String name;
//	protected String profile_background_color;
//	protected String profile_background_image_url;
//	protected String profile_background_image_url_https;
//	protected boolean profile_background_tile;
//	protected String profile_banner_url;
	protected String profile_image_url;
	protected String profile_image_url_https;
//	protected String profile_link_color;
//	protected String profile_sidebar_border_color;
//	protected String profile_sidebar_fill_color;
//	protected String profile_text_color;
//	protected boolean profile_use_background_image;
	@Property("protected")
	protected boolean _protected;
	protected String screen_name;
//	protected String name;
	protected boolean show_all_inline_media;
//	@Embedded
//	protected TwitterStatus status;
	protected Integer statuses_count;
	protected String time_zone;
	protected String url;
	protected Integer utc_offset;
	protected boolean verified;
	protected String withheld_in_countries;
	protected String withheld_scope;
	
	@PrePersist void prePersist() {
		lastUpdated = new Date();
	}

	public String getId() {
		return id;
	}
	public void setId(String aId) {
		id = aId;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TwitterUser [lastUpdated=" + lastUpdated + ", id=" + id
				+ ", location=" + location + ", profile_image_url="
				+ profile_image_url + ", screen_name=" + screen_name
				+ ", onBehalfOf=" + onBehalfOf + "]";
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
		TwitterUser other = (TwitterUser) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public boolean isContributors_enabled() {
		return contributors_enabled;
	}
	public void setContributors_enabled(boolean aContributors_enabled) {
		contributors_enabled = aContributors_enabled;
	}
	public Date getCreated_at() {
		return created_at;
	}
	public void setCreated_at(Date aCreated_at) {
		created_at = aCreated_at;
	}
	public boolean isDefault_profile() {
		return default_profile;
	}
	public void setDefault_profile(boolean aDefault_profile) {
		default_profile = aDefault_profile;
	}
	public boolean isDefault_profile_image() {
		return default_profile_image;
	}
	public void setDefault_profile_image(boolean aDefault_profile_image) {
		default_profile_image = aDefault_profile_image;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String aDescription) {
		description = aDescription;
	}
//	public UserEntities getEntities() {
//		return entities;
//	}
//	public void setEntities(UserEntities aEntities) {
//		entities = aEntities;
//	}
	public Integer getFavourites_count() {
		return favourites_count;
	}
	public void setFavourites_count(Integer aFavourites_count) {
		favourites_count = aFavourites_count;
	}
	public boolean isFollow_request_sent() {
		return follow_request_sent;
	}
	public void setFollow_request_sent(boolean aFollow_request_sent) {
		follow_request_sent = aFollow_request_sent;
	}
	public boolean isFollowing() {
		return following;
	}
	public void setFollowing(boolean aFollowing) {
		following = aFollowing;
	}
	public Integer getFollowers_count() {
		return followers_count;
	}
	public void setFollowers_count(Integer aFollowers_count) {
		followers_count = aFollowers_count;
	}
	public Integer getFriends_count() {
		return friends_count;
	}
	public void setFriends_count(Integer aFriends_count) {
		friends_count = aFriends_count;
	}
	public boolean isGeo_enabled() {
		return geo_enabled;
	}
	public void setGeo_enabled(boolean aGeo_enabled) {
		geo_enabled = aGeo_enabled;
	}
	public String getId_str() {
		return id_str;
	}
	public void setId_str(String aId_str) {
		id_str = aId_str;
	}
	public boolean isIs_translator() {
		return is_translator;
	}
	public void setIs_translator(boolean aIs_translator) {
		is_translator = aIs_translator;
	}
	public String getLang() {
		return lang;
	}
	public void setLang(String aLang) {
		lang = aLang;
	}
	public Integer getListed_count() {
		return listed_count;
	}
	public void setListed_count(Integer aListed_count) {
		listed_count = aListed_count;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String aLocation) {
		location = aLocation;
	}
	public String getName() {
		return name;
	}
	public void setName(String aName) {
		name = aName;
	}
//	public String getProfile_background_color() {
//		return profile_background_color;
//	}
//	public void setProfile_background_color(String aProfile_background_color) {
//		profile_background_color = aProfile_background_color;
//	}
//	public String getProfile_background_image_url() {
//		return profile_background_image_url;
//	}
//	public void setProfile_background_image_url(String aProfile_background_image_url) {
//		profile_background_image_url = aProfile_background_image_url;
//	}
//	public String getProfile_background_image_url_https() {
//		return profile_background_image_url_https;
//	}
//	public void setProfile_background_image_url_https(
//			String aProfile_background_image_url_https) {
//		profile_background_image_url_https = aProfile_background_image_url_https;
//	}
//	public boolean isProfile_background_tile() {
//		return profile_background_tile;
//	}
//	public void setProfile_background_tile(boolean aProfile_background_tile) {
//		profile_background_tile = aProfile_background_tile;
//	}
//	public String getProfile_banner_url() {
//		return profile_banner_url;
//	}
//	public void setProfile_banner_url(String aProfile_banner_url) {
//		profile_banner_url = aProfile_banner_url;
//	}
	public String getProfile_image_url() {
		return profile_image_url;
	}
	public void setProfile_image_url(String aProfile_image_url) {
		profile_image_url = aProfile_image_url;
	}
	public String getProfile_image_url_https() {
		return profile_image_url_https;
	}
	public void setProfile_image_url_https(String aProfile_image_url_https) {
		profile_image_url_https = aProfile_image_url_https;
	}
//	public String getProfile_link_color() {
//		return profile_link_color;
//	}
//	public void setProfile_link_color(String aProfile_link_color) {
//		profile_link_color = aProfile_link_color;
//	}
//	public String getProfile_sidebar_border_color() {
//		return profile_sidebar_border_color;
//	}
//	public void setProfile_sidebar_border_color(String aProfile_sidebar_border_color) {
//		profile_sidebar_border_color = aProfile_sidebar_border_color;
//	}
//	public String getProfile_sidebar_fill_color() {
//		return profile_sidebar_fill_color;
//	}
//	public void setProfile_sidebar_fill_color(String aProfile_sidebar_fill_color) {
//		profile_sidebar_fill_color = aProfile_sidebar_fill_color;
//	}
//	public String getProfile_text_color() {
//		return profile_text_color;
//	}
//	public void setProfile_text_color(String aProfile_text_color) {
//		profile_text_color = aProfile_text_color;
//	}
//	public boolean isProfile_use_background_image() {
//		return profile_use_background_image;
//	}
//	public void setProfile_use_background_image(
//			boolean aProfile_use_background_image) {
//		profile_use_background_image = aProfile_use_background_image;
//	}
	public boolean is_protected() {
		return _protected;
	}
	public void set_protected(boolean a_protected) {
		_protected = a_protected;
	}
	public String getScreen_name() {
		return screen_name;
	}
	public void setScreen_name(String aScreen_name) {
		screen_name = aScreen_name;
	}
	public boolean isShow_all_inline_media() {
		return show_all_inline_media;
	}
	public void setShow_all_inline_media(boolean aShow_all_inline_media) {
		show_all_inline_media = aShow_all_inline_media;
	}
//	public TwitterStatus getStatus() {
//		return status;
//	}
//	public void setStatus(TwitterStatus aStatus) {
//		status = aStatus;
//	}
	public Integer getStatuses_count() {
		return statuses_count;
	}
	public void setStatuses_count(Integer aStatuses_count) {
		statuses_count = aStatuses_count;
	}
	public String getTime_zone() {
		return time_zone;
	}
	public void setTime_zone(String aTime_zone) {
		time_zone = aTime_zone;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String aUrl) {
		url = aUrl;
	}
	public Integer getUtc_offset() {
		return utc_offset;
	}
	public void setUtc_offset(Integer aUtc_offset) {
		utc_offset = aUtc_offset;
	}
	public boolean isVerified() {
		return verified;
	}
	public void setVerified(boolean aVerified) {
		verified = aVerified;
	}
	public String getWithheld_in_countries() {
		return withheld_in_countries;
	}
	public void setWithheld_in_countries(String aWithheld_in_countries) {
		withheld_in_countries = aWithheld_in_countries;
	}
	public String getWithheld_scope() {
		return withheld_scope;
	}
	public void setWithheld_scope(String aWithheld_scope) {
		withheld_scope = aWithheld_scope;
	}
	public Long getVersion() {
		return version;
	}
	public void setVersion(Long aVersion) {
		version = aVersion;
	}
	public Date getLastUpdated() {
		return this.lastUpdated;
	}

	@Override
	public String getImageURL() {
		return this.getProfile_image_url();
	}

	@Override
	public String getUserID() {
		return this.getId();
	}

	@Override
	public String getUsername() {
		return this.getScreen_name();
	}

	@Override
	public String getServiceName() {
		return "twitter";
	}

	
	public String getFirstName() {
		return name;
	}
	
	public String getLastName() {
		return name;
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
	
}
