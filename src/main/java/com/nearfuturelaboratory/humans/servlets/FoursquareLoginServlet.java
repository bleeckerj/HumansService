package com.nearfuturelaboratory.humans.servlets;
import java.io.IOException;

import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.Foursquare2Api;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import com.nearfuturelaboratory.humans.entities.HumansUser;
import com.nearfuturelaboratory.humans.service.FoursquareService;
import com.nearfuturelaboratory.util.Constants;

@WebServlet(name = "FoursquareLoginServlet", urlPatterns = {"/login-foursquare", "/FoursquareLogin", "/scrumpy-foursquare"},
initParams = {@WebInitParam(name="tokens-file-root", value="/Volumes/Slippy/Users/julian/Documents/workspace/HumansService")}
		)
public class FoursquareLoginServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7242684152402981944L;
	private static String apiKey = Constants.getString("FOURSQUARE_API_KEY");//"MKGJ3OZYTDNZAI5ZMROF3PAMAUND0ZO2HYRTZYXHIIR5TW1Q";
	private static String apiSecret = Constants.getString("FOURSQUARE_API_SECRET");//"2G0DUIFCFAWBH1WPIYBUDQMESKRLFLGY5PHXY0BJNBE1MMN4";
	private String callbackURL = Constants.getString("FOURSQUARE_CALLBACK_URL");//"http://localhost:8080/HumansService/FoursquareLogin";
	private static final Token EMPTY_TOKEN = null;

	private Token accessToken;
	protected JSONObject user;
	protected FoursquareService foursquare;

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
			foursquareService.serviceRequestFriends();
			//logger.debug("User is "+Flickr.getThisUser());
			logger.debug("FoursquareUser is "+foursquareService.getThisUser());
			user.addService("foursquare", (String)foursquareService.getDerivedUsername(), (String)foursquareService.getThisUser().getId());


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
