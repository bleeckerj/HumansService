package com.nearfuturelaboratory.humans.rest;

import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.bson.types.ObjectId;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.authentication.CertificateAuthSettings;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.ResponseBody;
import com.jayway.restassured.specification.RequestSpecification;

import static com.jayway.restassured.RestAssured.expect;

import com.nearfuturelaboratory.humans.dao.HumansUserDAO;
import com.nearfuturelaboratory.humans.entities.Human;
import com.nearfuturelaboratory.humans.entities.HumansUser;
import com.nearfuturelaboratory.humans.entities.ServiceEntry;
import com.nearfuturelaboratory.humans.entities.ServiceUser;
import com.nearfuturelaboratory.humans.util.MyObjectIdSerializer;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNot.not;

public class UserHandlerTest {

	static RequestSpecification spec;
	HumansUserDAO test_dao = new HumansUserDAO("test_dao");
	String access_token;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		RestAssured.baseURI  = "https://localhost";
		RestAssured.port     = 8443;
		RestAssured.basePath = "/rest";
		RestAssured.keystore("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/src/main/resources/truststore.jks", "thisisit");
//		String sessionId = get("/login?username=darthjulian&password=darthjulian").sessionId();
//		RestAssured.sessionId = sessionId;
		spec = new RequestSpecBuilder().addQueryParam("access_token", "aa9c6f8ae7341c0380007062280b4b7a").build();
//				
//				setSessionId(sessionId).build();
	}
	
	@Test
	public void testRemoveService() {
		Gson gson = new Gson();

		HumansUser user = given().spec(spec).get("/user/get/").as(HumansUser.class);
		assertThat(user.getUsername(), equalTo("darthjulian"));
		
		ServiceEntry service_foo = new ServiceEntry("fee", "fi", "foo");
		ServiceEntry service_bar = new ServiceEntry("bee", "bi", "bar");

		user.addService(service_foo);
		user.addService(service_bar);
		
		Human h = new Human();
		h.addServiceUser(new ServiceUser("faa", "foo", "fam", "furl", service_foo));
		h.setName("Test Remove Service");
		h.addServiceUser(new ServiceUser("baa", "boo", "bam", "burl", service_bar));
		
		//user.addHuman(h);
		given()
		.request().spec(spec).contentType(ContentType.JSON).body(gson.toJson(h))
		.when()
		.post("/user/add/human")
		.then()
		.body("result", equalTo("success"));
		
		
		expect().statusCode(HttpStatus.SC_OK)
		.given().spec(spec).contentType(ContentType.JSON).body(gson.toJson(service_foo).toString())
		.post("/user/rm/service");
		
/*		given()
		.request().spec(spec).contentType(ContentType.JSON).body(gson.toJson(user).toString())
		.when()
		.post("/user/update");
*/		
	}

	
	@Test
	public void testAddNewHuman() {
		HumansUser user = given().spec(spec).get("/user/get/").as(HumansUser.class);
		assertThat(user.getUsername(), equalTo("darthjulian"));
		
		ServiceEntry service_foo = new ServiceEntry("fee", "fi", "foo");
		ServiceEntry service_bar = new ServiceEntry("bee", "bi", "bar");
//
//		user.addService(service_foo);
//		user.addService(service_bar);
		Gson gson = new Gson();
		
		Human h = new Human();
		h.addServiceUser(new ServiceUser("faa", "foo", "fam", "furl", service_foo));
		h.setName("Test Remove Service");
		h.addServiceUser(new ServiceUser("baa", "boo", "bam", "burl", service_bar));
		
		given()
		.request().spec(spec).contentType(ContentType.JSON).body(gson.toJson(h))
		.when()
		.post("/user/add/human")
		.then()
		.body("result", equalTo("success"));
		
	}

	
	@Test
	public void testGetUser() {
		HumansUser user = given().spec(spec).get("/user/get/").as(HumansUser.class);
		//System.out.println(user);
		assertThat(user.getUsername(), equalTo("darthjulian"));
		HumansUser logged_in = given().spec(spec).get("/login?username=darthjulian&password=darthjulian").as(HumansUser.class);
		//logged_in.setPassword(null);
		//System.out.println(logged_in);
		assertThat(user, equalTo(logged_in));
	}

	@Ignore
	public void testRemoveHuman() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testRemoveServiceUser() {
		HumansUser user = given().spec(spec).get("/user/get/").as(HumansUser.class);
		//System.out.println(user);
		assertThat(user.getUsername(), equalTo("darthjulian"));

		// add a human that has a service user
		ServiceEntry service = new ServiceEntry("test_id", "fancyguy1010", "Quankr");
		ServiceUser service_user = new ServiceUser("983939", "mack", service.getServiceName(), "http://baz.com", service);

		///////////////user.addService(service_entry);
		
		Human human = new Human();
		human.setName("addService");
		human.addServiceUser(service_user);

		Gson gson = new Gson();
		
		given()
		.request().spec(spec).contentType(ContentType.JSON).body(gson.toJson(human).toString())
		.when()
		.post("/user/add/human")
		.then()
		.body("result", equalTo("success"));		
		
		HumansUser logged_in = given().spec(spec).get("/login?username=darthjulian&password=darthjulian").as(HumansUser.class);
		List<ServiceUser> service_users = logged_in.getServiceUsersRelyingOn(service);
		
		assertThat(service_users, hasItem(service_user));
		assertThat(service_users, hasSize(1));
		// get the id of the service user

		ObjectId id = service_users.get(0).getId();

		// remove it
		ResponseBody body = 
		given()
		.request().spec(spec).contentType(ContentType.JSON)
		.when()
		.get("/user/rm/"+id.toString()+"/serviceuser").getBody();
			
		logged_in = given().spec(spec).get("/login?username=darthjulian&password=darthjulian").as(HumansUser.class);
		service_users = logged_in.getServiceUsersRelyingOn(service);
		
		assertThat(service_users, not(hasItem(service_user)));
		assertThat(service_users, hasSize(0));

		// make sure that any human that got its service user(s) removed isn't left empty of anything
		// that is, if we remove servivc users, we should also delete humans that find themselves without
		// any service users/friends.
		List<Human>humans = logged_in.getAllHumans();
		for(Human h : humans) {
			assertThat(h.getServiceUsers(), not(hasSize(0)));
		}
		
	}
	
	
	@Test
	public void testUpdateUser() {
		JsonObject req = new JsonObject();
		req.addProperty("email", "test-user@gmail.com");
		// if we change the password *really* then the next time this unit test class runs
		// it won't have the new password, won't be able to login in the top setup class method
		// and then won't get a session id and then will explode all over itself
		req.addProperty("password", "darthjulian"); // need to paramaterize this stuff better
		String str = req.toString();
		
		given()
		.request().spec(spec).contentType(ContentType.JSON).body(str)
		.when()
		.post("/user/update")
		.then()
		.body("email", equalTo(req.get("email").getAsString()));
		
		//"username", equalTo(req.get("username").getAsString()), 
		//HumansUser logged_in = given().spec(spec).get("/login?username=darthjulian&password="+req.get("password")).as(HumansUser.class);
		
		given()
		.request().spec(spec).contentType(ContentType.JSON)
		.when()
		.get("/login?username=darthjulian&password="+req.get("password"))
		.body()
		.path("email", req.get("email").getAsString());

		
		
		
	}

	@Test
	public void testCreateNewUser() {
		JsonObject req = new JsonObject();
		req.addProperty("username", "test-user");
		req.addProperty("email", "test-user@gmail.com");
		req.addProperty("password", "password");

		String str = req.toString();
		
		given()
		.request().spec(spec).contentType(ContentType.JSON).body(str)
		.when()
		.post("/user/new")
		.then()
		.body("username", equalTo(req.get("username").getAsString()), "email", equalTo(req.get("email").getAsString()));
		//.body("email", req.get("email").toString());
		
/*		expect().statusCode(200)
		.given()
		.spec(spec).contentType(ContentType.JSON)
		.with()
		.body(str)
		.when()
		.post("/user/new");
*/
		
		
	}
	
	@Test
	public void testUsernameExists() {
		//JsonElement response = get("/username/exists")
		JsonObject req = new JsonObject();
		req.addProperty("check_username", "thisguydoesntexistwhatsoeverandreallynevershould");
		String str = req.toString();

//		ResponseBody body = given().spec(spec).contentType(ContentType.JSON).with().body(str).post("/user/username/exists").andReturn().body();
//		System.out.println(body.toString());

		expect().statusCode(200)
		.given()
		.spec(spec).contentType(ContentType.JSON)
		.with()
		.body(str)
		.when()
		.post("/user/username/exists");

//		String json;
//		json = given().spec(spec).contentType(ContentType.JSON).with().body(str).post("/user/username/exists").andReturn().asString();
//		System.out.println(json);
		given()
		.request().spec(spec).contentType(ContentType.JSON).body(req.toString())
		.when()
		.post("/user/username/exists")
		.then()
		.body("exists", equalTo(Boolean.FALSE));

		req = new JsonObject();
		req.addProperty("check_username", "darthjulian");
		str = req.toString();
		
		given()
		.spec(spec).contentType(ContentType.JSON).body(req.toString())
		.when()
		.post("/user/username/exists")
		.then()
		.body("exists", equalTo(Boolean.TRUE));

	}



	@Test
	public void testGetAllHumans() {
		//Gson gson = new Gson();
		Gson in_gson = new GsonBuilder().
				setExclusionStrategies(new HumanJsonExclusionStrategy()).registerTypeAdapter(ObjectId.class, new MyObjectIdSerializer()).create();

				

		Type htype = new TypeToken<List<Human>>() {}.getType();
//		CertificateAuthSettings cert = new CertificateAuthSettings();   //java.security.KeyStore.getDefaultType(), 443, null, false);
//		cert.checkServerHostname(false);
//		cert.port(8443);
		//spec.auth().certificate("file:///Volumes/Slippy/Users/julian/Documents/workspace/HumansService/src/main/resources/truststore.jks", "thisisit", cert);
		
		JsonPath json = given().spec(spec).get("/user/get/humans").jsonPath();
		
		String json_str = given().spec(spec).get("/user/get/humans").asString();
		
		List<Human> foo = in_gson.fromJson(json_str, htype);
		//		List<HashMap> humans_2 = json.getList("", HashMap.class);
		//		System.out.println(humans_2);
		//		System.out.println(humans_2.size());
		//		System.out.println(humans_2.get(0).get("humanid"));
		//		System.out.println(humans_2.get(0).get("serviceUsers"));
		//		Type type = new TypeToken<List<ServiceUser>>() {}.getType();

//		List<Human> hu = json.getList("");
//		List<Human> obj_list = json.getList("");
		
//		Gson out_gson = new GsonBuilder().
//				setExclusionStrategies(new HumanJsonExclusionStrategy()).
//				registerTypeAdapter(ObjectId.class, new MyObjectIdSerializer()).create();
		
		//String elem = gson.toJson(obj_list.get(0));
		

		
		//String s = json.get("[0]").toString();
		//Human h = gson.fromJson(obj_list.get(0).toString(), Human.class);
		//Human h = gson.fromJson(json., classOfT)
		//System.out.println(h);
		
		assertThat(foo, is(is(is(notNullValue()))));
		//assertThat(h, instanceOf(Human.class));
		//assertThat(obj_list.get(0), is(htype));
		//assertThat(obj_list.get(0), instanceOf(Human.class));
//		System.out.println("hu="+hu);
//		System.out.println("obj="+obj_list.get(0));
//
//		System.out.println("hu(1)="+hu.get(0));

		/*		List<ServiceUser> su = json.getList("serviceUsers");
		System.out.println("su="+su);
		System.out.println("gson.toJson(humans_2.get(0)="+gson.toJson(humans_2.get(0)));
		System.out.println("humans_2.get(0)="+humans_2.get(0));
		 *///		assertThat(humans_2, hasSize(1));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetFriends() {

		JsonPath json = given().spec(spec).get("/user/friends/get").jsonPath();
		//		System.out.println(given().spec(spec).get("/user/friends/get").toString());

		//json.getObject(path, objectType)
		//		String foo = given().spec(spec).get("/user/friends/get").toString();
		//		System.out.println(foo);
		List<String> services = json.get("service");
		//		System.out.println(json.get("service"));
		//String[] s = { "twitter", "flickr", "instagram", "foursquare", "tumblr" };

		assertThat(services, hasItems(equalTo("twitter"),equalTo("foursquare"), equalTo("instagram"), equalTo("flickr")));

	}

}
