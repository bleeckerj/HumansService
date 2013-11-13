package com.nearfuturelaboratory.humans.service;

import org.scribe.builder.*;
import org.scribe.builder.api.*;
import org.scribe.model.*;
import org.scribe.oauth.*;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.*;

import com.nearfuturelaboratory.humans.service.status.FlickrStatus;
import com.nearfuturelaboratory.humans.service.status.InstagramStatus;
import com.nearfuturelaboratory.util.*;
import com.nearfuturelaboratory.util.file.*;
import com.google.common.collect.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.nearfuturelaboratory.util.file.Find.Finder;

import org.apache.commons.io.filefilter.*;
import org.apache.log4j.Logger;

import com.jayway.jsonpath.*;


public class FlickrService {
	final static Logger logger = Logger.getLogger("com.nearfuturelaboratory.humans.test.Test");

	private String apiKey = Constants.getString("FLICKR_API_KEY");
	private String apiSecret = Constants.getString("FLICKR_API_SECRET");
	private String callbackURL = Constants.getString("FLICKR_CALLBACK_URL");

	private Token accessToken;
	protected JSONObject user;
	private OAuthService service;
	private static final String FRIENDS_LIST_URL = "https://api.tumblr.com/v2/user/following?limit=20&offset=%s";
	private static final String GET_CONTACTS_URL = "";
	//private static final String FRIENDS_IDS_URL = "https://api.twitter.com/1.1/friends/ids.json?user_id=%s&cursor=%s&count=5000";
	//private static final String VERIFY_URL = "https://api.twitter.com/1.1/account/verify_credentials.json";
	//private static final String SHOW_USER_BY_ID_URL = "https://api.twitter.com/1.1/users/show.json?user_id=%s&include_entities=true";
	//private static final String USER_LOOKUP_URL = "https://api.twitter.com/1.1/users/lookup.json";
	//private static final String STATUS_URL = "https://api.twitter.com/1.1/statuses/user_timeline.json?user_id=%s&trim_user=true";
	private static final String USER_INFO = "https://api.tumblr.com/v2/user/info";
	private static final String SERVICE_URL = "https://secure.flickr.com/services/rest/?";

	private static final String STATUS_DB_PATH = Constants.getString("SERVICE_DATA_ROOT")+"/flickr/users/%s-%s/status/";
	private static final String USERS_DB_PATH_ROOT = Constants.getString("SERVICE_DATA_ROOT")+"/flickr/users/";
	private static final String USERS_DB_PATH = Constants.getString("SERVICE_DATA_ROOT")+"/flickr/users/%s-%s/";
	private static final String USERS_FOLLOWS_PATH = Constants.getString("SERVICE_DATA_ROOT")+"/flickr/users/%s-%s/follows/";

	private static final String USERS_SER_TOKEN = USERS_DB_PATH+"flickr-token-for-%s-%s.ser";

	public static FlickrService createFlickrServiceOnBehalfOfCodedUsername(String aCodedUsername) {
		FlickrService result;
		JSONObject user = getLocalUserBasicForCodedUsername(aCodedUsername);
		Token token = FlickrService.deserializeToken(user);
		result = new FlickrService(token);
		result.user = user;
		return result;
	}


	public FlickrService(Token aAccessToken) {
		// TODO Auto-generated constructor stub
		// TODO Auto-generated constructor stub
		accessToken = aAccessToken;
		service = new ServiceBuilder()
		.provider(FlickrApi.class)
		.apiKey(apiKey)
		.apiSecret(apiSecret)
		.callback(callbackURL)
		.build();
	}


	public List<FlickrStatus> getStatusForUserID(String aUserID) 
	{
		List<FlickrStatus> result = new ArrayList<FlickrStatus>();
		List<Path>statusPaths = getStatusPaths(aUserID);
		for(int i=0; statusPaths != null && i < statusPaths.size(); i++) {
			Path path = statusPaths.get(i);
			File f = path.toFile();
			JsonParser parser = new JsonParser();
			Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss").create();

			try {
				InputStreamReader char_input = 
						new InputStreamReader(new FileInputStream(f),Charset.forName("UTF-8").newDecoder());
				Object obj = parser.parse(char_input);
				JsonArray statuses = (JsonArray)obj;
				for(int j=0; j<statuses.size(); j++) {
					if(statuses.get(i).isJsonObject()) {
						FlickrStatus fs = gson.fromJson(statuses.get(j), FlickrStatus.class);
						fs.setStatusJSON(statuses.get(j).getAsJsonObject());
						result.add(fs);
						//logger.debug(is.toString());
					}

				}
			} catch (JsonIOException | JsonSyntaxException
					| FileNotFoundException e) {
				e.printStackTrace();
				logger.error(e);
			}


		}
		return result;
	}

