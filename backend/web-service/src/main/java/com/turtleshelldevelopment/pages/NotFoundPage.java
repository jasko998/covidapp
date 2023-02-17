package com.turtleshelldevelopment.pages;

import com.turtleshelldevelopment.utils.ModelUtil;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.template.velocity.VelocityTemplateEngine;

public class NotFoundPage implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {
        ModelUtil model = new ModelUtil(request);
        model.addError(404, "Whatever you're looking for doesn't exist!<br /> <h3>Check the url or notify the Health and Senior Services of this issue.</h3>");
        return new VelocityTemplateEngine().render(new ModelAndView(model.build(), "/frontend/error.vm"));
    }
}
