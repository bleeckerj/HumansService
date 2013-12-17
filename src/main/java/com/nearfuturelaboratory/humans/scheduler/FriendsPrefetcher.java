package com.nearfuturelaboratory.humans.scheduler;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.nearfuturelaboratory.humans.entities.*;
import com.nearfuturelaboratory.humans.exception.BadAccessTokenException;
import com.nearfuturelaboratory.humans.flickr.entities.FlickrFriend;
import com.nearfuturelaboratory.humans.foursquare.entities.FoursquareFriend;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramFriend;
import com.nearfuturelaboratory.humans.service.FlickrService;
import com.nearfuturelaboratory.humans.service.FoursquareService;
import com.nearfuturelaboratory.humans.service.InstagramService;
import com.nearfuturelaboratory.humans.service.TwitterService;
import com.nearfuturelaboratory.humans.twitter.entities.TwitterFriend;
import com.nearfuturelaboratory.humans.core.MinimalSocialServiceUser;
import com.nearfuturelaboratory.humans.dao.*;
import com.nearfuturelaboratory.util.Constants;

public class FriendsPrefetcher {
	final static Logger logger = Logger.getLogger(com.nearfuturelaboratory.humans.scheduler.FriendsPrefetcher.class);

	public FriendsPrefetcher() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		try {
			Constants.load("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/src/main/webapp/WEB-INF/lib/dev.app.properties");
			PropertyConfigurator.configureAndWatch("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/src/main/webapp/WEB-INF/lib/static-logger.properties");
			FriendsPrefetcher prefetcher = new FriendsPrefetcher();
			List<HumansUser> users = HumansUser.getAllHumansUsers();
			logger.debug("Hey Ho!");

			for(HumansUser user : users) {
				prefetcher.fetchFriendsForHumansUser(user);
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}

	}
	
	
	protected void fetchFriendsForHumansUser(HumansUser humans_user) {
			List<MinimalSocialServiceUser> friends = new ArrayList<MinimalSocialServiceUser>();
			for(ServiceEntry service_entry : humans_user.getServices()) {
				logger.info(service_entry);
				if(service_entry.getServiceName().equalsIgnoreCase("flickr")) {
					FlickrService flickr;
					try {
						flickr = FlickrService.createFlickrServiceOnBehalfOfUserID(service_entry.getServiceUserID());
						logger.info(flickr.getThisUser().getUsername()+" service request friends for flickr");
						flickr.serviceRequestFriends();
					} catch (BadAccessTokenException e) {
						logger.warn(e);
					}
				}
				if(service_entry.getServiceName().equalsIgnoreCase("instagram")) {
					InstagramService instagram = new InstagramService();
					try {
						instagram = InstagramService.createServiceOnBehalfOfUsername(service_entry.getServiceUsername());
						logger.info(instagram.getThisUser().getUsername()+" service request friends for instagram");

						instagram.serviceRequestFriends();
					} catch (BadAccessTokenException e) {
						logger.warn(e);
					}
				}
				if(service_entry.getServiceName().equalsIgnoreCase("twitter")) {
					TwitterService twitter = TwitterService.createTwitterServiceOnBehalfOfUsername(service_entry.getServiceUsername());
					logger.info(twitter.getThisUser().getUsername()+" service request friends for twitter");

					twitter.serviceRequestFollows();
				}
				if(service_entry.getServiceName().equalsIgnoreCase("foursquare")) {
					try {
						FoursquareService foursquare = FoursquareService.createFoursquareServiceOnBehalfOfUserID(service_entry.getServiceUserID());
						logger.info(foursquare.getThisUser().getUsername()+" service request friends for foursquare");

						foursquare.serviceRequestFriends();
					} catch (BadAccessTokenException e) {
						logger.warn(e);
					}
				}
			}
			//return friends;
	}	
	

}
