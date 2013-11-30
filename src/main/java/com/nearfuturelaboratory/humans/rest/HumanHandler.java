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


@Path("/human")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class HumanHandler {

	final static Logger logger = Logger.getLogger(com.nearfuturelaboratory.humans.rest.HumanHandler.class);
	static JsonObject invalid_user_error_response;
	static JsonObject success_response;
	static JsonObject fail_response;
	static JsonObject no_such_human_for_user;

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

	}

	@Context ServletContext context;
	Gson gson;

	public HumanHandler() {
		logger.debug("Constructor " + context);  // null here   
		gson = new GsonBuilder().registerTypeAdapter(ObjectId.class, new MyObjectIdSerializer()).create();
	}


	@GET @Path("/get")
	@Produces({"application/json"})
	public String getHumanByID(@QueryParam("id") String aHumanId, 
			@Context HttpServletRequest request,
			@Context HttpServletResponse response)
	{
		HttpSession session = request.getSession();
		HumansUser user = (HumansUser)session.getAttribute("logged-in-user");
		HumansUserDAO dao = new HumansUserDAO();
		
		HumansUser h = dao.findByHumanID(aHumanId);
		
		if(h ==  null) {
			return no_such_human_for_user.toString();
		}
		
		if(isValidUser(request, user) == false) {
			return invalid_user_error_response.toString();
		}

		Human human = h.getHumanByID(aHumanId);
		JsonElement human_elem = new JsonParser().parse(gson.toJson(human));

		return human_elem.toString();
	}


	@GET @Path("/get/{humanid}")
	@Produces({"application/json"})
	public String getHuman(
			@PathParam("humanid") String aHumanId, 
			@Context HttpServletRequest request,
			@Context HttpServletResponse response) 
	{
		HttpSession session = request.getSession();
		HumansUser user = (HumansUser)session.getAttribute("logged-in-user");
		HumansUserDAO dao = new HumansUserDAO();
		HumansUser h = dao.findByHumanID(aHumanId);
		if(h ==  null) {
			return no_such_human_for_user.toString();
		}

		
		if(isValidUser(request, user) == false) {
			return invalid_user_error_response.toString();
		}

		Human human = h.getHumanByID(aHumanId);
		JsonElement human_elem = new JsonParser().parse(gson.toJson(human));

		return human_elem.toString();
	}


	@GET @Path("/status")
	@Produces({"application/json"})
	public String getStatusByID(
			@QueryParam("id") String aHumanId, 
			@Context HttpServletRequest request,
			@Context HttpServletResponse response)
	{
		return this.getStatus(aHumanId, request, response);
	}

	@GET @Path("/status/{humanid}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getStatus(
			@PathParam("humanid") String aHumanId, 
			@Context HttpServletRequest request,
			@Context HttpServletResponse response) 
	{
		HttpSession session = request.getSession();
		HumansUser user = (HumansUser)session.getAttribute("logged-in-user");
		HumansUserDAO dao = new HumansUserDAO();
		HumansUser h = dao.findByHumanID(aHumanId);
		
		if(h == null) {
			fail_response.addProperty("message", "none such user found for humanid="+aHumanId);
			return fail_response.toString();
		}
		
		if(isValidUser(request, user) == false) {
			return invalid_user_error_response.toString();
		}

		Human human = h.getHumanByID(aHumanId);
		
		if(human == null) {
			fail_response.addProperty("message", "none such human found for humanid="+aHumanId);
			return fail_response.toString();
		}
		
		JsonArray result = user.getJsonStatusForHuman(human);
//		JsonArray result = new JsonArray();
//		for(ServiceStatus s : status) {
//			result.add(s.getStatusJSON());
//		}
		//logger.debug(status);
		return result.toString();
	}

	@GET
	@Path("{humanid}/update/serviceuser/{serviceuserid}")
	@Produces(MediaType.APPLICATION_JSON)
	public String updateServiceUserFromHuman(
			String aServiceUserJson,
			@PathParam("humanid") String aHumanId,
			@PathParam("serviceuserid") String aServiceUserId,
			@Context HttpServletRequest request,
			@Context HttpServletResponse response)
	{
		HumansUserDAO dao = new HumansUserDAO();
		HumansUser user = dao.findByHumanID(aHumanId);

		if(isValidUser(request, user) == false) {
			return invalid_user_error_response.toString();
		}

		ServiceUser aServiceUser = gson.fromJson(aServiceUserJson, ServiceUser.class);
		boolean result = user.updateServiceUserById(aServiceUser, aServiceUserId);

		if(result) {
			return success_response.toString();
		} else {
			return fail_response.toString();
		}
	}

	@GET
	@Path("{humanid}/rm/serviceuser/{serviceuserid}")
	@Produces(MediaType.APPLICATION_JSON)
	public String removeServiceUserFromHuman(
			@PathParam("humanid") String aHumanId,
			@PathParam("serviceuserid") String aServiceUserId,
			@Context HttpServletRequest request,
			@Context HttpServletResponse response)
	{

		//		HttpSession session = request.getSession();
		//		HumansUser user = (HumansUser)session.getAttribute("logged-in-user");
		HumansUserDAO dao = new HumansUserDAO();
		HumansUser user = dao.findByHumanID(aHumanId);

		if(isValidUser(request, user) == false) {
			return invalid_user_error_response.toString();
		}

		boolean success = user.removeServiceUserById(aServiceUserId);

		//		// search for serviceuserid
		//		Human human = user.getHumanByID(aHumanId);
		//		boolean success = human.removeServiceUserById(aServiceUserId);

		if(success) {
			return success_response.toString();
		} else {
			return fail_response.toString();
		}

	}


	@POST
	@Path("{humanid}/add/serviceuser/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String addServiceUserToHuman(
			String aServiceUserJson,
			@PathParam("humanid") String aHumanId, 
			@Context HttpServletRequest request,
			@Context HttpServletResponse response)
	{
		HttpSession session = request.getSession();
		HumansUser user = (HumansUser)session.getAttribute("logged-in-user");
		HumansUserDAO dao = new HumansUserDAO();
		HumansUser h = dao.findByHumanID(aHumanId);

		if(isValidUser(request, user) == false) {
			return invalid_user_error_response.toString();
		}
		logger.debug(aHumanId);
		logger.debug("aServiceUser "+aServiceUserJson);

		Human human = h.getHumanByID(aHumanId);

		JsonParser parser = new JsonParser();
		JsonElement e = parser.parse(aServiceUserJson);
		if(e.isJsonArray()) {
			user.removeHuman(human);
			JsonArray service_user_array = e.getAsJsonArray();
			for(JsonElement service_user : service_user_array) {
				ServiceUser aServiceUser = gson.fromJson(service_user, ServiceUser.class);
				human.addServiceUser(aServiceUser);
			}
			user.addHuman(human);
		} else {
			ServiceUser aServiceUser = gson.fromJson(aServiceUserJson, ServiceUser.class);
			// get the human with this aHumanId
			user.removeHuman(human);
			human.addServiceUser(aServiceUser);
			user.addHuman(human);
		}

		user.save();
		return gson.toJson(user);
	}





	protected boolean isValidUser(HttpServletRequest request, HumansUser h) {
		HumansUser user = getSessionUser(request);
		boolean result = false;
		if(user == null || h == null || false == h.getId().equals(user.getId())) {
			result = false;
		} else {
			result = true;
		}
		return result;
	}

	protected HumansUser getSessionUser(HttpServletRequest request) {
		return (HumansUser)request.getSession().getAttribute("logged-in-user");
	}

}
/*@Provider
class ServiceUserMessageBodyReader implements MessageBodyReader<ServiceUser> {

	@Override
	public boolean isReadable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return type == ServiceUser.class;
	}

	public ServiceUser readFrom(Class<ServiceUser> type,
			Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders,
			InputStream entityStream)
					throws IOException, WebApplicationException {

		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(ServiceUser.class);
			ServiceUser serviceUser = (ServiceUser) jaxbContext.createUnmarshaller()
					.unmarshal(entityStream);
			return serviceUser;
		} catch (JAXBException jaxbException) {
			throw new ProcessingException("Error deserializing a ServiceUser",
					jaxbException);
		}
	}
}
 */