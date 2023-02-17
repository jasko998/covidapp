package com.turtleshelldevelopment.endpoints;

import com.turtleshelldevelopment.BackendServer;
import com.turtleshelldevelopment.Constants;
import com.turtleshelldevelopment.utils.Issuers;
import com.turtleshelldevelopment.utils.ResponseUtils;
import com.turtleshelldevelopment.utils.TokenUtils;
import spark.Request;
import spark.Response;
import spark.Route;

 import java.sql.*;
import java.time.LocalDate;

public class AddVaccineEndpoint implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {
        String patientId = request.params("id");
        String manufacturer = request.queryParams("vaxMan");
        String lotNumber = request.queryParams("lotNumber");
        String dose = request.queryParams("dose");

        TokenUtils tokenVerifier = new TokenUtils(request.cookie("token"), Issuers.AUTHENTICATION.getIssuer());
        if(tokenVerifier.isInvalid()) {
            System.out.println("Token is invalid: " + tokenVerifier.getErrorReason());
            response.redirect("/");
            return ResponseUtils.createError("Invalid Token", 401, response);
        }

        if(patientId == null || manufacturer == null || lotNumber == null || dose == null) {
            return ResponseUtils.createError("Missing Parameters", 400, response);
        }
        if(patientId.isEmpty() || manufacturer.isEmpty() || lotNumber.isEmpty() || dose.isEmpty()) {
            return ResponseUtils.createError("Missing Parameters", 400, response);
        }
        if(Constants.VACCINE_SERIES.get(dose) == null) {
            return ResponseUtils.createError("Invalid Vaccine Series", 400, response);
        }

        try(Connection databaseConnection = BackendServer.database.getDatabase().getConnection();
            PreparedStatement addVaccine = databaseConnection.prepareStatement("INSERT INTO Vaccine (lot_num, site_id, patient_id, administered_date, manufacturer, dose, administrated_by) VALUES (?,?,?,?,?,?,?);")
        ) {
            addVaccine.setString(1, lotNumber);
            addVaccine.setInt(2, tokenVerifier.getSiteId()); //Site Id
            addVaccine.setInt(3, Integer.parseInt(patientId));// patient Id
            addVaccine.setDate(4, Date.valueOf(LocalDate.now()));// administered date
            addVaccine.setString(5, manufacturer.substring(0, 1).toUpperCase() + manufacturer.substring(1));// manufacturer
            addVaccine.setString(6, dose);// dose
            addVaccine.setInt(7, tokenVerifier.getUserId());// administrated by
            if(addVaccine.executeUpdate() == 1) {
                response.redirect("/patient/view/" + patientId);
                return ResponseUtils.createSuccess("Successfully added new vaccine to patient.", response);
            } else {
                return ResponseUtils.createError("Failed to update vaccine info for patient", 500, response);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseUtils.createError("SQL Error", 500, response);
        }
    }
}
