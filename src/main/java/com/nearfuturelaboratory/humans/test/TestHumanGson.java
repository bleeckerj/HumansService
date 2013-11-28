package com.nearfuturelaboratory.humans.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.gson.*;
import com.nearfuturelaboratory.humans.entities.HumansUser;
import com.nearfuturelaboratory.humans.entities.Human;
import com.nearfuturelaboratory.humans.entities.ServiceUser;
import com.nearfuturelaboratory.humans.exception.BadAccessTokenException;
import com.nearfuturelaboratory.humans.dao.HumansUserDAO;
import com.nearfuturelaboratory.humans.flickr.entities.FlickrStatus;
import com.nearfuturelaboratory.humans.foursquare.entities.FoursquareCheckin;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramStatus;
import com.nearfuturelaboratory.humans.service.FlickrService;
import com.nearfuturelaboratory.humans.service.FoursquareService;
import com.nearfuturelaboratory.humans.service.InstagramService;
import com.nearfuturelaboratory.humans.service.TwitterService;
import com.nearfuturelaboratory.humans.service.status.ServiceStatus;
import com.nearfuturelaboratory.humans.twitter.entities.*;
import com.nearfuturelaboratory.util.Constants;
import com.nearfuturelaboratory.util.Pair;

public class TestHumanGson {
	final static Logger logger = Logger.getLogger(com.nearfuturelaboratory.humans.test.TestHumanGson.class);

