package com.nearfuturelaboratory.humans.rest;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.nearfuturelaboratory.humans.dao.ServiceTokenDAO;
import com.nearfuturelaboratory.humans.entities.*;
import com.nearfuturelaboratory.humans.scheduler.ScheduledFriendsPrefetcher;
import com.nearfuturelaboratory.humans.scheduler.ScheduledHumanStatusFetcher;
import com.nearfuturelaboratory.humans.scheduler.ScheduledInstagramStatusFetcher;
import com.sun.java_cup.internal.runtime.lr_parser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.nearfuturelaboratory.humans.dao.HumansUserDAO;
import com.nearfuturelaboratory.humans.util.MyObjectIdSerializer;
//import static com.google.common.collect.Lists.partition;
import com.nearfuturelaboratory.util.Constants;
import org.cryptonode.jncryptor.AES256JNCryptor;
import org.cryptonode.jncryptor.JNCryptor;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.scribe.model.Token;
//import sun.misc.BASE64Encoder;
import org.apache.commons.codec.binary.Base64;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;


@Path("/user")
//@Consumes(MediaType.APPLICATION_JSON)
//@Produces(MediaType.APPLICATION_JSON)
public class UserHandler {
    final static Logger logger = LogManager.getLogger(com.nearfuturelaboratory.humans.rest.UserHandler.class);
     JsonObject invalid_user_error_response;
     JsonObject success_response;
     JsonObject fail_response;
     JsonObject no_such_human_for_user;
     JsonObject no_such_serviceuser_for_user;

    static {


    }

    @Context ServletContext context;
    Gson in_gson;
    Gson out_gson;
    Gson gson_get_humans;//// = new GsonBuilder().setDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz").registerTypeAdapter(ObjectId.class, new MyObjectIdSerializer()).create();
    Gson gson_add_humans;
    public UserHandler() {
        //logger.debug("Constructor " + context);  // null here
        in_gson = new GsonBuilder().registerTypeAdapter(ObjectId.class, new MyObjectIdSerializer()).create();
        out_gson = new GsonBuilder().
                setExclusionStrategies(new UserJsonExclusionStrategy()).
                registerTypeAdapter(ObjectId.class, new MyObjectIdSerializer()).create();

        gson_get_humans = new GsonBuilder().setDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz").registerTypeAdapter(ObjectId.class, new MyObjectIdSerializer()).create();
        gson_add_humans = new GsonBuilder().registerTypeAdapter(ObjectId.class, new MyObjectIdSerializer()).
                setExclusionStrategies(new ClientAddNewHumanJsonExclusionStrategy()).create();

        invalid_user_error_response = new JsonObject();
        invalid_user_error_response.addProperty("result", "error");
        invalid_user_error_response.addProperty("message", "invalid user");

        success_response = new JsonObject();
        success_response.addProperty("result", "success");

        fail_response = new JsonObject();
        fail_response.addProperty("result", "fail");

        no_such_human_for_user = new JsonObject();
        no_such_human_for_user.addProperty("result", "fail");
        no_such_human_for_user.addProperty("message", "no such human for user");

        no_such_serviceuser_for_user = new JsonObject();
        no_such_serviceuser_for_user.addProperty("result", "fail");
        no_such_serviceuser_for_user.addProperty("message", "no such service user for user");

    }




