package com.nearfuturelaboratory.humans.scheduler;

import com.nearfuturelaboratory.humans.dao.HumansUserDAO;
import com.nearfuturelaboratory.util.Constants;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by julian on 1/18/14.
 */
public class ScheduledStatusFetcherTest {
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        //Logger.getRootLogger().setLevel(Level.OFF);

        try {

            Constants.load("/Users/julian/Documents/workspace/HumansService/src/main/webapp/WEB-INF/lib/dev.app.properties");
            //PropertyConfigurator.configureAndWatch("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/src/main/webapp/WEB-INF/lib/static-logger.properties");

        } catch(Exception e) {
            e.printStackTrace();
        }

    }


    @Test
    public void testFetchStatusForHumans() throws Exception {
        ScheduledStatusFetcher fetcher = new ScheduledStatusFetcher();
        fetcher.fetchStatusForHumans();
    }
}
