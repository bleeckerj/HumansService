package com.nearfuturelaboratory.humans.util;

import com.nearfuturelaboratory.humans.dao.HumansUserDAO;
import com.nearfuturelaboratory.util.Constants;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by julian on 1/22/14.
 */
public class RefreshHumanTest {

    static HumansUserDAO dao;
    static HumansUserDAO dao_dev;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Constants.load("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/src/main/webapp/WEB-INF/lib/dev.app.properties");

        dao = new HumansUserDAO("test-humans-user");
        dao_dev = new HumansUserDAO("humans");

    }

    @Test
    public void testMain() throws Exception {

        Process p = new ProcessBuilder("java", "-cp", "/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/target/humans-0.1.0.war", "com.nearfuturelaboratory.humans.util.RefreshHuman","527826f84f38c1d2442f7be8","52df74850364e4bd329f50d5" ).start();

        System.out.println(System.getenv());
        //Process p = pb.start();


//        String[] args = {"527826f84f38c1d2442f7be8","52df74850364e4bd329f50d5"};
//        RefreshHuman.main(args);

    }

    @Test
    public void testRefreshHumanByID() throws Exception {

    }
}
