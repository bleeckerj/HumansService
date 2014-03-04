package com.nearfuturelaboratory.humans.rest;

import static com.google.common.collect.Lists.partition;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.bson.types.ObjectId;

import com.nearfuturelaboratory.humans.dao.HumansUserDAO;
import com.nearfuturelaboratory.humans.entities.Human;
import com.nearfuturelaboratory.humans.entities.HumansUser;
import com.nearfuturelaboratory.humans.entities.ServiceUser;
import com.nearfuturelaboratory.humans.util.MyObjectIdSerializer;
import com.nearfuturelaboratory.util.Constants;


@Path("/human")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class HumanHandler {

	final static Logger logger = LogManager.getLogger(com.nearfuturelaboratory.humans.rest.HumanHandler.class);
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
		//logger.debug("Constructor " + context);  // null here   
		gson = new GsonBuilder().setDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz").registerTypeAdapter(ObjectId.class, new MyObjectIdSerializer()).create();
	}


	/**
	 * Get a human by id for the currently logged in User
	 * @param aHumanId
	 * @param request
	 * @param response
	 * @return json representation of the logged in Human
	 */
	@GET @Path("/get")
	@Produces({"application/json"})
	public String getHumanByID(@QueryParam("id") String aHumanId, 
			@Context HttpServletRequest request,
			@Context HttpServletResponse response)
	{
		RestCommon common = new RestCommon();
		//		HttpSession session = request.getSession();
		//		HumansUser user = (HumansUser)session.getAttribute("logged-in-user");
		//		HumansUserDAO dao = new HumansUserDAO();
		//
		//		HumansUser h = dao.findByHumanID(aHumanId);

		HumansUser user;
		try {
			user = common.getUserForAccessToken(/*context, */request.getParameter("access_token"));
		} catch (InvalidAccessTokenException e) {
			logger.warn("invalid or missing access token", e);
			fail_response.addProperty("message", "invalid access token");
			return fail_response.toString();
			//			e.printStackTrace();
		} 

		Human human = user.getHumanByID(aHumanId);

		if(human ==  null) {
			return no_such_human_for_user.toString();
		}

		JsonElement human_elem = new JsonParser().parse(gson.toJson(human));

		return human_elem.toString();
	}


	/**
	 * Get a specific human by id from the logged in User
	 * @param aHumanId
	 * @param request
	 * @param response
	 * @return
	 */
	@GET @Path("/get/{humanid}")
	@Produces({"application/json"})
	public String getHuman(
			@PathParam("humanid") String aHumanId, 
			@Context HttpServletRequest request,
			@Context HttpServletResponse response) 
	{
		return this.getHumanByID(aHumanId, request, response);
	}

	/**
	 * Return status. This is the mother-mugger. You should page through this. If you pass no "page" parameter,
	 * you'll simply get the first page of 100 items.
	 * 
	 * @param aHumanId query parameter
	 * @param aPage query parameter
	 * @param request comes from the context
	 * @param response comes from the context
	 * @return
	 */
	@GET @Path("/status")
	//@Produces({"application/json"})
	public Response getStatusByID(
			@QueryParam("humanid") String aHumanId, 
			@QueryParam("page") String aPage,
			@Context HttpServletRequest request,
			@Context HttpServletResponse response)
	{
		return this.getStatus(aHumanId, aPage, request, response);
	}


    @GET @Path("/status/count")
    public Response getStatusCount(@Context HttpServletRequest request,
                                   @Context HttpServletResponse response)
    {
        RestCommon common = new RestCommon();
        HumansUser user;
        try {
            user = common.getUserForAccessToken(/*context, */request.getParameter("access_token"));
        } catch (InvalidAccessTokenException e1) {
            logger.warn("invalid or missing access token");
            fail_response.addProperty("message", "invalid access token");
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(fail_response.getAsJsonObject().toString()).build();
            //return fail_response.toString();
            //e1.printStackTrace();
        }

        List<Human> allUserHumans = user.getAllHumans();
        for(Human human : allUserHumans) {
            int result = user.getStatusCountFromCache(human);
            success_response.addProperty(human.getId(), String.valueOf(result));

        }
        return Response.ok().entity(success_response.toString()).type(MediaType.APPLICATION_JSON).build();
    }

    @GET @Path("/status/count/{humanid}")
    public Response getStatusCount(
            @PathParam("humanid") String aHumanId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response)
    {
        RestCommon common = new RestCommon();
        HumansUser user;
        try {
            user = common.getUserForAccessToken(/*context, */request.getParameter("access_token"));
        } catch (InvalidAccessTokenException e1) {
            logger.warn("invalid or missing access token");
            fail_response.addProperty("message", "invalid access token");
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(fail_response.getAsJsonObject().toString()).build();
            //return fail_response.toString();
            //e1.printStackTrace();
        }

        //logger.debug("humanid="+aHumanId+" and page="+aPage);
        Human human = null;
        try {
            human = user.getHumanByID(aHumanId);
        } catch(java.lang.IllegalArgumentException iae) {
            logger.warn(iae);
        }
        if(human ==  null) {
            fail_response.addProperty("message", "none such human found for humanid="+aHumanId);
            return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON).entity(fail_response.toString()).build();

            //return fail_response.toString();
        }

        int result = user.getStatusCountFromCache(human);
        success_response.addProperty("count", String.valueOf(result));
        String response_str = success_response.toString();
        return Response.ok().entity(response_str).type(MediaType.APPLICATION_JSON).build();

    }

    @GET @Path("/status/count/{humanid}/after/{timestamp}")
    public Response getStatusCountAfter(
            @PathParam("humanid") String aHumanId,
            @PathParam("timestamp") long aTimestamp,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response)
    {
        RestCommon common = new RestCommon();
        HumansUser user;
        try {
            user = common.getUserForAccessToken(/*context, */request.getParameter("access_token"));
        } catch (InvalidAccessTokenException e1) {
            logger.warn("invalid or missing access token");
            fail_response.addProperty("message", "invalid access token");
            return Response.ok().type(MediaType.APPLICATION_JSON).entity(fail_response.getAsJsonObject().toString()).build();
            //return fail_response.toString();
            //e1.printStackTrace();
        }

        //logger.debug("humanid="+aHumanId+" and page="+aPage);
        Human human = null;
        try {
            human = user.getHumanByID(aHumanId);
        } catch(java.lang.IllegalArgumentException iae) {
            logger.warn(iae);
        }
        if(human ==  null) {
            fail_response.addProperty("message", "none such human found for humanid="+aHumanId);
            return Response.ok().type(MediaType.APPLICATION_JSON).entity(fail_response.toString()).build();

            //return fail_response.toString();
        }
        int count;
        try {
        //long timestamp = Long.parseLong(aTimestamp);

        count = user.getStatusCountFromCacheAfterTimestamp(human, aTimestamp);
        } catch(Exception nfe) {
            logger.warn("Bad parameter passed as a timestamp "+aTimestamp, nfe);
            fail_response.addProperty("Bad parameter passed as a timestamp", aTimestamp);
            return Response.ok(fail_response.toString(), MediaType.APPLICATION_JSON).build();
        }
        success_response.addProperty("count", count);
        return Response.ok().entity(success_response.toString()).type(MediaType.APPLICATION_JSON).build();
    }

	@GET @Path("/status/{humanid}")
	//@Produces(MediaType.APPLICATION_JSON)
	public Response getStatus(
			@PathParam("humanid") String aHumanId, 
			@Context HttpServletRequest request,
			@Context HttpServletResponse response) 	
	{
		return this.getStatus(aHumanId, "0", request, response);
	}

    /**
     *
     * @param aHumanId
     * @param aPage 0-based (page=0 is the first page), which means the "pages" count is canonical not indexical
     * @param request
     * @param response
     * @return
     */
	@GET @Path("/status/{humanid}/{page}")
	//@Produces(MediaType.APPLICATION_JSON)
	public Response getStatus(
			@PathParam("humanid") String aHumanId, 
			@PathParam("page") String aPage,
			@Context HttpServletRequest request,
			@Context HttpServletResponse response) 
	{
		RestCommon common = new RestCommon();
		HumansUser user;
		try {
			user = common.getUserForAccessToken(/*context, */request.getParameter("access_token"));
		} catch (InvalidAccessTokenException e1) {
			logger.warn("invalid or missing access token");
			fail_response.addProperty("message", "invalid access token");
			return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(fail_response.getAsJsonObject().toString()).build();
		}

		//logger.debug("humanid="+aHumanId+" and page="+aPage);
		Human human = null;
		try {
			human = user.getHumanByID(aHumanId);
		} catch(java.lang.IllegalArgumentException iae) {
			logger.warn(iae);
		}
		if(human ==  null) {
			fail_response.addProperty("message", "none such human found for humanid="+aHumanId);
			return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON).entity(fail_response.toString()).build();

			//return fail_response.toString();
		}

        int page = 1;
        if(aPage != null) {
            try {
                page = Integer.parseInt(aPage);
            } catch(NumberFormatException nfe) {
                logger.warn("", nfe);
            }
        }

        //page-=1;

        int pages = user.getJsonCachedStatusPageCountForHuman(human, Constants.getInt("STATUS_CHUNK_SIZE", 25));
        if(page > pages) {
            page = pages;
        }
        int total_status = user.getJsonCachedStatusCountForHuman(human);
        //logger.debug("pages="+" total_status="+total_status);

        JsonArray array = new JsonArray();
        JsonArray tmp = user.getJsonCachedStatusForHuman(human, page);
        JsonElement newest = tmp.get(0);
        JsonElement oldest = tmp.get(tmp.size()-1);

        if(tmp == null || tmp.size() < 1) {
            logger.warn("I got empty json status here for "+human+" "+page);
            fail_response.addProperty("message", "no status found right now for page="+page+" of "+pages+" for humanid="+aHumanId);
            fail_response.addProperty("page", page);
            fail_response.addProperty("pages", pages);
            fail_response.addProperty("humanid", aHumanId);

            return Response.ok().type(MediaType.APPLICATION_JSON).entity(fail_response.toString()).build();
        }

        JsonObject data = new JsonObject();

        JsonObject head = new JsonObject();
        JsonObject head_m = new JsonObject();

        head_m.addProperty("pages", pages);

        head_m.addProperty("page", page);
        head_m.addProperty("total_status", total_status);//user.getJsonStatusCountForHuman(human));// status_list.size());
        head_m.addProperty("count", tmp.size());
        head_m.addProperty("human_name", human.getName());
        head_m.addProperty("human_id", human.getId());

        //head.add("head", head_m);

        data.add("head", head_m);
