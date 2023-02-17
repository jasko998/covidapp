package com.turtleshelldevelopment.endpoints;

import com.turtleshelldevelopment.JWTAuthentication;
import com.turtleshelldevelopment.utils.Issuers;
import com.turtleshelldevelopment.utils.ResponseUtils;
import com.turtleshelldevelopment.utils.TokenUtils;
import com.turtleshelldevelopment.utils.permissions.Permissions;
import spark.Request;
import spark.Response;
import spark.Route;

public class RefreshTokenEndpoint implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {
        TokenUtils utils = new TokenUtils(request.cookie("token"), Issuers.AUTHENTICATION.getIssuer());
        if(!utils.isInvalid()) {
            JWTAuthentication.generateAuthToken(utils.getDecodedJWT().getSubject(), new Permissions(utils.getDecodedJWT().getSubject()).getPermissionsAsString(), response);
        } else {
            response.redirect("/");
            return ResponseUtils.createError("Invalid Token", 401, response);
        }
        return ResponseUtils.createSuccess("Success", response);
    }
}