	protected List<Path> getStatusPaths() {
		return getStatusPaths(this.getThisUser().get("id").toString());
	}

	protected List<Path> getStatusPaths(String aUserID) {
		Path startingDir = Paths.get(USERS_DB_PATH_ROOT);
		//48288738-.*[^json]
		//String pattern = aUserID+"-*[!follows].json";
		//6347872-.*[^json] 
		//String pattern = aUserID+"-.*[^json]";
		//String pattern = aUserID+"-.*[^json]";
		String pattern = aUserID+"-(\\w*-(archival)?)status.json";
		//logger.debug(aUserID+" "+pattern);

		Finder finder = new Finder(pattern, "regex");
		//logger.debug(finder);
		try {
			Files.walkFileTree(startingDir, finder);
		} catch (IOException e) {
			//e.printStackTrace();
			logger.error(e);
		}
		//logger.debug(finder.results);
		List<Path> results = finder.results;

		return results;
	}


	public void serviceRequestStatusForUserID(String aUserID) {
		this.serviceRequestStatusForUserIDToMonthsAgo(aUserID, 6);
	}


	protected void serviceRequestStatusForUserIDToMonthsAgo(String aUserID, int aMonthsAgo) {
		Calendar ago =Calendar.getInstance();
		ago.add(Calendar.MONTH, -1*aMonthsAgo);
		String pattern = "yyyy-MM-dd hh:mm:ss";
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		String mysqlDateString = formatter.format(ago.getTime());
		//long year_ago = ago.getTimeInMillis();
		if(aUserID == null || aUserID.equalsIgnoreCase("self")) {
			aUserID = (String)user.get("id");
		}


		OAuthRequest request = new OAuthRequest(Verb.GET, SERVICE_URL);
		request.addQuerystringParameter("method", "flickr.people.getPhotos");
		request.addQuerystringParameter("api_key",apiKey);
		request.addQuerystringParameter("user_id", aUserID);
		request.addQuerystringParameter("extras", "description, license, date_upload, date_taken, owner_name, icon_server, original_format, last_update, geo, tags, machine_tags, o_dims, views, media, path_alias, url_sq, url_t, url_s, url_q, url_m, url_n, url_z, url_c, url_l, url_o");
		request.addQuerystringParameter("format", "json");
		request.addQuerystringParameter("per_page", "100");
		request.addQuerystringParameter("page","1");
		request.addQuerystringParameter("min_taken_date", mysqlDateString);
		request.addQuerystringParameter("nojsoncallback", "1");
		service.signRequest(accessToken, request);
		Response response = request.send();
		String s = response.getBody();
		Object jsonResponse = JSONValue.parse(s);
		JSONObject status = (JSONObject)jsonResponse;

		if(status == null || status.get("stat").toString().equalsIgnoreCase("fail")) {
			logger.warn("Error "+status.toString());
			logger.warn(aUserID);
			logger.warn(this);
			return;
		}

		JSONArray photos = JsonPath.read(status, "photos.photo");
		JSONObject oldest = null;


		// nothing in x months, try everything..
		if(photos.size() < 1) {
			logger.info("No Flickr status in the time for "+aUserID);
			request = new OAuthRequest(Verb.GET, SERVICE_URL);
			request.addQuerystringParameter("method", "flickr.people.getPhotos");
			request.addQuerystringParameter("api_key",apiKey);
			request.addQuerystringParameter("user_id", aUserID);
			request.addQuerystringParameter("extras", "description, license, date_upload, date_taken, owner_name, icon_server, original_format, last_update, geo, tags, machine_tags, o_dims, views, media, path_alias, url_sq, url_t, url_s, url_q, url_m, url_n, url_z, url_c, url_l, url_o");
			request.addQuerystringParameter("format", "json");
			request.addQuerystringParameter("per_page", "100");
			request.addQuerystringParameter("nojsoncallback", "1");
			service.signRequest(accessToken, request);
			response = request.send();
			s = response.getBody();
			jsonResponse = JSONValue.parse(s);
			status = (JSONObject)jsonResponse;
		}
		photos = JsonPath.read(status, "photos.photo");
		if(photos.size() < 1) {
			return;
		} else {
			oldest = (JSONObject)photos.get(photos.size()-1);
		}



		Date oldest_taken_date;
		long oldest_time = new Date().getTime();

		try {
			oldest_taken_date = formatter.parse(oldest.get("datetaken").toString());
			oldest_time = oldest_taken_date.getTime();

		} catch (java.text.ParseException | NullPointerException e) {
			logger.error(e);
			//			logger.warn(oldest);
			//			logger.error(s);
			//			logger.error(jsonResponse);
			e.printStackTrace();
			System.err.println(oldest);

		}

		int page = Integer.parseInt(		JsonPath.read(status, "photos.page").toString());
		int pages = Integer.parseInt(		JsonPath.read(status, "photos.pages").toString());
		JSONArray latest_data;
		while(oldest_time > ago.getTime().getTime() && pages > page) {
			request = new OAuthRequest(Verb.GET, SERVICE_URL);
			request.addQuerystringParameter("method", "flickr.people.getPhotos");
			request.addQuerystringParameter("api_key",apiKey);
			request.addQuerystringParameter("user_id", aUserID);
			request.addQuerystringParameter("extras", "description, license, date_upload, date_taken, owner_name, icon_server, original_format, last_update, geo, tags, machine_tags, o_dims, views, media, path_alias, url_sq, url_t, url_s, url_q, url_m, url_n, url_z, url_c, url_l, url_o");
			request.addQuerystringParameter("format", "json");
			request.addQuerystringParameter("per_page", "100");
			request.addQuerystringParameter("page", String.valueOf(page+1));
			request.addQuerystringParameter("min_taken_date", mysqlDateString);
			request.addQuerystringParameter("nojsoncallback", "1");
			service.signRequest(accessToken, request);
			response = request.send();
			s = response.getBody();
			jsonResponse = JSONValue.parse(s);
			status = (JSONObject)jsonResponse;
			latest_data = JsonPath.read(status, "photos.photo");
			photos.addAll(latest_data);
			oldest = (JSONObject)photos.get(photos.size()-1);
			try {
				oldest_taken_date =  formatter.parse(oldest.get("datetaken").toString());
			} catch (java.text.ParseException e) {
				// TODO Auto-generated catch block
				logger.error(e);
				e.printStackTrace();
				break;
			}
			oldest_time = oldest_taken_date.getTime();
			page = Integer.parseInt(JsonPath.read(status, "photos.page").toString());
			pages = Integer.parseInt(JsonPath.read(status, "photos.page").toString());

		}


		//ownername is returned in each photo..it's the username
		JSONObject one = (JSONObject)photos.get(1);
		String thisUsername = one.get("ownername").toString();
		String thisUserID = one.get("owner").toString();
		String path;
		path = String.format(STATUS_DB_PATH, aUserID, FileUtils.filenameSafeEncode(thisUsername));
		writeJSONToFile(photos, new File(path),  aUserID+"-"+FileUtils.filenameSafeEncode(thisUsername)+"-status.json");



	}


