package com.nearfuturelaboratory.humans.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mongodb.DB;
import com.nearfuturelaboratory.humans.dao.ServiceTokenDAO;
import com.nearfuturelaboratory.humans.dao.TwitterFollowsDAO;
import com.nearfuturelaboratory.humans.dao.TwitterStatusDAO;
import com.nearfuturelaboratory.humans.dao.TwitterUserDAO;
import com.nearfuturelaboratory.humans.entities.ServiceToken;
import com.nearfuturelaboratory.humans.service.status.ServiceStatus;
import com.nearfuturelaboratory.humans.twitter.entities.TwitterFriend;
import com.nearfuturelaboratory.humans.twitter.entities.TwitterStatus;
import com.nearfuturelaboratory.humans.twitter.entities.TwitterUser;
import com.nearfuturelaboratory.humans.util.MongoUtil;
import com.nearfuturelaboratory.util.Constants;
import static com.google.common.collect.Lists.partition;

public class TwitterService extends ServiceStatus {
	final static Logger logger = Logger
			.getLogger(com.nearfuturelaboratory.humans.service.TwitterService.class);

	private String apiKey = Constants.getString("TWITTER_API_KEY");// "09ARKva0K7HMz1DW1GUg";
	private String apiSecret = Constants.getString("TWITTER_API_SECRET");// "rwy7rZ2Uu3lkliYMfOaJD4UeUHFebDqXXrBgjnT8Rw";
	private String callbackURL = Constants.getString("TWITTER_CALLBACK_URL");// "http://localhost:8080/HumansService/scrumpy-twitter";

	private Token accessToken;
	protected TwitterUser user;
	protected DB db;
	protected TwitterStatusDAO statusDAO;
	protected TwitterUserDAO userDAO;
	protected TwitterFollowsDAO followsDAO;
	protected ServiceTokenDAO tokenDAO;

	private OAuthService service;
	// private static final String FRIENDS_LIST_URL =
	// "https://api.twitter.com/1.1/friends/list.json?user_id=%s&cursor=%s";
	private static final String FRIENDS_IDS_URL = "https://api.twitter.com/1.1/friends/ids.json?user_id=%s&cursor=%s&count=5000";
	private static final String VERIFY_URL = "https://api.twitter.com/1.1/account/verify_credentials.json";
	private static final String SHOW_USER_BY_ID_URL = "https://api.twitter.com/1.1/users/show.json?user_id=%s&include_entities=true";
	private static final String USER_LOOKUP_URL = "https://api.twitter.com/1.1/users/lookup.json";
	private static final String STATUS_URL = "https://api.twitter.com/1.1/statuses/user_timeline.json?user_id=%s&trim_user=true";

	protected Gson gson;

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

