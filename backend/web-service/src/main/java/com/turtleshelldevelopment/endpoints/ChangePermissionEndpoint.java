package com.turtleshelldevelopment.endpoints;

import com.turtleshelldevelopment.BackendServer;
import com.turtleshelldevelopment.utils.ResponseUtils;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ChangePermissionEndpoint implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {
        int userId = Integer.parseInt(request.params("id"));
        int permission = Integer.parseInt(request.queryParams("permission"));
        try(Connection conn = BackendServer.database.getDatabase().getConnection();
            PreparedStatement updatePermissions = conn.prepareStatement("UPDATE User SET user_type = ? WHERE user_id = ?")) {
            updatePermissions.setInt(1, permission);
            updatePermissions.setInt(2, userId);
            if(updatePermissions.executeUpdate() == 1) {
                return ResponseUtils.createSuccess("Successfully updated user permission", response);
            } else {
                return ResponseUtils.createError("Failed to update user Permissions", 500, response);
            }
        } catch (SQLException e) {
            return ResponseUtils.createError("Failed to update user Permissions", 500, response);
        }
    }
}
