"use strict";

function displayOnMap() {
  if (
    document.getElementById("address").value === "" ||
    document.getElementById("city").value === "" ||
    document.getElementById("zip").value === ""
  )
    return;
  fetch(
    `${window.location.origin}/api/lookupAddress?address=${
      document.getElementById("address").value
    }&city=${document.getElementById("city").value}&zip=${
      document.getElementById("zip").value
    }`
  ).then(async (resp) => {
    const info = await resp.json();
    console.log(info);
    if (
      info.hasOwnProperty("result") &&
      info["result"]["addressMatches"].length > 0
    ) {
      const markerLoc = new ol.geom.Point(
          proj4("EPSG:4326", "EPSG:3857", [
            info["result"]["addressMatches"][0]["coordinates"]["x"],
            info["result"]["addressMatches"][0]["coordinates"]["y"],
          ]),
          "EPSG:3857"
      );
      console.log(markerLoc);
      const marker = new ol.Feature(markerLoc);
      console.log(marker);
      markers.getSource().clear();
      markers.getSource().addFeature(marker);
      map.getView().fit(markerLoc.extent_, map.getSize());
      map.getView().setZoom(17);
    }
  });
}

document.getElementById("address").onblur = displayOnMap;
document.getElementById("city").onblur = displayOnMap;
document.getElementById("zip").onblur = displayOnMap;

document.getElementById("county").onchange = function (ele) {
  counties.forEach((county) => {
    if (ele.currentTarget.value === county["name"]) {
      document.getElementById("fips").value = county["fips"];

    }
  });
};

const isNumericInput = (event) => {
  const key = event.keyCode;
  return (
    (key >= 48 && key <= 57) || // Allow number line
    (key >= 96 && key <= 105) // Allow number pad
  );
};

const isModifierKey = (event) => {
  const key = event.keyCode;
  return (
    event.shiftKey === true ||
    key === 35 ||
    key === 36 || // Allow Shift, Home, End
    key === 8 ||
    key === 9 ||
    key === 13 ||
    key === 46 || // Allow Backspace, Tab, Enter, Delete
    (key > 36 && key < 41) || // Allow left, up, right, down
    // Allow Ctrl/Command + A,C,V,X,Z
    ((event.ctrlKey === true || event.metaKey === true) &&
      (key === 65 || key === 67 || key === 86 || key === 88 || key === 90))
  );
};

const enforceFormat = (event) => {
  // Input must be of a valid number format or a modifier key, and not longer than ten digits
  if (!isNumericInput(event) && !isModifierKey(event)) {
    event.preventDefault();
  }
};

const formatToPhone = (event) => {
  if (isModifierKey(event)) {
    return;
  }

  const input = event.target.value.replace(/\D/g, "").substring(0, 10); // First ten digits of input only
  const areaCode = input.substring(0, 3);
  const middle = input.substring(3, 6);
  const last = input.substring(6, 10);

  if (input.length > 6) {
    event.target.value = `(${areaCode}) ${middle} - ${last}`;
  } else if (input.length > 3) {
    event.target.value = `(${areaCode}) ${middle}`;
  } else if (input.length > 0) {
    event.target.value = `(${areaCode}`;
  }
};

const inputElement = document.getElementById("phoneNumber");
inputElement.addEventListener("keydown", enforceFormat);
inputElement.addEventListener("keyup", formatToPhone);

document.getElementById("newSite").onsubmit = function (event) {
  event.preventDefault();
  //Handle Address Validation
  const address = document.getElementById("address");
  const city = document.getElementById("city");
  const zipCode = document.getElementById("zip");
  const county = document.getElementById("county");
  const fips = document.getElementById("fips");
  const phone = document.getElementById("phone");

  fetch(`
    ${window.location.origin}/site/create/?
    address=${address}&
    city=${city}&
    zip=${zipCode}&
    county=${county}&
    fips=${fips}&
    phone=${phone}`).then(async () => {
      
    });
};
