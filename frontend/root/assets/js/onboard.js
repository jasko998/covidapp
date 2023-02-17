"use strict";
$(window).on("load", () => {
  console.log("Starting onboarding...");
  var start = new bootstrap.Modal("#onboard-start");
  document.getElementById("onboard-continue").onclick = continueOnboarding;
  start.show();
});

function continueOnboarding(evt) {
  //call generate MFA
  var request = new XMLHttpRequest();
  request.open("POST", window.location.origin + "/user/mfa/generate", true);
  request.onreadystatechange = (request, event) => {
    if (request.target.readyState === XMLHttpRequest.DONE) {
      if (request.target.status === 200) {
        var json_data = JSON.parse(request.target.responseText);
        var qrCode = new Image();
        qrCode.src = json_data["mfaQR"];
        document.getElementById("onboard-body").innerHTML =
          "<img style=\"width: 100%;text-align:center;\" src=\"" + qrCode.src +
          "\" alt=\"QR Code for MFA\"><br>Manual Code: " +
          json_data["mfaCode"] +
          '<br>Please input the code you are given below<br><input type="text" class="form-control" id="mfa-code" style=\"text-align:center;\" max-length=\"6\" pattern=\"\^d{6}$\">';
        document.getElementById("onboard-continue").innerText = "Validate";
        document.getElementById("onboard-continue").attributes["id"].value =
          "modal-validate";
        document.getElementById("modal-validate").removeAttribute("onclick");
        document.getElementById("modal-validate").onclick = () => {
          var requestValidate = new XMLHttpRequest();
          requestValidate.open(
            "POST",
            window.location.origin + "/user/mfa/validate",
            true
          );
          requestValidate.onreadystatechange = (request, event) => {
            //TODO add in validation check and continue
            // Also should we add new password as well?
            if(requestValidate.readyState === XMLHttpRequest.DONE) {
                if(requestValidate.status === 200) {
                    var json_data = JSON.parse(request.target.responseText);
                    document.getElementById("modal-validate").innerHTML = "Close";
                    document.getElementById("modal-validate").setAttribute("data-bs-dismiss", "modal");
                    document.getElementById("onboard-body").innerHTML = json_data["message"];
                } else {
                    var json_data = JSON.parse(request.target.responseText);
                    var toast = new bootstrap.Toast("#error-toast");
                    document.getElementById("error-message").innerHTML = json_data["message"];
                    toast.show();
                }
            }
          };
          var validateBody = {
            "mfaCode": document.getElementById("mfa-code").value
          };
          requestValidate.send(JSON.stringify(validateBody));
        };
      } else if (request.target.status === 400) {
        var json_data = JSON.parse(request.target.responseText);
        document.getElementById("modal-body").innerHTML =
          "Error Generating Code: " + json_data["message"];
        document.getElementById("onboard-continue").innerText = "Retry";
        document.getElementById("onboard-continue").attributes["id"].value =
          "modal-validate";
      }
    }
  };

  request.send();

  //TODO then we need to handle a submit button on the modal and validate on server side and if valid save to user and set validated to true in database
}
