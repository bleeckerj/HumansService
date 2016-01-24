package com.nearfuturelaboratory.humans.scheduler;

import com.nearfuturelaboratory.humans.instagram.entities.InstagramUser;
import com.nearfuturelaboratory.humans.service.InstagramAnalyticsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.*;

import java.io.IOException;
import java.util.List;

/**
 * Created by julian on 12/23/15.
 */
@DisallowConcurrentExecution
public class ScheduledInstagramUserBasicAnalyticsJob implements Job {
    final static Logger logger = LogManager.getLogger(ScheduledInstagramUserBasicAnalyticsJob.class);
//    static int SEVEN_DAYS_BACK = 7;
//    static int ONE_DAY_BACK = 1;
//    static int MONTHS_BACK = 1;
    static InstagramAnalyticsService instagram;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap data = context.getJobDetail().getJobDataMap();

        try {
            instagram = InstagramAnalyticsService.createServiceOnBehalfOfUsername("darthjulian");

        } catch (Exception e) {
            e.printStackTrace();
        }

        logger.info("Job in " + context);

        try {
            List<String> listOfUserNames = instagram.gatherUsernamesForAnalytics();
            int count = 1;
            for (String userName : listOfUserNames) {
                try {
                    logger.info("Capture Instagram user basic analytics for [" + userName + "] [" + count + " of " + listOfUserNames.size() + "]");

                    count++;

                    InstagramUser user_from_db = InstagramAnalyticsService.getLocalUserBasicForUsername(userName);

                    if (user_from_db != null) {

                        //if(instagram.localUserBasicIsFreshForUserID(user_from_db.getUserID())) {
                        InstagramUser user = instagram.serviceRequestUserBasicForUserID(user_from_db.getUserID());
                        // save user basic just general in the user collection
                        instagram.saveUserBasicForAnalytics(user);
                        // save user basic just general in the user collection
                        instagram.saveUserBasic(user);

                    } else {
                        List<InstagramUser> users = instagram.serviceUserSearch(userName);
                        if (users != null) {
                            users.removeIf(p -> (p.getUsername().equalsIgnoreCase(userName) == false));
                            if (users.size() == 1) {
                                // get the analytics
//                                instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.days(ONE_DAY_BACK));
//
//                                instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.days(SEVEN_DAYS_BACK));
//
//                                instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.months(MONTHS_BACK));
                                //instagram.captureInstagramUserAnalytics(users.get(0), month_days);

                                // save user basic snapshot for analytics
                                instagram.saveUserBasicForAnalytics(users.get(0));
                                // save user basic just general in the user collection
                                instagram.saveUserBasic(users.get(0));
                            }
                        }

                    }
                } catch (Exception e) {
                    logger.warn("", e);
                }
                try {
                    Thread.sleep(3000l);        //delay the code for 3 secs to account for rate limits
                } catch (InterruptedException ex) {  //and handle the exceptions
                    //Thread.currentThread().interrupt();
                    logger.warn("", ex);
                }

            }
        } catch (IOException ioe) {
            logger.warn("", ioe);
        }
    }
}