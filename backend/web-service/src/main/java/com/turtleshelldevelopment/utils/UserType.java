package com.turtleshelldevelopment.utils;

import com.turtleshelldevelopment.BackendServer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public record UserType(String name, int id) {

    public static UserType isValidType(int id) {
        try(Connection databaseConnection = BackendServer.database.getDatabase().getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("SELECT type_name, user_type_id FROM UserType WHERE user_type_id = ? LIMIT 1;");
        ) {
            preparedStatement.setInt(1, id);
            ResultSet rs;
            if((rs = preparedStatement.executeQuery()).next()) {
                UserType type = new UserType(rs.getString("type_name"), rs.getInt("user_type_id"));
                rs.close();
                return type;
            }
            rs.close();
            preparedStatement.close();
            return null;
            
        } catch (SQLException e) {
            BackendServer.serverLogger.error("Failed to get types from database: {}", e.getMessage());
            return null;
        }
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}
