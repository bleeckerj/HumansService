package com.nearfuturelaboratory.humans.service;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
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
import com.nearfuturelaboratory.humans.dao.InstagramFriendsDAO;
import com.nearfuturelaboratory.humans.dao.InstagramStatusDAO;
import com.nearfuturelaboratory.humans.dao.InstagramUserDAO;
import com.nearfuturelaboratory.humans.dao.ServiceTokenDAO;
import com.nearfuturelaboratory.humans.entities.ServiceToken;
import com.nearfuturelaboratory.humans.exception.BadAccessTokenException;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramFriend;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramStatus;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramUser;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramUserBriefly;
//import com.nearfuturelaboratory.humans.service.status.InstagramStatus;
import com.nearfuturelaboratory.humans.serviceapi.InstagramApi;
import com.nearfuturelaboratory.humans.util.MongoUtil;
import com.nearfuturelaboratory.util.Constants;

public class InstagramService /*implements AbstractService*/ {

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
	protected InstagramFriendsDAO followsDAO;
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

		followsDAO = new InstagramFriendsDAO();
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

		//		user = this.serviceRequestUserBasic();

	}

	protected void setAccessToken(Token aAccessToken) {
		accessToken = aAccessToken;
		service = new ServiceBuilder()
		.provider(InstagramApi.class)
		.apiKey(Constants.getString("INSTAGRAM_API_KEY"))
		.apiSecret(Constants.getString("INSTAGRAM_API_SECRET"))
		.callback(Constants.getString("INSTAGRAM_CALLBACK_URL"))
		.scope("basic,likes")
		.build();

	}

	public void initServiceOnBehalfOfUsername(String aUsername) throws BadAccessTokenException 
	{
		accessToken = InstagramService.deserializeToken(aUsername);
		setAccessToken(accessToken);
		if(accessToken == null) {
			throw new BadAccessTokenException("The access token for Instagram User "+aUsername+" is null. It probably does not exist.");
		} else {
			user = InstagramService.getLocalUserBasicForUsername(aUsername);
			if(user == null) {
				user = serviceRequestUserBasic();
			}
		}
	}

	public static InstagramService createServiceOnBehalfOfUsername(String aUsername) throws BadAccessTokenException
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
			throw new BadAccessTokenException("The access token for Instagram User "+aUsername+" is null. It probably does not exist.");
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

		saveUserBasicJson(aUser);
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

	public List<InstagramStatus> serviceRequestStatus() {
		return serviceRequestStatusForUserID(this.getThisUser().getId());
	}

	public List<InstagramStatus> serviceRequestStatusForUserID(String aUserID) {
		if(aUserID.equalsIgnoreCase("self")) {
			aUserID = this.getThisUser().getId();
		}
		//long startTime = System.nanoTime();
		String aMinID = this.getMostRecentStatusID(aUserID);
		//long endTime = System.nanoTime();
		if(aMinID == null) {
			return this.serviceRequestStatusForUserIDToMonthsAgo(aUserID, 1);
		} else {

			return this.serviceRequestStatusForUserIDAfterMinID(aUserID, aMinID);
		}
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


	public boolean localFriendsIsFresh() {
		boolean result = false;
		InstagramFriend friend = followsDAO.findOldestFriendsForUserID(this.getThisUser().getId());

		if (friend == null)
			return false;

		Date d = friend.getLastUpdated();//  .getFriend().getLastUpdated();

		long then = d.getTime();
		long now = new Date().getTime();
		long diff = now - then;
		if (diff < Constants.getLong("FOLLOWS_STALE_TIME")) {
			result = true;
		}

		return result;

	}

	public List<InstagramFriend> getFriends() {
		return followsDAO.findFollowsByExactUserID(this.getThisUser().getId());
	}

	protected List<InstagramFriend> getFriendsFor(String aUserID) {
		return followsDAO.findFollowsByExactUserID(aUserID);
	}

	/**
	 *  
	 *  
	 */
	public void serviceRequestFriends() {
		serviceRequestFriends(this.getThisUser().getId());
	}


	/**
	 * 
	 * @param aUserID
	 */
	protected List<InstagramFriend> serviceRequestFriends(String aUserID) {
		InstagramUser aUser;
		List<InstagramFriend> result = new ArrayList<InstagramFriend>();

		if(aUserID == null || aUserID.equalsIgnoreCase("self")) {
			aUserID = user.getId();
			// if the user basic isn't fresh for self, then request it and reset ourselves
			if(this.localUserBasicIsFresh() == false) {
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
		//JSONArray allFollows = new JSONArray();
		List<JSONObject> allFollows = new ArrayList<JSONObject>();

		do {
			//			JSONArray data = (JSONArray)map.get("data");
			List<JSONObject> f = JsonPath.read(map, "data");
			if(f != null) {
				allFollows.addAll(f);
			}
			//allFollows.addAll(data);
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
		saveFollowsJson(allFollows, aUserID);
		return result;
	}

	//TODO we should delete all the follows first..then add them back?
	//TODO or find the ones we would be deleting in the overlap exclusion?
	//TODO TwitterService passes the id of the user to this method
	protected void saveFollowsJson(List<JSONObject> data, String follower_id) {

		List<InstagramFriend> new_friends = new ArrayList<InstagramFriend>();
		for(JSONObject j : data) {
			InstagramUserBriefly iub = gson.fromJson(j.toString(), InstagramUserBriefly.class);
			InstagramUser friend = this.getLocalUserBasicForUserID(iub.getId());
			if(friend == null || this.localUserBasicIsFreshForUserID(iub.getId()) == false) {
				friend = this.serviceRequestUserBasicForUserID(iub.getId());
			}
			InstagramFriend iuf = new InstagramFriend(friend);
			iuf.setFollower_id(getThisUser().getId());
			iuf.setFriend_id(iub.getId());
			iuf.setFriend_username(iub.getUsername());
			iuf.setFollower(this.getThisUser());

			InstagramFriend f = followsDAO.findFollowsByFriendIDForUserID(friend.getId(), follower_id);
			if(f != null) {
				f.setFriend(friend);
				f.setFollower(this.getThisUser());
				new_friends.add(f);
			} else {
				new_friends.add(iuf);
			}
		}

		List<InstagramFriend> existing_friends = this.getFriendsFor(follower_id);

		Collection<InstagramFriend> new_friends_to_save = CollectionUtils.subtract(new_friends, existing_friends);
		Collection<InstagramFriend> no_longer_friends = CollectionUtils.subtract(existing_friends, new_friends);

		for(InstagramFriend not_a_friend : no_longer_friends) {
			followsDAO.delete(not_a_friend);
		}

		for(InstagramFriend is_a_friend : new_friends_to_save) {
			followsDAO.save(is_a_friend);
		}

		//		Iterator<JSONObject> iter = data.iterator();
		//		for(int i=0; i<data.size(); i++) {
		//			String u = data.get(i).toString();
		//			InstagramUserBriefly iub = gson.fromJson(u, InstagramUserBriefly.class);
		//			//iub.setFollower_id(getThisUser().getId());
		//			
		//			//TODO save the big user, or this brief one that comes back when we request follows?
		//			//InstagramUser friend = this.serviceRequestUserBasicForUserID(iub.getId());
		//			InstagramUser friend = this.getLocalUserBasicForUserID(iub.getId());
		//			if(friend == null || this.localUserBasicIsFreshForUserID(iub.getId()) == false) {
		//				friend = this.serviceRequestUserBasicForUserID(iub.getId());
		//			}
		//			
		//			InstagramFriend iuf = new InstagramFriend(friend);
		//			iuf.setFollower_id(getThisUser().getId());
		//			iuf.setFriend_id(iub.getId());
		//			iuf.setFriend_username(iub.getUsername());
		//			
		//			InstagramFriend f = followsDAO.findFollowsByUserIDFollowsID(friend.getId(), getThisUser().getId());
		//			if(f != null) {
		//				f.setFriend(friend);
		//				followsDAO.save(f);
		//			} else {
		//				followsDAO.save(iuf);
		//			}
		//		}

	}

	public static void serializeToken(Token aToken, InstagramUser aUser) {
		ServiceTokenDAO dao = new ServiceTokenDAO("instagram");
		ServiceToken tokenToSave = dao.findByExactUserID(aUser.getId()); //new ServiceToken();
		if(tokenToSave == null) {
			tokenToSave = new ServiceToken();
		}
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

	public boolean localUserBasicIsFresh() {
		return this.localUserBasicIsFreshForUserID(this.user.getId());
	}

	protected boolean localUserBasicIsFreshForUserID(String aUserID) {
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


	public boolean localServiceStatusIsFresh() {
		return localServiceStatusIsFreshForUserID(this.getThisUser().getId());
	}

	public boolean localServiceStatusIsFreshForUserID(String aUserID) {
		boolean result = false;

		try {

			com.nearfuturelaboratory.humans.instagram.entities.InstagramStatus most_recent = this.getMostRecentStatusForUserID(aUserID);
			Date d = most_recent.getLastUpdated();

			long then = d.getTime();
			long now = new Date().getTime();

			long diff = now - then;
			if(diff < Constants.getLong("STATUS_STALE_TIME")) {
				result = true;
			}
		} catch(NullPointerException npe) {
			logger.warn(npe);
			logger.warn("Probably no status at all, so no Farm Fresh Local Status Today");
			
		} finally {
			if(result == false) return false;
		}
		return result;
	}


	protected static InstagramUser getLocalUserBasicForUsername(String aUsername) {
		InstagramUserDAO dao = new InstagramUserDAO();
		InstagramUser user = dao.findByExactUsername(aUsername);
		return user;
	}










}
