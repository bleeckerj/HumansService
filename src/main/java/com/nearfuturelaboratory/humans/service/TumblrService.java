package com.nearfuturelaboratory.humans.service;

import org.scribe.builder.*;
import org.scribe.builder.api.*;
import org.scribe.model.*;
import org.scribe.oauth.*;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.*;
import java.util.regex.Pattern;
import java.io.*;
import java.nio.file.*;

import com.nearfuturelaboratory.util.*;
import com.nearfuturelaboratory.util.file.*;
import com.google.common.collect.*;
import com.nearfuturelaboratory.util.file.Find.Finder;

import org.apache.commons.io.filefilter.*;
import org.apache.log4j.Logger;


public class TumblrService {
	final static Logger logger = Logger.getLogger("com.nearfuturelaboratory.humans.test.Test");

	private String apiKey = Constants.getString("TUMBLR_API_KEY");
	private String apiSecret = Constants.getString("TUMBLR_API_SECRET");
	private String callbackURL = Constants.getString("TUMBLR_CALLBACK_URL");

	private Token accessToken;
	protected JSONObject user;
	private OAuthService service;
	private static final String FRIENDS_LIST_URL = "https://api.tumblr.com/v2/user/following?limit=20&offset=%s";
	//private static final String FRIENDS_IDS_URL = "https://api.twitter.com/1.1/friends/ids.json?user_id=%s&cursor=%s&count=5000";
	//private static final String VERIFY_URL = "https://api.twitter.com/1.1/account/verify_credentials.json";
	//private static final String SHOW_USER_BY_ID_URL = "https://api.twitter.com/1.1/users/show.json?user_id=%s&include_entities=true";
	//private static final String USER_LOOKUP_URL = "https://api.twitter.com/1.1/users/lookup.json";
	//private static final String STATUS_URL = "https://api.twitter.com/1.1/statuses/user_timeline.json?user_id=%s&trim_user=true";
	private static final String USER_INFO = "https://api.tumblr.com/v2/user/info";

	private static final String STATUS_DB_PATH = Constants.getString("SERVICE_DATA_ROOT")+"/tumblr/users/%s-%s/status/";
	private static final String USERS_DB_PATH_ROOT = Constants.getString("SERVICE_DATA_ROOT")+"/tumblr/users/";
	private static final String USERS_DB_PATH = Constants.getString("SERVICE_DATA_ROOT")+"/tumblr/users/%s/";
	private static final String USERS_FOLLOWS_PATH = Constants.getString("SERVICE_DATA_ROOT")+"/tumblr/users/%s/follows/";

	private static final String USERS_DB_PATH_CODED = Constants.getString("SERVICE_DATA_ROOT")+"/tumblr/users/%s/";
	private static final String USERS_SER_TOKEN = USERS_DB_PATH+"tumblr-token-for-%s.ser";

	
	
	public TumblrService(Token aAccessToken) {
		// TODO Auto-generated constructor stub
		accessToken = aAccessToken;
		service = new ServiceBuilder()
		.provider(TumblrApi.class)
		.apiKey(apiKey)
		.apiSecret(apiSecret)
		.callback(callbackURL)
		.build();
		
	}

	public void serviceRequestUserBasic() {
		OAuthRequest request = new OAuthRequest(Verb.GET, USER_INFO);
		service.signRequest(accessToken, request);
		Response response = request.send();
		String s = response.getBody();
		JSONObject obj = (JSONObject)JSONValue.parse(s);
		this.user = (JSONObject)(((JSONObject)obj.get("response")).get("user"));
		logger.debug(obj.get("user"));
		//this.user = (JSONObject)jsonResponse.get("user");
		File f = new File(String.format(USERS_DB_PATH, user.get("name")));
		String p =  (String)user.get("user")+".json";

		writeJSONToFile(user, f, p);
	
	}
	
	public JSONObject getThisUser() {
		return user;
	}
	
	
	//TODO This is ridiculous
	protected void createDirectoryHierarchyFromRoot(File aDir)
	{
		try {
			if(!aDir.exists()) {
				String[] subDirs = aDir.getPath().split(Pattern.quote(File.separator));
				List<String> dirs = Arrays.asList(subDirs);
				TwitterService.mkDirs(new File("/"), dirs, dirs.size());
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void writeJSONToFile(JSONObject objToWrite, File aDir, String aName)
	{

		createDirectoryHierarchyFromRoot(aDir);
		try {
			File aFile = new File(aDir, aName);
			//System.out.println("wrote user to "+aFile);
			//logger.debug("wrote user to "+aFile);

			FileWriter file = new FileWriter(aFile);
			file.write(objToWrite.toJSONString());
			file.flush();
			file.close();
		} catch(IOException ioe) {
			//ioe.printStackTrace();
			logger.error(ioe);
		}
	}

	
	public static Token deserializeToken(JSONObject aUser) {
		Token result = null;
		try{
			//use buffering
			String path = String.format(USERS_SER_TOKEN, aUser.get("name"), aUser.get("name"));

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
