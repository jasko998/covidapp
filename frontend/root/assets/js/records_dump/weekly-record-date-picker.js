"use strict";
/*
	Missouri COVID-19 Vaccine Tracker Application 
	Filename: 	daily-record-date-picker.js
	
	Written by Katherine Geist, to set the Daily Record date picker parameters

*/

$(document).ready(function(){
 $('#datepicker').datepicker({
  "format": "mm/dd/yyyy",
//   "startDate": "-1d",
  "endDate": "-1d",
  "keyboardNavigation": true,
  "clearBtn": true,
  "daysOfWeekDisabled": "0,2,3,4,5,6",
  "daysOfWeekHighlighted": "1"
 }); 
});

// If you need to extract the date using date picker
myDate = $('#datepicker').datepicker('getDate');
