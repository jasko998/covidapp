package com.turtleshelldevelopment.pages;

import com.turtleshelldevelopment.utils.Issuers;
import com.turtleshelldevelopment.utils.ModelUtil;
import com.turtleshelldevelopment.utils.ResponseUtils;
import com.turtleshelldevelopment.utils.TokenUtils;
import com.turtleshelldevelopment.utils.db.Patient;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.template.velocity.VelocityTemplateEngine;

import java.util.Map;

public class ViewPatientPage implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {
        TokenUtils tokenVerifier = new TokenUtils(request.cookie("token"), Issuers.AUTHENTICATION.getIssuer());
        if(!tokenVerifier.isInvalid()) {
            Map<String, Object> modelData = new ModelUtil(request).addPermissions(tokenVerifier.getPermissions()).build();
            String id = request.params("id");
            modelData.put("patient", Patient.getPatient(id));
            return new VelocityTemplateEngine().render(new ModelAndView(modelData, "/frontend/view.vm"));
        } else {
            System.out.println("Token is invalid: " + tokenVerifier.getErrorReason());
            response.redirect("/");
            return ResponseUtils.createError("Invalid Token", 401, response);
        }
    }
}
