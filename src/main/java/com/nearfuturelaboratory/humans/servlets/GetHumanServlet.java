package com.nearfuturelaboratory.humans.servlets;

import javax.servlet.http.HttpServlet;
import javax.servlet.annotation.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.scribe.builder.*;
import org.scribe.builder.api.*;
import org.scribe.model.*;
import org.scribe.oauth.*;

import com.nearfuturelaboratory.humans.core.Human;
import com.nearfuturelaboratory.humans.core.HumansUser;
import com.nearfuturelaboratory.humans.serviceapi.InstagramApi;
import com.nearfuturelaboratory.humans.service.*;
import com.nearfuturelaboratory.util.*;

import java.io.*;
import java.util.Scanner;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


@WebServlet(name = "GetHumanServlet", urlPatterns = {"/getHuman"})
public class GetHumanServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6406510742220694429L;
	final static Logger logger = LogManager.getLogger("com.nearfuturelaboratory.humans.test.Test");


	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		//

		HttpSession session = req.getSession();
		HumansUser user = (HumansUser)session.getAttribute("logged-in-user");
		if(user == null) {
			resp.sendRedirect(req.getContextPath()+"/login.jsp");
			return;
		}

		// load the Human into memory? and then portion it out?
		Human human;
		JSONObject json_response = new JSONObject();

		String humanName = req.getParameter("name");
		
		if(humanName == null) {
			json_response.put("status", "error");
			json_response.put("message", "Missing parameter 'name'");
			resp.setContentType("application/json");
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().write(json_response.toJSONString());
			resp.getWriter().flush();

			return;
		}
		
		human = user.getHumanByName(humanName);
		if(human == null) {
			JSONArray foo = new JSONArray();
			foo.addAll(user.getAllHumans());
			logger.debug(user.getAllHumansUsers_Usernames());
			json_response.put("error", "no such human for user");
			json_response.put("human name", humanName);
			json_response.put("user", user.getUsername());
			json_response.put("humans", foo);
			resp.setContentType("application/json");
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().write(json_response.toJSONString());
			resp.getWriter().flush();

			return;
			
		}
		
		
		//prior human
		String sessionHumanName = null;// = session.getAttribute("human_name").toString();
		
		if(session.getAttribute("human_name") == null || session.getAttribute("human_name").equals(humanName) == false) {
			session.removeAttribute("human_name");
			session.setAttribute("human_name", humanName);
			//session.removeAttribute("status");
			session.removeAttribute("human_json");
			sessionHumanName = humanName;
			
		}
		
		
		JSONArray human_status = null;
		//JSONArray service_users = null;

		JSONObject human_json = (JSONObject)session.getAttribute("human_json");
		//status = (JSONArray)session.getAttribute("status");
		//service_users = (JSONArray)session.getAttribute("service_users");

		if(human_json == null) {
			human_json = user.loadHumanStatuses(human.getName());
			session.setAttribute("human_json", human_json);
		}
			//service_users = (JSONArray)humanJSON.get("service_users");
			human_status = (JSONArray)human_json.get("status");
			//session.setAttribute("status", status);
			//session.setAttribute("service_users", service_users);
		
		String page = req.getParameter("page");
		if(page == null) page = "1";
		json_response.put("count", String.valueOf(human_status.size()));
		json_response.put("pages", human_status.size()/20);
		json_response.put("page", page);
		json_response.put("name", human.getName());
		json_response.put("human_user", human.toString());
		// the pages are 20 in length
		int start_of_chunk = 20*(Integer.parseInt(page) - 1);

		json_response.put("status", human_status.subList(start_of_chunk, start_of_chunk+20));



		resp.setContentType("application/json");
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.getWriter().write(json_response.toJSONString());
		resp.getWriter().flush();




		logger.debug("How Much Status? "+human_status.size());
	}

}