//        array.add(head);

        //JsonObject status = new JsonObject();
        //status.add("status", tmp);
        //array.add(status);
        //array.addAll(tmp);
        data.add("status", tmp);

        //status.add(0, json_result.getAsString());
        String response_str = data.toString();
        return Response.ok().type(MediaType.APPLICATION_JSON).entity(response_str).build();

	}

	//TODO
	//	@GET @Path("/status/{humanid}/{aftertimestamp}/{page}")
	//	//@Produces(MediaType.APPLICATION_JSON)
	//	public Response getStatus(
	//			@PathParam("humanid") String aHumanId,
	//			@PathParam("aftertimestamp") Long afterTimeStamp,
	//			@PathParam("page") String aPage,
	//			@Context HttpServletRequest request,
	//			@Context HttpServletResponse response) 
	//	{
	//		return Response.serverError().entity("not implemented yet").build();
	//	}


	//TODO status by service? Is this useful?

/*
	@GET @Path("/status/{humanid}/{page}/{service}")
	//@Produces(MediaType.APPLICATION_JSON)
	public Response getStatus(
			@PathParam("humanid") String aHumanId,
			@PathParam("page") String aPage,
			@PathParam("service") String service,
			@Context HttpServletRequest request,
			@Context HttpServletResponse response)
	{
		//String result = getStatus(aHumanId, aPage, request, response);
		// stoopid that you would have to convert it back to JSON
		HttpSession session = request.getSession();
		RestCommon common = new RestCommon();
		HumansUser user;
		try {
			user = common.getUserForAccessToken(*/
