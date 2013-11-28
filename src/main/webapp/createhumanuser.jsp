<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Create Human User</title>

<script src="assets/js/jquery.js"></script>
<script src="assets/js/jquery.validate.js"></script>
<script src="assets/js/select2.js"></script>
<script src="assets/js/bootstrap.js"></script>
<link href="assets/css/select2.css" rel="stylesheet" />
<link rel="stylesheet" type="text/css" href="assets/css/bootstrap.css" />
<link href="//netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap-glyphicons.css"
	rel="stylesheet">
<link href="//netdna.bootstrapcdn.com/font-awesome/3.2.1/css/font-awesome.min.css"
	rel="stylesheet">
<link rel="stylesheet" type="text/css" href="assets/css/humans.css" />

<script>
	$.validator.setDefaults({
		submitHandler : function() {
			$.ajax({
				async:true,
				type : "POST",
				traditional : true,
				url : '<%=request.getContextPath()%>/create.human.user',
				data : {
					username : $('#username').val(),
					password : $('#password').val(),
					email : $('#email').val(),
					
				},
			}).success(function(data) {
				console.log(data);
				window.location.replace("user.jsp");
			}).error(function(xhr, ajaxOptions, thrownError) {
				console.log(thrownError + " "+ xhr.status+" "+ajaxOptions);
//				$("#response").html(thrownError + " " + xhr.status + " " + ajaxOptions)
			});
		}
	});

	$.ajaxSetup({
		beforeSend : function(xhr) {
			xhr.setRequestHeader('X-CSRF-Token', $('meta[name="csrf-token"]').attr('content'));
		}
	});

	$(document).ready(function() {
		$("#createHumanUser").validate();
		
		$("#createHumanUser").validate({
			rules : {
				username : {
					required : true,
					minlength : 3
				},
				password : {
					required : true,
					minlength : 7
				},
				confirm_password : {
					required : true,
					minlength : 7,
					equalTo : "#password"
				},
				email : {
					required : true,
					email : true
				},
			},
			messages : {
				username : {
					required : "Please enter a username",
					minlength : "Your username must consist of at least 3 characters"
				},
				password : {
					required : "Please provide a password",
					minlength : "Your password must be at least 7 characters long"
				},
				confirm_password : {
					required : "Please provide a password",
					minlength : "Your password must be at least 7 characters long",
					equalTo : "Please enter the same password as above"
				},
				email : "Please enter a valid email address"
			}
		});

	});
</script>
</head>
<body>
	<form  id="createHumanUser" method="get" action="">
		<fieldset>
			<legend>Create a User</legend>
			<p>
				<label for="username">Username</label> <input id="username" name="username"
					type="text" />
			</p>
			<p>
				<label for="password">Password</label> <input id="password" name="password"
					type="password" />
			</p>
			<p>
				<label for="confirm_password">Confirm password</label> <input id="confirm_password"
					name="confirm_password" type="password" />
			</p>
			<p>
				<label for="email">Email</label> <input id="email" name="email" type="email" />
			</p>
			<p>
				<input class="submit" type="submit" value="Submit" />
			</p>
		</fieldset>
	</form>
</body>
</html>