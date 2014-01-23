package com.nearfuturelaboratory.humans.dao;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by julian on 1/15/14.
 */
public class TwitterStatusDAOTest {

    static TwitterStatusDAO dao;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        dao = new TwitterStatusDAO();
    }


    @Test
    public void testFindByExactScreename() throws Exception {

    }

    @Test
    public void testFindByExactUserID() throws Exception {

    }

    @Test
    public void testFindMostRecentStatusByExactUserID() throws Exception {

    }

    @Test
    public void testFindOldestStatusByExactUserID() throws Exception {

    }

    @Test
    public void testGetStatusCountForUserID() throws Exception {
        //dao.findByExactUserID("")
    }
}
