package com.nearfuturelaboratory.humans.rest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nearfuturelaboratory.humans.dao.*;
import com.nearfuturelaboratory.humans.entities.HumansUser;
import com.nearfuturelaboratory.humans.exception.BadAccessTokenException;
import com.nearfuturelaboratory.humans.flickr.entities.FlickrUser;
import com.nearfuturelaboratory.humans.foursquare.entities.FoursquareUser;
import com.nearfuturelaboratory.humans.instagram.entities.InstagramUser;
import com.nearfuturelaboratory.humans.service.FlickrService;
import com.nearfuturelaboratory.humans.service.FoursquareService;
import com.nearfuturelaboratory.humans.service.InstagramService;
import com.nearfuturelaboratory.humans.service.TwitterService;
import com.nearfuturelaboratory.humans.serviceapi.InstagramApi;
import com.nearfuturelaboratory.humans.twitter.entities.TwitterUser;
import com.nearfuturelaboratory.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.FlickrApi;
import org.scribe.builder.api.Foursquare2Api;
import org.scribe.builder.api.TumblrApi;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Token;
import org.scribe.model.Verb;
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

/**
 * Created by julian on 2/5/14.
 */


@Path("/auth")
public class AuthServices {
    //private OAuthService service;
    private String instagramAPIKey = Constants.getString("INSTAGRAM_API_KEY");
    private String instagramAPISecret = Constants.getString("INSTAGRAM_API_SECRET");
    private String instagramCallbackURL = Constants.getString("INSTAGRAM_CALLBACK_URL");

    private String twitterAPIKey = Constants.getString("TWITTER_API_KEY");
    private String twitterAPISecret = Constants.getString("TWITTER_API_SECRET");
    private String twitterCallbackURL = Constants.getString("TWITTER_CALLBACK_URL");

    private String flickrAPIKey = Constants.getString("FLICKR_API_KEY");
    private String flickrAPISecret = Constants.getString("FLICKR_API_SECRET");
    private String flickrCallbackURL = Constants.getString("FLICKR_CALLBACK_URL");

    private String foursquareAPIKey = Constants.getString("FOURSQUARE_API_KEY");
    private String foursquareAPISecret = Constants.getString("FOURSQUARE_API_SECRET");
    private String foursquareCallbackURL = Constants.getString("FOURSQUARE_CALLBACK_URL");

    private String tumblrAPIKey = Constants.getString("TUMBLR_API_KEY");
    private String tumblrAPISecret = Constants.getString("TUMBLR_API_SECRET");
    private String tumblrCallbackURL = Constants.getString("TUMBLR_CALLBACK_URL");


    private Token accessToken;
    private static final Token EMPTY_TOKEN = null;


    final static Logger logger = LogManager.getLogger(AuthServices.class);
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
    @Path("/tumblr")
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticateTumblrForUser(@Context HttpServletRequest request)
    {
        Token requestToken = null;

        String humans_access_token = request.getParameter("access_token");
        if(humans_access_token == null && (request.getParameter("oauth_token") == null && request.getParameter("oauth_verifier") == null) ) {
            fail_response.addProperty("message", "invalid or missing access token");
            return Response.status(Response.Status.UNAUTHORIZED).entity(fail_response.toString()).type(MediaType.APPLICATION_JSON).build();
        }

        HumansUser user;
        HttpSession session = request.getSession(true);

        logger.debug("now session="+session.getId());
        logger.debug("session contents="+session.getAttributeNames());
        logger.debug("session(logged-in-user)="+session.getAttribute("logged-in-user"));
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

        OAuthService service = new ServiceBuilder()
                //.provider(TwitterApi.SSL.class)
                .provider(TumblrApi.class)
                .apiKey(tumblrAPIKey)
                .apiSecret(tumblrAPISecret)
                .callback(tumblrCallbackURL)
                        //.scope("basic,likes")
                .build();
        //TwitterService twitter;

        if(request.getParameter("oauth_token")!=null && request.getParameter("oauth_verifier") != null) {
            // then this'll go second in the authentication flow
            //logger.debug("Up Here Token is "+requestToken.toString());
            requestToken = (Token)session.getAttribute("request-token");
            Verifier verifier = new Verifier(request.getParameter("oauth_verifier"));
            accessToken = service.getAccessToken(requestToken, verifier);
            /**
            tumblr = new TumblrService(accessToken);
            TumblrUser tumblrUser = tumblr.serviceRequestUserBasic();

            logger.info("just got access token for twitter - " + tumblr.getThisUser());


            tumblr.serviceRequestFollows();
            //logger.debug("screen_name is "+twitter.getThisUser().getScreen_name()+" "+twitter.getThisUser().getId());
            user.addService(tumblr.getThisUser().getId(), tumblr.getThisUser().getScreen_name(),"tumblr" );
            user.updateYouman();
            user.save();

            TumblrUserDAO dao = new TumblrUserDAO();
            dao.save(tumblr.getThisUser());

            TumblrService.serializeToken(accessToken, tumblr.getThisUser());
            Gson gson = new Gson();

            // convert java object to JSON format,
            // and returned as JSON formatted string
            String userJson = gson.toJson(tumblrUser);
            //twitter.getFriends();
            //return Response.ok(userJson, MediaType.APPLICATION_JSON).build();
             **/

            OAuthRequest auth_request = new OAuthRequest( Verb.GET ,
                    "http://api.tumblr.com/v2/user/following" );
            service.signRequest(accessToken, auth_request);
            org.scribe.model.Response auth_response = auth_request.send();
            String bar = auth_response.getBody();
            auth_request = new OAuthRequest(Verb.GET, "http://api.tumblr.com/v2/blog/unhappyhipsters.tumblr.com/posts/?api_key="+tumblrAPIKey+"&filter=text");
            service.signRequest(accessToken, auth_request);
            auth_response = auth_request.send();
            bar = auth_response.getBody();
            return Response.ok().entity(bar).type(MediaType.APPLICATION_JSON).build();
        } else {
            // this'll go first in the authentication flow
            requestToken = service.getRequestToken();
            session.setAttribute("request-token", requestToken);
            logger.debug("Now Request Token is "+requestToken);
            String authorizationUrl = service.getAuthorizationUrl(requestToken);
            return Response.seeOther(URI.create(authorizationUrl)).build();
        }


    }