	/**
	 *  Get basic user info from Flickr
	 */
	public void serviceRequestUserBasic(String aUserID) {
		OAuthRequest request = new OAuthRequest(Verb.GET, SERVICE_URL);
		request.addQuerystringParameter("method", "flickr.people.getInfo");
		request.addQuerystringParameter("user_id", aUserID);
		request.addQuerystringParameter("format", "json");
		request.addQuerystringParameter("nojsoncallback", "1");

		service.signRequest(accessToken, request);
		Response response = request.send();
		String s = response.getBody();
		JSONObject obj = (JSONObject)JSONValue.parse(s);
		logger.debug("got "+s+" "+obj);
		JSONObject user = (JSONObject)obj.get("user");
		String id = user.get("id").toString();
		logger.debug("id="+id);
		serviceRequestUserBasicByUserID(id);
		//this.user = (JSONObject)(((JSONObject)obj.get("user")));
		//logger.debug(obj.get("user"));
		//JSONObject foo = (JSONObject)user.get("username");
		//String username = foo.get("_content").toString();

		//this.user = (JSONObject)jsonResponse.get("user");
		//File f = new File(String.format(USERS_DB_PATH, user.get("id"), username));
		//String p =  (String)user.get("id")+"-"+username+".json";

		//writeJSONToFile(user, f, p);

	}