    /**
     * Retrieve useful user access parameters
     * @param aServiceName
     * @param aServiceUserId
     * @param request
     * @param response
     * @return
     */
    @GET
    @Path("/get/auth/{servicename}/{serviceuserid}")
    public Response getAuthTokenByServiceUserId(
            @PathParam("servicename") String aServiceName,
            @PathParam("serviceuserid") String aServiceUserId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response) {

        String access_token = RestCommon.getAccessTokenFromRequestHeader(request);

        if (access_token == null) {
            fail_response.addProperty("message", "invalid or missing access token");
            return Response.status(Response.Status.UNAUTHORIZED).entity(fail_response.toString()).type(MediaType.APPLICATION_JSON).build();
        }

        HumansUser user = getUserForAccessToken(context, access_token);

        if (user == null) {
            invalid_user_error_response.addProperty("message", "no such user. invalid access token");
            return Response.status(Response.Status.UNAUTHORIZED).entity(invalid_user_error_response).type(MediaType.APPLICATION_JSON).build();
        }

        ServiceTokenDAO dao = new ServiceTokenDAO(aServiceName);
        ServiceToken service_token = dao.findByExactUserId(aServiceUserId);
        Token token = service_token.getToken();
//        Gson gson = new Gson();
//        String token_str = gson.toJson(token.getToken());
        JsonObject obj = new JsonObject();
        String pw_key_key = aServiceName.toUpperCase() + "_API_KEY";
        String cs_key_key = aServiceName.toUpperCase() + "_API_SECRET";
        String aw_key_key = aServiceName.toUpperCase() + "_CF_PV";
//        StandardPBEStringEncryptor stringEncryptor = new StandardPBEStringEncryptor();
//        stringEncryptor.setAlgorithm("AES");
//        stringEncryptor.setPassword("abadbassword");
//        stringEncryptor.initialize();

        String api_key = Constants.getString(pw_key_key);
        String api_secret = Constants.getString(cs_key_key);
        JsonObject result;
        try {

            JNCryptor cryptor = new AES256JNCryptor();
            byte[] bytes = api_key.getBytes();
            byte[] ciphertext = cryptor.encryptData(bytes, new String("humans-" + aw_key_key).toCharArray());

            String encStr = Base64.encodeBase64String(ciphertext);  //  new BASE64Encoder().encode(ciphertext);
            obj.addProperty("ck", encStr);


            bytes = api_secret.getBytes();
            ciphertext = cryptor.encryptData(bytes, new String("humans-" + aw_key_key).toCharArray());

            encStr = Base64.encodeBase64String(ciphertext);
            obj.addProperty("cs", encStr);


            bytes = token.getToken().getBytes();
            ciphertext = cryptor.encryptData(bytes, new String("humans-" + aw_key_key).toCharArray());

            encStr = Base64.encodeBase64String(ciphertext);
            obj.addProperty("tk", encStr);

            bytes = token.getSecret().getBytes();
            ciphertext = cryptor.encryptData(bytes, new String("humans-" + aw_key_key).toCharArray());

            encStr = Base64.encodeBase64String(ciphertext);
            obj.addProperty("ts", encStr);


            bytes = obj.toString().getBytes();
            byte[] parcel_bytes = cryptor.encryptData(bytes, new String("client-" + aw_key_key).toCharArray());
            String parcel = Base64.encodeBase64String(parcel_bytes);
            result = new JsonObject();
            result.addProperty("parcel", parcel);
        } catch (Exception e) {
            logger.error(e);
            fail_response.addProperty("exception", e.getMessage());
            //Response.Status.fromStatusCode(417);
            //Response.Status.EXPECTATION_FAILED
            return Response.status(Response.Status.EXPECTATION_FAILED).entity(fail_response.toString()).build();

        }
        // obj.addProperty("ck", stringEncryptor.encrypt(Constants.getString(key_key)));
//        return Response.ok().build();
        return Response.ok(result.toString(), MediaType.APPLICATION_JSON).header("Content-Length", result.toString().length()).build();
//        return Response.ok().entity(obj.toString()).build();
    }

