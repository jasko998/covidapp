package com.turtleshelldevelopment.pages;

import com.turtleshelldevelopment.BackendServer;
import com.turtleshelldevelopment.utils.*;
import com.turtleshelldevelopment.utils.db.Account;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.template.velocity.VelocityTemplateEngine;

import java.sql.SQLException;
import java.util.Map;

public class DashboardPage implements Route {
    @Override
    public Object handle(Request request, Response response) {
        //Frontend Development test code
        if(BackendServer.environment.equals(EnvironmentType.FRONT_DEVEL)) {
            Map<String, Object> modelData = new ModelUtil(request).addPermissions(new DevelopmentTokenUtils().getPermissions()).build();
            return new VelocityTemplateEngine().render(new ModelAndView(modelData, "/frontend/dashboard.vm"));
        }
        TokenUtils tokenVerifier = new TokenUtils(request.cookie("token"), Issuers.AUTHENTICATION.getIssuer());
        if(!tokenVerifier.isInvalid()) {
            boolean onboard = false;
            try {
                Account account = Account.getAccountInfo(tokenVerifier.getDecodedJWT().getSubject());
                onboard = account.isOnboarding();
            } catch (SQLException e) {
                BackendServer.serverLogger.error("Could not get account information from database");
            }
            Map<String, Object> modelData = new ModelUtil(request).addPermissions(tokenVerifier.getPermissions()).build();
            modelData.put("show_onboard", onboard);
            return new VelocityTemplateEngine().render(new ModelAndView(modelData, "/frontend/dashboard.vm"));
        } else {
            System.out.println("Token is invalid: " + tokenVerifier.getErrorReason());
            response.cookie("/", "token", null, 0, true, true);
            response.redirect("/");
        }
        return "";
    }
}
