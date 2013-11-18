package com.nearfuturelaboratory.humans.instagram.entities;

import java.util.Date;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

import com.nearfuturelaboratory.humans.entities.BaseEntity;


@Entity(value="follows", noClassnameStored = true)
@Indexes( @Index(name="follows_index", value="follower_id, user_briefly.id, user_briefly.follower_id", unique=true) )
public class InstagramFollows  extends BaseEntity {
	
	protected Date lastUpdated;
	@PrePersist void prePersist() {
		lastUpdated = new Date();
	}

	
	@Embedded
	protected InstagramUserBriefly user_briefly;
	
	@Reference
	protected InstagramUser follower;
	
	protected String follower_id;

	protected InstagramFollows() {
		super();
	}
	
	public InstagramFollows(InstagramUserBriefly aInstagramUserBriefly) {
		super();
		user_briefly = aInstagramUserBriefly;
	}

	public InstagramUserBriefly getUser_briefly() {
		return user_briefly;
	}

	public void setUser_briefly(InstagramUserBriefly aUser_briefly) {
		user_briefly = aUser_briefly;
	}

	public String getFollower_id() {
		return follower_id;
	}

	public void setFollower_id(String aFollower_id) {
		follower_id = aFollower_id;
	}

	@Override
	public String toString() {
		return "InstagramFollows [lastUpdated=" + lastUpdated
				+ ", user_briefly=" + user_briefly + ", follower=" + follower
				+ ", follower_id=" + follower_id + "]";
	}
	
}

