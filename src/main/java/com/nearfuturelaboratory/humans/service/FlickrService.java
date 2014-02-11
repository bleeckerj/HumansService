package com.nearfuturelaboratory.humans.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.FlickrApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.jsonpath.JsonPath;
import com.nearfuturelaboratory.humans.dao.FlickrFollowsDAO;
import com.nearfuturelaboratory.humans.dao.FlickrStatusDAO;
import com.nearfuturelaboratory.humans.dao.FlickrUserDAO;
import com.nearfuturelaboratory.humans.dao.ServiceTokenDAO;
import com.nearfuturelaboratory.humans.entities.ServiceToken;
import com.nearfuturelaboratory.humans.exception.BadAccessTokenException;
import com.nearfuturelaboratory.humans.flickr.entities.FlickrFriend;
import com.nearfuturelaboratory.humans.flickr.entities.FlickrStatus;
import com.nearfuturelaboratory.humans.flickr.entities.FlickrUser;
import com.nearfuturelaboratory.util.Constants;
//import com.sun.istack.internal.NotNull;

public class FlickrService {
	final static Logger logger = LogManager.getLogger(com.nearfuturelaboratory.humans.service.FlickrService.class);

	private String apiKey = Constants.getString("FLICKR_API_KEY");
	private String apiSecret = Constants.getString("FLICKR_API_SECRET");
	private String callbackURL = Constants.getString("FLICKR_CALLBACK_URL");

	private Token accessToken;
	protected FlickrUser user;
	private OAuthService service;
	//	private static final String FRIENDS_LIST_URL = "https://api.tumblr.com/v2/user/following?limit=20&offset=%s";
	//	private static final String GET_CONTACTS_URL = "";
	//	private static final String USER_INFO = "https://api.tumblr.com/v2/user/info";
	private static final String SERVICE_URL = "https://secure.flickr.com/services/rest/?";

	protected FlickrUserDAO userDAO;
	protected FlickrStatusDAO statusDAO;
	protected FlickrFollowsDAO friendsDAO;

	protected Gson gson;

	public static FlickrService createFlickrServiceOnBehalfOfUserID(String aUserID) throws BadAccessTokenException {
		FlickrService result;
		Token token = null;
		FlickrUser user = getLocalUserBasicForUsername(aUserID);
		if(user == null) {
			token = FlickrService.deserializeTokenByUserID(aUserID);		
		} else {
			token = FlickrService.deserializeToken(user);
		}
		if(token == null) {
			throw new BadAccessTokenException("Bad access token for Flickr for userid="+aUserID+" user="+user);
		}
		result = new FlickrService(token);

		if(user == null) {
			user = result.serviceRequestUserBasicForUserID(aUserID);
		}
		result.user = user;
		return result;
	}

	private static FlickrUser getLocalUserBasicForUsername(String aUserID) {
		FlickrUserDAO dao = new FlickrUserDAO();
		return dao.findByExactUserID(aUserID);
	}

	public FlickrService() {

		userDAO = new FlickrUserDAO();
		userDAO.ensureIndexes();

		statusDAO = new FlickrStatusDAO();
		statusDAO.ensureIndexes();

		friendsDAO = new FlickrFollowsDAO();
		friendsDAO.ensureIndexes();

		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss").create();

		//		gson = new Gson();
	}


	public FlickrService(Token aAccessToken) {

		this();
		accessToken = aAccessToken;
		service = new ServiceBuilder()
		.provider(FlickrApi.class)
		.apiKey(apiKey)
		.apiSecret(apiSecret)
		.callback(callbackURL)
		.build();
	}


	//	public FlickrStatus getMostRecentStatusForUserID(String aUserID) {
	//		statusDAO.
	//	}


	public boolean localServiceStatusIsFreshForUserID(String aServiceID) {
		boolean result = false;
        long then = 0;
		//Date d = this. getLatestStatus().getDateupload();
        FlickrStatus latest = this.getLatestStatus();
        if(latest != null) {
		then = this.getLatestStatus().getDateupload();
        }
		long now = new Date().getTime();
		long diff = now - then;
		if(diff < Constants.getLong("STATUS_STALE_TIME")) {
			result = true;
		}
		return result;



	}

