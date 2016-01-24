package com.nearfuturelaboratory.humans.service;

import com.google.gson.*;
import com.jayway.jsonpath.JsonPath;
import com.mongodb.*;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.nearfuturelaboratory.humans.exception.BadAccessTokenException;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramFriend;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramUser;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramUserBriefly;
import com.nearfuturelaboratory.humans.util.MongoUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.joda.time.*;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.nearfuturelaboratory.util.Constants;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Indexes.ascending;
import static com.mongodb.client.model.Indexes.descending;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Sorts.orderBy;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;

/**
 * Created by julian on 11/23/15.
 */
public class InstagramAnalyticsServiceTest {
    static InstagramAnalyticsService instagram;
    final static Logger logger = LogManager.getLogger(com.nearfuturelaboratory.humans.service.InstagramAnalyticsServiceTest.class);
    static int SEVEN_DAYS_BACK = 7;
    static int ONE_DAY_BACK = 1;
    static int TWO_DAYS_BACK = 2;
    static int THREE_DAYS_BACK = 3;
    static int FIVE_DAYS_BACK = 5;
    static int MONTHS_BACK = 1;


    @Before
    public void setUp() throws Exception {
        try {
            Constants.load("/Users/julian/Documents/workspace/HumansService/src/main/webapp/WEB-INF/lib/dev.app.properties");
            //PropertyConfigurator.configureAndWatch("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/WebContent/WEB-INF/lib/static-logger.properties");
            //logger.debug("Hey Ho!");
            instagram = InstagramAnalyticsService.createServiceOnBehalfOfUsername("darthjulian");

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() throws Exception {

    }

    @Ignore
    public void testCreateServiceOnBehalfOfUsername() throws Exception {

    }

    @Test
    public void testUsersSearch() throws Exception {
        List<InstagramUser> users =  instagram.serviceUserSearch("darthjulian");
        InstagramUser user = null;
        if(users.size() > 0) {
            user = users.get(0);
            //instagram.captureInstagramUserAnalytics(user);
            //List<InstagramStatus> status = instagram.serviceRequestStatusForUserIDToMonthsAgo(user.getUserID(), 3);
            //logger.debug(status);


        }
    }
    //TODO Mark
    // this generates sorted sort of things
    @Test
    public void test_getSortedListByEngagementAnalytic() {
        List<Document> sorted = instagram.getSortedListByEngagementAnalytic("avg-likes");
        List<Document> clipped = instagram.getListOfInstagramAnalyticsDocumentsByDayOfYearFromDocumentList(sorted, 338);

        String sortedClippedJson = new Gson().toJson(clipped);
        logger.debug("done");
    }

    @Test
    public void test_getListOfInstagramAnalyticsDocumentsByDate() {
        List<String> collectionNames = instagram.getListOfInstagramAnalyticsCollections();
        List<Document> result = instagram.getListOfInstagramAnalyticsDocumentsByDate(collectionNames, "123015", new Period().withDays(1));
        logger.debug("");




    }



    @Test
    public void wtf() {
        MongoDatabase db = MongoUtil.getMongo().getDatabase("instagram-analytics");
        List<Document> result = new ArrayList<Document>();
        List<String> aCollectionNames = new ArrayList<String>();
        aCollectionNames.add("rapha_10282731"); //instagram.getListOfInstagramAnalyticsCollections();
        String date = "121715";

        ArrayList<String> unique = new ArrayList<>();
        aCollectionNames.forEach((s) -> {
            // should add an additional clause for the Period (eg range from P1D, P7D, P1M)
            FindIterable<Document> docs_i;// = db.getCollection(s).find(eq("snapshot-date", date));
            //this.analytics[\"engagement-analytics-meta\"][\"earliest-in-period\"] = Dec 21,2015 17 12
            DateTimeFormatter formatter = DateTimeFormat.forPattern("MM dd,yyyy");
            docs_i = db.getCollection(s).find(
                    new Document
                            //("analytics.engagement-analytics-meta.earliest-in-period-date", date)
                            ("period-coverage-start-millis", new Document("$gte", 145033920000l))
                            .append("snapshot-coverage-period", "P1D")

                            .append("analytics.engagement-analytics-meta.period-posts-count", new Document("$gte", 0))
            ).sort(orderBy(descending("period-coverage-start-millis")));


            docs_i.forEach((Block<Document>) document -> {
                //TODO
                // check that we don't include duplicates, and if there are duplicate docs with a username
                // caused by multiple runs against Instagram, get the latest one
                // And maybe flat that there are dupes so we can garden later, maybe..
                if( unique.contains((document.getString("snapshot-date"))) == false) {

                    result.add(document);
                    unique.add(document.getString("snapshot-date"));
                }
            });
        });
    }

    /**
     * Find dates of instagram postings for a particular user
     */
    @Test
    public void test_getListOfInstagramAnalyticsDocumentsByUsername() {
        List<Document> docs = instagram.getListOfInstagramAnalyticsDocumentsByUsername("rapha");

    }


    @Test
    public void test_getPostDatesForUsername() {
        DateTimeFormatter fmt_short = ISODateTimeFormat.dateTime();//DateTimeFormat.forPattern("MMddYY-");
        List<DateTime> d = instagram.getPostDatesFor("iamtedking");
        d.forEach(t -> {
            System.out.println("---> "+fmt_short.print(t));
        });
    }

    @Test
    public void weird() {
        MongoDatabase db = MongoUtil.getMongo().getDatabase("instagram-analytics");
        FindIterable<Document> docs = db.getCollection("_superdomestik_417717129").find(eq("snapshot-day-of-year", "333"));

        Document foo = docs.first();
        docs.forEach((Block<Document>) document -> {
            String json = document.toJson();
            logger.debug(json);
        });
    }


    //TODO FIX
    @Test
    public void fixDatabaseEarliestLatestInPeriod() {
        MongoDatabase database = MongoUtil.getMongo("localhost", 29017 ).getDatabase("instagram-analytics");
//        MongoClient client = new MongoClient("localhost", 29017);
//        instagram.setDBClient(client);

        List<String> collectionNames = instagram.getListOfInstagramAnalyticsCollections();
        //String name = "iamtedking_174583746";
        collectionNames.forEach(name -> {
            MongoCollection<Document> collection = database.getCollection(name);
//            FindIterable<Document> docs = collection.find();
//            FindIterable<Document> docs = db.getCollection(name).find();
//            docs.forEach((Block<Document>) document -> {
            // here is where I'm actually in a document
            //BasicDBObject carrier = new BasicDBObject();
            BasicDBObject query = new BasicDBObject();

            //query.put("analytics.engagement-analytics-meta.earliest-in-period", java.util.regex.Pattern.compile(".*Dec 16.*"));

            //BasicDBObject set = new BasicDBObject("$set", carrier);
            //carrier.put("analytics.engagement-analytics-meta.earliest-in-period-date", "121615");
            //carrier.put("analytics.engagement-analytics-meta.latest-in-period-date", "121615");
            //carrier.put("earliest-in-period-date", "121615");

            // get earliest-in-period
            //BasicDBObject e_query = new BasicDBObject();
            //e_query.put("", "analytics.engagement-analytics-meta.earliest-in-period : 1");
            ///fields(include("analytics.engagement-analytics-meta.earliest-in-period", "analytics.engagement-analytics-meta.latest-in-period"), excludeId());
            // new Document("_id", new ObjectId("56725a4090db697cb4798207"))
            FindIterable<Document> e_found = collection.find().projection(fields(include(
                    "username",
                    "userid",
                    "snapshot-coverage-period",
                    "snapshot-run-date",
                    "snapshot-date",
                    "analytics.engagement-analytics-meta.period-posts-count",
                    "analytics.engagement-analytics-meta.earliest-in-period",
                    "analytics.engagement-analytics-meta.latest-in-period")
            ));
            e_found.forEach((Block<Document>) document -> {
                String username = document.getString("username");
                String userid = document.getString("userid");
                if(document != null || username != null) {

                    try {
                        logger.debug("fixDatabaseSchema for db.getCollection('" + username + "_" + userid + "\').find({}) " + document.get("snapshot-coverage-period"));

                        DateTimeFormatter sh_formatter = DateTimeFormat.forPattern("MMddyy");

                        String earliest = document.
                                get("analytics", org.bson.Document.class).
                                get("engagement-analytics-meta", org.bson.Document.class).
                                getString("earliest-in-period");

                        String latest = document.
                                get("analytics", org.bson.Document.class).
                                get("engagement-analytics-meta", org.bson.Document.class).
                                getString("latest-in-period");

                        Integer i_posts = null;
                        Double posts;
                        try {
                            posts = document.
                                    get("analytics", org.bson.Document.class).
                                    get("engagement-analytics-meta", org.bson.Document.class).
                                    getDouble("period-posts-count");
                        } catch (ClassCastException cce) {
                            i_posts = document.
                                    get("analytics", org.bson.Document.class).
                                    get("engagement-analytics-meta", org.bson.Document.class).
                                    getInteger("period-posts-count");
                            posts = i_posts.doubleValue();
                        }


                        String period = document.getString("snapshot-coverage-period");
                        Period p = Period.parse(period);

                        DateTime sn_date = sh_formatter.parseDateTime(document.getString("snapshot-date"));
                        DateTime sn_date_minus_period_date = sn_date.withPeriodAdded(p, -1);

                        DateTimeFormatter formatter = DateTimeFormat.forPattern("MMM dd,yyyy HH mm");
                        DateTime dt_earliest = formatter.parseDateTime(earliest);
                        DateTime dt_latest = formatter.parseDateTime(latest);

                        query.put("_id", document.getObjectId("_id"));
                        // do "analytics.engagement-analytics-meta.earliest-in-period-date
                        // do "analytics.engagement-analytics-meta.latest-in-period-date
                        // do "earliest-in-period-date
                        // do "period-coverage-start-date
                        // do "period-coverage-end-date
                        // do "posts-count-in-coverage-period" (e.g. analytics.engagement-analytics-meta.periods-posts-count)
                        Document fix = new Document("$set", new Document()
                                .append("period-coverage-start-date", sh_formatter.print(sn_date_minus_period_date))
                                .append("period-coverage-end-date", sh_formatter.print(sn_date))
                                .append("period-coverage-start-millis", Double.valueOf(sn_date_minus_period_date.getMillis()))
                                .append("period-coverage-end-millis", Double.valueOf(sn_date.getMillis()))
                                .append("period-coverage-start-day-of-year", Double.valueOf(sn_date_minus_period_date.getDayOfYear()))
                                .append("period-coverage-end-day-of-year", Double.valueOf(sn_date.getDayOfYear()))
                                .append("period-coverage-start-month", Double.valueOf(sn_date_minus_period_date.getMonthOfYear()))
                                .append("period-coverage-end-month", Double.valueOf(sn_date.getMonthOfYear()))
                                .append("period-coverage-start-week", Double.valueOf(sn_date_minus_period_date.getWeekOfWeekyear()))
                                .append("period-coverage-end-week", Double.valueOf(sn_date.getWeekOfWeekyear()))
                                .append("period-coverage-start-year", Double.valueOf(sn_date_minus_period_date.getYear()))
                                .append("period-coverage-end-year", Double.valueOf(sn_date.getYear()))
                                .append("posts-count-in-coverage-period", posts)
                                .append("analytics.engagement-analytics-meta.earliest-in-period-date", sh_formatter.print(dt_earliest))
                                .append("analytics.engagement-analytics-meta.latest-in-period-date", sh_formatter.print(dt_latest)));
                        UpdateResult result = collection.updateOne(query, fix, new UpdateOptions().upsert(false));
                        logger.debug("result=" + result);
                        //UpdateResult result = collection.updateMany(query, set, new UpdateOptions().upsert(false));
                    } catch(Exception e) {
                        logger.warn("", e);
                        logger.warn(document.toJson());
                    }
                } else {
                    logger.debug("This is weird: "+document);
                }

            });


        });

    }




    @Test
    public void test_getAllStatusForDateOrdered() {
        //DateTime now = new DateTime(ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))));
        Gson gson = new Gson();
        JsonObject o = new JsonObject();
        JsonArray a = new JsonArray();
        List<Document> docs = instagram.getAllStatusForDateOrdered(new DateTime());
        docs.forEach((Consumer<? super Document>) d -> {

            //            DateTime dt = new DateTime(new Date(d.getInteger("created_time").longValue()*1000));
            //            logger.debug(fmt_short.print(dt));
            //            logger.debug(d.getString("link")+", "+new Date(d.getInteger("created_time").longValue()*1000));

            a.add(new JsonParser().parse(d.toJson()));
        });
        //logger.debug(docs.size());
        o.add("status",a);
    }

    @Test
    public void test_getAnalyticsDocumentsByUsername() throws Exception {
        // start with a username_userid
        ArrayList<Document> result = new ArrayList<>();
        MongoDatabase db = MongoUtil.getMongo().getDatabase("instagram-analytics");
        InstagramUser user = instagram.localUserSearch("rapha");
        String coll_name = user.getUsername()+"_"+user.getUserID();
        FindIterable<Document> docs_i = db.getCollection(coll_name).find(eq("snapshot-day-of-year", "338"));
        docs_i.forEach((Block<Document>) document -> {
            result.add(document);
        });

    }


    @Test
    public void test_getListOfInstagramAnalyticsDocumentsAndSort() throws Exception {
        List<String> collectionNames = instagram.getListOfInstagramAnalyticsCollections();
        List<Document> docs = instagram.getListOfInstagramAnalyticsDocumentsByDayOfYear(collectionNames, 333);
        //logger.debug(docs);
        // String json = docs.get(0).toJson();
        //Number n = (Number)JsonPath.read(json, "$.analytics.engagement-analytics-meta.max-likes");

        Collections.sort(docs,new Comparator<Document>() {
            public int compare(Document d1, Document d2) {
                Double n1 = JsonPath.read( d1.toJson(), "$.analytics.engagement-analytics-meta.avg-comments");
                Double n2 = JsonPath.read( d2.toJson(), "$.analytics.engagement-analytics-meta.avg-comments");
                return n2.compareTo(n1);
            }
        });
        // logger.debug(docs);

    }

    @Test
    public void test_captureInstagramUserBasicAnalytics() throws Exception {
        String userName = "100copies";


        try {
            instagram.setDBClient(MongoUtil.getMongo("localhost", 29017));
            // logger.info("Back Capture Instagram analytics for [" + userName + "] "+count+" of "+listOfUserNames.size());
            InstagramUser user_from_db = InstagramAnalyticsService.getLocalUserBasicForUsername(userName);

            DateTime latest = new DateTime(2015, 12, 15, 0, 0, ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))));

//                instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.days(1));
//                //if(instagram.localUserBasicIsFreshForUserID(user_from_db.getUserID())) {
//        InstagramUser user = instagram.serviceRequestUserBasicForUserID(user_from_db.getUserID());
//        // save user basic just general in the user collection
//        instagram.saveUserBasicForAnalytics(user);
//        // save user basic just general in the user collection
//        instagram.saveUserBasic(user);
            //               instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.days(ONE_DAY_BACK));
//
//                instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.days(SEVEN_DAYS_BACK));
//
//                instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.months(MONTHS_BACK));
            Thread.sleep(5000l);




        }catch(Exception e) {
            logger.error(e);
        }


    }
