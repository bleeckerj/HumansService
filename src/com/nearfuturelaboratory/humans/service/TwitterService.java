package com.nearfuturelaboratory.humans.service;

import org.scribe.builder.*;
import org.scribe.builder.api.*;
import org.scribe.model.*;
import org.scribe.oauth.*;
import org.json.simple.*;
import org.mongodb.morphia.Key;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mongodb.DB;
import com.nearfuturelaboratory.humans.dao.ServiceTokenDAO;
import com.nearfuturelaboratory.humans.dao.TwitterStatusDAO;
import com.nearfuturelaboratory.humans.dao.TwitterUserDAO;
import com.nearfuturelaboratory.humans.entities.ServiceToken;
import com.nearfuturelaboratory.humans.twitter.entities.TwitterFollows;
import com.nearfuturelaboratory.humans.twitter.entities.TwitterFollowsDAO;
import com.nearfuturelaboratory.humans.twitter.entities.TwitterStatus;
import com.nearfuturelaboratory.humans.twitter.entities.TwitterUser;
import com.nearfuturelaboratory.humans.util.MongoUtil;
import com.nearfuturelaboratory.util.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.istack.internal.NotNull;

import org.apache.log4j.Logger;


public class TwitterService {
	final static Logger logger = Logger.getLogger(com.nearfuturelaboratory.humans.service.TwitterService.class);

	private String apiKey = Constants.getString("TWITTER_API_KEY");//"09ARKva0K7HMz1DW1GUg";
	private String apiSecret = Constants.getString("TWITTER_API_SECRET");//"rwy7rZ2Uu3lkliYMfOaJD4UeUHFebDqXXrBgjnT8Rw";
	private String callbackURL = Constants.getString("TWITTER_CALLBACK_URL");//"http://localhost:8080/HumansService/scrumpy-twitter";

	private Token accessToken;
	protected TwitterUser user;
	protected DB db;
	protected TwitterStatusDAO statusDAO;
	protected TwitterUserDAO userDAO;
	protected TwitterFollowsDAO followsDAO;
	protected ServiceTokenDAO tokenDAO;


	private OAuthService service;
//	private static final String FRIENDS_LIST_URL = "https://api.twitter.com/1.1/friends/list.json?user_id=%s&cursor=%s";
	private static final String FRIENDS_IDS_URL = "https://api.twitter.com/1.1/friends/ids.json?user_id=%s&cursor=%s&count=5000";
	private static final String VERIFY_URL = "https://api.twitter.com/1.1/account/verify_credentials.json";
	private static final String SHOW_USER_BY_ID_URL = "https://api.twitter.com/1.1/users/show.json?user_id=%s&include_entities=true";
	private static final String USER_LOOKUP_URL = "https://api.twitter.com/1.1/users/lookup.json";
	private static final String STATUS_URL = "https://api.twitter.com/1.1/statuses/user_timeline.json?user_id=%s&trim_user=true";


	//	private static final String STATUS_DB_PATH = Constants.getString("SERVICE_DATA_ROOT")+"/twitter/users/%s-%s/status/";
	//	private static final String USERS_DB_PATH_ROOT = Constants.getString("SERVICE_DATA_ROOT")+"/twitter/users/";
	//	private static final String USERS_DB_PATH = Constants.getString("SERVICE_DATA_ROOT")+"/twitter/users/%s-%s/";
	//	private static final String USERS_FOLLOWS_PATH = Constants.getString("SERVICE_DATA_ROOT")+"/twitter/users/%s-%s/follows/";
	//
	//	private static final String USERS_DB_PATH_CODED = Constants.getString("SERVICE_DATA_ROOT")+"/twitter/users/%s/";
	//	private static final String USERS_SER_TOKEN = USERS_DB_PATH+"twitter-token-for-%s-%s.ser";

	//	private static final long STATUS_STALE_TIME = Constants.getLong("STATUS_STALE_TIME");
	//	private static final long USER_BASIC_STALE_TIME = Constants.getLong("USER_BASIC_STALE_TIME");

	protected Gson gson;

	//	public static TwitterService createTwitterServiceOnBehalfOfCodedUsername(String aCodedUsername) {
	//		TwitterService result;
	//		logger.debug("create twitter service on behalf of "+aCodedUsername);
	//		JSONObject user = getLocalUserBasicForCodedUser(aCodedUsername);
	//		Token token = TwitterService.deserializeToken(user);
	//		result = new TwitterService(token);
	//		result.user = user;
	//		return result;
	//	}

	public TwitterService() {
		db = MongoUtil.getMongo().getDB("twitter");

		statusDAO = new TwitterStatusDAO();
		statusDAO.ensureIndexes();

		userDAO = new TwitterUserDAO();
		userDAO.ensureIndexes();

		followsDAO = new TwitterFollowsDAO();
		followsDAO.ensureIndexes();

		tokenDAO = new ServiceTokenDAO("twitter");
		tokenDAO.ensureIndexes();

		gson = new GsonBuilder().setDateFormat("EEE MMM dd HH:mm:ss Z yyyy").serializeNulls().create();
	}

