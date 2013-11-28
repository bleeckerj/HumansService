package com.nearfuturelaboratory.humans.rest;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nearfuturelaboratory.humans.dao.HumansUserDAO;
import com.nearfuturelaboratory.humans.entities.Human;
import com.nearfuturelaboratory.humans.entities.HumansUser;
import com.nearfuturelaboratory.humans.entities.ServiceUser;
import com.nearfuturelaboratory.humans.service.status.ServiceStatus;
import com.nearfuturelaboratory.humans.util.MyObjectIdSerializer;


@Path("/user")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserHandler {
	final static Logger logger = Logger.getLogger(com.nearfuturelaboratory.humans.rest.UserHandler.class);
	static JsonObject invalid_user_error_response;
	static JsonObject success_response;
	static JsonObject fail_response;
	static JsonObject no_such_human_for_user;
	static JsonObject no_such_serviceuser_for_user;

	static {
		invalid_user_error_response = new JsonObject();
		invalid_user_error_response.addProperty("result", "error");
		invalid_user_error_response.addProperty("message", "invalid user");

		success_response = new JsonObject();
		success_response.addProperty("result", "success");

		fail_response = new JsonObject();
		fail_response.addProperty("result", "fail");

		no_such_human_for_user = new JsonObject();
		no_such_human_for_user.addProperty("result", "fail");
		no_such_human_for_user.addProperty("message", "no such human for user");

		no_such_serviceuser_for_user = new JsonObject();
		no_such_serviceuser_for_user.addProperty("result", "fail");
		no_such_serviceuser_for_user.addProperty("message", "no such service user for user");

	}

	@Context ServletContext context;
	Gson gson;

	public UserHandler() {
		logger.debug("Constructor " + context);  // null here   
		gson = new GsonBuilder().registerTypeAdapter(ObjectId.class, new MyObjectIdSerializer()).create();
	}

	@GET @Path("/get")
	@Produces({"application/json"})
	public String getUser(
			@Context HttpServletRequest request,
			@Context HttpServletResponse response)
	{
		HttpSession session = request.getSession();
		HumansUser user = (HumansUser)session.getAttribute("logged-in-user");

		if(isValidUser(request, user) == false) {
			return invalid_user_error_response.toString();
		}

		JsonElement human_elem = new JsonParser().parse(gson.toJson(user));
		return human_elem.toString();


	}

	@GET @Path("/rm/{humanid}/human")
	@Produces({"application/json"})
	public String removeHuman(
			@PathParam("humanid") String aHumanId,
			@Context HttpServletRequest request,
			@Context HttpServletResponse response)
	{
		HumansUserDAO dao = new HumansUserDAO();
		HumansUser user = dao.findByHumanID(aHumanId);
		//HttpSession session = request.getSession();
		if(user == null) {
			return no_such_human_for_user.toString();
		}
		if(isValidUser(request, user) == false) {
			return invalid_user_error_response.toString();
		}

		boolean result = user.removeHumanById(aHumanId);
		user.save();
		setSessionUser(request, user);

		if(result) {
			return success_response.toString();
		} else {
			return fail_response.toString();
		}
	}

	@GET @Path("/rm/{serviceuserid}/serviceuser")
	@Produces({"application/json"})
	public String removeServiceUser(
			@PathParam("serviceuserid") String aServiceUserId,
			@Context HttpServletRequest request,
			@Context HttpServletResponse response)
	{
		//HumansUserDAO dao = new HumansUserDAO();
		HumansUser user = this.getSessionUser(request);
		//HttpSession session = request.getSession();
		if(isValidUser(request, user) == false) {
			return invalid_user_error_response.toString();
		}

		try {
			ObjectId o = new ObjectId(aServiceUserId);
		} catch(IllegalArgumentException iae) {
			logger.warn("", iae);
			return fail_response.toString();
		}

		if(user == null) {
			return invalid_user_error_response.toString();
		}
		//		if(aServiceUserId == null) {
		//			
		//		}

		boolean result = user.removeServiceUserById(aServiceUserId);
		user.save();
		setSessionUser(request, user);

		if(result) {
			return success_response.toString();
		} else {
			return fail_response.toString();
		}
	}

	@GET @Path("/add/human/")
	@Produces({"application/json"})
	public String addNewHuman(
			String aHumanJson,
			@Context HttpServletRequest request,
			@Context HttpServletResponse response)
	{
		HumansUser user = this.getSessionUser(request);
		Human human = gson.fromJson(aHumanJson, Human.class);
		boolean result = user.addHuman(human);
		if(isValidUser(request, user) == false) {
			return invalid_user_error_response.toString();
		}

		if(result) {
			user.save();
			return success_response.toString();
		} else {
			return fail_response.toString();
		}

	}

	@GET @Path("/get/humans/")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAllHumans(
			@Context HttpServletRequest request,
			@Context HttpServletResponse response
			)
	{
		HumansUser user = this.getSessionUser(request);

		if(isValidUser(request, user) == false) {
			return invalid_user_error_response.toString();
		}

		Gson gson = new GsonBuilder().registerTypeAdapter(ObjectId.class, new MyObjectIdSerializer()).create();

		List<Human> humans = user.getAllHumans();
		JsonArray array_of_humans = new JsonArray();

		for(Human human : humans) {
			array_of_humans.add(gson.toJsonTree(human, Human.class));
		}


		return array_of_humans.toString();
	}


	protected boolean isValidUser(HttpServletRequest request, HumansUser h) {
		HumansUser user = getSessionUser(request);
		boolean result = false;
		logger.debug(user.getId());
		logger.debug(h.getId());
		if(user == null || h == null || h.getId().equals(user.getId()) == false) {
			result = false;
		} else {
			result = true;
		}
		return result;
	}

	protected HumansUser getSessionUser(HttpServletRequest request) {
		return (HumansUser)request.getSession().getAttribute("logged-in-user");
	}

	protected void setSessionUser(HttpServletRequest request, HumansUser user) {
		request.getSession().setAttribute("logged-in-user", user);
	}
}
