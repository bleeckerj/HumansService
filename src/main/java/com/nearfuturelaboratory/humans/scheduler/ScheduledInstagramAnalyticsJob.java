package com.nearfuturelaboratory.humans.scheduler;

import com.nearfuturelaboratory.humans.instagram.entities.InstagramUser;
import com.nearfuturelaboratory.humans.service.InstagramAnalyticsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.*;
import org.joda.time.chrono.ISOChronology;
import org.quartz.*;

import java.io.IOException;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
@SuppressWarnings("WeakerAccess")
@DisallowConcurrentExecution
public class ScheduledInstagramAnalyticsJob implements Job {

    final static Logger logger = LogManager.getLogger(ScheduledInstagramAnalyticsJob.class);
    static final int SEVEN_DAYS_BACK = 7;
    static final int TWO_DAYS_BACK = 2;
    static final int THREE_DAYS_BACK = 3;
    static final int FIVE_DAYS_BACK = 5;
    static final int TEN_DAYS_BACK = 10;
    static final int FIFTEEN_DAYS_BACK = 15;

    static final int ONE_DAY_BACK = 1;
    static final int MONTHS_BACK = 1;
    static InstagramAnalyticsService instagram;

    public void execute(JobExecutionContext context) throws JobExecutionException {
       // JobDataMap data = context.getJobDetail().getJobDataMap();

        try {
            //Constants.load("/Users/julian/Documents/workspace/HumansService/src/main/webapp/WEB-INF/lib/dev.app.properties");
            //PropertyConfigurator.configureAndWatch("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/WebContent/WEB-INF/lib/static-logger.properties");
            //logger.debug("Hey Ho!");

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
                    logger.info("Capture Instagram analytics for [" + userName + "] [" + count + " of " + listOfUserNames.size() + "]");

                    count++;

                    InstagramUser user_from_db = InstagramAnalyticsService.getLocalUserBasicForUsername(userName);
                    DateTime latest = new DateTime(ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles")))).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);


                    if (user_from_db != null) {

                        instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.days(ONE_DAY_BACK));

                        instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.days(TWO_DAYS_BACK));

                        instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.days(THREE_DAYS_BACK));

                        instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.days(FIVE_DAYS_BACK));

                        instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.days(SEVEN_DAYS_BACK));

                        instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.days(TEN_DAYS_BACK));

                        instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.days(FIFTEEN_DAYS_BACK));

                        instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.months(MONTHS_BACK));

//                        for(int i=1; i<8; i++) {
//                            instagram.captureInstagramUserAnalytics(user_from_db, latest.minusDays(i), Period.days(ONE_DAY_BACK));
//                        }


//                        //if(instagram.localUserBasicIsFreshForUserID(user_from_db.getUserID())) {
//                        InstagramUser user = instagram.serviceRequestUserBasicForUserID(user_from_db.getUserID());
//                        // save user basic just general in the user collection
//                        instagram.saveUserBasicForAnalytics(user);
//                        // save user basic just general in the user collection
//                        instagram.saveUserBasic(user);


                    } else {
                        List<InstagramUser> users = instagram.serviceUserSearch(userName);
                        if (users != null) {
                            users.removeIf(p -> (!p.getUsername().equalsIgnoreCase(userName)));
                            if (users.size() == 1) {
                                user_from_db = users.get(0);
                                // get the analytics
                                instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.days(ONE_DAY_BACK));

                                instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.days(TWO_DAYS_BACK));

                                instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.days(THREE_DAYS_BACK));

                                instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.days(FIVE_DAYS_BACK));

                                instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.days(SEVEN_DAYS_BACK));

                                instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.days(TEN_DAYS_BACK));

                                instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.days(FIFTEEN_DAYS_BACK));

                                instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.months(MONTHS_BACK));


//                                for(int i=1; i<8; i++) {
//                                    instagram.captureInstagramUserAnalytics(user_from_db, latest.minusDays(i), Period.days(ONE_DAY_BACK));
//                                }
//                                // save user basic snapshot for analytics
//                                instagram.saveUserBasicForAnalytics(users.get(0));
//                                // save user basic just general in the user collection
//                                instagram.saveUserBasic(users.get(0));
                            }
                        } else {
                            logger.warn("Could not find "+userName+" to perform analytics.");
                        }

                    }
                } catch (Exception e) {
                    logger.warn("", e);
                }
                try {
                    Thread.sleep(1000L);        //delay the code for 3 secs to account for rate limits
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