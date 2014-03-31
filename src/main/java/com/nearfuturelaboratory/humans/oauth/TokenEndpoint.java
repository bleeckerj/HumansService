package com.nearfuturelaboratory.humans.oauth;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.JsonObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.as.request.OAuthUnauthenticatedTokenRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import com.nearfuturelaboratory.humans.dao.HumansUserDAO;
import com.nearfuturelaboratory.humans.entities.HumansUser;
import com.nearfuturelaboratory.humans.entities.InvalidUserException;
//import com.bilyoner.api.demo.TestContent;
import com.nearfuturelaboratory.humans.oauth.TestContent;
import com.nearfuturelaboratory.util.Constants;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;


/**
 * Basically the endpoint to Login and get an access_token, which clients would use for all subsequent requests.
 *	https://humans.nearfuturelaboratory.com:8443/oauth2/token?grant_type=password&username=darthjulian&password=darthjulian&client_id=ioshumans
 *
 */
@Path("/token")
public class TokenEndpoint {
    final static Logger logger = LogManager.getLogger(com.nearfuturelaboratory.humans.oauth.TokenEndpoint.class);
    static JsonObject invalid_user_error_response;
    static JsonObject success_response;
    static JsonObject fail_response;
    static JsonObject no_such_human_for_user;
    static JsonObject no_such_serviceuser_for_user;

    static {

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

    //@SuppressWarnings("unused")
    @GET
    //@Consumes("application/x-www-form-urlencoded")
    //@Consumes("application/json")
    @Produces("application/json")
    public Response authorize(@Context HttpServletRequest request) throws OAuthSystemException {


        Base64 decoder = new Base64();
        String auth = request.getHeader("Authorization");
        String foo = auth.substring(6);
        byte[] bar = decoder.decode(foo);
        Map<?, ?> params = request.getParameterMap();
        String d_auth = new String( bar );
        logger.debug("d_auth="+d_auth);
        String username;
        String password;
        if(d_auth != null) {
            String[] parts = d_auth.split(":");
            if(parts != null && parts.length == 2) {
                username = parts[0];
                password = parts[1];
            } else {
                fail_response.addProperty("message", "invalid authorization");
                return Response.status(HttpServletResponse.SC_NOT_ACCEPTABLE).entity(fail_response.toString()).build();

            }
        } else {
            fail_response.addProperty("message", "incomplete authorization");
            return Response.status(HttpServletResponse.SC_NOT_ACCEPTABLE).entity(fail_response).build();
        }
        try {
            OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
//        logger.debug("Token Endpoint "+request);

//			oauthRequest = new OAuthUnauthenticatedTokenRequest(request);
//			logger.debug("oauthRequest="+oauthRequest);
            //check if clientid is valid
            if (!Constants.getString("CLIENT_ID").equals(request.getParameter(OAuth.OAUTH_CLIENT_ID))) {
//
//                return Response.status(HttpServletResponse.SC_BAD_REQUEST)
//                                .entity(fail_response.toString()).build();

                return Response.status(Response.Status.BAD_REQUEST).entity(fail_response.toString()).build();
            }

            if (request.getParameter(OAuth.OAUTH_GRANT_TYPE)
                    .equals(GrantType.PASSWORD.toString())) {
                HumansUserDAO dao = new HumansUserDAO();
                HumansUser user = dao.findOneByUsername(username); //new HumansUser(oauthRequest.getUsername(), oauthRequest.getPassword());
                if (user == null) {
//                    OAuthResponse response = OAuthASResponse
//                            .errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
//                            .setError(OAuthError.TokenResponse.INVALID_CLIENT)
//                            .setErrorDescription("invalid username or password")
//                            .buildJSONMessage();
                    fail_response.addProperty("message", "invalid username or password");

                    return Response.status(Response.Status.UNAUTHORIZED).entity(fail_response.toString()).build();

                    //return Response.status(HttpServletResponse.SC_UNAUTHORIZED)
                    //        .entity(fail_response.toString()).build();

                }

                if (user.verifyPassword(password)) {
                    String access_token = oauthIssuerImpl.accessToken();
                    user.setAccessToken(access_token);
                    user.save();
                    //System.out.println("Token="+token);
//                    OAuthResponse response = OAuthASResponse
//                            .tokenResponse(HttpServletResponse.SC_ACCEPTED)
//                            .setAccessToken(access_token)
//							/*.setExpiresIn("3600")*/
//                            .buildJSONMessage();
                    success_response.addProperty("access_token", access_token);
                    return Response.ok(success_response.toString()).type(MediaType.APPLICATION_JSON).build();
                    //return Response.status(response.getResponseStatus()).entity(response.getBody()).build();

                } else {
                    user.setAccessToken(null);
                    user.save();
//                    OAuthResponse response = OAuthASResponse
//                            .errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
//                            .setError(OAuthError.TokenResponse.INVALID_GRANT)
//                            .setErrorDescription("invalid username or password")
//                            .buildJSONMessage();
                    return Response.status(Response.Status.UNAUTHORIZED).entity(fail_response.toString()).build();
//                    return Response.status(response.getResponseStatus()).entity(response.getBody()).build();

                }

            }
            // otherwise, invalid grant type
//            OAuthResponse response = OAuthASResponse
//                    .errorResponse(HttpServletResponse.SC_BAD_REQUEST)
//                    .setError(OAuthError.TokenResponse.INVALID_GRANT)
//                    .setErrorDescription("invalid grant")
//                    .buildJSONMessage();
            return Response.status(Response.Status.BAD_REQUEST).entity(fail_response.toString()).build();
//            return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
            fail_response.addProperty("exception", e.getMessage());
            return Response.status(Response.Status.BAD_GATEWAY).entity(fail_response.toString()).build();
        }



    }

    @POST @Path("/foo")
    //@Consumes("application/json")
    @Produces("application/json")
    public Response foo(@Context HttpServletRequest request) throws OAuthSystemException {
        Base64 decoder = new Base64();
        String auth = request.getHeader("Authorization");
        String foo = auth.substring(6);
        byte[] bar = decoder.decode(foo);
        Map<?, ?> params = request.getParameterMap();
        String d_auth = new String( bar );
        logger.debug("d_auth="+d_auth);



        return null;
    }
//	@GET
//	//@Consumes("application/x-www-form-urlencoded")
//	@Produces("application/json")
//	public Response authorizeGet(@Context HttpServletRequest request) throws OAuthSystemException {
//		return this.authorize(request);
//		OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
//
//		OAuthResponse response = OAuthASResponse
//				.tokenResponse(HttpServletResponse.SC_OK)
//				.setAccessToken(oauthIssuerImpl.accessToken())
//				.setExpiresIn("3600")
//				.buildJSONMessage();
//
//		return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
//	}

}