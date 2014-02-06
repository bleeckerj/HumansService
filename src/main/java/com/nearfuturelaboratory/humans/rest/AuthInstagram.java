package com.nearfuturelaboratory.humans.rest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nearfuturelaboratory.humans.dao.HumansUserDAO;
import com.nearfuturelaboratory.humans.entities.HumansUser;
import com.nearfuturelaboratory.humans.entities.ServiceEntry;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramUser;
import com.nearfuturelaboratory.humans.service.InstagramService;
import com.nearfuturelaboratory.humans.serviceapi.InstagramApi;
import com.nearfuturelaboratory.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URI;
import java.nio.file.Paths;

/**
 * Created by julian on 2/5/14.
 */


@Path("/auth")
public class AuthInstagram {
    private OAuthService service;
    private String apiKey = Constants.getString("INSTAGRAM_API_KEY");
    private String apiSecret = Constants.getString("INSTAGRAM_API_SECRET");
    private String callbackURL = Constants.getString("INSTAGRAM_CALLBACK_URL");
    private Token accessToken;
    private static final Token EMPTY_TOKEN = null;


    final static Logger logger = LogManager.getLogger(AuthInstagram.class);
    static JsonObject invalid_user_error_response;
    static JsonObject success_response;
    static JsonObject fail_response;
    static JsonObject no_such_human_for_user;

    static {

        invalid_user_error_response = new JsonObject();
        invalid_user_error_response.addProperty("result", "error");
        invalid_user_error_response.addProperty("message", "invalid user");

        success_response = new JsonObject();
        success_response.addProperty("result", "success");

        fail_response = new JsonObject();
        fail_response.addProperty("result", "error");

        no_such_human_for_user = new JsonObject();
        no_such_human_for_user.addProperty("result", "error");
        no_such_human_for_user.addProperty("message", "no such human for user");

    }

    @Context
    ServletContext context;


    @GET
    @Path("/instagram")
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticateForUser(@Context HttpServletRequest request,
                                        @Context HttpServletResponse response)
    {
        String humans_access_token = request.getParameter("access_token");
        if(humans_access_token == null && request.getParameter("code") == null) {
            fail_response.addProperty("message", "invalid or missing access token");
            return Response.status(Response.Status.UNAUTHORIZED).entity(fail_response.toString()).type(MediaType.APPLICATION_JSON).build();
        }

        HumansUser user;
        HttpSession session = request.getSession(true);

        logger.debug("now session="+session.getId());
        if(humans_access_token != null) {
            user = getUserForAccessToken(context, humans_access_token);
            // it might be that mobile browser is holding onto the cookie/session id
            // even after it is unloaded and cleared..caches or something..
//            if(request.isRequestedSessionIdValid()) {
//                request.getSession().invalidate();
//            }
            session.setAttribute("logged-in-user", user);

        } else {
            user = (HumansUser)request.getSession().getAttribute("logged-in-user");
        }


        if(user == null) {
            invalid_user_error_response.addProperty("message", "no such user.");
            return Response.status(Response.Status.UNAUTHORIZED).entity(invalid_user_error_response.toString()).type(MediaType.APPLICATION_JSON).build();
        }

        service = new ServiceBuilder()
                .provider(InstagramApi.class)
                .apiKey(apiKey)
                .apiSecret(apiSecret)
                .callback(callbackURL)
                .scope("basic,likes")
                .build();


        if(request.getParameter("code") != null) {

            logger.debug("now a response code="+request.getParameter("code"));
            Verifier verifier = new Verifier(request.getParameter("code"));
            accessToken = service.getAccessToken(EMPTY_TOKEN, verifier);
            logger.debug("and access token "+accessToken);

            InstagramService instagramService = new InstagramService(accessToken);
            InstagramUser instagramUser = instagramService.serviceRequestUserBasic();


            logger.debug("instagram user="+instagramUser);

            logger.info("got access token for "+instagramService.getThisUser());
            InstagramService.serializeToken(accessToken, instagramService.getThisUser());
            //HttpSession session = request.getSession(true);


            logger.debug("screen_name is "+instagramService.getThisUser().getUsername()+" "+instagramService.getThisUser().getId());
            user.addService( (String)instagramService.getThisUser().getId(),  (String)instagramService.getThisUser().getUsername(),"instagram");
            user.save();
//            session.setAttribute("logged-in-user", user);
//            ServiceEntry entry = new ServiceEntry(instagramService.getThisUser().getUserID(),
//                    instagramService.getThisUser().getUsername(), "instagram");

//            if(user.addService(entry)) {
//            user.save();
//                logger.info("added service_entry="+entry+" for user="+user.getUsername());
//            } else {
//                logger.warn("couldn't add service_entry="+entry);
//            }
//            user.addService("instagram", (String)instagramService.getThisUser().getUsername(), (String)instagramService.getThisUser().getId());


            InstagramService.serializeToken(accessToken, instagramService.getThisUser());

            //response.sendRedirect(request.getContextPath()+"/services.jsp");

            Gson gson = new Gson();

            // convert java object to JSON format,
            // and returned as JSON formatted string
            String userJson = gson.toJson(instagramUser);
            instagramService.getFriends();
            //return Response.ok(userJson, MediaType.APPLICATION_JSON).build();
            return Response.ok().entity(userJson).type(MediaType.APPLICATION_JSON).build();
        } else {
            try {

                String authorizationUrl = service.getAuthorizationUrl(EMPTY_TOKEN);
                logger.info("Authorization URL="+authorizationUrl);
                //response.sendRedirect(authorizationUrl);
                return Response.seeOther(URI.create(authorizationUrl)).build();
            } catch(Exception ioe) {
                logger.error(ioe);
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ioe.printStackTrace(pw);
                //sw.toString();
                fail_response.addProperty("message", ioe.getMessage());
                fail_response.addProperty("trace", sw.toString());
                return Response.status(Response.Status.UNAUTHORIZED).entity(fail_response).type(MediaType.APPLICATION_JSON).build();
            }
        }
        //return Response.status(Response.Status.BAD_REQUEST).entity(fail_response).type(MediaType.APPLICATION_JSON_TYPE).build();
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

}
