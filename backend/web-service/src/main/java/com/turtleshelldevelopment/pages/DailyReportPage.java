package com.turtleshelldevelopment.pages;

import com.turtleshelldevelopment.utils.ModelUtil;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.template.velocity.VelocityTemplateEngine;

public class DailyReportPage implements Route {

    @Override
    public Object handle(Request request, Response response) throws Exception {

        return new VelocityTemplateEngine().render(new ModelAndView(new ModelUtil(request).build(), "/frontend/daily_report.vm"));
    }
}
