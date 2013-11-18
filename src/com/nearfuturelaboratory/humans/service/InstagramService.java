package com.nearfuturelaboratory.humans.service;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mongodb.morphia.Key;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import com.mongodb.DB;
import com.nearfuturelaboratory.humans.dao.InstagramFollowsDAO;
import com.nearfuturelaboratory.humans.dao.InstagramStatusDAO;
import com.nearfuturelaboratory.humans.dao.InstagramUserDAO;
import com.nearfuturelaboratory.humans.dao.ServiceTokenDAO;
import com.nearfuturelaboratory.humans.entities.InstagramFollows;
import com.nearfuturelaboratory.humans.entities.InstagramStatus;
import com.nearfuturelaboratory.humans.entities.InstagramUser;
import com.nearfuturelaboratory.humans.entities.ServiceToken;
//import com.nearfuturelaboratory.humans.service.status.InstagramStatus;
import com.nearfuturelaboratory.humans.serviceapi.InstagramApi;
import com.nearfuturelaboratory.humans.util.MongoUtil;
import com.nearfuturelaboratory.util.Constants;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class InstagramService {

	private static final String FOLLOWS_URL = "https://api.instagram.com/v1/users/%s/follows";
	private static final String STATUS_URL = "https://api.instagram.com/v1/users/%s/media/recent";
	private static final String USER_URL = "https://api.instagram.com/v1/users/%s";

	private OAuthService service;
//	private static String apiKey = Constants.getString("INSTAGRAM_API_KEY");
//	private static String apiSecret = Constants.getString("INSTAGRAM_API_SECRET");
//	private static String callbackURL = Constants.getString("INSTAGRAM_CALLBACK_URL");

	private Token accessToken;

	//protected JSONObject user;
	protected InstagramUser user;

	protected DB db;
	protected InstagramStatusDAO statusDAO;
	protected InstagramUserDAO userDAO;
	protected InstagramFollowsDAO followsDAO;
	protected ServiceTokenDAO tokenDAO;
	
	protected Gson gson;

	final static Logger logger = Logger.getLogger(com.nearfuturelaboratory.humans.service.InstagramService.class);

	public InstagramService() {
		//TODO Gross..
		db = MongoUtil.getMongo().getDB("instagram");

		statusDAO = new InstagramStatusDAO();
		statusDAO.ensureIndexes();

		userDAO = new InstagramUserDAO();
		userDAO.ensureIndexes();

		followsDAO = new InstagramFollowsDAO();
		followsDAO.ensureIndexes();

		tokenDAO = new ServiceTokenDAO("instagram");
		tokenDAO.ensureIndexes();
		
		gson = new Gson();

	}

	public static InstagramService createInstagramServiceOnBehalfOfUsername(String aUsername)
	{
		InstagramUser local_user = InstagramService.getLocalUserBasicForUsername(aUsername);

		//InstagramUser local_user = InstagramService.getLocalUserBasicForUsername(aUsername);
		Token token = InstagramService.deserializeToken(local_user);
		InstagramService result = new InstagramService(token);
		result.user = local_user;
		return result;
	}

	protected static InstagramUser staticGetLocalUserBasicForUserID(String aUserID) {
		InstagramUserDAO dao = new InstagramUserDAO();
		dao.ensureIndexes();	
		InstagramUser result =  dao.findByExactUserID(aUserID);
		return result;
	}

	//TODO Change this super ridiculous constructor. Should all be factory methods like above.
	public InstagramService(Token aAccessToken) {
		this();
		System.out.println(Constants.getString("INSTAGRAM_API_KEY"));
		accessToken = aAccessToken;
		service = new ServiceBuilder()
		.provider(InstagramApi.class)
		.apiKey(Constants.getString("INSTAGRAM_API_KEY"))
		.apiSecret(Constants.getString("INSTAGRAM_API_SECRET"))
		.callback(Constants.getString("INSTAGRAM_CALLBACK_URL"))
		.scope("basic,likes")
		.build();
	}

	/**
	 * This will go to the service and get "self" for whoever's accessToken we have
	 */
	public InstagramUser serviceRequestUserBasic() {
		return this.serviceRequestUserBasicForUserID("self");
	}


	public InstagramUser getThisUser() {
		return user;
	}

	/**
	 * Request from Instagram the basic user info for a particular user id 
	 * and save it.
	 * 
	 * @param aUserID
	 * @param save
	 * @return
	 */
	public InstagramUser serviceRequestUserBasicForUserID(String aUserID)
	{
		//JSONObject result = __serviceRequestUserBasicForUserID(aUserID);
		JSONObject aUser;
		String userURL = String.format(USER_URL, aUserID);
		OAuthRequest request = new OAuthRequest(Verb.GET, userURL);
		service.signRequest(accessToken, request);
		Response response = request.send();
		// TODO Error check..
		String s = response.getBody();
		Object obj = JSONValue.parse(s);
		aUser = (JSONObject) ((JSONObject)obj).get("data");

		this.saveUserBasicJson(aUser);
		com.nearfuturelaboratory.humans.entities.InstagramUser iuser = gson.fromJson(aUser.toString(), 
				com.nearfuturelaboratory.humans.entities.InstagramUser.class);
		return iuser;
	}



	protected InstagramUser getLocalUserBasicForUserID(String aUserID) {
		InstagramUser result = userDAO.findByExactUserID(aUserID);
		return result;
	}

	protected com.nearfuturelaboratory.humans.entities.InstagramStatus getMostRecentStatusForUserID(String aUserID) {
		return statusDAO.findMostRecentStatusByExactUserID(aUserID);
	}


	@SuppressWarnings("unchecked")
	protected Key<InstagramUser> saveUserBasicJson(JSONObject aUserJson) {
		com.nearfuturelaboratory.humans.entities.InstagramUser iuser = gson.fromJson(aUserJson.toString(), 
				com.nearfuturelaboratory.humans.entities.InstagramUser.class);
		return userDAO.save(iuser);

	}
	
	protected String getMostRecentStatusID(String aUserID) {
		String result = null;
		com.nearfuturelaboratory.humans.entities.InstagramStatus most_recent = getMostRecentStatusForUserID(aUserID);
		if(most_recent != null) {
			result = most_recent.getStatusId();
		}
		return result;
	}


	/**
	 * Given a minimum (most recent) status ID, get all the ones after it.
	 * The way the Instagram endpoint works, this basically also includes the status with the aMinID, if it exists
	 * 
	 * @param aUserID
	 * @param aMinID
	 * @return 
	 */
	public List<InstagramStatus> serviceRequestStatusForUserIDAfterMinID(String aUserID, String aMinID) {
		if(aUserID == null || aUserID.equalsIgnoreCase("self")) {
			aUserID = (String)user.getId();
		}
		String statusURL = String.format(STATUS_URL, aUserID);
		OAuthRequest request = new OAuthRequest(Verb.GET, statusURL);
		request.addQuerystringParameter("min_id", aMinID);
		service.signRequest(accessToken, request);
		Response response = request.send();
		String s = response.getBody();
		Object jsonResponse = JSONValue.parse(s);
		@SuppressWarnings("unused")
		JSONObject status = (JSONObject)jsonResponse;
		JSONArray full_data = (JSONArray)status.get("data");
		// Save to Mongo
		return this.saveStatusJson(full_data, aUserID);

	}

	/**
	 * For aUserID get one chunk of status from some number of months ago
	 * 
	 * @param aUserID
	 * @param aMonthsAgo
	 */
	//TODO Alter this method to be more efficient by using both max_timestamp as well as max_id or something
	public void serviceRequestStatusForUserIDFromMonthsAgo(String aUserID, int aMonthsAgo) {
		Calendar ago =Calendar.getInstance();
		ago.add(Calendar.MONTH, -1*aMonthsAgo);
		//long year_ago = ago.getTimeInMillis();
		if(aUserID == null || aUserID.equalsIgnoreCase("self")) {
			aUserID = (String)user.getId();
		}
		String statusURL = String.format(STATUS_URL, aUserID);
		OAuthRequest request = new OAuthRequest(Verb.GET, statusURL+"?max_timestamp="+String.valueOf(ago.getTimeInMillis()/1000l));
		request.addQuerystringParameter("count", "40");
		//request.addQuerystringParameter("MIN_TIMESTAMP", String.valueOf(ago.getTimeInMillis()));
		service.signRequest(accessToken, request);
		Response response = request.send();
		String s = response.getBody();
		//TODO Error checking
		Object jsonResponse = JSONValue.parse(s);

		JSONObject status = (JSONObject)jsonResponse;
		JSONArray full_data = (JSONArray)status.get("data");
		JSONObject oldest = (JSONObject)full_data.get(full_data.size()-1);
		long oldest_time = Long.parseLong(oldest.get("created_time").toString());
		Calendar oldest_cal = Calendar.getInstance();
		oldest_cal.setTimeInMillis(oldest_time * 1000l);

		saveStatusJson(full_data, aUserID);

	}

	public List<InstagramStatus> serviceRequestStatusForUserID(String aUserID) {
		if(aUserID.equalsIgnoreCase("self")) {
			aUserID = this.getThisUser().getId();
		}
		//long startTime = System.nanoTime();
		String aMinID = this.getMostRecentStatusID(aUserID);
		//long endTime = System.nanoTime();
		return this.serviceRequestStatusForUserIDAfterMinID(aUserID, aMinID);
		//serviceRequestStatusForUserIDToMonthsAgo(aUserID, 0);
	}

	/**
	 * For aUserID get status from most recent until some number of months ago
	 * 
	 * @param aUserID
	 * @param aBackMonthsAgo
	 */
	public List<InstagramStatus> serviceRequestStatusForUserIDToMonthsAgo(String aUserID, int aBackMonthsAgo) {
		new ArrayList<InstagramStatus>();
		Calendar ago =Calendar.getInstance();
		ago.add(Calendar.MONTH, -1*aBackMonthsAgo);
		//long year_ago = ago.getTimeInMillis();
		if(aUserID == null || aUserID.equalsIgnoreCase("self")) {
			aUserID = (String)user.getId();
		}
		String statusURL = String.format(STATUS_URL, aUserID);
		OAuthRequest request = new OAuthRequest(Verb.GET, statusURL);
		request.addQuerystringParameter("count", "50");
		service.signRequest(accessToken, request);
		Response response = request.send();

		//TODO Something..
		if(response.getCode() != 200) ;

		String s = response.getBody();
		Object jsonResponse = JSONValue.parse(s);

		JSONObject status = (JSONObject)jsonResponse;
		JSONArray full_data = (JSONArray)status.get("data");
		JSONObject oldest = (JSONObject)full_data.get(full_data.size()-1);
		long oldest_time = Long.parseLong(oldest.get("created_time").toString());
		Calendar oldest_cal = Calendar.getInstance();
		oldest_cal.setTimeInMillis(oldest_time);
		String next_url = JsonPath.read(status, "pagination.next_url");

		do {
			if(next_url == null) {
				break;
			}
			request = new OAuthRequest(Verb.GET, next_url);
			service.signRequest(accessToken, request);
			response = request.send();
			s = response.getBody();
			jsonResponse = JSONValue.parse(s);
			status = (JSONObject)jsonResponse;
			JSONArray latest_data = (JSONArray)status.get("data");
			full_data.addAll(latest_data);
			oldest = (JSONObject)latest_data.get(latest_data.size()-1);
			oldest_time = Long.parseLong(oldest.get("created_time").toString());
			oldest_cal.setTimeInMillis(oldest_time*1000);
			next_url = JsonPath.read(status, "pagination.next_url");
		} while(oldest_cal.compareTo(ago)>0 && next_url != null);

		return saveStatusJson(full_data, aUserID);
	}

	@SuppressWarnings("unused")
	List<InstagramStatus> saveStatusJson(JSONArray data, String aUserID) {
		Gson gson = new Gson();
		List<InstagramStatus>result = new ArrayList<InstagramStatus>();
		//		DBCollection coll = db.getCollection("instagram.status."+aUserID);
		//		coll.ensureIndex(new BasicDBObject("id", 1), "uniq_id", true);
		//		Iterator iter = data.iterator();
		// one way..you save it without the Morphia layer..
		//		while(false && iter.hasNext()) {
		//DataObject obj = gson.fromJson(br, DataObject.class);
		//com.nearfuturelaboratory.humans.entities.InstagramStatus istatus = gson.fromJson(iter.next().toString(), com.nearfuturelaboratory.humans.entities.InstagramStatus.class)
		//			BasicDBObject bobj = gson.fromJson(iter.next().toString(), BasicDBObject.class);
		//			coll.insert(bobj);
		//		}
		//		iter = data.iterator();
		// the way with morphia + DAO model..
		@SuppressWarnings("unchecked")
		Iterator<JSONObject> iter = data.iterator();
		while(iter.hasNext()) {
			String i = iter.next().toString();
			//logger.debug(i);
			com.nearfuturelaboratory.humans.entities.InstagramStatus istatus = gson.fromJson(i, com.nearfuturelaboratory.humans.entities.InstagramStatus.class);
			result.add(istatus);
			statusDAO.save(istatus);
		}
		return result;
	}

	/**
	 *  Bad name for a network call. You don't get anything no returned value. Instead,
	 *  this loads the follows from Instagram across the wire and saves them.
	 *  
	 */
	//TODO rename this thing.
	public void getFollows() {
		getFollows("self");
	}

	//TODO rename this thing.
	/**
	 * 
	 * @param aUserID
	 */
	@SuppressWarnings("unchecked")
	public List<InstagramFollows> getFollows(String aUserID) {
		InstagramUser aUser;
		List<InstagramFollows> result = new ArrayList<InstagramFollows>();
		
		if(aUserID == null || aUserID.equalsIgnoreCase("self")) {
			aUserID = user.getId();
			// if the user basic isn't fresh for self, then request it and reset ourselves
			if(this.localUserBasicIsFreshForSelf() == false) {
				user = serviceRequestUserBasic();
			}
			//aUser = user;
		} else {
			if(this.localUserBasicIsFreshForUserID(aUserID) == false) {
				this.serviceRequestUserBasicForUserID(aUserID);
			}
		}

		aUser = this.getLocalUserBasicForUserID(aUserID);

		String followsURL = String.format(FOLLOWS_URL, aUserID);

		OAuthRequest request = new OAuthRequest(Verb.GET, followsURL);
		service.signRequest(accessToken, request);
		Response response = request.send();
		String s = response.getBody();
		Object obj=JSONValue.parse(s);
		JSONObject map = (JSONObject)obj;

		JSONObject pagination = (JSONObject)map.get("pagination");
		JSONArray allFollows = new JSONArray();

		do {
			JSONArray data = (JSONArray)map.get("data");
			allFollows.addAll(data);
			//logger.debug("Adding "+data.size());
			//logger.debug("All Follows now "+allFollows.size());
			String next_url = (String)pagination.get("next_url");
			if(next_url != null) {
				request = new OAuthRequest(Verb.GET, (String)next_url);
				response = request.send();
				s = response.getBody();
				//TODO Error checking
				if(s == null) {
					logger.error("Null body in the response "+response);
					break;
				}
				map = (JSONObject)JSONValue.parse(s);
				if(map == null) {
					logger.error("No pagination in the get follows request "+response+" "+s);
					break;
				}
				pagination = (JSONObject)map.get("pagination");
			} else {
				break;
			}
		} while(pagination != null);
		//JSONObject meta = (JSONObject)map.get("meta");
		for(int i=0; i<allFollows.size(); i++) {
			String u = allFollows.get(i).toString();

			com.nearfuturelaboratory.humans.entities.InstagramUserBriefly iub = gson.fromJson(u, com.nearfuturelaboratory.humans.entities.InstagramUserBriefly.class);
			iub.setFollower_id(aUser.getId());
			InstagramFollows iuf = new InstagramFollows(iub);
			iub.setFollower_id(aUser.getId());
			followsDAO.save(iuf);
			result.add(iuf);
		}
		logger.debug("Wrote follows for "+aUser.getUsername());
		return result;
	}


	public static void serializeToken(Token aToken, InstagramUser aUser) {
		ServiceTokenDAO dao = new ServiceTokenDAO("instagram");
		ServiceToken tokenToSave = new ServiceToken();
		tokenToSave.setToken(aToken);
		tokenToSave.setUser_id(aUser.getId());
		tokenToSave.setUsername(aUser.getUsername());
		tokenToSave.setServicename("instagram");
		dao.save(tokenToSave);
	}
	
	
	public static Token deserializeToken(InstagramUser aUser) {
		//Token result = null;
		ServiceTokenDAO dao = new ServiceTokenDAO("instagram");
		ServiceToken serviceToken = dao.findByExactUserId( aUser.getId() );
		return serviceToken.getToken();
	}

	public boolean localUserBasicIsFreshForSelf() {
		return this.localUserBasicIsFreshForUserID(this.user.getId());
	}

	public boolean localUserBasicIsFreshForUserID(String aUserID) {
		boolean result = false;
		com.nearfuturelaboratory.humans.entities.InstagramUser user = this.getLocalUserBasicForUserID(aUserID);
		
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


	public boolean localServiceStatusIsFreshForUserID(String aUserID) {
		boolean result = false;
		com.nearfuturelaboratory.humans.entities.InstagramStatus most_recent = this.getMostRecentStatusForUserID(aUserID);
		Date d = most_recent.getLastUpdated();

		long then = d.getTime();
		long now = new Date().getTime();

		long diff = now - then;
		if(diff < Constants.getLong("STATUS_STALE_TIME")) {
			result = true;
		}
		
		return result;
	}

	
	protected static InstagramUser getLocalUserBasicForUsername(String aUsername) {
		InstagramUserDAO dao = new InstagramUserDAO();
		InstagramUser user = dao.findByExactUsername(aUsername);
		return user;
	}


//	public List<InstagramStatus> getStatus() {
//		return getStatus(this.getThisUser().getId().toString());
//	}
//
//
//	public List<InstagramStatus> getStatus(String aUserID) {
//		List<InstagramStatus> result = new ArrayList<InstagramStatus>();
//		//List<Path>statusPaths = getStatusPaths(aUserID);
//
//		// load each file, iterate the status
//		for(int i=0; statusPaths != null && i < statusPaths.size(); i++) {
//			Path path = statusPaths.get(i);
//			File f = path.toFile();
//			JsonParser parser = new JsonParser();
//			Gson gson = new GsonBuilder().create();
//			try {
//				InputStreamReader char_input = 
//						new InputStreamReader(new FileInputStream(f),Charset.forName("UTF-8").newDecoder());
//				Object obj = parser.parse(char_input);
//				JsonArray statuses = (JsonArray)obj;
//				for(int j=0; j<statuses.size(); j++) {
//					if(statuses.get(i).isJsonObject()) {
//						InstagramStatus is = gson.fromJson(statuses.get(j), InstagramStatus.class);
//						is.setStatusJSON(statuses.get(j).getAsJsonObject());
//						result.add(is);
//						//logger.debug(is.toString());
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

//	protected List<Path> getStatusPaths() {
//		return getStatusPaths(this.getThisUser().getId().toString());
//	}

//	protected List<Path> getStatusPaths(String aUserID) {
//		Path startingDir = Paths.get(USERS_DB_PATH_ROOT);
//		String pattern = aUserID+"-\\w*-(\\d*-archival)?status.json";
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

}
