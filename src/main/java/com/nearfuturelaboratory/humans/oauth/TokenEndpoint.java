package com.nearfuturelaboratory.humans.oauth;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

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

/**
 * Basically the endpoint to Login and get an access_token, which clients would use for all subsequent requests.
 *	https://humans.nearfuturelaboratory.com:8443/oauth2/token?grant_type=password&username=darthjulian&password=darthjulian&client_id=ioshumans
 *
 */
@Path("/token")
public class TokenEndpoint {
	final static Logger logger = LogManager.getLogger(com.nearfuturelaboratory.humans.oauth.TokenEndpoint.class);

	//@SuppressWarnings("unused")
	@POST
	@Consumes("application/x-www-form-urlencoded")
	//@Consumes("application/json")
	@Produces("application/json")
	public Response authorize(@Context HttpServletRequest request) throws OAuthSystemException {

		//OAuthTokenRequest oauthRequest = null;
		OAuthUnauthenticatedTokenRequest oauthRequest = null;
		OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());

		try {
			oauthRequest = new OAuthUnauthenticatedTokenRequest(request);

			//check if clientid is valid
			if (!Constants.getString("CLIENT_ID").equals(oauthRequest.getParam(OAuth.OAUTH_CLIENT_ID))) {
				
				OAuthResponse response =
						OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
						.setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("invalid client")
						.buildJSONMessage();
				return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
			}

			if (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE)
					.equals(GrantType.PASSWORD.toString())) {
				HumansUserDAO dao = new HumansUserDAO();
				HumansUser user = dao.findOneByUsername(oauthRequest.getUsername()); //new HumansUser(oauthRequest.getUsername(), oauthRequest.getPassword());
				if(user == null) {
					OAuthResponse response = OAuthASResponse
							.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
							.setError(OAuthError.TokenResponse.INVALID_CLIENT)
							.setErrorDescription("invalid username or password")
							.buildJSONMessage();
					return Response.status(response.getResponseStatus()).entity(response.getBody()).build();

				}
				
				if(user.verifyPassword(oauthRequest.getPassword())) {
					String access_token = oauthIssuerImpl.accessToken();
					user.setAccessToken(access_token);
					user.save();
					//System.out.println("Token="+token);
					OAuthResponse response = OAuthASResponse
							.tokenResponse(HttpServletResponse.SC_ACCEPTED)
							.setAccessToken(access_token)
							/*.setExpiresIn("3600")*/
							.buildJSONMessage();

					return Response.status(response.getResponseStatus()).entity(response.getBody()).build();

				} else {
					user.setAccessToken(null);
					user.save();
					OAuthResponse response = OAuthASResponse
							.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
							.setError(OAuthError.TokenResponse.INVALID_GRANT)
							.setErrorDescription("invalid username or password")
							.buildJSONMessage();
					return Response.status(response.getResponseStatus()).entity(response.getBody()).build();

				}
				
			}
			// otherwise, invalid grant type
			OAuthResponse response = OAuthASResponse
					.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
					.setError(OAuthError.TokenResponse.INVALID_GRANT)
					.setErrorDescription("invalid grant")
					.buildJSONMessage();
			return Response.status(response.getResponseStatus()).entity(response.getBody()).build();


		} catch (OAuthProblemException e) {
			OAuthResponse res = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST).
					setErrorDescription("invalid request").error(e)
					.buildJSONMessage();
			e.printStackTrace();
			logger.warn("bad request for oauth. oauth problems");
			return Response.status(res.getResponseStatus()).entity(res.getBody()).build();
		}

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