	/**
	 * Get basic user info on a user
	 * @param aUserID
	 */
	public void serviceRequestUserBasicByUserID(String aUserID) {
		OAuthRequest request = new OAuthRequest(Verb.GET, SERVICE_URL);
		request.addQuerystringParameter("method", "flickr.people.getInfo");

		request.addQuerystringParameter("format", "json");
		request.addQuerystringParameter("nojsoncallback", "1");
		request.addQuerystringParameter("user_id", aUserID);

		service.signRequest(accessToken, request);
		Response response = request.send();
		String s = response.getBody();
		logger.debug(s);
		JSONObject obj = (JSONObject)JSONValue.parse(s);
		this.user = (JSONObject)(((JSONObject)obj.get("person")));
		logger.debug(obj.get("person"));
		//JSONObject foo = (JSONObject)user.get("path_alias");
		//String username = foo.toString();

		//this.user = (JSONObject)jsonResponse.get("user");
		File f = new File(String.format(USERS_DB_PATH, user.get("id"), this.getFilesafeUsername()));
		String p =  (String)user.get("id")+"-"+this.getFilesafeUsername()+".json";

		writeJSONToFile(user, f, p);

	}


	public JSONObject getThisUser() {
		return user;
	}

	public URL getURLForBuddyIcon() {
		return this.getURLForBuddyIconForUser(user);
	}

	public URL getURLForBuddyIconForUser(JSONObject aUser) {
		URL result = null;
		// http://farm{icon-farm}.staticflickr.com/{icon-server}/buddyicons/{nsid}.jpg
		String str = "http://farm%s.staticflickr.com/%s/buddyicons/%s.jpg";
		String urlStr = String.format(str, JsonPath.read(aUser,"iconfarm"), JsonPath.read(aUser,"iconserver"), JsonPath.read(aUser,"nsid"));
		logger.debug("url string for buddy icon is "+urlStr);
		try {
			result = new URL(urlStr);
		} catch (MalformedURLException e) {
			//e.printStackTrace();
			logger.error(e);
		}
		return result;
	}

	public JSONObject serviceRequestUserBasicForUserID(String aUserID)
	{
		JSONObject aUser;

		OAuthRequest request = new OAuthRequest(Verb.GET, SERVICE_URL);
		request.addQuerystringParameter("method", "flickr.people.getInfo");
		request.addQuerystringParameter("format", "json");
		request.addQuerystringParameter("nojsoncallback", "1");
		request.addQuerystringParameter("user_id", aUserID);


		service.signRequest(accessToken, request);
		Response response = request.send();
		String s = response.getBody();
		//Map<String, String> h = response.getHeaders();
		//System.out.println(h);
		JSONObject obj = (JSONObject)JSONValue.parse(s);
		aUser = (JSONObject)obj.get("person");
		return aUser;

	}



	public String getUsername() {
		String username = JsonPath.read(user, "username._content").toString();
		return username;
	}

	/**
	 * Flickr has the strangest JSON. The username is hidden in username._content WTF?
	 * In any case, lets use "path_alias" which is whitespace free.
	 * @return
	 */
	public String getFilesafeUsername() {
		String username = JsonPath.read(user, "username._content").toString();
		String result = FileUtils.filenameSafeEncode(username);//(username).replaceAll("\\s+|(\\p{Punct}*)|[\u0000-\u001f]","");
		return result;
	}

	protected static String getFilesafeUsername(JSONObject aUser) {
		String username = JsonPath.read(aUser, "username._content").toString();
		String result = FileUtils.filenameSafeEncode(username);//(username).replaceAll("\\s+|(\\p{Punct}*)|[\u0000-\u001f]","");
		return result;
	}


	public void serviceRequestUserInfo()
	{
		OAuthRequest request = new OAuthRequest(Verb.GET, SERVICE_URL);
		request.addQuerystringParameter("method", "flickr.test.login");
		request.addQuerystringParameter("format", "json");
		request.addQuerystringParameter("nojsoncallback", "1");

		service.signRequest(accessToken, request);
		Response response = request.send();
		String s = response.getBody();
		JSONObject obj = (JSONObject)JSONValue.parse(s);
		logger.debug("got "+s+" "+obj);
		JSONObject user = (JSONObject)obj.get("user");
		String id = user.get("id").toString();
		logger.debug("id="+id);
		serviceRequestUserBasicByUserID(id);
	}
	

