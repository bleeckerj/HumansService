package com.nearfuturelaboratory.humans.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
//import com.nearfuturelaboratory.humans.core.HumansUser;
//import com.nearfuturelaboratory.humans.core.Human;
//import com.nearfuturelaboratory.humans.core.ServiceUser;

import com.nearfuturelaboratory.humans.entities.HumansUser;
import com.nearfuturelaboratory.humans.entities.Human;
import com.nearfuturelaboratory.humans.entities.ServiceUser;



import com.nearfuturelaboratory.humans.dao.HumansUserDAO;
import com.nearfuturelaboratory.humans.service.FlickrService;
import com.nearfuturelaboratory.humans.service.FoursquareService;
import com.nearfuturelaboratory.humans.service.InstagramService;
import com.nearfuturelaboratory.humans.service.TwitterService;
import com.nearfuturelaboratory.humans.service.status.*;
import com.nearfuturelaboratory.util.Constants;
import com.nearfuturelaboratory.util.Pair;

public class TestHumanGson {
	final static Logger logger = Logger.getLogger("com.nearfuturelaboratory.humans.test.Test");

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

		test.getAllEverythingForEveryone();

		/*		HumansUser me = new HumansUser("nicolas", "nicolas");
//		test.getAllStatusForHumansUsersHumans(me);


		List<Human> myHumans = me.getAllHumans();
		Iterator<Human> iterHumans = myHumans.iterator();
		while(iterHumans.hasNext()) {
			Human h = iterHumans.next();
			List<ServiceStatus> statuses = test.getAllStatusForHumansUsersByHuman(h);
			Iterator<ServiceStatus> iterStatus = statuses.iterator();
			logger.debug("========== Humane Status for "+h.getName());
			JSONObject humanJSON = new JSONObject();
			humanJSON.put("name", h.getName());
			humanJSON.put("id", h.getID());
			humanJSON.put("service_users", h.getServiceUsers());
			JSONArray status = new JSONArray();
			while(iterStatus.hasNext()) {
				ServiceStatus s = iterStatus.next();
				if (s instanceof InstagramStatus) {
					InstagramStatus i = (InstagramStatus)s;
					status.add(i.getStatusJSON());
					//logger.debug(i.getImageURL_StandardResolution()+" "+i.getCreatedDate());
				} 
				if(s instanceof TwitterStatus) {
					TwitterStatus t = (TwitterStatus)s;
					status.add(t.getStatusJSON());
					//logger.debug(t.getText()+" "+t.getCreatedDate());
				}

			}

			humanJSON.put("status", status);
			me.saveHumanStatus(humanJSON);
			FileWriter file = new FileWriter("/tmp/status.json");
			file.write(humanJSON.toJSONString());
			file.flush();
			file.close();
		}


				Gson gson = new GsonBuilder().setDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy").create();
		String aDate = "Tue Oct 08 23:47:26 +0000 2013";
		SimpleDateFormat f = new SimpleDateFormat();
		f.applyPattern("EEE MMM dd HH:mm:ss ZZZZZ yyyy");
		Date date = 		f.parse(aDate);

		logger.debug(date);
		 */

		/*		InstagramService instagram = InstagramService.createInstagramServiceOnBehalfOfUsername("darthjulian");
		TwitterService twitter = TwitterService.createTwitterServiceOnBehalfOfCodedUsername("185383-darthjulian");
		List<InstagramStatus> instagramStatus = instagram.getStatus();
		List<TwitterStatus> twitterStatus = twitter.getStatus();

		//List<InstagramStatus>tmpStatusList = instagramStatus;

		// scrub it for duplicates
		HashSet<InstagramStatus> hs = new HashSet<InstagramStatus>();
		hs.addAll(instagramStatus);
		instagramStatus.clear();
		instagramStatus.addAll(hs);

		HashSet<TwitterStatus> ts = new HashSet<TwitterStatus>();
		ts.addAll(twitterStatus);
		twitterStatus.clear();
		twitterStatus.addAll(ts);

		HashSet<ServiceStatus> all = new HashSet<ServiceStatus>();
		all.addAll(instagramStatus);
		all.addAll(twitterStatus);

		List<ServiceStatus> allStatus = new ArrayList<ServiceStatus>();
		allStatus.addAll(all);



		// sort it
		Collections.sort(allStatus);
		// load a bunch of status




		Iterator<ServiceStatus> iter = allStatus.iterator();
		while(iter.hasNext()) {
			ServiceStatus s = iter.next();

			//logger.debug(s);
			if (s instanceof InstagramStatus) {
				InstagramStatus i = (InstagramStatus)s;
				logger.debug(i.getImageURL_StandardResolution()+" "+i.getCreatedDate());
			} 
			if(s instanceof TwitterStatus) {
				TwitterStatus t = (TwitterStatus)s;
				logger.debug(t.getText()+" "+t.getCreatedDate());
			}
		}


		 */
		// convert it to InstagramStatus objects
		//test.getMyStatus();