	public FlickrStatus getLatestStatus() {
		FlickrStatus lastLocalStatus = statusDAO.findMostRecentStatusByExactUserID(this.getThisUser().getId());
		return lastLocalStatus;
	}

	public List<FlickrStatus> getStatusForUserID(String aUserID) 
	{
		List<FlickrStatus> result = new ArrayList<FlickrStatus>();
        result = statusDAO.findByExactUserID(aUserID);
		return result;
	}


	public void serviceRequestStatus() {
		serviceRequestStatusForUserID(getThisUser().getId());
	}

	/**
	 * Okay. Weird one. This will get status for a user after the most recent status we already have stored locally.
	 * 
	 * @param aUserID
	 * @return The status retrieved
	 */
	public List<FlickrStatus> serviceRequestStatusForUserID(String aUserID) {
		//		this.serviceRequestStatusForUserIDToMonthsAgo(aUserID, 6);
		if(aUserID == null || aUserID.equalsIgnoreCase("self")) {
			aUserID = (String)user.getId();
		}



		FlickrStatus latest = statusDAO.findMostRecentStatusByExactUserID(aUserID);
		if(latest == null) {
			logger.info("Doesn't seem to be any local status for "+aUserID+". Going to try and fix that by requesting some.");
			return this.serviceRequestStatusForUserIDToMonthsAgo(aUserID, 1);
		}
		OAuthRequest request = new OAuthRequest(Verb.GET, SERVICE_URL);
		request.addQuerystringParameter("method", "flickr.people.getPhotos");
		request.addQuerystringParameter("api_key",apiKey);
		request.addQuerystringParameter("user_id", aUserID);
		request.addQuerystringParameter("extras", "description, license, date_upload, date_taken, owner_name, icon_server, original_format, last_update, geo, tags, machine_tags, o_dims, views, media, path_alias, url_sq, url_t, url_s, url_q, url_m, url_n, url_z, url_c, url_l, url_o");
		request.addQuerystringParameter("format", "json");
		request.addQuerystringParameter("per_page", "100");
		request.addQuerystringParameter("page","1");
		request.addQuerystringParameter("min_upload_date", String.valueOf(latest.getDateupload()));
		request.addQuerystringParameter("nojsoncallback", "1");
		service.signRequest(accessToken, request);
		Response response = request.send();
		String s = response.getBody();

        //TODO Error checking
        // eg { "stat": "fail", "code": "112", "message": "Method unknown not found" }
		Object jsonResponse = JSONValue.parse(s);
		JSONObject status = (JSONObject)jsonResponse;

		JSONArray photos = new JSONArray();
        try {
        photos = JsonPath.read(status, "photos.photo");

		int page = Integer.parseInt(		JsonPath.read(status, "photos.page").toString());
		int pages = Integer.parseInt(		JsonPath.read(status, "photos.pages").toString());
		JSONArray latest_data;
		while(pages > page) {
			request = new OAuthRequest(Verb.GET, SERVICE_URL);
			request.addQuerystringParameter("method", "flickr.people.getPhotos");
			request.addQuerystringParameter("api_key",apiKey);
			request.addQuerystringParameter("user_id", aUserID);
			request.addQuerystringParameter("extras", "description, license, date_upload, date_taken, owner_name, icon_server, original_format, last_update, geo, tags, machine_tags, o_dims, views, media, path_alias, url_sq, url_t, url_s, url_q, url_m, url_n, url_z, url_c, url_l, url_o");
			request.addQuerystringParameter("format", "json");
			request.addQuerystringParameter("per_page", "100");
			request.addQuerystringParameter("page", String.valueOf(page+1));
			request.addQuerystringParameter("min_upload_date", String.valueOf(latest.getDateupload()));
			request.addQuerystringParameter("nojsoncallback", "1");
			service.signRequest(accessToken, request);
			response = request.send();
			s = response.getBody();
			jsonResponse = JSONValue.parse(s);
			status = (JSONObject)jsonResponse;
			latest_data = JsonPath.read(status, "photos.photo");
			photos.addAll(latest_data);
			//oldest = (JSONObject)photos.get(photos.size()-1);
			//			try {
			//				oldest_taken_date =  formatter.parse(oldest.get("datetaken").toString());
			//			} catch (java.text.ParseException e) {
			//				logger.error(e);
			//				e.printStackTrace();
			//				break;
			//			}
			//			oldest_time = oldest_taken_date.getTime();
			page = Integer.parseInt(JsonPath.read(status, "photos.page").toString());
			pages = Integer.parseInt(JsonPath.read(status, "photos.page").toString());

		}
        } catch(Exception e) {
            logger.error(e);
            logger.error("For this FlickrStatus "+status);
        }
		return saveStatus(photos);

	}

