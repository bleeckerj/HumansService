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

import com.nearfuturelaboratory.humans.entities.HumansUser;
import com.nearfuturelaboratory.humans.service.TwitterService;
import com.nearfuturelaboratory.util.*;

import java.io.*;

import org.apache.log4j.Logger;

@WebServlet(name = "TwitterLoginServlet", urlPatterns = {"/login-twitter", "/TwitterLogin", "/scrumpy-twitter"}
		)
public class TwitterLoginServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2472125196689613326L;
	private OAuthService service;
	private String apiKey = Constants.getString("TWITTER_API_KEY");//"09ARKva0K7HMz1DW1GUg";
	private String apiSecret = Constants.getString("TWITTER_API_SECRET");//"rwy7rZ2Uu3lkliYMfOaJD4UeUHFebDqXXrBgjnT8Rw";
	private String callbackURL = Constants.getString("TWITTER_CALLBACK_URL");//"http://localhost:8080/HumansService/scrumpy-twitter";
	private Token accessToken;
	protected JSONObject user;
	protected TwitterService twitter;


	final static Logger logger = Logger.getLogger("com.nearfuturelaboratory.humans.test.Test");
	protected Token requestToken;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		service = new ServiceBuilder()
		.provider(TwitterApi.class)
		.apiKey(apiKey)
		.apiSecret(apiSecret)
		.callback(callbackURL)
		.build();

		HttpSession session = req.getSession();
		HumansUser user = (HumansUser)session.getAttribute("logged-in-user");
		if(user == null) {
			resp.sendRedirect(req.getContextPath()+"/login.jsp");
			return;
		}
		
		if(req.getParameter("oauth_token")!=null && req.getParameter("oauth_verifier") != null) {
			// then this'll go second in the authentication flow
			//logger.debug("Up Here Token is "+requestToken.toString());
			Verifier verifier = new Verifier(req.getParameter("oauth_verifier"));
			accessToken = service.getAccessToken(requestToken, verifier);
			twitter = new TwitterService(accessToken);
			twitter.serviceRequestUserBasic();
			
			twitter.serviceRequestFollows();
			//logger.debug("screen_name is "+twitter.getThisUser().getScreen_name()+" "+twitter.getThisUser().getId());
			user.addService(twitter.getThisUser().getId(), twitter.getThisUser().getScreen_name(),"twitter" );
			//logger.debug("And user is now "+user);
			user.save();
			TwitterService.serializeToken(accessToken, twitter.getThisUser());
			session.setAttribute("logged-in-user", user);
			resp.sendRedirect(req.getContextPath()+"/services.jsp");
		} else {
			// this'll go first in the authentication flow
			requestToken = service.getRequestToken();
			logger.debug("Now Request Token is "+requestToken);
			String authUrl = service.getAuthorizationUrl(requestToken);
			resp.sendRedirect(authUrl);
		}
	}

}