		//test.getCheckins();



	}

	public void getAllEverythingForEveryone() {

		HumansUserDAO dao = new HumansUserDAO();
		List<com.nearfuturelaboratory.humans.entities.HumansUser> humans = dao.getAllHumansUsers();
		//List<HumansUser> humans = com.nearfuturelaboratory.humans.dao.HumansUserDAO.getAllHumansUsers();
		for(int i=0; i<humans.size(); i++) {
			com.nearfuturelaboratory.humans.entities.HumansUser user = humans.get(i);
			logger.debug("Operating on Humans User "+user);
			//			if(user.getUsername().equalsIgnoreCase("grignani") == false) {
			//				continue;
			//			} else {
			//			freshenFollowsForHumansUser(user);
			//			getTwitterStatusForHumansUser(user);
			//			getFoursquareCheckinsForHumansUser(user);
			//			getFlickrStatusForHumansUser(user);
			getInstagramStatusForHumansUser(user);

			//			getAllStatusForHumansUsersHumans(user);

			//			getHumansForHumansUser(user);
		}
		//		}
		logger.debug("DONE after "+humans.size()+" humans.");
	}

	/**
	 * For a specific HumansUser (someone who has the app, an account, etc.)
	 * Get the status aggregates I call "Human" and plop them in the filesystem as a kind of "cache" of Human status
	 * The status will be date ordered and it might be a lot of it because now I think Instagram goes back 12 months
	 * Twitter I think makes two large requests of around 200 tweets each
	 * Etc.
	 * @param me
	 */

	@SuppressWarnings("unchecked")
	public void getHumansForHumansUser(HumansUser me) {
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
					//logger.debug(i.getImageURL_StandardResolution()+" "+i.getCreatedDate());
				} 
				if(s instanceof TwitterStatus) {
					TwitterStatus t = (TwitterStatus)s;
					status.add(t.getStatusJSON());
					//logger.debug(t.getText()+" "+t.getCreatedDate());
				}
				if(s instanceof FoursquareStatus) {
					FoursquareStatus f = (FoursquareStatus)s;
					status.add(f.getStatusJSON());
				}
				if(s instanceof FlickrStatus) {
					FlickrStatus fl = (FlickrStatus)s;
					status.add(fl.getStatusJSON());
				}

			}

			humanJSON.put("status", status);
			me.saveHumanStatus(humanJSON);
		}

	}

	public void freshenFollowsForHumansUser(HumansUser aHumansUser) {
		// go through all the services and freshen follows..
		TwitterService twitter;
		FoursquareService foursquare;
		InstagramService instagram;
		FlickrService flickr;

		List<String> twitterServiceUsers = aHumansUser.getCodedServiceUsersForServiceName("twitter");
		if(twitterServiceUsers != null && twitterServiceUsers.size() > 0) {
			for(int i=0; i<twitterServiceUsers.size(); i++) {
				twitter = TwitterService.createTwitterServiceOnBehalfOfCodedUsername(twitterServiceUsers.get(i));
				twitter.getFollows();
			}
		}

		List<String>foursquareServiceUsers = aHumansUser.getCodedServiceUsersForServiceName("foursquare");
		if(foursquareServiceUsers != null && foursquareServiceUsers.size()>0) {
			foursquare = FoursquareService.createFoursquareServiceOnBehalfOfCodedUsername(foursquareServiceUsers.get(0));
			foursquare.getFollows();
		}

		List<String>instagramServiceUsers = aHumansUser.getServiceUsersForServiceName("instagram");  //aHumansUser.getCodedServiceUsersForServiceName("instagram");
		if(instagramServiceUsers != null) {
			for(int i=0; i<instagramServiceUsers.size(); i++) {
				instagram = InstagramService.createInstagramServiceOnBehalfOfCodedUsername(instagramServiceUsers.get(i));
				instagram.getFollows();
			}
		}
		List<String>flickrServiceUsers = aHumansUser.getCodedServiceUsersForServiceName("flickr");
		if(flickrServiceUsers != null) {
			for(int i=0; i<flickrServiceUsers.size(); i++) {
				flickr = FlickrService.createFlickrServiceOnBehalfOfCodedUsername(flickrServiceUsers.get(i));
				flickr.getFollows();
			}
		}

	}

	public List<ServiceStatus> getAllStatusForHumansUsersByHuman(Human aHuman) {
		TwitterService twitter;
		InstagramService instagram;
		FoursquareService foursquare;
		FlickrService flickr;
		List<ServiceStatus> allStatus = new ArrayList<ServiceStatus>();

		Set<ServiceUser> serviceUsers = aHuman.getServiceUsers();
		Iterator<ServiceUser> iter = serviceUsers.iterator();
		while(iter.hasNext()) {
			ServiceUser su = iter.next();
			if(su.getService().equalsIgnoreCase("twitter")) {
				logger.debug("Twitter: Operating on "+su.getUsername()+" on behalf of "+su.getOnBehalfOf());

				twitter = TwitterService.createTwitterServiceOnBehalfOfCodedUsername(su.getOnBehalfOf());
				if(twitter.localServiceStatusIsFreshForUserID(su.getServiceID()) == false) {
					twitter.serviceRequestUserBasicForUserID(su.getServiceID(), true);
					twitter.serviceRequestStatusForUserID(su.getServiceID());
				}
				List<TwitterStatus> status = twitter.getStatus(su.getServiceID());
				// make sure there are no duplicates
				HashSet<TwitterStatus> ts = new HashSet<TwitterStatus>();
				ts.addAll(status);
				//status.clear();
				allStatus.addAll(ts);
			}
			if(su.getService().equalsIgnoreCase("instagram")) {
				logger.debug("Instagram: Operating on "+su.getUsername()+" on behalf of "+su.getOnBehalfOf());

				instagram = InstagramService.createInstagramServiceOnBehalfOfCodedUsername(su.getOnBehalfOf());
				if(instagram.localServiceStatusIsFreshForUserID(su.getServiceID()) == false) {
					instagram.serviceRequestUserBasicForUserID(su.getServiceID());
					instagram.serviceRequestStatusForUserID(su.getServiceID());
				}
				List<InstagramStatus> status = instagram.getStatus(su.getServiceID());
				// make sure there are no duplicates
				HashSet<InstagramStatus> is = new HashSet<InstagramStatus>();
				is.addAll(status);
				//status.clear();
				allStatus.addAll(is);
			}
			if(su.getService().equalsIgnoreCase("foursquare")) {
				logger.debug("Foursquare: Operating on "+su.getUsername()+" on behalf of "+su.getOnBehalfOf());

				foursquare = FoursquareService.createFoursquareServiceOnBehalfOfCodedUsername(su.getOnBehalfOf());
				// foursquare can only get checkins for "self"..so we'll search for checkins that were gathered
				// during a normal "get latest" sorta query. if the user exists on the system, we can get the checkins
				// based on that. the assumption is that if this user has someone as a friend on foursquare, it's legit
				// to send the checkins in the humans feed....

				//if(foursquare.localServiceStatusIsFreshForUserID(su.getServiceID()) == false) {
				//instagram.serviceRequestUserBasicForUserID(su.getServiceID(), true);
				//foursquare.serviceRequestCheckins();
				//}
				List<FoursquareStatus> status = foursquare.getCheckinsForUserID(su.getServiceID());
				// make sure there are no duplicates
				HashSet<FoursquareStatus> fs = new HashSet<FoursquareStatus>();
				fs.addAll(status);
				//status.clear();
				allStatus.addAll(fs);
			}
			if(su.getService().equalsIgnoreCase("flickr")) {
				logger.debug("Flickr: Operating on "+su.getUsername()+" on behalf of "+su.getOnBehalfOf());

				flickr = FlickrService.createFlickrServiceOnBehalfOfCodedUsername(su.getOnBehalfOf());
				//if(flickr.localServiceStatusIsFreshForUserID(su.getServiceID())) {
				flickr.serviceRequestStatusForUserID(su.getServiceID());
				//}
				List<FlickrStatus> status = flickr.getStatusForUserID(su.getServiceID());
				// make sure there are no duplicates
				HashSet<FlickrStatus> fls = new HashSet<FlickrStatus>();
				fls.addAll(status);
				//status.clear();
				allStatus.addAll(fls);
			}

		}
		Collections.sort(allStatus);
		return allStatus;
	}

	public void getAllStatusForHumansUsersHumans(HumansUser aHumansUser)
	{
		List<Human> allHumans = aHumansUser.getAllHumans();
		Iterator<Human> iter = allHumans.iterator();
		while(iter.hasNext()) {
			Human h = iter.next();
			logger.debug("******** "+h+" ****************");
			this.getAllStatusForHumansUsersByHuman(h);
		}

	}


	public void getFlickrStatusForHumansUser(HumansUser aHumansUser) {
		FlickrService flickr;
		List<String> flickrServiceUsers = aHumansUser.getCodedServiceUsersForServiceName("flickr");
		if(flickrServiceUsers != null && flickrServiceUsers.size() > 0) {
			flickr = FlickrService.createFlickrServiceOnBehalfOfCodedUsername(flickrServiceUsers.get(0));
			if(flickr.isTokenValid()) {
				logger.debug("getting flickr status for humans user "+aHumansUser.getUsername());
				//if(flickr.localServiceStatusIsFreshForUserID("self") == false) {
				flickr.serviceRequestStatusForUserID("self");
				//}
			}
			//twitter.getStatusOldestID();
		}


	}


	public void getTwitterStatusForHumansUser(HumansUser aHumansUser)
	{
		TwitterService twitter;

		List<String> twitterServiceUsers = aHumansUser.getCodedServiceUsersForServiceName("twitter");
		if(twitterServiceUsers != null && twitterServiceUsers.size() > 0) {
			twitter = TwitterService.createTwitterServiceOnBehalfOfCodedUsername(twitterServiceUsers.get(0));
			logger.debug("getting twitter status for humans user "+aHumansUser.getUsername());
			if(twitter.localServiceStatusIsFreshForUserID("self") == false) {
				twitter.serviceRequestStatusForUserID("self");
			}

			//twitter.getStatusOldestID();
		}
		logger.debug("Now getting for this Humans User's humans.."+aHumansUser.getUsername());
		// now get for this Humans User's humans
		List<ServiceUser> allServiceUsersForTwitter = aHumansUser.getServiceUsersForAllHumansByService("twitter");

		Iterator<ServiceUser> iter = allServiceUsersForTwitter.iterator();

		while(iter.hasNext()) {

			ServiceUser su = iter.next();
			String serviceUserTwitterUsername = su.getUsername();
			String serviceUserTwitterUserID = su.getServiceID();


			twitter = TwitterService.createTwitterServiceOnBehalfOfCodedUsername(su.getOnBehalfOf());
			logger.debug("operating on "+serviceUserTwitterUsername+" on behalf of.. "+su.getOnBehalfOf());

			if(twitter.localServiceStatusIsFreshForUserID(serviceUserTwitterUserID) == false) {
				if(twitter.localUserBasicIsFreshForUserID(serviceUserTwitterUserID) == false) {
					twitter.serviceRequestUserBasicForUserID(serviceUserTwitterUserID, true);
				}
				twitter.serviceRequestStatusForUserID(serviceUserTwitterUserID);
			}

		}
	}

	public void getInstagramStatusForHumansUser(com.nearfuturelaboratory.humans.entities.HumansUser aHumansUser) {
		InstagramService instagram;

		//List<Pair<String,String>> instagramServiceUsers = aHumansUser.getServiceUsersForServiceName("instagram");
		List<com.nearfuturelaboratory.humans.entities.ServiceUser> instagramServiceUsers = aHumansUser.getServiceUsersForServiceName("instagram");
		for(int i=0; i<instagramServiceUsers.size(); i++) {
			//Pair<String,String> instagramServiceUser = instagramServiceUsers.get(i);
			com.nearfuturelaboratory.humans.entities.ServiceUser instagramServiceUser = instagramServiceUsers.get(i);
			logger.debug("Instagram Service User "+instagramServiceUser);
			//String codedUsername = instagramServiceUser.getFirst()+"-"+instagramServiceUser.getSecond();
			//instagram = InstagramService.createInstagramServiceOnBehalfOfCodedUsername(codedUsername);
			instagram = InstagramService.createInstagramServiceOnBehalfOfUsername(instagramServiceUser.getOnBehalfOfUsername());
			// freshen basic shit
			//TODO This is way over kill to do all the time..
			if(false == instagram.localUserBasicIsFreshForUserID(instagramServiceUser.getServiceID())) {
				instagram.serviceRequestUserBasicForUserID(instagramServiceUser.getServiceID());
			}

			if(false == instagram.localUserBasicIsFreshForSelf()) {
				instagram.serviceRequestUserBasic();
			}
			// freshen follows
			//instagram.getFollows();
			// get recent status
			//logger.debug("For "+instagramServiceUser.getSecond()+" "+instagram.getMostRecentLocalStatusID());
			instagram.serviceRequestStatusForUserID(instagramServiceUser.getServiceID());
			//instagram.serviceRequestStatusForUserIDFromMonthsAgo(instagramServiceUser.getFirst(), 12);
			instagram.serviceRequestStatusForUserIDFromMonthsAgo(instagramServiceUser.getServiceID(), 6);

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
		List<Pair<String,String>> foursquareServiceUsers = aHumansUser.getServiceUsersForServiceName("foursquare");
		for(int i=0; i<foursquareServiceUsers.size(); i++) {
			Pair<String,String> foursquareServiceUser = foursquareServiceUsers.get(i);
			logger.debug("Foursquare Service User "+foursquareServiceUser);
			String codedUsername = foursquareServiceUser.getFirst()+"-"+foursquareServiceUser.getSecond();
			foursquare = FoursquareService.createFoursquareServiceOnBehalfOfCodedUsername(codedUsername);
			logger.warn("Should be checking the freshness of Foursquare Checkins here..");
			foursquare.serviceRequestCheckins();
		}
	}


	public void assembleHumans() {
		@SuppressWarnings("unused")
		InstagramService instagram;// = InstagramService.createInstagramServiceOnBehalfOfUsername("darthjulian");
		TwitterService twitter;
		FlickrService flickr;
		FoursquareService foursquare;
		//twitter = TwitterService.createTwitterServiceOnBehalfOfCodedUsername();

		Gson gson = new Gson();


		HumansUser humansUser = new HumansUser("darthjulian", "darthjulian");
		//humansUser.loadByUsername("darthjulian");
		List<ServiceUser> serviceUsers = humansUser.getServiceUsersForAllHumans();
		Iterator<ServiceUser> serviceUsersIter = serviceUsers.iterator();
		while(serviceUsersIter.hasNext()) {
			ServiceUser serviceUser = serviceUsersIter.next();
			logger.debug(serviceUser);
			if(serviceUser.getService().equalsIgnoreCase("instagram")) {
				instagram = InstagramService.createInstagramServiceOnBehalfOfUsername(serviceUser.getOnBehalfOfUsername());
				instagram.serviceRequestStatusForUserID(serviceUser.getServiceID());
			}
			if(serviceUser.getService().equalsIgnoreCase("twitter")) {
				twitter = TwitterService.createTwitterServiceOnBehalfOfCodedUsername(serviceUser.getOnBehalfOf());
				twitter.serviceRequestStatusForUserID(serviceUser.getServiceID());
			}
			if(serviceUser.getService().equalsIgnoreCase("flickr")) {
				flickr = FlickrService.createFlickrServiceOnBehalfOfCodedUsername(serviceUser.getOnBehalfOf());
				flickr.serviceRequestUserBasicByUserID(serviceUser.getServiceID());
			}
			if(serviceUser.getService().equalsIgnoreCase("foursquare")) {
				foursquare = FoursquareService.createFoursquareServiceOnBehalfOfCodedUsername(serviceUser.getOnBehalfOf());
				foursquare.serviceRequestUserBasicByUserID(serviceUser.getServiceID());
			}
		}
		/*

		Human human = new Human();
		human.setName("Julian");
		human.setID("101010101");
		human.addServiceUser("frank", "10", "twitter", "1818181-darthjulian");
		human.addServiceUser("skippy", "56262611", "instagram", "11918181-darthjulian");

		humansUser.addHuman(human);

		human = new Human();
		human.setName("Sparky");
		human.addServiceUser("strumpy", "10", "twitter", "1818181-darthjulian");
		human.addServiceUser("martha", "17849300", "instagram", "11918181-darthjulian");

		humansUser.addHuman(human);

		System.out.println(humansUser.toString());
		 */


		/*		String json = gson.toJson(human);
		System.out.println(json);
		Object obj = JSONValue.parse(json);
		System.out.println(obj);
		Human frankenstein = gson.fromJson(json, Human.class);

		System.out.println(frankenstein);
		 */
	}

}