    /**
     * This is a way to "gettyup" and get friends quickly for a new user after they've authenticated
     * with services.
     *
     * @see com.nearfuturelaboratory.humans.scheduler.ScheduledFriendsPrefetcher
     *
     * @param request
     * @param response
     * @return Not much, cause it'll just spin up a job to execute once
     */
    @GET
    @Path("/getty/update/friends")
    public Response updateFriends(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response)
    {
        String access_token = RestCommon.getAccessTokenFromRequestHeader(request);
        if(access_token == null) {
            fail_response.addProperty("message", "invalid or missing access token");
            return Response.status(Response.Status.UNAUTHORIZED).entity(fail_response.toString()).type(MediaType.APPLICATION_JSON).build();
        }

        HumansUser user = getUserForAccessToken(context, access_token);

        if(user == null) {
            invalid_user_error_response.addProperty("message", "no such user. invalid access token");
            return Response.status(Response.Status.UNAUTHORIZED).entity(invalid_user_error_response.toString()).type(MediaType.APPLICATION_JSON).build();
        }

        try {
            logger.info("gettyup/update/friends for "+user.getUsername());

            SchedulerFactory sf = new StdSchedulerFactory();
            Scheduler sched = sf.getScheduler();
            sched.start();

            JobDataMap data = new JobDataMap();
            data.put("user", user);
            data.put("access_token", access_token);

            JobDetail job = newJob(ScheduledFriendsPrefetcher.class)
                    .withIdentity("updateFriends."+user.getUsername(), "group."+context)
                    .setJobData(data)
                    .build();

            //sched.start();

            ScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.
                    simpleSchedule().
                    withRepeatCount(0);

            //JobDetail details = JobBuilder.newJob().setJobData(data).build();
            Trigger trigger = newTrigger()
                    .withDescription("updateFriends."+user.getUsername()+ " group."+context)
                    .withIdentity("updateFriends." + user.getUsername(), "group." + context)
                    .withSchedule(scheduleBuilder).forJob(job)
                    .startNow().build();

            sched.scheduleJob(job, trigger);
            success_response.addProperty("message", "started job "+trigger.getDescription());
            //return Response.ok("{}", MediaType.APPLICATION_JSON).build();
        }catch(Exception e) {
            String msg = "Exception attempting to schedule updateFriendsPrefetcher="+user.getUsername()+" user="+user.getUsername();
            logger.error(e);
            logger.error(msg);
            fail_response.addProperty("message", msg);
            return Response.ok(fail_response.toString(), MediaType.APPLICATION_JSON).build();
        }


        return Response.ok().entity(success_response.toString()).type(MediaType.APPLICATION_JSON).build();
    }


    /**
     * This is a way to "gettyup" and update status quickly for a new human, with alls it
     * This really should only ever be called when a new human is created otherwise it could
     * possibly clobber a running status updater by crashing into database writes or something, especially
     * when it refreshes the cache..
     *
     * Also, if this instance is far away from the database, it could take a bit of time to
     * write the database, it seems. Maybe best to run it on an instance surrounding the DB (eg on the same
     * instance as the DB) or figure out how to move the application instance nearby.
     *
     * @see com.nearfuturelaboratory.humans.scheduler.ScheduledHumanStatusFetcher
     *
     * @param aHumanId
     * @param request
     * @param response
     * @return
     */
    @GET @Path("/getty/update/{humanid}")
    public Response updateStatusAndCacheForHuman(
            @PathParam("humanid") String aHumanId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response)
    {
        //Response response = ResponseBuilder;
        String access_token = RestCommon.getAccessTokenFromRequestHeader(request);
        if(access_token == null) {
            fail_response.addProperty("message", "invalid or missing access token");
            return Response.status(Response.Status.UNAUTHORIZED).entity(fail_response).type(MediaType.APPLICATION_JSON).build();
        }

        HumansUser user = getUserForAccessToken(context, access_token);

        if(user == null) {
            invalid_user_error_response.addProperty("message", "no such user. invalid access token");
            return Response.status(Response.Status.UNAUTHORIZED).entity(invalid_user_error_response).type(MediaType.APPLICATION_JSON).build();
        }

        Human aHuman = user.getHumanByID(aHumanId);
        if(aHuman != null) {
            // take the user and put it off somewhre to update?
            //user.serviceRefreshStatusForHuman(aHuman);
            try {
                //schedule the job
                logger.info("gettyup/update/{"+aHuman.getId()+"}="+aHuman.getName()+" for "+user.getUsername());
                SchedulerFactory sf = new StdSchedulerFactory();
                Scheduler sched = sf.getScheduler();
                sched.start();

                JobDataMap data = new JobDataMap();
                data.put("humanid", aHumanId);
                data.put("access_token", access_token);

                JobDetail job = newJob(ScheduledHumanStatusFetcher.class)
                        .withIdentity("fetchHumanStatus."+aHumanId, "group."+context)
                        .setJobData(data)
                        .build();

                ScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.
                        simpleSchedule().
                        withRepeatCount(0);

                //JobDetail details = JobBuilder.newJob().setJobData(data).build();
                Trigger trigger = newTrigger()
                        .withDescription("getty.update.fetchHumanStatus."+aHuman.getName() +" group."+context)
                        .withIdentity("getty.update.fetchHumanStatus."+aHumanId, "group."+context)
                        .withSchedule(scheduleBuilder).forJob(job)
                        .startNow().build();

                sched.scheduleJob(job, trigger);

                success_response.addProperty("message", "started job "+trigger.getDescription());
                //return Response.ok("{}", MediaType.APPLICATION_JSON).build();
            }catch(Exception e) {
                String msg = "Exception attempting to schedule fetcher for human status, human_id="+aHumanId+" user="+user.getUsername();
                logger.error(msg);
                fail_response.addProperty("message", msg);
                return Response.ok(fail_response.toString(), MediaType.APPLICATION_JSON).build();
            }
        } else {
            fail_response.addProperty("message", "invalid id for human");
            logger.warn("invalid id for human "+aHumanId);
            return Response.ok(fail_response.toString(), MediaType.APPLICATION_JSON).build();
        }

        return Response.ok().entity(success_response.toString()).type(MediaType.APPLICATION_JSON).build();
    }


