package com.nearfuturelaboratory.humans.rest;

import com.google.gson.*;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.mapper.JsonSmartMappingProvider;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.nearfuturelaboratory.humans.exception.BadAccessTokenException;
import com.nearfuturelaboratory.humans.exception.UsernameNotFoundException;
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
import org.joda.time.Period;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodParser;
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
    static InstagramAnalyticsService instagram;

    static {
        try {
            instagram = InstagramAnalyticsService.createServiceOnBehalfOfUsername("darthjulian");
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    @Context
    ServletContext context;
    Gson gson;


    public InstagramAnalyticsEndpoint() throws BadAccessTokenException {
        gson = new GsonBuilder().setDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz").registerTypeAdapter(ObjectId.class, new MyObjectIdSerializer()).create();
        invalid_user_error_response = new JsonObject();
        invalid_user_error_response.addProperty("result", "error");
        invalid_user_error_response.addProperty("message", "invalid user");

        success_response = new JsonObject();
        success_response.addProperty("result", "success");

        fail_response = new JsonObject();
        fail_response.addProperty("result", "fail");

        //instagram = InstagramAnalyticsService.createServiceOnBehalfOfUsername("darthjulian");

    }


    @GET
    @Path("/top/{count}/by/{key-suffix}/date/{date}")
    @Produces({"application/json"})
    public Response getTopAnalyticsByKeySuffixAndDate(@PathParam("count") int aCount,
                                                      @PathParam("key-suffix") String aKeySuffix,
                                                      @PathParam("date") String aDate,
                                                      @Context HttpServletRequest request,
                                                      @Context HttpServletResponse response) {
        return getTopAnalyticsByKeySuffixAndDate(aCount, aKeySuffix, aDate, InstagramAnalyticsService.ONE_DAY, request, response);
    }

    @GET
    @Path("/rank/by/{key-suffix}/user/{username}/date/{date}/period/{period}")
    @Produces({"application/json"})
    public Response getRankingByKeySuffixAndDate(@PathParam("key-suffix") String aKeySuffix,
                                                 @PathParam("username") String aUsername,
                                                 @PathParam("date") String aDate,
                                                 @PathParam("period") String aPeriod,
                                                 @Context HttpServletRequest request,
                                                 @Context HttpServletResponse response) {

        JsonObject r = new JsonObject();
        try {
            instagram = InstagramAnalyticsService.createServiceOnBehalfOfUsername("darthjulian");
            List<String> collectionNames = instagram.getListOfInstagramAnalyticsCollections();
            List<Document> docs = instagram.getListOfInstagramAnalyticsDocumentsByDate(collectionNames, aDate, periodForString(aPeriod));
            docs = sortByKeySuffix(docs, aKeySuffix);
            Document doc = new Document();
            int rank = 0;
            JsonParser parser = new JsonParser();
            JsonArray a = new JsonArray();
            //docs.stream().anyMatch()
            for (int i = 0; i < docs.size(); i++) {
                Document document = docs.get(i);
                String f = JsonPath.read(document.toJson(), "username");
                if (f != null && f.equalsIgnoreCase(aUsername)) {
                    rank = i + 1;
                    String json = document.toJson();
                    JsonObject obj = parser.parse(json).getAsJsonObject();
                    a.add(obj);
                }
            }
            r.addProperty("Name", aUsername + " #" + rank + "/" + docs.size() + " by " + aKeySuffix + " on period " + aPeriod + " ending " + aDate);
            r.addProperty("period", aPeriod);
            r.addProperty("rank", rank);
            r.addProperty("of", docs.size());
            r.addProperty("by", aKeySuffix);
            r.addProperty("period", aPeriod);
            r.addProperty("ending", aDate);
            r.add("report", a);

        } catch (BadAccessTokenException bate) {
            fail_response.addProperty("message", bate.getMessage());
            return Response.status(Response.Status.EXPECTATION_FAILED).type(MediaType.APPLICATION_JSON).entity(fail_response.toString()).build();
        }
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(r.toString()).build();
    }


    //   //                    /period/{period : P1D|P7D|P1M}")
    @GET
    @Path("/top/{count}/by/{key-suffix}/date/{date}/period/{period}")
    @Produces({"application/json"})
    public Response getTopAnalyticsByKeySuffixAndDate(@PathParam("count") int aCount,
                                                      @PathParam("key-suffix") String aKeySuffix,
                                                      @PathParam("date") String aDate,
                                                      @PathParam("period") String aPeriod,
                                                      @Context HttpServletRequest request,
                                                      @Context HttpServletResponse response) {

        //InstagramAnalyticsService instagram;
        JsonObject r = new JsonObject();

        try {
            instagram = InstagramAnalyticsService.createServiceOnBehalfOfUsername("darthjulian");
            List<String> collectionNames = instagram.getListOfInstagramAnalyticsCollections();
            List<Document> docs = instagram.getListOfInstagramAnalyticsDocumentsByDate(collectionNames, aDate, periodForString(aPeriod));
            docs = sortByKeySuffix(docs, aKeySuffix);

            JsonParser parser = new JsonParser();
            JsonArray a = new JsonArray();
            List<Document> result = docs.stream().limit(aCount).collect(Collectors.toList());
            result.forEach((document) -> {
                String json = document.toJson();
                JsonObject obj = parser.parse(json).getAsJsonObject();
                a.add(obj);
                // a.add(document);
            });
            r.addProperty("Name", "Top " + aCount + "/" + docs.size() + " by " + aKeySuffix + " on period " + aPeriod + " ending " + aDate);
            r.addProperty("period", aPeriod);
            r.addProperty("from", "1");
            r.addProperty("to", aCount);
            r.addProperty("of", docs.size());
            r.addProperty("by", aKeySuffix);
            r.addProperty("period", aPeriod);
            r.addProperty("ending", aDate);
            r.add("reports", a);

        } catch (BadAccessTokenException bate) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(fail_response.toString()).build();
        }

        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(r.toString()).build();


    }


    @GET
    @Path("/top/{count}/by/{key-suffix}/user/{username}/date/{date}")
    @Produces({"application/json"})
    public Response getTopAnalyticsRangeByKeySuffixByDateByUser(@PathParam("count") int aCount,
                                                                @PathParam("key-suffix") String aKeySuffix,
                                                                @PathParam("date") String aDate,
                                                                @PathParam("username") String aUsername,
                                                                @Context HttpServletRequest request,
                                                                @Context HttpServletResponse response)

    {
        return getTopAnalyticsRangeByKeySuffixByDateByUser(aCount, aKeySuffix, aDate, aUsername, InstagramAnalyticsService.ONE_DAY, request, response);
    }

    @GET
    @Path("/top/{count}/by/{key-suffix}/user/{username}/date/{date}/period/{period}")
    @Produces({"application/json"})
    public Response getTopAnalyticsRangeByKeySuffixByDateByUser(@PathParam("count") int aCount,
                                                                @PathParam("key-suffix") String aKeySuffix,
                                                                @PathParam("date") String aDate,
                                                                @PathParam("username") String aUsername,
                                                                @PathParam("period") String aPeriod,
                                                                @Context HttpServletRequest request,
                                                                @Context HttpServletResponse response)

    {
        //InstagramAnalyticsService instagram;
        JsonObject r = new JsonObject();
        try {
            logger.debug("Starting Endpoint " + aCount + " " + aKeySuffix + " " + aDate + " " + aUsername + " " + aPeriod);
            instagram = InstagramAnalyticsService.createServiceOnBehalfOfUsername("darthjulian");
            List<Document> result = new ArrayList<Document>();
            List<String> collectionNames = instagram.getListOfInstagramAnalyticsCollections();
            List<Document> docs = instagram.getListOfInstagramAnalyticsDocumentsByDate(collectionNames, aDate, periodForString(aPeriod));
            docs = sortByKeySuffix(docs, aKeySuffix);
            int index = -1;
            int fromIndex = 0;
            int toIndex = 0;
            for (int i = 0; i < docs.size(); i++) {
                if (aUsername.equalsIgnoreCase(docs.get(i).getString("username"))) {
                    // mark the index
                    index = i;

                    fromIndex = i - aCount / 2;
                    toIndex = i + aCount / 2;
                    if (fromIndex < 0) fromIndex = 0;
                    if (toIndex > docs.size() - 1) toIndex = docs.size() - 1;

                    if (aCount % 2 == 1) {
                        toIndex += 1;
                    } else {
                        toIndex -= 1;
                    }

                    if ((toIndex - fromIndex) < aCount) {
                        toIndex = fromIndex + aCount;
                    }

                    if (toIndex > docs.size() - 1) {
                        toIndex = docs.size() - 1;
                    }

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
            r.addProperty("Name", "Top " + aCount + " around #" + (index + 1) + "/" + docs.size() + " " + aUsername + " ranked by " + aKeySuffix + " on period " + aPeriod + " ending date " + aDate);
            r.addProperty("from", fromIndex + 1);
            r.addProperty("to", toIndex - 1);
            r.addProperty("of", docs.size());
            r.addProperty("by", aKeySuffix);
            r.addProperty("period", aPeriod);
            r.addProperty("ending", aDate);
            r.add("reports", a);
            //r.add("start_index", new JsonPrimitive(String.valueOf(fromIndex)));

        } catch (BadAccessTokenException bate) {
            logger.warn("", bate);
            fail_response.addProperty("message", bate.getMessage());
            return Response.status(Response.Status.EXPECTATION_FAILED).type(MediaType.APPLICATION_JSON).entity(fail_response.toString()).build();
        }
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(r.toString()).build();

    }

    ///period/{period : ^P1D$|^P7D$|^P1M$}

    @GET
    @Path("/top/{count-from}-{count-to}/by/{key-suffix}/date/{date}")
    @Produces({"application/json"})
    public Response getTopAnalyticsByKeySuffix(@PathParam("count-from") int aCountFrom,
                                               @PathParam("count-to") int aCountTo,
                                               @PathParam("key-suffix") String aKeySuffix,
                                               @PathParam("date") String aDate,
                                               @Context HttpServletRequest request,
                                               @Context HttpServletResponse response) {

        return getTopAnalyticsByKeySuffix(aCountFrom, aCountTo, aKeySuffix, aDate, "P1D", request, response);
    }


    @GET
    @Path("/top/{count-from}-{count-to}/by/{key-suffix}/date/{date}/period/{period}")
    @Produces({"application/json"})
    public Response getTopAnalyticsByKeySuffix(@PathParam("count-from") int aCountFrom,
                                               @PathParam("count-to") int aCountTo,
                                               @PathParam("key-suffix") String aKeySuffix,
                                               @PathParam("date") String aDate,
                                               @PathParam("period") String aPeriod,
                                               @Context HttpServletRequest request,
                                               @Context HttpServletResponse response) {
        //InstagramAnalyticsService instagram;
        JsonObject r = new JsonObject();
        //String aPeriod = "P1D";
        try {
            instagram = InstagramAnalyticsService.createServiceOnBehalfOfUsername("darthjulian");

            List<String> collectionNames = instagram.getListOfInstagramAnalyticsCollections();

            List<Document> docs;

//            if(aPeriod.equalsIgnoreCase(InstagramAnalyticsService.ONE_DAY) ||
//                    aPeriod.equalsIgnoreCase(InstagramAnalyticsService.SEVEN_DAYS) ||
//                    aPeriod.equalsIgnoreCase(InstagramAnalyticsService.ONE_MONTH)) {
//
//                docs = instagram.getListOfInstagramAnalyticsDocumentsByDate(collectionNames, aDate, periodForString(aPeriod));
//
//            } else {
//                docs = instagram.getListOfInstagramAnalyticsDocumentsByDate(collectionNames, aDate, periodForString(aPeriod));
//            }

            docs = instagram.getListOfInstagramAnalyticsDocumentsByDate(collectionNames, aDate, periodForString(aPeriod));


            docs = sortByKeySuffix(docs, aKeySuffix);

            List<Document> trunc = new ArrayList<Document>();

            if (aCountTo > docs.size()) aCountTo = docs.size();
            if (aCountFrom < 1) aCountFrom = 1;

            for (int i = aCountFrom - 1; i < aCountTo; i++) {
                trunc.add(docs.get(i));
            }

            JsonParser parser = new JsonParser();
            JsonArray a = new JsonArray();

            trunc.forEach((document) -> {
                String json = document.toJson();
                JsonObject obj = parser.parse(json).getAsJsonObject();
                a.add(obj);
            });
            r.addProperty("Name", "Top From " + aCountFrom + " to " + aCountTo + " of " + docs.size() + " by " + aKeySuffix + " on period of " + aPeriod + " ending day of year " + aDate);
            r.addProperty("from", aCountFrom);
            r.addProperty("to", aCountTo);
            r.addProperty("of", docs.size());
            r.addProperty("by", aKeySuffix);
            r.addProperty("period", aPeriod);
            r.addProperty("ending", aDate);
            r.add("reports", a);

        } catch (BadAccessTokenException bate) {
            logger.warn("", bate);
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(fail_response.toString()).build();
        } catch (Exception e) {
            logger.warn("", e);
            return Response.status(Response.Status.EXPECTATION_FAILED).type(MediaType.APPLICATION_JSON).entity(fail_response.toString()).build();

        }

        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(r.toString()).build();
    }

    protected Double convertToDouble(Integer i) {
        return new Double(i.doubleValue());
    }

    @Deprecated
    @GET
    @Path("/top/{count}/by/{key-suffix}/day-of-year/{day-of-year}")
    @Produces({"application/json"})
    public Response getTopAnalyticsByKeySuffix(@PathParam("count") int aCount,
                                               @PathParam("key-suffix") String aKeySuffix,
                                               @PathParam("day-of-year") int aDayOfYear,
                                               @Context HttpServletRequest request,
                                               @Context HttpServletResponse response) {
        //InstagramAnalyticsService instagram;
        JsonObject r = new JsonObject();

        try {
            instagram = InstagramAnalyticsService.createServiceOnBehalfOfUsername("darthjulian");

            List<String> collectionNames = instagram.getListOfInstagramAnalyticsCollections();
            List<Document> docs = instagram.getListOfInstagramAnalyticsDocumentsByDayOfYear(collectionNames, aDayOfYear);

            docs = sortByKeySuffix(docs, aKeySuffix);

            JsonParser parser = new JsonParser();
            JsonArray a = new JsonArray();
            List<Document> result = docs.stream().limit(aCount).collect(Collectors.toList());
            result.forEach((document) -> {
                String json = document.toJson();
                JsonObject obj = parser.parse(json).getAsJsonObject();
                a.add(obj);
            });
            r.addProperty("Name", "Top " + aCount + " by " + aKeySuffix + " on period ending day of year " + aDayOfYear);
            r.addProperty("from", "1");
            r.addProperty("to", aCount);
            r.addProperty("of", docs.size());
            r.addProperty("by", aKeySuffix);
//            r.addProperty("period", aPeriod);
//            r.addProperty("ending", aDate);
            r.add("reports", a);

        } catch (BadAccessTokenException bate) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(fail_response.toString()).build();
        }

        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(r.toString()).build();
    }


    @Deprecated
    @GET
    @Path("/top/{count-from}-{count-to}/by/{key-suffix}/day-of-year/{day-of-year}")
    @Produces({"application/json"})
    public Response getTopAnalyticsByKeySuffix(@PathParam("count-from") int aCountFrom,
                                               @PathParam("count-to") int aCountTo,
                                               @PathParam("key-suffix") String aKeySuffix,
                                               @PathParam("day-of-year") int aDayOfYear,
                                               @Context HttpServletRequest request,
                                               @Context HttpServletResponse response) {
        //InstagramAnalyticsService instagram;
        JsonObject r = new JsonObject();

        try {
            //instagram = InstagramAnalyticsService.createServiceOnBehalfOfUsername("darthjulian");

            List<String> collectionNames = instagram.getListOfInstagramAnalyticsCollections();
            List<Document> docs = instagram.getListOfInstagramAnalyticsDocumentsByDayOfYear(collectionNames, aDayOfYear);


            Collections.sort(docs, new Comparator<Document>() {
                public int compare(Document d1, Document d2) {
                    Object f = JsonPath.read(d1.toJson(), "$.analytics.engagement-analytics-meta." + aKeySuffix);

                    Double n1 = JsonPath.read(d1.toJson(), "$.analytics.engagement-analytics-meta." + aKeySuffix);
                    Double n2 = JsonPath.read(d2.toJson(), "$.analytics.engagement-analytics-meta." + aKeySuffix);
                    //logger.debug(n1+" "+n2);
                    return n2.compareTo(n1);
                }
            });

            List<Document> trunc = new ArrayList<Document>();

            if (aCountTo > docs.size()) aCountTo = docs.size();
            if (aCountFrom < 1) aCountFrom = 1;

            for (int i = aCountFrom - 1; i < aCountTo; i++) {
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
            r.addProperty("Name", "Top From " + aCountFrom + " to " + aCountTo + " of " + docs.size() + " by " + aKeySuffix + " on period ending day of year " + aDayOfYear);
            r.addProperty("from", aCountFrom);
            r.addProperty("to", aCountTo);
            r.addProperty("of", docs.size());
            r.addProperty("by", aKeySuffix);
            r.add("reports", a);

        } catch (Exception bate) {
            fail_response.addProperty("message", bate.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(fail_response.toString()).build();
        }

        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(r.toString()).build();
    }


    @GET
    @Path("/get/feed-for-day/{date}")
    @Produces("application/json")
    public Response getFeedForDay(@PathParam("date") String date,
                                  @Context HttpServletRequest request,
                                  @Context HttpServletResponse response) {
        JsonArray a = new JsonArray();

        try {
            DateTimeFormatter f = DateTimeFormat.forPattern("MMddyy");
            //InstagramAnalyticsService instagram = InstagramAnalyticsService.createServiceOnBehalfOfUsername("darthjulian");
            List<Document> docs = instagram.getAllStatusForDateOrdered(f.parseDateTime(date));

            docs.forEach((Consumer<? super Document>) doc -> {

                a.add(new JsonParser().parse(doc.toJson()));
            });


        } catch (Exception bate) {
            fail_response.addProperty("message", bate.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(fail_response.toString()).build();

        }

        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(a.toString()).build();
    }

    @GET
    @Path("/get/endemic-sorted/by/{key-suffix}/date/{date}/period/{period}")
    @Produces({"application/json"})
    public Response getSortedEndemicAnalytic(@PathParam("key-suffix") String aKeySuffix,
                                             @PathParam("date") String aDate,
                                             @PathParam("period") String aPeriod)
    {
        try {
            InstagramAnalyticsService instagram = InstagramAnalyticsService.createServiceOnBehalfOfUsername("darthjulian");

            List<String> a_1 = instagram.getListOfEndemicAnalyticsCollectionNames();
            Period p = periodForString(aPeriod);
            List<Document>docs = instagram.getListOfInstagramAnalyticsDocumentsByDate(
                    a_1,
                    aDate,
                    p);
            //JsonElement e = instagram.getRankingByEngagementAnalyticFor(docs, aUsername, aKeySuffix, aDate, p);
            List<Document> sorted = instagram.getSortedAnalyticsFor(docs, aKeySuffix, aDate, p);
            return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(new Gson().toJson(sorted)).build();

        } catch (BadAccessTokenException bate) {
            //return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE);
            fail_response.addProperty("BadAccessTokenException", bate.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(fail_response.toString()).build();
        }

    }

    @GET
    @Path("/get/endemic-rank/user/{username}/by/{key-suffix}/date/{date}/")
    @Produces({"application/json"})
    public Response getEndemicAnalyticRankingAllPeriods(@PathParam("username") String aUsername,
                                                        @PathParam("key-suffix") String aKeySuffix,
                                                        @PathParam("date") String aDate) {
        try {
            InstagramAnalyticsService instagram = InstagramAnalyticsService.createServiceOnBehalfOfUsername("darthjulian");

            List<String> a_1 = instagram.getListOfEndemicAnalyticsCollectionNames();

            List<Period> periods = new ArrayList<Period>();
            periods.add(periodForString("P1D"));
            periods.add(periodForString("P3D"));
            periods.add(periodForString("P5D"));
            periods.add(periodForString("P7D"));
            periods.add(periodForString("P10D"));
            periods.add(periodForString("P15D"));
            periods.add(periodForString("P1M"));

            JsonArray a = new JsonArray();

            periods.forEach((Consumer<Period>) p -> {
                List<Document>docs = instagram.getListOfInstagramAnalyticsDocumentsByDate(
                        a_1,
                        aDate,
                        p);
                JsonElement e = instagram.getRankingByEngagementAnalyticFor(docs, aUsername, aKeySuffix, aDate, p);
                a.add(e);
            });


            return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(new Gson().toJson(a)).build();

        } catch (BadAccessTokenException bate) {
            //return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE);
            fail_response.addProperty("BadAccessTokenException", bate.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(fail_response.toString()).build();
        }
    }


    @GET
    @Path("/get/endemic-rank/user/{username}/by/{key-suffix}/date/{date}/period/{period}")
    @Produces({"application/json"})
    public Response getEndemicAnalyticRanking(@PathParam("username") String aUsername,
                                              @PathParam("key-suffix") String aKeySuffix,
                                              @PathParam("date") String aDate,
                                              @PathParam("period") String aPeriod)
    {
        try {
            InstagramAnalyticsService instagram = InstagramAnalyticsService.createServiceOnBehalfOfUsername("darthjulian");

            List<String> a_1 = instagram.getListOfEndemicAnalyticsCollectionNames();
            Period p = periodForString(aPeriod);
            List<Document>docs = instagram.getListOfInstagramAnalyticsDocumentsByDate(
                    a_1,
                    aDate,
                    p);
            JsonElement e = instagram.getRankingByEngagementAnalyticFor(docs, aUsername, aKeySuffix, aDate, p);

            return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(new Gson().toJson(e)).build();

        } catch (BadAccessTokenException bate) {
            //return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE);
            fail_response.addProperty("BadAccessTokenException", bate.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(fail_response.toString()).build();
        }

    }


    @GET
    @Path("/get/rank/user/{username}/by/{key-suffix}/date/{date}/period/{period}")
    @Produces({"application/json"})
    public Response getAnalyticRanking(@PathParam("username") String aUsername,
                                       @PathParam("key-suffix") String aKeySuffix,
                                       @PathParam("date") String aDate,
                                       @PathParam("period") String aPeriod) {
        try {
            InstagramAnalyticsService instagram = InstagramAnalyticsService.createServiceOnBehalfOfUsername("darthjulian");
            JsonElement e = instagram.getRankingByEngagementAnalyticFor(aUsername, aKeySuffix, aDate, periodForString(aPeriod));
            return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(new Gson().toJson(e)).build();

        } catch (BadAccessTokenException bate) {
            //return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE);
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(fail_response.toString()).build();
        }


    }

    @GET
    @Path("/get/users/{usernames}/date/{date}")
    @Produces({"application/json"})
    public Response getAnalyticsByUsernames(@PathParam("usernames") String aUsernames,
                                            @PathParam("date") String aDate,
                                            @Context HttpServletRequest request,
                                            @Context HttpServletResponse response) {
        //DateTime today = new DateTime(ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))));
        DateTime day = InstagramAnalyticsService.short_date_fmt.parseDateTime(aDate).withChronology(ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))));
        //DateTime yesterday = today.minusDays(1);
        ArrayList<Document> result = new ArrayList<>();
        ArrayList<String> list = new ArrayList();
        try {
            // parse the usernames, which should be comma-delimited
            String[] u = aUsernames.split(",");
            for (int i = 0; u != null && i < u.length; i++) {
                list.add((String) u[i]);
            }
            // now we have a list of usernames
            for (int i = 0; i < list.size(); i++) {
                String username = list.get(i);
                try {
                    List<Document> count = instagram.getSortedListOfUserCounts(username, day, 1);
                    if (count != null)
                        result.addAll(count);
                } catch (UsernameNotFoundException unfe) {
                    logger.info(unfe);
                }
            }

        } catch (Exception e) {
            fail_response.addProperty("Exception", e.getMessage());
            return Response.status(Response.Status.EXPECTATION_FAILED).type(MediaType.APPLICATION_JSON).entity(fail_response.toString()).build();

        }
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(new Gson().toJson(result)).build();

    }


    @GET
    @Path("/get/users/{usernames}")
    @Produces({"application/json"})
    public Response getAnalyticsByUsernames(@PathParam("usernames") String aUsernames,
                                            @Context HttpServletRequest request,
                                            @Context HttpServletResponse response) {
        DateTime today = new DateTime(ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))));
        return getAnalyticsByUsernames(aUsernames, InstagramAnalyticsService.short_date_fmt.print(today), request, response);
    }


    @GET
    @Path("/get/user/{username}")
    @Produces({"application/json"})
    public Response getAnalyticsByUsername(@PathParam("username") String aUsername,
                                           @Context HttpServletRequest request,
                                           @Context HttpServletResponse response) {
        // InstagramUser user = Inst
        DateTime yesterday = new DateTime(ISOChronology.getInstance(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))));
        yesterday = yesterday.minusDays(1);
        ArrayList<Document> result = new ArrayList<>();
        JsonArray a = new JsonArray();
        try {
            //instagram.getListOfInstagramAnalyticsDocuments()
            MongoDatabase db = MongoUtil.getMongo().getDatabase("instagram-analytics");
            InstagramUser user = instagram.localUserSearch(aUsername);
            if (user == null) {
                fail_response.addProperty("Error", "No such user " + aUsername);
                return Response.status(Response.Status.EXPECTATION_FAILED).type(MediaType.APPLICATION_JSON).entity(fail_response.toString()).build();
            }
            String coll_name = InstagramAnalyticsService.encodeKey(user.getUsername()) + "_" + user.getUserID() + "_snapshot-counts";
            //FindIterable<Document> docs_i = db.getCollection(coll_name).find(eq("snapshot-day-of-year", DateTimeFormat.forPattern("D").print(yesterday)));
            FindIterable<Document> docs_i = db.getCollection(coll_name).find();
            docs_i.forEach((Block<Document>) document -> {
                result.add(document);
            });

        } catch (Exception e) {
            fail_response.addProperty("Exception", e.getMessage());
            return Response.status(Response.Status.EXPECTATION_FAILED).type(MediaType.APPLICATION_JSON).entity(fail_response.toString()).build();
        }
        //Document r = result.get(0);
        //r.toJson();
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(new Gson().toJson(result)).build();
    }


    @GET
    @Path("/get/aggregate-period-data/user/{username}")
    @Produces({"application/json"})
    public Response getAggregatePeriodDataForUser(@PathParam("username") String aUsername,
                                                  @Context HttpServletRequest request,
                                                  @Context HttpServletResponse response) {
        DateTime yesterday = new DateTime().withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).minusDays(1);
        String date = InstagramAnalyticsService.short_date_fmt.print(yesterday);
        return getAggregatePeriodDataForUser(aUsername, date, request, response);

    }

    @GET
    @Path("/get/aggregate-period-data/user/{username}/end-date/{enddate}")
    @Produces({"application/json"})
    public Response getAggregatePeriodDataForUser(@PathParam("username") String aUsername,
                                                  @PathParam("enddate") String endDate,
                                                  @Context HttpServletRequest request,
                                                  @Context HttpServletResponse response) {
        InstagramUser user = instagram.getLocalUserBasicForUsername(aUsername);
        //DateTime yesterday = new DateTime().withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).minusDays(1);
        //String date = InstagramAnalyticsService.short_date_fmt.print(d);
        DateTime date = InstagramAnalyticsService.short_date_fmt.parseDateTime(endDate);
        if (user == null) {
            fail_response.addProperty("message", "No such username found " + aUsername);
            return Response.status(Response.Status.EXPECTATION_FAILED).type(MediaType.APPLICATION_JSON).entity(fail_response.toString()).build();
        }
        ArrayList<Document> documents = instagram.gatherAggregatePeriodData(user, date);

        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(new Gson().toJson(documents)).build();

    }


    @GET
    @Path("/admin/gettyup/runuserbasic")
    @Produces({"application/json"})
    public Response getGettyupGetUserBasic(@Context HttpServletRequest request,
                                           @Context HttpServletResponse response) {

        try {
//            logger.info("gettyup/update/friends for "+user.getUsername());

            SchedulerFactory sf = new StdSchedulerFactory();
            Scheduler sched = sf.getScheduler();
            sched.start();

            JobDataMap data = new JobDataMap();
//            data.put("user", user);
//            data.put("access_token", access_token);

            JobDetail job = newJob(ScheduledInstagramAnalyticsJob.class)
                    .withIdentity("runUserBasic", "group." + context)
                    .setJobData(data)
                    .build();

            //sched.start();

            ScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.
                    simpleSchedule().
                    withRepeatCount(0);

            //JobDetail details = JobBuilder.newJob().setJobData(data).build();
            Trigger trigger = newTrigger()
                    .withDescription("runUserBasic group." + context)
                    .withIdentity("runUserBasic group." + context)
                    .withSchedule(scheduleBuilder).forJob(job)
                    .startNow().build();

            sched.scheduleJob(job, trigger);
            success_response.addProperty("message", "started job " + trigger.getDescription());
            //return Response.ok("{}", MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            String msg = "Exception attempting to schedule runUserBasic";
            logger.error(e);
            logger.error(msg);
            fail_response.addProperty("message", msg);
            return Response.ok(fail_response.toString(), MediaType.APPLICATION_JSON).build();
        }


        return Response.ok().entity(success_response.toString()).type(MediaType.APPLICATION_JSON).build();
    }


    protected Period periodForString(String aPeriod) {
        Period p = new Period();
        if (aPeriod == null || aPeriod.equals("")) {
            p = p.withDays(1);
        } else if (aPeriod.equalsIgnoreCase("P1D")) {
            p = p.withDays(1);
        } else if (aPeriod.equalsIgnoreCase("P2D")) {
            p = p.withDays(2);
        } else if (aPeriod.equalsIgnoreCase("P3D")) {
            p = p.withDays(3);
        } else if (aPeriod.equalsIgnoreCase("P5D")) {
            p = p.withDays(5);
        } else if (aPeriod.equalsIgnoreCase("P7D")) {
            p = p.withDays(7);
        } else if (aPeriod.equalsIgnoreCase("P10D")) {
            p = p.withDays(10);
        } else if (aPeriod.equalsIgnoreCase("P15D")) {
            p = p.withDays(15);
        } else if (aPeriod.equalsIgnoreCase("P1M")) {
            p = p.withMonths(1);
        } else if (aPeriod.equalsIgnoreCase("P30D")) {
            p = p.withDays(30);
        }

        return p;
    }


    protected List<Document> sortByKeySuffix(List<Document> docs, String aKeySuffix) {


        Collections.sort(docs, new Comparator<Document>() {

            public int compare(Document d1, Document d2) {

                Double n1, n2;
                n1 = new Double(0);
                n2 = new Double(0);
                try {
                    Object f1 = JsonPath.read(d1.toJson(), "$.analytics.engagement-analytics-meta." + aKeySuffix);
                    Object f2 = JsonPath.read(d2.toJson(), "$.analytics.engagement-analytics-meta." + aKeySuffix);
                    if (f1.getClass().getName().equalsIgnoreCase("java.lang.Integer")) {
                        n1 = convertToDouble((Integer) f1);
                    } else {
                        n1 = (Double) f1;
                    }
                    if (f2.getClass().getName().equalsIgnoreCase("java.lang.Integer")) {
                        n2 = convertToDouble((Integer) f2);
                    } else {
                        n2 = (Double) f2;
                    }
                } catch (Exception e) {

                    logger.error(e.getMessage());
                }
                return n2.compareTo(n1);
            }

        });

        return docs;
    }

}
