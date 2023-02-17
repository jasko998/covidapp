 "use strict";
/*
	Missouri COVID-19 Vaccine Tracker Application 
	Filename: 	radio_button_error_flag.js
	
	Written by Katherine Geist, to flag if a radio button hasn't been selected; modified from Geeks 4 Geeks: https://www.geeksforgeeks.org/how-to-display-validation-message-for-radio-buttons-with-inline-images-using-bootstrap-4/

*/

// On clicking submit do following
$("input[type=submit]").click(function() {
	 
	var atLeastOneChecked = false;
	$("input[type=radio]").each(function() {
	 
		// If radio button not checke then display the alert message
		if ($(this).attr("checked") != "checked") {
			$("#msg").html("<span class='alert alert-danger' id='error'>" + "Please Choose atleast one</span>");
		}
	});
});

submitButton.addEventListener("click", getOptions);
