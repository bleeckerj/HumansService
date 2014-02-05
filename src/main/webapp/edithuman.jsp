<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.nearfuturelaboratory.util.file.*"%>
<%@ page import="java.nio.file.*"%>
<%@ page import="java.nio.file.attribute.*"%>
<%@ page import="java.util.*"%>
<%@ page import="org.json.simple.JSONObject"%>
<%@ page import="org.jasypt.util.password.*"%>
<%@ page import="java.io.*"%>
<%@ page import="org.json.simple.parser.*"%>
<%@ page import="com.nearfuturelaboratory.humans.entities.*"%>
<%@ page import="com.nearfuturelaboratory.humans.entities.MinimalSocialServiceUser"%>

<%@ page import="org.json.simple.*"%>
<%
HumansUser user = (HumansUser) session.getAttribute("logged-in-user");

// the session attribute used to build up the new human on this page
session.removeAttribute("human");
List<Human> humans = new ArrayList<Human>();
//Human human = (Human) session.getAttribute("human_in_progress");
if (user == null) {
	response.sendRedirect(request.getContextPath() + "/login.jsp");
} else {
	humans = user.getAllHumans();
}

%>
<!DOCTYPE html>
<html>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Edit Human</title>
</head>
<body>
<%  for(Human human : humans) {  %>

<%=human.getName() %><br/>

<% for(ServiceUser service_user : human.getServiceUsers()) { %>

<img src="<%= service_user.getImageURL() %>"/><%= service_user.getService() %>  <%=service_user.getUsername() %><br/>

<% } %>
<% } %>
</body>
</html>