    @GET
    @Path("/twitter")
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticateTwitterForUser(@Context HttpServletRequest request,
                                        @Context HttpServletResponse response)
    {
        Token requestToken = null;

        String humans_access_token = request.getParameter("access_token");
        if(humans_access_token == null && (request.getParameter("oauth_token") == null && request.getParameter("oauth_verifier") == null) ) {
            fail_response.addProperty("message", "invalid or missing access token");
            return Response.status(Response.Status.UNAUTHORIZED).entity(fail_response.toString()).type(MediaType.APPLICATION_JSON).build();
        }

        HumansUser user;
        HttpSession session = request.getSession(true);

        logger.debug("now session="+session.getId());
        logger.debug("session contents="+session.getAttributeNames());
        logger.debug("session(logged-in-user)="+session.getAttribute("logged-in-user"));
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

        OAuthService service = new ServiceBuilder()
                //.provider(TwitterApi.SSL.class)
                .provider(TwitterApi.SSL.class)
                .apiKey(twitterAPIKey)
                .apiSecret(twitterAPISecret)
                .callback(twitterCallbackURL)
                //.scope("basic,likes")
                .build();
        TwitterService twitter;

        if(request.getParameter("oauth_token")!=null && request.getParameter("oauth_verifier") != null) {
            // then this'll go second in the authentication flow
            //logger.debug("Up Here Token is "+requestToken.toString());
            requestToken = (Token)session.getAttribute("request-token");
            Verifier verifier = new Verifier(request.getParameter("oauth_verifier"));
            accessToken = service.getAccessToken(requestToken, verifier);
            twitter = new TwitterService(accessToken);
            TwitterUser twitterUser = twitter.serviceRequestUserBasic();

            logger.info("just got access token for twitter - " + twitter.getThisUser());


            twitter.serviceRequestFollows();
            //logger.debug("screen_name is "+twitter.getThisUser().getScreen_name()+" "+twitter.getThisUser().getId());
            user.addService(twitter.getThisUser().getId(), twitter.getThisUser().getScreen_name(),"twitter" );
            user.updateYouman();
            user.save();

            TwitterUserDAO dao = new TwitterUserDAO();
            dao.save(twitter.getThisUser());

            TwitterService.serializeToken(accessToken, twitter.getThisUser());
            Gson gson = new Gson();

            // convert java object to JSON format,
            // and returned as JSON formatted string
            String userJson = gson.toJson(twitterUser);
            //twitter.getFriends();
            //return Response.ok(userJson, MediaType.APPLICATION_JSON).build();
            return Response.ok().entity(userJson).type(MediaType.APPLICATION_JSON).build();
        } else {
            // this'll go first in the authentication flow
            requestToken = service.getRequestToken();
            session.setAttribute("request-token", requestToken);
            logger.debug("Now Request Token is "+requestToken);
            String authorizationUrl = service.getAuthorizationUrl(requestToken);
            return Response.seeOther(URI.create(authorizationUrl)).build();
        }

    }

