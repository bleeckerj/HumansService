<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
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
	JSONObject obj_follows = null;
	HumansUser user = (HumansUser) session
			.getAttribute("logged-in-user");

	// the session attribute used to build up the new human on this page
	session.removeAttribute("human");

	//Human human = (Human) session.getAttribute("human_in_progress");
	if (user == null) {
		response.sendRedirect(request.getContextPath() + "/login.jsp");
	} else {
		obj_follows = user.getFollows();
		//obj_twitter = (JSONObject) obj.get("twitter");

	}
%>


<html>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<head>
<script src="assets/js/jquery.js"></script>
<script src="assets/js/select2.js"></script>
<script src="assets/js/bootstrap.js"></script>
<link href="assets/css/select2.css" rel="stylesheet" />
<link rel="stylesheet" type="text/css" href="assets/css/bootstrap.css" />
<link href="//netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap-glyphicons.css"
	rel="stylesheet">
<link href="//netdna.bootstrapcdn.com/font-awesome/3.2.1/css/font-awesome.min.css"
	rel="stylesheet">
<link rel="stylesheet" type="text/css" href="assets/css/humans.css" />

<script type="text/javascript">

$.ajaxSetup({
	  beforeSend: function(xhr) {
	    xhr.setRequestHeader('X-CSRF-Token', $('meta[name="csrf-token"]').attr('content'));
	  }
	}); 

$(document).ready(function() {
 	$("#human_name_form").hide();
 
	
});


function saveThisHuman(obj) {
	var service_users = $('#prepare_to_add');
	var usernames = $(service_users).find('#username');
	var services = $(service_users).find('#service');
	var onbehalfofs = $(service_users).find('#onbehalfof');
	var service_ids = $(service_users).find('#service_id');
	var image_urls = $(service_users).find('#image_url');
	
	var usernameArray = [];
	var service_idArray = [];
	var onbehalfofArray = [];
	var serviceArray = [];
	var image_urlArray = [];
	
/* 	
	for(var j=0; j<usernames.length; j++) {
		usernameArray[j] = $(usernames[j]).text();	
		service_idArray[j] = $(service_ids[j]).text();
		onbehalfofArray[j] = $(onbehalfofs[j]).text();
		serviceArray[j] = $(services[j]).text();
		image_urlArray[j] = $(image_urls[j]).attr('src');
	}
 */	
	//for(var i = 0; i < usernames.length; i++) {
		//console.log($(usernames[i]).text()+" "+$(services[i]).text()+" "+$(onbehalfof[i]).text()+" "+$(service_id[i]).text());
		console.log(usernameArray);
		console.log(JSON.stringify(usernameArray));
		//TODO put it in one thing rather than willy-nilly lists
		// put it into a proper json structure
		var human = new Object();
		human.name = $('#human_name').val();
		var service_users = [];
		for(var i=0; i<services.length; i++) {
			var service_user = new Object();
			service_user.service = $(services[i]).text();
			service_user.username = $(usernames[i]).text();
			service_user.service_id = $(service_ids[i]).text();
			service_user.onbehalfof = $(onbehalfofs[i]).text();
			service_user.image_url = image_urls[i].src;
			service_users.push(service_user);
		}
		human.serviceUsers = service_users;
		
		$.ajax({
			async:false,
			type : "POST",
			traditonal : true,
			url : '<%=request.getContextPath()%>/HandleHumanFollowsServlet',
				data : {
					human_json : JSON.stringify( human ),
					human_name : $('#human_name').val(),
					add_service_user : 'yes',
				
				},
			}).success(function(data) {
				console.log(data);
				window.location.replace("createhuman.jsp");
			}).error(function(xhr, ajaxOptions, thrownError) {
				console.log(thrownError + " "+ xhr.status+" "+ajaxOptions);
//				$("#response").html(thrownError + " " + xhr.status + " " + ajaxOptions)
			});
		
		
	//}

	
}

var arrayOfServiceUsers = new Array();

