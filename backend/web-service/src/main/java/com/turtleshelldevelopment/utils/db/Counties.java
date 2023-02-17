package com.turtleshelldevelopment.utils.db;

import com.turtleshelldevelopment.BackendServer;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public record Counties(String name, int id) {
        public static List<Counties> getCounties() {
            List<Counties> counties = new ArrayList<>();
            try (Connection databaseConnection = BackendServer.database.getDatabase().getConnection(); CallableStatement countiesCall = databaseConnection.prepareCall("CALL GET_COUNTIES()")) {
                ResultSet set = countiesCall.executeQuery();
                while (set.next()) {
                    counties.add(new Counties(set.getString("county_name"), set.getInt("fip_id")));
                }
                set.close();
            } catch (SQLException e) {
                BackendServer.serverLogger.error("Error getting counties: {}", e.getMessage());
                return null;
            }
            return counties;
        }

        public String getName() {
            return name;
        }

        public int getFIPS() {
            return id;
        }
}
