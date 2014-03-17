package com.nearfuturelaboratory.humans.util;

import com.nearfuturelaboratory.humans.dao.HumansUserDAO;
import com.nearfuturelaboratory.humans.entities.HumansUser;
import com.nearfuturelaboratory.util.Constants;
import org.apache.log4j.PropertyConfigurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by julian on 3/11/14.
 */
public class HumansUserHygeine {

    final static Logger logger = LogManager.getLogger(HumansUserHygeine.class);
    static HumansUserDAO dao;

    public static void main(String[] args) {
        try {
            Constants.load("/Users/julian/Documents/workspace/HumansService/src/main/webapp/WEB-INF/lib/dev.app.properties");
            PropertyConfigurator.configureAndWatch("/Users/julian/Documents/workspace/HumansService/src/main/webapp/WEB-INF/lib/static-logger.properties");
            logger.debug("Hey Ho!");

            dao = new HumansUserDAO();
            HumansUserHygeine hygeine = new HumansUserHygeine();
            hygeine.cleanAllHumans();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    protected void cleanAllHumans() {
        List<HumansUser> users = dao.getAllHumansUsers();
        logger.debug(users);

    }

}
