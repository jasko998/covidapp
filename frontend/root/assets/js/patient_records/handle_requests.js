function handleCall(elem) {
    $.post(elem.action, $(elem).serialize(), (data) => {
        if(data.success) {
            console.log("success");
            document.getElementById("response-name").innerHTML = "<b style=\"color:green;\">Add Patient</b>";
            document.getElementById("response").innerText = data.message;
            if($(elem) != null && $(elem).attr("data-redirect") != null) {
                window.location.replace(window.location.origin + $(elem).attr("data-redirect"));
            }
            $("#main-toast").toast("show");
            return;
        } else {
            console.log("success failed");
            document.getElementById("response-name").innerHTML = "<b style=\"color:green;\">Add Patient</b>";
            document.getElementById("response").innerText = data.message;
            $("#main-toast").toast("show");
            return;
        }
    }).fail((xhr, textStatus, error) => {
        console.log("success failed");
        document.getElementById("response-name").innerHTML = "<b style=\"color:green;\">Add Patient</b>";
        document.getElementById("response").innerText = JSON.parse(xhr.responseText).message;
        $("#main-toast").toast("show");
        return;
    });
}

$(() => {
    for(const backgroundCall of document.getElementsByClassName("background-call")) {
        backgroundCall.addEventListener("submit", (event) => {
            event.preventDefault();
            handleCall(backgroundCall);
            return false;
        });
    }
});