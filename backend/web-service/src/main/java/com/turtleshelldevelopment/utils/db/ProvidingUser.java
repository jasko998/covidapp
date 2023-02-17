package com.turtleshelldevelopment.utils.db;

import com.turtleshelldevelopment.BackendServer;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public record ProvidingUser(String firstName, String lastName, String email) {
    public static ProvidingUser getProvidingUser(int id) {
        try (Connection databaseConnection = BackendServer.database.getDatabase().getConnection(); CallableStatement getInsurances = databaseConnection.prepareCall("CALL GET_PROVIDING_USER(?)");) {
            getInsurances.setInt(1, id);
            ResultSet res = getInsurances.executeQuery();
            if(res.next()) {
                ProvidingUser user = new ProvidingUser(res.getString("first_name"), res.getString("last_name"), res.getString("email"));
                getInsurances.close();
                return user;
            } else {
                getInsurances.close();
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