	public boolean isTokenValid() {
		boolean result = false;
		OAuthRequest request = new OAuthRequest(Verb.GET, SERVICE_URL);
		request.addQuerystringParameter("method", "flickr.test.login");
		request.addQuerystringParameter("format", "json");
		request.addQuerystringParameter("nojsoncallback", "1");
		service.signRequest(accessToken, request);
		Response response = request.send();
		String s = response.getBody();
		JSONObject obj = (JSONObject)JSONValue.parse(s);
		if(obj.containsValue("fail")) {
			result = true;
			logger.warn(this.getCodedUsername()+"\n\r"+obj.get("message")+" for:\n\r"+this.getThisUser());
		}
		return result;
	}

	//TODO might be a few pages need to check right now you only get the first 1000
	public void getFollows() {
		List<String> follows;// = new JSONArray();
		OAuthRequest request = new OAuthRequest(Verb.GET, SERVICE_URL);
		request.addQuerystringParameter("method", "flickr.contacts.getList");
		request.addQuerystringParameter("format", "json");
		request.addQuerystringParameter("nojsoncallback", "1");
		//request.addQuerystringParameter("per_page", "200");
		request.addQuerystringParameter("page", "1");
		service.signRequest(accessToken, request);
		Response response = request.send();

		String s = response.getBody();
		//TODO Error checking, etc.


		JSONObject obj = (JSONObject)JSONValue.parse(s);
		//JSONObject contacts = (JSONObject)obj.get("contacts");

		follows = null;
		// error check
		if(obj == null || obj.get("stat").toString().equalsIgnoreCase("fail")) {
			logger.warn("Error "+obj.toString());
			logger.warn(this);
			return;
		}
		try {
			follows = JsonPath.read(obj, "contacts.contact");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e);
			logger.warn("Error "+obj.toString());
			logger.warn(this);
			e.printStackTrace();
			return;
		}


		int pages = Integer.parseInt(JsonPath.read(obj, "contacts.pages").toString());
		int page = Integer.parseInt(JsonPath.read(obj, "contacts.page").toString());
		while(pages > page) {
			page++;
			request = new OAuthRequest(Verb.GET, SERVICE_URL);
			request.addQuerystringParameter("method", "flickr.contacts.getList");
			request.addQuerystringParameter("format", "json");
			request.addQuerystringParameter("nojsoncallback", "1");
			//request.addQuerystringParameter("per_page", "200");

			request.addQuerystringParameter("page", String.valueOf(page));
			service.signRequest(accessToken, request);
			response = request.send();
			s = response.getBody();
			//TODO Error checking, etc.

			obj = (JSONObject)JSONValue.parse(s);
			//contacts = (JSONObject)obj.get("contacts");
			List<String> f = JsonPath.read(obj, "contacts.contact");

			if(f != null) {
				follows.addAll(f);		    
			}
			pages = Integer.parseInt(JsonPath.read(obj, "contacts.pages").toString());
			page = Integer.parseInt(JsonPath.read(obj, "contacts.page").toString());
		}
		JSONArray result = new JSONArray();
		for(int i=0; i<follows.size(); i++) {
			result.add(follows.get(i));
		}
		String username = this.getFilesafeUsername();//(String)((JSONObject)user.get("username")).get("_content");//.get("_content");
		File f = new File(String.format(USERS_FOLLOWS_PATH, user.get("id"), username.toString()));
		String p =  user.get("id")+"-"+username+"-follows.json";

