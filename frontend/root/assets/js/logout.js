"use strict";
/*
	Missouri COVID-19 Vaccine Tracker Application 
	Filename: 	logout.js
	
	Written by Jasmin Milicevic, put into separate file by KSG & updated to work with dashboards

*/

// Have to add multiple logout variables because multiple instances in dashboard HTML
const logout = document.querySelector(".logout");

function gohome() {
	window.location.replace(window.location.origin + "/user/logout");
} 
logout.onclick = gohome;