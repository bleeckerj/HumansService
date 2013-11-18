package com.nearfuturelaboratory.humans.service;

import org.scribe.builder.*;
import org.scribe.builder.api.*;
import org.scribe.model.*;
import org.scribe.oauth.*;
import org.json.simple.*;

import java.util.*;

import com.nearfuturelaboratory.humans.dao.FoursquareCheckinDAO;
import com.nearfuturelaboratory.humans.dao.FoursquareFollowsDAO;
import com.nearfuturelaboratory.humans.dao.FoursquareUserDAO;
import com.nearfuturelaboratory.humans.dao.ServiceTokenDAO;
import com.nearfuturelaboratory.humans.entities.ServiceToken;
import com.nearfuturelaboratory.humans.exception.BadAccessTokenException;
import com.nearfuturelaboratory.humans.foursquare.entities.FoursquareCheckin;
import com.nearfuturelaboratory.humans.foursquare.entities.FoursquareCompactUser;
import com.nearfuturelaboratory.humans.foursquare.entities.FoursquareFriend;
import com.nearfuturelaboratory.humans.foursquare.entities.FoursquareUser;
import com.nearfuturelaboratory.util.*;
import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import com.sun.istack.internal.NotNull;

import org.apache.log4j.Logger;

public class FoursquareService {

	private static final String FOLLOWS_URL = "https://api.foursquare.com/v2/users/%s/friends/?oauth_token=%s&v=20131006";
	private static final String CHECKINS_URL = "https://api.foursquare.com/v2/users/self/checkins/?oauth_token=%s&v=20131006&sort=newestfirst";
	private static final String USER_URL = "https://api.foursquare.com/v2/users/%s?oauth_token=%s&v=20131006";

	final static Logger logger = Logger.getLogger(com.nearfuturelaboratory.humans.service.FoursquareService.class);
	private static final Token EMPTY_TOKEN = null;
	private static String apiKey = Constants.getString("FOURSQUARE_API_KEY");//"MKGJ3OZYTDNZAI5ZMROF3PAMAUND0ZO2HYRTZYXHIIR5TW1Q";
	private static String apiSecret = Constants.getString("FOURSQUARE_API_SECRET");//"2G0DUIFCFAWBH1WPIYBUDQMESKRLFLGY5PHXY0BJNBE1MMN4";
	private static String callbackURL = Constants.getString("FOURSQUARE_CALLBACK_URL");
	private Token accessToken;
	protected FoursquareUser user;
	private OAuthService service;

	protected FoursquareCheckinDAO checkinDAO;
	protected FoursquareUserDAO userDAO;
	protected FoursquareFollowsDAO followsDAO;
	protected ServiceTokenDAO tokenDAO;
	protected Gson gson;

	public FoursquareService() {
		//db = MongoUtil.getMongo().getDB("foursquare");

		checkinDAO = new FoursquareCheckinDAO();
		checkinDAO.ensureIndexes();

		userDAO = new FoursquareUserDAO();
		userDAO.ensureIndexes();

		followsDAO = new FoursquareFollowsDAO();
		followsDAO.ensureIndexes();

		tokenDAO = new ServiceTokenDAO("foursquare");
		tokenDAO.ensureIndexes();

		//gson = new GsonBuilder().setDateFormat("EEE MMM dd HH:mm:ss Z yyyy").serializeNulls().create();
		gson = new Gson();
	}

	public static FoursquareService createFoursquareServiceOnBehalfOfUserID(String aUserID)  
			throws BadAccessTokenException 
	{
		FoursquareService result;//  = new FoursquareService(token);
		Token token = null;
		FoursquareUser user = getLocalUserBasicForUserID(aUserID);
		if(user == null) {
			token = FoursquareService.deserializeTokenByUserID(aUserID);
		} else {
			token = FoursquareService.deserializeToken(user);
		}
		if(token == null) throw new BadAccessTokenException("The access token for Foursquare User "+aUserID+" is null. It probably does not exist.");
		result = new FoursquareService(token);
		result.user = user;
		return result;
	}


