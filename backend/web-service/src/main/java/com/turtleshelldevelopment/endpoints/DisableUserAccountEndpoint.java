package com.turtleshelldevelopment.endpoints;

import com.turtleshelldevelopment.BackendServer;
import com.turtleshelldevelopment.utils.ResponseUtils;
import com.turtleshelldevelopment.utils.TokenUtils;
import spark.Request;
import spark.Response;
import spark.Route;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DisableUserAccountEndpoint implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {
        TokenUtils token = new TokenUtils(request.cookie("token"), null);
        try(Connection conn = BackendServer.database.getDatabase().getConnection();
            PreparedStatement disableUser = conn.prepareStatement("UPDATE User SET allow_login = 0 WHERE user_id = ?;")) {
            int requestedUser = Integer.parseInt(request.params("id"));
            if(token.getUserId() == requestedUser) {
                return ResponseUtils.createError("You can't disable your own account!", 400, response);
            } else {
                disableUser.setInt(1, Integer.parseInt(request.params("id")));
                disableUser.executeUpdate();
            }
            response.redirect("/user/edit");
            return ResponseUtils.createSuccess("Successfully disabled user account", response);
        } catch (SQLException e) {
            return ResponseUtils.createError("Failed to disable user account", 500, response);
        }
    }
}
