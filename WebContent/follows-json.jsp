<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.nearfuturelaboratory.util.file.*"%>
<%@ page import="java.nio.file.*"%>
<%@ page import="java.nio.file.attribute.*"%>
<%@ page import="java.util.*"%>
<%@ page import="org.json.simple.JSONObject"%>
<%@ page import="org.jasypt.util.password.*"%>
<%@ page import="java.io.*"%>
<%@ page import="org.json.simple.parser.*"%>
<%@ page import="com.nearfuturelaboratory.humans.core.*"%>
<%@ page import="org.json.simple.*"%>
<%@ page import="java.io.*"%>
<%@ page import="org.json.simple.parser.*"%>
<%
	JSONObject array = null;
	HumansUser user = (HumansUser) session
			.getAttribute("logged-in-user");
	if (user == null) {
		response.sendRedirect(request.getContextPath() + "/login.jsp");
	} else {
		JSONObject obj = user.getFollows();
		array = (JSONObject) obj.get("twitter");
	out.print(array);

	}
%>
