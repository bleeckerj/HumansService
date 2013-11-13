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

import com.nearfuturelaboratory.humans.service.*;
import com.nearfuturelaboratory.util.*;
import com.nearfuturelaboratory.humans.core.*;

import java.io.*;

import org.apache.log4j.Logger;

@WebServlet(name = "FlickrLoginServlet", urlPatterns = {"/login-flickr", "/FlickrLogin"}
		)
public class FlickrLoginServlet extends HttpServlet {

	private OAuthService service;
	private String apiKey = Constants.getString("FLICKR_API_KEY");
	private String apiSecret = Constants.getString("FLICKR_API_SECRET");
	private String callbackURL = Constants.getString("FLICKR_CALLBACK_URL");
	private static final Token EMPTY_TOKEN = null;


	private Token accessToken;
	protected JSONObject user;
	protected FlickrService flickr;


	final static Logger logger = Logger.getLogger("com.nearfuturelaboratory.humans.test.Test");
	protected Token requestToken;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		service = new ServiceBuilder()
		.provider(FlickrApi.class)
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
		
		//logger.debug("oauth_token="+req.getParameter("oauth_token")+" oauth_verifier="+req.getParameter("oauth_verifier"));
		
		if(req.getParameter("oauth_token")!=null && req.getParameter("oauth_verifier") != null) {
			// then this'll go second in the authentication flow
			//logger.debug("Up Here Token is "+requestToken.toString());
			Verifier verifier = new Verifier(req.getParameter("oauth_verifier"));
			accessToken = service.getAccessToken(requestToken, verifier);
			//Flickr = FlickrService.createFlickrServiceOnBehalfOfCodedUsername(aCodedUsername);//new FlickrService(accessToken);
			flickr = new FlickrService(accessToken);
			flickr.serviceRequestUserInfo();
			
			flickr.getFollows();
			//logger.debug("User is "+Flickr.getThisUser());
			logger.debug("username is "+flickr.getFilesafeUsername()+" "+flickr.getThisUser().get("id"));
			user.addServiceForHuman("Flickr", (String)flickr.getFilesafeUsername(), (String)flickr.getThisUser().get("id"));
			
			
			flickr.serializeToken(accessToken);
			session.setAttribute("logged-in-user", user);
			resp.sendRedirect(req.getContextPath()+"/services.jsp");
		} else {
			// this'll go first in the authentication flow
			requestToken = service.getRequestToken();
			logger.debug("Now Request Token is "+requestToken);
			String authUrl = service.getAuthorizationUrl(requestToken);
			resp.sendRedirect(authUrl);
			//		Verifier verifier = new Verifier()
		}
	}

}
