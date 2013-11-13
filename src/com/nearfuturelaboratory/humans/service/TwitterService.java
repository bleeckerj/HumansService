package com.nearfuturelaboratory.humans.service;

import org.scribe.builder.*;
import org.scribe.builder.api.*;
import org.scribe.model.*;
import org.scribe.oauth.*;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;

import com.nearfuturelaboratory.humans.service.status.InstagramStatus;
import com.nearfuturelaboratory.humans.service.status.TwitterStatus;
import com.nearfuturelaboratory.util.*;
import com.nearfuturelaboratory.util.file.*;
import com.google.common.collect.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.jayway.jsonpath.JsonPath;
import com.nearfuturelaboratory.util.file.Find.Finder;

import org.apache.commons.io.filefilter.*;
import org.apache.log4j.Logger;


public class TwitterService {
	final static Logger logger = Logger.getLogger("com.nearfuturelaboratory.humans.test.Test");

	private String apiKey = Constants.getString("TWITTER_API_KEY");//"09ARKva0K7HMz1DW1GUg";
	private String apiSecret = Constants.getString("TWITTER_API_SECRET");//"rwy7rZ2Uu3lkliYMfOaJD4UeUHFebDqXXrBgjnT8Rw";
	private String callbackURL = Constants.getString("TWITTER_CALLBACK_URL");//"http://localhost:8080/HumansService/scrumpy-twitter";

	private Token accessToken;
	protected JSONObject user;
	private OAuthService service;
	private static final String FRIENDS_LIST_URL = "https://api.twitter.com/1.1/friends/list.json?user_id=%s&cursor=%s";
	private static final String FRIENDS_IDS_URL = "https://api.twitter.com/1.1/friends/ids.json?user_id=%s&cursor=%s&count=5000";
	private static final String VERIFY_URL = "https://api.twitter.com/1.1/account/verify_credentials.json";
	private static final String SHOW_USER_BY_ID_URL = "https://api.twitter.com/1.1/users/show.json?user_id=%s&include_entities=true";
	private static final String USER_LOOKUP_URL = "https://api.twitter.com/1.1/users/lookup.json";
	private static final String STATUS_URL = "https://api.twitter.com/1.1/statuses/user_timeline.json?user_id=%s&trim_user=true";


	private static final String STATUS_DB_PATH = Constants.getString("SERVICE_DATA_ROOT")+"/twitter/users/%s-%s/status/";
	private static final String USERS_DB_PATH_ROOT = Constants.getString("SERVICE_DATA_ROOT")+"/twitter/users/";
	private static final String USERS_DB_PATH = Constants.getString("SERVICE_DATA_ROOT")+"/twitter/users/%s-%s/";
	private static final String USERS_FOLLOWS_PATH = Constants.getString("SERVICE_DATA_ROOT")+"/twitter/users/%s-%s/follows/";

	private static final String USERS_DB_PATH_CODED = Constants.getString("SERVICE_DATA_ROOT")+"/twitter/users/%s/";
	private static final String USERS_SER_TOKEN = USERS_DB_PATH+"twitter-token-for-%s-%s.ser";

	private static final long STATUS_STALE_TIME = Constants.getLong("STATUS_STALE_TIME");
	private static final long USER_BASIC_STALE_TIME = Constants.getLong("USER_BASIC_STALE_TIME");
	
	
	public static TwitterService createTwitterServiceOnBehalfOfCodedUsername(String aCodedUsername) {
		TwitterService result;
		logger.debug("create twitter service on behalf of "+aCodedUsername);
		JSONObject user = getLocalUserBasicForCodedUser(aCodedUsername);
		Token token = TwitterService.deserializeToken(user);
		result = new TwitterService(token);
		result.user = user;
		return result;
	}

//	public static TwitterService createTwitterServiceOnBehalfOf(String aOnBehalfOf) {
//		TwitterService result;
//		logger.debug("create twitter service on behalf of "+aOnBehalfOf);
//		JSONObject user = getLocalUserBasicForCodedUser(aCodedUsername);
//		Token token = TwitterService.deserializeToken(user);
//		result = new TwitterService(token);
//		result.user = user;
//		return result;
//		
//	}
	