function unprepareThisServiceUser(obj) {
	//$(obj).animate({ opacity: 0});
	//$(obj).attr("disabled", "disabled");
	var parent = $(obj.parentElement);
	var parent_parent = $(obj.parentElement.parentElement);

	var i = parent_parent.find('#element_number').text();
	arrayOfServiceUsers.splice(i, 1);
/* 	$(obj.parentElement.parentElement.parentElement.parentElement).fadeOut("slow", function() {
	});
 */	$("#prepare_to_add").html(arrayOfServiceUsers);
	var item_to_bring_back;
	var service_id = parent_parent.find('#service_id').text();
	$('*#service_id').each(function(){
	    if( $(this).text() == service_id ) {
	    	item_to_bring_back = $(this);
	    	return;
	    }
	});
	var baz = $(item_to_bring_back).parent().parent().parent().parent();
	var foo = $(item_to_bring_back.parentElement);
	var bar = $(foo.parentElement);
	//$(baz).show();
	
 	$(baz).fadeIn("fast", function() {
		
	});
 	var j = 0;
}

function prepareThisServiceUser(obj) {
	//$(obj).animate({ opacity: 0 });
	//$(obj).attr("disabled", "disabled");
	//$(obj).removeAttr("disabled");
	var parent = $(obj.parentElement);
	var parent_parent = $(obj.parentElement.parentElement);
	var coded_id = parent.find('#coded').text();
	var service_id = parent.find('#service_id').text();
	var username = parent.find('#username').text();
	var onbehalfof = parent.find("#onbehalfof").text();
	var service = parent.find("#service").text();
	var image_url = parent_parent.find('#image_url')[0].outerHTML;
	
	//console.log(image_url);
	
	
	
	arrayOfServiceUsers.push("<div><div class='row' style='padding-top: 20px; height: 140px'><div style='padding-left: 20px' >"+image_url+"<button type='button' class='btn btn-sm btn-danger' onclick='unprepareThisServiceUser(this)'>Remove</button></div><div class='row'>"+coded_id+" "+service_id+" "+username+" "+onbehalfof+" "+service+"</div>"+
		"<div class='hidden' id='service_id'>"+service_id+"</div><div class='hidden' id='username'>"+ username+"</div><div class='hidden' id='service'>"+service+"</div><div class='hidden' id='onbehalfof'>"+onbehalfof+"</div>"+
		"<div id='element_number'>"+arrayOfServiceUsers.length+"</div></div>");
	$("#prepare_to_add").html(arrayOfServiceUsers);
	$("#human_name_form").show();
	
   	$(obj.parentElement.parentElement.parentElement.parentElement).fadeOut("slow", function() {
		});
  
 
 //$(obj.parentElement.parentElement.parentElement.parentElement).hide();
 //	$(parent_parent).hide();
}