		writeJSONToFile(result, f, p);
	}



	public static JSONArray getFollowsFor(String aCodedUser)
	{
		JSONArray result = new JSONArray();
		// have to find a path to a file by a unique userID, which will also contain the username, but we don't need it for sure
		Path startingDir = Paths.get(USERS_DB_PATH_ROOT);
		Finder finder = new Finder(aCodedUser+"-follows.json");
		try {
			Files.walkFileTree(startingDir, finder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			logger.error(e);
		}
		List<Path> results = finder.results;		
		if(results != null && results.size() > 0) {
			try {
				JSONParser parser = new JSONParser();
				File aFile = results.get(0).toFile();
				result = (JSONArray)parser.parse(new FileReader(aFile));
			} catch(IOException | ParseException ioe) {
				logger.error(ioe);
			}

		}

		return result;
	}





	protected static JSONObject getLocalUserBasicForUsername(String aUsername) {
		// usernames are unique, so actually we can find a user without the userid
		JSONObject result = null;
		Path startingDir = Paths.get(USERS_DB_PATH_ROOT);
		Finder finder = new Finder("*-"+aUsername+".json");
		try {
			Files.walkFileTree(startingDir, finder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			logger.error(e);
		}
		List<Path> results = finder.results;		
		if(results != null && results.size() > 0) {
			try {
				JSONParser parser = new JSONParser();
				File aFile = results.get(0).toFile();
				result = (JSONObject)parser.parse(new FileReader(aFile));
			} catch(IOException | ParseException ioe) {
				logger.error(ioe);
			}

		}
		return result;
	}


	protected static JSONObject getLocalUserBasicForCodedUsername(String aCodedUsername) {
		JSONObject result = null;
		Path startingDir = Paths.get(USERS_DB_PATH_ROOT);
		//String pattern = aUserID+"-*[!follows].json";
		String pattern = aCodedUsername+".json";
		//Pair<List<Path>, Boolean> result = new Pair<List<Path>, Boolean>(null, new Boolean(false));
		Finder finder = new Finder(pattern);
		try {
			Files.walkFileTree(startingDir, finder);
		} catch (IOException e) {
			//e.printStackTrace();
			logger.error(e);
		}
		if(finder.results == null || finder.results.size() < 1) {
			logger.error("Get Local User Basic for "+aCodedUsername+" "+finder.results);
		}
		List<Path> results = finder.results;		

		if(results != null & results.size() > 0) {
			try {
				JSONParser parser = new JSONParser();
				File aFile = results.get(0).toFile();
				result = (JSONObject)parser.parse(new FileReader(aFile));
			} catch(IOException | ParseException ioe) {
				logger.error(ioe);
			}

		}

		if(result == null) {
			//result = this.serviceRequestUserBasicForUserID(aUserID);
		}

		return result;

	}





	//TODO This is ridiculous
	protected void createDirectoryHierarchyFromRoot(File aDir)
	{
		try {
			if(!aDir.exists()) {
				String[] subDirs = aDir.getPath().split(Pattern.quote(File.separator));
				List<String> dirs = Arrays.asList(subDirs);
				FlickrService.mkDirs(new File("/"), dirs, dirs.size());
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

	public void writeJSONToFile(JSONArray arrayToWrite, File aDir, String aName)
	{

		createDirectoryHierarchyFromRoot(aDir);
		try {
			File aFile = new File(aDir, aName);
			//System.out.println("wrote user to "+aFile);
			//logger.debug("wrote user to "+aFile);

			FileWriter file = new FileWriter(aFile);
			file.write(arrayToWrite.toJSONString());
			file.flush();
			file.close();
		} catch(IOException ioe) {
			//ioe.printStackTrace();
			logger.error(ioe);
		}
	}


	public void serializeToken(Token aToken) {
		try{
			//use buffering
			//String path = Constants.getString("SERVICE_DATA_ROOT")+"/flickr/users/"+this.user.get("id")+"-"+this.getThisUsername()+"/flickr-token-for-"+user.get("id")+"-"+this.getThisUsername()+".ser";
			String path = String.format(USERS_SER_TOKEN, this.user.get("id"), this.getFilesafeUsername(), this.user.get("id"), this.getFilesafeUsername());

			OutputStream file = new FileOutputStream( path );
			OutputStream buffer = new BufferedOutputStream( file );
			ObjectOutput output = new ObjectOutputStream( buffer );
			try{
				output.writeObject(aToken);
			}
			finally{
				output.close();
			}
		}  
		catch(IOException ex){
			//fLogger.log(Level.SEVERE, "Cannot perform output.", ex);
			ex.printStackTrace();
		}

	}



	public static Token deserializeToken(JSONObject aUser) {
		Token result = null;
		try{
			//use buffering
			String path = String.format(USERS_SER_TOKEN, aUser.get("id"), getFilesafeUsername(aUser), aUser.get("id"), getFilesafeUsername(aUser));

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


	public Token deserializeToken() {
		Token result = null;
		try{
			//use buffering
			String path = String.format(USERS_SER_TOKEN, this.user.get("id"), this.getFilesafeUsername(), this.user.get("id"), this.getFilesafeUsername());

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


	public String getCodedUsername()
	{
		String result = this.getThisUser().get("id")+"-"+this.getFilesafeUsername();
		return result;
	}

	public static void mkDirs(File root, List<String> dirs, int depth) {
		if (depth == 0) return;
		for (String s : dirs) {
			File subdir = new File(root, s);
			if(!subdir.exists()) {
				//System.out.println("Subdir "+subdir);
				subdir.mkdir();
			}
			root = subdir;
			//		    mkDirs(subdir, dirs, depth - 1);
		}
	}

	public String toString() {
		return "["+this.getThisUser().toString()+", accessToken="+this.accessToken+"]";
	}
}
