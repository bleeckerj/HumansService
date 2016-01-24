package com.nearfuturelaboratory.humans.rest;

import com.google.gson.*;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.mapper.JsonSmartMappingProvider;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.nearfuturelaboratory.humans.exception.BadAccessTokenException;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramUser;
import com.nearfuturelaboratory.humans.scheduler.ScheduledInstagramAnalyticsJob;
import com.nearfuturelaboratory.humans.service.InstagramAnalyticsService;
import com.nearfuturelaboratory.humans.util.MongoUtil;
import com.nearfuturelaboratory.humans.util.MyObjectIdSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.DateTimeZone;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Created by julian on 12/5/15.
 */

@Path("/analytics")
public class InstagramAnalyticsEndpoint {

    final static Logger logger = LogManager.getLogger(com.nearfuturelaboratory.humans.rest.InstagramAnalyticsEndpoint.class);
    JsonObject invalid_user_error_response;
    JsonObject success_response;
    JsonObject fail_response;
    JsonObject no_such_human_for_user;

    @Context
    ServletContext context;
    Gson gson;

    public InstagramAnalyticsEndpoint() {
        gson = new GsonBuilder().setDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz").registerTypeAdapter(ObjectId.class, new MyObjectIdSerializer()).create();
        invalid_user_error_response = new JsonObject();
        invalid_user_error_response.addProperty("result", "error");
        invalid_user_error_response.addProperty("message", "invalid user");

        success_response = new JsonObject();
        success_response.addProperty("result", "success");

        fail_response = new JsonObject();
        fail_response.addProperty("result", "fail");
    }