/*context, *//*
request.getParameter("access_token"));
		} catch (InvalidAccessTokenException e1) {
			logger.warn("invalid or missing access token", e1);
			fail_response.addProperty("message", "invalid access token");
			return Response.status(Response.Status.UNAUTHORIZED).entity("invalid access token").build();
			//return fail_response.toString();
			//e1.printStackTrace();
		}

		logger.debug("humanid="+aHumanId);
		Human human = user.getHumanByID(aHumanId);

		if(human ==  null) {
			fail_response.addProperty("message", "none such human found for humanid="+aHumanId);
			return Response.status(Response.Status.NOT_FOUND).entity("none such human found for humanid="+aHumanId).build();

			//return fail_response.toString();
		}

		//StatusPagingHelper paging_helper = getStatusPagingHelperFromSession(session, user, human);
//		StatusPagingHelper paging_helper = this.getStatusPagingHelper(user, human);
//		if(paging_helper == null) {
//			return Response.status(Response.Status.NOT_ACCEPTABLE).entity("no such paging helper found for "+human).build();
//		}


		int page = 1;
		if(aPage != null) {
			try {
				page = Integer.parseInt(aPage);
			} catch(NumberFormatException nfe) {
				logger.warn("", nfe);
			}
		}

		JsonObject status_response = paging_helper.statusJsonByPage(page-1);

		return Response.ok().type(MediaType.APPLICATION_JSON).entity(status_response.toString()).build();
	}

*/



	@POST
	@Path("{humanid}/update/serviceuser/{serviceuserid}")
	//@Consumes(MediaType.APPLICATION_JSON)
	//@Produces(MediaType.APPLICATION_JSON)
	public Response updateServiceUserFromHuman(
			String aServiceUserJson,
			@PathParam("humanid") String aHumanId,
			@PathParam("serviceuserid") String aServiceUserId,
			@Context HttpServletRequest request,
			@Context HttpServletResponse response)
	{
		HumansUserDAO dao = new HumansUserDAO();
		HumansUser user = dao.findByHumanID(aHumanId);

		if(isValidUser(request, user) == false) {
			return Response.status(Response.Status.UNAUTHORIZED).entity("invalid access token").build();
			//return invalid_user_error_response.toString();
		}

		ServiceUser aServiceUser = gson.fromJson(aServiceUserJson, ServiceUser.class);
		boolean result = user.updateServiceUserById(aServiceUser, aServiceUserId);

		if(result) {
			return Response.ok(success_response).build();
			//return success_response.toString();
		} else {
			return Response.status(Response.Status.NOT_ACCEPTABLE).entity(fail_response).build();//.toString();
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

		//		HttpSession session = request.getSesxfwebsion();
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
	public Response addServiceUserToHuman(
			String aServiceUserJson,
			@PathParam("humanid") String aHumanId, 
			@Context HttpServletRequest request,
			@Context HttpServletResponse response)
	{
        HumansUser user;
        RestCommon common = new RestCommon();
        Gson exc_gson = new GsonBuilder()
                .setExclusionStrategies(new ExclusionStrategy() {

                public boolean shouldSkipField(FieldAttributes f) {
                        if(f.getName().equals("id") || f.getName().equals("lastUpdated") || f.getName().equals("version")) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                    public boolean shouldSkipClass(Class<?> arg0) {
                        return false;
                    }
                }).create();

        try {
            user = common.getUserForAccessToken(/*context, */request.getParameter("access_token"));
        } catch (InvalidAccessTokenException e1) {
            logger.warn("invalid or missing access token");
            fail_response.addProperty("message", "invalid access token");
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(fail_response.getAsJsonObject().toString()).build();
        }
		logger.debug(aHumanId);
		logger.debug("aServiceUser "+aServiceUserJson);

		Human human = user.getHumanByID(aHumanId);

		JsonParser parser = new JsonParser();
		JsonElement e = parser.parse(aServiceUserJson);
        ServiceUser aServiceUser;
		if(e.isJsonArray()) {
			user.removeHuman(human);
			JsonArray service_user_array = e.getAsJsonArray();
			for(JsonElement service_user : service_user_array) {
				aServiceUser = exc_gson.fromJson(service_user, ServiceUser.class);
				human.addServiceUser(aServiceUser);
			}
			user.addHuman(human);
		} else {
			aServiceUser = exc_gson.fromJson(aServiceUserJson, ServiceUser.class);
			// get the human with this aHumanId
			user.removeHuman(human);
			human.addServiceUser(aServiceUser);
			user.addHuman(human);
		}

		user.save();
        success_response.addProperty("message", "Added new service user");
        success_response.addProperty("user", gson.toJson(user, HumansUser.class));
		return Response.ok(success_response.toString()).build();
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

/**
 * This helps us page through status results in chunks of 50
 * @author julian
 *
 */
/*class StatusPagingHelper {


	ArrayList<JsonElement> status_list;
	ArrayList<String> status_list_strings;
	Human human;
	List<List<JsonElement>> status_chunks;
	List<List<String>> status_chunks_strings;
	int pages;
	long created_time;

//	public StatusPagingHelper(ArrayList<String> aStatus, Human aHuman) {
//		status_list_strings = aStatus;
//		human = aHuman;
//		status_chunks_strings = partition(status_list_strings,  Constants.getInt("STATUS_CHUNK_SIZE", 50));
//	}

    public StatusPagingHelper(ArrayList<String> aStatus, Human aHuman) {
        status_list_strings = aStatus;
        human = aHuman;
        status_chunks_strings = partition(status_list_strings, Constants.getInt("STATUS_CHUNK_SIZE", 50));
        created_time = new Date().getTime();
        pages = status_chunks.size();
    }

	public StatusPagingHelper(JsonArray aStatus, Human aHuman) {
		status_list = getStatusAsList(aStatus);
		human = aHuman;
		//pages = aTotalPages;
		status_chunks = partition(status_list, Constants.getInt("STATUS_CHUNK_SIZE", 50));
		created_time = new Date().getTime();
		pages = status_chunks.size();
	}

	public List<String> statusJsonByPageStr(int aPage) {
		JsonObject json_result = new JsonObject();
        ArrayList<String> status = new ArrayList<String>();
		//String status_str_result = new String();
		if(aPage < 0) aPage = 0;

		if(aPage < status_chunks_strings.size()) {
			List<String> chunk = status_chunks_strings.get(aPage);
			//			JsonElement first = chunk.get(0);
			//			JsonElement last = chunk.get(0);
			//			if(first.isJsonObject()) {
			//				JsonObject obj = first.getAsJsonObject();
			//				obj.get("");
			//			}
			JsonObject head = new JsonObject();
			head.addProperty("pages", pages);
			if(aPage+1 > pages) {
				aPage = pages-1;
			}
			head.addProperty("page", aPage+1);
			head.addProperty("total_status", status_list.size());
			head.addProperty("count", chunk.size());
			head.addProperty("human_name", human.getName());
			head.addProperty("human_id", human.getId());
			json_result.add("head", head);

            status.add(0, json_result.getAsString());

			for(int i=0; i<chunk.size(); i++) {
				status.add(chunk.get(i));
			}
			//String result = head.getAsString()+",\"status\":["
			//json_result.add("status", status);
		}
		return status;//json_result;


	}
	
	public JsonObject statusJsonByPage(int aPage) {
		JsonObject json_result = new JsonObject();
		if(aPage < 0) aPage = 0;

		if(aPage < status_chunks.size()) {
			List<JsonElement> chunk = status_chunks.get(aPage);
			//			JsonElement first = chunk.get(0);
			//			JsonElement last = chunk.get(0);
			//			if(first.isJsonObject()) {
			//				JsonObject obj = first.getAsJsonObject();
			//				obj.get("");
			//			}
			JsonObject head = new JsonObject();
			head.addProperty("pages", pages);
			if(aPage+1 > pages) {
				aPage = pages-1;
			}
			head.addProperty("page", aPage+1);
			head.addProperty("total_status", status_list.size());
			head.addProperty("count", chunk.size());
			head.addProperty("human_name", human.getName());
			head.addProperty("human_id", human.getId());
			json_result.add("head", head);

			JsonArray status = new JsonArray();

			for(int i=0; i<chunk.size(); i++) {
				status.add(chunk.get(i));
			}
			json_result.add("status", status);
		}
		return json_result;

	}

	public String statusByPage(int aPage) {
		return statusJsonByPage(aPage).toString();
	}


	public int getTotalPages() {
		return pages;
	}

	protected ArrayList<JsonElement> getStatusAsList(JsonArray status) {
		//chunks = partition(status, 100);
		ArrayList<JsonElement> status_list = new ArrayList<JsonElement>();     
		//JsonArray jArray = (JsonArray)jsonObject; 
		if (status != null) { 
			for (int i=0; i<status.size(); i++){ 
				status_list.add(status.get(i));
			} 
		} 
		return status_list;		
	}


}*/

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