    @GET
    @Path("/instagram")
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticateInstagramForUser(@Context HttpServletRequest request,
                                                 @Context HttpServletResponse response)
    {
        //TODO THis should be new style using RestCommon
        String humans_access_token = request.getParameter("access_token");
        if(humans_access_token == null && request.getParameter("code") == null) {
            fail_response.addProperty("message", "invalid or missing access token");
            return Response.status(Response.Status.UNAUTHORIZED).entity(fail_response.toString()).type(MediaType.APPLICATION_JSON).build();
        }

        HumansUser user = null;
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

        OAuthService service = new ServiceBuilder()
                .provider(InstagramApi.class)
                .apiKey(instagramAPIKey)
                .apiSecret(instagramAPISecret)
                .callback(instagramCallbackURL)
                .scope("likes")
                .build();


        if(request.getParameter("code") != null) {
        try {
            logger.debug("now a response code="+request.getParameter("code"));
            Verifier verifier = new Verifier(request.getParameter("code"));
            accessToken = service.getAccessToken(EMPTY_TOKEN, verifier);
            logger.debug("and access token "+accessToken);

            InstagramService instagramService = new InstagramService(accessToken);
            InstagramUser instagramUser = instagramService.serviceRequestUserBasic();


            logger.debug("just got access for instagram user="+instagramUser);

            logger.info("just got access token for instagram - "+instagramService.getThisUser());
            InstagramService.serializeToken(accessToken, instagramService.getThisUser());
            //HttpSession session = request.getSession(true);


            //logger.debug("screen_name is "+instagramService.getThisUser().getUsername()+" "+instagramService.getThisUser().getId());
            user.addService( (String)instagramService.getThisUser().getId(),  (String)instagramService.getThisUser().getUsername(),"instagram");

            user.updateYouman();

            user.save();

            InstagramUserDAO dao = new InstagramUserDAO();
            dao.save(instagramService.getThisUser());

            InstagramService.serializeToken(accessToken, instagramService.getThisUser());


            Gson gson = new Gson();

            // convert java object to JSON format,
            // and returned as JSON formatted string
            String userJson = gson.toJson(instagramUser);
            //instagramService.getFriends();
            //return Response.ok(userJson, MediaType.APPLICATION_JSON).build();
            return Response.ok().entity(userJson).type(MediaType.APPLICATION_JSON).build();
        } catch(BadAccessTokenException bate) {
            fail_response.addProperty("error", bate.getMessage());
            return Response.status(400).entity(fail_response.toString()).type(MediaType.APPLICATION_JSON).build();
        }
        } else {
            try {

                String authorizationUrl = service.getAuthorizationUrl(EMPTY_TOKEN);
                logger.debug("Authorization URL=" + authorizationUrl);
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
                return Response.ok().entity(fail_response.toString()).type(MediaType.APPLICATION_JSON).build();
            }
        }
        //return Response.status(Response.Status.BAD_REQUEST).entity(fail_response).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @GET
    @Path("/foursquare")
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticateFoursquareForUser(@Context HttpServletRequest request,
                                                  @Context HttpServletResponse response)
    {
        String humans_access_token = request.getParameter("access_token");
        logger.debug("access_token = "+humans_access_token);

        if(humans_access_token == null && request.getParameter("code") == null) {
            fail_response.addProperty("message", "invalid or missing access token");
            return Response.status(Response.Status.UNAUTHORIZED).entity(fail_response.toString()).type(MediaType.APPLICATION_JSON).build();
        }

        HumansUser user = null;
        HttpSession session = request.getSession(true);

        logger.debug("now session="+session.getId());
        logger.debug("session contents="+session.getAttributeNames());
        logger.debug("session(logged-in-user)="+session.getAttribute("logged-in-user"));

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

        OAuthService service = new ServiceBuilder()
                .provider(Foursquare2Api.class)
                .apiKey(foursquareAPIKey)
                .apiSecret(foursquareAPISecret)
                .callback(foursquareCallbackURL)
                .scope("basic+likes")
                .build();


        if(request.getParameter("code") != null) {

            logger.debug("now a response code="+request.getParameter("code"));
            Verifier verifier = new Verifier(request.getParameter("code"));
            accessToken = service.getAccessToken(EMPTY_TOKEN, verifier);
            logger.debug("and access token "+accessToken);


            FoursquareService foursquareService = new FoursquareService(accessToken);
            FoursquareUser foursquareUser = foursquareService.serviceRequestUserBasic();


            logger.info("just got access for foursquare user=" + foursquareUser);
            //logger.info("just got access token for foursquare - "+foursquareService.getThisUser());
            FoursquareService.serializeToken(accessToken, foursquareService.getThisUser());
            //HttpSession session = request.getSession(true);


            //logger.debug("screen_name is "+instagramService.getThisUser().getUsername()+" "+instagramService.getThisUser().getId());
            user.addService( (String)foursquareService.getThisUser().getId(),  (String)foursquareService.getThisUser().getUsername(),"foursquare");
            user.updateYouman();
            user.save();

            FoursquareUserDAO dao = new FoursquareUserDAO();
            dao.save(foursquareService.getThisUser());

            FoursquareService.serializeToken(accessToken, foursquareService.getThisUser());


            Gson gson = new Gson();

            // convert java object to JSON format,
            // and returned as JSON formatted string
            String userJson = gson.toJson(foursquareUser);
            //instagramService.getFriends();
            //return Response.ok(userJson, MediaType.APPLICATION_JSON).build();
            return Response.ok().entity(userJson).type(MediaType.APPLICATION_JSON).build();
        } else {
            try {

                String authorizationUrl = service.getAuthorizationUrl(EMPTY_TOKEN);
                logger.debug("Authorization URL=" + authorizationUrl);
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
                return Response.ok().entity(fail_response.toString()).type(MediaType.APPLICATION_JSON).build();
            }
        }
        //return Response.status(Response.Status.BAD_REQUEST).entity(fail_response).type(MediaType.APPLICATION_JSON_TYPE).build();

    }

    @GET
    @Path("/flickr")
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticateFlickrForUser(@Context HttpServletRequest request,
                                               @Context HttpServletResponse response)
    {
        String humans_access_token = request.getParameter("access_token");
        logger.debug("access_token = "+humans_access_token);
        if(humans_access_token == null && (request.getParameter("oauth_token") == null && request.getParameter("oauth_verifier") == null)) {
            fail_response.addProperty("message", "invalid or missing access token");
            fail_response.addProperty("parameters", request.getParameterMap().toString());
            return Response.status(Response.Status.UNAUTHORIZED).entity(fail_response.toString()).type(MediaType.APPLICATION_JSON).build();
        }
        Token requestToken = null;
        HumansUser user;
        HttpSession session = request.getSession(true);
        FlickrService flickr;

        logger.debug("now session="+session.getId());
        logger.debug("session contents="+session.getAttributeNames());
        logger.debug("session(logged-in-user)="+session.getAttribute("logged-in-user"));
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

        OAuthService service = new ServiceBuilder()
                .provider(FlickrApi.class)
                .apiKey(flickrAPIKey)
                .apiSecret(flickrAPISecret)
                .callback(flickrCallbackURL)
                .build();


        //logger.debug("oauth_token="+req.getParameter("oauth_token")+" oauth_verifier="+req.getParameter("oauth_verifier"));

        if(request.getParameter("oauth_token")!=null && request.getParameter("oauth_verifier") != null) {
            requestToken = (Token)session.getAttribute("request-token");

            // then this'll go second in the authentication flow
            //logger.debug("Up Here Token is "+requestToken.toString());
            Verifier verifier = new Verifier(request.getParameter("oauth_verifier"));
            accessToken = service.getAccessToken(requestToken, verifier);
            //Flickr = FlickrService.createFlickrServiceOnBehalfOfCodedUsername(aCodedUsername);//new FlickrService(accessToken);
            flickr = new FlickrService(accessToken);
            FlickrUser flickrUser = flickr.serviceRequestUserInfo();

            //flickr.serviceRequestFriends();
            //logger.debug("User is "+Flickr.getThisUser());
            logger.debug("just got auth token for flickr - username is "+flickr.getThisUser().getUsername()+" "+flickr.getThisUser().getId());

            user.addService( (String) flickr.getThisUser().getId(),  (String) flickr.getThisUser().getUsername(), "flickr");
            user.updateYouman();
            user.save();

            FlickrUserDAO dao = new FlickrUserDAO();
            dao.save(flickr.getThisUser());

            flickr.serializeToken(accessToken);
            session.setAttribute("logged-in-user", user);
            Gson gson = new Gson();

            // convert java object to JSON format,
            // and returned as JSON formatted string
            String userJson = gson.toJson(flickrUser);
            //instagramService.getFriends();
            //return Response.ok(userJson, MediaType.APPLICATION_JSON).build();
            return Response.ok().entity(userJson).type(MediaType.APPLICATION_JSON).build();

            //resp.sendRedirect(request.getContextPath() + "/services.jsp");
        } else {
            // this'll go first in the authentication flow
            requestToken = service.getRequestToken();
            session.setAttribute("request-token", requestToken);
            logger.debug("Now Request Token is "+requestToken);
            String authUrl = service.getAuthorizationUrl(requestToken)+"&perms=write";
            logger.debug("And authURL is "+authUrl);

            return Response.seeOther(URI.create(authUrl)).build();
            //resp.sendRedirect(authUrl);
            //		Verifier verifier = new Verifier()
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
