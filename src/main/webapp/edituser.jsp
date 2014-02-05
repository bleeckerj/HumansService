<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="com.nearfuturelaboratory.humans.entities.*"%>
<%@ page import="com.nearfuturelaboratory.util.*"%>
<%@ page import="java.nio.charset.*"%>
<%@ page import="org.apache.logging.log4j.*"%>
<%
    HumansUser user = (HumansUser) session.getAttribute("logged-in-user");
    if(user == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
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

<script src="assets/js/humansEditUser.js"></script>

<meta charset="utf-8">
<title>Insert title here</title>
</head>
<body>
	<div class="container">
		<div class="row">
			<div class="span4 offset4">
				<div class="well">
					<form class="form-horizontal" id="userform" name="userform"
						action='edituser.jsp' method="POST">
						<fieldset>
							<div id="legend">
								<legend class="">Register</legend>
							</div>
							<div class="control-group">
								<!-- Username -->
								<label class="control-label" for="username">Username</label>
								<div class="controls">
									<input type="text" id="username"
										value="" name="username"
										placeholder="" class="input-xlarge" required>
									<p class="help-block">Username can contain any letters or
										numbers, without spaces</p>
								</div>
							</div>

							<div class="control-group">
								<!-- E-mail -->
								<label class="control-label" for="email">E-mail</label>
								<div class="controls">
									<input type="email" id="email" name="email" placeholder=""
										class="input-xlarge" required>
									<p class="help-block">Please provide your E-mail</p>
								</div>
							</div>

							<div class="control-group">
								<!-- Password-->
								<label class="control-label" for="password">Password</label>
								<div class="controls">
									<input type="password" id="password" name="password"
										placeholder="" class="input-xlarge" required>
									<p class="help-block">Password should be at least 8
										characters</p>



								</div>
							</div>

							<div class="control-group">
								<!-- Password -->
								<label class="control-label" for="password_confirm">Password
									(Confirm)</label>
								<div class="controls">
									<input type="password" id="password_confirm"
										name="password_confirm" placeholder="" class="input-xlarge">
									<p class="help-block">Please confirm password</p>
								</div>
							</div>

							<div class="control-group">
								<!-- Button -->
								<div class="controls">
									<button class="btn btn-success" name="register" id="register">Register</button>
									<button type="submit" name="submit" id="submit"
										class="btn btn-warning">
										<span class="glyphicon glyphicon-star"></span>Create<span
											class="glyphicon glyphicon-star">
									</button>
								</div>
							</div>
						</fieldset>
					</form>
					<!-- <script id="demo" type="text/javascript">
$(document).ready(function() {
	$("#userform").validate();
	//$("#password").valid();
	
});
</script> -->
				</div>
			</div>
		</div>
	</div>
	<script>
		$('.alert').hide();
	</script>

</body>
</html>