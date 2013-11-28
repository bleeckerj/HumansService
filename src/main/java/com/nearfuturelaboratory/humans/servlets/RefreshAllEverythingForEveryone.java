package com.nearfuturelaboratory.humans.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.nearfuturelaboratory.humans.test.TestHumanGson;

/**
 * Servlet implementation class RefreshAllEverythingForEveryone
 */
@WebServlet("/RefreshAllEverythingForEveryone")
public class RefreshAllEverythingForEveryone extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static boolean is_running = false;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public RefreshAllEverythingForEveryone() {
		super();
		is_running = false;
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject json_response = new JSONObject();
		if(is_running == false) {
			is_running = true;
			TestHumanGson test = new TestHumanGson();
			test.getAllEverythingForEveryone();
			is_running = false;
			json_response.put("status", "success");
			json_response.put("message", "finished");
			response.setContentType("application/json");
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().write(json_response.toJSONString());
			response.getWriter().flush();

		} else {
			json_response.put("status", "error");
			json_response.put("message", "Process already running");
			response.setContentType("application/json");
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().write(json_response.toJSONString());
			response.getWriter().flush();


		}
		return;

	}

}
