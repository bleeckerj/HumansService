package com.nearfuturelaboratory.humans.scheduler;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramUser;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramUserBriefly;
import com.nearfuturelaboratory.humans.service.InstagramAnalyticsService;
import com.nearfuturelaboratory.humans.util.MongoUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.joda.time.*;
import org.joda.time.chrono.ISOChronology;
import org.quartz.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by julian on 1/12/16.
 */
@SuppressWarnings("WeakerAccess")
@DisallowConcurrentExecution
public class ScheduledFollowersFetcherJob implements Job{
    final static Logger logger = LogManager.getLogger(ScheduledFollowersFetcherJob.class);
    static InstagramAnalyticsService instagram;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        //String username = "0_just_a_test";

        try {
            instagram = InstagramAnalyticsService.createServiceOnBehalfOfUsername("darthjulian");

        } catch (Exception e) {
            e.printStackTrace();
        }

        logger.info("Job in " + context);
        String now = InstagramAnalyticsService.short_date_fmt.print(new DateTime().withChronology(ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles")))));
        MongoClient client = MongoUtil.getMongo();
        MongoDatabase database = client.getDatabase("instagram-followers");

        ArrayList<String> users = new ArrayList();
        //stinnerframeworks,paulcomponent,vulpinecc,theradavist,vernor,blacksheepcycling,iamtedking,fabian_cancellara
        // 18%

        //users.add("stinnerframeworks");
        users.add("paulcomponent");
        //users.add("vulpinecc");
        users.add("theradavist");
        users.add("vernor");
        users.add("blacksheepcycling");
        users.add("iamtedking");

        //users.add("fabian_cancellara");
        users.add("darthjulian");
        users.add("omata_la");
        users.add("stravacycling");
        //users.add("rapha");
        //users.add("rittecycles");
        users.add("kylebkelley");
        users.add("johnprolly");
        //users.add("goldensaddlecyclery");
        users.add("daisukeyanocx");
        users.add("bicyclingmag");

        //ArrayList<String> all = new ArrayList<>();
        //final int[] count = {0};
        //final Integer[] count = {0};
        //Gson gson = new Gson();
        //String s = gson.toJson(friends);
        for(int i=0; i<users.size(); i++) {
            try {
                String username = users.get(i);
                logger.debug("On "+username+" "+(i+1)+"/"+users.size());
                InstagramUser user = InstagramAnalyticsService.staticGetLocalUserBasicForUsername(username);
                List<InstagramUserBriefly> followers = instagram.serviceRequestFollowersAsUsersBriefly(user.getUserID(),8500);
                ArrayList<Document> l = new ArrayList<>();
                MongoCollection collection = database.getCollection(username+"_followers_"+now);
                ArrayList<String> uniques = new ArrayList();
                for(int d=0; d<followers.size(); d++) {
                    InstagramUserBriefly follower = followers.get(d);
                    if(uniques.contains(follower.getUsername()) == false) {
                        logger.debug(follower.getUsername() + " " + (d + 1) + "/" + followers.size());
                        l.add(new Document().append("_id", Long.parseLong(follower.getId())).append("id", Long.parseLong(follower.getId())).append("username", follower.getUsername()).append("name", follower.getFull_name()));
                    }
                }
                database.listCollectionNames();
                collection.drop();
                collection.insertMany(l);
                collection.createIndex(new Document().append("username", 1));
                try {
                    Thread.sleep(1000*60*1);
                } catch(InterruptedException ie) {
                    logger.warn(ie);
                }
            } catch(Exception exc) {
                logger.warn(exc);
            }
        }

    }


}