function addThisServiceUser(obj) {
	var parent = $(obj.parentElement);
	var coded_id = parent.find('#coded').text();
	var service_id = parent.find('#service_id').text();
	var username = parent.find('#username').text();
	var onbehalfof = parent.find("#onbehalfof").text();
	var service = parent.find("#service").text();
	
	// now we have the parameters to create a ServiceUser and add it ot the HumansUser, which
	// should be in the session for this page..
	// we'll still need to give the new Human a name..
	
<%-- 	$.ajax({
		type : "POST",
		url : '<%=request.getContextPath()%>/HandleHumanFollowsServlet',
			data : {
				coded_id : coded_id,
				service_id : service_id,
				username : username,
				onbehalfof : onbehalfof,
				service : service,
				add_service_user : 'yes',
				human_name : 'test name',
				save_new_human : 'yes'
			},
		}).success(function(data) {
			$("#response").html(data);
			//panel.fadeOut("slow", function() {
			//});
		}).error(function(xhr, ajaxOptions, thrownError) {
			$("#response").html(thrownError + " " + xhr.status + " " + ajaxOptions)
		});
 --%>		//var text = parent.find('.col-sm-10').text();
	}

	function searchUserLike(obj) {
		//var json = foo;

		// @formatter:off

		var json =<%=obj_follows%>;
		var result = new Array();
		var regex = new RegExp(".*" + obj + ".*", "gi");
		for ( var key in json) {
			//alert(key);
			//alert(JSON.stringify(json[key]));
			//console.log(JSON.stringify(json[key]));
			var followsByAccount = json[key];
			//alert(followsByAccount);
			for ( var account in followsByAccount) {
				//for(var follower in followsByAccount[account]) {
				$.each(followsByAccount[account], function(i, item) {
					if (regex.test(item.name) || regex.test(item.screen_name) || regex.test(item.username) || regex.test(item.firstName) 
						|| regex.test(item.lastName) )  {
						//alert(JSON.stringify(item));
						//alert(item.id+" "+item.name+" "+key+" i="+i);
						var image_url;
						// twitter?
						if (item.profile_image_url != null) {
							image_url = item.profile_image_url;
						}
						// instagram
						if (item.profile_picture != null) {
							image_url = item.profile_picture;
						}
						// flickr
						if(item.iconfarm != null) {
							if(item.iconfarm != 0) {
							image_url = "http://farm"+item.iconfarm+".staticflickr.com/"+item.iconserver+"/buddyicons/"+item.nsid+".jpg";
						} else {
							image_url = "http://www.flickr.com/images/buddyicon.gif";
						}
							item.id = item.nsid;
						}
						// foursquare
						if(item.photo != null) {
							image_url = item.photo.prefix+"height48"+item.photo.suffix;
						}
						
						var username;
						if (item.username != null) {
							username = item.username;
						} 
						if (item.screen_name != null) {
							username = item.screen_name;
						}
						if(item.firstName != null) {
							username = item.firstName+"_"+item.lastName;
						}
						//.replace("_normal.", "_small.");
						var markup = "<div class='panel panel-default'><div class='panel-body'>";
						markup += "<div class='panel-heading'>" + key + " " + account + " " + item.id + " " + username + "</div>";
						markup += "<div class='row'>";
						markup += "<div class='col-xs-2'>";
						markup += "<img id='image_url' style='max-height: 48px; max-width: 48px' src='"+image_url+"' class='img-rounded'>";
						markup += "</div><div class='col-xs-2'>";
						markup += "<div id='coded' class='hidden'>" + item.id + "-" + username + "</div>";
						markup += "<div id='username' class='hidden'>" + username + "</div>";
						markup += "<div id='service_id' class='hidden'>" + item.id + "</div>";
						markup += "<div id='onbehalfof' class='hidden'>" + account + "</div>";
						markup += "<div id='service' class='hidden'>" + key + "</div>";
						markup += "<button type='button' class='btn btn-sm btn-success' onclick='prepareThisServiceUser(this)'>Add</button><button type='button' class='btn btn-sm btn-danger'>Remove</button>";
						markup += "</div></div>";
						markup += "</div></div>";

						result.push(markup);
					}
				});
				//}
			}
		}

		$("#result").html(result);
	}
</script>



<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>


</head>
<body>

	<div class="container">
		<div class="row">
			<div class="col-xs-4" style="background-color: #f0d0d0;">
				<h4 id="response">Response Here</h4>
			</div>
			<div class="col-xs-5 pull-right"></div>
		</div>
		<div class="row">
			<div class="col-xs-4" style="background-color: #d0d0f0;">
				<input type="text" class="form-control" id="name_like" placeholder="Name Like..">
				<button class="btn btn-info btn-sm" style=""
					onclick="searchUserLike($('#name_like').val())">Search</button>
			</div>
			<div class="col-xs-8">Hello</div>
		</div>
		<div class="row">
			<div class="col-xs-7"></div>
			<div class="col-xs-5 pull-right" id="human_name_form"
				style="background-color: #dedede;">
				<input type="text" class="form-control pull-left" maxlength="25"
					style="max-width: 200px" id="human_name" placeholder="Name..">
				<div>
					<button type='button' class='btn btn-sm btn-success' onclick='saveThisHuman(this)'>Do
						Smtg</button>
				</div>
			</div>
		</div>
		<div class="row">
			<div class="col-xs-7" id="result" style="background-color: #d0d0d0;"></div>
			<div class="col-xs-5" id="prepare_to_add"
				style="height: 130px; background-color: #ff00e0;"></div>

		</div>


	</div>

	<!-- 


	<div class="row col-lg-6">
		input type="text" class="form-control" id="human_name" placeholder="Name.."

	</div> -->

</body>
</html>