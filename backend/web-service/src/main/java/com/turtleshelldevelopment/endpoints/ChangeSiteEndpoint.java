package com.turtleshelldevelopment.endpoints;

import com.turtleshelldevelopment.BackendServer;
import com.turtleshelldevelopment.utils.ResponseUtils;
import spark.Request;
import spark.Response;
import spark.Route;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ChangeSiteEndpoint implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {
        int userId = Integer.parseInt(request.params("id"));
        int siteId = Integer.parseInt(request.queryParams("siteId"));
        try(Connection conn = BackendServer.database.getDatabase().getConnection();
            PreparedStatement updatePermissions = conn.prepareStatement("UPDATE User SET site_id = ? WHERE user_id = ?")) {
            updatePermissions.setInt(1, siteId);
            updatePermissions.setInt(2, userId);
            if(updatePermissions.executeUpdate() == 1) {
                return ResponseUtils.createSuccess("Successfully updated User Site", response);
            } else {
                return ResponseUtils.createError("Failed to update User Site", 500, response);
            }
        } catch (SQLException e) {
            return ResponseUtils.createError("Failed to update User Site", 500, response);
        }
    }
}
