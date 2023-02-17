"use strict";
/*
	Missouri COVID-19 Vaccine Tracker Application 
	Filename: 	popup_24a.js
	
	Written by Justin Macapanan

*/

const submitButton = document.getElementById("login");
const exit = document.getElementById("exit");
var tfaElements = document.getElementsByClassName("twofainput");
for(var elem = 0; elem < tfaElements.length; elem++ ) {
  tfaElements[elem].addEventListener("keyup", event => handleMFAErase(event));
}

exit.onclick = function () {
  document.getElementById("pop-up").style.display = "none";
  document.getElementById("pop-up-content").style.display = "none";
  document.getElementById("messages").innerText = "";
  $(".modal-backdrop").remove();
  document.getElementById("pop-up-content").classList.remove("remove-show");
};

function handle2FAInput(event) {
  event.value = event.value.replace(/[^0-9.]/g, "").replace(/(\..*)\./g, "$1");
  if (Number(event.value) != NaN && event.value != "") {
    if (event.nextElementSibling != null) {
      event.nextElementSibling.focus();
    }
  } else if (event.value == "") {
    if (event.previousElementSibling != null) {
      event.previousElementSibling.focus();
    }
  } else {
    if (event.previousElementSibling != null) {
      event.previousElementSibling.focus();
    }
  }
}

function handleMFAErase(event, elem) {
  console.log("Code: " + event.code + " or " + event.key);
  if(event.key === "Backspace") {
    console.log("backspace");
    if(event.target.value == "") {
      if (event.target.previousElementSibling != null) {
        event.target.previousElementSibling.value = "";
        event.target.previousElementSibling.focus();
      }
    }
  }
}
