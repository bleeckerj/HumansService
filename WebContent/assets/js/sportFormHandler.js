function showAlert(text)
{
	alert(text);	
}

/**
 * 
 * @param id of a sport
 * @returns {Boolean}
 * 
 * This allows us to edit an existing sport when we pass the id
 * Called by the "Edit button (id: sport_button_${sport.getID()}) on the sport.jsp page
 * It calls sport-handler.jsp which saves a sport by id. I think I do this because
 * I'll also use the same handler to save a new sport??
 */
function updateSportByID(id)
{
	var sport_name;
	var sport_season;
	var sport_gender;


	if(id > -1) {
		sport_name = document.getElementById("sport_name_"+id).value;
		sport_season = document.getElementById("sport_season_"+id).value;
		sport_gender = document.getElementById("sport_gender_"+id).value;
	} else {
		sport_name = document.getElementById("sport_name").value;
		sport_season = document.getElementById("season").value;
		sport_gender = document.getElementById("gender").value;
	}	


	//alert(id);
	/*	document.getElementById("entity_name_"+id).style.visibility="hidden";
	 */	
	$.ajax({
		// what's this do? it allows us to edit
		// an existing sport that the user may've clicked on
		// via the "Edit" button in the rows of existing sports below the new sport
		// the response allows us to populate the 
		type: "POST",
		url: "./sport-handler.jsp",
		data: { name: sport_name, ID: id, season: sport_season, gender: sport_gender, hasBeenSubmitted: "yes"},
		dataType: 'json',
	}).success(function( msg ) {
		if( console && console.log ) {
		    console.log("Ajax Response from sport-handler.jsp:", msg);
		  }
		var foo = JSON.stringify(msg);
		var obj = jQuery.parseJSON(foo);
		// put in a message from the ajax response
		$('#message_area').text(obj.message);
		document.getElementById("addCompetitionButton").disabled = false;
		//document.getElementById("addCompetitionButton").text("Add Competition For "+msg.id);
//alert("Hoa! "+obj.message);

		// if we saved okay, then enable the add competition button

		//and change the save button title to update


		// Here was my practice for updating the contents of the
		// input form element..
		// I don't think we need this for the sport field
		// If we're 'editing' we can change it in place in the row below
		// No need to move it upstairs to the main sport form
		// Oh, whatever..

		// change the field in the sport name input element
		$('#sport').find('#sport_name').val(obj.sport_name);

		// change the hidden sport_id input element
		$('#sport').find('#sport_id').val(obj.id);
//		$('#sport').find('#addCompetitionButton')
		// change the gender field
		// note that the select2 styling extension to bootstrap has an api for making
		// changes that's different from the plain ol' select fields
		// with the plain ol' you can just change the value with val
		// cf: http://stackoverflow.com/questions/13987379/change-select2-option-on-click
		$('#sport').find("#gender").select2("val", obj.gender);

		if( console && console.log ) {
		    console.log("Message Contains: "+obj.message+" "+obj.sport_name+" "+obj.id);
		  }
		
		// with that fix above, I *don't think I need to fire an event.
		// I'll leave this commented out in case I'm wrong, but I think I
		// tested it thoroughly enough for the time being..
		/*		var event;
		if (document.createEvent) {
			event = document.createEvent("HTMLEvents");
			event.initEvent("onChange", true, true);
		} else {
			event = document.createEventObject();
			event.eventType = "onChange";
		}

		if (document.createEvent) {
			document.getElementById("gender").dispatchEvent(event);
		} else {
			document.getElementById("gender").fireEvent("on" + event.eventType, event);
		}
		
		 */	}
	).error(function( jqXHR, textStatus, errorThrown) {
			 if( console && console.log ) {
				    console.log("Error "+textStatus+" "+errorThrown);
				    console.log(jqXHR);
				  }
			 $('#message_area').val("Error "+textStatus+" "+errorThrown); 
	});

	return false;
}



function toggleEditFormVisibility(id)
{
	alert(id);
	document.getElementById(id).style.visibility="hidden";
	if(id.hidden == true) {
		id.hidden = false;
	} else {
		id.hidden = true;
	} 
	return false;
}

$(function() {
	$('.error').hide();
	$('.editcontainer1').hide();

	//$document.getElementById('#addCompetitionButton')
	
	$(window).keydown(function(event){
		if(event.keyCode == 13) {
			event.preventDefault();
			return false;
		}
	});

	// um..keep the form from submitting with a return/enter keypress
	// that way we don't accidentally do that? can't remember..
	$('form').bind("keyup", function(e) {
		var code = e.keyCode || e.which; 
		console.log("code = "+code);
		if (code  == 13) {               
			e.preventDefault();
			return false;
		}
	});
	//$('#sport_edit_1').hide();


//	$("#form1").validate({
//	submitHandler: function() {
//	//submit the form
//	$.post("<?php echo $_SERVER[PHP_SELF]; ?>", //post
//	$("#form1").serialize(), 
//	function(data){
//	//if message is sent
//	if (data == 'Sent') {
//	$("#message").fadeIn(); //show confirmation message
//	$("#form1")[0].reset(); //reset fields
//	}
//	//
//	});
//	return false; //don't let the page refresh on submit.
//	}
//	}); //validate the form  	
	$("#save_sport_submit").click(function() {
		// validate and process form here

		$('.error').hide();

		var name = $("input#sport_name").val();
		if(name == "") {
			$("label#sport_name_error").show();
			$("input#sport_name").focus();
			return false;
		} else {
			$("label#sport_name_error").hide();
		}
		/*    	var someText = "What The..";
    	alert("#{new_sport.name}"); */
		return true;
	});
});

function log(msg){
	if (window.console && console.log) {
		console.log(msg); //for firebug
	}
	document.write(msg); //write to screen
	$("#logBox").append(msg); //log to container
}