package com.nearfuturelaboratory.humans.entities;

import com.nearfuturelaboratory.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jasypt.util.binary.BasicBinaryEncryptor;
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.utils.IndexDirection;
import org.scribe.model.Token;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

@Entity(value = "service_token", noClassnameStored = true)
public class ServiceToken extends BaseEntity {
	final static Logger logger = LogManager
			.getLogger(com.nearfuturelaboratory.humans.entities.ServiceToken.class);

	@Transient
	Token token;
	byte[] token_bytes;

	@Indexed(value = IndexDirection.ASC, name = "user_id", unique = true, dropDups = true)
	String user_id;
	String username;
	String servicename;

	// TODO Encrypt the Token

	@PrePersist
	void prePersist() {
		// OutputStream os = new ByteArrayOutputStream();
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(token);
			token_bytes = this.EncryptByteArray(baos.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
	}

	@PostPersist
	void postPersist() {
	}

	@PostLoad
	void postLoad() {
		try {
			token_bytes = this.DecryptByteArray(token_bytes);
			ObjectInputStream ois = new ObjectInputStream(
					new ByteArrayInputStream(token_bytes));
			token = (Token) ois.readObject();
		} catch (Exception e) {
			// e.printStackTrace();
			logger.error(
					"The token bytes probably do not represent an encrypted token!",
					e);
		}
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
		// try {
		// InputStream buffer = new ByteArrayInputStream( aToken_bytes );
		// ObjectInput input = new ObjectInputStream ( buffer );
		// token = (Token) input.readObject();
		// } catch (IOException | ClassNotFoundException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

	protected byte[] EncryptByteArray(byte[] array) throws Exception {
		BasicBinaryEncryptor binaryEncryptor = new BasicBinaryEncryptor();
		binaryEncryptor.setPassword(Constants.getString("TOKEN_PW"));
		byte[] myEncryptedBytes = binaryEncryptor.encrypt(array);
		return myEncryptedBytes;
	}

	protected byte[] DecryptByteArray(byte[] array) throws Exception {
		BasicBinaryEncryptor binaryEncryptor = new BasicBinaryEncryptor();
		binaryEncryptor.setPassword(Constants.getString("TOKEN_PW"));
		// byte[] myEncryptedBytes = binaryEncryptor.encrypt(array);
		byte[] plainBytes = binaryEncryptor.decrypt(array);
		/*
		 * Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
		 * aes.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getBytes(),
		 * "AES")); byte[] ciphertext = aes.doFinal(array); return ciphertext;
		 */
		return plainBytes;
	}

}
