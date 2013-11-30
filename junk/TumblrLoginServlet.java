package com.nearfuturelaboratory.humans.servlets;

import javax.servlet.annotation.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;
import org.scribe.builder.*;
import org.scribe.builder.api.*;
import org.scribe.model.*;
import org.scribe.oauth.*;

import com.nearfuturelaboratory.humans.serviceapi.InstagramApi;
import com.nearfuturelaboratory.humans.service.*;
import com.nearfuturelaboratory.util.*;
import com.nearfuturelaboratory.humans.core.*;

import java.io.*;

import org.apache.log4j.Logger;



@WebServlet(name = "TumblrLoginServlet", urlPatterns = {"/login-tumblr", "/TumblrLogin", "/scrumpy-tumblr"}
		)
public class TumblrLoginServlet extends HttpServlet {

	private OAuthService service;
	private String apiKey = Constants.getString("TUMBLR_API_KEY");
	private String apiSecret = Constants.getString("TUMBLR_API_SECRET");
	private String callbackURL = Constants.getString("TUMBLR_CALLBACK_URL");
	private static final Token EMPTY_TOKEN = null;


	private Token accessToken;
	protected JSONObject user;
	protected TumblrService tumblr;


	final static Logger logger = Logger.getLogger("com.nearfuturelaboratory.humans.test.Test");
	protected Token requestToken;
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		service = new ServiceBuilder()
		.provider(TumblrApi.class)
		.apiKey(apiKey)
		.apiSecret(apiSecret)
		.callback(callbackURL)
		.build();


		logger.debug("Request Parameters are "+req.getParameterMap());
		HttpSession session = req.getSession();
		HumansUser user = (HumansUser)session.getAttribute("logged-in-user");
		if(user == null) {
			resp.sendRedirect(req.getContextPath()+"/login.jsp");
			return;
		}
		
		logger.debug(req.getParameterMap());
		
		// second
		if(req.getParameter("oauth_token")!=null && req.getParameter("oauth_verifier") != null) {
			// then this'll go second in the authentication flow
			//logger.debug("Up Here Token is "+requestToken.toString());
			Verifier verifier = new Verifier(req.getParameter("oauth_verifier"));
			accessToken = service.getAccessToken(requestToken, verifier);
			logger.debug("Got access token "+accessToken);
			tumblr = new TumblrService(accessToken);
			tumblr.serviceRequestUserBasic();
			
			//tumblr.getFollows();
			logger.debug("User is "+tumblr.getThisUser());
			logger.debug("name is "+tumblr.getThisUser().get("name"));
			user.addServiceForHuman("tumblr", (String)tumblr.getThisUser().get("name"), "");
			
			
			serializeToken(accessToken, tumblr.getThisUser());
			session.setAttribute("logged-in-user", user);
			resp.sendRedirect(req.getContextPath()+"/services.jsp");
		} else 
		// first
		{
			// this'll go first in the authentication flow
			requestToken = service.getRequestToken();
			logger.debug("Now Request Token is "+requestToken);
			String authUrl = service.getAuthorizationUrl(requestToken);
			resp.sendRedirect(authUrl);
			//		Verifier verifier = new Verifier()
		}

		
		
	}
	
	
	
	static void serializeToken(Token aToken, JSONObject aUser) {
		try{
			//use buffering
			String path = Constants.getString("SERVICE_DATA_ROOT")+"/tumblr/users/"+aUser.get("name")+"/tumblr-token-for-"+aUser.get("name")+".ser";

			OutputStream file = new FileOutputStream( path );
			OutputStream buffer = new BufferedOutputStream( file );
			ObjectOutput output = new ObjectOutputStream( buffer );
			try{
				output.writeObject(aToken);
			}
			finally{
				output.close();
			}
		}  
		catch(IOException ex){
			//fLogger.log(Level.SEVERE, "Cannot perform output.", ex);
			//ex.printStackTrace();
			logger.error(ex);
		}

	}

}
