package com.nearfuturelaboratory.humans.rest;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.ParameterStyle;
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest;
import org.bson.types.ObjectId;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.nearfuturelaboratory.humans.dao.HumansUserDAO;
import com.nearfuturelaboratory.humans.entities.HumansUser;
import com.nearfuturelaboratory.humans.util.MyObjectIdSerializer;

@Path("/friends")
@Deprecated
public class GetFriends {

	final static Logger logger = Logger.getLogger(com.nearfuturelaboratory.humans.rest.GetFriends.class);


	@Context ServletContext context;
	//Gson gson;

	public GetFriends() {
		logger.debug("Constructor " + context);  // null here   
		//gson = new GsonBuilder().registerTypeAdapter(ObjectId.class, new MyObjectIdSerializer()).create();
	}


	@SuppressWarnings("unused")
	@GET @Path("/get")
	@Produces({"application/json"})
	public String getFriends(
			@Context HttpServletRequest request,
			@Context HttpServletResponse response
			) 
	{
		logger.debug("HELLO??");
	    try {
		OAuthAccessResourceRequest oauthRequest = new OAuthAccessResourceRequest(request,
                ParameterStyle.BODY);        
    
		String access_token = oauthRequest.getAccessToken();
	    HumansUserDAO dao = new HumansUserDAO();
	    
	    } catch (OAuthProblemException | OAuthSystemException e) {
	    	logger.error("",e);
	    }
		
		HttpSession session = request.getSession();
		HumansUser user = (HumansUser)session.getAttribute("logged-in-user");
		//HumansUserDAO dao = new HumansUserDAO();
		//HumansUser h = dao.findByHumanID(aHumanId);

		if(user == null) {
			JsonObject json = new JsonObject();

			json.addProperty("result", "error");
			json.addProperty("message", "invalid user");
			String error = json.toString();
			return error;
		}

		JsonArray result = user.getFriendsAsJson();

		return result.toString();
	}

}
