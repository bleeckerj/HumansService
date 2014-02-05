package com.nearfuturelaboratory.humans.util;

import com.nearfuturelaboratory.humans.dao.HumansUserDAO;
import com.nearfuturelaboratory.humans.entities.Human;
import com.nearfuturelaboratory.humans.entities.HumansUser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by julian on 1/22/14.
 */
public class RefreshHuman {
    final static Logger logger = LogManager.getLogger(com.nearfuturelaboratory.humans.util.RefreshHuman.class);
//    static String humansUserID;
//    static String humanID;

    public static void main(String[] args) {
        if(args.length < 2) {
            logger.warn("RefreshHuman received wrong argument count "+args);
        } else {
//           humansUserID = args[0];
//            humanID = args[1];
            RefreshHuman refresher = new RefreshHuman();
            refresher.refreshHumanByID(args[0], args[1]);
        }

    }

    public void refreshHumanByID(String aHumansUserID, String aHumanID) {
        HumansUserDAO dao = new HumansUserDAO();
        HumansUser user = dao.findOneByID(aHumansUserID);
        Human human = user.getHumanByID(aHumanID);
        user.serviceRefreshStatusForHuman(human);

        //logger.info("Hot flash serviceRefreshStatusForHuman "+aHuman);
        //aHumansUser.serviceRefreshStatusForHuman(aHuman);

    }

}
