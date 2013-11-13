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

<%@ page import="com.nearfuturelaboratory.humans.core.*"%>
<%
//out.println(request.getContextPath());

	if (request.getParameter("username") != null
			&& request.getParameter("password") != null) {

		HumansUser user = new HumansUser(
				request.getParameter("username"),
				request.getParameter("password"));
		//System.out.println(user);
		if(user.isValidUser()) {
			session.setAttribute("logged-in-user", user);
			//System.err.println("Okay.."+request.getContextPath());
			response.sendRedirect(request.getContextPath()+"/user.jsp");
		}
	}
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
<title>Insert title here</title>
</head>
<body>
	<div class="container">
		<div class="row">
			<div class="span4 offset4">
				<div class="well">
					<fieldset style="border: 0">
						<legend style="line-height: 50px">Sign In To Humans</legend>

						<form method="POST" action="<%=request.getContextPath() %>/login.jsp"
							accept-charset="UTF-8">
							<div class="alert alert-error">
								<a class="close" data-dismiss="alert" href="#">x</a>Incorrect
								Username or Password!
							</div>
							<input class="span3" placeholder="Username" type="text"
								name="username"> <input class="span3"
								placeholder="Password" type="password" name="password">
							<label class="checkbox"> <input type="checkbox"
								name="remember" value="1"> Remember Me
							</label>
							<button class="btn-info btn" type="submit">Login</button>
						</form>
					</fieldset>
				</div>
			</div>
		</div>
	</div>
	<script>
		$('.alert').hide();
	</script>

</body>
</html>