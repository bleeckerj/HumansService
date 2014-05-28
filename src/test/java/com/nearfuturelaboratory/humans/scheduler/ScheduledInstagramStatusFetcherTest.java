package com.nearfuturelaboratory.humans.scheduler;

import com.nearfuturelaboratory.util.Constants;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.quartz.JobExecutionContext;

/**
 * Created by julian on 5/27/14.
 */
public class ScheduledInstagramStatusFetcherTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        try {

            Constants.load("/Users/julian/Documents/workspace/HumansService/src/main/webapp/WEB-INF/lib/dev.app.properties");
            //PropertyConfigurator.configureAndWatch("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/src/main/webapp/WEB-INF/lib/static-logger.properties");

        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void test_ScheduledInstagramStatusFetcher() throws Exception {
        ScheduledInstagramStatusFetcher fetcher = new ScheduledInstagramStatusFetcher();
        fetcher.fetchStatus("darthjulian", "729176318958312770_1342246");
    }
}
