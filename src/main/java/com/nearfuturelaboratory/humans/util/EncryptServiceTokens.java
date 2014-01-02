package com.nearfuturelaboratory.humans.util;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

//import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scribe.model.Token;

import com.nearfuturelaboratory.humans.dao.ServiceTokenDAO;
import com.nearfuturelaboratory.humans.entities.ServiceToken;
import com.nearfuturelaboratory.util.Constants;

//import static com.jayway.restassured.RestAssured.given;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

public class EncryptServiceTokens {
	static ServiceTokenDAO dao_twitter;
	static ServiceTokenDAO dao_foursquare;
	static ServiceTokenDAO dao_instagram, dao_flickr;

	public EncryptServiceTokens() {
		// TODO Auto-generated constructor stub
	}

	
	@BeforeClass
	public static void beforeClass() {
		EncryptServiceTokens.main(null);
	}
	
	public static void main(String[] args) {
		try {
			Constants.load("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/src/main/webapp/WEB-INF/lib/dev.app.properties");
			//PropertyConfigurator.configureAndWatch("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/src/main/webapp/WEB-INF/lib/static-logger.properties");

			dao_twitter = new ServiceTokenDAO("twitter");
			dao_foursquare = new ServiceTokenDAO("foursquare");
			dao_instagram = new ServiceTokenDAO("instagram");
			dao_flickr = new ServiceTokenDAO("flickr");

			//logger.debug("Hey Ho!");
			
			EncryptServiceTokens encryptor = new EncryptServiceTokens();
			encryptor.encryptForInstagramServices();
			encryptor.encryptForFlickrServices();
			encryptor.encryptForFoursquareServices();
			encryptor.encryptForTwitterServices();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void encryptForFoursquareServices() throws Exception {
		List<ServiceToken> all = dao_foursquare.findAll();
		List<String>ids = new ArrayList<String>();
		for(ServiceToken st : all) {
			ids.add(st.getUser_id());
		}
		Token t = null;
		for(String id : ids) {
			ServiceToken service_token = dao_foursquare.findByExactUserID(id);
			
			service_token.setServicename("foursquare");
			t = service_token.getToken();
			
			assertThat(t, notNullValue());
			
			dao_foursquare.save(service_token);
			
			service_token = dao_foursquare.findByExactUserId(id);
			
			ByteArrayInputStream bais = new ByteArrayInputStream(service_token.getToken_bytes());
			ObjectInputStream ois = new ObjectInputStream(bais);

			Token post_token = (Token)ois.readObject();
			
			assertThat(post_token, equalTo(t));
			
			service_token = dao_foursquare.findByExactUserID(id);
			assertThat(service_token.getToken(), equalTo(t));
			
		}

	}
	@Test
	public void encryptForInstagramServices() throws Exception {
		List<ServiceToken> all = dao_instagram.findAll();
		List<String>ids = new ArrayList<String>();
		for(ServiceToken st : all) {
			ids.add(st.getUser_id());
		}
		Token t = null;
		for(String id : ids) {
			ServiceToken service_token = dao_instagram.findByExactUserID(id);
			
			service_token.setServicename("instagram");
			t = service_token.getToken();
			
			assertThat(t, notNullValue());
			
			dao_instagram.save(service_token);
			
			service_token = dao_instagram.findByExactUserId(id);
			
			ByteArrayInputStream bais = new ByteArrayInputStream(service_token.getToken_bytes());
			ObjectInputStream ois = new ObjectInputStream(bais);

			Token post_token = (Token)ois.readObject();
			
			assertThat(post_token, equalTo(t));
			
			service_token = dao_instagram.findByExactUserID(id);
			assertThat(service_token.getToken(), equalTo(t));	
		}
	}
	
	@Test
	public void encryptForFlickrServices() throws Exception {
		List<ServiceToken> all = dao_flickr.findAll();
		List<String>ids = new ArrayList<String>();
		for(ServiceToken st : all) {
			ids.add(st.getUser_id());
		}
		Token t = null;
		for(String id : ids) {
			ServiceToken service_token = dao_flickr.findByExactUserID(id);
			
			service_token.setServicename("flickr");
			t = service_token.getToken();
			
			assertThat(t, notNullValue());
			
			dao_flickr.save(service_token);
			
			service_token = dao_flickr.findByExactUserId(id);
			
			ByteArrayInputStream bais = new ByteArrayInputStream(service_token.getToken_bytes());
			ObjectInputStream ois = new ObjectInputStream(bais);

			Token post_token = (Token)ois.readObject();
			
			assertThat(post_token, equalTo(t));
			
			service_token = dao_flickr.findByExactUserID(id);
			assertThat(service_token.getToken(), equalTo(t));
		}
	}


	@Test
	public void encryptForTwitterServices() throws Exception {
		List<ServiceToken> all = dao_twitter.findAll();
		List<String>ids = new ArrayList<String>();
		for(ServiceToken st : all) {
			ids.add(st.getUser_id());
		}
		Token t = null;
		for(String id : ids) {
			ServiceToken service_token = dao_twitter.findByExactUserID(id);
			
			service_token.setServicename("twitter");
			t = service_token.getToken();
			
			assertThat(t, notNullValue());
			
			dao_twitter.save(service_token);
			
			service_token = dao_twitter.findByExactUserId(id);
			
			ByteArrayInputStream bais = new ByteArrayInputStream(service_token.getToken_bytes());
			ObjectInputStream ois = new ObjectInputStream(bais);

			Token post_token = (Token)ois.readObject();
			
			assertThat(post_token, equalTo(t));
			
			service_token = dao_twitter.findByExactUserID(id);
			assertThat(service_token.getToken(), equalTo(t));	
		}
	}
}