//TODO BACKCAPTURE
//TODO BACKCAPTURE
//TODO BACKCAPTURE
//TODO BACKCAPTURE
//TODO BACKCAPTURE

    @Test
    public void test_backCaptureInstagramUserAnalytics() throws Exception {
//        MongoDatabase database = MongoUtil.getMongo("localhost", 29017 ).getDatabase("instagram-analytics");
        MongoClient client = new MongoClient("localhost", 29017);
        InstagramAnalyticsService h_instagram = InstagramAnalyticsService.createServiceOnBehalfOfUsername("darthjulian");

        h_instagram.setDBClient(client);
        DateTime master_latest = new DateTime(2016, 1, 17, 0, 0, ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))));
        DateTime end = new DateTime(2016, 1, 18, 0, 0, ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))));
        DateTime latest;

        int count = 1;

        List<String> listOfUserNames = instagram.gatherUsernamesForAnalytics(); //new ArrayList<String>();
//        List<String> listOfUserNames = new ArrayList<>();
        //listOfUserNames.addAll(Arrays.asList("JulianBleecker", "Skimskam", "skamskim"));
//        listOfUserNames.add("omata_la");
//        listOfUserNames.add("vernor");
        //       listOfUserNames.add("iamtedking");
//        listOfUserNames.add("ktonic");
//        listOfUserNames.add("outsidemagazine");
//        listOfUserNames.add("stinnerframeworks");
//        listOfUserNames.add("JeredGruber");


        listOfUserNames = listOfUserNames.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        List<String> alreadyDone = getCapturedUsernames(client, master_latest, end);

        alreadyDone = alreadyDone.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        listOfUserNames.removeAll(alreadyDone);