	//TODO Change this all �����ridiculous constructor. Should all be factory methods like above.
	public TwitterService(Token aAccessToken) {
		accessToken = aAccessToken;
		service = new ServiceBuilder()
		.provider(TwitterApi.class)
		.apiKey(apiKey)
		.apiSecret(apiSecret)
		.callback(callbackURL)
		.build();
	}

	/**
	 * This will go to the service and get "self" for whoever's accessToken we have
	 */
	public void serviceRequestUserBasic() {
		OAuthRequest request = new OAuthRequest(Verb.GET, VERIFY_URL);
		service.signRequest(accessToken, request);
		Response response = request.send();
		
		Map<String, String> h = response.getHeaders();
		logger.info("From Headers for Twitter Request rate-limit="+h.get("x-rate-limit-limit")+" reset ms="+h.get("x-rate-limit-reset"));

		
		String s = response.getBody();
		JSONObject obj = (JSONObject)JSONValue.parse(s);

		user = (JSONObject)serviceRequestUserBasicForUserID((String)obj.get("id_str"), true);
	}


	/**
	 * Well - gets a cached/stored/databased/filesystem'd JSON for this particular user
	 * instead of hitting the service
	 * @param aCodedUsername
	 * @return
	 */
	protected static JSONObject getLocalUserBasicForCodedUser(String aCodedUsername) {
		JSONObject result = null;
		JSONParser parser = new JSONParser();
		File f = new File(String.format(USERS_DB_PATH_CODED, aCodedUsername)+"/"+aCodedUsername+".json");
		try {
			result = (JSONObject)parser.parse(new FileReader(f));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			logger.error(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			logger.error(e);
		}

		return result;
	}

	/*	public Path getUserDBPathForUserID(String aUserID)
	{
		Path result = new Path()



	}
	 */	
	public JSONObject getThisUser()
	{
		return user;
	}

	//TODO This will not work. The file name is wrong �����we're naming status files with the status id..not sure I should worry too much as I'll move
	// from this silly file-based database?
	public String getStatusOldestID() {
		// get the data
		//TODO need to lock down how we do these paths and crap
		String path = String.format(STATUS_DB_PATH, getThisUser().get("id"), getThisUser().get("screen_name"))+getThisUser().get("id")+"-status.json";
		JSONParser parser = new JSONParser();
		JSONArray status;
		try {
			status = (JSONArray)parser.parse(new FileReader(new File(path)));
			this.getOldestID(status);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			logger.error(e);
		} catch (ParseException | IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			logger.error(e);
		}
		return "";
	}


	public void serviceRequestStatusForUserID(String aUserID) {
		long max_id = serviceRequestStatusForUserIDAndMaxID(aUserID, null);
		serviceRequestStatusForUserIDAndMaxID(aUserID, String.valueOf(max_id));
		//long max_id = getOldestID();
		//serviceRequestStatusForUserIDAndMaxID(aUserID, )
	}

	protected long serviceRequestStatusForUserIDAndMinID(String aUserID, String min_id) {
		if(aUserID == null || aUserID.equalsIgnoreCase("self")) {
			//logger.debug(user.get("id").getClass());
			aUserID = user.get("id").toString();
		}
		String statusURL = String.format(STATUS_URL, aUserID);
		OAuthRequest request = new OAuthRequest(Verb.GET, statusURL);
		request.addQuerystringParameter("count", "200");

		if(min_id != null) {
			request.addQuerystringParameter("min_id", min_id);
		}

		service.signRequest(accessToken, request);
		Response response = request.send();
		String s = response.getBody();
		Object objResponse = JSONValue.parse(s);
		//logger.debug(objResponse);
		//TODO error checking!
		JSONArray jsonResponse = (JSONArray)objResponse;
		JSONObject u = this.getLocalUserBasicForUserID(aUserID);
		Map h = response.getHeaders();
		String serviceUserTwitterUsername = (String)u.get("screen_name");

		logger.debug("Twitter: Getting "+serviceUserTwitterUsername+" on behalf of "+this.getThisUser().get("screen_name"));
		logger.info("From Headers for Twitter Request rate-limit="+h.get("x-rate-limit-limit")+" reset ms="+h.get("x-rate-limit-reset"));
		
		

		String path;
		if(aUserID.equalsIgnoreCase((String)user.get("screen_name"))) {
			serviceUserTwitterUsername = (String)user.get("screen_name");
			path = String.format(STATUS_DB_PATH, aUserID, serviceUserTwitterUsername);
		} else {
			path = String.format(STATUS_DB_PATH, aUserID, serviceUserTwitterUsername);
		}

		JSONObject new_obj = (JSONObject)jsonResponse.get(0);
		JSONObject old_obj = (JSONObject)jsonResponse.get(jsonResponse.size()-1);
		long newest_id = 		JsonPath.read(new_obj, "id");
		long oldest_id = JsonPath.read(old_obj, "id");
		writeJSONToFile(jsonResponse, new File(path), aUserID+"-"+oldest_id+"-"+newest_id+"-status.json");
		return newest_id;
	}
	
	public List<TwitterStatus> getStatus() {
		return getStatus(this.getThisUser().get("id").toString());
	}
	
	
	public List<TwitterStatus> getStatus(String aUserID) {
		List<TwitterStatus> result = new ArrayList<TwitterStatus>();
		List<Path>statusPaths = getStatusPaths(aUserID);

		// load each file, iterate the status
		for(int i=0; statusPaths != null && i < statusPaths.size(); i++) {
			Path path = statusPaths.get(i);
			File f = path.toFile();
			JsonParser parser = new JsonParser();
			Gson gson = new GsonBuilder().setDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy").create();
			try {
				
				InputStreamReader char_input = 
						new InputStreamReader(new FileInputStream(f),Charset.forName("UTF-8").newDecoder());
				
				Object obj = parser.parse(char_input);
				JsonArray statuses = (JsonArray)obj;
				for(int j=0; j<statuses.size(); j++) {
					if(statuses.get(i).isJsonObject()) {
						//logger.debug(statuses.get(j));
						JsonObject tmp = (JsonObject)statuses.get(j);
//						if(tmp.get("id").toString().equalsIgnoreCase("389516164449452032")) {
//							logger.debug("WHOOOOA!");
//						}
						TwitterStatus ts = gson.fromJson(statuses.get(j), TwitterStatus.class);
						ts.setStatusJSON(statuses.get(j).getAsJsonObject());
						result.add(ts);
						//logger.debug(is.toString());
					}

				}
			} catch (JsonIOException | JsonSyntaxException
					| FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error(e);
			}
		}
		return result;
	}
	
	protected List<Path> getStatusPaths(String aUserID) {
		Path result = null;
		Path startingDir = Paths.get(USERS_DB_PATH_ROOT);
		String pattern = aUserID+"-\\d*-\\d*-status.json";
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


	protected List<Path> getStatusPaths() {
		return getStatusPaths(this.getThisUser().get("id").toString());
	}

	protected long serviceRequestStatusForUserIDAndMaxID(String aUserID, String max_id) {
		
		long oldest_id = 0l;
		Object objResponse = null;
		try {
			if(aUserID == null || aUserID.equalsIgnoreCase("self")) {
				//logger.debug(user.get("id").getClass());
				aUserID = user.get("id").toString();
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
			objResponse = JSONValue.parse(s);
			//logger.debug(objResponse);
			//TODO error checking!
			//logger.debug("==================================================================");

			//logger.debug("Looking for status from "+aUserID+" on behalf of "+this.getThisUser().get("screen_name"));
			
			JSONArray jsonResponse = (JSONArray)objResponse;
			Map h = response.getHeaders();
			
			//logger.debug("Twitter: Request on behalf of "+this.getThisUser().get("screen_name"));
			//logger.info("From Headers for Twitter Request rate-limit="+h.get("x-rate-limit-limit")+" reset ms="+h.get("x-rate-limit-reset"));

			String thisUsername;
			String path;
			if(aUserID.equalsIgnoreCase((String)user.get("id").toString())) {
				thisUsername = (String)user.get("screen_name");
				path = String.format(STATUS_DB_PATH, aUserID, thisUsername);
			} else {
				JSONObject u = this.getLocalUserBasicForUserID(aUserID);//serviceRequestUserBasicForUserID(aUserID);
				thisUsername = (String)u.get("screen_name");
				path = String.format(STATUS_DB_PATH, aUserID, thisUsername);
			}
			if(jsonResponse != null && jsonResponse.size() > 0) {
			JSONObject new_obj = (JSONObject)jsonResponse.get(0);
			JSONObject old_obj = (JSONObject)jsonResponse.get(jsonResponse.size()-1);
			
			long newest_id = 	Long.parseLong(	(String) new_obj.get("id").toString() );
			oldest_id = Long.parseLong(	(String) old_obj.get("id").toString() );
			writeJSONToFile(jsonResponse, new File(path), aUserID+"-"+oldest_id+"-"+newest_id+"-status.json");
			} else {
				logger.warn("Something went wrong with a request for status. Got nothing at all back. WTF?");
				oldest_id = 0l;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e);
			logger.debug(objResponse.toString());
			e.printStackTrace();
		}
		return oldest_id;
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


	protected long getTimeOfOldestID(JSONArray status) {
		long oldestID = getOldestID(status);
		JSONObject oldest_status = getOldestStatus(status);
		String created = oldest_status.get("created_at").toString();
		SimpleDateFormat format = new SimpleDateFormat("E MMM dd HH:mm:ss Z YYYY");
		Date date = new Date();
		try {
			date = format.parse(created);
		} catch (java.text.ParseException e) {
			logger.error(e);
			e.printStackTrace();
		}
		return date.getTime();
	}


	protected JSONObject getOldestStatus(JSONArray status) {
		status.toArray();
		Collections.sort(status, TwitterService.TwitterStatusIDComparator);
		JSONObject result;
		JSONObject obj = (JSONObject) status.get(0);
		result = (JSONObject) status.get(0);
		return result;
	}

	protected long getOldestID(JSONArray status) {
		//status.iterator();
		status.toArray();
		Collections.sort(status, TwitterService.TwitterStatusIDComparator);
		long result;
		JSONObject obj = (JSONObject) status.get(0);
		result = Long.valueOf( obj.get("id").toString());
		return result;
		//Arrays.sort(status.toArray(), TwitterService.TwitterStatusComparator);
	}



	public static JSONArray getHydratedFollowsFor(String aCodedUsername)
	{
		JSONArray result;
		JSONParser parser = new JSONParser();
		File f = new File(String.format(USERS_DB_PATH_CODED, aCodedUsername)+"/follows/"+aCodedUsername+"-hydratedfollows.json");
		//logger.debug("Getting hydrated follows from :"+f);
		try {
			result = (JSONArray)parser.parse(new FileReader(f));
			//logger.debug(aCodedUsername+" had "+result.size()+" hydrated follows from "+f);
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e);
			result = new JSONArray();
		}

		return result;
	}

	//TODO ugh. need to get the path to the status file for this user
	// probably first find their root directory given only the userID
	// then look below that. STATUS_DB_PATH is the wrong path
	protected static Path getStatusPathForUserID(String aUserID) {
		Path result = null;
		Path startingDir = Paths.get(USERS_DB_PATH_ROOT);
		//48288738-.*[^json]
		//String pattern = aUserID+"-*[!follows].json";
		//6347872-.*[^json] 
		//String pattern = aUserID+"-.*[^json]";
		//String pattern = aUserID+"-.*[^json]";
		String pattern = aUserID+"-.*-.*-status\\.json";
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
		
		if(results != null && results.size() > 0) {
			
			//Path r = results.get(0);
			
			result = results.get(results.size()-1);
			
		}
		return result;
	}
	
	protected static Path getStatusPathForUsername(String aUsername) {
		Path result = null;
		Path startingDir = Paths.get(STATUS_DB_PATH);
		String pattern = "*-"+aUsername+"*[!follows].json";
		Finder finder = new Finder(pattern);
		try {
			Files.walkFileTree(startingDir, finder);
		} catch (IOException e) {
			//e.printStackTrace();
			logger.error(e);
		}
		//logger.debug(finder.results);
		List<Path> results = finder.results;
		if(results != null && results.size() > 0) {
			result = results.get(0);
		}
		return result;		
	}
	
	public boolean localUserBasicIsFreshForUserID(String aUserID) {
		long modified_time = Long.MAX_VALUE;
		boolean result = false;
		modified_time = this.getUserBasicLastModifiedTime(aUserID);
		long now = new Date().getTime();
		long diff = now - modified_time;
		if(diff < USER_BASIC_STALE_TIME) {
			result = true;
		}
		return result;
	
	}

	public boolean localServiceStatusIsFreshForUserID(String aServiceID) {
		boolean result = false;
		List<Path>statusPaths = getStatusPaths(aServiceID);
		if(statusPaths == null || statusPaths.size() < 1) {
			return false;
		}
		Iterator<Path> iter = statusPaths.iterator();
		long now = new Date().getTime();
	
		long oldest = now;
		while(iter.hasNext()) {
			Path p = iter.next();
			File f = p.toFile();
			long l = f.lastModified();
			if(l < oldest) oldest = l;
		}

		long diff = now - oldest;
		if(diff < Constants.getLong("STATUS_STALE_TIME")) {
			result = true;
		}

		return result;
	}

	
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

	public static long getHydratedFollowsModifiedTime(String aCodedUsername) 
	{
		long result = Long.MAX_VALUE;
		File f = new File(String.format(USERS_DB_PATH_CODED, aCodedUsername)+"/follows/"+aCodedUsername+"-hydratedfollows.json");
		result = f.lastModified();
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
	public JSONObject serviceRequestUserBasicForUserID(String aUserID, boolean save)
	{
		JSONObject result = serviceRequestUserBasicForUserID(aUserID);
		//logger.debug(result+" "+aUserID);
		if(save) {
			String username = (String)result.get("screen_name");
			//			String path = String.format(USERS_DB_PATH, aUserID, username);
			File f = new File(String.format(USERS_DB_PATH, aUserID, username));
			String p =  result.get("id_str")+"-"+username+".json";


			writeJSONToFile(result, f, p);
		}
		return result;
	}


	public long getUserBasicLastModifiedTime(String aUserID)
	{
		// see if it exists
		long result = 0l;
		File dir = new File("./"+USERS_DB_PATH_ROOT);
		FileFilter fileFilter = new WildcardFileFilter(aUserID+"-*");
		File[] files = dir.listFiles(fileFilter);
		if(files.length > 0 && files.length == 1) {
			result = files[0].lastModified();
		}
		long now = new Date().getTime();
		return result;
	}

	public JSONObject serviceRequestUserBasicForUserID(String aUserID)
	{
		JSONObject aUser;
		String userURL = String.format(SHOW_USER_BY_ID_URL, aUserID);
		OAuthRequest request = new OAuthRequest(Verb.GET, userURL);
		service.signRequest(accessToken, request);
		Response response = request.send();
		String s = response.getBody();
		Map<String, String> h = response.getHeaders();
		//logger.debug("Twitter: Request on behalf of "+this.getThisUser().get("screen_name"));
		logger.debug("Service Request User Basic for User ID "+aUserID);
		//logger.info("From Headers for Twitter Request rate-limit="+h.get("x-rate-limit-limit")+" reset ms="+h.get("x-rate-limit-reset"));

		//System.out.println(h);
		Object obj = JSONValue.parse(s);
		aUser = (JSONObject) ((JSONObject)obj);
		return aUser;
	}


	public void getFollows() {
		getFollows((String)user.get("id_str"));
	}


	public void unpackFollowsFor(String aUserID)
	{
		JSONArray follows = getFollowsLocal(aUserID);
		Iterator iter = follows.iterator();
		while(iter.hasNext()) {
			JSONObject obj = (JSONObject) iter.next();
			String path = String.format(USERS_DB_PATH, obj.get("id"), obj.get("screen_name"));
			writeJSONToFile(obj, new File(path), obj.get("id")+"-"+obj.get("screen_name")+".json");
		}

	}

	public JSONArray getFollowsLocal(String aUserID)
	{
		JSONArray jsonArray = new JSONArray();
		Pair<List<Path>, Boolean> local = isFollowsLocal(aUserID);
		// consume the stuff in the List of <Path>
		List<Path> paths = local.getFirst();
		if(paths != null && paths.size() > 0 && (Boolean)local.getSecond().booleanValue() == true) {
			JSONParser parser = new JSONParser();

			Path path = (Path)paths.get(0);
			Object obj = null;
			try {
				obj = parser.parse(new FileReader(path.toFile()));
			} catch (FileNotFoundException e) {
				logger.error(e);
				e.printStackTrace();
			} catch (ParseException e) {
				logger.error(e);
				e.printStackTrace();
			} catch (IOException e) {
				logger.error(e);
				e.printStackTrace();
			} 

			jsonArray = (JSONArray) obj;
		}
		return jsonArray;
	}


	/**
	 * Check to see if a userid has its follows data locally.
	 * If it does, return the file path
	 * 
	 * @param aUserID
	 * @return
	 */
	public Pair<List<Path>, Boolean> isFollowsLocal(String aUserID) {
		//boolean result = false;
		Path startingDir = Paths.get(USERS_DB_PATH_ROOT);
		String pattern = aUserID+"-hydratedfollows.json";
		Pair<List<Path>, Boolean> result = new Pair<List<Path>, Boolean>(null, new Boolean(false));
		Finder finder = new Finder(pattern);
		try {
			Files.walkFileTree(startingDir, finder);
			logger.debug(finder.results);
			if(finder.results != null & finder.results.size() > 0) {
				result.setFirst(finder.results);
				result.setSecond(new Boolean(true));
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e);
		}

		return result;
	}


	protected JSONObject getLocalUserBasicForUserID(String aUserID) {
		JSONObject result = null;
		Path startingDir = Paths.get(USERS_DB_PATH_ROOT);
		String pattern = aUserID+".+([^status]|[^follows]).json";
		
		//String pattern = "66854529@N00-*[!follows].json";
		//Pair<List<Path>, Boolean> result = new Pair<List<Path>, Boolean>(null, new Boolean(false));
		Finder finder = new Finder(pattern, "regex");
		try {
			Files.walkFileTree(startingDir, finder);
		} catch (IOException e) {
			//e.printStackTrace();
			logger.error(e);
		}
		//logger.debug(finder.results);
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
			result = this.serviceRequestUserBasicForUserID(aUserID);
		}

		return result;

	}

	@SuppressWarnings("unchecked")
	public void getFollows(String aUserID) {
		String id_str = (String)user.get("id_str");
		if(aUserID == null) {
			aUserID = id_str;
		}

		JSONObject aUser;

		if(aUserID.equalsIgnoreCase(id_str)) {
			aUser = user;	
		} else {
			aUser = serviceRequestUserBasicForUserID(aUserID);
		}

		String followsURL = String.format(FRIENDS_IDS_URL, aUserID, "-1");
		OAuthRequest request = new OAuthRequest(Verb.GET, followsURL);
		service.signRequest(accessToken, request);
		
		Response response = request.send();
		String s = response.getBody();

		Map<String, String> h = response.getHeaders();
		
		//logger.info("From Headers for Twitter Request rate-limit="+h.get("x-rate-limit-limit")+" reset ms="+h.get("x-rate-limit-reset"));

		//System.out.println(s);
		Object obj=JSONValue.parse(s);
		JSONObject map = (JSONObject)obj;

		// error check
		//System.out.println(map);
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
				//System.out.println("Adding "+users.size());
				//System.out.println("All Follows now "+allFollowsIDs.size());
				//logger.debug("Adding "+users.size());
				//logger.debug("All Follows now "+allFollowsIDs.size());
				String next_url = String.format(FRIENDS_IDS_URL, user.get("screen_name"), next_cursor);   //(String)pagination.get("next_url");
				next_cursor_l = next_cursor.longValue();
				if(next_cursor != null && next_cursor_l != 0) {
					request = new OAuthRequest(Verb.GET, (String)next_url);
					service.signRequest(accessToken, request);
					response = request.send();
					s = response.getBody();
					map = (JSONObject)JSONValue.parse(s);
					next_cursor = (Long)map.get("next_cursor");
					h = response.getHeaders();
					logger.debug("paging ("+page_count++ +") through follows for "+user.get("screen_name"));
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
			//System.out.println(map.get("pagination"));
			//System.out.println(allFollows);
			String p = String.format(USERS_FOLLOWS_PATH, aUser.get("id"), aUser.get("screen_name"));
			writeJSONToFile(allFollowsIDs, new File(p), aUser.get("id")+"-"+aUser.get("screen_name")+"-follows.json");

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
			p = String.format(USERS_FOLLOWS_PATH, aUser.get("id"), aUser.get("screen_name"));
			writeJSONToFile(allFollowsHydrated, new File(p), aUser.get("id")+"-"+aUser.get("screen_name")+"-hydratedfollows.json");
			//System.out.println(allFollowsHydrated);

		} else {
			//throw new Exception("Do something about rate limit errors, etc."+map.toString());
			System.err.println("Do something about rate limit errors, etc."+map.toString());
			logger.warn("Do something about rate limit errors, etc."+map.toString());
		}


	}


	public List<JSONObject> getUsersFromFollows(JSONArray arrayOfUsers)
	{
		List<JSONObject> result = new ArrayList<JSONObject>();
		for(int i=0; i<arrayOfUsers.size(); i++) {
			result.add((JSONObject)arrayOfUsers.get(i));
		}

		return result;
	}

	public <E> List<E> doIt()
	{
		List<E> result = null;

		return result;
	}
	

	public void writeJSONToFile(JSONArray arrayToWrite, File aDir, String aName)
	{
		//logger.debug("writeJSONToFile "+aDir+"/"+aName);
		//logger.debug("writeJSONToFile "+aDir+" "+aDir.exists());
		//System.out.println("and "+aDir.getAbsolutePath()+" "+aDir.getPath());
		//logger.debug("and "+aDir.getAbsolutePath()+" "+aDir.getPath());
		createDirectoryHierarchyFromRoot(aDir);
		try {
			File aFile = new File(aDir, aName);

			OutputStreamWriter char_output = new OutputStreamWriter(new FileOutputStream(aFile), Charset.forName("UTF-8").newEncoder());
			char_output.write(arrayToWrite.toJSONString());
			char_output.flush();
			char_output.close();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e);
		}


	}

	public void writeJSONToDB(JSONObject objToWrite, String aName) {
		
	}
	
	public void writeJSONToFile(JSONObject objToWrite, File aDir, String aName)
	{

		createDirectoryHierarchyFromRoot(aDir);
		try {
			File aFile = new File(aDir, aName);
			//System.out.println("wrote user to "+aFile);
			OutputStreamWriter char_output = new OutputStreamWriter(new FileOutputStream(aFile), Charset.forName("UTF-8").newEncoder());
			char_output.write(objToWrite.toJSONString());
			char_output.flush();
			char_output.close();
		} catch(IOException ioe) {
			ioe.printStackTrace();
			logger.error(ioe);
		}
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
			logger.error(e);
		}
	}




	public static Token deserializeToken(JSONObject aUser) {
		Token result = null;
		String path = null;
		try{
			//use buffering
			
			path = String.format(USERS_SER_TOKEN, aUser.get("id"), aUser.get("screen_name"), aUser.get("id"), aUser.get("screen_name"));

			InputStream file = new FileInputStream( path );
			InputStream buffer = new BufferedInputStream( file );
			ObjectInput input = new ObjectInputStream ( buffer );
			try{
				//deserialize the List
				result = (Token)input.readObject();
				//display its data
				//logger.debug("Deserialized Token is: "+result);
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
		catch(NullPointerException ex) {
			logger.debug(aUser);
			logger.debug(path);
			logger.error(ex);
			logger.error(Thread.currentThread().getStackTrace());
		}
		return result;
	}

	public static void mkDirs(File root, List<String> dirs, int depth) {
		FileUtils.mkDirs(root, dirs);
	}
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
