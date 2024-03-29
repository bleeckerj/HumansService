<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%@ page import="com.nearfuturelaboratory.util.file.*"%>
<%@ page import="com.nearfuturelaboratory.util.*"%>

<%@ page import="java.nio.file.*"%>
<%@ page import="java.nio.file.attribute.*"%>
<%@ page import="java.util.*"%>
<%@ page import="org.json.simple.JSONObject"%>
<%@ page import="org.jasypt.util.password.*"%>
<%@ page import="java.io.*"%>
<%@ page import="org.json.simple.parser.*"%>
<%@ page import="com.nearfuturelaboratory.humans.entities.HumansUser"%>
<%@ page import="com.nearfuturelaboratory.humans.entities.ServiceEntry"%>



<%
	//request.getContextPath()+"/
	HumansUser user = (HumansUser) session
			.getAttribute("logged-in-user");

	if (user == null) {
		response.sendRedirect(request.getContextPath() + "/login.jsp");
	}
%>


<!DOCTYPE html>
<html>
<meta name="viewport" content="width=device-width, initial-scale=1.0">

<head>
<script type="text/javascript">
function deleteServiceUser(obj)
{
	var parent = $(obj.parentElement.parentElement);
	var service_user_id = parent.find('#service_user_id').text();
	var service_username = parent.find('#service_username').text();
	//var service = service_name.text();
	//var service = panel.find('#service_name');
	var service_name = parent.find('#service_name').text().toLowerCase();
	
	var panel = parent.parent();
	
	var service = {
		serviceUserId : ('#service_user_id').text();
	
	}
	
	$.ajax({
		type : 'POST',
		dataType : 'json',
		url : '<%=request.getContextPath()%>/removeServiceFromUser.jsp',
			data : {
				service_name : service_name,
				service_user_id : service_user_id,
				service_username : service_username
			},
		}).success(function(data) {
			$("#response").html("HEY! "+data);
			panel.fadeOut("slow", function() {
			});
		}).error(function(xhr, ajaxOptions, thrownError) {
					$("#response").html("HELLOOOOO!");
/* 					$("#response").html(
							thrownError + " HELLO " + xhr.status + " " + ajaxOptions)
 */				});
		//var text = parent.find('.col-sm-10').text();
	}

	/* $(document).ready(function() {
	 $("#click").click(function() {
	 $.ajax({
	 type : "POST",
	 url : "pageTwo.jsp",
	 data : "name=" + name + "&age=" + age,
	 success : function(data) {
	 $("#response").html(data);
	 }
	 });
	 }); */
</script>


<script src="assets/js/jquery.js"></script>
<script src="assets/js/select2.js"></script>
<script src="assets/js/bootstrap.js"></script>
<link href="assets/css/select2.css" rel="stylesheet" />
<link rel="stylesheet" type="text/css" href="assets/css/bootstrap.css" />



<link
	href="//netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap-glyphicons.css"
	rel="stylesheet">
<link
	href="//netdna.bootstrapcdn.com/font-awesome/3.2.1/css/font-awesome.min.css"
	rel="stylesheet">


<link rel="stylesheet" type="text/css" href="assets/css/humans.css" />

