package com.turtleshelldevelopment.pages;

import com.turtleshelldevelopment.utils.ModelUtil;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.template.velocity.VelocityTemplateEngine;

import java.util.Map;

public class EditRecordPage implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {
        Map<String, Object> modelData = new ModelUtil(request).build();
        return new VelocityTemplateEngine().render(new ModelAndView(modelData, "/frontend/edit_record.vm"));
    }
}