    /**
     * Spins of a quick task to update a specific Instagram status item by its mediaID
     * on behalf of a specific instagram username.
     * Use this if you want to update a single status item for things such as rechecking the "like"
     * state or whatever. It'll effectively update the status as represented in the datastore.
     *
     * @param aMediaId
     * @param aInstagramUsername
     * @param response
     * @return
     */
    @GET @Path("/getty/update/instagram/{onBehalfOfInstagramUsername}/status/{mediaid}")
    public Response updateInstagramStatus (
            @PathParam("mediaid") String aMediaId,
            @PathParam("onBehalfOfInstagramUsername") String aInstagramUsername,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response) {
        //Response response = ResponseBuilder;
        String access_token = RestCommon.getAccessTokenFromRequestHeader(request);
        if (access_token == null) {
            fail_response.addProperty("message", "invalid or missing access token");
            return Response.status(Response.Status.UNAUTHORIZED).entity(fail_response).type(MediaType.APPLICATION_JSON).build();
        }

        HumansUser user = getUserForAccessToken(context, access_token);

        if (user == null) {
            invalid_user_error_response.addProperty("message", "no such user. invalid access token");
            return Response.status(Response.Status.UNAUTHORIZED).entity(invalid_user_error_response).type(MediaType.APPLICATION_JSON).build();
        }

        try {
            //schedule the job
            logger.info("gettyup/update/instagram/{"+aInstagramUsername +"}/{"+aMediaId+"} for "+user.getUsername());
            SchedulerFactory sf = new StdSchedulerFactory();
            Scheduler sched = sf.getScheduler();
            sched.start();

            JobDataMap data = new JobDataMap();
            data.put("mediaid", aMediaId);
            data.put("onBehalfOfInstagramUsername", aInstagramUsername);
            //data.put("access_token", access_token);

            JobDetail job = newJob(ScheduledInstagramStatusFetcher.class)
                    .withIdentity("/getty/update/instagram/"+aInstagramUsername +"/status/"+aMediaId, "group."+context)
                    .setJobData(data)
                    .build();

            ScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.
                    simpleSchedule().
                    withRepeatCount(0);

            //JobDetail details = JobBuilder.newJob().setJobData(data).build();
            Trigger trigger = newTrigger()
                    .withDescription("/getty/update/instagram/"+aInstagramUsername +"/status/"+aMediaId +" group."+context)
                    .withIdentity("/getty/update/instagram/"+aInstagramUsername+"/status/"+aMediaId, "group."+context)
                    .withSchedule(scheduleBuilder).forJob(job)
                    .startNow().build();

            sched.scheduleJob(job, trigger);

            success_response.addProperty("message", "started job "+trigger.getDescription());
            //return Response.ok("{}", MediaType.APPLICATION_JSON).build();
        }catch(Exception e) {
            String msg = "Exception attempting to schedule fetcher for instagram status,mediaID="+aMediaId+" on behalf of "+aInstagramUsername +" user="+user.getUsername();
            logger.error(msg);
            fail_response.addProperty("message", msg);
            return Response.ok(fail_response.toString(), MediaType.APPLICATION_JSON).build();
        }
        return Response.ok().entity(success_response.toString()).type(MediaType.APPLICATION_JSON).build();
    }



