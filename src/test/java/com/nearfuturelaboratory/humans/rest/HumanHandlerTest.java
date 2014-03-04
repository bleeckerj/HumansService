package com.nearfuturelaboratory.humans.rest;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.JsonObject;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.specification.RequestSpecification;
import com.nearfuturelaboratory.humans.entities.Human;
import com.nearfuturelaboratory.humans.entities.HumansUser;

import static com.jayway.restassured.RestAssured.when;
import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsArrayContaining.hasItemInArray;
//import static org.junit.Assert.assertThat;


public class HumanHandlerTest {
	static RequestSpecification spec;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		RestAssured.baseURI  = "http://localhost";
//		RestAssured.port     = 8080;
//		RestAssured.basePath = "/rest";
		RestAssured.port     = 8443;
		RestAssured.basePath = "/rest";
		RestAssured.keystore("/Users/julian/Documents/workspace/HumansService/src/main/resources/truststore.jks", "thisisit");
		//spec = new RequestSpecBuilder().setSessionId(sessionId).build();
		spec = new RequestSpecBuilder().addQueryParam("access_token", "aa9c6f8ae7341c0380007062280b4b7a").build();

	}

	@Test
	//@JsonIgnoreProperties(ignoreUnknown=true)
	public void testGetHumanByID() {

		HumansUser aUser = get("/login?username=darthjulian&password=darthjulian").as(HumansUser.class);
		List<Human> humans = aUser.getAllHumans();
		Human human = humans.get(0);
		
		Human human_req = given().spec(spec).get("/human/get/"+human.getId()).as(Human.class);
		
		assertThat(human_req, equalTo(human));		
	}

	@Test
	public void testGetHuman() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetStatusByID() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetStatusStringHttpServletRequestHttpServletResponse() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetStatusStringStringHttpServletRequestHttpServletResponse() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdateServiceUserFromHuman() {
		fail("Not yet implemented");
	}

	@Test
	public void testRemoveServiceUserFromHuman() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddServiceUserToHuman() {
		fail("Not yet implemented");
	}

}
