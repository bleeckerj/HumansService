package com.nearfuturelaboratory.humans.rest;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.google.gson.JsonObject;
import com.nearfuturelaboratory.humans.entities.HumansUser;

@SuppressWarnings("serial")

@WebServlet(value = "/manage", asyncSupported = true)
public class ServiceStatusManagerEndpoint extends HttpServlet {
	final static Logger logger = LogManager.getLogger(com.nearfuturelaboratory.humans.rest.ServiceStatusManagerEndpoint.class);
	
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

	public ServiceStatusManagerEndpoint() {
	}

	
	
	//@GET @Path("/status/{humanid}")
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
    	Object foo = request.getDispatcherType();
		final AsyncContext asyncContext = request.startAsync(request, response);
		asyncContext.getResponse().getWriter().println("Start");
        //asyncContext.getResponse().getWriter().println("Start");
        asyncContext.start(new StockPriceRunner(asyncContext));
        
//		String access_token = request.getParameter("access_token");
//		if(access_token == null) {
//			fail_response.addProperty("message", "invalid or missing access token");
//			return Response.status(Response.Status.UNAUTHORIZED).entity(fail_response).type(MediaType.APPLICATION_JSON).build();
//		}
//		
//		HumansUser user = getUserForAccessToken(context, access_token);
//		
//		if(user == null) {
//			invalid_user_error_response.addProperty("message", "no such user. invalid access token");
//			return Response.status(Response.Status.UNAUTHORIZED).entity(invalid_user_error_response).type(MediaType.APPLICATION_JSON).build();
//		}
//
//		// take the user and put it off somewhre to update?
		
		//return Response.ok("{}", MediaType.APPLICATION_JSON).build();
	}
	

	 private class StockPriceRunner implements Runnable {
	        AsyncContext asyncContext;
	        
	        public StockPriceRunner(AsyncContext asyncContext) {
	            this.asyncContext = asyncContext;
	        }
	 
	        @Override
	        public void run() {
	        	System.out.println(asyncContext.getRequest());
	        	System.out.println(asyncContext.getRequest().getParameter("humanid"));
//	        	String human_id = asyncContext.getRequest().getAttribute("humanid").toString();
//	        	System.out.println(human_id);
	            try {
		        	//Thread.sleep(20000);
		        	System.out.println(asyncContext.getRequest().getParameter("humanid"));

	                asyncContext.getResponse().getWriter().printf("parameter was: %s", asyncContext.getRequest().getParameter("humanid"));
	                asyncContext.dispatch();

	            } catch (IOException e) {
	                throw new RuntimeException(e);
				} finally {
	                asyncContext.complete();
	            }
	        }
	    }
	
}