	//protected void serviceRequestStatusForUserID

	@SuppressWarnings("unchecked")
	protected List<FlickrStatus> serviceRequestStatusForUserIDToMonthsAgo(String aUserID, int aMonthsAgo) {
		Calendar ago =Calendar.getInstance();
		ago.add(Calendar.MONTH, -1*aMonthsAgo);
		String pattern = "yyyy-MM-dd hh:mm:ss";
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		String mysqlDateString = formatter.format(ago.getTime());
		//long year_ago = ago.getTimeInMillis();
		if(aUserID == null || aUserID.equalsIgnoreCase("self")) {
			aUserID = (String)user.getId();
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
			logger.warn("Despite best efforts, I got an error from Flickr "+status.toString()+"\nfor userid="+aUserID+"\n"+this);
			return new ArrayList<FlickrStatus>();
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
			logger.warn("Despite my best efforts, I got nothing back from Flickr for userid="+aUserID+"\n"+this);
			return new ArrayList<FlickrStatus>();
		} else {
			oldest = (JSONObject)photos.get(photos.size()-1);
		}

		Date oldest_taken_date;
		long oldest_time = new Date().getTime();

		try {
			oldest_taken_date = formatter.parse(oldest.get("datetaken").toString());
			oldest_time = oldest_taken_date.getTime();

		} catch (java.text.ParseException e) {
			logger.error(e);
			logger.error(e.getStackTrace());
			e.printStackTrace();
		} catch( NullPointerException e) {
            logger.error(e);
            logger.error(e.getStackTrace());
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
		return saveStatus(photos);

	}


	protected void saveUser(FlickrUser aUser) {
		userDAO.save(aUser);
	}

	@SuppressWarnings("unchecked")
	protected List<FlickrStatus> saveStatus(JSONArray status) {
		List<FlickrStatus>result = new ArrayList<FlickrStatus>();

		Iterator<JSONObject> iter = status.iterator();
		while(iter.hasNext()) {
			String i = iter.next().toString();
			FlickrStatus fstatus = gson.fromJson(i, FlickrStatus.class);
			result.add(fstatus);
			statusDAO.save(fstatus);
		}
		return result;
	}

	//	/**
	//	 *  Get basic user info from Flickr
	//	 */
	//	public void serviceRequestUserBasic(String aUserID) {
	//		OAuthRequest request = new OAuthRequest(Verb.GET, SERVICE_URL);
	//		request.addQuerystringParameter("method", "flickr.people.getInfo");
	//		request.addQuerystringParameter("user_id", aUserID);
	//		request.addQuerystringParameter("format", "json");
	//		request.addQuerystringParameter("nojsoncallback", "1");
	//
	//		service.signRequest(accessToken, request);
	//		Response response = request.send();
	//		String s = response.getBody();
	//		JSONObject obj = (JSONObject)JSONValue.parse(s);
	//		logger.debug("got "+s+" "+obj);
	//		JSONObject user = (JSONObject)obj.get("user");
	//		String id = user.get("id").toString();
	//		logger.debug("id="+id);
	//		//serviceRequestUserBasicByUserID(id);
	//	}

	

	/**
	 * Get basic user info on a user
	 * @param aUserID
	 */
	public FlickrUser serviceRequestUserBasicByUserID(String aUserID) {
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

		this.user = gson.fromJson(obj.get("person").toString(), FlickrUser.class);		


		saveUser(user);
        return user;
	}


	public FlickrUser getThisUser() {
		return user;
	}


    public FlickrUser getUserByID(String aID) throws BadAccessTokenException {
        FlickrUser user = userDAO.findByExactUserID(aID);
        if(user == null) {
            user = this.serviceRequestUserBasicForUserID(aID);
            userDAO.save(user);
        }
        return user;
    }


	public static URL getURLForBuddyIconForUser(JSONObject aUser) {
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
	
	public FlickrUser serviceRequestUserBasic() throws BadAccessTokenException
	{
		return serviceRequestUserBasicForUserID(this.user.getId());
	}

	public FlickrUser serviceRequestUserBasicForUserID(String aUserID) throws BadAccessTokenException
	{
		Response response = null;

		JSONObject aUser;

		OAuthRequest request = new OAuthRequest(Verb.GET, SERVICE_URL);
		request.addQuerystringParameter("method", "flickr.people.getInfo");
		request.addQuerystringParameter("format", "json");
		request.addQuerystringParameter("nojsoncallback", "1");
		request.addQuerystringParameter("user_id", aUserID);


		service.signRequest(accessToken, request);
		response = request.send();
		String s = response.getBody();

		JsonElement e = new JsonParser().parse(s);
		JsonObject o = e.getAsJsonObject();
		if(o != null && o.get("stat").getAsString().equalsIgnoreCase("fail")) {

			String msg = o.get("message").getAsString();
			int code = o.get("code").getAsInt();
			if(code == 98) {
				throw new BadAccessTokenException("For user id "+aUserID+" Flickr says '"+msg+"'\nToken is "+this.accessToken);
			} else {
				logger.warn("Flickr returned fail but the code wasn't 98 "+o);
			}
		}

		JSONObject obj = (JSONObject)JSONValue.parse(s);
		aUser = (JSONObject)obj.get("person");
		FlickrUser fuser = gson.fromJson(aUser.toJSONString(), FlickrUser.class);

		return fuser;
	}


	public FlickrUser serviceRequestUserInfo()
	{
		OAuthRequest request = new OAuthRequest(Verb.GET, SERVICE_URL);
		request.addQuerystringParameter("method", "flickr.test.login");
		request.addQuerystringParameter("format", "json");
		request.addQuerystringParameter("nojsoncallback", "1");

		service.signRequest(accessToken, request);
		Response response = request.send();
		String s = response.getBody();
		JSONObject obj = (JSONObject)JSONValue.parse(s);
		//logger.debug("got "+s+" "+obj);
		JSONObject user = (JSONObject)obj.get("user");
		String id = user.get("id").toString();
		//logger.debug("id="+id);
		return serviceRequestUserBasicByUserID(id);
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
			logger.warn(obj.get("message")+" for:\n\r"+this.getThisUser());
		}
		return result;
	}


	public boolean localFriendsIsFresh() {
		boolean result = false;
		FlickrFriend friend = friendsDAO.findNewestFriendByExactUserID(this.getThisUser().getId());

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

	public List<FlickrFriend> getFriends() {
		return friendsDAO.findByUserID(this.getThisUser().getId());
	}

	protected List<FlickrFriend> getFriendsFor(String aUserId) {
		return friendsDAO.findByUserID(aUserId);
	}

	//TODO might be a few pages need to check right now you only get the first 1000
	public void serviceRequestFriends() {
		List<JSONObject> follows;// = new JSONArray();
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
			List<JSONObject> f = JsonPath.read(obj, "contacts.contact");

			if(f != null) {
				follows.addAll(f);		    
			}
			pages = Integer.parseInt(JsonPath.read(obj, "contacts.pages").toString());
			page = Integer.parseInt(JsonPath.read(obj, "contacts.page").toString());
		}
		saveFollowsJson(follows, this.getThisUser().getId());
	}



    protected void saveFollowsJson(List<JSONObject> list_of_friends, String follower_id) {
		//JSONArray result = new JSONArray();
		//friendsDAO.deleteByQuery??;

		List<FlickrFriend> new_friends= new ArrayList<FlickrFriend>();
		for(JSONObject j : list_of_friends) {
			FlickrFriend friend = gson.fromJson(j.toString(), FlickrFriend.class);
			new_friends.add(friend);
		}

		// presave this will be the friends the last time we made a service request
		List<FlickrFriend> existing_friends = this.getFriendsFor(follower_id);

		Collection<FlickrFriend> new_friends_to_save = CollectionUtils.subtract(new_friends, existing_friends);
		Collection<FlickrFriend> no_longer_friends = CollectionUtils.subtract(existing_friends, new_friends);

		for(FlickrFriend not_a_friend : no_longer_friends) {
			friendsDAO.delete(not_a_friend);
		}

		for(FlickrFriend is_a_friend : new_friends_to_save) {
			FlickrFriend f = friendsDAO.findByFriendIDUserID(is_a_friend.getFriendID(), follower_id);
			//FlickrUser friend_as_user = userDAO.findByExactUserID(is_a_friend.getFriendID());
			if(f == null) {
				//is_a_friend.setFriend(aFriend);
				is_a_friend.setUser(this.getThisUser());
				friendsDAO.save(is_a_friend);
			} else {
				friendsDAO.save(f);
			}
		}


	}


	/**
	 * Weird bootstrap method needed while migrating. We really should not have a token
	 * if we don't have a local user in the database, but we did when we migrated Tokens from the
	 * old filesystem database and the database had no users in it yet. Really, when you
	 * save a new user you also save the user basic with the token
	 * 
	 * @param aUserID
	 */
	private static Token deserializeTokenByUserID(String aUserID) {
		ServiceTokenDAO dao = new ServiceTokenDAO("flickr");
		ServiceToken st = dao.findByExactUserID(aUserID);
		if(st == null) {
			return null;
		}
		return st.getToken();
	}


    public static void serializeToken(Token aToken, FlickrUser aUser) {
        ServiceTokenDAO dao = new ServiceTokenDAO("flickr");
        ServiceToken tokenToSave = dao.findByExactUserId(aUser.getId()); //new ServiceToken();
        if(tokenToSave == null) {
            tokenToSave = new ServiceToken();
        }
        tokenToSave.setToken(aToken);
        tokenToSave.setUser_id(aUser.getId());
        tokenToSave.setUsername(aUser.getUsername());
        //tokenToSave; aUser.getLargeImageURL()
        tokenToSave.setServicename("twitter");

        dao.save(tokenToSave);

    }


	public  void serializeToken(Token aToken) {
		ServiceTokenDAO dao = new ServiceTokenDAO("flickr");
		ServiceToken tokenToSave = dao.findByExactUserId(this.getThisUser().getId());new ServiceToken();
		if(tokenToSave == null) {
			tokenToSave = new ServiceToken();
		}
		tokenToSave.setToken(aToken);
		tokenToSave.setUser_id(this.getThisUser().getId());
		tokenToSave.setUsername(this.getThisUser().getUsername());
		tokenToSave.setServicename("flickr");
		dao.save(tokenToSave);

	}

//	public static void serializeToken(Token aToken, FlickrUser aUser) {
//		ServiceTokenDAO dao = new ServiceTokenDAO("flickr");
//		ServiceToken tokenToSave = dao.findByExactUserId(aUser.getId());//new ServiceToken();
//		if(tokenToSave == null) {
//			tokenToSave = new ServiceToken();
//		}
//		tokenToSave.setToken(aToken);
//		tokenToSave.setUser_id(aUser.getId());
//		tokenToSave.setUsername(aUser.getUsername());
//		tokenToSave.setServicename("flickr");
//		dao.save(tokenToSave);
//	}
//

	public static Token deserializeToken(FlickrUser aUser) {
		//Token result = null;
		ServiceTokenDAO dao = new ServiceTokenDAO("flickr");
		ServiceToken serviceToken = dao.findByExactUserId( aUser.getId() );
		return serviceToken.getToken();
	}


	public String toString() {
		return "["+this.getThisUser().toString()+", accessToken="+this.accessToken+"]";
	}

    public long getStatusCountForUserID(String userID) {
        long result = 0;
        result = statusDAO.getStatusCountForUserID(userID);
        return result;
    }
}