	public FoursquareService(Token aAccessToken) {
		this();
		this.accessToken = aAccessToken;
		service = new ServiceBuilder()
		.provider(Foursquare2Api.class)
		.apiKey(apiKey)
		.apiSecret(apiSecret)
		.callback(callbackURL)
		.build();

	}

	private static FoursquareUser getLocalUserBasicForUserID(String aUserID) {
		FoursquareUser result = null;
		FoursquareUserDAO dao = new FoursquareUserDAO();
		result = dao.findByExactUserID(aUserID);
		return result;
	}


	public FoursquareUser serviceRequestUserBasicForUserID(String aUserID) {
		// break up aCodedUsername
		//String id = parts[0];
		String userURL = String.format(USER_URL, aUserID, accessToken.getToken());
		OAuthRequest request = new OAuthRequest(Verb.GET, userURL );
		service.signRequest(accessToken, request);
		Response response = request.send();
		//TODO Error checking
		String s = response.getBody();
		JSONObject obj = (JSONObject)JSONValue.parse(s);
		JSONObject respJSON = (JSONObject) obj.get("response");
		JSONObject respUser = (JSONObject)respJSON.get("user");

		FoursquareUser fuser = gson.fromJson(respUser.toJSONString(), FoursquareUser.class);		
		//this.saveUserBasicJson(obj);
		userDAO.save(fuser);
		return fuser;

		//		File f = new File(String.format(USERS_FOLLOWS_PATH_CODED, getCodedUsername()));
		//		String p =  String.format(USER_INFO_FILENAME_CODED, aCodedUsername);
		//		writeJSONToFile(user, f, p);
	}




	/**
	 * This will go to the service and get "self" for whoever's accessToken we have
	 * @return 
	 */
	public FoursquareUser serviceRequestUserBasic() {
		return serviceRequestUserBasicForUserID("self");
		//		String userURL = String.format(USER_URL, "self", accessToken.getToken());
		//		logger.debug("here access token is "+accessToken.getToken());
		//		logger.debug("userURL is "+userURL);
		//		OAuthRequest request = new OAuthRequest(Verb.GET, userURL );
		//		service.signRequest(accessToken, request);
		//		
		//		Response response = request.send();
		//		String s = response.getBody();
		//		JSONObject obj = (JSONObject)JSONValue.parse(s);
		//		JSONObject respJSON = (JSONObject) obj.get("response");
		//
		//		JSONObject user = (JSONObject)respJSON.get("user");
		//
		//		logger.debug("user here is "+user);
		//		File f = new File(String.format(USERS_DB_PATH_CODED, this.getCodedUsername()));
		//		String p =  String.format(USER_INFO_FILENAME, (String)user.get("id"), getDerivedUsername());
		//		writeJSONToFile(user, f, p);
	}



	public boolean localUserBasicIsFreshForUserID(String aUserID) {
		boolean result = false;
		FoursquareUser user = FoursquareService.getLocalUserBasicForUserID(aUserID);
		if(user == null) return false;

		Date d = user.getLastUpdated();

		long then = d.getTime();
		long now = new Date().getTime();
		long diff = now - then;
		if(diff < Constants.getLong("USER_BASIC_STALE_TIME")) {
			result = true;
		}
		return result;
	}

	protected FoursquareUser getLocalUserBasicForUserID1(String aUserID) {
		FoursquareUser result = null;
		result = userDAO.findByExactUserID(aUserID);
		return result;
	}

	/*	public void getLatestCheckins() {
		// get the last check in? get the ones since them somehow? or..who cares?
	}
	 */	
	public List<FoursquareCheckin> getCheckins() {
		return checkinDAO.findByExactUserID(this.getThisUser().getId());
	}

	public List<FoursquareCheckin> getCheckinsForUserID(String aUserID) {
		return checkinDAO.findByExactUserID(aUserID);
	}

	public void serviceRequestCheckins() {
		serviceRequestCheckins(0);
	}

	public void serviceRequestLatestCheckins() {
		FoursquareCheckin lastLocalCheckin = checkinDAO.findLatestCheckin(user.getId());

		if(lastLocalCheckin != null) {
			Long lastLocalCheckinTime = lastLocalCheckin.getCreatedAt();
			serviceRequestCheckins(lastLocalCheckinTime);
		} else {
			serviceRequestCheckins(-1);
		}

	}
	
