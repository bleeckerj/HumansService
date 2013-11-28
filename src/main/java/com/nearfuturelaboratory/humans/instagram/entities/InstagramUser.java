package com.nearfuturelaboratory.humans.instagram.entities;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Version;
import org.mongodb.morphia.utils.IndexDirection;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.nearfuturelaboratory.humans.core.MinimalSocialServiceUser;
import com.nearfuturelaboratory.humans.entities.BaseEntity;

@Entity(value="user",noClassnameStored = true)
public class InstagramUser extends MinimalSocialServiceUser {
	@Version
	@Property ("version")
	private Long version;

	@Id
	protected String id;
	
	protected String username;
	protected String full_name;
	protected String profile_picture;
	protected String bio;
	protected String website;
	@Embedded
	protected Counts counts;

	protected Date lastUpdated;


	@PrePersist void prePersist() {
		lastUpdated = new Date();
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}


	
	
	
	public String getId() {
		return id;
	}

	public void setId(String aId) {
		id = aId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String aUsername) {
		username = aUsername;
	}

	public String getFull_name() {
		return full_name;
	}

	public void setFull_name(String aFull_name) {
		full_name = aFull_name;
	}

	public String getProfile_picture() {
		return profile_picture;
	}

	public void setProfile_picture(String aProfile_picture) {
		profile_picture = aProfile_picture;
	}

	public String getBio() {
		return bio;
	}

	public void setBio(String aBio) {
		bio = aBio;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String aWebsite) {
		website = aWebsite;
	}

	public Counts getCounts() {
		return counts;
	}

	public void setCounts(Counts aCounts) {
		counts = aCounts;
	}

	public String getMediaCount() {
		return counts.getMedia();
	}
	
	public String getFollowsCount() {
		return counts.getFollows();
	}
	
	public String getFollowedByCount() {
		return counts.getFollowedBy();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "InstagramUser [id=" + id + ", username=" + username
				+ ", profile_picture=" + profile_picture + ", lastUpdated="
				+ lastUpdated + "]";
	}

	@Override
	public String getImageURL() {
			return this.getProfile_picture();
	}

	@Override
	public String getUserID() {
		return this.getUserID();
	}

	@Override
	public String getFirstName() {
		return this.getFull_name();
	}

	@Override
	public String getLastName() {
		return this.getFull_name();

	}

	@Override
	public String getServiceName() {
		return "instagram";
	}

	@Override
	public String getLargeImageURL() {
		return this.getProfile_picture();
	}
}

class Counts {
	protected String media;
	protected String follows;
	protected String followed_by;
	protected String getMedia() {
		return media;
	}
	protected void setMedia(String aMedia) {
		media = aMedia;
	}
	protected String getFollows() {
		return follows;
	}
	protected void setFollows(String aFollows) {
		follows = aFollows;
	}
	protected String getFollowedBy() {
		return followed_by;
	}
	protected void setFollowedBy(String aFollowed_by) {
		followed_by = aFollowed_by;
	}
}