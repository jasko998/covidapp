"use strict";
/*
	Missouri COVID-19 Vaccine Tracker Application 
	Filename: 	print_VIS.js
	
	Written by Katherine Geist, to print the appropriate Vaccine Info Sheet

*/

const dose = document.querySelector("#dose");
const printButton = document.querySelector(".print-button");
const vaxMan = document.getElementsByName("vaxMan");
const lang = document.getElementsByName("lang");

function getFromRadio(radios) {
  let val = "english";
  // 	Get the manufacturer selected from the radio buttons
  let length = radios.length;
  for (let i = 0; i < length; i++) {
    if (radios[i].checked) {
      // 	Set the manufacturer flag
      val = radios[i].value;
      // Only one radio can be logically checked, so don't check the rest
      break;
    }
  }
  return val;
}

function getOptions(event) {
  event.preventDefault();
  // 	Get the vaccine dose selected from the dropdown
  const vax = dose.options[dose.selectedIndex].value;
  // 		document.querySelector('.vax').textContent = vax;

  const manu = getFromRadio(vaxMan);
  // 		document.querySelector('.manu').textContent = manu;

  const language = getFromRadio(lang);
  // 		document.querySelector('.language').textContent = language;

  // 	Pass to the fx where we actually open the VIS based on specs provided
  openVIS(language, vax, manu);
}

function openVIS(lang, vax, manu) {
  let url = "";
  switch (manu) {
    case "novavax":
      if (vax === "adDose1" || vax === "teenDose1") {
        if (lang === "english")
          url = "https://www.fda.gov/media/159898/download";
        else if (lang === "spanish")
          url = "https://www.fda.gov/media/160242/download";
      }
      break;
    case "janssen":
      if (vax === "adDose1" || vax === "adDose2" || "adBoosterBiv") {
        if (lang === "english")
          url =
            "https://www.janssenlabels.com/emergency-use-authorization/Janssen+COVID-19+Vaccine-Recipient-fact-sheet.pdf";
        else if (lang === "spanish")
          url =
            "https://www.janssenlabels.com/emergency-use-authorization/Janssen+COVID-19+Vaccine-Recipient-fact-sheet-es.pdf";
      }
      break;
    case "pfizer":
      if (
        /adDose\d/.test(vax) ||
        /adBooster\d/.test(vax) ||
        vax === "adBoosterBiv" ||
        /teenDose\d/.test(vax) ||
        /teenBooster\d/.test(vax) ||
        vax === "teenBoosterBiv"
      ) {
        if (lang === "english")
          url =
            "https://labeling.pfizer.com/ShowLabeling.aspx?id=14472&format=pdf";
        else if (lang === "spanish")
          url = "https://webfiles.pfizer.com/spanisheuarecipientfs12yrsandup";
      } else if (
        vax === "chDose1" ||
        vax === "chDose2" ||
        vax === "chBooster1" ||
        vax === "chBoosterBiv"
      ) {
        if (lang === "english")
          url =
            "https://labeling.pfizer.com/ShowLabeling.aspx?id=16074&format=pdf";
        else if (lang === "spanish")
          url =
            "https://webfiles.pfizer.com/spanisheuarecipientfs5through11years";
      } else if (
        vax === "infDose1" ||
        vax === "infDose2" ||
        vax === "infBooster1"
      ) {
        if (lang === "english")
          url =
            "https://labeling.pfizer.com/ShowLabeling.aspx?id=17228&format=pdf";
        else if (lang === "spanish")
          url = "https://webfiles.pfizer.com/spanisheuarecipientfs6mo4yr";
      }
      break;
    case "moderna":
      if (
        /adDose\d/.test(vax) ||
        /adBooster\d/.test(vax) ||
        vax === "adBoosterBiv" ||
        /teenDose\d/.test(vax) ||
        /teenBooster\d/.test(vax) ||
        vax === "teenBoosterBiv"
      ) {
        if (lang === "english")
          url =
            "https://eua.modernatx.com/covid19vaccine-eua/bivalent-dose-recipient.pdf";
        else if (lang === "spanish")
          url =
            "https://eua.modernatx.com/covid19vaccine-eua/EUAFactSheet-Recipients-Prime-Bivalent-Boost-12y+-Highlighted8.31.22_es-ES_Clean.pdf";
      } else if (
        vax === "chDose1" ||
        vax === "chDose2" ||
        vax === "chBooster1" ||
        vax === "chBoosterBiv"
      ) {
        if (lang === "english")
          url =
            "https://eua.modernatx.com/covid19vaccine-eua/6-11y-facts-recipient.pdf";
        else if (lang === "spanish")
          url =
            "https://eua.modernatx.com/covid19vaccine-eua/6-11y-facts-recipient_es-ES.pdf";
      } else if (
        vax === "infDose1" ||
        vax === "infDose2" ||
        vax === "infBooster1"
      ) {
        if (lang === "english")
          url =
            "https://eua.modernatx.com/covid19vaccine-eua/6m-5y-facts-recipient.pdf";
        else if (lang === "spanish")
          url =
            "https://eua.modernatx.com/covid19vaccine-eua/6m-5y-facts-recipient_es-ES.pdf";
      }
      break;
  }
  // 	And then we don't want to open an empty webpage:
  if (url === "") {
    window.alert(
      "This vaccine dose is not available for this manufacturer and/or age group."
    );
    return;
  }
  window.open(url);
}
printButton.addEventListener("click", getOptions);