	/**
	 *  You can only get checkins for "self", not for anyone
	 */
	protected void serviceRequestCheckins(long afterTimeStamp)
	{
		List<JSONObject> checkinsAll;

		String checkinsURL = String.format(CHECKINS_URL, accessToken.getToken());
		OAuthRequest request = new OAuthRequest(Verb.GET, checkinsURL );
		request.addQuerystringParameter("limit", "250");
		if(afterTimeStamp > -1) {
			request.addQuerystringParameter("afterTimestamp", String.valueOf(afterTimeStamp));
		}
		service.signRequest(accessToken, request);
		Response response = request.send();
		//TODO Error checking
		String s = response.getBody();

		JSONObject obj = (JSONObject)JSONValue.parse(s);
		JSONObject checkins = JsonPath.read(obj, "response.checkins");
		//TODO no error checking..fix that
		int total_checkins = Integer.parseInt(JsonPath.read(checkins, "count").toString());
		JSONArray items = (JSONArray)checkins.get("items");
		int items_count = items.size();

		checkinsAll = JsonPath.read(obj, "response.checkins.items");
		// this is good code - it just gets all a users checkins, so i need to think about how to use this/put it
		int offset = 0;
		while(offset < total_checkins) {
			offset+=items.size();
			request = new OAuthRequest(Verb.GET, checkinsURL );
			request.addQuerystringParameter("limit", "250");
			request.addQuerystringParameter("offset", String.valueOf(offset));
			service.signRequest(accessToken, request);
			response = request.send();
			s = response.getBody();
			obj = (JSONObject)JSONValue.parse(s);
			try {
				items = JsonPath.read(obj, "response.checkins.items");
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e);
				logger.error(obj);
			}
			if(items != null) {
				checkinsAll.addAll(items);
			}
			if(items != null && items.size() < 1) {
				break;
			}
		}
		logger.debug("For user_id "+this.getThisUser().getId()+" found "+checkinsAll.size()+" checkins.");

