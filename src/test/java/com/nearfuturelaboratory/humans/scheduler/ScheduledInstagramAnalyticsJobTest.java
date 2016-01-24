package com.nearfuturelaboratory.humans.scheduler;

import com.nearfuturelaboratory.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by julian on 12/15/15.
 */
public class ScheduledInstagramAnalyticsJobTest {
    //final static Logger logger = LogManager.getLogger(com.nearfuturelaboratory.humans.scheduler.ScheduledInstagramAnalyticsJobTest);
    @Before
    public void setUp() throws Exception {
        try {
            Constants.load("/Users/julian/Documents/workspace/HumansService/src/main/webapp/WEB-INF/lib/dev.app.properties");
            //PropertyConfigurator.configureAndWatch("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/WebContent/WEB-INF/lib/static-logger.properties");
            //logger.debug("Hey Ho!");

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void test_Execute() {

    }

}
