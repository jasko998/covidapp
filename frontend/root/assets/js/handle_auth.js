const JWT = document.cookie.split("token=")[1]
var expiryTime = jwtDecode(JWT)["payload"]["exp"];
setTimeout(() => {
    var expire = setTimeout(() => {
        alert("Token Expired");
        window.location.replace(window.location.origin + "/");
    }, (expiryTime*1000 - Date.now()));
    var continueSession = confirm("Your Login will expire in 2 minutes, Would you like continue your session?");
    if(continueSession == true) {
        //Refresh token call
        $.get(window.location.origin + "/user/refresh", {}, (data) => {
            if(data != null) {
                if(!data.success) {
                    alert("Could not refresh your token please login again.");
                    window.location.replace(window.location.origin + "/");
                    return;
                }
            }
            clearTimeout(expire);
        });
    } else {
        //Logout
        window.location.replace(window.location.origin + "/user/logout");
    }
}, (expiryTime*1000 - Date.now()) - 120000);

function jwtDecode(t) {
    let token = {};
    token.raw = t;
    token.header = JSON.parse(window.atob(t.split('.')[0]));
    token.payload = JSON.parse(window.atob(t.split('.')[1]));
    return (token)
}