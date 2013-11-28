

var competitionCount;

function showAlert(text)
{
	alert(text);	
}

function submitHolderAsync(obj)
{
	var fieldset = $(obj.parentElement);
	var competition_id = $(document).find('#competition_id').val();
	var holder_name = fieldset.find('#holder').val();
	var holder_event = fieldset.find('#event').val();
	var holder_place = fieldset.find('#holder_place').val();
	var holder_gender = fieldset.find('#gender').val();
	var holder_country_id = fieldset.find('#country_id').val();
	
	$.ajax({
		// what's this do? it allows us to edit
		// an existing sport that the user may've clicked on
		// via the "Edit" button in the rows of existing sports below the new sport
		// the response allows us to populate the 
		type: "POST",
		url: "./holder-handler.jsp",
		data: { holder_name: holder_name, competition_id: competition_id, 
				holder_event:holder_event, holder_place:holder_place, holder_gender:holder_gender, 
				holder_country_id:holder_country_id,
				hasBeenSubmitted: "yes"},
		dataType: 'json',
	}).success(function( msg ) {
		if( console && console.log ) {
		    console.log("Ajax Response from competition-handler.jsp:", msg);
		  }
		var foo = JSON.stringify(msg);
		var obj = jQuery.parseJSON(foo);
		// put in a message from the ajax response
		$('#message_area').text(obj.message);
		$('#competition_id').val(obj.competition_id);

	}).error(function( msg) {
		
	});
	
}

/**
 * 
 * @param id of a competition
 * @returns {Boolean}
 * 
 * This allows us to edit an existing sport when we pass the id
 * Called by the "Edit button (id: sport_button_${sport.getID()}) on the sport.jsp page
 * It calls sport-handler.jsp which saves a sport by id. I think I do this because
 * I'll also use the same handler to save a new sport??
 */
function submitCompetitionAsync(obj)
{
	var comp_name;
	var comp_yearFirstHeld;
	var comp_yearNextHeld;
	var comp_dateLastHeld;
	var comp_frequency;
	var sport_id;
	
	var fieldset = $(obj.parentElement); // this'll be the fieldset of the competition form to which the button belongs
	var comp_id = fieldset.find('#competition_id').val();
	
	comp_name = fieldset.find('#competition_name').val();
	comp_yearFirstHeld = $(obj.parentElement).find('#yearfirstheld').val();
	comp_yearNextHeld = $(obj.parentElement).find('#yearnextheld').val();
	comp_dateLastHeld = $(obj.parentElement).find('#datelastheld').val();
	comp_frequency = fieldset.find('#frequency').val();
	sport_id = document.getElementById('sport_id').value;
	
	if(comp_id > -1) {
		//document.getElementById("competition_name).value;
	} else {

	}	
	/*	document.getElementById("entity_name_"+id).style.visibility="hidden";
	 */	
	$.ajax({
		// what's this do? it allows us to edit
		// an existing sport that the user may've clicked on
		// via the "Edit" button in the rows of existing sports below the new sport
		// the response allows us to populate the 
		type: "POST",
		url: "./competition-handler.jsp",
		data: { comp_name: comp_name, comp_id: comp_id, yearFirstHeld: comp_yearFirstHeld, yearNextHeld: comp_yearNextHeld, 
			dateLastHeld: comp_dateLastHeld, frequency: comp_frequency, sport_id: sport_id, holderHasBeenSubmitted: "yes"},
		dataType: 'json',
	}).success(function( msg ) {
		if( console && console.log ) {
		    console.log("Ajax Response from competition-handler.jsp:", msg);
		  }
		var foo = JSON.stringify(msg);
		var obj = jQuery.parseJSON(foo);
		// put in a message from the ajax response
		$('#message_area').text(obj.message);
		$('#competition_id').val(obj.competition_id);

		addHolderFormBefore();
		
		if( console && console.log ) {
		    console.log("Message Contains: "+obj.message+" "+obj.sport_name+" "+obj.id);
		  }
		
	}
	).error(function( jqXHR, textStatus, errorThrown) {
			 if( console && console.log ) {
				    console.log("Error "+textStatus+" "+errorThrown);
				    console.log(jqXHR);
				  }
			 $('#message_area').val("Error "+textStatus+" "+errorThrown); 
	});

	return false;
}

function addCompetitionFormBefore()
{
	//competitionCount++;
	if(document.querySelector('#competition_name') != null) {
		console.log(document.querySelector('#competition_name').value);
	}
	var competitionFormStr;
	jQuery.get('competition-form.jsp',function(data){
		  //alert(data);
		  competitionFormStr = data;
		  $('#addCompetitionFormBefore').prepend($(competitionFormStr).fadeIn("slow"));
		  
		  $("#competition_form .select2").select2();

		});

}

function addHolderFormBefore()
{
	//competitionCount++;
	if(document.querySelector('#addHolderFormAfter') != null) {
		console.log(document.querySelector('#competition_name').value);
	}
	var holderFormStr;
	jQuery.get('holder-form.jsp',function(data){
		  //alert(data);
		  holderFormStr = data;
		  $('#addHolderFormAfter').prepend($(holderFormStr).fadeIn("slow"));
		  
		  // wow..you have to "activate" these select elements..this should do it
		  // activate after the form is added..
		  $("#holder_form .select2").select2();
		  

		});

}

function submitCompetition()
{
	
}
function isNumberKey(evt){
    var charCode = (evt.which) ? evt.which : event.keyCode;
    if (charCode > 31 && (charCode < 48 || charCode > 57))
        return false;
    return true;
}

$(function() {
	competitionCount = 0;

	//$document.getElementById('#addCompetitionButton')
	
	$("#save_button").click(function() {
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