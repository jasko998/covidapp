package com.turtleshelldevelopment.pages;

import com.turtleshelldevelopment.utils.ModelUtil;
import com.turtleshelldevelopment.utils.db.Patient;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.template.velocity.VelocityTemplateEngine;

import java.util.Map;

public class NewDosePage implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {
        Map<String, Object> modelMap = new ModelUtil(request).build();
        String id = request.params("id");

        modelMap.put("patient", Patient.getPatient(id));
        return new VelocityTemplateEngine().render(new ModelAndView(modelMap, "/frontend/next_dose_record.vm"));
    }
}
