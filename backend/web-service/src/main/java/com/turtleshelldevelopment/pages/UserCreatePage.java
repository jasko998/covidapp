package com.turtleshelldevelopment.pages;

import com.turtleshelldevelopment.utils.db.Sites;
import com.turtleshelldevelopment.utils.UserType;
import com.turtleshelldevelopment.BackendServer;
import com.turtleshelldevelopment.utils.ModelUtil;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.template.velocity.VelocityTemplateEngine;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserCreatePage implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {
        try(Connection databaseConnection = BackendServer.database.getDatabase().getConnection();
            CallableStatement sites = databaseConnection.prepareCall("CALL GET_SITES()");
            CallableStatement types = databaseConnection.prepareCall("CALL GET_TYPES()")
            ) {
            List<Sites> sitesList = new ArrayList<>();
            ResultSet set = sites.executeQuery();
            while (set.next()) {
                sitesList.add(new Sites(set.getString("name"), set.getInt("site_id")));
            }
            set.close();
            sites.close();
            List<UserType> typesList = new ArrayList<>();
            ResultSet typeSet = types.executeQuery();
            while (typeSet.next()) {
                typesList.add(new UserType(typeSet.getString("type_name"), typeSet.getInt("user_type_id")));
            }
            typeSet.close();
            types.close();
            return new VelocityTemplateEngine().render(new ModelAndView(new ModelUtil(request).add("sites", sitesList).add("types", typesList).build(), "/frontend/add_user.vm"));
        } catch (SQLException e) {
            e.printStackTrace();
            return new VelocityTemplateEngine().render(new ModelAndView(new ModelUtil(request).addError(500, "Failed to load data"), "error.vm"));
        }
    }
}
