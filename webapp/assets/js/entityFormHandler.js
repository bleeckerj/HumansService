function showAlert(text)
{
 alert(text);	
}

function updateEntityByID(id)
{
	var value = document.getElementById("entity_name_"+id).value;
	alert(value);
/*	document.getElementById("entity_name_"+id).style.visibility="hidden";
*/	
	$.ajax({
		  type: "POST",
		  url: "./entity.jsp",
		  data: { name: value, ID: id, hasBeenSubmitted: "yes"}
		}).done(function( msg ) {
		  //alert( "Data Saved: " + msg );
		});
	
	return false;
}

$(function() {
  	$('.error').hide();
  	$('.editcontainer1').hide();
  	//$('#sport_edit_1').hide();
  	
  	
//	$("#form1").validate({
//		submitHandler: function() {
//			//submit the form
//			$.post("<?php echo $_SERVER[PHP_SELF]; ?>", //post
//				$("#form1").serialize(), 
//				function(data){
//				  //if message is sent
//				  if (data == 'Sent') {
//				    $("#message").fadeIn(); //show confirmation message
//				    $("#form1")[0].reset(); //reset fields
//				}
//				//
//			});
//			return false; //don't let the page refresh on submit.
//		}
//	}); //validate the form  	
    $("#save_button").click(function() {
      // validate and process form here
    
    	$('.error').hide();
    	
    	
    	
    	var name = $("input#sport_name").val();
    	if(name == "") {
    		$("label#sport_name_error").show();
    		$("input#sport_name").focus();
    		return false;
    	}
/*    	var someText = "What The..";
    	alert("#{new_sport.name}"); */
    	return true;
    });
  });