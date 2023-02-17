package com.turtleshelldevelopment;

import com.turtleshelldevelopment.utils.EnvironmentType;
import com.turtleshelldevelopment.utils.Issuers;
import com.turtleshelldevelopment.utils.ModelUtil;
import com.turtleshelldevelopment.utils.db.Account;
import com.turtleshelldevelopment.utils.permissions.PermissionType;
import com.turtleshelldevelopment.utils.TokenUtils;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.velocity.VelocityTemplateEngine;

import java.sql.SQLException;

import static spark.Spark.halt;

public class EndpointFilters {

    public static void verifyCredentials(Request req, Response resp, PermissionType requiredEntitlement) {
        TokenUtils tokenUtils = new TokenUtils(req.cookie("token"), Issuers.AUTHENTICATION.getIssuer());
        if(!tokenUtils.isInvalid()) {
            try {
                Account user = Account.getAccountInfo(tokenUtils.getDecodedJWT().getSubject());
                if(user == null) {
                    ModelUtil error = new ModelUtil(req)
                            .addError(401, "Account no longer exists!");
                    halt(401, new VelocityTemplateEngine().render(new ModelAndView(error.build(), "/frontend/error.vm")));
                    return;
                }
                if(user.isDisabled()) {
                    resp.cookie("/", "token", null, 0, true, true);
                    resp.redirect("/");
                    ModelUtil error = new ModelUtil(req)
                            .addError(401, "Account has disabled!");
                    halt(401, new VelocityTemplateEngine().render(new ModelAndView(error.build(), "/frontend/error.vm")));
                    return;
                }
            } catch (SQLException e) {
                ModelUtil error = new ModelUtil(req)
                        .addError(401, "Failed to get User Information");
                halt(401, new VelocityTemplateEngine().render(new ModelAndView(error.build(), "/frontend/error.vm")));
                return;
            }
            if(requiredEntitlement == null) return;
            if(!tokenUtils.getPermissions().get(requiredEntitlement)) {
                ModelUtil error = new ModelUtil(req)
                        .addError(401, "You do not have permission to view this page");
                halt(401, new VelocityTemplateEngine().render(new ModelAndView(error.build(), "/frontend/error.vm")));
            }
        } else {
            //System.out.println("Token is invalid with auth: " + tokenUtils.getErrorReason());
            resp.cookie("/", "token", null, 0, true, true);
            resp.redirect("/");
            ModelUtil error = new ModelUtil(req)
                    .addError(401, tokenUtils.getErrorReason());
            halt(401, new VelocityTemplateEngine().render(new ModelAndView(error.build(), "/frontend/error.vm")));
        }
    }
    public static void verifyCredentials(Request req, Response res) {
        verifyCredentials(req, res, null);
    }
}
