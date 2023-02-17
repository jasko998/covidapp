package com.turtleshelldevelopment.pages;

import com.turtleshelldevelopment.BackendServer;
import spark.Request;
import spark.Response;
import spark.Route;

import java.sql.*;

public class PrintInfoPage implements Route {

    @Override
    public Object handle(Request request, Response response) throws SQLException {
        try(Connection databaseConnection = BackendServer.database.getDatabase().getConnection(); PreparedStatement patientSearch =databaseConnection.prepareCall("CALL SEARCH_PATIENTS(?, ?, ?)")) {
            patientSearch.setString(1, request.queryParams("ss4"));
            patientSearch.setString(2, request.queryParams("lname"));
            patientSearch.setDate(3, Date.valueOf(request.queryParams("dob")));
            ResultSet set = patientSearch.executeQuery();

        } catch (SQLException e) {

        }

        return "";
    }
}