        @GET @Path("/get")
    @Produces({"application/json"})
    public Response getUser(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response)
    {
//		HttpSession session = request.getSession();
//		HumansUser user = (HumansUser)session.getAttribute("logged-in-user");
//
//		if(isValidUser(request, user) == false) {
//			return invalid_user_error_response.toString();
//		}

        String access_token = RestCommon.getAccessTokenFromRequestHeader(request);//request.getHeader("Authorization");//request.getParameter("access_token");

        if(access_token == null) {
            fail_response.addProperty("message", "invalid or missing access token");
            return Response.status(Response.Status.UNAUTHORIZED).entity(fail_response.toString()).type(MediaType.APPLICATION_JSON).build();
        }

        HumansUser user = getUserForAccessToken(context, access_token);

        if(user == null) {
            invalid_user_error_response.addProperty("message", "invalid access token");
            return Response.status(Response.Status.UNAUTHORIZED).entity(invalid_user_error_response.toString()).type(MediaType.APPLICATION_JSON).build();
        }


        // don't send the encrypted password
        //user.setPassword(null);
        JsonElement user_elem = new JsonParser().parse(out_gson.toJson(user));
        return Response.ok(user_elem.toString(), MediaType.APPLICATION_JSON).build();


    }

    @POST @Path("/rm/service")
    @Produces(MediaType.APPLICATION_JSON)
    public String removeService(
            String aServiceJson,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response)
    {
//		HumansUser  user = this.getSessionUser(request);
//		if(user == null) {
//			return no_such_human_for_user.toString();
//		}

        String access_token = RestCommon.getAccessTokenFromRequestHeader(request);

        if(access_token == null) {
            fail_response.addProperty("message", "invalid or missing access token");
            return fail_response.toString();
        }

        HumansUser user = getUserForAccessToken(context, access_token);

        if(user == null) {
            invalid_user_error_response.addProperty("message", "invalid access token");
            return invalid_user_error_response.toString();
        }

        try {
            ServiceEntry service = in_gson.fromJson(aServiceJson, ServiceEntry.class);
            boolean result = user.removeService(service);
            if(result == true) {
                user.updateYouman();
                user.save();
                return success_response.toString();
            } else {
                return fail_response.toString();
            }
        }catch(Exception e) {
            logger.error("", e);
            fail_response.addProperty("message", e.getMessage());
            return fail_response.toString();
        }
    }


    @GET @Path("/rm/{humanid}/human")
    @Produces({"application/json"})
    public String removeHuman(
            @PathParam("humanid") String aHumanId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response)
    {
        //HumansUserDAO dao = new HumansUserDAO();
        //HumansUser user = dao.findByHumanID(aHumanId);

        String access_token = RestCommon.getAccessTokenFromRequestHeader(request);

        if(access_token == null) {
            fail_response.addProperty("message", "invalid or missing access token");
            return fail_response.toString();
        }

        HumansUser user = getUserForAccessToken(context, access_token);

        if(user == null) {
            invalid_user_error_response.addProperty("message", "invalid access token");
            return invalid_user_error_response.toString();
        }

        boolean result = user.removeHumanById(aHumanId);
        user.save();
        this.clearContextOfUser(context, access_token);
//		setSessionUser(request, user);

        if(result) {
            return success_response.toString();
        } else {
            fail_response.addProperty("message", "failed to remove human by id "+aHumanId);
            return fail_response.toString();
        }
    }

