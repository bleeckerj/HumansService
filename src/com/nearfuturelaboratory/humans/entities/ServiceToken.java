package com.nearfuturelaboratory.humans.entities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.OutputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.scribe.model.Token;
import org.apache.log4j.Logger;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.PostPersist;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Serialized;
import org.mongodb.morphia.annotations.Transient;
import org.mongodb.morphia.utils.IndexDirection;

import com.nearfuturelaboratory.humans.util.PersistableToken;

@Entity("service_token")
public class ServiceToken extends BaseEntity {
	final static Logger logger = Logger.getLogger(com.nearfuturelaboratory.humans.entities.ServiceToken.class);
	private final static String key = "1234567890123456";

	@Serialized
	Token token;
	byte[] token_bytes;

	@Indexed(value = IndexDirection.ASC, name = "user_id", unique = true, dropDups = true)
	String user_id;
	String username;
	String servicename;
	//TODO Encrypt the Token
	@PrePersist void prePersist() {
		//OutputStream os = new ByteArrayOutputStream();
//		try {
//			token_bytes = this.EncryptByteArray(token_bytes);
//		} catch(Exception e) {
//			logger.error(e);
//		}
	}

	@PostPersist void postPersist() {
//		try {
//			token_bytes = this.DecryptByteArray(token_bytes);
//		} catch(Exception e) {
//			logger.error(e);
//		}
	}
	
	public Token getToken() {
		return token;
	}
	public void setToken(Token aToken) {
		token = aToken;
	}
	public String getUser_id() {
		return user_id;
	}
	public void setUser_id(String aUser_id) {
		user_id = aUser_id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String aUsername) {
		username = aUsername;
	}
	public String getServicename() {
		return servicename;
	}

	public void setServicename(String aServicename) {
		servicename = aServicename;
	}

	public byte[] getToken_bytes() {
		return token_bytes;
	}
	
	public void setToken_bytes(byte[] aToken_bytes) {
		token_bytes = aToken_bytes;
//		try {
//			InputStream buffer = new ByteArrayInputStream( aToken_bytes );
//			ObjectInput input = new ObjectInputStream ( buffer );
//			token = (Token) input.readObject();
//		} catch (IOException | ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

	}

	protected byte[] EncryptByteArray(byte[] array) throws Exception
	{
		Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
		
//		KeySpec spec = new PBEKeySpec(password, salt, 65536, 256);
//		SecretKey tmp = factory.generateSecret(spec);
//		SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
		
		aes.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key.getBytes(), "AES"));
		byte[] ciphertext = aes.doFinal("my cleartext".getBytes());
		return ciphertext;
	}
	
	protected byte[] DecryptByteArray(byte[] array) throws Exception {
		Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
		aes.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getBytes(), "AES"));
		byte[] ciphertext = aes.doFinal(array);
		return ciphertext;
		
	}

}