		for(int i=0; i<checkinsAll.size(); i++) {
			JSONObject o = (JSONObject)JSONValue.parse(checkinsAll.get(i).toJSONString());
			FoursquareCheckin checkin = gson.fromJson(o.toJSONString(), FoursquareCheckin.class);	
			checkin.setUserID(this.getThisUser().getId());
			//			checkin.setUsername(this.getThisUser().getFirstName()+"_"+this.getThisUser().getLastname());
			checkinDAO.save(checkin);
			//result.add(checkinsAll.get(i));
		}
	}


	public void serviceRequestFollows() {
		List<JSONObject> follows;
		String userURL = String.format(FOLLOWS_URL, "self", accessToken.getToken());
		OAuthRequest request = new OAuthRequest(Verb.GET, userURL );
		request.addQuerystringParameter("limit", "500");
		service.signRequest(accessToken, request);
		Response response = request.send();
		String s = response.getBody();
		JSONObject obj = (JSONObject)JSONValue.parse(s);
		JSONObject friends = JsonPath.read(obj, "response.friends");
		//TODO no error checking..fix that


		int count = Integer.parseInt(JsonPath.read(friends, "count").toString());
		JSONArray items = (JSONArray)friends.get("items");
		int items_count = items.size();

		follows = JsonPath.read(obj, "response.friends.items");
		int offset = 0;		
		while(offset < count) {
			offset+=follows.size();
			request = new OAuthRequest(Verb.GET, userURL );
			request.addQuerystringParameter("limit", "500");
			request.addQuerystringParameter("offset", String.valueOf(offset));
			service.signRequest(accessToken, request);
			response = request.send();
			s = response.getBody();
			obj = (JSONObject)JSONValue.parse(s);
			items = JsonPath.read(obj, "response.friends.items");
			if(items != null) {
				follows.addAll(items);
			}
		}

		//JSONArray result = new JSONArray();
		for(int i=0; i<follows.size(); i++) {
			JSONObject o = (JSONObject)JSONValue.parse(follows.get(i).toJSONString());
			FoursquareUser friend = gson.fromJson(o.toJSONString(), FoursquareUser.class);	
			FoursquareUser friendLocal = userDAO.findByExactUserID(friend.getId());
//			if(friendLocal == null) {
				userDAO.save(friend);
//			}
			
			
			FoursquareFriend me = followsDAO.findForUserIDFriendID(this.getThisUser().getId(), friend.getId());
			if(me == null) {
				me = new FoursquareFriend();
				me.setUser_id(this.getThisUser().getId());
				me.setFriend_id(friend.getId());
				me.setFriend(friend);
				followsDAO.save(me);
			} else {
				//followsDAO.save(me);

				//followsDAO.update(me);
			}
			//logger.debug("My Friend is "+me.getFriend());
			/*else {
				followsDAO.delete(me);
				me.setUser_id(this.getThisUser().getId());
				me.setFriend_id(friend.getId());
//				me.setFriend(friend);
				followsDAO.save(me);
				
			//FoursquareUser h = userDAO.findByExactUserID(friend.getId());
			//g.setFriend(f);


			//followsDAO.save(g);
			//result.add(follows.get(i));
			}
			*/
		}
	}

	public List<FoursquareFriend> getFriends() {
		return followsDAO.findForUserID(this.getThisUser().getId());
	}
	//	public List<FoursquareStatus> getCheckinsForUserID(String aUserID) {
	//		List<FoursquareStatus> result = new ArrayList<FoursquareStatus>();
	//		List<Path>statusPaths = getCheckinsPaths(aUserID);
	//
	//		// load each file, iterate the status
	//		for(int i=0; statusPaths != null && i < statusPaths.size(); i++) {
	//			Path path = statusPaths.get(i);
	//			File f = path.toFile();
	//			JsonParser parser = new JsonParser();
	//			Gson gson = new GsonBuilder().create();
	//			try {
	//				
	//				InputStreamReader char_input = 
	//						new InputStreamReader(new FileInputStream(f),Charset.forName("UTF-8").newDecoder());
	//				
	//				Object obj = parser.parse(char_input);
	//				JsonArray statuses = (JsonArray)obj;
	//				for(int j=0; j<statuses.size(); j++) {
	//					if(statuses.get(i).isJsonObject()) {
	//						//logger.debug(statuses.get(j));
	//						FoursquareStatus fs = gson.fromJson(statuses.get(j), FoursquareStatus.class);
	//						fs.setStatusJSON(statuses.get(j).getAsJsonObject());
	//						result.add(fs);
	//					}
	//
	//				}
	//			} catch (JsonIOException | JsonSyntaxException
	//					| FileNotFoundException e) {
	//				// TODO Auto-generated catch block
	//				e.printStackTrace();
	//				logger.error(e);
	//			}
	//		}
	//		return result;
	//	}


	//	protected List<Path> getCheckinsPath() {
	//		return getCheckinsPaths(this.getThisUser().get("id").toString());
	//	}

	//	protected List<Path> getCheckinsPaths(String aUserID) {
	//		Path startingDir = Paths.get(USERS_DB_PATH_ROOT);
	//		String pattern = aUserID+"-.+-checkins.json";
	//		Finder finder = new Finder(pattern, "regex");
	//		//logger.debug(finder);
	//		try {
	//			Files.walkFileTree(startingDir, finder);
	//		} catch (IOException e) {
	//			//e.printStackTrace();
	//			logger.error(e);
	//		}
	//		//logger.debug(finder.results);
	//		List<Path> results = finder.results;
	//
	//		return results;
	//	}

	//TODO ugh. need to get the path to the status file for this user
	// probably first find their root directory given only the userID
	// then look below that. STATUS_DB_PATH is the wrong path
	//	protected static Path getCheckinsPathForUserID(String aUserID) {
	//		Path result = null;
	//		Path startingDir = Paths.get(USERS_DB_PATH_ROOT);
	//		//48288738-.*[^json]
	//		//String pattern = aUserID+"-*[!follows].json";
	//		//6347872-.*[^json] 
	//		//String pattern = aUserID+"-.*[^json]";
	//		//String pattern = aUserID+"-.*[^json]";
	//		String pattern = aUserID+"-.*-.*-status\\.json";
	//		//logger.debug(aUserID+" "+pattern);
	//		Finder finder = new Finder(pattern, "regex");
	//		//logger.debug(finder);
	//		try {
	//			Files.walkFileTree(startingDir, finder);
	//		} catch (IOException e) {
	//			//e.printStackTrace();
	//			logger.error(e);
	//		}
	//		//logger.debug(finder.results);
	//		List<Path> results = finder.results;
	//		
	//		if(results != null && results.size() > 0) {
	//			
	//			//Path r = results.get(0);
	//			
	//			result = results.get(results.size()-1);
	//			
	//		}
	//		return result;
	//	}
	//	
	//	private static String getFilesafeUsernameForUser(String aUserID, String aUserFirstname, String aUserLastname) {
	//		String username =  String.format(USER_CODED, aUserID, aUserFirstname.replaceAll("\\s+|(\\p{Punct}*)|[\u0000-\u001f]",""), aUserLastname.replaceAll("\\s+|(\\p{Punct}*)|[\u0000-\u001f]",""));
	//		return username;
	//	}
	//	
	//	private static String getFilesafeUsernameForUser(JSONObject aUser) {
	//		String username =  String.format(USER_CODED, (String)aUser.get("id"), ((String)aUser.get("firstName")).replaceAll("\\s+|(\\p{Punct}*)|[\u0000-\u001f]",""), ((String)aUser.get("lastName")).replaceAll("\\s+|(\\p{Punct}*)|[\u0000-\u001f]",""));
	//		return username;
	//	}
	//	
	//	private String getFilesafeUsername() {
	//		String username =  String.format(USER_CODED, (String)user.get("id"), ((String)user.get("firstName")).replaceAll("\\s+|(\\p{Punct}*)|[\u0000-\u001f]",""), ((String)user.get("lastName")).replaceAll("\\s+|(\\p{Punct}*)|[\u0000-\u001f]",""));
	//		//String path_alias = (String)user.get("path_alias");
	//		//String result = (username).replaceAll("\\s+|(\\p{Punct}*)|[\u0000-\u001f]","");
	//		//logger.debug(result+" from "+username);
	//		return username;	
	//	}


	public FoursquareUser getThisUser() {
		return user;
	}


	/**
	 * Weird bootstrap method needed while migrating. We really should not have a token
	 * if we don't have a local user in the database, but we did when we migrated Tokens from the
	 * old filesystem database and the database had no users in it yet. Really, when you
	 * save a new user you also save the user basic with the token
	 * 
	 * @param aUsername
	 */
	private static Token deserializeTokenByUserID(String aUserID) {
		ServiceTokenDAO dao = new ServiceTokenDAO("foursquare");
		ServiceToken st = dao.findByExactUserID(aUserID);
		if(st == null) {
			logger.error("No Token for Foursquare userid:"+aUserID+" maybe a data error in the token.");
			return null;
		}
		return st.getToken();
	}

	public static void serializeToken(Token aToken, FoursquareUser aUser) {
		ServiceTokenDAO dao = new ServiceTokenDAO("foursquae");
		ServiceToken tokenToSave = new ServiceToken();
		tokenToSave.setToken(aToken);
		tokenToSave.setUser_id(aUser.getId());
		tokenToSave.setUsername(aUser.getFirstName()+"_"+aUser.getLastName());
		tokenToSave.setServicename("foursquare");
		dao.save(tokenToSave);
	}


	public static Token deserializeToken(@NotNull FoursquareUser aUser) {
		//Token result = null;
		ServiceTokenDAO dao = new ServiceTokenDAO("foursquare");
		ServiceToken serviceToken = dao.findByExactUserId( aUser.getId() );
		return serviceToken.getToken();
	}

	public FoursquareCheckin getLatestCheckin() {
		return checkinDAO.findLatestCheckin(user.getId());
	}

	public String getDerivedUsername() {
		return getThisUser().getFirstName()+"_"+getThisUser().getLastName();
	}



	//	public static void serializeToken(Token aToken, JSONObject aUser) {
	//		try{
	//			//use buffering
	//			String coded = FoursquareService.getFilesafeUsernameForUser(aUser);
	//			String path = String.format(USERS_SER_TOKEN, coded, coded);
	//			OutputStream file = new FileOutputStream( path);
	//			OutputStream buffer = new BufferedOutputStream( file );
	//			ObjectOutput output = new ObjectOutputStream( buffer );
	//			try{
	//				logger.info("serializing token to "+path);
	//
	//				output.writeObject(aToken);
	//			}
	//			finally{
	//				output.close();
	//			}
	//		}  
	//		catch(IOException ex){
	//			//fLogger.log(Level.SEVERE, "Cannot perform output.", ex);
	//			ex.printStackTrace();
	//		}
	//
	//	}
	//
	//	public static Token deserializeToken(JSONObject aUser) {
	//		Token result = null;
	//		try{
	//			//use buffering
	//			//String path = Constants.getString("SERVICE_DATA_ROOT")+"/instagram/users/"+aUser.get("id")+"-"+aUser.get("username")+"/instagram-token-for-"+aUser.get("id")+"-"+aUser.get("username")+".ser";
	//			String coded = FoursquareService.getFilesafeUsernameForUser(aUser);
	//			String path = String.format(USERS_SER_TOKEN, coded, coded);
	//
	//			InputStream file = new FileInputStream( path );
	//			InputStream buffer = new BufferedInputStream( file );
	//			ObjectInput input = new ObjectInputStream ( buffer );
	//			try{
	//				//deserialize the List
	//				result = (Token)input.readObject();
	//				//display its data
	//				logger.debug("Deserialized Token is: "+result);
	//			}
	//			finally{
	//				input.close();
	//			}
	//		}
	//		catch(ClassNotFoundException ex){
	//			logger.error("Cannot perform input. Class not found.", ex);
	//			ex.printStackTrace();
	//		}
	//		catch(IOException ex){
	//			logger.error("Cannot perform input.", ex);
	//			ex.printStackTrace();
	//		}
	//		return result;
	//	}

	//	public void writeJSONToFile(JSONObject objToWrite, File aDir, String aName)
	//	{
	//		logger.debug("writeJSONToFile "+aDir+" "+aDir.exists());
	//		logger.debug("and "+aDir.getPath()+" "+aDir.getAbsolutePath());
	//		createDirectoryHierarchy(aDir, new File("/"));
	//
	//		/*		try {
	//			if(!aDir.exists()) {
	//				String[] subDirs = aDir.getPath().split(Pattern.quote(File.separator));
	//				List<String> dirs = Arrays.asList(subDirs);
	//				InstagramService.mkDirs(new File("."), dirs, dirs.size());
	//			}
	//		 */			
	//		try {
	//			File aFile = new File(aDir, aName);
	//			OutputStreamWriter char_output = new OutputStreamWriter(new FileOutputStream(aFile), Charset.forName("UTF-8").newEncoder());
	//			char_output.write(objToWrite.toJSONString());
	//			char_output.flush();
	//			char_output.close();
	//		} catch(IOException ioe) {
	//			ioe.printStackTrace();
	//		}
	//	}
	//

	//	public void writeJSONToFile(JSONArray arrayToWrite, File aDir, String aName)
	//	{
	///*		logger.debug("writeJSONToFile "+aDir+" "+aDir.exists());
	//		logger.debug("and "+aDir.getAbsolutePath()+" "+aDir.getPath());
	//		logger.info("writeJSONToFile "+aDir+" "+aDir.exists());
	//		logger.info("and "+aDir.getAbsolutePath()+" "+aDir.getPath());
	//*/		createDirectoryHierarchy(aDir, new File("/"));
	//		/*		try {
	//			if(!aDir.exists()) {
	//				String[] subDirs = aDir.getAbsolutePath().split(Pattern.quote(File.separator));
	//				List<String> dirs = Arrays.asList(subDirs);
	//				InstagramService.mkDirs(new File("."), dirs, dirs.size());
	//			}
	//		 */			
	//		try {
	//			File aFile = new File(aDir, aName);
	///*			logger.debug("writeJSONToFile with "+aDir+" "+aName);
	//			logger.debug("trying to write JSON data to "+aFile);
	//			logger.debug("does the directory exist?"+aDir.exists());
	//			logger.debug("does the file exist?"+aFile.exists());
	//*/			OutputStreamWriter char_output = new OutputStreamWriter(new FileOutputStream(aFile), Charset.forName("UTF-8").newEncoder());
	//			char_output.write(arrayToWrite.toJSONString());
	//			char_output.flush();
	//			char_output.close();
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		}
	//
	//
	//	}


	//	protected void createDirectoryHierarchy(File aDir, File root)
	//	{
	//		try {
	//			if(!aDir.exists()) {
	//				String[] subDirs = aDir.getPath().split(Pattern.quote(File.separator));
	//				List<String> dirs = Arrays.asList(subDirs);
	//				FoursquareService.mkDirs(root, dirs, dirs.size());
	//			}
	//		} catch(Exception e) {
	//			//logger.error(e.getStackTrace());
	//			e.printStackTrace();
	//		}
	//
	//	}
	//
	//	protected static void mkDirs(File root, List<String> dirs, int depth) {
	//		if (depth == 0) return;
	//		for (String s : dirs) {
	//			File subdir = new File(root, s);
	//			if(!subdir.exists()) {
	//				logger.debug("Subdir "+subdir);
	//				subdir.mkdir();
	//			}
	//			root = subdir;
	//			//		    mkDirs(subdir, dirs, depth - 1);
	//		}
	//	}



	//	public static JSONArray getFollowsFor(String aCodedUser)
	//	{
	//		JSONArray result = new JSONArray();
	//		// have to find a path to a file by a unique userID, which will also contain the username, but we don't need it for sure
	//		Path startingDir = Paths.get(USERS_DB_PATH_ROOT);
	//		Finder finder = new Finder(aCodedUser+"-follows.json");
	//		try {
	//			Files.walkFileTree(startingDir, finder);
	//		} catch (IOException e) {
	//			// TODO Auto-generated catch block
	//			//e.printStackTrace();
	//			logger.error(e);
	//		}
	//		List<Path> results = finder.results;		
	//		if(results != null && results.size() > 0) {
	//			try {
	//				JSONParser parser = new JSONParser();
	//				File aFile = results.get(0).toFile();
	//				result = (JSONArray)parser.parse(new FileReader(aFile));
	//			} catch(IOException | ParseException ioe) {
	//				logger.error(ioe);
	//			}
	//
	//		}
	//
	//		return result;
	//	}


	//	public static JSONArray getFollowsForUserID(String aUserID)
	//	{
	//		JSONArray result = new JSONArray();
	//		// have to find a path to a file by a unique userID, which will also contain the username, but we don't need it for sure
	//		Path startingDir = Paths.get(USERS_DB_PATH_ROOT);
	//		Finder finder = new Finder(aUserID+"-*-follows.json");
	//		try {
	//			Files.walkFileTree(startingDir, finder);
	//		} catch (IOException e) {
	//			// TODO Auto-generated catch block
	//			//e.printStackTrace();
	//			logger.error(e);
	//		}
	//		List<Path> results = finder.results;		
	//		if(results != null && results.size() > 0) {
	//			try {
	//				JSONParser parser = new JSONParser();
	//				File aFile = results.get(0).toFile();
	//				result = (JSONArray)parser.parse(new FileReader(aFile));
	//			} catch(IOException | ParseException ioe) {
	//				logger.error(ioe);
	//			}
	//
	//		}
	//
	//		return result;
	//	}

	//	protected static void mkDirs(File root, List<String> dirs, int depth) {
	//		if (depth == 0) return;
	//		for (String s : dirs) {
	//			File subdir = new File(root, s);
	//			if(!subdir.exists()) {
	//				logger.debug("Subdir "+subdir);
	//				subdir.mkdir();
	//			}
	//			root = subdir;
	//			//		    mkDirs(subdir, dirs, depth - 1);
	//		}
	//	}




}
