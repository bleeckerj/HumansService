package com.nearfuturelaboratory.humans.test;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

@Entity
public class Person {

	@Id ObjectId id;
	
	String name;
	
	@Indexed
	Integer height;
	
}
