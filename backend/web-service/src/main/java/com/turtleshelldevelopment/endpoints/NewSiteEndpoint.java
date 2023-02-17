package com.turtleshelldevelopment.endpoints;

import com.turtleshelldevelopment.BackendServer;
import com.turtleshelldevelopment.utils.FormValidator;
import com.turtleshelldevelopment.utils.ResponseUtils;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.sql.*;
import java.util.regex.Pattern;

public class NewSiteEndpoint implements Route {
    private final Pattern streetAddressPattern = Pattern.compile("^\\d+(\\s\\w+)(\\s?\\w+)+$");


    //TODO Properly handle and make sure this works
    @Override
    public Object handle(Request request, Response response) throws SQLException {

        String location = request.queryParams("address");
        String county = request.queryParams("county");
        String phoneNum = request.queryParams("phone");
        String fips = request.queryParams("fips");
        String zip = request.queryParams("zip");
        String name = request.queryParams("siteName");
        if(name.length() > 45) {
            return ResponseUtils.createError("Site Name is too long (max 45 characters)", 400, response);
        }
        if(zip.length() > 5) {
            return ResponseUtils.createError("Zip code is invalid (max 5 characters", 400, response);
        }

        //Check for county and verify the fips code is the same
        try(Connection conn = BackendServer.database.getDatabase().getConnection(); CallableStatement createSite = conn.prepareCall("CALL ADD_SITE(?,?,?,?,?)")) {

            if (streetAddressPattern.matcher(location).find()) {
                createSite.setString(1, location);
                createSite.setString(2, FormValidator.parsePhoneNumberFromForm(phoneNum));
                createSite.setString(3, fips);
                createSite.setString(4, zip);
                createSite.setString(5, name);
                if (createSite.executeUpdate() == 1) {
                    JSONObject success = new JSONObject();
                    success.put("error", 200);
                    success.put("", "");
                    createSite.close();
                    return success;
                } else {
                    BackendServer.serverLogger.error("Failed to update database for site");
                    createSite.close();
                    return ResponseUtils.createError("Could not add new site to the database!", 500, response);
                }
            }
            return ResponseUtils.createError("No Street address give", 400, response);
        } catch (SQLException e) {
            return ResponseUtils.createError("Failed to create new site", 500, response);
        }
    }
}
