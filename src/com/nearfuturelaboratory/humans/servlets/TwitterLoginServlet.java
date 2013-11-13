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

@WebServlet(name = "TwitterLoginServlet", urlPatterns = {"/login-twitter", "/TwitterLogin", "/scrumpy-twitter"}
		)
public class TwitterLoginServlet extends HttpServlet {

	private OAuthService service;
	private String apiKey = Constants.getString("TWITTER_API_KEY");//"09ARKva0K7HMz1DW1GUg";
	private String apiSecret = Constants.getString("TWITTER_API_SECRET");//"rwy7rZ2Uu3lkliYMfOaJD4UeUHFebDqXXrBgjnT8Rw";
	private String callbackURL = Constants.getString("TWITTER_CALLBACK_URL");//"http://localhost:8080/HumansService/scrumpy-twitter";
	private static final Token EMPTY_TOKEN = null;


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
			//twitter = TwitterService.createTwitterServiceOnBehalfOfCodedUsername(aCodedUsername);//new TwitterService(accessToken);
			twitter = new TwitterService(accessToken);
			twitter.serviceRequestUserBasic();
			
			twitter.getFollows();
			//logger.debug("User is "+twitter.getThisUser());
			logger.debug("screen_name is "+twitter.getThisUser().get("screen_name")+" "+twitter.getThisUser().get("id_str")+" "+twitter.getThisUser().get("id"));
			user.addServiceForHuman("twitter", (String)twitter.getThisUser().get("screen_name"), (String)twitter.getThisUser().get("id_str"));
			
			
			serializeToken(accessToken, twitter.getThisUser());
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

	static void serializeToken(Token aToken, JSONObject aUser) {
		try{
			//use buffering
			String path = Constants.getString("SERVICE_DATA_ROOT")+"/twitter/users/"+aUser.get("id")+"-"+aUser.get("screen_name")+"/twitter-token-for-"+aUser.get("id")+"-"+aUser.get("screen_name")+".ser";

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
			ex.printStackTrace();
		}

	}

	static Token deserializeToken(String aTwitterUsername) {
		Token result = null;
		try{
			//use buffering
			InputStream file = new FileInputStream( "twitter-token-for-"+aTwitterUsername+".ser" );
			InputStream buffer = new BufferedInputStream( file );
			ObjectInput input = new ObjectInputStream ( buffer );
			try{
				//deserialize the List
				result = (Token)input.readObject();
				//display its data
				System.out.println("Deserialized Token is: "+result);
			}
			finally{
				input.close();
			}
		}
		catch(ClassNotFoundException ex){
			//fLogger.log(Level.SEVERE, "Cannot perform input. Class not found.", ex);
			ex.printStackTrace();
		}
		catch(IOException ex){
			//		      fLogger.log(Level.SEVERE, "Cannot perform input.", ex);
			ex.printStackTrace();
		}
		return result;
	}


}
