package com.turtleshelldevelopment.pages;

import com.turtleshelldevelopment.BackendServer;
import com.turtleshelldevelopment.utils.Issuers;
import com.turtleshelldevelopment.utils.ModelUtil;
import com.turtleshelldevelopment.utils.TokenUtils;
import com.turtleshelldevelopment.utils.UserType;
import com.turtleshelldevelopment.utils.db.Account;
import com.turtleshelldevelopment.utils.db.Sites;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.template.velocity.VelocityTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserEditPage implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {
        int siteId;

        TokenUtils tokenUtils = new TokenUtils(request.cookie("token"), Issuers.AUTHENTICATION.getIssuer());
        ModelUtil model = new ModelUtil(request);
        try {
            siteId = Integer.parseInt(request.queryParams("site"));
        } catch (NumberFormatException e) {
            siteId = tokenUtils.getSiteId();
        }
        model.add("current_site", siteId);
        try(Connection conn = BackendServer.database.getDatabase().getConnection();
            PreparedStatement getUsers = conn.prepareStatement("SELECT username FROM User WHERE site_id = ?;");
            CallableStatement sites = conn.prepareCall("CALL GET_SITES()");
            CallableStatement types = conn.prepareCall("CALL GET_TYPES()")) {
            ArrayList<Account> userList = new ArrayList<>();
            getUsers.setInt(1, siteId);
            ResultSet set = getUsers.executeQuery();
            while(set.next()) {
                userList.add(Account.getAccountInfo(set.getString("username")));
            }
            model.add("users", userList);
            set.close();
            ArrayList<Sites> sitesArrayList = new ArrayList<>();
            ResultSet sitesList = sites.executeQuery();
            while(sitesList.next()) {
                sitesArrayList.add(new Sites(sitesList.getString("name"), sitesList.getInt("site_id")));
            }
            model.add("sites", sitesArrayList);
            sitesList.close();
            List<UserType> typesList = new ArrayList<>();
            ResultSet typeSet = types.executeQuery();
            while (typeSet.next()) {
                typesList.add(new UserType(typeSet.getString("type_name"), typeSet.getInt("user_type_id")));
            }
            typeSet.close();
            model.add("permissions", typesList);
        } catch (SQLException e) {
            model.addError(500, "Failed to get users");
            e.printStackTrace();
            return new VelocityTemplateEngine().render(new ModelAndView(model.build(), "/frontend/error.vm"));
        }
        return new VelocityTemplateEngine().render(new ModelAndView(model.build() ,"/frontend/edit_user.vm"));
    }
}
