<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.nearfuturelaboratory.humans.entities.HumansUser" %>
<%@ page import="com.nearfuturelaboratory.humans.util.MyObjectIdSerializer" %>
<%@ page import="org.bson.types.ObjectId" %>
<%@ page import="com.google.gson.*" %>

<%
HumansUser user = (HumansUser)session.getAttribute("logged-in-user");
if(user == null) {
	user = new HumansUser();
	response.sendRedirect(request.getContextPath()+"/login.jsp");
} 
Gson gson ;
gson = new GsonBuilder().registerTypeAdapter(ObjectId.class, new MyObjectIdSerializer()).create();
user.setPassword("*************");
String jsonUser = gson.toJson(user);
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


<link href="//netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap-glyphicons.css" rel="stylesheet">
<link href="//netdna.bootstrapcdn.com/font-awesome/3.2.1/css/font-awesome.min.css" rel="stylesheet">


<link rel="stylesheet" type="text/css" href="assets/css/bootstrap-select.css" />
<script src="assets/js/bootstrap-select.js"></script>
<script type="text/javascript">

var user = <%= jsonUser %>;

$.ajaxSetup({
	  beforeSend: function(xhr) {
	    xhr.setRequestHeader('X-CSRF-Token', $('meta[name="csrf-token"]').attr('content'));
	  }
	}); 

$(function() {
	   //$(document).on("click", "#clickme", showResults); 
	   showResults();
	});

function writeToDom(title, content) {
    $("#the_user").append("<div class='header'>" + title + "</div><div><pre>" + content + "</pre></div>");
}

function showResults() {
    //writeToDom('Plain', JSON.stringify(person));
    writeToDom('<%=user.getUsername()%>', JSON.stringify(user, null, 4));
}

$(function() {
   $(document).on("click", "#clickme", showResults); 
});

/* $(document).ready(function() {
 	$("#human_name_form").hide();
});
 */
 

/* $.ajax({
	
});
 */
</script>
<meta charset="utf-8">
<title>User</title>
</head>
<body>
<br/>

<p/>
<div class="row">
<div class="col-md-9">
<table class="table table-striped table-condensed" style="table-layout:fixed; word-wrap: break-word">
	  <thead>
      <tr>
          <th>Username</th>
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
	  <%=user.getEmail() %>
	  </td>
	  <td>
	  <span class="label label-success">Active</span>
	  </td>
	  <td><button type="button" id="services" class="btn btn-danger btn-xs" onclick="location.href='<%=request.getContextPath()%>/services.jsp';">Services</button></td>
	  </tr>
	  <tr>
	  <td colspan="4" >
	  <%=user.getUsername() %>
	  
	  </td>
	  <td></td>
	  <td>Click</td>
	  </tr>
	</tbody>
	</table>
		  	  <div class="col-lg-12" id="the_user"></div>
	
	</div>

	</div>
</body>
</html>