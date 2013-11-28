package com.nearfuturelaboratory.humans.rest;


import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nearfuturelaboratory.humans.dao.HumansUserDAO;
import com.nearfuturelaboratory.humans.entities.HumansUser;
import com.nearfuturelaboratory.humans.util.MyObjectIdSerializer;


@Path("login")
public class Login {
	final static Logger logger = Logger.getLogger(com.nearfuturelaboratory.humans.rest.Login.class);

	@Context ServletContext context;

    public Login() {
         logger.debug("Constructor " + context);  // null here     
    }
	
//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//	public String login(
//			@QueryParam("username") String aUsername, 
//			@QueryParam("password") String aPassword,
//			@Context HttpServletRequest request)
//	{
//		HumansUserDAO dao = new HumansUserDAO();
//		HumansUser user = dao.findOneByUsername(aUsername);
//		if(user != null && user.verifyPassword(aPassword))
//		{
//			HttpSession session = request.getSession();
//			session.setAttribute("logged-in-user", user);
//			logger.debug("Logged in user "+user);
//		}
//		
//		
//		return user.toString();
//
//	}
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String loginJson(			
    		@QueryParam("username") String aUsername, 
			@QueryParam("password") String aPassword,
			@DefaultValue("true") @QueryParam("min-m") boolean hasMin,
			@DefaultValue("false") @QueryParam("defcon") boolean foo,
			@Context HttpServletRequest request)
    {
    	String result;
    	String a = request.getParameter("min-m");
    	String b = request.getParameter("defcon");
		HumansUserDAO dao = new HumansUserDAO();
		HumansUser user = dao.findOneByUsername(aUsername);
		if(foo) {
			HttpSession session = request.getSession();
			session.setAttribute("logged-in-user", user);
			logger.debug("Logged in user "+user);
			Gson gson = new GsonBuilder().registerTypeAdapter(ObjectId.class, new MyObjectIdSerializer()).create();
			JsonElement user_elem = new JsonParser().parse(gson.toJson(user));
			JsonObject user_obj = user_elem.getAsJsonObject();
			user_obj.remove("password");
			 
			result = user_obj.toString();
			return result;

		}
		
		if( user != null && user.verifyPassword(aPassword))
		{
			HttpSession session = request.getSession();
			session.setAttribute("logged-in-user", user);
			logger.debug("Logged in user "+user);
			Gson gson = new GsonBuilder().registerTypeAdapter(ObjectId.class, new MyObjectIdSerializer()).create();
			JsonElement user_elem = new JsonParser().parse(gson.toJson(user));
			JsonObject user_obj = user_elem.getAsJsonObject();
			user_obj.remove("password");
			 
			result = user_obj.toString();
		} else {
			JsonObject json = new JsonObject();
			
			json.addProperty("result", "error");
			json.addProperty("message", "invalid credentials");
			result = json.toString();
		}
		return result;

    	
    	
    	
    }

    
	
}
