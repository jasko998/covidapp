package com.turtleshelldevelopment.endpoints;

import com.turtleshelldevelopment.BackendServer;
import com.turtleshelldevelopment.utils.ResponseUtils;
import spark.Request;
import spark.Response;
import spark.Route;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class WeeklyReportEndpoint implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {
        String date = request.queryParams("weekly-report-date");
        String dose = request.queryParams("dose");
        String sex = request.queryParams("sex");
        String startAge = request.queryParams("start_age");
        String endAge = request.queryParams("end_age");
        String fips = request.queryParams("fips");
        if(date.isEmpty()) {
            return ResponseUtils.createError("Date is missing", 400, response);
        }
        DateTimeFormatter f = DateTimeFormatter.ofPattern( "MM/dd/uuuu" );
        LocalDate ld;
        try {
            ld = LocalDate.parse(date, f);
        } catch (DateTimeParseException e) {
            return ResponseUtils.createError("Date is in invalid format", 400, response);
        }
        if(ld.isAfter(LocalDate.now())) {
            return ResponseUtils.createError("Can't generate report of date in the future", 400, response);
        }

        try(Connection databaseConnection = BackendServer.database.getDatabase().getConnection();
            PreparedStatement statement = databaseConnection.prepareStatement("SELECT vax.lot_num, vax.administered_date, vax.manufacturer, vax.dose, site.location, site.phone_number, site.name, site.zip_code, patient.dob, patient.gender, user.first_name, user.last_name FROM is241_mo_vat.Vaccine vax, is241_mo_vat.Site site, is241_mo_vat.PatientInformation patient, is241_mo_vat.User user WHERE site.site_id = vax.site_id AND patient.patient_id = vax.patient_id AND user.user_id = vax.administrated_by AND vax.administered_date >= ? AND vax.administered_date <= ? AND vax.dose = ? AND patient.gender = ? AND patient.dob >= ? AND patient.dob <= ? AND site.fips = ?;");
        ) {
            statement.setDate(1, Date.valueOf(ld));
            statement.setDate(2, Date.valueOf(ld.plusWeeks(1)));
            statement.setString(3, dose != null && !dose.equals("All") && !dose.isEmpty() ? dose : "%");
            statement.setString(4, sex != null  && !sex.equals("All") && !sex.isEmpty() ? sex : "%");
            statement.setDate(5, endAge != null && !endAge.isEmpty() ? Date.valueOf(LocalDate.now().minusYears(Long.parseLong(endAge))) : Date.valueOf(LocalDate.now().minusYears(50)));
            statement.setDate(6, startAge != null && !startAge.isEmpty() ? Date.valueOf(LocalDate.now().minusYears(Long.parseLong(startAge))) : Date.valueOf(LocalDate.now()));
            statement.setString(7, fips != null && !fips.isEmpty() ? fips : "%");
            ResultSet set = statement.executeQuery();
            StringBuilder output = new StringBuilder();
            output.append("Lot Number,Administered Date,Manufacturer,Dose,Location,Location Phone Number,Location Name,Location Zip Code,Patient Date of Birth,Patient Gender,Administered By\n");
            while(set.next()) {
                output.append(set.getString("lot_num")).append(",")
                        .append(set.getString("administered_date"))
                        .append(",\"").append(set.getString("manufacturer"))
                        .append("\",\"").append(set.getString("dose"))
                        .append("\",\"").append(set.getString("location"))
                        .append("\",").append(set.getString("phone_number"))
                        .append(",\"").append(set.getString("name"))
                        .append("\",").append(set.getString("zip_code"))
                        .append(",").append(set.getString("dob"))
                        .append(",").append(set.getString("gender"))
                        .append(",\"").append(set.getString("last_name"))
                        .append(", ").append(set.getString("first_name"))
                        .append("\"\n");
            }
            set.close();
            response.header("Content-Type", "text/csv");
            return output.toString();
        } catch (SQLException e) {
            return ResponseUtils.createError("Failed to generate CSV File", 500, response);
        }
    }
}
