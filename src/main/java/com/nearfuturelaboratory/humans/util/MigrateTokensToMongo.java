package com.nearfuturelaboratory.humans.util;

import java.io.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.mongodb.morphia.Key;
import org.scribe.model.Token;

import com.nearfuturelaboratory.humans.dao.ServiceTokenDAO;
import com.nearfuturelaboratory.humans.entities.ServiceToken;
import com.nearfuturelaboratory.util.Constants;

public class MigrateTokensToMongo {
	final static Logger logger = Logger.getLogger(com.nearfuturelaboratory.humans.util.MigrateTokensToMongo.class);

	private static final String USERS_DB_PATH = Constants.getString("SERVICE_DATA_ROOT", ".")+"/%s/users/%s-%s/";
	private static final String USERS_SER_TOKEN = USERS_DB_PATH+"%s-token-for-%s-%s.ser";

	public static void main(String[] args) throws IOException {
		try {
			Constants.load("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/src/main/webapp/WEB-INF/lib/dev.app.properties");
			PropertyConfigurator.configureAndWatch("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/src/main/webapp/WEB-INF/lib/static-logger.properties");
			logger.debug("Hey Ho!");
		} catch(Exception e) {
			e.printStackTrace();
		}
		//Token token = deserializeToken(args[0]);
		String user_id = args[0];
		String username = args[1];
		String service_name = args[2];
		Token token = deserializeToken(service_name, user_id, username);
		logger.debug(token);
		byte[] bytes = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(token);
		bytes = baos.toByteArray();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(bais);
		Token foo = null;
		try {
			foo = (Token) ois.readObject();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		ServiceTokenDAO dao = new ServiceTokenDAO(service_name);
		dao.ensureIndexes();
		
		ServiceToken service_token = dao.findByExactUserId(user_id);
		if(service_token == null) {
			service_token = new ServiceToken();
		}
		service_token.setToken_bytes("Hello This Is A Test".getBytes());
		service_token.setToken(token);
		service_token.setUser_id(user_id);
		service_token.setUsername(username);
		service_token.setServicename(service_name);
		Key<ServiceToken> k = dao.save(service_token);
		logger.debug(k);
		
	}
	
	protected static Token deserializeToken(String path) {
		Token result = null;
		try {
			
			InputStream file = new FileInputStream( path );
			InputStream buffer = new BufferedInputStream( file );
			ObjectInput input = new ObjectInputStream ( buffer );
			try{
				//deserialize the List
				result = (Token)input.readObject();
				//display its data
				logger.debug("Deserialized Token is: "+result);
			}
			finally{
				input.close();
			}
		}
		catch(ClassNotFoundException ex){
			logger.error("Cannot perform input. Class not found.", ex);
			ex.printStackTrace();
		}
		catch(IOException ex){
			logger.error("Cannot perform input.", ex);
			ex.printStackTrace();
		}

		return result;
	}
	
	protected static Token deserializeToken(String aServicename, String aUserID, String aUsername) {
	Token result = null;
	try {
		String path = String.format(USERS_SER_TOKEN, aServicename, aUserID, aUsername, aServicename, aUserID, aUsername);

		InputStream file = new FileInputStream( path );
		InputStream buffer = new BufferedInputStream( file );
		ObjectInput input = new ObjectInputStream ( buffer );
		try{
			//deserialize the List
			result = (Token)input.readObject();
			//display its data
			logger.debug("Deserialized Token is: "+result);
		}
		finally{
			input.close();
		}
	}
	catch(ClassNotFoundException ex){
		logger.error("Cannot perform input. Class not found.", ex);
		ex.printStackTrace();
	}
	catch(IOException ex){
		logger.error("Cannot perform input.", ex);
		ex.printStackTrace();
	}
	return result;

}

}
