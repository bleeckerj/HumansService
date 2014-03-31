package com.nearfuturelaboratory.humans.scheduler;

import com.nearfuturelaboratory.humans.dao.HumansUserDAO;
import com.nearfuturelaboratory.humans.entities.Human;
import com.nearfuturelaboratory.humans.entities.HumansUser;
import org.quartz.Job;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by julian on 2/9/14.
 */

/**
 * Takes a humanid and access_token in order to run as a job a fetch of human status and then caching of it.
 * A way to immediately update a human's status for, example, when you create a new human so you don't have to
 * wait a full refresh cycle..
 * @see com.nearfuturelaboratory.humans.rest.UserHandler#updateStatusAndCacheForHuman(String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 */
public class ScheduledHumanStatusFetcher implements Job {

    final static Logger logger = LogManager.getLogger(ScheduledHumanStatusFetcher.class);

    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap data = context.getJobDetail().getJobDataMap();
        String human_id = data.getString("humanid");
        String access_token = data.getString("access_token");
        logger.info("Fetch Individual Human Status for humanid="+human_id+" access_token="+access_token+" context="+context);
        HumansUserDAO dao = new HumansUserDAO();
        HumansUser user = dao.findOneByAccessToken(access_token);
        if(user != null) {
            Human human = user.getHumanByID(human_id);

            human.fixImageUrls();
            // TODO for every human, also refresh its own service users? make sure they have correct imageURL, etc.? how?
            // N.B. they may not have them if it's a weird case like Youman where the client may not have full specs on the service user
            logger.info("started fetching individual status for "+human.getName()+" for "+user.getUsername());
            user.serviceRefreshStatusForHuman(human);
            user.refreshCache(human);
            logger.info("done fetching individual status for "+human.getName()+" for "+user.getUsername());
        }
    }



}
