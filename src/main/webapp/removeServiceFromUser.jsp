<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%@ page import="java.util.*"%>
<%@ page import="org.json.simple.JSONObject"%>
<%@ page import="java.io.*"%>
<%@ page import="org.json.simple.parser.*"%>
<%@ page import="com.nearfuturelaboratory.humans.entities.HumansUser"%>

<%
// this should be a servlet
out.println(request.getParameterMap());
HumansUser user = (HumansUser)session.getAttribute("logged-in-user");
out.println(user);
if(user != null) {
	//out.println(user);
	user.removeService(request.getParameter("service_user_id"), request.getParameter("service_username"), request.getParameter("service_name"));
	user.save();
}




%>





