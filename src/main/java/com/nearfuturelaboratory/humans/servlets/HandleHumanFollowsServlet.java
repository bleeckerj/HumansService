package com.nearfuturelaboratory.humans.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.nearfuturelaboratory.humans.entities.Human;
import com.nearfuturelaboratory.humans.entities.HumansUser;
import com.nearfuturelaboratory.humans.entities.ServiceEntry;
import com.nearfuturelaboratory.humans.entities.ServiceUser;

/**
 * Servlet implementation class HandleHumanFollowsServlet
 */
@WebServlet("/HandleHumanFollowsServlet")
public class HandleHumanFollowsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	final static Logger logger = LogManager.getLogger(com.nearfuturelaboratory.humans.servlets.HandleHumanFollowsServlet.class);

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public HandleHumanFollowsServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.debug(request+" "+response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		//logger.debug("doPost called a bunch of times? "+request.getParameterMap());
		HttpSession session = request.getSession();
		Human human;// = (Human)session.getAttribute("human");
		//logger.debug("is session from cookie? "+request.isRequestedSessionIdFromCookie());
		if(session.isNew()) {
			logger.debug("new session..");
			human = new Human();
			session.setAttribute("human", human);
		}  else {
			//logger.debug("not a new session");
			/*			Enumeration<String> names = session.getAttributeNames();
			while(names.hasMoreElements()) {
				//logger.debug("In Session: "+names.nextElement());
			}
			 */			
			human = (Human)session.getAttribute("human");

		}
		//logger.debug("Session is "+session);
		HumansUser user = (HumansUser)session.getAttribute("logged-in-user");

		//logger.debug("does the session have human? "+human);
		if(user == null) {
			response.sendRedirect(request.getContextPath()+"/login.jsp");
			return;
		}

		if(human == null) {
			human = new Human();
			//logger.debug("created new human in the session "+session);
			session.setAttribute("human", human);
			//logger.debug("WTF? "+session.getAttribute("human"));
		}

		logger.debug("human is "+human);
		
		//logger.debug(request.getParameterMap());
		if(true || request.getParameter("add_service_user") != null) {
			String human_json_str = request.getParameter("human_json");
			logger.debug(human_json_str);
			// marshall my parameters

			//String coded_id = request.getParameter("coded_id");
/*			String service_id = request.getParameter("service_id");
			String username = request.getParameter("username");
			String onBehalfOf = request.getParameter("onbehalfof");
			String service = request.getParameter("service");
			String image_url = request.getParameter("image_url");
			logger.debug(request.getParameter("test_array"));
			human.addServiceUser(username, service_id, service, onBehalfOf, image_url);
*/			
			//List<String>service_ids = request.getParameter("service_ids");
			String[] x = request.getParameterValues("usernames");
			String foo = request.getParameter("usernames");
			//Object obj = JSONValue.parse(foo);
			String bar = request.getParameter("services");
			//obj = JSONValue.parse(bar);
			//JSONArray a = (JSONArray)obj;
			
			Object obj = JSONValue.parse(request.getParameter("human_json"));
			JSONObject human_json = (JSONObject)obj;
			JSONArray serviceUsers = (JSONArray)human_json.get("serviceUsers");
			
			
			
			//JSONArray array = (JSONArray)obj;
			//List<String>service_ids = request.getParameterValues("service_ids");
/*			List<String>usernames = Arrays.asList(request.getParameterValues("usernames"));
			List<JSONArray>services =  Arrays.asList(a);
			List<String>onbehalfofs = Arrays.asList(request.getParameterValues("onbehalfofs"));
			List<String>image_urls = Arrays.asList(request.getParameterValues("image_urls"));
*/			
			for(int k=0; k<serviceUsers.size(); k++) {
				JSONObject serviceUser = (JSONObject)serviceUsers.get(k);
				String username = serviceUser.get("username").toString();
				String service_id = serviceUser.get("service_id").toString();
				String service = serviceUser.get("service").toString();
				String onbehalfof_username = serviceUser.get("onbehalfof_username").toString();
				String onbehalfof_userid = serviceUser.get("onbehalfof_userid").toString();
				String image_url;// = "http://scrumpythekat.tumblr.com/image/61234462933";
				try {
					image_url = serviceUser.get("image_url").toString();
				} catch(NullPointerException npe) {
					logger.warn("Sometimes image_url doesn't come through?");
					image_url = "http://scrumpythekat.tumblr.com/image/61234462933";
				}
				ServiceEntry onbehalfof = new ServiceEntry(onbehalfof_userid, onbehalfof_username, service);

				ServiceUser service_user = new ServiceUser(service_id, username, service, image_url, onbehalfof);
				//service_user.setImageURL(image_url);
				logger.debug(service_user);
				logger.debug(onbehalfof);
				
				human.addServiceUser(service_user);
			}
			
			logger.debug("add_service_user "+human);
			//logger.debug("in doPost "+user);
			//logger.debug("and huma = "+human);

			logger.debug("added service user now have "+human);
		}
		
		logger.debug("Did we find a name for the new human? "+request.getParameter("human_name"));
		// trying to do this in one step
		if(true || request.getParameter("save_new_human") != null) {
			// need the human name..
			String name = request.getParameter("human_name");
			if(name == null || name.length() < 1) {
				logger.warn("Service User name is empty. Nothing. Not going to save."+human.getServiceUsers());
				JSONObject responseJSON = new JSONObject();
				responseJSON.put("error", "Service User is name is empty.  Not going to save.");
				responseJSON.put("human", human.toString());
				response.setContentType("application/json");
				response.setStatus(HttpServletResponse.SC_OK);
				response.getWriter().write(responseJSON.toJSONString());
				response.getWriter().flush();
				return;
			}

			human.setName(name);
			if (human.getServiceUsers() == null | human.getServiceUsers().size() < 1) {
				logger.warn("Service User is empty. Nothing. Not going to save."+human.getServiceUsers());
				JSONObject responseJSON = new JSONObject();
				responseJSON.put("error", "Service User is empty. Not going to save.");
				responseJSON.put("human", human.toString());
				response.setContentType("application/json");
				response.setStatus(HttpServletResponse.SC_OK);
				response.getWriter().write(responseJSON.toJSONString());
				response.getWriter().flush();
				return;
			}
			logger.debug("trying to add human "+human);
			user.addHuman(human);
			user.save();
			session.removeAttribute("human");
			human = null;
			response.sendRedirect(request.getContextPath()+"/user.jsp");
			// now it should save..

		}

	}

}
