package com.nearfuturelaboratory.humans.entities;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.PropertyConfigurator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Serialized;
import org.mongodb.morphia.query.QueryResults;
import org.mongodb.morphia.utils.IndexDirection;
import org.scribe.model.Token;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.nearfuturelaboratory.humans.dao.ServiceTokenDAO;
import com.nearfuturelaboratory.humans.entities.ServiceToken;
import com.nearfuturelaboratory.humans.util.MongoUtil;
import com.nearfuturelaboratory.util.Constants;

import static org.hamcrest.Matchers.equalTo;

import org.jasypt.util.binary.BasicBinaryEncryptor;

public class ServiceTokenTest {

	@Rule public ExpectedException thrown= ExpectedException.none();

	static ServiceTokenDAO dao_twitter;
	static ServiceTokenDAO dao_foursquare;
	static ServiceTokenDAO dao_instagram, dao_flickr;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		try {
			Constants.load("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/src/main/webapp/WEB-INF/lib/dev.app.properties");
			PropertyConfigurator.configureAndWatch("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/src/main/webapp/WEB-INF/lib/static-logger.properties");

			dao_twitter = new ServiceTokenDAO("twitter");
			dao_foursquare = new ServiceTokenDAO("foursquare");
			dao_instagram = new ServiceTokenDAO("instagram");
			dao_flickr = new ServiceTokenDAO("flickr");

			//logger.debug("Hey Ho!");
		} catch(Exception e) {
			e.printStackTrace();
		}


	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}


	//	@Test
	//	public void encryptAllServiceTokens() {
	//		DB db = MongoUtil.getMongo().getDB("twitter");
	//		DBCollection twitter_service_tokens = db.getCollection("service_token");
	//
	//		BasicDBObject obj = new BasicDBObject();
	//
	//		DBCursor cursor = twitter_service_tokens.find();
	//		DBObject key = null;
	//		if(cursor.hasNext()) {
	//			key = cursor.next();
	//			try {
	//				ServiceToken st_dummy = new ServiceToken();
	//				byte[] o = (byte[])key.get("token");
	//			
	//				ByteArrayOutputStream baos;// = new ByteArrayOutputStream();
	//				ObjectOutputStream oos;// = new ObjectOutputStream(baos);
	////				oos.writeObject(o);
	//
	//				byte[] token_bytes = o;//baos.toByteArray();
	//
	//				ByteArrayInputStream bais = new ByteArrayInputStream(token_bytes);
	//				ObjectInputStream ois = new ObjectInputStream(bais);
	//				Token token = (Token)ois.readObject();
	//				
	//				baos = new ByteArrayOutputStream();
	//				oos = new ObjectOutputStream(baos);
	//				oos.writeObject(token);
	//				token_bytes = st_dummy.EncryptByteArray(baos.toByteArray());
	//				key.put("token_bytes", token_bytes);
	//				twitter_service_tokens.save(key);
	//			} catch(Exception e) {
	//				e.printStackTrace();
	//				//logger.error(e);
	//			}
	//
	//		}
	//	}

	@Entity(value="service_token",noClassnameStored = true)
	class SimpleServiceToken {

		@Serialized
		Token token;
		byte[] token_bytes;

		@Indexed(value = IndexDirection.ASC, name = "user_id", unique = true, dropDups = true)
		String user_id;
		String username;
		String servicename;

	}

	@Ignore
	public void testSaveEncrypt() throws IOException, ClassNotFoundException {


		//List<ServiceToken> service_tokens = dao_twitter.find().asList();

		//		for(ServiceToken token : service_tokens) {

		
		List<ServiceToken> all = dao_foursquare.findAll();
		List<String>ids = new ArrayList();
		for(ServiceToken st : all) {
			ids.add(st.getUser_id());
		}
		for(String id : ids) {
			ServiceToken service_token = dao_foursquare.findByExactUserID(id);
			dao_foursquare.save(service_token);
			thrown.expect(StreamCorruptedException.class);
			ByteArrayInputStream bais = new ByteArrayInputStream(service_token.getToken_bytes());
			ObjectInputStream ois = new ObjectInputStream(bais);

			Token post_token = (Token)ois.readObject();
			fail("Somehow read encryptd access token as an actual instance of Token "+post_token);

		}
//		ServiceToken t = dao_twitter.findByExactUsername("darthjulian");
//		dao_twitter.save(t);
//		
//		t = dao_flickr.findByExactUsername("darthjulian");
//		dao_flickr.save(t);
//		
//		t = dao_instagram.findByExactUsername("fgirardin");
//		dao_instagram.save(t);
//
//		t = dao_foursquare.findByExactUsername("Fabien_Girardin");
//		dao_foursquare.save(t);
		
		//		}
	}

	@Test
	public void testLoadDecrypt() throws Exception {
		ServiceToken t = dao_twitter.findByExactUsername("darthjulian");
		Token pre_token = t.getToken();
		//		System.out.println(t.getServicename()+" "+t.getUser_id());
		//		System.out.println(pre_token);

		// decrypt the bytes
		byte[] token_bytes = t.getToken_bytes();
		ByteArrayInputStream bais = new ByteArrayInputStream(token_bytes);
		ObjectInputStream ois = new ObjectInputStream(bais);
		Token post_token = (Token)ois.readObject();

		//		System.out.println(post_token);

		assertThat(pre_token, equalTo(post_token));
	}

}