		gson = new GsonBuilder().setDateFormat("EEE MMM dd HH:mm:ss Z yyyy")
				.serializeNulls().create();
	}

	// TODO Change this all ridiculous constructor. Should all be factory
	// methods like above.
	public TwitterService(Token aAccessToken) {
		this();
		accessToken = aAccessToken;
		service = new ServiceBuilder().provider(TwitterApi.class)
				.apiKey(apiKey).apiSecret(apiSecret).callback(callbackURL)
				.build();
	}

	public static TwitterService createTwitterServiceOnBehalfOfUsername(
			String aUsername) {
		TwitterService result;
		Token token;
		TwitterUser user = TwitterService
				.getLocalUserBasicForUsername(aUsername);
		if (user == null) {
			logger.warn("null token. trying to find one by username "
					+ aUsername);
			token = TwitterService.deserializeToken(aUsername);
			result = new TwitterService(token);
			user = result.serviceRequestUserBasic();
		} else {
			token = TwitterService.deserializeToken(user);
			result = new TwitterService(token);
		}
		if (token == null) {
			logger.error("null token for " + aUsername);
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


	/**
	 * This will go to the service and get "self" for whoever's accessToken we
	 * have
	 */
	public TwitterUser serviceRequestUserBasic() {
		OAuthRequest request = new OAuthRequest(Verb.GET, VERIFY_URL);
		service.signRequest(accessToken, request);
		Response response = request.send();

		Map<String, String> h = response.getHeaders();
		// logger.info("From Headers for Twitter Request rate-limit="+h.get("x-rate-limit-limit")+" reset ms="+h.get("x-rate-limit-reset"));

		String s = response.getBody();
		JSONObject obj = (JSONObject) JSONValue.parse(s);
		TwitterUser user;// = serviceRequestUserBasicForUserID("self");
		com.nearfuturelaboratory.humans.twitter.entities.TwitterUser tuser = gson
				.fromJson(
						s,
						com.nearfuturelaboratory.humans.twitter.entities.TwitterUser.class);
		userDAO.save(tuser);
		// this.saveUserBasicJson(obj);
		this.user = tuser;
		return tuser;
	}

	public TwitterUser serviceRequestUserBasicForUserID(String aUserID) {
		JSONObject aUser;
		String userURL = String.format(SHOW_USER_BY_ID_URL, aUserID);
		OAuthRequest request = new OAuthRequest(Verb.GET, userURL);
		service.signRequest(accessToken, request);
		Response response = request.send();
		String s = response.getBody();
		// TODO Check for errors
		Map<String, String> h = response.getHeaders();
		Object obj = JSONValue.parse(s);
		aUser = (JSONObject) ((JSONObject) obj);
		// this.saveUserBasicJson(aUser);
		com.nearfuturelaboratory.humans.twitter.entities.TwitterUser tuser = gson
				.fromJson(
						aUser.toString(),
						com.nearfuturelaboratory.humans.twitter.entities.TwitterUser.class);

		userDAO.save(tuser);
		return tuser;
	}

	protected TwitterUser getLocalUserBasicForUserID(String aUserID) {
		TwitterUser result = null;
		result = userDAO.findByExactUserID(aUserID);
		return result;
	}
	

	public boolean localServiceStatusIsFresh() {
		return localUserBasicIsFreshForUserID(this.getThisUser().getId());
	}

	public boolean localUserBasicIsFreshForUserID(String aUserID) {
		boolean result = false;
		com.nearfuturelaboratory.humans.twitter.entities.TwitterUser user = this
				.getLocalUserBasicForUserID(aUserID);

		if (user == null)
			return false;

		Date d = user.getLastUpdated();

		long then = d.getTime();
		long now = new Date().getTime();
		long diff = now - then;
		if (diff < Constants.getLong("USER_BASIC_STALE_TIME")) {
			result = true;
		}

		return result;
	}

	public TwitterUser getThisUser() {
		return user;
	}

	public TwitterStatus getMostRecentStatus() {
		return statusDAO.findMostRecentStatusByExactUserID(this.getThisUser()
				.getId());
	}

	public TwitterStatus getOldestStatus(String aUserID) {
		return statusDAO.findOldestStatusByExactUserID(aUserID);
	}

	public List<TwitterStatus> serviceRequestStatus() {
		return serviceRequestStatusForUserID(this.user.getId_str());
	}

	public List<TwitterStatus> serviceRequestStatusForUserID(String aUserID) {
		// long max_id = serviceRequestStatusForUserIDAndMaxID(aUserID, null);
		if (aUserID.equalsIgnoreCase("self") || aUserID == null) {
			aUserID = user.getId_str();
		}
		List<TwitterStatus> result;
		TwitterStatus most_recent = statusDAO.findMostRecentStatusByExactUserID(aUserID);
		if (most_recent == null) {
			result = this.serviceRequestStatusForUserIDAndSinceID(aUserID, null);
		} else {
			result = serviceRequestStatusForUserIDAndSinceID(aUserID,
					most_recent.getId_str());

		}
		return result;
	}

	protected List<TwitterStatus> serviceRequestStatusForUserIDAndSinceID(
			String aUserID, String since_id) {
		if (aUserID == null || aUserID.equalsIgnoreCase("self")) {
			// logger.debug(user.get("id").getClass());
			aUserID = user.getId_str().toString();
		}
		String statusURL = String.format(STATUS_URL, aUserID);
		OAuthRequest request = new OAuthRequest(Verb.GET, statusURL);
		request.addQuerystringParameter("count", "200");

		if (since_id != null) {
			request.addQuerystringParameter("since_id", since_id);
		}

		service.signRequest(accessToken, request);
		Response response = request.send();
		String s = response.getBody();
		// TODO error chck
		Object objResponse = JSONValue.parse(s);
		// logger.debug(objResponse);
		// TODO error checking!
		JSONArray jsonResponse = (JSONArray) objResponse;

		return this.saveStatusJson(jsonResponse);
	}

	public List<TwitterStatus> getStatus() {
		return getStatusForUserID(this.getThisUser().getId_str());
	}

	public List<TwitterStatus> getStatusForUserID(String aUserID) {
		// List<TwitterStatus> result = new ArrayList<TwitterStatus>();
		return statusDAO.findByExactUserID(aUserID);
	}


	protected void serviceRequestStatusForUserIDAndMaxID(String aUserID,
			String max_id) {
		Object objResponse = null;
		try {
			if (aUserID == null || aUserID.equalsIgnoreCase("self")) {
				aUserID = user.getId_str().toString();
			}
			String statusURL = String.format(STATUS_URL, aUserID);
			OAuthRequest request = new OAuthRequest(Verb.GET, statusURL);
			request.addQuerystringParameter("count", "200");

			if (max_id != null) {
				request.addQuerystringParameter("max_id", max_id);
			}

			service.signRequest(accessToken, request);
			Response response = request.send();
			String s = response.getBody();
			// TODO Error checking
			objResponse = JSONValue.parse(s);

			JSONArray jsonResponse = (JSONArray) objResponse;
			// Map<String, String> h = response.getHeaders();

			if (jsonResponse != null && jsonResponse.size() > 0) {
				this.saveStatusJson(jsonResponse);
			}
		} catch (Exception e) {
			logger.error(e);
			logger.debug(objResponse.toString());
			e.printStackTrace();
		}
	}

	List<TwitterStatus> saveStatusJson(JSONArray status) {
		List<TwitterStatus> result = new ArrayList<TwitterStatus>();

		@SuppressWarnings("unchecked")
		Iterator<JSONObject> iter = status.iterator();
		while (iter.hasNext()) {
			String i = iter.next().toString();
			// logger.debug(i);
			TwitterStatus tstatus = gson.fromJson(i,TwitterStatus.class);
			result.add(tstatus);
			statusDAO.save(tstatus);
		}
		return result;

	}

	protected static Comparator<JSONObject> TwitterStatusIDComparator = new Comparator<JSONObject>() {
		public int compare(JSONObject status_1, JSONObject status_2) {
			int result = 0;
			if (status_1 != null && status_2 != null) {
				long status_1_id = Long
						.parseLong(status_1.get("id").toString());
				long status_2_id = Long
						.parseLong(status_2.get("id").toString());
				if (status_1_id > status_2_id) {
					result = 1;
				} else {
					result = -1;
				}
			}
			return result;
		}
	};

	public boolean localServiceStatusIsFreshFor(String aUserID) {
		boolean result = false;
		com.nearfuturelaboratory.humans.twitter.entities.TwitterStatus most_recent = this
				.getMostRecentStatusForUserID(aUserID);
		if (most_recent == null) {
			return result;
		}
		Date d = most_recent.getLastUpdated();

		long then = d.getTime();
		long now = new Date().getTime();

		long diff = now - then;
		if (diff < Constants.getLong("STATUS_STALE_TIME")) {
			result = true;
		}

		return result;
	}

	private TwitterStatus getMostRecentStatusForUserID(String aUserID) {
		return statusDAO.findMostRecentStatusByExactUserID(aUserID);
	}

	public List<TwitterFriend> getFriends() {
		return getFriendsFor(this.getThisUser().getId());
	}

	protected List<TwitterFriend> getFriendsFor(String aUserId) {
		List<TwitterFriend> follows = new ArrayList<TwitterFriend>();
		follows = followsDAO.findFollowsByExactUserID(this.getThisUser().getId());
		return follows;
	}


	public boolean localFriendsIsFresh() {
		return localFollowsIsFreshFor(this.getThisUser().getId());
	}

	protected boolean localFollowsIsFreshFor(String aUserID) {
		boolean result = false;

		TwitterFriend follows = followsDAO.findOldestFriendByExactUserID(aUserID);

		if (follows == null)
			return false;

		Date d = follows.getFriend().getLastUpdated();

		long then = d.getTime();
		long now = new Date().getTime();
		long diff = now - then;
		if (diff < Constants.getLong("FOLLOWS_STALE_TIME")) {
			result = true;
		}

		return result;
	}


	public void serviceRequestFollows() {
		serviceRequestFollowsForUserID((String) user.getId_str());
	}




	@SuppressWarnings("unchecked")
	public void serviceRequestFollowsForUserID(String aUserID) {
		String id_str = (String) user.getId_str();
		if (aUserID == null) {
			aUserID = id_str;
		}

		TwitterUser aUser;

		if (aUserID.equalsIgnoreCase(id_str)) {
			aUser = user;
		} else {
			// check to see if we already have this user..
			if (this.localUserBasicIsFreshForUserID(aUserID)) {
				aUser = userDAO.findByExactUserID(aUserID);
			} else {
				aUser = serviceRequestUserBasicForUserID(aUserID);
			}
		}

		String followsURL = String.format(FRIENDS_IDS_URL, aUserID, "-1");
		OAuthRequest request = new OAuthRequest(Verb.GET, followsURL);
		service.signRequest(accessToken, request);

		Response response = request.send();
		// TODO Error checking
		String s = response.getBody();

		Map<String, String> h = response.getHeaders();

		//logger.info("From Headers for Twitter Request rate-limit="+h.get("x-rate-limit-limit")+" reset ms="+h.get("x-rate-limit-reset"));
		Object obj = JSONValue.parse(s);
		JSONObject map = (JSONObject) obj;

		// error check
		if (map != null && map.get("errors") == null) {
			Long next_cursor = (Long) map.get("next_cursor");
			long next_cursor_l = next_cursor.longValue();
			// System.out.println("next_cursor="+next_cursor);
			JSONArray allFollowsIDs = new JSONArray();
			//JSONArray allFollowsHydrated = new JSONArray();
			List<JSONObject> allFollowsHydrated = new ArrayList<JSONObject>();
			int page_count = 1;
			do {
				// JSONArray users = (JSONArray)map.get("users");
				JSONArray users = (JSONArray) map.get("ids");
				allFollowsIDs.addAll(users);
				String next_url = String.format(FRIENDS_IDS_URL,
						user.getScreen_name(), next_cursor); // (String)pagination.get("next_url");
				next_cursor_l = next_cursor.longValue();
				if (next_cursor != null && next_cursor_l != 0) {
					request = new OAuthRequest(Verb.GET, (String) next_url);
					service.signRequest(accessToken, request);
					response = request.send();
					s = response.getBody();
					map = (JSONObject) JSONValue.parse(s);
					next_cursor = (Long) map.get("next_cursor");
					h = response.getHeaders();
					logger.debug("paging (" + page_count++
							+ ") through follows for " + user.getScreen_name());
				} else {
					break;
				}
			} while (next_cursor != null && next_cursor_l != 0);

			//JSONObject meta = (JSONObject) map.get("meta");

			OAuthRequest postUserLookup = new OAuthRequest(Verb.POST, USER_LOOKUP_URL);
			StringBuffer buf;// = new StringBuffer();

			List<String> chunks = partition(allFollowsIDs, 100);

			Iterator iter = chunks.iterator();
			while (iter.hasNext()) {
				List<Long> chunk = (List<Long>) iter.next();
				buf = new StringBuffer();
				for (int i = 0; (i < 100 && i < chunk.size()); i++) {
					buf.append(chunk.get(i));
					buf.append(",");
				}
				buf.deleteCharAt(buf.length() - 1);

				postUserLookup = new OAuthRequest(Verb.POST, USER_LOOKUP_URL);
				postUserLookup.setConnectionKeepAlive(false);

				postUserLookup.addBodyParameter("user_id", buf.toString());
				service.signRequest(accessToken, postUserLookup);
				response = postUserLookup.send();
				s = response.getBody();
				h = response.getHeaders();
				// logger.info("From Headers for Twitter Request rate-limit="+h.get("x-rate-limit-limit")+" reset ms="+h.get("x-rate-limit-reset"));

				JSONArray usersArray = (JSONArray) JSONValue.parse(s);
				allFollowsHydrated.addAll(usersArray);
			}

			saveFollows(allFollowsHydrated, aUserID);
		} else {

			logger.warn("Do something about rate limit errors, etc."
					+ map.toString());
		}

	}
	//TODO we should delete all the friends of follower_id first..
	protected List<TwitterFriend> saveFollows(List<JSONObject> list_of_friends, String follower_id) {

		List<TwitterFriend> new_friends= new ArrayList<TwitterFriend>();
		for(JSONObject j : list_of_friends) {
			TwitterUser friend = gson.fromJson(j.toString(), TwitterUser.class);
			userDAO.save(friend);
			TwitterUser follower = userDAO.findByExactUserID(follower_id);
			TwitterFriend f = followsDAO.findFollowsByUserIDFollowsID(friend.getId(), follower_id);
			if(f == null) {
				f = new TwitterFriend();
				f.setFriend(friend);
				f.setFollower(follower);

				f.setFollower_id(follower_id);
				f.setFriend_id(friend.getId());
			}
			new_friends.add(f);
		}
		// presave this will be the friends the last time we made a service request
		List<TwitterFriend> existing_friends = this.getFriendsFor(follower_id);

		Collection<TwitterFriend> new_friends_to_save = CollectionUtils.subtract(new_friends, existing_friends);
		Collection<TwitterFriend> no_longer_friends = CollectionUtils.subtract(existing_friends, new_friends);

		// delete the ones that have dropped off
		for(TwitterFriend not_a_friend : no_longer_friends) {
			followsDAO.delete(not_a_friend);
			// but keep the user around?
			//
		}
		
		// okay, because TwitterFriend is a weird data structure, we need to
		// load the item by the the compound key (friend id and follower or 'my' ID)
		// but we also need that 'friend' as a TwitterUser so we have to take the friend's ID and either
		// load that person from the DB or load them from the service
		for(TwitterFriend is_a_friend : new_friends_to_save) {
			followsDAO.save(is_a_friend);
		}




		
		return followsDAO.findFollowsByExactUserID(this.getThisUser()
				.getId_str());
	}



	/**
	 * Weird bootstrap method needed while migrating. We really should not have
	 * a token if we don't have a local user in the database, but we did when we
	 * migrated Tokens from the old filesystem database and the database had no
	 * users in it yet. Really, when you save a new user you also save the user
	 * basic with the token
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
		ServiceToken tokenToSave = dao.findByExactUserId(aUser.getId()); //new ServiceToken();
		if(tokenToSave == null) {
			tokenToSave = new ServiceToken();
		}
		tokenToSave.setToken(aToken);
		tokenToSave.setUser_id(aUser.getId());
		tokenToSave.setUsername(aUser.getScreen_name());
		tokenToSave.setServicename("twitter");
		
		dao.save(tokenToSave);
	}

	public static Token deserializeToken( TwitterUser aUser) {
		// Token result = null;
		ServiceTokenDAO dao = new ServiceTokenDAO("twitter");
		ServiceToken serviceToken = dao.findByExactUserId(aUser.getId_str());
		return serviceToken.getToken();
	}

	public JsonObject getStatusJSON() {
		JsonObject obj = new Gson().toJsonTree(this, this.getClass()).getAsJsonObject();
		obj.addProperty("service", "twitter");
		return obj;
	}


	public long getCreated() {
		return this.getCreatedDate().getTime();
	}



}
