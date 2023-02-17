package com.turtleshelldevelopment.endpoints;

import spark.Request;
import spark.Response;
import spark.Route;

public class LogoutEndpoint implements Route {
    @Override
    public Object handle(Request request, Response response) {
        response.removeCookie("/", "token");
        response.redirect("/");
        return "";
    }
}