    @GET
    @Path("/top/{count}/by/{key-suffix}/date/{date}")
    @Produces({"application/json"})
    public Response getTopAnalyticsByKeySuffixAndDate(@PathParam("count") int aCount,
                                                      @PathParam("key-suffix") String aKeySuffix,
                                                      @PathParam("date") String aDate,
                                                      @Context HttpServletRequest request,
                                                      @Context HttpServletResponse response) {

        InstagramAnalyticsService instagram;
        JsonObject r = new JsonObject();

        try {
            instagram = InstagramAnalyticsService.createServiceOnBehalfOfUsername("darthjulian");

            List<String> collectionNames = instagram.getListOfInstagramAnalyticsCollections();
            List<Document> docs = instagram.getListOfInstagramAnalyticsDocumentsByDate(collectionNames, aDate);
            docs = sortByKeySuffix(docs, aKeySuffix);
//            Collections.sort(docs, new Comparator<Document>() {
//                public int compare(Document d1, Document d2) {
//                    d1.get("analytics");
//
//                    try {
//                        Double n1 = JsonPath.parse(d1.toJson()).read("$.analytics.engagement-analytics-meta." + aKeySuffix, Double.class);
//                        Double n2 = JsonPath.parse(d2.toJson()).read("$.analytics.engagement-analytics-meta." + aKeySuffix, Double.class);
//                        return n2.compareTo(n1);
//
//                        // logger.debug(n1+" "+n2);
//                    } catch (Exception e) {
//                        logger.error(e);
//                        //logger.debug(d1.toJson()+" "+d2.toJson());
//                        BigDecimal n1 = JsonPath.read(d1.toJson(), "$.analytics.engagement-analytics-meta." + aKeySuffix);
//                        BigDecimal n2 = JsonPath.read(d2.toJson(), "$.analytics.engagement-analytics-meta." + aKeySuffix);
//                        return n2.compareTo(n1);
//
//                    }
//                }
//            });

            JsonParser parser = new JsonParser();
            JsonArray a = new JsonArray();
            List<Document> result = docs.stream().limit(aCount).collect(Collectors.toList());
            result.forEach((document) -> {
                String json = document.toJson();
                JsonObject obj = parser.parse(json).getAsJsonObject();
                a.add(obj);
            });
            r.addProperty("Name", "Top " + aCount + " by " + aKeySuffix + " on period ending " + aDate);
            r.add("reports", a);

        } catch (BadAccessTokenException bate) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(fail_response.toString()).build();
        }

        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(r.toString()).build();


    }


    @GET
    @Path("/top/{count}/by/{key-suffix}/date/{date}/user/{user}")
    @Produces({"application/json"})
    public Response getTopAnalyticsRangeByKeySuffixByDateByUser(@PathParam("count") int aCount,
                                                                @PathParam("key-suffix") String aKeySuffix,
                                                                @PathParam("date") String aDate,
                                                                @PathParam("user") String aUsername,
                                                                @Context HttpServletRequest request,
                                                                @Context HttpServletResponse response)

    {
        InstagramAnalyticsService instagram;
        JsonObject r = new JsonObject();
        try {
            instagram = InstagramAnalyticsService.createServiceOnBehalfOfUsername("darthjulian");
            List<Document> result = new ArrayList<Document>();
            List<String> collectionNames = instagram.getListOfInstagramAnalyticsCollections();
            List<Document> docs = instagram.getListOfInstagramAnalyticsDocumentsByDate(collectionNames, aDate);
            docs = sortByKeySuffix(docs, aKeySuffix);
            int index = -1;
            for(int i = 0; i<docs.size(); i++) {
                if (aUsername.equalsIgnoreCase(docs.get(i).getString("username"))) {
                    // mark the index
                    index = i;
                    int fromIndex = i-aCount/2;
                    int toIndex = i+aCount/2;
                    if(fromIndex < 0) fromIndex = 0;
                    if(toIndex > docs.size()-1) toIndex = docs.size()-1;
                    result = docs.subList(fromIndex, toIndex);
                    break;
                }
            }

            JsonParser parser = new JsonParser();
            JsonArray a = new JsonArray();
            result.forEach((document) -> {
                String json = document.toJson();
                JsonObject obj = parser.parse(json).getAsJsonObject();
                a.add(obj);
            });
            r.addProperty("Name", "Top "+aCount+" around #"+index+" "+aUsername+" ranked by "+aKeySuffix+" on period ending date "+aDate);
            r.add("reports", a);

        } catch(BadAccessTokenException bate) {
            logger.warn("", bate);
            fail_response.addProperty("message", bate.getMessage());
            return Response.status(Response.Status.EXPECTATION_FAILED).type(MediaType.APPLICATION_JSON).entity(fail_response.toString()).build();
        }
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(r.toString()).build();

    }


    @GET @Path("/top/{count-from}-{count-to}/by/{key-suffix}/date/{date}")
    @Produces({"application/json"})
    public Response getTopAnalyticsByKeySuffix(@PathParam("count-from") int aCountFrom,
                                               @PathParam("count-to") int aCountTo,
                                               @PathParam("key-suffix") String aKeySuffix,
                                               @PathParam("date") String aDate,
                                               @Context HttpServletRequest request,
                                               @Context HttpServletResponse response)
    {
        InstagramAnalyticsService instagram;
        JsonObject r = new JsonObject();

        try {
            instagram = InstagramAnalyticsService.createServiceOnBehalfOfUsername("darthjulian");

            List<String> collectionNames = instagram.getListOfInstagramAnalyticsCollections();
            List<Document> docs = instagram.getListOfInstagramAnalyticsDocumentsByDate(collectionNames, aDate);
            docs = sortByKeySuffix(docs, aKeySuffix);

            List<Document> trunc = new ArrayList<Document>();

            if(aCountTo > docs.size()) aCountTo = docs.size();
            if(aCountFrom < 1) aCountFrom = 1;

            for(int i=aCountFrom-1; i<aCountTo; i++) {
                trunc.add(docs.get(i));
            }

            JsonParser parser = new JsonParser();
            JsonArray a = new JsonArray();
            //List<Document> result = docs.stream().limit(aCountTo).collect(Collectors.toList());



            trunc.forEach((document) -> {
                String json = document.toJson();
                JsonObject obj = parser.parse(json).getAsJsonObject();
                a.add(obj);
            });
            r.addProperty("Name", "Top From "+aCountFrom+" to "+aCountTo  +" by "+aKeySuffix+" on period ending day of year "+aDate);
            r.addProperty("top-from", aCountFrom);
            r.addProperty("top-to", aCountTo);
            r.add("reports", a);

        } catch(BadAccessTokenException bate) {
            logger.warn("",bate);
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(fail_response.toString()).build();
        } catch(Exception e) {
            logger.warn("",e);
            return Response.status(Response.Status.EXPECTATION_FAILED).type(MediaType.APPLICATION_JSON).entity(fail_response.toString()).build();

        }

        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(r.toString()).build();
    }

    protected Double convertToDouble(Integer i) {
        return new Double(i.doubleValue());
    }

    @GET @Path("/top/{count}/by/{key-suffix}/day-of-year/{day-of-year}")
    @Produces({"application/json"})
    public Response getTopAnalyticsByKeySuffix(@PathParam("count") int aCount,
                                               @PathParam("key-suffix") String aKeySuffix,
                                               @PathParam("day-of-year") int aDayOfYear,
                                               @Context HttpServletRequest request,
                                               @Context HttpServletResponse response)
    {
        InstagramAnalyticsService instagram;
        JsonObject r = new JsonObject();

        try {
            instagram = InstagramAnalyticsService.createServiceOnBehalfOfUsername("darthjulian");

            List<String> collectionNames = instagram.getListOfInstagramAnalyticsCollections();
            List<Document> docs = instagram.getListOfInstagramAnalyticsDocumentsByDayOfYear(collectionNames, aDayOfYear);

            docs = sortByKeySuffix(docs, aKeySuffix);
//            Collections.sort(docs,new Comparator<Document>() {
//                public int compare(Document d1, Document d2) {
//                    Object f = JsonPath.read(d1.toJson(), "$.analytics.engagement-analytics-meta."+aKeySuffix);
//
//                    Double n1 = JsonPath.read( d1.toJson(), "$.analytics.engagement-analytics-meta."+aKeySuffix);
//                    Double n2 = JsonPath.read( d2.toJson(), "$.analytics.engagement-analytics-meta."+aKeySuffix);
//                    logger.debug(n1+" "+n2);
//                    return n2.compareTo(n1);
//                }
//            });
            JsonParser parser = new JsonParser();
            JsonArray a = new JsonArray();
            List<Document> result = docs.stream().limit(aCount).collect(Collectors.toList());
            result.forEach((document) -> {
                String json = document.toJson();
                JsonObject obj = parser.parse(json).getAsJsonObject();
                a.add(obj);
            });
            r.addProperty("Name", "Top "+aCount+" by "+aKeySuffix+" on period ending day of year "+aDayOfYear);
            r.add("reports", a);

        } catch(BadAccessTokenException bate) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(fail_response.toString()).build();
        }

        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(r.toString()).build();
    }



    @GET @Path("/top/{count-from}-{count-to}/by/{key-suffix}/day-of-year/{day-of-year}")
    @Produces({"application/json"})
    public Response getTopAnalyticsByKeySuffix(@PathParam("count-from") int aCountFrom,
                                               @PathParam("count-to") int aCountTo,
                                               @PathParam("key-suffix") String aKeySuffix,
                                               @PathParam("day-of-year") int aDayOfYear,
                                               @Context HttpServletRequest request,
                                               @Context HttpServletResponse response)
    {
        InstagramAnalyticsService instagram;
        JsonObject r = new JsonObject();

        try {
            instagram = InstagramAnalyticsService.createServiceOnBehalfOfUsername("darthjulian");

            List<String> collectionNames = instagram.getListOfInstagramAnalyticsCollections();
            List<Document> docs = instagram.getListOfInstagramAnalyticsDocumentsByDayOfYear(collectionNames, aDayOfYear);


            Collections.sort(docs,new Comparator<Document>() {
                public int compare(Document d1, Document d2) {
                    Object f = JsonPath.read(d1.toJson(), "$.analytics.engagement-analytics-meta."+aKeySuffix);

                    Double n1 = JsonPath.read( d1.toJson(), "$.analytics.engagement-analytics-meta."+aKeySuffix);
                    Double n2 = JsonPath.read( d2.toJson(), "$.analytics.engagement-analytics-meta."+aKeySuffix);
                    //logger.debug(n1+" "+n2);
                    return n2.compareTo(n1);
                }
            });

            List<Document> trunc = new ArrayList<Document>();

            if(aCountTo > docs.size()) aCountTo = docs.size();
            if(aCountFrom < 1) aCountFrom = 1;

            for(int i=aCountFrom-1; i<aCountTo; i++) {
                trunc.add(docs.get(i));
            }

            JsonParser parser = new JsonParser();
            JsonArray a = new JsonArray();
            //List<Document> result = docs.stream().limit(aCountTo).collect(Collectors.toList());



            trunc.forEach((document) -> {
                String json = document.toJson();
                JsonObject obj = parser.parse(json).getAsJsonObject();
                a.add(obj);
            });
            r.addProperty("Name", "Top From "+aCountFrom+" to "+aCountTo  +" by "+aKeySuffix+" on period ending day of year "+aDayOfYear);
            r.add("reports", a);

        } catch(BadAccessTokenException bate) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(fail_response.toString()).build();
        }

        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(r.toString()).build();
    }



    @GET @Path("/get/feed-for-day/{date}")
    @Produces("application/json")
    public Response getFeedForDay(@PathParam("date") String date,
                                  @Context HttpServletRequest request,
                                  @Context HttpServletResponse response)
    {
        JsonArray a = new JsonArray();

        try {
            DateTimeFormatter f = DateTimeFormat.forPattern("MMddyy");
            InstagramAnalyticsService instagram = InstagramAnalyticsService.createServiceOnBehalfOfUsername("darthjulian");
            List<Document> docs = instagram.getAllStatusForDateOrdered(f.parseDateTime(date));
            // DateTime d = f.parseDateTime(date);
/*
            docs.removeIf(doc -> {
                int t = doc.getInteger("created_time");
                DateTime dt = new DateTime(t*1000l);
                int c = DateTimeComparator.getDateOnlyInstance().compare(dt, d);
                if(c == 0) {
                    return false;
                } else {
                    return true;
                }
            });
*/
            docs.forEach((Consumer<? super Document>) doc -> {
                //d.getDate("created_time");
                //logger.debug("Time -> "+fmt_short.print(d.getInteger("created_time").longValue()));
                //DateTime dt = new DateTime(new Date(d.getInteger("created_time").longValue()*1000));
                a.add(new JsonParser().parse(doc.toJson()));
            });



        } catch(BadAccessTokenException bate) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(fail_response.toString()).build();

        }

        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(a.toString()).build();
    }

    @GET  @Path("/get/user/{username}")
    @Produces({"application/json"})
    public Response getAnalyticsByUsername(@PathParam("username") String aUsername,
                                           @Context HttpServletRequest request,
                                           @Context HttpServletResponse response)
    {
        // InstagramUser user = Inst
        DateTime yesterday = new DateTime(ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))));
        yesterday = yesterday.minusDays(1);
        ArrayList<Document> result = new ArrayList<>();
        JsonArray a = new JsonArray();
        try {
            InstagramAnalyticsService instagram = InstagramAnalyticsService.createServiceOnBehalfOfUsername("darthjulian");
            //instagram.getListOfInstagramAnalyticsDocuments()
            MongoDatabase db = MongoUtil.getMongo().getDatabase("instagram-analytics");
            InstagramUser user = instagram.localUserSearch(aUsername);
            String coll_name = InstagramAnalyticsService.encodeKey(user.getUsername())+"_"+user.getUserID()+"_snapshot-counts";
            //FindIterable<Document> docs_i = db.getCollection(coll_name).find(eq("snapshot-day-of-year", DateTimeFormat.forPattern("D").print(yesterday)));
            FindIterable<Document> docs_i = db.getCollection(coll_name).find();
            docs_i.forEach((Block<Document>) document -> {
                result.add(document);
            });

        } catch(BadAccessTokenException bate) {
            //return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE);
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(fail_response.toString()).build();
        }
        //Document r = result.get(0);
        //r.toJson();
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(new Gson().toJson(result)).build();
    }


    @GET @Path("/admin/gettyup/runuserbasic")
    @Produces({"application/json"})
    public Response getGettyupGetUserBasic(@Context HttpServletRequest request,
                                           @Context HttpServletResponse response)
    {

        try {
//            logger.info("gettyup/update/friends for "+user.getUsername());

            SchedulerFactory sf = new StdSchedulerFactory();
            Scheduler sched = sf.getScheduler();
            sched.start();

            JobDataMap data = new JobDataMap();
//            data.put("user", user);
//            data.put("access_token", access_token);

            JobDetail job = newJob(ScheduledInstagramAnalyticsJob.class)
                    .withIdentity("runUserBasic", "group."+context)
                    .setJobData(data)
                    .build();

            //sched.start();

            ScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.
                    simpleSchedule().
                    withRepeatCount(0);

            //JobDetail details = JobBuilder.newJob().setJobData(data).build();
            Trigger trigger = newTrigger()
                    .withDescription("runUserBasic group."+context)
                    .withIdentity("runUserBasic group." + context)
                    .withSchedule(scheduleBuilder).forJob(job)
                    .startNow().build();

            sched.scheduleJob(job, trigger);
            success_response.addProperty("message", "started job "+trigger.getDescription());
            //return Response.ok("{}", MediaType.APPLICATION_JSON).build();
        }catch(Exception e) {
            String msg = "Exception attempting to schedule runUserBasic";
            logger.error(e);
            logger.error(msg);
            fail_response.addProperty("message", msg);
            return Response.ok(fail_response.toString(), MediaType.APPLICATION_JSON).build();
        }


        return Response.ok().entity(success_response.toString()).type(MediaType.APPLICATION_JSON).build();
    }


    protected List<Document> sortByKeySuffix(List<Document> docs, String aKeySuffix)
    {

        Collections.sort(docs,new Comparator<Document>() {
            public int compare(Document d1, Document d2) {

                Double n1, n2;

                Object f1 = JsonPath.read(d1.toJson(), "$.analytics.engagement-analytics-meta." + aKeySuffix);
                Object f2 = JsonPath.read(d2.toJson(), "$.analytics.engagement-analytics-meta." + aKeySuffix);
                if(f1.getClass().getName().equalsIgnoreCase("java.lang.Integer")) {
                    n1 = convertToDouble((Integer)f1);
                } else {
                    n1 = (Double)f1;
                }
                if(f2.getClass().getName().equalsIgnoreCase("java.lang.Integer")) {
                    n2 = convertToDouble((Integer)f2);
                } else {
                    n2 = (Double)f2;
                }
                return n2.compareTo(n1);
            }
        });
        return docs;
    }

}
