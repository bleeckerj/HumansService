package com.nearfuturelaboratory.humans.scheduler;

import com.nearfuturelaboratory.humans.exception.BadAccessTokenException;
import com.nearfuturelaboratory.humans.service.InstagramService;
//import com.nearfuturelaboratory.humans.service.TwitterService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by julian on 5/27/14.
 */
public class ScheduledTwitterStatusFetcher implements Job {
    final static Logger logger = LogManager.getLogger(ScheduledTwitterStatusFetcher.class);


    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("Fetch Instagram Status for " + context);

//        String twitterUsername = (String) context.getMergedJobDataMap().get("onBehalfOfTwitterUsername");
//        String tweetID = (String) context.getMergedJobDataMap().get("tweet_id");
//        fetchStatus(twitterUsername, tweetID);
//        try {
//            InstagramService instagram = InstagramService.createServiceOnBehalfOfUsername(instagramUsername);
//            instagram.serviceRequestStatusByMediaID(mediaID);
//
//        } catch (BadAccessTokenException bate) {
//            logger.warn(bate);
//
//        }


    }

//    protected void fetchStatus(String aOnBehalfOf, String aTweetID) {
//       // String instagramUsername = (String) context.getMergedJobDataMap().get("onBehalfOfInstagramUsername");
//        //String mediaID = (String) context.getMergedJobDataMap().get("mediaid");
//        try {
//            TwitterService twitter = TwitterService.createServiceOnBehalfOfUsername(aOnBehalfOf);
//            twitter.serviceRequestStatusByTweetID(aTweetID);
//
//        } catch (BadAccessTokenException bate) {
//            logger.warn(bate);
//
//        }
//
//    }
}