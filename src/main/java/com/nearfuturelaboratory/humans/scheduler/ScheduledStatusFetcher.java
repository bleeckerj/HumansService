package com.nearfuturelaboratory.humans.scheduler;

import com.nearfuturelaboratory.humans.dao.HumansUserDAO;
import com.nearfuturelaboratory.humans.entities.HumansUser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;
import java.util.List;

/**
 * Created by julian on 1/16/14.
 */
@DisallowConcurrentExecution
public class ScheduledStatusFetcher implements Job {
    final static Logger logger = LogManager.getLogger(ScheduledStatusFetcher.class);

    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        logger.info("Fetch Status for Humans " + context);
        try {
            fetchStatusForHumans();
        } catch (Exception e) {
            logger.error("", e);
        } catch (Error f) {
            logger.error("", f);
        }
    }

    protected void fetchStatusForHumans() {
        //logger.info("Trying to fetch status for humans");

        HumansUserDAO dao = new HumansUserDAO();
        List<HumansUser> all = dao.getAllHumansUsers();
        int all_count = 0;
        Date begin = new Date();

        logger.info("****** Starting fetchStatusForHumans " + this + " count=" + all.size());
        for (HumansUser humansUser : all) {
            logger.info("=========== Fetch Status For " + humansUser.getUsername() + " ==============");
            Date start = new Date();
            try {
                humansUser.refreshStatusForAllHumans();
                //humansUser.getHumans();
                //humansUser.save();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                logger.error("Something bad happened whilst fetching status for " + humansUser.getUsername() + " " + humansUser.getId());
            } catch (Error error) {
                logger.error(error.getMessage(), error);
            }
            int count = humansUser.getHumansServiceUsersCount();
            all_count += count;
            Date end = new Date();
            double time = end.getTime() - start.getTime();
            double rate = (time/1000)/count;
            logger.info("=========== Done Fetch Status For " + humansUser.getUsername() + " fetched "+ count +" at "+ rate +" seconds per  ==============");
        }
        Date finish = new Date();
        double time = finish.getTime() - begin.getTime();
        double rate = (time/1000)/all_count;
        logger.info("****** Done fetching status for humans. Fetched "+all_count+" at "+ rate +" seconds per ");
    }

}
