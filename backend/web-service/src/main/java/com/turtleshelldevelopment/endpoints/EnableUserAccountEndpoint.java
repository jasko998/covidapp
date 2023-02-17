package com.turtleshelldevelopment.endpoints;

import com.turtleshelldevelopment.BackendServer;
import com.turtleshelldevelopment.utils.ResponseUtils;
import spark.Request;
import spark.Response;
import spark.Route;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EnableUserAccountEndpoint implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {
        try(Connection conn = BackendServer.database.getDatabase().getConnection();
            PreparedStatement disableUser = conn.prepareStatement("UPDATE User SET allow_login = 1 WHERE user_id = ?;")) {
            disableUser.setInt(1, Integer.parseInt(request.params("id")));
            disableUser.executeUpdate();
            response.redirect("/user/edit");
            return ResponseUtils.createSuccess("Successfully enabled user account", response);
        } catch (SQLException e) {
            return ResponseUtils.createError("Failed to disable user account", 500, response);
        }
    }
}