<meta charset="utf-8">
<title>Services</title>
</head>
<body>
<div id="#response"></div>

	<div class="container">
		<div class="row">
			<div class="col-sm-12">
				<h4 id="response">Most Basic - Extra Small Columns</h4>
			</div>
		</div>
		<div class="row">
			<div class="col-xs-6" style="background: gray">Hello</div>
			<div class="col-xs-2">Goodbye</div>
		</div>
	</div>

	<div class="container">
		<div class="row">
			<div class="col-xs-2">
				<a href="<%=request.getContextPath()%>/rest/auth/twitter">twitter</a>
			</div>
		</div>
	</div>

	<%
		if (user != null) {
			List<ServiceEntry> twitterServiceUsers = user.getServicesForServiceName("twitter");
			for (int i = 0; twitterServiceUsers != null
					&& i < twitterServiceUsers.size(); i++) {
	%>

	<div class="container">

		<div class="row">

			<div class="panel panel-primary service-panel-sm">
				<div class="panel-heading">
					<h4 class="panel-title">Twitter</h4>
				</div>
				<div class="panel-body">
				<div class="hidden" id="service_user_id"><%=twitterServiceUsers.get(i).getServiceUserID() %></div>
				<div class="hidden" id="service_username"><%=twitterServiceUsers.get(i).getServiceUsername() %></div>
				<div class="hidden" id="service_name"><%=twitterServiceUsers.get(i).getServiceName() %></div>
					<div class="col-sm-10" id="service"
						style="background-color: #f0f0f0"><%=twitterServiceUsers.get(i)%></div>
					<div class="col-sm-2">
						<button class="btn btn-danger btn-xs"
							onclick="deleteServiceUser(this)">Remove</button>
					</div>
				</div>
			</div>

			<%
				}
			%>
			<button class="btn btn-default"
				onclick="location.href='<%=request.getContextPath()%>/rest/auth/twitter?access_token=<%=user.getAccessToken()%>';">Add
				Twitter</button>

		</div>
	</div>
	<br />
	<br />
	<%
		List<ServiceEntry> instagramServiceUsers = user.getServicesForServiceName("instagram");
			for (int i = 0; instagramServiceUsers != null
					&& i < instagramServiceUsers.size(); i++) {
	%>
	<div class="container">

		<div class="row">

			<div class="panel panel-primary service-panel-sm">
				<div class="panel-heading">
					<h4 id="service_name" class="panel-title">Instagram</h4>
				</div>
				<div class="panel-body">
					<div id="service_code" class="col-sm-10" style="background-color: #f0f0f0"><%=instagramServiceUsers.get(i)%></div>
					<div class="col-sm-2">
						<button class="btn btn-danger btn-xs"
							onclick="deleteServiceUser(this)">Remove</button>
					</div>

				</div>
			</div>

			<%
				}
			%>
	<button class="btn btn-default"
		onclick="location.href='<%=request.getContextPath()%>/rest/auth/instagram?access_token=<%=user.getAccessToken()%>';">Add
		Instagram</button>


		</div>
	</div>
	
	
		<div class="container">
		<div class="row">
			<div class="col-xs-2">
				<a href="<%=request.getContextPath()%>/rest/auth/instagram">Instagram</a>
			</div>
		</div>
	</div>
<br />
<br />
	<%
			List<ServiceEntry> flickrServiceUsers = user.getServicesForServiceName("flickr");
			for (int i = 0; flickrServiceUsers != null
					&& i < flickrServiceUsers.size(); i++) {
	%>

	<div class="container">

		<div class="row">

			<div class="panel panel-primary service-panel-sm">
				<div class="panel-heading">
					<h4 id="service_name" class="panel-title">Flickr</h4>
				</div>
				<div class="panel-body">
					<div class="col-sm-10" id="service_code"
						style="background-color: #f0f0f0"><%=flickrServiceUsers.get(i)%></div>
					<div class="col-sm-2">
						<button class="btn btn-danger btn-xs"
							onclick="deleteServiceUser(this)">Remove</button>
					</div>
				</div>
			</div>

			<%
				}
			%>
			<button class="btn btn-default"
				onclick="location.href='<%=request.getContextPath()%>/rest/auth/flickr?access_token=<%=user.getAccessToken()%>';">Add
				Flickr</button>

		</div>
	</div>
	<br />
	<br />
	
	
		<div class="container">
		<div class="row">
			<div class="col-xs-2">
				<a href="<%=request.getContextPath()%>/rest/auth/foursquare">foursquare</a>
			</div>
		</div>
	</div>

	<%
			List<ServiceEntry> foursquareServiceUsers = user.getServicesForServiceName("foursquare");
			for (int i = 0; foursquareServiceUsers != null
					&& i < foursquareServiceUsers.size(); i++) {
	%>

	<div class="container">

		<div class="row">

			<div class="panel panel-primary service-panel-sm">
				<div class="panel-heading">
					<h4 id="service_name" class="panel-title">Foursquare</h4>
				</div>
				<div class="panel-body">
					<div class="col-sm-10" id="service_code"
						style="background-color: #f0f0f0"><%=foursquareServiceUsers.get(i)%></div>
					<div class="col-sm-2">
						<button class="btn btn-danger btn-xs"
							onclick="deleteServiceUser(this)">Remove</button>
					</div>
				</div>
			</div>

			<%
				}
			%>
			<button class="btn btn-default"
				onclick="location.href='<%=request.getContextPath()%>/login-foursquare';">Add
				Foursquare</button>

		</div>
	</div>
	<br />
	<br />
	
	
	<%
		}
	%>

</body>
</html>