    /**
     *TODO  Weird. SHouldn't we remove a service user at the "human" level? Oh well..
     * @param aServiceUserId
     * @param request
     * @param response
     * @return
     */
    @GET @Path("/rm/{serviceuserid}/serviceuser")
    @Produces({"application/json"})
    public String removeServiceUser(
            @PathParam("serviceuserid") String aServiceUserId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response)
    {
        fail_response.addProperty("message", "deprecated endpoint. use the human");
        return fail_response.toString();

    }

    // TODO if you add a human and it contains a service that does not yet exist..
    // what do you do? Should fail? Need to add a service first before adding humans that
    // use that service??
    @POST @Path("/add/human/")
    @Produces({"application/json"})
    public Response addNewHuman(
            String aHumanJson,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response) {

        String access_token = RestCommon.getAccessTokenFromRequestHeader(request);
        logger.warn("This is a test");

        if (access_token == null) {
            fail_response.addProperty("message", "invalid or missing access token");
            return Response.status(Response.Status.UNAUTHORIZED).encoding(fail_response.toString()).build();
        }

        HumansUser user = getUserForAccessToken(context, access_token);

        if (user == null) {
            invalid_user_error_response.addProperty("message", "invalid access token");
            return Response.status(Response.Status.UNAUTHORIZED).encoding(invalid_user_error_response.toString()).build();
            //return invalid_user_error_response.toString();
        }
        Human human = null;
        try {
            human = gson_add_humans.fromJson(aHumanJson, Human.class);
//		if(isValidUser(request, user) == false) {
//			return invalid_user_error_response.toString();
//		}

            // TODO Finish this - need to check if the human we're adding has a
            // service user that shouldn't be there because the User doesn't have
            // the onBehalfOf component?
            List<ServiceEntry> x = human.getServicesThisHumanReliesUpon();
            human.getServiceUsers();
            logger.warn("WTF?");
            boolean result = user.addHuman(human);

            if (result) {
               // logger.debug("user?"+user.getUsername());
                user.save();
                // we'll save and the human should now have an ID
                Human new_human =  user.getHumanByName(human.getName());
                //Process p = new ProcessBuilder("java", "com.nearfuturelaboratory.humans.util.RefreshHuman")
                logger.debug("new_human?"+new_human);
                //this.clearContextOfUser(context, access_token);
                //String bar = success_response.
                //Response foo = Response.status(Response.Status.OK).entity(success_response).build();
                //Response.ok().entity(success_response.toString()).type(MediaType.APPLICATION_JSON).build();
                success_response.addProperty("human.id", new_human.getId());
                success_response.addProperty("human.name", new_human.getName());
                return Response.ok(success_response.toString(), MediaType.APPLICATION_JSON).build(); //Response.OK.build();//success_response.toString();
            } else {
                fail_response.addProperty("message", "couldn't save human");
                logger.warn("add human failed because user.addHuman "+human);
                return Response.status(Response.Status.OK).entity(fail_response.getAsString()).build();//.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("in /user/add/human with " + human+" "+e.getCause(), e);
            logger.warn("", e);
            fail_response.addProperty("exception", e.getCause().getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(fail_response.toString()).type( MediaType.APPLICATION_JSON).build();
            //Response.status()
            //return Response.status(Response.Status.NOT_ACCEPTABLE).entity(fail_response).build();

            //return fail_response.toString();
        }
    }

    @GET @Path("/get/humans/")
    @Produces(MediaType.APPLICATION_JSON)
    public String getAllHumans(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    )
    {
        String access_token = RestCommon.getAccessTokenFromRequestHeader(request);// request.getParameter("access_token");

        if(access_token == null) {
            fail_response.addProperty("message", "invalid or missing access token");
            return fail_response.toString();
        }

        HumansUser user = getUserForAccessToken(context, access_token);

        if(user == null) {
            invalid_user_error_response.addProperty("message", "invalid access token");
            return invalid_user_error_response.toString();
        }


        //Gson gson_get_humans = new GsonBuilder().setDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz").registerTypeAdapter(ObjectId.class, new MyObjectIdSerializer()).create();

        List<Human> humans = user.getAllHumans();
        JsonArray array_of_humans = new JsonArray();
        for(Human human : humans) {
            array_of_humans.add(gson_get_humans.toJsonTree(human, Human.class));
        }
        return array_of_humans.toString();
    }

    protected boolean doesUsernameExist(String aUsername) {
        return HumansUser.doesUsernameExist(aUsername);
    }

    /**
     *  [{"check_username" : "foo"}]
     *
     *  [{"result" : "success"}, {"exists" : false}];
     *
     */
    @POST @Path("/username/exists")
    //@Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String usernameExists(
            String usernameJson,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    )
    {
        JsonElement elem = in_gson.fromJson(usernameJson, JsonElement.class);
        if(elem.isJsonObject()) {
            JsonObject obj = elem.getAsJsonObject();
            String username = obj.get("username").getAsString();

            if(HumansUser.doesUsernameExist(username)) {
                success_response.addProperty("exists", Boolean.TRUE);
                return success_response.toString();
            } else {
                success_response.addProperty("exists", Boolean.FALSE);
                return success_response.toString();
            }
        } else {
            fail_response.addProperty("message", "invalid request");
            fail_response.add("parameter", elem);
            return fail_response.toString();
        }
    }

    @POST @Path("/new")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNewUser(
            String minimalUserJson,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    )
    {
        // TODO Should be checking for new administrative user??
        // Validate the json?
        HumansUser new_user;

        try {
            new_user = in_gson.fromJson(minimalUserJson, HumansUser.class);
            if(new_user.getUsername() == null || new_user.getUsername().length() < 2) {
                fail_response.addProperty("message", "username missing.");
                fail_response.addProperty("received", minimalUserJson);
                return Response.ok(fail_response.toString(), MediaType.APPLICATION_JSON).build();
            }
            if(new_user.getPassword() == null || new_user.getPassword().length() < 5) {
                fail_response.addProperty("message", "password missing or too short.");
                fail_response.addProperty("received", minimalUserJson);
                return Response.ok( fail_response.toString(), MediaType.APPLICATION_JSON).build();
            }
            if(doesUsernameExist(new_user.getUsername())) {
                fail_response.addProperty("message", "username already exists.");
                fail_response.addProperty("received", minimalUserJson);
                return Response.ok(fail_response.toString(), MediaType.APPLICATION_JSON).build();
            }
            if(new_user.getEmail() == null || new_user.getEmail().length() < 2 ) {
                fail_response.addProperty("message", "email invalid.");
                fail_response.addProperty("received", minimalUserJson);
                return Response.ok(fail_response.toString(), MediaType.APPLICATION_JSON).build();
            }

            // need to do this to get the password encrypted
            // TODO
            new_user.setPassword(new_user.getPassword());
            new_user.save();
            //this.clearContextOfUser(context, access_token);
        } catch(JsonSyntaxException jse) {
            logger.warn("Received bad json for createNewUser", jse);
            logger.warn(minimalUserJson);
            fail_response.addProperty("message", "Received bad json for createNewUser");
            fail_response.addProperty("received", minimalUserJson);
            return Response.ok(fail_response.toString(), MediaType.APPLICATION_JSON).build();
        }
        success_response.addProperty("message", "successfully created "+new_user.getUsername());
        success_response.addProperty("user", new Gson().toJson(new_user));
        return Response.ok(success_response.toString(), MediaType.APPLICATION_JSON).build();
    }

    /**
     * Only updates email address or password
     *
     * @param minimalUserJsonOnlyPasswordAndEmail
     * @param request
     * @param response
     * @return
     */
    @POST @Path("/update")
    @Produces(MediaType.APPLICATION_JSON)
    public String updateUser(
            String minimalUserJsonOnlyPasswordAndEmail,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    )
    {
        String access_token = RestCommon.getAccessTokenFromRequestHeader(request);

        if(access_token == null) {
            fail_response.addProperty("message", "invalid or missing access token");
            return fail_response.toString();
        }

        HumansUser user = getUserForAccessToken(context, access_token);

        if(user == null) {
            invalid_user_error_response.addProperty("message", "invalid access token");
            return invalid_user_error_response.toString();
        }
        JsonElement elem = new JsonParser().parse(minimalUserJsonOnlyPasswordAndEmail);
        JsonObject obj = elem.getAsJsonObject();

        if(obj.has("password")) {
            String pw = obj.get("password").getAsString();
            if(pw != null && pw.length() < Constants.getInt("MINIMUM_PASSWORD_LENGTH", 5)) {
                fail_response.addProperty("message", "Password missing or too short");
                fail_response.addProperty("received", minimalUserJsonOnlyPasswordAndEmail);
                return fail_response.toString();
            }
            user.setPassword(pw);
        }
        if(obj.has("email")) {
            String email = obj.get("email").getAsString();
            // can't delete an email address?
            if(email.length() > 0) {
                user.setEmail(email);
            }

        }
        user.save();
        this.clearContextOfUser(context, access_token);
        // don't send the encrypted password
        //user.setPassword(null);
        JsonElement user_elem = new JsonParser().parse(out_gson.toJson(user));
        return user_elem.toString();
    }

    protected void clearContextOfUser(ServletContext context, String access_token)
    {
        context.removeAttribute(access_token+"_user");
    }

    protected HumansUser getUserForAccessToken(ServletContext context, String access_token)
    {

        HumansUser user;

        user = (HumansUser)context.getAttribute(access_token+"_user");
        //logger.debug("context="+context);
        HumansUserDAO dao = (HumansUserDAO)context.getAttribute("dao");
        if(dao == null) {
            dao = new HumansUserDAO();
            context.setAttribute("dao", dao);

        }
        //		HttpSession session = request.getSession();
        //		logger.debug(session.getId());
        //		HumansUser user = (HumansUser)session.getAttribute(access_token);
        //		if(user == null) {
        user = dao.findOneByAccessToken(access_token);
        //MongoUtil.getMongo().getConnector().close();
        //logger.debug("dao = "+dao);

        return user;
    }

    @GET @Path("/friends/get/")
    @Produces({"application/json"})
    public Response getFriends(
            @QueryParam("name-like") String aHumanId,
            @Context HttpServletRequest request,
            @Context ServletContext context
    )
    {
        // String access_token = request.getParameter("access_token");
        String access_token = RestCommon.getAccessTokenFromRequestHeader(request);

        if(access_token == null) {
            fail_response.addProperty("message", "invalid or missing access token");
            return Response.status(Response.Status.UNAUTHORIZED).entity(fail_response.toString()).type(MediaType.APPLICATION_JSON).build();
        }

        HumansUser user = getUserForAccessToken(context, access_token);

        if(user == null) {
            invalid_user_error_response.addProperty("message", "invalid access token");

            return Response.status(Response.Status.UNAUTHORIZED).entity(invalid_user_error_response.toString()).type(MediaType.APPLICATION_JSON).build();
        }


        JsonArray result = user.getFriendsAsJson();
        String response_str = result.toString();
        byte[] b = response_str.getBytes(Charset.forName("UTF-8"));
        int content_length = b.length;


        return Response.ok(result.toString(), MediaType.APPLICATION_JSON).header("Content-Length", String.valueOf(content_length)).build();
    }




}