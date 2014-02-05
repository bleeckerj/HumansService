package com.nearfuturelaboratory.humans.servlets;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.json.simple.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import com.nearfuturelaboratory.humans.entities.HumansUser;
import com.nearfuturelaboratory.humans.service.InstagramService;
import com.nearfuturelaboratory.humans.serviceapi.InstagramApi;
import com.nearfuturelaboratory.util.Constants;





@SuppressWarnings("serial")
@WebServlet(name = "InstagramLoginServlet", urlPatterns = {"/login-instagram", "/InstagramLogin"}
		)
public class InstagramLoginServlet extends HttpServlet {

	private OAuthService service;
	private String apiKey = Constants.getString("INSTAGRAM_API_KEY");
	private String apiSecret = Constants.getString("INSTAGRAM_API_SECRET");
	private String callbackURL = Constants.getString("INSTAGRAM_CALLBACK_URL");

	private static final Token EMPTY_TOKEN = null;


	private Token accessToken;
	protected JSONObject user;


	final static Logger logger = LogManager.getLogger(com.nearfuturelaboratory.humans.servlets.InstagramLoginServlet.class);

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		service = new ServiceBuilder()
		.provider(InstagramApi.class)
		.apiKey(apiKey)
		.apiSecret(apiSecret)
		.callback(callbackURL)
		.scope("basic,likes")
		.build();

		//logger.debug("Request Parameters are "+req.getParameterMap());
		
		if(req.getParameter("code") != null) {
			logger.debug("now a response code="+req.getParameter("code"));
			Verifier verifier = new Verifier(req.getParameter("code"));
			accessToken = service.getAccessToken(EMPTY_TOKEN, verifier);
			logger.debug("and access token "+accessToken);
			
			InstagramService instagramService = new InstagramService(accessToken);
			instagramService.serviceRequestUserBasic();
			String keyForUser = instagramService.getThisUser().getId()+"-"+instagramService.getThisUser().getUsername();
			logger.debug("instagram user="+instagramService.getThisUser());
			logger.debug("keyForUser="+keyForUser);
			
			logger.info("got access token for "+instagramService.getThisUser());
			InstagramService.serializeToken(accessToken, instagramService.getThisUser());
			HttpSession session = req.getSession(true);
			HumansUser user = (HumansUser)session.getAttribute("logged-in-user");

			if(user == null) {
				resp.sendRedirect(req.getContextPath()+"/login.jsp");
				return;
			}
			//InstagramUser instagramUser = instagramService.getThisUser();
			
			logger.debug("screen_name is "+instagramService.getThisUser().getUsername()+" "+instagramService.getThisUser().getId());
			user.addService("instagram", (String)instagramService.getThisUser().getUsername(), (String)instagramService.getThisUser().getId());
			
			session.setAttribute("logged-in-user", user);
			// save the user?
			String jsonUserString = user.toString();
			Path startingDir = Paths.get(Constants.getString("USER_DATA_ROOT"));
			Writer output = null;
			File file = new File(startingDir.toFile()+"/"+user.getUsername()+"-user.json");
			output = new BufferedWriter(new FileWriter(file));
			output.write(jsonUserString);
			output.close();
			
			
			InstagramService.serializeToken(accessToken, instagramService.getThisUser());
			resp.sendRedirect(req.getContextPath()+"/services.jsp");
			instagramService.getFriends();
		} else {
			
			String authorizationUrl = service.getAuthorizationUrl(EMPTY_TOKEN);
			logger.info("Authorization URL="+authorizationUrl);
			resp.sendRedirect(authorizationUrl);
		}


	}

}
