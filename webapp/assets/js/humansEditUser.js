$.validator.setDefaults({
	submitHandler: function() { alert("submitted!");}
});

$().ready(function() {

/*
	$("#userform").validate({
		rules: {
            email: "required",
            username:{
            	required: true,
            	minlength: 3
            },
			password: {
				required: true,
				minlength: 8
			},
			password_confirm: {
				required: true,
				equalTo: "#password"
			}
		},
        messages: {
            username: {
                    required: "Enter a username",
                    minlength: jQuery.format("Enter at least {0} characters")
            },
            password_confirm: {
                    required: "Repeat your password",
                    minlength: jQuery.format("Enter at least {0} characters"),
                    equalTo: "Enter the same password as above"
            }
    },
    errorPlacement: function(error, element) {
        error.prependTo( element.parent().next() );
    }
	});*/
});

function handlerUser() {
	var username= document.getElementById("username").value;
	var email = document.getElementById("email").value;
	var password = document.getElementById("password").value;
	var password_confirm = document.getElementById("password_confirm").value;
	
/*	if(validEmail(email)) {
		log(email+" email is valid");
	}
*/	

	
}

function validEmail(v) {
    var r = new RegExp("[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?");
    return (v.match(r) == null) ? false : true;
}

function log(msg){
	if (window.console && console.log) {
		console.log(msg); //for firebug
	}
	document.write(msg); //write to screen
	$("#logBox").append(msg); //log to container
}