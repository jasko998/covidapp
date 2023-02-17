package com.turtleshelldevelopment.pages;

import com.turtleshelldevelopment.utils.ModelUtil;
import com.turtleshelldevelopment.utils.db.Patient;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.template.velocity.VelocityTemplateEngine;

public class AddContactPage implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {
        ModelUtil modelData = new ModelUtil(request);
        String id = request.params("id");
        modelData.add("patient", Patient.getPatient(id));
        return new VelocityTemplateEngine().render(new ModelAndView(modelData.build(), "/frontend/add_contact.vm"));
    }
}