	public TestHumanGson() {
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws ConfigurationException, IOException, ParseException {
		// TODO Auto-generated method stub
		try {
			Constants.load("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/WebContent/WEB-INF/lib/dev.app.properties");
			PropertyConfigurator.configureAndWatch("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/WebContent/WEB-INF/lib/static-logger.properties");
			logger.debug("Hello!");
		} catch(Exception e) {
			e.printStackTrace();
		}
		TestHumanGson test = new TestHumanGson();
		//		HumansUser me = new HumansUser("darthjulian", "darthjulian");
		//		me.removeServiceUsersOnBehalfOf("185383-darthjulian");

		//test.getAllEverythingForEveryone();

		HumansUser hu = new HumansUser();
		//HumanDAO humanDAO = new HumanDAO();
		HumansUserDAO dao = new HumansUserDAO();
		List<HumansUser> humans_users = dao.getAllHumansUsers();
		for(HumansUser user : humans_users) {
			
			// freshen to get an id for serviceUsers - should only have to do this one run
			//dao.save(user);
			
			List<Human> humans = user.getAllHumans();
			for(Human human : humans) {
				test.serviceRequestStatusForHuman(human);
			}
			test.makeHumansForUser(user);
		}
		//		
		//		HumansUser user = dao.findOneByUsername("fabien");
		//		test.makeHumansForUser(user);
	}


	@SuppressWarnings("unchecked")
	public void makeHumansForUser(HumansUser user) {
		TwitterService twitter;
		FoursquareService foursquare;
		InstagramService instagram;
		FlickrService flickr;
		List<Human> humans = user.getAllHumans();
		for(Human human : humans) {
			
			List<ServiceStatus> human_status = new ArrayList();
			List<ServiceUser> su = human.getServiceUsers();
			for(ServiceUser service_user : su) {
				//
				//
				//ServiceUserDAO dao = new ServiceUserDAO();
				
				
				//
				//
				String service = service_user.getService();
				if(service.equalsIgnoreCase("twitter")) {
					twitter = TwitterService.createTwitterServiceOnBehalfOfUsername(service_user.getOnBehalfOfUsername());
					List<TwitterStatus> s = twitter.getStatusForUserID(service_user.getServiceID());
					human_status.addAll(s);
				}
				if(service.equalsIgnoreCase("instagram")) {
					try {
						instagram = InstagramService.createServiceOnBehalfOfUsername(service_user.getOnBehalfOfUsername());
						List<InstagramStatus> s = instagram.getStatusForUserID(service_user.getServiceID());
						human_status.addAll(s);

					} catch (BadAccessTokenException e) {
						logger.error("", e);
						e.printStackTrace();
						continue;
					}
				}
				if(service.equalsIgnoreCase("flickr")) {
					try {
						flickr = FlickrService.createFlickrServiceOnBehalfOfUserID(service_user.getOnBehalfOfUserId());
						List<FlickrStatus> s = flickr.getStatusForUserID(service_user.getServiceID());
						human_status.addAll(s);
					} catch(BadAccessTokenException bad) {
						logger.error("",bad);
						bad.printStackTrace();
						continue;
					}
				}
				if(service.equalsIgnoreCase("foursquare")) {
					try {
						foursquare = FoursquareService.createFoursquareServiceOnBehalfOfUserID(service_user.getOnBehalfOfUserId());

						List<FoursquareCheckin> s = foursquare.getCheckinsForUserID(service_user.getServiceID());
						human_status.addAll(s);
					} catch (BadAccessTokenException bad) {
						// TODO Auto-generated catch block
						logger.error("",bad);
						bad.printStackTrace();	
					continue;	
					}
				}

			}
			Collections.sort(human_status);
			JSONArray a = new JSONArray();
			for(ServiceStatus s : human_status) {
				a.add(s.getStatusJSON());
			}
			JSONObject o = new JSONObject();
			o.put("status", a);
			o.put("name", human.getName());
			o.put("service_users", human.getServiceUsers());
			//writeJSON(user.getUsername()+"-"+human.getName()+"-humanstatus.json", "./", o);
		}
	}

/*	*//**
	 * Get status for a human (the aggregate) and save it for later
	 * @param aHuman
	 *//*
	public void serviceRequestStatusForHuman(Human aHuman) {
		TwitterService twitter;
		FoursquareService foursquare;
		InstagramService instagram;
		FlickrService flickr;
		List<ServiceStatus> human_status = new ArrayList();
		List<ServiceUser> su = aHuman.getServiceUsers();
		for(ServiceUser service_user : su) {
			String service = service_user.getService();
			if(service.equalsIgnoreCase("twitter")) {
				twitter = TwitterService.createTwitterServiceOnBehalfOfUsername(service_user.getOnBehalfOfUsername());
				if(false == twitter.localServiceStatusIsFreshFor(service_user.getServiceID())) {
					List<TwitterStatus> s = twitter.serviceRequestStatusForUserID(service_user.getServiceID());
					//List<TwitterStatus> s = twitter.getStatusForUserID(service_user.getServiceID());
					//human_status.addAll(s);
				}
			}
			if(service.equalsIgnoreCase("instagram")) {
				instagram = InstagramService.createInstagramServiceOnBehalfOfUsername(service_user.getOnBehalfOfUsername());
				if(false == instagram.localServiceStatusIsFreshForUserID(service_user.getServiceID())) {
					List<InstagramStatus> s = instagram.serviceRequestStatusForUserID(service_user.getServiceID());
					//human_status.addAll(s);
				}
			}
			if(service.equalsIgnoreCase("flickr")) {
				try {
					flickr = FlickrService.createFlickrServiceOnBehalfOfUserID(service_user.getOnBehalfOfUserId());
					if(false == flickr.localServiceStatusIsFreshForUserID(service_user.getServiceID())) {
						List<FlickrStatus> s = flickr.serviceRequestStatusForUserID(service_user.getServiceID());
						//human_status.addAll(s);
					}
				} catch(BadAccessTokenException bad) {
					logger.error("",bad);
					//logger.error(bad.getStackTrace());
					bad.printStackTrace();
				}
			}
			if(service.equalsIgnoreCase("foursquare")) {
				try {
					//TODO we can get *sometimes* the latest checkin for a user via their user info..
					// only bother if the service_user is ourself..foursquare does not allow you to
					// get checkins/status for someone of your friends..
					if(service_user.getOnBehalfOfUserId().equalsIgnoreCase(service_user.getServiceID())) {
						foursquare = FoursquareService.createFoursquareServiceOnBehalfOfUserID(service_user.getOnBehalfOfUserId());
						if(false == foursquare.localServiceStatusIsFreshForUserID(foursquare.getThisUser().getId())) {
							//TODO have service request return the checkins/status
							List<FoursquareCheckin> s = 
							foursquare.serviceRequestLatestCheckins();
						}
						//human_status.addAll(s);
					}
				} catch (BadAccessTokenException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}*/

/*
	public void getAllEverythingForEveryone() {

		HumansUserDAO dao = new HumansUserDAO();
		List<HumansUser> humans = dao.getAllHumansUsers();
		//List<HumansUser> humans = com.nearfuturelaboratory.humans.dao.HumansUserDAO.getAllHumansUsers();
		for(int i=0; i<humans.size(); i++) {
			com.nearfuturelaboratory.humans.entities.HumansUser user = humans.get(i);
			logger.debug("Operating on Humans User "+user);
			//			freshenFollowsForHumansUser(user);
			//			getTwitterStatusForHumansUser(user);
			//			getFoursquareCheckinsForHumansUser(user);
			//			getFlickrStatusForHumansUser(user);
			//			getInstagramStatusForHumansUser(user);

			getAllStatusForHumansUsersHumans(user);

			getHumansForHumansUser(user);
		}

		logger.debug("DONE after "+humans.size()+" humans.");
	}

*/	/**
	 * For a specific HumansUser (someone who has the app, an account, etc.)
	 * Get the status aggregates I call "Human" and plop them in the filesystem as a kind of "cache" of Human status
	 * The status will be date ordered and it might be a lot of it because now I think Instagram goes back 12 months
	 * Twitter I think makes two large requests of around 200 tweets each
	 * Etc.
	 * @param me
	 */
/*
	@SuppressWarnings("unchecked")
	public void getHumansForHumansUser(HumansUser me) {
		Gson gson = new Gson();
		int instagram_count, twitter_count, foursquare_count, flickr_count;
		instagram_count = 0;
		twitter_count = 0;
		foursquare_count = 0;
		flickr_count = 0;
		// get all Humans
		List<Human> myHumans = me.getAllHumans();
		Iterator<Human> iterHumans = myHumans.iterator();
		while(iterHumans.hasNext()) {
			// for each one get all the status on behalf of this HumansUser ("me")
			Human h = iterHumans.next();
			List<ServiceStatus> statuses = getAllStatusForHumansUsersByHuman(h);
			Iterator<ServiceStatus> iterStatus = statuses.iterator();
			logger.debug("========== Humane Status for "+h.getName());
			JSONObject humanJSON = new JSONObject();
			humanJSON.put("name", h.getName());
			//humanJSON.put("id", h.getHumanid());
			humanJSON.put("service_users", h.getServiceUsers());
			JSONArray status = new JSONArray();
			while(iterStatus.hasNext()) {
				ServiceStatus s = iterStatus.next();
				if (s instanceof InstagramStatus) {
					InstagramStatus i = (InstagramStatus)s;
					status.add(i.getStatusJSON());
					instagram_count++;
					//logger.debug(i.getImageURL_StandardResolution()+" "+i.getCreatedDate());
				} 
				if(s instanceof TwitterStatus) {
					TwitterStatus t = (TwitterStatus)s;
					status.add(t.getStatusJSON());
					twitter_count++;
					//logger.debug(t.getText()+" "+t.getCreatedDate());
				}
				if(s instanceof FoursquareCheckin) {
					FoursquareCheckin f = (FoursquareCheckin)s;
					status.add(f.getStatusJSON());
					foursquare_count++;
				}
				if(s instanceof FlickrStatus) {
					FlickrStatus fl = (FlickrStatus)s;
					status.add(fl.getStatusJSON());
					flickr_count++;
				}

			}

			humanJSON.put("status", status);
			logger.debug("HERE IT IS =================================");
			logger.debug("instagram="+instagram_count+" twitter="+twitter_count+" foursquare="+foursquare_count+" flickr="+flickr_count);
			//logger.debug(humanJSON.toJSONString());
			writeJSON(me.getUsername()+"-"+h.getName()+"-humanstatus.json", "./", humanJSON);
			//me.saveHumanStatus(humanJSON);
			// save the status for a humans id
		}

	}

	private void writeJSON(String aName, String aDir, JSONObject objToWrite) {
		try {
			File aFile = new File(aDir, aName);
			OutputStreamWriter char_output = new OutputStreamWriter(new FileOutputStream(aFile), Charset.forName("UTF-8").newEncoder());
			char_output.write(objToWrite.toJSONString());
			char_output.flush();
			char_output.close();
		} catch(IOException ioe) {
			ioe.printStackTrace();
			logger.error(ioe);
		}

	}

*//*	public void freshenFollowsForHumansUser(HumansUser aHumansUser) {
		// go through all the services and freshen follows..
		TwitterService twitter;
		FoursquareService foursquare;
		InstagramService instagram;
		FlickrService flickr;

		List<ServiceUser> twitterServiceUsers = aHumansUser.getServiceUsersForServiceName("twitter");
		if(twitterServiceUsers != null && twitterServiceUsers.size() > 0) {
			for(int i=0; i<twitterServiceUsers.size(); i++) {
				twitter = TwitterService.createTwitterServiceOnBehalfOfUsername(twitterServiceUsers.get(i).getOnBehalfOfUsername());
				twitter.serviceRequestFollows();
			}
		}

		List<ServiceUser>foursquareServiceUsers = aHumansUser.getServiceUsersForServiceName("foursquare");
		if(foursquareServiceUsers != null && foursquareServiceUsers.size()>0) {
			for(int i=0; i<foursquareServiceUsers.size(); i++) {
				try {
					foursquare = FoursquareService.createFoursquareServiceOnBehalfOfUserID(foursquareServiceUsers.get(i).getOnBehalfOfUserId());
					foursquare.serviceRequestFriends();
				} catch (BadAccessTokenException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.error("For Foursquare "+foursquareServiceUsers.get(i).getOnBehalfOf());
					logger.error(e);
				}
			}
		}

		List<ServiceUser>instagramServiceUsers = aHumansUser.getServiceUsersForServiceName("instagram");  //aHumansUser.getCodedServiceUsersForServiceName("instagram");
		if(instagramServiceUsers != null) {
			for(int i=0; i<instagramServiceUsers.size(); i++) {
				instagram = InstagramService.createInstagramServiceOnBehalfOfUsername(instagramServiceUsers.get(i).getOnBehalfOfUsername());
				instagram.serviceRequestFollows();
			}
		}
		List<ServiceUser>flickrServiceUsers = aHumansUser.getServiceUsersForServiceName("flickr");
		if(flickrServiceUsers != null) {
			for(int i=0; i<flickrServiceUsers.size(); i++) {
				try {
					flickr = FlickrService.createFlickrServiceOnBehalfOfUserID(flickrServiceUsers.get(i).getOnBehalfOfUserId());
					flickr.serviceRequestFriends();
				} catch(BadAccessTokenException bad) {
					logger.error(bad);
					bad.printStackTrace();
				}
			}
		}

	}

*/	
	
/*	
	public List<ServiceStatus> getAllStatusForHumansUsersByHuman(Human aHuman) {
		TwitterService twitter;
		InstagramService instagram;
		FoursquareService foursquare;
		FlickrService flickr;
		List<ServiceStatus> allStatus = new ArrayList<ServiceStatus>();

		List<ServiceUser> serviceUsers = aHuman.getServiceUsers();
		Iterator<ServiceUser> iter = serviceUsers.iterator();
		while(iter.hasNext()) {
			ServiceUser su = iter.next();
			if(su.getOnBehalfOfUsername() == null || su.getOnBehalfOfUserId() == null) {
				logger.warn("This ServiceUser is fucked "+su);
				continue;
			}

			if(su.getService().equalsIgnoreCase("twitter")) {
				logger.debug("Twitter: Operating on "+su.getUsername()+" on behalf of "+su.getOnBehalfOf());
				twitter = TwitterService.createTwitterServiceOnBehalfOfUsername(su.getOnBehalfOfUsername());
				if(twitter.localServiceStatusIsFreshFor(su.getServiceID()) == false) {
					twitter.serviceRequestUserBasicForUserID(su.getServiceID());
					twitter.serviceRequestStatusForUserID(su.getServiceID());
				}
				List<TwitterStatus> status = twitter.getStatusForUserID(su.getServiceID());
				// make sure there are no duplicates
				HashSet<TwitterStatus> ts = new HashSet<TwitterStatus>();
				ts.addAll(status);
				//status.clear();
				allStatus.addAll(ts);
			}
			if(su.getService().equalsIgnoreCase("instagram")) {
				logger.debug("Instagram: Operating on "+su.getUsername()+" on behalf of "+su.getOnBehalfOf());

				instagram = InstagramService.createInstagramServiceOnBehalfOfUsername(su.getOnBehalfOfUsername());
				if(instagram.localServiceStatusIsFreshForUserID(su.getServiceID()) == false) {
					instagram.serviceRequestUserBasicForUserID(su.getServiceID());
					instagram.serviceRequestStatusForUserID(su.getServiceID());
				}
				List<InstagramStatus> status = instagram.getStatusForUserID(su.getServiceID());
				// make sure there are no duplicates
				HashSet<InstagramStatus> is = new HashSet<InstagramStatus>();
				is.addAll(status);
				//status.clear();
				allStatus.addAll(is);
			}
			if(su.getService().equalsIgnoreCase("foursquare")) {
				logger.debug("Foursquare: Operating on "+su.getUsername()+" on behalf of "+su.getOnBehalfOf()+" id="+su.getOnBehalfOfUserId());

				try {
					foursquare = FoursquareService.createFoursquareServiceOnBehalfOfUserID(su.getOnBehalfOfUserId());
					List<FoursquareCheckin> status = foursquare.getCheckinsForUserID(su.getServiceID());
					// make sure there are no duplicates
					HashSet<FoursquareCheckin> fs = new HashSet<FoursquareCheckin>();
					fs.addAll(status);
					//status.clear();
					allStatus.addAll(fs);

				} catch (BadAccessTokenException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.error(su+" "+e.getMessage());
					logger.error(e.getStackTrace());
				}

				// foursquare can only get checkins for "self"..so we'll search for checkins that were gathered
				// during a normal "get latest" sorta query. if the user exists on the system, we can get the checkins
				// based on that. the assumption is that if this user has someone as a friend on foursquare, it's legit
				// to send the checkins in the humans feed....

				//if(foursquare.localServiceStatusIsFreshForUserID(su.getServiceID()) == false) {
				//instagram.serviceRequestUserBasicForUserID(su.getServiceID(), true);
				//foursquare.serviceRequestCheckins();
				//}

			}
			if(su.getService().equalsIgnoreCase("flickr")) {
				logger.debug("Flickr: Operating on "+su.getUsername()+" on behalf of "+su.getOnBehalfOf());

				try {
					flickr = FlickrService.createFlickrServiceOnBehalfOfUserID(su.getOnBehalfOfUserId());
					//TODO Every service should do this in case we have an invalid token
					if(flickr == null || flickr.isTokenValid() == false) {
						continue;
					}
					//if(flickr.localServiceStatusIsFreshForUserID(su.getServiceID())) {
					flickr.serviceRequestStatusForUserID(su.getServiceID());
					//}
					List<FlickrStatus> status = flickr.getStatusForUserID(su.getServiceID());
					// make sure there are no duplicates
					HashSet<FlickrStatus> fls = new HashSet<FlickrStatus>();
					fls.addAll(status);
					//status.clear();
					allStatus.addAll(fls);
				} catch(BadAccessTokenException bad) {
					logger.error(bad);
					bad.printStackTrace();
				}

			}

		}
		Collections.sort(allStatus);
		return allStatus;
	}
*/
/*	public void getAllStatusForHumansUsersHumans(HumansUser aHumansUser)
	{
		List<Human> allHumans = aHumansUser.getAllHumans();
		Iterator<Human> iter = allHumans.iterator();
		while(iter.hasNext()) {
			Human h = iter.next();
			//logger.debug("******** "+h+" ****************");
			this.getAllStatusForHumansUsersByHuman(h);

		}

	}

*/
	public void getFlickrStatusForHumansUser(HumansUser aHumansUser) {
		FlickrService flickr;
		List<ServiceUser> flickrServiceUsers = aHumansUser.getServiceUsersForServiceName("flickr");
		if(flickrServiceUsers != null && flickrServiceUsers.size() > 0) {
			for(int i=0; i<flickrServiceUsers.size(); i++) {
				try {
					flickr = FlickrService.createFlickrServiceOnBehalfOfUserID(flickrServiceUsers.get(i).getOnBehalfOfUserId());
					if(flickr == null) {
						return;
					}
					if(flickr.isTokenValid()) {
						logger.debug("getting flickr status for humans user "+aHumansUser.getUsername());
						//if(flickr.localServiceStatusIsFreshForUserID("self") == false) {
						flickr.serviceRequestStatusForUserID("self");
						//}
					}
				} catch(BadAccessTokenException bad) {
					logger.error(bad);
					bad.printStackTrace();
				}

			}
			//twitter.getStatusOldestID();
		}


	}


	public void getTwitterStatusForHumansUser(HumansUser aHumansUser)
	{
		TwitterService twitter;

		List<ServiceUser> twitterServiceUsers = aHumansUser.getServiceUsersForServiceName("twitter");
		if(twitterServiceUsers != null && twitterServiceUsers.size() > 0) {
			for(int i=0; i<twitterServiceUsers.size(); i++) {
				twitter = TwitterService.createTwitterServiceOnBehalfOfUsername(twitterServiceUsers.get(i).getOnBehalfOfUsername());
				logger.debug("getting twitter status for humans user "+aHumansUser.getUsername());
				if(twitter.localServiceStatusIsFreshFor("self") == false) {
					twitter.serviceRequestStatusForUserID("self");
				}

				//twitter.getStatusOldestID();
			}
		}
		logger.debug("Now getting for this Humans User's humans.."+aHumansUser.getUsername());
		// now get for this Humans User's humans
		List<ServiceUser> allServiceUsersForTwitter = aHumansUser.getServiceUsersForAllHumansByService("twitter");

		Iterator<ServiceUser> iter = allServiceUsersForTwitter.iterator();

		while(iter.hasNext()) {

			ServiceUser su = iter.next();
			String serviceUserTwitterUsername = su.getUsername();
			String serviceUserTwitterUserID = su.getServiceID();


			twitter = TwitterService.createTwitterServiceOnBehalfOfUsername(su.getOnBehalfOfUsername());
			logger.debug("operating on "+serviceUserTwitterUsername+" on behalf of.. "+su.getOnBehalfOf());

			if(twitter.localServiceStatusIsFreshFor(serviceUserTwitterUserID) == false) {
				if(twitter.localUserBasicIsFreshForUserID(serviceUserTwitterUserID) == false) {
					twitter.serviceRequestUserBasicForUserID(serviceUserTwitterUserID);
				}
				twitter.serviceRequestStatusForUserID(serviceUserTwitterUserID);
			}

		}
	}
/*
	public void getInstagramStatusForHumansUser(com.nearfuturelaboratory.humans.entities.HumansUser aHumansUser) {
		InstagramService instagram;

		List<com.nearfuturelaboratory.humans.entities.ServiceUser> instagramServiceUsers = aHumansUser.getServiceUsersForServiceName("instagram");
		for(int i=0; i<instagramServiceUsers.size(); i++) {

			com.nearfuturelaboratory.humans.entities.ServiceUser instagramServiceUser = instagramServiceUsers.get(i);
			logger.debug("Instagram Service User "+instagramServiceUser);
			instagram = InstagramService.createInstagramServiceOnBehalfOfUsername(instagramServiceUser.getOnBehalfOfUsername());
			// freshen basic shit

			if(false == instagram.localUserBasicIsFreshForUserID(instagramServiceUser.getServiceID())) {
				instagram.serviceRequestUserBasicForUserID(instagramServiceUser.getServiceID());
			}

			if(false == instagram.localUserBasicIsFresh()) {
				instagram.serviceRequestUserBasic();
			}
			// freshen follows
			//instagram.getFollows();
			// get recent status

			instagram.serviceRequestStatusForUserID(instagramServiceUser.getServiceID());
			//instagram.serviceRequestStatusForUserIDFromMonthsAgo(instagramServiceUser.getFirst(), 12);
			//instagram.serviceRequestStatusForUserIDFromMonthsAgo(instagramServiceUser.getServiceID(), 6);

		}

		List<com.nearfuturelaboratory.humans.entities.ServiceUser>allServiceUsersForInstagram = aHumansUser.getServiceUsersForAllHumansByService("instagram");
		Iterator<com.nearfuturelaboratory.humans.entities.ServiceUser> iter = allServiceUsersForInstagram.iterator();

		while(iter.hasNext()) {
			com.nearfuturelaboratory.humans.entities.ServiceUser su = iter.next();
			String serviceUserInstagramUsername = su.getUsername();
			String serviceUserInstagramID = su.getServiceID();

			instagram = InstagramService.createInstagramServiceOnBehalfOfUsername(su.getOnBehalfOfUsername());
			logger.debug("operating on "+serviceUserInstagramUsername+" on behalf of.. "+su.getOnBehalfOfUsername());

			////////////////////instagram.serviceRequestUserBasicForUserID(serviceUserInstagramID, true);
			instagram.serviceRequestStatusForUserID(serviceUserInstagramID);
		}
	}

	public void getFoursquareCheckinsForHumansUser(HumansUser aHumansUser) {
		FoursquareService foursquare;

		@SuppressWarnings("rawtypes")
		List<ServiceUser> foursquareServiceUsers = aHumansUser.getServiceUsersForServiceName("foursquare");
		for(int i=0; i<foursquareServiceUsers.size(); i++) {
			ServiceUser foursquareServiceUser = foursquareServiceUsers.get(i);
			logger.debug("Foursquare Service User "+foursquareServiceUser);
			try {
				foursquare = FoursquareService.createFoursquareServiceOnBehalfOfUserID(foursquareServiceUser.getOnBehalfOfUserId());
				foursquare.serviceRequestCheckins();

			} catch (BadAccessTokenException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error(foursquareServiceUser.getOnBehalfOf()+" "+e.getMessage());
				logger.error(e.getStackTrace());
			}
			logger.warn("Should be checking the freshness of Foursquare Checkins here..");
		}
	}


*/	//	public void assembleHumans() {
	//		@SuppressWarnings("unused")
	//		InstagramService instagram;// = InstagramService.createInstagramServiceOnBehalfOfUsername("darthjulian");
	//		TwitterService twitter;
	//		FlickrService flickr;
	//		FoursquareService foursquare;
	//		//twitter = TwitterService.createTwitterServiceOnBehalfOfCodedUsername();
	//
	//		Gson gson = new Gson();
	//
	//
	//		HumansUser humansUser = new HumansUser("darthjulian", "darthjulian");
	//		List<ServiceUser> serviceUsers = humansUser.getServiceUsersForAllHumans();
	//		Iterator<ServiceUser> serviceUsersIter = serviceUsers.iterator();
	//		while(serviceUsersIter.hasNext()) {
	//			ServiceUser serviceUser = serviceUsersIter.next();
	//			logger.debug(serviceUser);
	//			if(serviceUser.getService().equalsIgnoreCase("instagram")) {
	//				instagram = InstagramService.createInstagramServiceOnBehalfOfUsername(serviceUser.getOnBehalfOfUsername());
	//				instagram.serviceRequestStatusForUserID(serviceUser.getServiceID());
	//			}
	//			if(serviceUser.getService().equalsIgnoreCase("twitter")) {
	//				twitter = TwitterService.createTwitterServiceOnBehalfOfUsername(serviceUser.getOnBehalfOfUsername());
	//				twitter.serviceRequestStatusForUserID(serviceUser.getServiceID());
	//			}
	//			if(serviceUser.getService().equalsIgnoreCase("flickr")) {
	//				flickr = FlickrService.createFlickrServiceOnBehalfOfUserID(serviceUser.getOnBehalfOfUserId());
	//				flickr.serviceRequestUserBasicByUserID(serviceUser.getServiceID());
	//			}
	//			if(serviceUser.getService().equalsIgnoreCase("foursquare")) {
	//				foursquare = FoursquareService.createFoursquareServiceOnBehalfOfUserID(serviceUser.getOnBehalfOfUserId());
	//				foursquare.serviceRequestUserBasicForUserID(serviceUser.getServiceID());
	//			}
	//		}
	//	}
	//
}
