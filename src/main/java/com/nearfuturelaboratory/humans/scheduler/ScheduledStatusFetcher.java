package com.nearfuturelaboratory.humans.scheduler;

import com.nearfuturelaboratory.humans.dao.HumansUserDAO;
import com.nearfuturelaboratory.humans.entities.Human;
import com.nearfuturelaboratory.humans.entities.HumansUser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;

/**
 * Created by julian on 1/16/14.
 */
public class ScheduledStatusFetcher implements Job {
    final static Logger logger = LogManager.getLogger(ScheduledStatusFetcher.class);

    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        logger.info("Fetch Status for Humans " + context);
        fetchStatusForHumans();
    }

    protected void fetchStatusForHumans()
    {
        logger.info("Trying to fetch status for humans");
        try {
        HumansUserDAO dao = new HumansUserDAO();
        List<HumansUser> all = dao.getAllHumansUsers();
            logger.info("starting fetchStatusForHumans");
        for (HumansUser humansUser : dao.getAllHumansUsers()) {
            logger.debug("=========== Fetch Status For "+humansUser.getUsername() +" ==============");

                humansUser.refreshStatusForAllHumans();
        }
            logger.info("done fetchStatusForHumans");

        }catch (Exception e) {
            logger.error("woops", e);
        }
        logger.info("Done fetching status for humans");
    }

}
