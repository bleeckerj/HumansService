package com.nearfuturelaboratory.humans.servlets;

import java.io.IOException;


import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import com.nearfuturelaboratory.humans.core.HumansUser;

@WebServlet(name = "CreateHumanUserServlet", urlPatterns = {"/create.human.user"})
public class CreateHumanUserServlet extends HttpServlet {


	/**
	 * 
	 */
	private static final long serialVersionUID = 8802837154256801301L;
	final static Logger logger = Logger.getLogger("com.nearfuturelaboratory.humans.test.Test");


	public CreateHumanUserServlet() {
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		JSONObject json_response = new JSONObject();
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String email = request.getParameter("email");
		
		if(HumansUser.doesUsernameExist(username)) {
			json_response.put("status", "error");
			json_response.put("message", "Username "+username+" already exists");
			response.setContentType("application/json");
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().write(json_response.toJSONString());
			response.getWriter().flush();
			return;

		}
		if(username == null) {

		}
		if(username.length() < 3) {

		}
		if(password == null) {

		}
		if(password.length() < 7) {

		}

		HumansUser humansUser = new HumansUser();		
		humansUser.setUsername(username);
		humansUser.setPassword(password);
		humansUser.setEmail(email);
		
		
		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().write(humansUser.toString());
		response.getWriter().flush();

	}

}
