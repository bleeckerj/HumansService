package com.nearfuturelaboratory.humans.scheduler;

import com.nearfuturelaboratory.humans.dao.HumansUserDAO;
import com.nearfuturelaboratory.humans.entities.Human;
import com.nearfuturelaboratory.humans.entities.HumansUser;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramUser;
import com.nearfuturelaboratory.humans.service.InstagramAnalyticsService;
import com.nearfuturelaboratory.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.*;
import org.joda.time.chrono.ISOChronology;
import org.quartz.*;

import javax.servlet.ServletContext;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by julian on 12/9/15.
 */
@DisallowConcurrentExecution
public class ScheduledInstagramAnalyticsJob implements Job {

    final static Logger logger = LogManager.getLogger(ScheduledInstagramAnalyticsJob.class);
    static int SEVEN_DAYS_BACK = 7;
    static int ONE_DAY_BACK = 1;
    static int MONTHS_BACK = 1;
    static InstagramAnalyticsService instagram;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap data = context.getJobDetail().getJobDataMap();

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
                    DateTime latest = new DateTime(ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))));
                    latest.withHourOfDay(0);
                    latest.withMinuteOfHour(0);
                    latest.withSecondOfMinute(0);

                    DateTime month_ago = latest.minusMonths(1);
                    if (user_from_db != null) {

                        instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.days(ONE_DAY_BACK));

                        instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.days(SEVEN_DAYS_BACK));

                        instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.months(MONTHS_BACK));


//                        //if(instagram.localUserBasicIsFreshForUserID(user_from_db.getUserID())) {
//                        InstagramUser user = instagram.serviceRequestUserBasicForUserID(user_from_db.getUserID());
//                        // save user basic just general in the user collection
//                        instagram.saveUserBasicForAnalytics(user);
//                        // save user basic just general in the user collection
//                        instagram.saveUserBasic(user);


                    } else {
                        List<InstagramUser> users = instagram.serviceUserSearch(userName);
                        if (users != null) {
                            users.removeIf(p -> (p.getUsername().equalsIgnoreCase(userName) == false));
                            if (users.size() == 1) {
                                // get the analytics
                                instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.days(ONE_DAY_BACK));

                                instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.days(SEVEN_DAYS_BACK));

                                instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.months(MONTHS_BACK));

//                                // save user basic snapshot for analytics
//                                instagram.saveUserBasicForAnalytics(users.get(0));
//                                // save user basic just general in the user collection
//                                instagram.saveUserBasic(users.get(0));
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