//        listOfUserNames.add("JeredGruber");
//        listOfUserNames.remove("omata_la");
//        listOfUserNames.remove("ktonic");
//        listOfUserNames.remove("theradavist");
//        listOfUserNames.remove("50ur15");
//        listOfUserNames.remove("thevanillaworkshop");

// 122515 - 010116
        InstagramUser user_from_db = null;
        for (String userName : listOfUserNames) {
            try {
                latest = master_latest;
                user_from_db = InstagramAnalyticsService.getLocalUserBasicForUsername(userName);
                if(user_from_db == null) {
                    logger.warn("What? "+userName+" isn't in instagram-analytics.user?");
                    continue;
                }
                logger.info("Back Capture Instagram analytics for [" + userName + "_"+user_from_db.getUserID()+"] "+count+" of "+listOfUserNames.size());

                do {

                    h_instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.days(ONE_DAY_BACK));

                    h_instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.days(TWO_DAYS_BACK));

                    h_instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.days(THREE_DAYS_BACK));

                    h_instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.days(FIVE_DAYS_BACK));

                    h_instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.days(SEVEN_DAYS_BACK));

                    h_instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.days(10));

                    h_instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.days(15));

                    h_instagram.captureInstagramUserAnalytics(user_from_db, latest, Period.months(MONTHS_BACK));


                    logger.warn("====> "+InstagramAnalyticsService.short_date_fmt.print(latest)+" for "+user_from_db.getUsername()+"_"+user_from_db.getUserID()+" "+count+"/"+listOfUserNames.size() +" <====");
                    logger.warn("====> "+InstagramAnalyticsService.short_date_fmt.print(latest)+" for "+user_from_db.getUsername()+"_"+user_from_db.getUserID()+" "+count+"/"+listOfUserNames.size() +" <====");
                    logger.warn("====> "+InstagramAnalyticsService.short_date_fmt.print(latest)+" for "+user_from_db.getUsername()+"_"+user_from_db.getUserID()+" "+count+"/"+listOfUserNames.size() +" <====");

                    //Thread.sleep(5000l);        //delay the code for 3 secs to account for rate limits

                    latest = latest.plusDays(1);
                    try {
                        Thread.sleep(1000);
                    } catch(InterruptedException ie) {
                        logger.error(ie);
                    }
                } while(!InstagramAnalyticsService.short_date_fmt.print(latest).equalsIgnoreCase(InstagramAnalyticsService.short_date_fmt.print(end)));
            } catch (Exception e) {
                logger.warn("user_from_db="+user_from_db, e);
            }
            addCapturedUsername(client, master_latest, end, user_from_db.getUsername());
            count++;
            //Thread.sleep(1000l);
        }
        finishAndMarkCompletedCapturedSet(client, master_latest, end);
    }


    @Test
    public void test_removeAnalyticsDocument() {
        MongoDatabase database = MongoUtil.getMongo("localhost", 29017).getDatabase("instagram-analytics");

        //MongoCollection rootUser = database.getCollection(encodeKey(aUser.getUsername()) + /*"_" + fmt_short.print(now)+*/"_" + aUser.getUserID());
        MongoCollection collection = database.getCollection("omata_la_1572694762");
        DeleteResult result = collection.deleteMany(new Document("username", "omata_la").append("period-coverage-end-date", "122215"));
        logger.debug(result);
    }


    //TODO Pick here to start
    //TODO Pick here to start
    //TODO Pick here to start
    //TODO Pick here to start
    //TODO Pick here to start
    //TODO Pick here to start
    //TODO Pick here to start

    // main test..despite captureInstagramUserAnalytics deprecated
    @Test
    public void test_captureInstagramUserAnalytics()  {
        //

        try {
            //////////List<String> listOfUserNames = instagram.gatherUsernamesForAnalytics();
            List<String> listOfUserNames = new ArrayList<>();
            listOfUserNames.add("vernor");

            for (String userName : listOfUserNames) {
                logger.info("Capture Instagram analytics for [" + userName + "]..");
                InstagramUser user_from_db = InstagramAnalyticsService.getLocalUserBasicForUsername(userName);
                DateTime now = new DateTime(ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))));
                //now.minusMonths(1);
                now.withHourOfDay(23);
                now.withMinuteOfHour(59);
                now.withSecondOfMinute(59);
                try {
                    if (user_from_db != null) {

                        instagram.captureInstagramUserAnalytics(user_from_db, now, new Period().withDays(ONE_DAY_BACK));

                        instagram.captureInstagramUserAnalytics(user_from_db, now, new Period().withDays(TWO_DAYS_BACK));

                        instagram.captureInstagramUserAnalytics(user_from_db, now, new Period().withDays(THREE_DAYS_BACK));

                        instagram.captureInstagramUserAnalytics(user_from_db, now, new Period().withDays(FIVE_DAYS_BACK));

                        instagram.captureInstagramUserAnalytics(user_from_db, now, new Period().withDays(SEVEN_DAYS_BACK));

                        int month_days = Days.daysBetween(new LocalDate(now.minusMonths(MONTHS_BACK)), new LocalDate(now)).getDays();

                        instagram.captureInstagramUserAnalytics(user_from_db, now, new Period().withMonths(MONTHS_BACK));

                        //if(instagram.localUserBasicIsFreshForUserID(user_from_db.getUserID())) {

                        InstagramUser user = instagram.serviceRequestUserBasicForUserID(user_from_db.getUserID());
                        // save user basic just general in the user collection
                        instagram.saveUserBasicForAnalytics(user);
                        // save user basic just general in the user collection
                        instagram.saveUserBasic(user);

                    } else {
                        //InstagramUser user = instagram.localUserSearch(userName);
                        List<InstagramUser> users = instagram.serviceUserSearch(userName);
                        if (users != null) {
                            users.removeIf(p -> (p.getUsername().equalsIgnoreCase(userName) == false));
                            if (users.size() == 1) {
                                // get the analytics
                                instagram.captureInstagramUserAnalytics(user_from_db, now, new Period().withDays(ONE_DAY_BACK));

                                instagram.captureInstagramUserAnalytics(user_from_db, now, new Period().withDays(TWO_DAYS_BACK));

                                instagram.captureInstagramUserAnalytics(user_from_db, now, new Period().withDays(THREE_DAYS_BACK));

                                instagram.captureInstagramUserAnalytics(user_from_db, now, new Period().withDays(FIVE_DAYS_BACK));

                                instagram.captureInstagramUserAnalytics(user_from_db, now, new Period().withDays(SEVEN_DAYS_BACK));

                                int month_days = Days.daysBetween(new LocalDate(now.minusMonths(MONTHS_BACK)), new LocalDate(now)).getDays();

                                instagram.captureInstagramUserAnalytics(user_from_db, now, new Period().withMonths(MONTHS_BACK));

                                // save user basic snapshot for analytics
                                instagram.saveUserBasicForAnalytics(users.get(0));
                                // save user basic just general in the user collection
                                instagram.saveUserBasic(users.get(0));
                            }
                        }
                    }
                } catch(Exception e) {
                    logger.warn(e);
                    logger.warn("", e);
                }
            }
        }catch(Exception e) {
            logger.error("", e);
        }
    }

    @Test
    public void test_serviceRequestTagsDataByTagName() {
        JsonElement result = instagram.serviceRequestTagsDataByTagName("design");
        logger.debug("");
    }

    @Test
    public void test_serviceRequestMediaRecentsByTag() {
        ArrayList<JSONObject> result = instagram.serviceRequestMediaRecentsByTag("cycling", 0, 1);
        JSONObject last = result.get(result.size()-1);
        ArrayList<JSONObject> results = new ArrayList<JSONObject>();
        result.forEach(status -> {
            Long count = JsonPath.read(status, "likes.count");
            if(count.longValue() == 0) {
                instagram.serviceLikeStatusByMediaID((String)status.get("id"));
                results.add(status);
            }

        });
        logger.debug("");
    }

    @Test
    public void test_getListOfInstagramAnalyticsCollections() {
        List<String> collectionNames = instagram.getListOfInstagramAnalyticsCollections();
        collectionNames.forEach((name -> {
            logger.debug(name);

        }));
    }

    @Test
    public void test_captureForDateBackwards() throws Exception {
        InstagramUser user_from_db = instagram.localUserSearch("rapha");
        DateTime latest = new DateTime(2015, 12, 19, 0, 0, ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))));
        Period period = Period.days(1);
        Date earliestDate = latest.minus(period).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).toDate();
        DateTime earliest = latest.minus(period).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
        instagram.captureInstagramUserAnalytics(user_from_db, latest, period);
        logger.debug("");
    }

    @Test
    public void test_serviceRequestFriendsAsUsers() throws Exception {
        List<InstagramUser> users = instagram.serviceUserSearch("darthjulian");
        InstagramUser user = null;
        List<InstagramUser> friends = null;
        if (users.size() > 0) {
            user = users.get(0);
                /*
                if(instagram.localFriendsIsFresh(user.getUserID())) {
                    instagram.getLocalFriendsFor(user.getUserID());
                } else {
                   friends = instagram.serviceRequestFriendsAsUsers(user.getUserID());
                }*/
            friends = instagram.serviceRequestFriendsAsUsers(user, 1000);
            //            logger.debug(friends);
            //            instagram.getInstagramUsersForTopOfListAnalyticsJson(friends);
        }
        Gson gson = new Gson();
        String s = gson.toJson(friends);

        logger.debug(s);
    }

    @Test
    public void test_saveRootUserFriends() throws Exception {
        InstagramUser user = instagram.localUserSearch("omata_la");
        instagram.saveRootUserFriends(user);



    }

    @Test
    public void test_saveUserBasicForAnalytics() {
        InstagramUser user = InstagramAnalyticsService.staticGetLocalUserBasicForUsername("darthjulian");
        instagram.saveUserBasicForAnalytics(user);

    }

    @Test
    public void test_serviceRequestFollowersAsUsers() throws Exception {
        List<InstagramUser> users = instagram.serviceUserSearch("rhnewman");
        InstagramUser user = null;
        List<InstagramUser> followers = null;
        if (users.size() > 0) {
            user = users.get(0);
            followers = instagram.serviceRequestFollowersAsUsers(user.getUserID());
            JsonElement result = instagram.getInstagramUsersForTopOfListAnalyticsJson(followers);
            logger.debug(result);
            //            instagram.getInstagramUsersForTopOfListAnalyticsJson(friends);
        }

    }

    @Test
    public void test_getInstagramUserFriends() throws Exception {
        List<InstagramUser> users = instagram.serviceUserSearch("rhnewman");
        InstagramUser user = null;
        List<InstagramUser> followers = null;
        if (users.size() > 0) {
            user = users.get(0);
            JsonElement e = instagram.getInstagramUserFriendsAsJson(user);
            if(e.isJsonArray()) {
                // it's an array..
            }
        }


    }

    @Test
    public void test_getInstagramUserStatusAndAnalytics() throws Exception {
        List<InstagramUser> users = instagram.serviceUserSearch("rhnewman");
        InstagramUser user = null;
        if (users.size() > 0) {
            user = users.get(0);
            JsonElement e = instagram.getInstagramUserStatusAndAnalytics(user);
            JsonObject obj = instagram.getRootUserMeta(user, new DateTime()
                    .withChronology(ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles")))));
            //obj.add("status", e);
            logger.debug("done");
            //logger.debug(obj);
        }
    }

    @Test
    public void foo() {
        DateTime now = new DateTime(ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))));
        now.minusMonths(1);
        int days = Days.daysBetween(new LocalDate(now), new LocalDate(now.minusMonths(1))).getDays();
        logger.debug("days "+days);
    }

    @Test
    public void bar() {
        DateTime now = new DateTime(ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))));
        DateTime yesterday = now.minusDays(1);
        logger.debug("yesterday="+yesterday);
    }
    @Test
    public void test_getInstagramUserFriendsAsList() throws Exception {
        List<InstagramUser> users = instagram.serviceUserSearch("omata.la");
        try {
            InstagramUser user = null;
            List<InstagramFriend> friends = null;
            if (users.size() > 0) {
                user = users.get(0);
                friends = instagram.serviceRequestFriends(user.getUserID(), true);
                logger.debug("");
            }

        }catch(Exception e) {
            logger.warn(e);
        }
    }

    @Test
    public void createBaseUsersForAnalytics() {
        //MongoDatabase db = MongoUtil.getMongo().getDatabase("instagram-analytics");
        MongoDatabase db = MongoUtil.getMongo("localhost", 29017).getDatabase("instagram-analytics");

        //DateTime now = new DateTime(ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))));
        //DateTimeFormatter fmt_short = DateTimeFormat.forPattern("MMddYY");
        BasicDBObject obj = new BasicDBObject();
        BasicDBList list = new BasicDBList();

        MongoCollection<Document> rootCollection;
        //rootUser = foo.getCollection(encodeKey(aUser.getUsername()) + /*"_" + fmt_short.print(now)+*/"_" + aUser.getUserID());
        rootCollection = db.getCollection("base_analytics_users");
        rootCollection.drop();
        JsonObject rootUserJson = new JsonObject();
        JsonArray listJson = new JsonArray();
        try {
            ArrayList<String> listOfUserNames = new ArrayList<>();
            // BufferedWriter writer = new BufferedWriter(new FileWriter("/Users/julian/Documents/workspace/HumansService/Omata-ListOfInstagramAccounts-Output.txt"));


            BufferedReader reader = new BufferedReader(new FileReader("/Users/julian/Documents/workspace/HumansService/src/main/webapp/WEB-INF/lib/Omata-ListOfInstagramAccounts.txt"));
            // BufferedWriter writer = new BufferedWriter(new FileWriter("/Users/julian/Documents/workspace/HumansService/Omata-ListOfInstagramAccounts-Output.txt"));
            for (String s = reader.readLine(); s != null && s.length() > 0; s = reader.readLine()) {
                if (listOfUserNames.contains(s) == true) {
                    continue;
                }
                list.add(s);
                listJson.add(new JsonPrimitive(s));
            }
            obj.append("base_analytics_users", list);

            rootCollection.insertOne(new Document().append("base_analytics_users", list));

        } catch(IOException ioe) {
            logger.warn(ioe);
        }
    }



    @Test
    public void getBaseAnalyticsUsers() {
        MongoDatabase db = MongoUtil.getMongo("localhost", 29017).getDatabase("instagram-analytics");
        MongoCollection<Document> rootCollection;
        //rootUser = foo.getCollection(encodeKey(aUser.getUsername()) + /*"_" + fmt_short.print(now)+*/"_" + aUser.getUserID());
        rootCollection = db.getCollection("base_analytics_users");
        FindIterable<Document> f = rootCollection.find();
        ArrayList<String> list = new ArrayList<>();
        Document doc = f.first();
        //noinspection unchecked
        list = doc.get("base_analytics_users", ArrayList.class);

        f.forEach(new Block<Document>() {
            @Override
            public void apply(final Document document) {
                //System.out.println(document);
                logger.debug(document);

            }
        });
    }

    @Test
    public void test_gatherAggregatePeriodData() throws BadAccessTokenException {
        MongoClient client = new MongoClient("localhost", 27017);
        InstagramAnalyticsService h_instagram = InstagramAnalyticsService.createServiceOnBehalfOfUsername("darthjulian");

        h_instagram.setDBClient(client);
        InstagramUser user = h_instagram.staticGetLocalUserBasicForUsername("omata_la");
        List list = instagram.gatherAggregatePeriodData(user, new DateTime().withDate(2015,12,15));
        logger.debug(list);
    }


    @Test
    public void test_playWithMongoAggregateQuery() {
        MongoDatabase database = MongoUtil.getMongo("localhost", 29017).getDatabase("instagram-analytics");

        AggregateIterable<Document> iterable = database.getCollection("omata_la_1572694762").aggregate(asList(
                new Document("$match", (
                        new Document("snapshot-coverage-period", new Document("$in",
                                asList("P1M","P15D","P10D","P7D","P5D","P3D","P2D","P1D"))))
                        .append("period-coverage-end-date", "010116")
                ),
                new Document("$sort", new Document("snapshot-coverage-duration-days", -1)),
                new Document("$project", new Document("snapshot-coverage-period", 1)
                        .append("snapshot-coverage-duration-days", 1)
                        .append("snapshot-date", 1)
                        .append("snapshot-run-time", 1)
                        .append("period-coverage-end-date", 1)
                        .append("period-coverage-start-date", 1)
                        .append("posts-count-in-coverage-period", 1)
                        .append("snapshot-run-time-millis", 1)
                        .append("analytics.engagement-analytics-meta.max-likes", 1)
                        .append("analytics.engagement-analytics-meta.max-comments", 1)
                        .append("analytics.engagement-analytics-meta.avg-likes", 1)
                        .append("analytics.engagement-analytics-meta.avg-comments", 1)
                )));
/*
        iterable = database.getCollection("omata_la_1572694762").aggregate(asList(
                new Document("$match", (new Document("snapshot-coverage-period",
                        new Document("$in",
                                asList("P1M","P15D","P10D","P7D","P5D","P3D","P2D","P1D"))))
                        .append("period-coverage-end-date", "010116")
                ),
                new Document("$sort", new Document("snapshot-coverage-duration-days", -1)),
                new Document("$project", new Document("snapshot-coverage-period", 1)
                        .append("snapshot-date", 1)
                        .append("snapshot-run-time", 1)
                        .append("snapshot-coverage-period", 1)
                        .append("posts-count-in-coverage-period", 1)
                        .append("analytics.engagement-analytics-meta.avg-likes", 1)
                        .append("analytics.engagement-analytics-meta.avg-comments", 1)
                )
                )
        );
        */
/*
        iterable = database.getCollection("omata_la_1572694762").aggregate(asList(
                new Document("$match", (new Document("snapshot-coverage-period", new Document("$in",asList("P7D"))))
                        .append("period-coverage-end-date", new Document("$in", asList("010116", "123115", "123015","122915")))),
                new Document("$sort", new Document("period-coverage-end-date", -1).append("snapshot-coverage-period", -1)),
                new Document("$project", new Document("snapshot-coverage-period", 1).append("snapshot-date", 1)
                        .append("snapshot-run-time", 1)
                        .append("snapshot-coverage-period", 1)
                        .append("posts-count-in-coverage-period", 1)
//                        .append("snapshot-run-time-millis", 1)
                        .append("analytics.engagement-analytics-meta.avg-likes", 1)
                        .append("analytics.engagement-analytics-meta.avg-comments", 1)
//                        .append("analytics", 1)
                )
                )
        );
*/
        ArrayList<Document> result = new ArrayList();

        iterable.forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                result.add(document);
            }
        });



        logger.debug(result);

        //.append("count", new Document("$sum", 1)))));


    }

    @Test
    public void test_getCapturedUsernames() {
        MongoClient client = new MongoClient("localhost", 27017);
        DateTime master_latest = new DateTime(2015, 12, 30, 0, 0, ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))));
        DateTime end = new DateTime(2016, 1, 2, 0, 0, ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))));
        List l = getCapturedUsernames(client, master_latest, end);
        logger.debug(l);
        addCapturedUsername(client, master_latest, end, "darthjulian");
        l = getCapturedUsernames(client, master_latest, end);
        logger.debug(l);
        finishAndMarkCompletedCapturedSet(client, master_latest, end);
    }

    @Test
    public void test_saveFollowersFor() {
        //String username = "0_just_a_test";
        String now = InstagramAnalyticsService.short_date_fmt.print(new DateTime().withChronology(ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles")))));
        MongoClient client = MongoUtil.getMongo();
        MongoDatabase database = client.getDatabase("instagram-followers");

        ArrayList<String>users = new ArrayList();
        //stinnerframeworks,paulcomponent,vulpinecc,theradavist,vernor,blacksheepcycling,iamtedking,fabian_cancellara
        // 18%

//        users.add("stinnerframeworks");
//        users.add("paulcomponent");
//        users.add("vulpinecc");
//        users.add("theradavist");
//        users.add("vernor");
//        users.add("blacksheepcycling");
//        users.add("iamtedking");

            users.add("fabian_cancellara");

        //users.add("omata_la");

        //users.add("rapha");
        //users.add("rittecycles");
        //users.add("kylebkelley");
        //users.add("johnprolly");
        //users.add("goldensaddlecyclery");
        //users.add("daisukeyanocx");

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

    public List<String> getCapturedUsernames(MongoClient client, DateTime latest, DateTime end) {
        MongoDatabase database = client.getDatabase("instagram-capture");
        MongoCollection collection = database.getCollection("captured");
        List<Document> result = new ArrayList<>();

        Document d = new Document("catalog", new Document("period-start-date", InstagramAnalyticsService.short_date_fmt.print(latest))
                .append("period-end-date",InstagramAnalyticsService.short_date_fmt.print(end)));

        //collection.updateOne(d, d);
        DateTimeFormatter fmt = DateTimeFormat.forPattern("HHmmss");
        Document x = new Document("catalog", new Document("period-start-date", InstagramAnalyticsService.short_date_fmt.print(latest))
                .append("period-end-date",InstagramAnalyticsService.short_date_fmt.print(end)));

        collection.find(x)
                .limit(1)
                .forEach((Block<Document>) document -> {
                    result.add(document);
                });

        Document r;
        if(result.size() < 1) {
            collection.insertOne(d);

            collection.find(x)
                    .limit(1)
                    .forEach((Block<Document>) document -> {
                        result.add(document);
                    });

        }
        r = result.get(0);

        List<String> l = r.get("processed", ArrayList.class);
        if(l == null) l = new ArrayList<String>();
        return l;
    }

    @Test
    public void test_findUserOverlaps() {
        ArrayList<String>users = new ArrayList();
        //stinnerframeworks,paulcomponent,vulpinecc,theradavist,vernor,blacksheepcycling,iamtedking,fabian_cancellara
        // 18%
        users.add("stinnerframeworks");
        users.add("paulcomponent");
        users.add("vulpinecc");
        users.add("theradavist");
        users.add("vernor");
        users.add("blacksheepcycling");
        users.add("iamtedking");
        users.add("fabian_cancellara");
        ArrayList<String> all = new ArrayList<>();
        //final int[] count = {0};
        final Integer[] count = {0};

        for(int i=0; i<users.size(); i++) {
            try {
                String username = users.get(i);
                InstagramUser user = InstagramAnalyticsService.staticGetLocalUserBasicForUsername(username);
                List<InstagramUserBriefly> followers = instagram.serviceRequestFollowersAsUsersBriefly(user.getUserID(),1000*15);
                followers.forEach((Consumer<InstagramUserBriefly>) follower -> {
                    count[0]++;
                    if(all.contains(follower.getUsername()) == false) {
                        all.add(follower.getUsername());
                    }
                });
            } catch(Exception exc) {
                logger.warn(exc);
            }
        }

        logger.debug("Of all "+ count[0]+" followers of "+users+", "+ all.size()+" are uniques");

    }



    public void finishAndMarkCompletedCapturedSet(MongoClient client, DateTime latest, DateTime end) {
        MongoDatabase database = client.getDatabase("instagram-capture");
        MongoCollection collection = database.getCollection("captured");
        List<Document> result = new ArrayList<>();

        DateTimeFormatter fmt = DateTimeFormat.forPattern("MMddyy-HH:mm:ss");
        Document x = new Document("catalog", new Document("period-start-date", InstagramAnalyticsService.short_date_fmt.print(latest))
                .append("period-end-date",InstagramAnalyticsService.short_date_fmt.print(end)));

        collection.find(x)
                .limit(1)
                .forEach((Block<Document>) document -> {
                    result.add(document);
                });
        if(result.size() < 1) {
            return;
        }

        Document r = result.get(0);

        UpdateResult o = collection.updateOne(new Document("_id", r.getObjectId("_id")),
                new Document("$set",
                        r.append("completed", fmt.print(new DateTime()))));

        logger.debug(o);

        if(o.wasAcknowledged()) {
            logger.info("Marked Completed "+InstagramAnalyticsService.short_date_fmt.print(latest)+" - "+InstagramAnalyticsService.short_date_fmt.print(end));
        } else {
            logger.warn("Couldn't mark as completed "+InstagramAnalyticsService.short_date_fmt.print(latest)+" - "+InstagramAnalyticsService.short_date_fmt.print(end));
        }
    }


    public void addCapturedUsername(MongoClient client, DateTime latest, DateTime end, String newUsername) {
        MongoDatabase database = client.getDatabase("instagram-capture");
        MongoCollection collection = database.getCollection("captured");
        List<Document> result = new ArrayList<>();

        Document d = new Document("catalog", new Document("period-start-date", InstagramAnalyticsService.short_date_fmt.print(latest))
                .append("period-end-date",InstagramAnalyticsService.short_date_fmt.print(end)));

        //collection.updateOne(d, d);
        Document x = new Document("catalog", new Document("period-start-date", InstagramAnalyticsService.short_date_fmt.print(latest))
                .append("period-end-date",InstagramAnalyticsService.short_date_fmt.print(end)));

        collection.find(x)
                .limit(1)
                .forEach((Block<Document>) document -> {
                    result.add(document);
                });

        Document r;
        if(result.size() < 1) {
            collection.insertOne(d);

            collection.find(x)
                    .limit(1)
                    .forEach((Block<Document>) document -> {
                        result.add(document);
                    });
            //      r = result.get(0);
        }
        r = result.get(0);

        List<String> l = r.get("processed", ArrayList.class);
        if(l == null) {
            l = new ArrayList<String>();
        }
        l.add(newUsername);

        UpdateResult o = collection.updateOne(new Document("_id", r.getObjectId("_id")),
                new Document("$set", new Document("processed", l ))
                        .append("$currentDate", new Document("lastModified", true)));

        if(o.wasAcknowledged()) {
            logger.info("Added " + newUsername);
        } else {
            logger.warn("Unacknowledge add "+newUsername);
        }
    }


    @Test
    public void test_playWithMongo() {
        MongoDatabase database = MongoUtil.getMongo("localhost", 27017).getDatabase("instagram-capture");
        DateTime latest = new DateTime(2015, 12, 15, 0, 0, ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))));
        DateTime end = new DateTime(2016, 1, 2, 0, 0, ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))));
        List<Document> result = new ArrayList<>();

        MongoCollection collection = database.getCollection("captured");
        Document d = new Document("catalog", new Document("period-start-date", InstagramAnalyticsService.short_date_fmt.print(latest))
                .append("period-end-date",InstagramAnalyticsService.short_date_fmt.print(end)));

        //collection.updateOne(d, d);
        DateTimeFormatter fmt = DateTimeFormat.forPattern("HHmmss");
        Document x = new Document("catalog", new Document("period-start-date", InstagramAnalyticsService.short_date_fmt.print(latest))
                .append("period-end-date",InstagramAnalyticsService.short_date_fmt.print(end)));

        collection.find(x)
                .limit(1)
                .forEach((Block<Document>) document -> {
                    result.add(document);
                });


        Document r;
        if(result.size() < 1) {
            collection.insertOne(d);

            collection.find(x)
                    .limit(1)
                    .forEach((Block<Document>) document -> {
                        result.add(document);
                    });
            //      r = result.get(0);
        }
        r = result.get(0);

        //r.append("processed", Arrays.asList("foo", "bar", "baz"));

        List<String> l = r.get("processed", ArrayList.class);
        if(l == null) {
            l = new ArrayList<String>();
        }
        l.addAll(Arrays.asList( fmt.print(new DateTime()), "bar", "baz"));

        UpdateResult o = collection.updateOne(new Document("_id", r.getObjectId("_id")),
                new Document("$set", new Document("processed", l ))
                        .append("$currentDate", new Document("lastModified", true)));

        logger.debug(o);
    }

    @Test
    public void test_instagramUserBasicsForAnalytics() throws Exception {

        List<String> listOfUserNames = instagram.gatherUsernamesForAnalytics();
        int count = 1;
        for (String userName : listOfUserNames) {
            try {
                logger.info("Capture Instagram user basic analytics for [" + userName + "] [" + count + " of " + listOfUserNames.size() + "]");

                count++;

                InstagramUser user_from_db = InstagramAnalyticsService.getLocalUserBasicForUsername(userName);

                if (user_from_db != null) {
                    InstagramUser user = instagram.serviceRequestUserBasicForUserID(user_from_db.getUserID());
                    // save user basic just general in the user collection
                    instagram.saveUserBasicForAnalytics(user);
                    // save user basic just general in the user collection
                    instagram.saveUserBasic(user);

                } else {
                    List<InstagramUser> users = instagram.serviceUserSearch(userName);
                    if (users != null) {
                        users.removeIf(p -> (p.getUsername().equalsIgnoreCase(userName) == false));
                        if (users.size() == 1) {
                            // save user basic snapshot for analytics
                            instagram.saveUserBasicForAnalytics(users.get(0));
                            // save user basic just general in the user collection
                            instagram.saveUserBasic(users.get(0));
                        }
                    }

                }
            } catch (Exception e) {
                logger.warn("", e);
            }
            try {
                Thread.sleep(3000l);        //delay the code for 3 secs to account for rate limits
            } catch (InterruptedException ex) {  //and handle the exceptions
                //Thread.currentThread().interrupt();
                logger.warn("", ex);
            }

        }
    }

    @Test
    public void test_getSomething() {
        List<String> a_1 = instagram.getListOfEndemicAnalyticsCollectionNames();
        String str_date = instagram.short_date_fmt.print(new DateTime().minusDays(1));

        List<Document>docs = instagram.getListOfInstagramAnalyticsDocumentsByDate(
                a_1,
                str_date,
                InstagramAnalyticsService.THREE_DAY_P);

        JsonElement e = instagram.getRankingByEngagementAnalyticFor(docs, "rapha", "avg-likes", str_date, InstagramAnalyticsService.THREE_DAY_P);

        logger.debug("Foo");
    }

    @Test
    public void test_gatherUsernamesForAnalytics() throws Exception {
        List<String> listOfUserNames = instagram.gatherUsernamesForAnalytics();
        //logger.debug(listOfUserNames);
    }

    @Test
    public void test_split() {
        String aUsernames = "darthjulian,stinnerframeworks,blacksheepcycling";
        String[] u = aUsernames.split(",");
        logger.debug("u="+u);
    }


    @Test
    public void test_serviceRequestUserBasicForId() {
        // 1572694762
        // 268339146
        try {
           InstagramUser user = instagram.serviceRequestUserBasicForUserID("1572694762");
            logger.debug(user);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gathers posts for an account over a period and sorts by number of likes (greater to smaller) to see if there
     * is any rhyme or reason as tto when posting gathers the most likes..
     *
     * @throws Exception
     */
    @Test
    public void test_gatherAggregatePeriodDataForPeriod() throws Exception {
        MongoClient client = new MongoClient("localhost", 29017);
        InstagramAnalyticsService h_instagram = InstagramAnalyticsService.createServiceOnBehalfOfUsername("darthjulian");

        h_instagram.setDBClient(client);
        InstagramUser me = h_instagram.staticGetLocalUserBasicForUsername("stinnerframeworks");

        List<Document> docs = h_instagram.gatherAggregatePeriodDataForPeriod(me, new DateTime().withDate(2016, 1, 16), "P1M");
        Document d1 = docs.get(0);
       net.minidev.json.JSONArray foo = JsonPath.read(d1.toJson(), "$.analytics.status-json");
        ArrayList list = new ArrayList();
        for(int i=0; i<foo.size(); i++) {
            //DateTime d = new DateTime().withMillis(foo.get("created_time"))
            LinkedHashMap h = (LinkedHashMap) foo.get(i);
            Integer t = (Integer) ((LinkedHashMap)(h)).get("created_time");
            if(t != null) {
                LinkedHashMap<String, DateTime> x = new LinkedHashMap<>();
               x.put("created_datetime", new DateTime(t.longValue() * 1000l));
                //h.put("created_datetime", new DateTime(t.longValue() * 1000l));
                h.putAll(x);
            }
            list.add(h);
        }
        Collections.sort(list, new Comparator<LinkedHashMap>() {
                    public int compare(LinkedHashMap l1, LinkedHashMap r1) {
                        LinkedHashMap l2 = (LinkedHashMap) l1.get("likes");
                        LinkedHashMap r2 = (LinkedHashMap) r1.get("likes");
                        Integer x1 = (Integer) l2.get("count");
                        Integer x2 = (Integer) r2.get("count");


                        return x2.compareTo(x1);
                    }
                });

        logger.debug(list);
    }


    @Test
    public void test_getListOfEndemicInstagramAnalyticsCollections() {
        BiPredicate<String, String> p1 = (a, b) -> a.equalsIgnoreCase(b);
        ArrayList<String> a = instagram.getListOfEndemicCyclistsInstagramAnalyticsUsernames();
        ArrayList<String> b = instagram.getListOfEndemicCylingInstagramAnalyticsUsernames();
        ArrayList<String> c = new ArrayList<>();

        c.addAll(b);
        c.addAll(a);

        c.removeIf(s -> Collections.frequency(c, s) > 1);
        ArrayList<String> result = new ArrayList<>();
        c.stream()
                .sorted((e1, e2) -> e1.compareTo(e2))
                        .forEach(e -> { result.add(e); });

        logger.debug(result);

    }


    @Test
    public void test_omataFollowers() {
        List listOfUserNames = new ArrayList<>();
        InstagramUser me = InstagramAnalyticsService.staticGetLocalUserBasicForUsername("omata_la");
        List<InstagramUser> follows = instagram.getInstagramUserFriendsAsList(me);
        follows.forEach((instagramUser) -> {
            String a_follow = instagramUser.getUsername();
            if (listOfUserNames.contains(a_follow) == false) {
                logger.info("Adding ["+a_follow+"] via "+me.getUsername()+" to list..("+listOfUserNames.size()+")");

                listOfUserNames.add(a_follow);
            }
        });
        listOfUserNames.forEach((name) -> {
            logger.info(name);
            //System.out.println(name);
        });
    }


    @Test
    public void playWithJoda() {
        Period p = new Period().withDays(1);
        DateTime d = new DateTime().minus(p);
        logger.debug(d);

        Period p1 = new Period().withDays(30);
        Period m1 = new Period().withMonths(1);

        int days30 = p1.getDays();
        int days1m = m1.getDays();

        DateTime d2 = new DateTime().minus(p1);
        DateTime d3 = new DateTime().minus(m1);

        DateTime latest = new DateTime(ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles")))).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
        DateTime huh = latest.minusDays(1);

        Period period = new Period().withDays(1);

        Date earliestDate = latest.minus(period).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).toDate();

        logger.debug(earliestDate);
        logger.debug("done");
    }


@Test
public void lambdaTest() {
    //with type declaration
    MathOperation addition = (int a, int b) -> a + b;

    //with out type declaration
    MathOperation subtraction = (a, b) -> a - b;

    //with return statement along with curly braces
    MathOperation multiplication = (int a, int b) -> { return a * b; };

    //without return statement and without curly braces
    MathOperation division = (int a, int b) -> a / b;

    int x = 100;

    System.out.println("10 + 5 = " + operate(x, 5, addition));
    System.out.println("10 - 5 = " + operate(10, 5, subtraction));
    System.out.println("10 x 5 = " + operate(10, 5, multiplication));
    System.out.println("10 / 5 = " + operate(10, 5, division));

    //with parenthesis
    GreetingService greetService1 = message ->
            System.out.println("Hello " + message);

    //without parenthesis
    GreetingService greetService2 = (message) ->
            System.out.println("Hello " + message);

    greetService1.sayMessage("Mahesh");
    greetService2.sayMessage("Suresh");
}

interface MathOperation {
    int operation(int a, int b);
}

interface GreetingService {
    void sayMessage(String message);
}

    private int operate(int a, int b, MathOperation mathOperation){
        return mathOperation.operation(a, b);
    }


}