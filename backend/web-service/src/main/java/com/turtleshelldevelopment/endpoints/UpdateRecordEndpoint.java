package com.turtleshelldevelopment.endpoints;

import com.turtleshelldevelopment.BackendServer;
import com.turtleshelldevelopment.Constants;
import com.turtleshelldevelopment.utils.ResponseUtils;
import spark.Request;
import spark.Response;
import spark.Route;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpdateRecordEndpoint implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {
        System.out.println("Update Record has been called");
        String lastName = request.queryParams("lname");
        String firstName = request.queryParams("fname");
        String middleName = request.queryParams("mname");
        String sexIdentity = request.queryParams("sex");
        String email = request.queryParams("email");
        String id = request.params("id");
        int patientId;
        try {
            patientId = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return ResponseUtils.createError("Invalid patient id", 400, response);
        }
        if(lastName == null) {
            return ResponseUtils.createError("No Last Name", 400, response);
        }
        if(lastName.length() > 45) {
            return ResponseUtils.createError("Last Name too long (max 45 characters)", 400, response);
        }
        if(firstName.length() > 45) {
            return ResponseUtils.createError("First Name too long (max 45 characters)", 400, response);
        }
        if(middleName.length() > 45) {
            return ResponseUtils.createError("Middle Name too long (max 45 characters)", 400, response);
        }
        if(email == null) {
            return ResponseUtils.createError("No email was provided", 400, response);
        }
        if(!Constants.CLIENT_SEX_IDENTITIES.contains(sexIdentity)) {
            return ResponseUtils.createError("Invalid Sex Identity", 400, response);
        }

        //TODO write stored procedure to take in these values and update them if need be (ignore values that are null/empty strings)
        try(Connection conn = BackendServer.database.getDatabase().getConnection()) {
            PreparedStatement updatePatient = conn.prepareStatement("UPDATE PatientInformation SET first_name = ?, last_name = ?, middle_name = ?, gender = ?, email = ? WHERE patient_id = ?");
            updatePatient.setString(1, firstName);
            updatePatient.setString(2, lastName);
            updatePatient.setString(3, middleName);
            updatePatient.setString(4, sexIdentity);
            updatePatient.setString(5, email);
            updatePatient.setInt(6, patientId);
            if(updatePatient.executeUpdate() == 1) {
                return ResponseUtils.createSuccess("Successfully updated User", response);
            } else {
                return ResponseUtils.createError("Failed to update Patient",500,  response);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseUtils.createError("Failed to update Patient", 500, response);
        }
    }
}
