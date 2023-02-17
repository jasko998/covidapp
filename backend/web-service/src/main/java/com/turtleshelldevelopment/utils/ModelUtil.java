package com.turtleshelldevelopment.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.turtleshelldevelopment.BackendServer;
import com.turtleshelldevelopment.JWTAuthentication;
import com.turtleshelldevelopment.utils.db.Counties;
import com.turtleshelldevelopment.utils.permissions.PermissionType;
import org.json.JSONObject;
import spark.ModelAndView;
import spark.Request;
import spark.template.velocity.VelocityTemplateEngine;
import spark.utils.IOUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ModelUtil {


    private final JSONObject modelData = new JSONObject();

    public ModelUtil(Request request) {
        JSONObject headerInfo = new JSONObject();
        if(request.cookie("token") != null) {
            TokenUtils token = new TokenUtils(request.cookie("token"), Issuers.AUTHENTICATION.getIssuer());
            if(!token.isInvalid()) {
                headerInfo.put("logged_in", true);
                headerInfo.put("user", token.getDecodedJWT().getSubject());
                modelData.put("username", token.getDecodedJWT().getSubject().substring(0,1).toUpperCase() + token.getDecodedJWT().getSubject().substring(1));
            } else {
                headerInfo.put("logged_in", false);
            }
        }
        modelData.put("header", new VelocityTemplateEngine().render(new ModelAndView(headerInfo.toMap(), "/frontend/header.vm")));
        modelData.put("global_head", new VelocityTemplateEngine().render(new ModelAndView(Map.of(), "/frontend/global_head.vm")));
    }

    public ModelUtil addPermissions(HashMap<PermissionType, Boolean> perms) {
        if(perms == null) return this;
        for(Map.Entry<PermissionType, Boolean> permission : perms.entrySet()) {
            modelData.put(permission.getKey().getVal().toLowerCase(), permission.getValue());
        }
        return this;
    }
    public ModelUtil addCounties() {
        modelData.put("counties", Counties.getCounties());
        return this;
    }
    public ModelUtil add(String key, Object value) {
        modelData.put(key, value);
        return this;
    }

    public ModelUtil addError(int errorCode, String errorMessage) {
        modelData.put("error_code", errorCode);
        modelData.put("error_message", errorMessage);
        return this;
    }

    public ModelUtil addMFAError(boolean success, String message, boolean shouldRetry) {
        modelData.put("success", success);
        modelData.put("message", message);
        modelData.put("retry", shouldRetry);
        return this;
    }

    public Map<String, Object> build() {
        return this.modelData.toMap();
    }

    public String toJSONString() {
        return this.modelData.toString();
    }
}
