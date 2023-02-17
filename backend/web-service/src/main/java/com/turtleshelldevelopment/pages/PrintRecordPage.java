package com.turtleshelldevelopment.pages;

import com.turtleshelldevelopment.utils.ModelUtil;
import com.turtleshelldevelopment.utils.db.Patient;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.template.velocity.VelocityTemplateEngine;

public class PrintRecordPage implements Route {
    @Override
    public Object handle(Request request, Response response) {
        String id = request.params("id");
        ModelUtil model = new ModelUtil(request).add("patient", Patient.getPatient(id));

        return new VelocityTemplateEngine().render(new ModelAndView(model.build(), "/frontend/print_record.vm"));
    }
}