	//TODO Change this all ridiculous constructor. Should all be factory methods like above.
	public TwitterService(@NotNull Token aAccessToken) {
		this();
		accessToken = aAccessToken;
		service = new ServiceBuilder()
		.provider(TwitterApi.class)
		.apiKey(apiKey)
		.apiSecret(apiSecret)
		.callback(callbackURL)
		.build();
	}

	public static TwitterService createTwitterServiceOnBehalfOfUsername(String aUsername) {
		TwitterService result;
		Token token;
		TwitterUser user = TwitterService.getLocalUserBasicForUsername(aUsername);
		if(user == null) {
			logger.warn("null token. trying to find one by username "+aUsername);
			token = TwitterService.deserializeToken(aUsername);
			result = new TwitterService(token);
			user = result.serviceRequestUserBasic();
		} else {
			token = TwitterService.deserializeToken(user);
			result = new TwitterService(token);
		}
		if(token == null) {
			logger.error("null token for "+aUsername);
			return null;
		}
		result.user = user;
		return result;

	}

	private static TwitterUser getLocalUserBasicForUsername(String screen_name) {
		TwitterUser result = null;
		TwitterUserDAO dao = new TwitterUserDAO();
		result = dao.findByExactUsername(screen_name);
		return result;
	}

	/*	public JSONObject serviceRequestUserBasicForSelf(boolean save)
		{
			JSONObject result = serviceRequestUserBasicForUserID("self");
			logger.debug(result+" "+"self");
			this.user = result;
			if(save) {
				String username = (String)result.get("screen_name");
				//			String path = String.format(USERS_DB_PATH, aUserID, username);
				File f = new File(String.format(USERS_DB_PATH, aUserID, username));
				String p =  (String)user.get("id_str")+"-"+username+".json";


				writeJSONToFile(result, f, p);
			}
			return result;
		}
	 */
	//		public JSONObject serviceRequestUserBasicForUserID(String aUserID, boolean save)
	//		{
	//			JSONObject result = serviceRequestUserBasicForUserID(aUserID);
	//			//logger.debug(result+" "+aUserID);
	//			if(save) {
	//				String username = (String)result.get("screen_name");
	//				//			String path = String.format(USERS_DB_PATH, aUserID, username);
	//				File f = new File(String.format(USERS_DB_PATH, aUserID, username));
	//				String p =  result.get("id_str")+"-"+username+".json";
	//	
	//	
	//				writeJSONToFile(result, f, p);
	//			}
	//			return result;
	//		}

	/**
	 * This will go to the service and get "self" for whoever's accessToken we have
	 */
	public TwitterUser serviceRequestUserBasic() {
		OAuthRequest request = new OAuthRequest(Verb.GET, VERIFY_URL);
		service.signRequest(accessToken, request);
		Response response = request.send();

		Map<String, String> h = response.getHeaders();
		//logger.info("From Headers for Twitter Request rate-limit="+h.get("x-rate-limit-limit")+" reset ms="+h.get("x-rate-limit-reset"));


		String s = response.getBody();
		JSONObject obj = (JSONObject)JSONValue.parse(s);
		TwitterUser user;// = serviceRequestUserBasicForUserID("self");
		//aUser = (JSONObject) ((JSONObject)obj);
		
		
//		DateTimeFormatter format = 
//			    DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss Z").withLocale(Locale.ENGLISH);
		com.nearfuturelaboratory.humans.twitter.entities.TwitterUser tuser = gson.fromJson(s, 
				com.nearfuturelaboratory.humans.twitter.entities.TwitterUser.class);	
		userDAO.save(tuser);
		//this.saveUserBasicJson(obj);

		return tuser;
	}

	public TwitterUser serviceRequestUserBasicForUserID(String aUserID)
	{
		JSONObject aUser;
		String userURL = String.format(SHOW_USER_BY_ID_URL, aUserID);
		OAuthRequest request = new OAuthRequest(Verb.GET, userURL);
		service.signRequest(accessToken, request);
		Response response = request.send();
		String s = response.getBody();
		//TODO Check for errors
		Map<String, String> h = response.getHeaders();
		//logger.debug("Twitter: Request on behalf of "+this.getThisUser().get("screen_name"));
		//logger.debug("Service Request User Basic for User ID "+aUserID);
		//logger.info("From Headers for Twitter Request rate-limit="+h.get("x-rate-limit-limit")+" reset ms="+h.get("x-rate-limit-reset"));

		//System.out.println(h);
		Object obj = JSONValue.parse(s);
		aUser = (JSONObject) ((JSONObject)obj);
		//this.saveUserBasicJson(aUser);
		com.nearfuturelaboratory.humans.twitter.entities.TwitterUser tuser = gson.fromJson(aUser.toString(), 
				com.nearfuturelaboratory.humans.twitter.entities.TwitterUser.class);		

		userDAO.save(tuser);
		return tuser;
	}


//	protected Key<TwitterUser> saveUserBasicJson(JSONObject aUserJson) {
//		com.nearfuturelaboratory.humans.entities.TwitterUser tuser = gson.fromJson(aUserJson.toString(), 
//				com.nearfuturelaboratory.humans.entities.TwitterUser.class);
//		return userDAO.save(tuser);
//
//	}

