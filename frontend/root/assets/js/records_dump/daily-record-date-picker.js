"use strict";
/*
	Missouri COVID-19 Vaccine Tracker Application 
	Filename: 	daily-record-date-picker.js
	
	Written by Katherine Geist, to set the Daily Record date picker parameters

*/

$(document).ready(function(){
 $('#daily-report-date').datepicker({
  "format": "mm/dd/yyyy",
  "startDate": "-8d",
  "endDate": "-1d",
  "keyboardNavigation": true
 }); 
});

// If you need to extract the date using date picker
myDate = $('#daily-report-date').datepicker('getDate');
