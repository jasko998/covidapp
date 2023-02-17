package com.turtleshelldevelopment.pages;

import com.turtleshelldevelopment.utils.ModelUtil;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.template.velocity.VelocityTemplateEngine;

import java.util.Map;

public class SiteCreatePage implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {
        Map<String, Object> modelInfo = new ModelUtil(request).addCounties().build();
        return new VelocityTemplateEngine().render(new ModelAndView(modelInfo, "/frontend/create_site.vm"));
    }
}
