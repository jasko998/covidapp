"use strict";

document.getElementById("createUserForm").onsubmit = createUser;

function createUser(evt) {
    evt.preventDefault();
    var xhr = new XMLHttpRequest();
    var values = new FormData(document.getElementById("createUserForm"));
    xhr.open("POST", window.location.origin + "/user/add", true);
    var object = {};
    values.forEach((value, key) => object[key] = value);
    var requestData = JSON.stringify(object);
    
    xhr.onreadystatechange = (request, event) => {
        if (request.target.readyState === XMLHttpRequest.DONE) {
            if(xhr.status === 200) {
                const responseModal = new bootstrap.Modal("#successModal", {
                    backdrop: true, 
                    focus: true,
                    keyboard: false
                });
                var json_data = JSON.parse(request.target.responseText);
                document.getElementById("success-modal-label").innerHTML = "Success!";
                document.getElementById("success-body").innerHTML = "Username: " + values.get("username") + "<br>Password: " + values.get("password");
                document.getElementById("success-mail").style.display = "block";
                document.getElementById("success-mail").onclick = () => {
                    window.open("mailto:" + encodeURIComponent("?subject=Your IIS Login Credentials&body=Here is your IIS Login:\nUsername: " + values.get("username") + "\nPassword: " + values.get("password") + "\n You will get a 2 factor authentication code upon first login.", "_blank"));
                };
                responseModal.show();
            } else if(xhr.status === 400) {
                const responseModal = new bootstrap.Modal("#successModal", {
                    backdrop: true, 
                    focus: true,
                    keyboard: false
                });
                var json_data = JSON.parse(request.target.responseText);
                document.getElementById("success-modal-label").innerHTML = "Error";
                document.getElementById("success-body").innerHTML = json_data["message"];
                document.getElementById("success-mail").style.display = "none";
                responseModal.show();
                console.log("error");
            }
        }
    }
    
    xhr.send(requestData);
}