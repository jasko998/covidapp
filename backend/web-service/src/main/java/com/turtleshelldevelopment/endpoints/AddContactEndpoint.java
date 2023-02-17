package com.turtleshelldevelopment.endpoints;

import com.turtleshelldevelopment.BackendServer;
import com.turtleshelldevelopment.utils.ResponseUtils;
import spark.Request;
import spark.Response;
import spark.Route;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AddContactEndpoint implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {
        String patient_id = request.params("id");
        String address = request.queryParams("address");
        String phoneNumber = request.queryParams("phone").replaceAll("\\D+", "");
        String phoneType = request.queryParams("PhoneType");

        if(!phoneType.equals("Home") && !phoneType.equals("Mobi") && !phoneType.equals("Work")) {
            return ResponseUtils.createError("Invalid Phone Type", 400, response);
        }

        try(Connection conn = BackendServer.database.getDatabase().getConnection();
            PreparedStatement add = conn.prepareStatement("INSERT INTO PatientContact(patient_id, address, phone_num, phone_type) VALUES (?,?,?,?)")) {
            add.setInt(1, Integer.parseInt(patient_id));
            add.setString(2, address);
            add.setString(3, phoneNumber);
            add.setString(4, phoneType);
            add.executeUpdate();
            //response.redirect("/patient/view/" + patient_id);
            return ResponseUtils.createSuccess("Successfully added Contact", response);
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseUtils.createError("Failed to update Contact", 500, response);
        }
    }
}
