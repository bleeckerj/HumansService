package com.nearfuturelaboratory.humans.entities;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Property;


@Entity("userfollows")
//@Indexes( @Index("id, follower_id") )
public class InstagramFollows  extends BaseEntity {
	
	@Embedded
	protected InstagramUserBriefly user_briefly;
	
	protected InstagramFollows() {
		super();
	}
	
	public InstagramFollows(InstagramUserBriefly aInstagramUserBriefly) {
		super();
		user_briefly = aInstagramUserBriefly;
	}
	
}

