package com.nearfuturelaboratory.humans.instagram.entities;

public class InstagramUserBriefly {

	protected String id;
	protected String profile_picture;
	protected String username;
	protected String bio;
	protected String website;
	protected String full_name;
	protected String follower_id;

	public String getId() {
		return id;
	}

	public String getProfile_picture() {
		return profile_picture;
	}
	public void setProfile_picture(String aProfile_picture) {
		profile_picture = aProfile_picture;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String aUsername) {
		username = aUsername;
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
	public String getFull_name() {
		return full_name;
	}
	public void setFull_name(String aFull_name) {
		full_name = aFull_name;
	}
	public String getFollower_id() {
		return follower_id;
	}
	public void setFollower_id(String aFollower_id) {
		follower_id = aFollower_id;
	}

	@Override
	public String toString() {
		return "InstagramUserBriefly [id=" + id + ", profile_picture="
				+ profile_picture + ", username=" + username + ", bio=" + bio
				+ ", website=" + website + ", full_name=" + full_name
				+ ", follower_id=" + follower_id + "]";
	}



}