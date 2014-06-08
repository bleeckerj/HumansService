package com.nearfuturelaboratory.humans.scheduler;

import com.nearfuturelaboratory.humans.dao.HumansUserDAO;
import com.nearfuturelaboratory.humans.entities.Human;
import com.nearfuturelaboratory.humans.entities.HumansUser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

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
        } catch(Exception e) {
            logger.error(e);
        } catch(Error f) {
            logger.error(f);
        }
    }

    protected void fetchStatusForHumans()
    {
        //logger.info("Trying to fetch status for humans");

        HumansUserDAO dao = new HumansUserDAO();
        List<HumansUser> all = dao.getAllHumansUsers();


        logger.info("****** Starting fetchStatusForHumans "+this+" count="+all.size());
        for (HumansUser humansUser : all) {
            logger.info("=========== Fetch Status For "+humansUser.getUsername() +" ==============");

            try {
                humansUser.refreshStatusForAllHumans();
                //humansUser.save();
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
                logger.warn("Something bad happened whilst fetching status for "+humansUser.getUsername()+" "+humansUser.getId());
            }
            logger.info("=========== Done Fetch Status For "+humansUser.getUsername() +" ==============");
        }

        logger.info("****** Done fetching status for humans");
    }

}
