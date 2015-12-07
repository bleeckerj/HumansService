package com.nearfuturelaboratory.humans.rest;

import com.google.gson.*;
import com.jayway.jsonpath.JsonPath;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.nearfuturelaboratory.humans.exception.BadAccessTokenException;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramUser;
import com.nearfuturelaboratory.humans.service.InstagramAnalyticsService;
import com.nearfuturelaboratory.humans.util.MongoUtil;
import com.nearfuturelaboratory.humans.util.MyObjectIdSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;

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

    @Context ServletContext context;
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

    @GET @Path("/top-by/{key-suffix}/date/{date}")
    @Produces({"application/json"})
    public Response getTopAnalyticsByKeySuffixAndDate(@PathParam("key-suffix") String aKeySuffix,
                                                      @PathParam("date") String aDate,
                                                      @Context HttpServletRequest request,
                                                      @Context HttpServletResponse response) {

        InstagramAnalyticsService instagram;
        JsonObject r = new JsonObject();

        try {
            instagram = InstagramAnalyticsService.createServiceOnBehalfOfUsername("darthjulian");

            List<String> collectionNames = instagram.getListOfInstagramAnalyticsCollections();
            List<Document> docs = instagram.getListOfInstagramAnalyticsDocumentsByDate(collectionNames, aDate);
            //logger.debug(docs);
            // String json = docs.get(0).toJson();
            //Number n = (Number)JsonPath.read(json, "$.analytics.engagement-analytics-meta.max-likes");

            Collections.sort(docs,new Comparator<Document>() {
                public int compare(Document d1, Document d2) {
                    Double n1 = JsonPath.read( d1.toJson(), "$.analytics.engagement-analytics-meta."+aKeySuffix);
                    Double n2 = JsonPath.read( d2.toJson(), "$.analytics.engagement-analytics-meta."+aKeySuffix);
                    logger.debug(n1+" "+n2);
                    return n2.compareTo(n1);
                }
            });
            JsonParser parser = new JsonParser();
            JsonArray a = new JsonArray();
            List<Document> result = docs.stream().limit(20).collect(Collectors.toList());
            result.forEach((document) -> {
                String json = document.toJson();
                JsonObject obj = parser.parse(json).getAsJsonObject();
                a.add(obj);
            });
            r.add("Top 20", a);

        } catch(BadAccessTokenException bate) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(fail_response.toString()).build();
        }

        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(r.toString()).build();



    }


    @GET @Path("/top-by/{key-suffix}/day-of-year/{day-of-year}")
    @Produces({"application/json"})
    public Response getTopAnalyticsByKeySuffix(@PathParam("key-suffix") String aKeySuffix,
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
        //logger.debug(docs);
        // String json = docs.get(0).toJson();
        //Number n = (Number)JsonPath.read(json, "$.analytics.engagement-analytics-meta.max-likes");

        Collections.sort(docs,new Comparator<Document>() {
            public int compare(Document d1, Document d2) {
                Object f = JsonPath.read(d1.toJson(), "$.analytics.engagement-analytics-meta."+aKeySuffix);
//                if(f instanceof String) {
//                    String n1 = JsonPath.read( d1.toJson(), "$.analytics.engagement-analytics-meta."+aKeySuffix);
//                    String n2 = JsonPath.read( d2.toJson(), "$.analytics.engagement-analytics-meta."+aKeySuffix);
//
//                }
                Double n1 = JsonPath.read( d1.toJson(), "$.analytics.engagement-analytics-meta."+aKeySuffix);
                Double n2 = JsonPath.read( d2.toJson(), "$.analytics.engagement-analytics-meta."+aKeySuffix);
                logger.debug(n1+" "+n2);
                return n2.compareTo(n1);
            }
        });
            JsonParser parser = new JsonParser();
            JsonArray a = new JsonArray();
            List<Document> result = docs.stream().limit(20).collect(Collectors.toList());
            result.forEach((document) -> {
                String json = document.toJson();
                JsonObject obj = parser.parse(json).getAsJsonObject();
                a.add(obj);
            });
            r.add("Top 20", a);

        } catch(BadAccessTokenException bate) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(fail_response.toString()).build();
        }

        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(r.toString()).build();
    }


    @GET  @Path("/get/{username}")
    @Produces({"application/json"})
    public Response getAnalyticsByUsername(@PathParam("username") String aUsername,
                                         @Context HttpServletRequest request,
                                         @Context HttpServletResponse response)
    {
       // InstagramUser user = Inst
        ArrayList<Document> result = new ArrayList<>();
        try {
            InstagramAnalyticsService instagram = InstagramAnalyticsService.createServiceOnBehalfOfUsername("darthjulian");
            //instagram.getListOfInstagramAnalyticsDocuments()
            MongoDatabase db = MongoUtil.getMongo().getDatabase("instagram-analytics");
            InstagramUser user = instagram.userSearch(aUsername);
            String coll_name = InstagramAnalyticsService.encodeKey(user.getUsername())+"_"+user.getUserID();
            FindIterable<Document> docs_i = db.getCollection(coll_name).find(eq("snapshot-day-of-year", "339"));
            docs_i.forEach((Block<Document>) document -> {
                result.add(document);
            });

        } catch(BadAccessTokenException bate) {
            //return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE);
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(fail_response.toString()).build();
        }
        //Document r = result.get(0);
        //r.toJson();
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(result.get(0).toJson()).build();
    }

}
