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

import com.nearfuturelaboratory.humans.core.HumansUser;
import com.nearfuturelaboratory.humans.serviceapi.InstagramApi;
import com.nearfuturelaboratory.humans.service.*;
import com.nearfuturelaboratory.util.*;

import java.io.*;
import java.util.Scanner;

import org.apache.log4j.Logger;

@WebServlet(name = "FoursquareLoginServlet", urlPatterns = {"/login-foursquare", "/FoursquareLogin", "/scrumpy-foursquare"},
initParams = {@WebInitParam(name="tokens-file-root", value="/Volumes/Slippy/Users/julian/Documents/workspace/HumansService")}
		)
public class FoursquareLoginServlet extends HttpServlet {

	private OAuthService service;
	private static String apiKey = Constants.getString("FOURSQUARE_API_KEY");//"MKGJ3OZYTDNZAI5ZMROF3PAMAUND0ZO2HYRTZYXHIIR5TW1Q";
	private static String apiSecret = Constants.getString("FOURSQUARE_API_SECRET");//"2G0DUIFCFAWBH1WPIYBUDQMESKRLFLGY5PHXY0BJNBE1MMN4";
	private String callbackURL = Constants.getString("FOURSQUARE_CALLBACK_URL");//"http://localhost:8080/HumansService/FoursquareLogin";
	private static final Token EMPTY_TOKEN = null;

	private Token accessToken;
	protected JSONObject user;
	//protected FoursquareService foursquare;

	final static Logger logger = Logger.getLogger("com.nearfuturelaboratory.humans.test.Test");
	protected Token requestToken;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		// TODO Auto-generated constructor stub
		//resp.
		OAuthService service = new ServiceBuilder()
		.provider(Foursquare2Api.class)
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

		if(req.getParameter("code") != null) {
			logger.debug("code is "+req.getParameter("code"));
			Verifier verifier = new Verifier(req.getParameter("code"));
			accessToken = service.getAccessToken(EMPTY_TOKEN, verifier);
			logger.debug("access token is "+accessToken.getToken());
			
			FoursquareService foursquareService = new FoursquareService(accessToken);
			foursquareService.serviceRequestUserBasic();
			foursquareService.getFollows();
			//logger.debug("User is "+Flickr.getThisUser());
			logger.debug("username is "+foursquareService.getCodedUsername()+" "+foursquareService.getThisUser().get("id"));
			user.addServiceForHuman("Foursquare", (String)foursquareService.getDerivedUsername(), (String)foursquareService.getThisUser().get("id"));


			FoursquareService.serializeToken(accessToken, foursquareService.getThisUser());
			session.setAttribute("logged-in-user", user);
			resp.sendRedirect(req.getContextPath()+"/services.jsp");

		} else {
			String authorizationUrl = service.getAuthorizationUrl(EMPTY_TOKEN);
			logger.info("Authorization URL="+authorizationUrl);
			resp.sendRedirect(authorizationUrl);

		}

	}

}
