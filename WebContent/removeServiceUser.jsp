<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/sql" prefix="sql"%>
<%@ page import="java.util.*"%>
<%@ page import="org.json.simple.JSONObject"%>
<%@ page import="java.io.*"%>
<%@ page import="org.json.simple.parser.*"%>
<%@ page import="com.nearfuturelaboratory.humans.core.*"%>

<%
// this should be a servlet
out.println(request.getParameterMap());
HumansUser user = (HumansUser)session.getAttribute("logged-in-user");
out.println(user);
if(user != null) {
	
	user.removeServiceUser(request.getParameter("service_name"), request.getParameter("service_code"));
}




%>





