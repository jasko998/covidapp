package com.turtleshelldevelopment.endpoints;

import com.turtleshelldevelopment.BackendServer;
import com.turtleshelldevelopment.utils.FormValidator;
import com.turtleshelldevelopment.utils.ResponseUtils;
import com.turtleshelldevelopment.utils.UserType;
import com.turtleshelldevelopment.utils.db.Account;
import org.json.JSONException;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

//*****************************************************
//*                                                   *
//* Created By: Colin Kinzel                          *
//* Created On: 10/6/2022, 4:28:35 PM                 *
//* Last Modified By: Colin Kinzel                    *
//* Last Modified On: 10/6/2022, 4:28:35 PM           *
//* Description: New Account Endpoint Handler Route   *
//*                                                   *
//*****************************************************
public class NewAccountEndpoint implements Route {

    @Override
    public Object handle(Request request, Response response) {
        BackendServer.serverLogger.debug("Handling New Account!");
        try(Connection databaseConnection = BackendServer.database.getDatabase().getConnection();
            PreparedStatement statement = databaseConnection.prepareStatement("CALL CHECK_USERNAME(?);");
            CallableStatement insertUser = databaseConnection.prepareCall("CALL ADD_USER(?,?,?,?,?,?,?,?)")
            ) {
            JSONObject body = new JSONObject(request.body());
            String username = body.getString("username");
            String password = body.getString("password");
            String firstName = body.getString("fname");
            String lastName = body.getString("lname");
            String email = body.getString("email");
            int site = body.getInt("site");
            //Validate Username
            if (username.length() > 45) {
                //To long of username
                return ResponseUtils.createError("Username is too long.", 400, response);
            } else if (username.length() <= 4) {
                //To short of username
                return ResponseUtils.createError("Username is too short.", 400, response);
            }
            //Check for usernames with non letter or number
            if(!FormValidator.isValidUsername(username)) {
                return ResponseUtils.createError("Username contains invalid characters.", 400, response);
            }
            statement.setString(1, username);
            if (statement.executeQuery().next()) {
                statement.close();
                return ResponseUtils.createError("Username already exists", 400, response);
            }

            Account acc = new Account(username, password);
            //Take in the password and hash it
            byte[] salt = acc.getPasswordSalt();
            //Put Permissions
            int userType = body.getInt("user_type");
            UserType type;
            if ((type = UserType.isValidType(userType)) == null) {
                return ResponseUtils.createError("User Type is invalid.", 400, response);
            }
            //Insert new account
            insertUser.setString(1, username);
            insertUser.setBytes(2, acc.getPasswordHash());
            insertUser.setBytes(3, salt);
            insertUser.setString(4, firstName);
            insertUser.setString(5, lastName);
            insertUser.setInt(6, site);
            insertUser.setInt(7, type.id());
            insertUser.setString(8, email);
            if (insertUser.executeUpdate() == 1) {
                BackendServer.serverLogger.info("Success!");
                insertUser.close();
                return ResponseUtils.createCreateUserSuccessResponse(response).toString();
            } else {
                insertUser.close();
                BackendServer.serverLogger.error("Failed to update while creating new account");
                return ResponseUtils.createError("Failed to update database", 500, response);
            }
        } catch (SQLException e) {
            BackendServer.serverLogger.info("ERROR: " + e.getMessage());
            return ResponseUtils.createError("Error handling creating a new account", 500, response);
        } catch (JSONException e) {
            return ResponseUtils.createError("Body is malformed", 400, response);
        }
    }
}
