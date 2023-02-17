package com.turtleshelldevelopment.pages;

import com.turtleshelldevelopment.utils.ModelUtil;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.template.velocity.VelocityTemplateEngine;

public class WeeklyReportPage implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {
        //TODO
        return new VelocityTemplateEngine().render(new ModelAndView(new ModelUtil(request).build(), "/frontend/weekly_report.vm"));
    }
}
