<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>





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

<script type="text/javascript">

$.ajaxSetup({
	  beforeSend: function(xhr) {
	    xhr.setRequestHeader('X-CSRF-Token', $('meta[name="csrf-token"]').attr('content'));
	  }
	}); 

/* $(document).ready(function() {
// 	$("#human_name_form").hide();
 
	
});
 */
function getParameterByName(name) {
    var match = RegExp('[?&]' + name + '=([^&]*)').exec(window.location.search);
    return match && decodeURIComponent(match[1].replace(/\+/g, ' '));
}

$('#login').on('click', function(e) {
	alert('whoho!');
});

function login(obj) {
	console.log(obj);
	var username = $(obj.parentElement).find("#username").val();
	var password = $(obj.parentElement).find("#password").val();
	var params = {
        username : username,
        password : password
    };
	
$.ajax({
	async:false,
	type : "GET",
	traditonal : true,
	//url : "/rest/login?username="+getParameterByName('username')+"&password="+getParameterByName('password'),
	url : "<%=request.getContextPath()%>/rest/login?"+$.param(params),
	data : {
//			human_json : JSON.stringify( human ),
//			human_name : $('#human_name').val(),
//			add_service_user : 'yes',
		
		},
	}).success(function(data) {
		console.log(data);
		window.location.replace("user.jsp");
	}).error(function(xhr, ajaxOptions, thrownError) {
		console.log(thrownError + " "+ xhr.status+" "+ajaxOptions);
//		$("#response").html(thrownError + " " + xhr.status + " " + ajaxOptions)
	});
}

</script>

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

						<form action="javascript:void(0)">
			
							<div class="alert alert-error">
								<a class="close" data-dismiss="alert" href="#">x</a>Incorrect
								Username or Password!
							</div>
							<input class="span3" placeholder="Username" type="text"
								id="username"> <input class="span3"
								placeholder="Password" type="password" id="password">
							<label class="checkbox"> <input type="checkbox"
								name="remember" value="1"> Remember Me
							</label>
							<button type='button' onClick='login(this)' class="btn-info btn">Login</button>
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