package com.nearfuturelaboratory.humans.service;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
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
import com.nearfuturelaboratory.humans.entities.ServiceToken;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramFollows;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramStatus;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramUser;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramUserBriefly;
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


	//TODO Change this super ridiculous constructor. Should all be factory methods like above.
	public InstagramService(Token aAccessToken) {
		this();
		accessToken = aAccessToken;
		service = new ServiceBuilder()
		.provider(InstagramApi.class)
		.apiKey(Constants.getString("INSTAGRAM_API_KEY"))
		.apiSecret(Constants.getString("INSTAGRAM_API_SECRET"))
		.callback(Constants.getString("INSTAGRAM_CALLBACK_URL"))
		.scope("basic,likes")
		.build();
	}

	

	public static InstagramService createInstagramServiceOnBehalfOfUsername(String aUsername)
	{
		InstagramService result;
		Token token;
		InstagramUser user = InstagramService.getLocalUserBasicForUsername(aUsername);
		if(user == null) {
			logger.warn("null token. trying to find one by username");
			token = InstagramService.deserializeToken(aUsername);
			result = new InstagramService(token);
			user = result.serviceRequestUserBasic();

		} else {
			token = InstagramService.deserializeToken(user);
			result = new InstagramService(token);
		}
		if(token == null) {
			logger.error("null token for "+aUsername);
			return null;
		}

		result.user = user;
		return result;
	}

	protected static InstagramUser staticGetLocalUserBasicForUserID(String aUserID) {
		InstagramUserDAO dao = new InstagramUserDAO();
		dao.ensureIndexes();	
		InstagramUser result =  dao.findByExactUserID(aUserID);
		return result;
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
		com.nearfuturelaboratory.humans.instagram.entities.InstagramUser iuser = gson.fromJson(aUser.toString(), 
				com.nearfuturelaboratory.humans.instagram.entities.InstagramUser.class);
		return iuser;
	}



	protected InstagramUser getLocalUserBasicForUserID(String aUserID) {
		InstagramUser result = userDAO.findByExactUserID(aUserID);
		return result;
	}

	protected com.nearfuturelaboratory.humans.instagram.entities.InstagramStatus getMostRecentStatusForUserID(String aUserID) {
		return statusDAO.findMostRecentStatusByExactUserID(aUserID);
	}


	protected Key<InstagramUser> saveUserBasicJson(JSONObject aUserJson) {
		com.nearfuturelaboratory.humans.instagram.entities.InstagramUser iuser = gson.fromJson(aUserJson.toString(), 
				com.nearfuturelaboratory.humans.instagram.entities.InstagramUser.class);
		return userDAO.save(iuser);

	}
	
	protected String getMostRecentStatusID(String aUserID) {
		String result = null;
		com.nearfuturelaboratory.humans.instagram.entities.InstagramStatus most_recent = getMostRecentStatusForUserID(aUserID);
		if(most_recent != null) {
			result = most_recent.getStatusId();
		}
		return result;
	}
	
	/**
	 * Weird method to see if I can easily change the type of the created_time field to Long
	 */
	public void freshenStatus() {
		List<InstagramStatus> status = getStatusForUserID(this.getThisUser().getId());
		for(InstagramStatus item : status) {
			statusDAO.save(item);
		}
	}
	
	public List<InstagramStatus> getStatus() {
		return getStatusForUserID(this.getThisUser().getId());
	}
	
	public List<InstagramStatus> getStatusForUserID(String aUserID) {
		return statusDAO.findByExactUserID(aUserID);
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
		return this.saveStatusJson(full_data);

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

		saveStatusJson(full_data);

	}

	public List<InstagramStatus> serviceRequestStatusForUserID() {
		return serviceRequestStatusForUserID(this.getThisUser().getId());
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

		return saveStatusJson(full_data);
	}

	@SuppressWarnings("unused")
	List<InstagramStatus> saveStatusJson(JSONArray data) {
		gson = new Gson();
		List<InstagramStatus>result = new ArrayList<InstagramStatus>();
		// the way with morphia + DAO model..
		@SuppressWarnings("unchecked")
		Iterator<JSONObject> iter = data.iterator();
		while(iter.hasNext()) {
			String i = iter.next().toString();
			//logger.debug(i);
			com.nearfuturelaboratory.humans.instagram.entities.InstagramStatus istatus = gson.fromJson(i, com.nearfuturelaboratory.humans.instagram.entities.InstagramStatus.class);
			result.add(istatus);
			statusDAO.save(istatus);
		}
		return result;
	}

	public List<InstagramFollows> getFollows() {
		return followsDAO.findFollowsByExactUserID(this.getThisUser().getId());
	}
	
	
	/**
	 *  
	 *  
	 */
	public void serviceRequestFollows() {
		serviceRequestFollows(this.getThisUser().getId());
	}

	
	/**
	 * 
	 * @param aUserID
	 */
	@SuppressWarnings("unchecked")
	public List<InstagramFollows> serviceRequestFollows(String aUserID) {
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
		logger.debug("Save follows for "+aUser.getUsername());
		saveFollowsJson(allFollows);
		return result;
	}

	protected void saveFollowsJson(JSONArray data) {
		Iterator<JSONObject> iter = data.iterator();
		for(int i=0; i<data.size(); i++) {
			String u = data.get(i).toString();
			InstagramUserBriefly iub = gson.fromJson(u, InstagramUserBriefly.class);
			iub.setFollower_id(getThisUser().getId());
			InstagramFollows iuf = new InstagramFollows(iub);
			iuf.setFollower_id(getThisUser().getId());
			
			InstagramFollows f = followsDAO.findFollowsByUserIDFollowsID(iub.getId(), getThisUser().getId());
			if(f != null) {
				f.setUser_briefly(iub);
				followsDAO.save(f);
			} else {
				followsDAO.save(iuf);
			}
		}
		
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
	
	private static Token deserializeToken(String aUsername) {
		ServiceTokenDAO dao = new ServiceTokenDAO("instagram");
		ServiceToken st = dao.findByExactUsername(aUsername);
		return st.getToken();
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
		com.nearfuturelaboratory.humans.instagram.entities.InstagramUser user = this.getLocalUserBasicForUserID(aUserID);
		
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
		com.nearfuturelaboratory.humans.instagram.entities.InstagramStatus most_recent = this.getMostRecentStatusForUserID(aUserID);
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










}
