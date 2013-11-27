<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/sql" prefix="sql"%>
<%@ page import="com.nearfuturelaboratory.util.file.*"%>
<%@ page import="java.nio.file.*"%>
<%@ page import="java.nio.file.attribute.*"%>
<%@ page import="java.util.*"%>
<%@ page import="org.json.simple.JSONObject"%>
<%@ page import="org.jasypt.util.password.*"%>
<%@ page import="java.io.*"%>
<%@ page import="org.json.simple.parser.*" %>
<%@ page import="com.nearfuturelaboratory.humans.entities.HumansUser" %>
<%
	HumansUser user = (HumansUser)session.getAttribute("logged-in-user");
if(user == null) {
	user = new HumansUser();
	response.sendRedirect(request.getContextPath()+"/login.jsp");
} 
	//out.println(user);
%>


<!DOCTYPE html>
<html>


<head>

<script src="assets/js/jquery.js"></script>
<script src="assets/js/select2.js"></script>
<script src="assets/js/bootstrap.js"></script>
<link href="assets/css/select2.css" rel="stylesheet" />
<link rel="stylesheet" type="text/css" href="assets/css/bootstrap.css" />


<link rel="stylesheet" type="text/css"
	href="assets/css/bootstrap-datetimepicker.min.css" />
<script src="assets/js/bootstrap-datetimepicker.min.js"></script>


<link
	href="//netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap-glyphicons.css"
	rel="stylesheet">
<link
	href="//netdna.bootstrapcdn.com/font-awesome/3.2.1/css/font-awesome.min.css"
	rel="stylesheet">


<link rel="stylesheet" type="text/css"
	href="assets/css/bootstrap-select.css" />
<script src="assets/js/bootstrap-select.js"></script>

<meta charset="utf-8">
<title>User</title>
</head>
<body>
<br/>

<p/>
<div class="container">
<div class="span5">
<table class="table table-striped table-condensed" style="table-layout:fixed; word-wrap: break-word">
	  <thead>
      <tr>
          <th>Username</th>
          <th>Date registered</th>
          <th>Email</th>
          <th>Status</th> 
          <th>Services</th>                                         
      </tr>
  </thead>   
	<tbody>
	  <tr>
	  <td>
	  <%=user.getUsername() %>
	  
	  </td>
	  <td>
	  </td>
	  <td>
	  <%=user.getEmail() %>
	  </td>
	  <td>
	  <span class="label label-success">Active</span>
	  </td>
	  <td><button type="button" id="services" class="btn btn-danger btn-xs" onclick="location.href='<%=request.getContextPath()%>/services.jsp';">Services</button></td>
	  </tr>
	  <tr>
	  <td colspan="3">
	  <%=user.toString() %>
	  </td>
	  <td></td>
	  <td>Click</td>
	  </tr>
	</tbody>
	</table>
	</div>

	</div>
</body>
</html>