	protected TwitterUser getLocalUserBasicForUserID(String aUserID) {
		TwitterUser result = null;
		result = userDAO.findByExactUserID(aUserID);
		return result;
	}

	public boolean localUserBasicIsFreshForUserID(String aUserID) {
		boolean result = false;
		com.nearfuturelaboratory.humans.twitter.entities.TwitterUser user = this.getLocalUserBasicForUserID(aUserID);

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



	public TwitterUser getThisUser()
	{
		return user;
	}

	public TwitterStatus getMostRecentStatus() {
		return statusDAO.findMostRecentStatusByExactUserID(this.getThisUser().getId());
	}
	
	public TwitterStatus getOldestStatus(String aUserID) {
		return statusDAO.findOldestStatusByExactUserID(aUserID);
	}

	public List<TwitterStatus> serviceRequestStatus() {
		return serviceRequestStatusForUserID(this.user.getId_str());
	}
	
	public List<TwitterStatus> serviceRequestStatusForUserID(String aUserID) {
		//long max_id = serviceRequestStatusForUserIDAndMaxID(aUserID, null);
		if(aUserID.equalsIgnoreCase("self") || aUserID == null) {
			aUserID = user.getId_str();
		}
		List<TwitterStatus> result;
		TwitterStatus most_recent = statusDAO.findMostRecentStatusByExactUserID(aUserID);
		if(most_recent == null) {
			result = this.serviceRequestStatusForUserIDAndSinceID(aUserID, null);
		} else {
			result = serviceRequestStatusForUserIDAndSinceID(aUserID, most_recent.getId_str());

		}
		return result;
		//serviceRequestStatusForUserIDAndMaxID(aUserID, most_recent.getId_str());
		//long max_id = getOldestID();
		//serviceRequestStatusForUserIDAndMaxID(aUserID, )
	}

	protected List<TwitterStatus> serviceRequestStatusForUserIDAndSinceID(String aUserID, String since_id) {
		if(aUserID == null || aUserID.equalsIgnoreCase("self")) {
			//logger.debug(user.get("id").getClass());
			aUserID = user.getId_str().toString();
		}
		String statusURL = String.format(STATUS_URL, aUserID);
		OAuthRequest request = new OAuthRequest(Verb.GET, statusURL);
		request.addQuerystringParameter("count", "200");

		if(since_id != null) {
			request.addQuerystringParameter("since_id", since_id);
		}

		service.signRequest(accessToken, request);
		Response response = request.send();
		String s = response.getBody();
		//TODO error chck
		Object objResponse = JSONValue.parse(s);
		//logger.debug(objResponse);
		//TODO error checking!
		JSONArray jsonResponse = (JSONArray)objResponse;
		TwitterUser u = this.getLocalUserBasicForUserID(aUserID);
		Map h = response.getHeaders();
		String serviceUserTwitterUsername = (String)u.getScreen_name();

		logger.debug("Twitter: Getting "+serviceUserTwitterUsername+" on behalf of "+this.getThisUser().getScreen_name());
		logger.info("From Headers for Twitter Request rate-limit="+h.get("x-rate-limit-limit")+" reset ms="+h.get("x-rate-limit-reset"));


		//		JSONObject new_obj = (JSONObject)jsonResponse.get(0);
		//		JSONObject old_obj = (JSONObject)jsonResponse.get(jsonResponse.size()-1);
		////		long newest_id = 		JsonPath.read(new_obj, "id");
		//		long oldest_id = JsonPath.read(old_obj, "id");
		//writeJSONToFile(jsonResponse, new File(path), aUserID+"-"+oldest_id+"-"+newest_id+"-status.json");

		return this.saveStatusJson(jsonResponse);
	}

	public List<TwitterStatus> getStatus() {
		return getStatusForUserID(this.getThisUser().getId_str());
	}


	public List<TwitterStatus> getStatusForUserID(String aUserID) {
		//List<TwitterStatus> result = new ArrayList<TwitterStatus>();
		return statusDAO.findByExactUserID(aUserID);
		//		List<Path>statusPaths = getStatusPaths(aUserID);
		//
		//		// load each file, iterate the status
		//		for(int i=0; statusPaths != null && i < statusPaths.size(); i++) {
		//			Path path = statusPaths.get(i);
		//			File f = path.toFile();
		//			JsonParser parser = new JsonParser();
		//			Gson gson = new GsonBuilder().setDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy").create();
		//			try {
		//				
		//				InputStreamReader char_input = 
		//						new InputStreamReader(new FileInputStream(f),Charset.forName("UTF-8").newDecoder());
		//				
		//				Object obj = parser.parse(char_input);
		//				JsonArray statuses = (JsonArray)obj;
		//				for(int j=0; j<statuses.size(); j++) {
		//					if(statuses.get(i).isJsonObject()) {
		//						JsonObject tmp = (JsonObject)statuses.get(j);
		//						TwitterStatus ts = gson.fromJson(statuses.get(j), TwitterStatus.class);
		//						//ts.setStatusJSON(statuses.get(j).getAsJsonObject());
		//						result.add(ts);
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
		//return result;
	}

	//	protected List<Path> getStatusPaths(String aUserID) {
	//		Path result = null;
	//		Path startingDir = Paths.get(USERS_DB_PATH_ROOT);
	//		String pattern = aUserID+"-\\d*-\\d*-status.json";
	//		//logger.debug(aUserID+" "+pattern);
	//
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
	//
	//
	//	protected List<Path> getStatusPaths() {
	//		return getStatusPaths(this.getThisUser().get("id").toString());
	//	}

	protected void serviceRequestStatusForUserIDAndMaxID(String aUserID, String max_id) {
		Object objResponse = null;
		try {
			if(aUserID == null || aUserID.equalsIgnoreCase("self")) {
				aUserID = user.getId_str().toString();
			}
			String statusURL = String.format(STATUS_URL, aUserID);
			OAuthRequest request = new OAuthRequest(Verb.GET, statusURL);
			request.addQuerystringParameter("count", "200");

			if(max_id != null) {
				request.addQuerystringParameter("max_id", max_id);
			}

			service.signRequest(accessToken, request);
			Response response = request.send();
			String s = response.getBody();
			//TODO Error checking
			objResponse = JSONValue.parse(s);

			JSONArray jsonResponse = (JSONArray)objResponse;
			//Map<String, String> h = response.getHeaders();

			if(jsonResponse != null && jsonResponse.size() > 0) {
				this.saveStatusJson(jsonResponse);
			} 
		} catch (Exception e) {
			logger.error(e);
			logger.debug(objResponse.toString());
			e.printStackTrace();
		}
	}

	List<TwitterStatus> saveStatusJson(JSONArray status) {
		List<TwitterStatus>result = new ArrayList<TwitterStatus>();
		
		@SuppressWarnings("unchecked")
		Iterator<JSONObject> iter = status.iterator();
		while(iter.hasNext()) {
			String i = iter.next().toString();
			//logger.debug(i);
			com.nearfuturelaboratory.humans.twitter.entities.TwitterStatus tstatus = gson.fromJson(i, com.nearfuturelaboratory.humans.twitter.entities.TwitterStatus.class);
			result.add(tstatus);
			statusDAO.save(tstatus);
		}
		return result;

	}

	protected static Comparator<JSONObject> TwitterStatusIDComparator = new Comparator<JSONObject>() {
		public int compare(JSONObject status_1, JSONObject status_2) 
		{
			int result = 0;
			if(status_1 != null && status_2 != null) {
				long status_1_id = Long.parseLong(status_1.get("id").toString());
				long status_2_id = Long.parseLong(status_2.get("id").toString());
				if(status_1_id > status_2_id) {
					result = 1;
				} else {
					result= -1;
				}
			}
			return result;
		}
	};


	//	protected long getTimeOfOldestID(JSONArray status) {
	//		long oldestID = getOldestID(status);
	//		JSONObject oldest_status = getOldestStatus(status);
	//		String created = oldest_status.get("created_at").toString();
	//		SimpleDateFormat format = new SimpleDateFormat("E MMM dd HH:mm:ss Z YYYY");
	//		Date date = new Date();
	//		try {
	//			date = format.parse(created);
	//		} catch (java.text.ParseException e) {
	//			logger.error(e);
	//			e.printStackTrace();
	//		}
	//		return date.getTime();
	//	}
	//
	//
	//	protected JSONObject getOldestStatus(JSONArray status) {
	//		status.toArray();
	//		Collections.sort(status, TwitterService.TwitterStatusIDComparator);
	//		JSONObject result;
	//		JSONObject obj = (JSONObject) status.get(0);
	//		result = (JSONObject) status.get(0);
	//		return result;
	//	}
	//
	//	protected long getOldestID(JSONArray status) {
	//		//status.iterator();
	//		status.toArray();
	//		Collections.sort(status, TwitterService.TwitterStatusIDComparator);
	//		long result;
	//		JSONObject obj = (JSONObject) status.get(0);
	//		result = Long.valueOf( obj.get("id").toString());
	//		return result;
	//		//Arrays.sort(status.toArray(), TwitterService.TwitterStatusComparator);
	//	}



	//	public static JSONArray getHydratedFollowsFor(String aCodedUsername)
	//	{
	//		JSONArray result;
	//		JSONParser parser = new JSONParser();
	//		File f = new File(String.format(USERS_DB_PATH_CODED, aCodedUsername)+"/follows/"+aCodedUsername+"-hydratedfollows.json");
	//		//logger.debug("Getting hydrated follows from :"+f);
	//		try {
	//			result = (JSONArray)parser.parse(new FileReader(f));
	//			//logger.debug(aCodedUsername+" had "+result.size()+" hydrated follows from "+f);
	//		} catch (IOException | ParseException e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//			logger.error(e);
	//			result = new JSONArray();
	//		}
	//
	//		return result;
	//	}

	//TODO ugh. need to get the path to the status file for this user
	// probably first find their root directory given only the userID
	// then look below that. STATUS_DB_PATH is the wrong path
	//	protected static Path getStatusPathForUserID(String aUserID) {
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

	//	protected static Path getStatusPathForUsername(String aUsername) {
	//		Path result = null;
	//		Path startingDir = Paths.get(STATUS_DB_PATH);
	//		String pattern = "*-"+aUsername+"*[!follows].json";
	//		Finder finder = new Finder(pattern);
	//		try {
	//			Files.walkFileTree(startingDir, finder);
	//		} catch (IOException e) {
	//			//e.printStackTrace();
	//			logger.error(e);
	//		}
	//		//logger.debug(finder.results);
	//		List<Path> results = finder.results;
	//		if(results != null && results.size() > 0) {
	//			result = results.get(0);
	//		}
	//		return result;		
	//	}


	public boolean localServiceStatusIsFreshForUserID(String aUserID) {
		boolean result = false;
		com.nearfuturelaboratory.humans.twitter.entities.TwitterStatus most_recent = this.getMostRecentStatusForUserID(aUserID);
		if(most_recent == null) {
			return result;
		}
		Date d = most_recent.getLastUpdated();

		long then = d.getTime();
		long now = new Date().getTime();

		long diff = now - then;
		if(diff < Constants.getLong("STATUS_STALE_TIME")) {
			result = true;
		}

		return result;
	}

	//	public boolean localServiceStatusIsFreshForUserID(String aServiceID) {
	//		boolean result = false;
	//		List<Path>statusPaths = getStatusPaths(aServiceID);
	//		if(statusPaths == null || statusPaths.size() < 1) {
	//			return false;
	//		}
	//		Iterator<Path> iter = statusPaths.iterator();
	//		long now = new Date().getTime();
	//
	//		long oldest = now;
	//		while(iter.hasNext()) {
	//			Path p = iter.next();
	//			File f = p.toFile();
	//			long l = f.lastModified();
	//			if(l < oldest) oldest = l;
	//		}
	//
	//		long diff = now - oldest;
	//		if(diff < Constants.getLong("STATUS_STALE_TIME")) {
	//			result = true;
	//		}
	//
	//		return result;
	//	}


	//	public boolean localServiceStatusIsFreshForUserID(String aUserID) {
	//		long modified_time = Long.MAX_VALUE;
	//		boolean result = false;
	//		if(aUserID.equalsIgnoreCase("self")) {
	//			aUserID = this.getThisUser().get("id").toString();
	//		}
	//		Path path = getStatusPathForUserID(aUserID);
	//		File f = null;
	//		long diff = 0l;
	//
	//		if(path != null) {
	//			f = path.toFile();
	//			modified_time = f.lastModified();
	//			long now = new Date().getTime();
	//			diff  = now - modified_time;
	//			if(diff < STATUS_STALE_TIME) {
	//				result = true;
	//			}
	//		} else {
	//			result = false;
	//		}
	//		
	//		String freshOrStale = (result?"BEEN FRESH for":"STALE by");
	//		logger.debug("checking freshness of "+f+"   "+freshOrStale+" "+diff/1000l/60l+" minutes. "+(STATUS_STALE_TIME-diff)/1000l+" seconds to go.");
	//
	//		return result;
	//	}

	private TwitterStatus getMostRecentStatusForUserID(String aUserID) {
		return statusDAO.findMostRecentStatusByExactUserID(aUserID);
	}

	//	public static long getHydratedFollowsModifiedTime(String aCodedUsername) 
	//	{
	//		long result = Long.MAX_VALUE;
	//		File f = new File(String.format(USERS_DB_PATH_CODED, aCodedUsername)+"/follows/"+aCodedUsername+"-hydratedfollows.json");
	//		result = f.lastModified();
	//		return result;
	//	}
	//

	public void serviceRequestFollows() {
		serviceRequestFollowsForUserID((String)user.getId_str());
	}


	//	public void unpackFollowsFor(String aUserID)
	//	{
	//		JSONArray follows = getFollowsLocal(aUserID);
	//		Iterator iter = follows.iterator();
	//		while(iter.hasNext()) {
	//			JSONObject obj = (JSONObject) iter.next();
	//			String path = String.format(USERS_DB_PATH, obj.get("id"), obj.get("screen_name"));
	//			writeJSONToFile(obj, new File(path), obj.get("id")+"-"+obj.get("screen_name")+".json");
	//		}
	//
	//	}

	//	public JSONArray getFollowsLocal(String aUserID)
	//	{
	//		JSONArray jsonArray = new JSONArray();
	//		Pair<List<Path>, Boolean> local = isFollowsLocal(aUserID);
	//		// consume the stuff in the List of <Path>
	//		List<Path> paths = local.getFirst();
	//		if(paths != null && paths.size() > 0 && (Boolean)local.getSecond().booleanValue() == true) {
	//			JSONParser parser = new JSONParser();
	//
	//			Path path = (Path)paths.get(0);
	//			Object obj = null;
	//			try {
	//				obj = parser.parse(new FileReader(path.toFile()));
	//			} catch (FileNotFoundException e) {
	//				logger.error(e);
	//				e.printStackTrace();
	//			} catch (ParseException e) {
	//				logger.error(e);
	//				e.printStackTrace();
	//			} catch (IOException e) {
	//				logger.error(e);
	//				e.printStackTrace();
	//			} 
	//
	//			jsonArray = (JSONArray) obj;
	//		}
	//		return jsonArray;
	//	}


	/**
	 * Check to see if a userid has its follows data locally.
	 * If it does, return the file path
	 * 
	 * @param aUserID
	 * @return
	 */
	//	public Pair<List<Path>, Boolean> isFollowsLocal(String aUserID) {
	//		//boolean result = false;
	//		Path startingDir = Paths.get(USERS_DB_PATH_ROOT);
	//		String pattern = aUserID+"-hydratedfollows.json";
	//		Pair<List<Path>, Boolean> result = new Pair<List<Path>, Boolean>(null, new Boolean(false));
	//		Finder finder = new Finder(pattern);
	//		try {
	//			Files.walkFileTree(startingDir, finder);
	//			logger.debug(finder.results);
	//			if(finder.results != null & finder.results.size() > 0) {
	//				result.setFirst(finder.results);
	//				result.setSecond(new Boolean(true));
	//			}
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//			logger.error(e);
	//		}
	//
	//		return result;
	//	}


	@SuppressWarnings("unchecked")
	public void serviceRequestFollowsForUserID(String aUserID) {
		String id_str = (String)user.getId_str();
		if(aUserID == null) {
			aUserID = id_str;
		}

		TwitterUser aUser;

		if(aUserID.equalsIgnoreCase(id_str)) {
			aUser = user;	
		} else {
			// check to see if we already have this user..
			if(this.localUserBasicIsFreshForUserID(aUserID)) {
				aUser = userDAO.findByExactUserID(aUserID);
			} else {
				aUser = serviceRequestUserBasicForUserID(aUserID);
			}
		}

		String followsURL = String.format(FRIENDS_IDS_URL, aUserID, "-1");
		OAuthRequest request = new OAuthRequest(Verb.GET, followsURL);
		service.signRequest(accessToken, request);

		Response response = request.send();
		//TODO Error checking
		String s = response.getBody();

		Map<String, String> h = response.getHeaders();

		//logger.info("From Headers for Twitter Request rate-limit="+h.get("x-rate-limit-limit")+" reset ms="+h.get("x-rate-limit-reset"));
		Object obj=JSONValue.parse(s);
		JSONObject map = (JSONObject)obj;

		// error check
		if(map != null && map.get("errors") == null) {
			Long next_cursor = (Long)map.get("next_cursor");
			long next_cursor_l = next_cursor.longValue();
			//System.out.println("next_cursor="+next_cursor);
			JSONArray allFollowsIDs = new JSONArray();
			JSONArray allFollowsHydrated = new JSONArray();
			int page_count = 1;
			do {
				//			JSONArray users = (JSONArray)map.get("users");
				JSONArray users = (JSONArray)map.get("ids");
				allFollowsIDs.addAll(users);
				String next_url = String.format(FRIENDS_IDS_URL, user.getScreen_name(), next_cursor);   //(String)pagination.get("next_url");
				next_cursor_l = next_cursor.longValue();
				if(next_cursor != null && next_cursor_l != 0) {
					request = new OAuthRequest(Verb.GET, (String)next_url);
					service.signRequest(accessToken, request);
					response = request.send();
					s = response.getBody();
					map = (JSONObject)JSONValue.parse(s);
					next_cursor = (Long)map.get("next_cursor");
					h = response.getHeaders();
					logger.debug("paging ("+page_count++ +") through follows for "+user.getScreen_name());
					//logger.info("From Headers for Twitter Request rate-limit="+h.get("x-rate-limit-limit")+" reset ms="+h.get("x-rate-limit-reset"));

					//logger.debug("Next URL: "+next_url);
					//logger.debug("next_cursor: "+next_cursor);
				} else {
					//logger.debug("Response "+map);
					//logger.debug("next_cursor is "+next_cursor);

					break;
				}
			} while(next_cursor != null && next_cursor_l != 0);


			JSONObject meta = (JSONObject)map.get("meta");
			//			this.saveFollows();
			//			String p = String.format(USERS_FOLLOWS_PATH, aUser.get("id"), aUser.get("screen_name"));
			//			writeJSONToFile(allFollowsIDs, new File(p), aUser.get("id")+"-"+aUser.get("screen_name")+"-follows.json");

			OAuthRequest postUserLookup = new OAuthRequest(Verb.POST, USER_LOOKUP_URL);
			StringBuffer buf;// = new StringBuffer();

			//Iterator iter = allFollows.iterator();

			List<String> chunks = com.google.common.collect.Lists.partition(allFollowsIDs, 100);

			Iterator iter = chunks.iterator();
			while(iter.hasNext()) {
				List<Long>chunk = (List<Long>)iter.next();
				buf = new StringBuffer();
				for(int i=0; (i<100 && i < chunk.size()); i++) {
					buf.append(chunk.get(i));
					buf.append(",");
				}
				buf.deleteCharAt(buf.length()-1);

				postUserLookup = new OAuthRequest(Verb.POST, USER_LOOKUP_URL);
				postUserLookup.setConnectionKeepAlive(false);

				postUserLookup.addBodyParameter("user_id", buf.toString());
				service.signRequest(accessToken, postUserLookup);
				response = postUserLookup.send();
				s= response.getBody();
				//System.out.println("s="+s);
				h = response.getHeaders();
				//logger.info("From Headers for Twitter Request rate-limit="+h.get("x-rate-limit-limit")+" reset ms="+h.get("x-rate-limit-reset"));

				JSONArray usersArray = (JSONArray)JSONValue.parse(s);
				allFollowsHydrated.addAll(usersArray);

				//System.out.println("response="+s);

			}

			saveFollows(allFollowsHydrated, aUserID);


			//			p = String.format(USERS_FOLLOWS_PATH, aUser.get("id"), aUser.get("screen_name"));
			//			writeJSONToFile(allFollowsHydrated, new File(p), aUser.get("id")+"-"+aUser.get("screen_name")+"-hydratedfollows.json");
			//System.out.println(allFollowsHydrated);

		} else {
			//throw new Exception("Do something about rate limit errors, etc."+map.toString());
			logger.warn("Do something about rate limit errors, etc."+map.toString());
		}


	}

	protected List<TwitterFollows> saveFollows(JSONArray arrayOfFollows, String follower_id) {
		if(arrayOfFollows != null) {
			// really, the first thing we want to do is delete all the follows because
			// the people we follows is dynanamic — we unfollow, too. so, we want this
			// list to be up to date and not contain anyone we're now not following anymore
			// best way? i think delete them all, then we'll add below
			followsDAO.deleteByFollowerID(follower_id);
			@SuppressWarnings("unchecked")
			Iterator<JSONObject> iter = arrayOfFollows.iterator();
			while(iter.hasNext()) {
				String u = iter.next().toJSONString();
				// save the user we're following into the user collection.
				com.nearfuturelaboratory.humans.twitter.entities.TwitterUser twitter_user = gson.fromJson(u, com.nearfuturelaboratory.humans.twitter.entities.TwitterUser.class);
				userDAO.save(twitter_user);
				TwitterFollows twitter_follows = followsDAO.findFollowsByUserIDFollowsID(twitter_user.getId_str(), follower_id);
				if(twitter_follows == null) {		
						twitter_follows = new TwitterFollows();
				}
				// save the user we're following into the userfollows collection
				// we need this collection when we list users we follow as a way to find
				// the humans we want to add into an aggregate human
				twitter_follows.setUser(twitter_user);
				twitter_follows.setFollower_id(follower_id);
				followsDAO.save(twitter_follows);
			}
		}
		return followsDAO.findFollowsByExactUserID(this.getThisUser().getId_str());
	}

	//	public List<JSONObject> getUsersFromFollows(JSONArray arrayOfUsers)
	//	{
	//		List<JSONObject> result = new ArrayList<JSONObject>();
	//		for(int i=0; i<arrayOfUsers.size(); i++) {
	//			result.add((JSONObject)arrayOfUsers.get(i));
	//		}
	//
	//		return result;
	//	}


	//	public void writeJSONToFile(JSONArray arrayToWrite, File aDir, String aName)
	//	{
	//		//logger.debug("writeJSONToFile "+aDir+"/"+aName);
	//		//logger.debug("writeJSONToFile "+aDir+" "+aDir.exists());
	//		//System.out.println("and "+aDir.getAbsolutePath()+" "+aDir.getPath());
	//		//logger.debug("and "+aDir.getAbsolutePath()+" "+aDir.getPath());
	//		createDirectoryHierarchyFromRoot(aDir);
	//		try {
	//			File aFile = new File(aDir, aName);
	//
	//			OutputStreamWriter char_output = new OutputStreamWriter(new FileOutputStream(aFile), Charset.forName("UTF-8").newEncoder());
	//			char_output.write(arrayToWrite.toJSONString());
	//			char_output.flush();
	//			char_output.close();
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//			logger.error(e);
	//		}
	//
	//
	//	}

	//	public void writeJSONToFile(JSONObject objToWrite, File aDir, String aName)
	//	{
	//
	//		createDirectoryHierarchyFromRoot(aDir);
	//		try {
	//			File aFile = new File(aDir, aName);
	//			//System.out.println("wrote user to "+aFile);
	//			OutputStreamWriter char_output = new OutputStreamWriter(new FileOutputStream(aFile), Charset.forName("UTF-8").newEncoder());
	//			char_output.write(objToWrite.toJSONString());
	//			char_output.flush();
	//			char_output.close();
	//		} catch(IOException ioe) {
	//			ioe.printStackTrace();
	//			logger.error(ioe);
	//		}
	//	}

	//TODO This is ridiculous
	//	protected void createDirectoryHierarchyFromRoot(File aDir)
	//	{
	//		try {
	//			if(!aDir.exists()) {
	//				String[] subDirs = aDir.getPath().split(Pattern.quote(File.separator));
	//				List<String> dirs = Arrays.asList(subDirs);
	//				TwitterService.mkDirs(new File("/"), dirs, dirs.size());
	//			}
	//		} catch(Exception e) {
	//			e.printStackTrace();
	//			logger.error(e);
	//		}
	//	}

	/**
	 * Weird bootstrap method needed while migrating. We really should not have a token
	 * if we don't have a local user in the database, but we did when we migrated Tokens from the
	 * old filesystem database and the database had no users in it yet. Really, when you
	 * save a new user you also save the user basic with the token
	 * 
	 * @param aUsername
	 */
	private static Token deserializeToken(String aUsername) {
		ServiceTokenDAO dao = new ServiceTokenDAO("twitter");
		ServiceToken st = dao.findByExactUsername(aUsername);
		return st.getToken();
	}

	public static void serializeToken(Token aToken, TwitterUser aUser) {
		ServiceTokenDAO dao = new ServiceTokenDAO("twitter");
		ServiceToken tokenToSave = new ServiceToken();
		tokenToSave.setToken(aToken);
		tokenToSave.setUser_id(aUser.getId_str());
		tokenToSave.setUsername(aUser.getScreen_name());
		tokenToSave.setServicename("twitter");
		dao.save(tokenToSave);
	}


	public static Token deserializeToken(@NotNull TwitterUser aUser) {
		//Token result = null;
		ServiceTokenDAO dao = new ServiceTokenDAO("twitter");
		ServiceToken serviceToken = dao.findByExactUserId( aUser.getId_str() );
		return serviceToken.getToken();
	}


	//	@Deprecated
	//	public static Token deserializeToken(JSONObject aUser) {
	//		Token result = null;
	//		String path = null;
	//		try{
	//			//use buffering
	//
	//			path = String.format(USERS_SER_TOKEN, aUser.get("id"), aUser.get("screen_name"), aUser.get("id"), aUser.get("screen_name"));
	//
	//			InputStream file = new FileInputStream( path );
	//			InputStream buffer = new BufferedInputStream( file );
	//			ObjectInput input = new ObjectInputStream ( buffer );
	//			try{
	//				//deserialize the List
	//				result = (Token)input.readObject();
	//				//display its data
	//				//logger.debug("Deserialized Token is: "+result);
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
	//		catch(NullPointerException ex) {
	//			logger.debug(aUser);
	//			logger.debug(path);
	//			logger.error(ex);
	//			logger.error(Thread.currentThread().getStackTrace());
	//		}
	//		return result;
	//	}

	//	public static void mkDirs(File root, List<String> dirs, int depth) {
	//		FileUtils.mkDirs(root, dirs);
	//	}
	//		if (depth == 0) return;
	//		for (String s : dirs) {
	//			File subdir = new File(root, s);
	//			if(!subdir.exists()) {
	//				//System.out.println("Subdir "+subdir);
	//				subdir.mkdir();
	//			}
	//			root = subdir;
	//			//		    mkDirs(subdir, dirs, depth - 1);
	//		}
	//	}


}
