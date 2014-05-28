package com.nearfuturelaboratory.humans.scheduler;

import com.nearfuturelaboratory.humans.exception.BadAccessTokenException;
import com.nearfuturelaboratory.humans.service.InstagramService;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by julian on 5/27/14.
 */
public class ScheduledInstagramStatusFetcher implements Job {
    final static Logger logger = LogManager.getLogger(ScheduledInstagramStatusFetcher.class);


    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("Fetch Instagram Status for " + context);

        String instagramUsername = (String) context.getMergedJobDataMap().get("onBehalfOfInstagramUsername");
        String mediaID = (String) context.getMergedJobDataMap().get("mediaid");
        fetchStatus(instagramUsername, mediaID);
//        try {
//            InstagramService instagram = InstagramService.createServiceOnBehalfOfUsername(instagramUsername);
//            instagram.serviceRequestStatusByMediaID(mediaID);
//
//        } catch (BadAccessTokenException bate) {
//            logger.warn(bate);
//
//        }


    }

    protected void fetchStatus(String aOnBehalfOf, String aMediaID) {
       // String instagramUsername = (String) context.getMergedJobDataMap().get("onBehalfOfInstagramUsername");
        //String mediaID = (String) context.getMergedJobDataMap().get("mediaid");
        try {
            InstagramService instagram = InstagramService.createServiceOnBehalfOfUsername(aOnBehalfOf);
            instagram.serviceRequestStatusByMediaID(aMediaID);

        } catch (BadAccessTokenException bate) {
            logger.warn(bate);

        }

    }
}