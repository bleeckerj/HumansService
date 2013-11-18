package com.nearfuturelaboratory.humans.twitter.entities;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
//import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.annotations.Index;
//import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;

import com.nearfuturelaboratory.humans.entities.BaseEntity;

@Entity(value="follows",noClassnameStored = true)
@Indexes( @Index(name="follows_index", value="follower_id, user._id", unique=true/*, dropDups=true*/) )
public class TwitterFollows extends BaseEntity {


	@Embedded
	protected TwitterUser user;
	protected String follower_id;

	public TwitterFollows() {
		super();
	}

	public TwitterFollows(TwitterUser aTwitterUserBriefly) {
		super();
		user = aTwitterUserBriefly;
	}

	public TwitterUser getUser() {
		return user;
	}

	public void setUser(TwitterUser aUser) {
		user = aUser;
	}

	public String getFollower_id() {
		return follower_id;
	}

	public void setFollower_id(String aFollower_id) {
		follower_id = aFollower_id;
	}

}
