package com.turtleshelldevelopment.endpoints;

import com.turtleshelldevelopment.BackendServer;
import com.turtleshelldevelopment.JWTAuthentication;
import com.turtleshelldevelopment.utils.EnvironmentType;
import com.turtleshelldevelopment.utils.ResponseUtils;
import com.turtleshelldevelopment.utils.db.Account;
import com.turtleshelldevelopment.utils.permissions.Permissions;
import org.json.JSONException;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Login Route to handle requests to /api/login
 */
public class LoginEndpoint implements Route {

    /**
     * Login with username and password. Username and password must be in a well-formed
     * JSON body.
     * @param request  The request object providing information about the HTTP request
     * @param response The response object providing functionality for modifying the response
     * @return response body
     */
    @Override
    public Object handle(Request request, Response response) {
        String username, password;
        try {
            //parse body as JSON throw error if it cannot be parsed
            JSONObject body = new JSONObject(request.body());
            //get username and password from JSON body
            username = (String) body.get("username");
            password = (String) body.get("password");
            //Check if were in development and just accept it
            if(BackendServer.environment.equals(EnvironmentType.DEVEL)) {
                JWTAuthentication.generateAuthToken(username, new Permissions(username).getPermissionsAsString(), response);
                return ResponseUtils.createLoginSuccess(false, response).toString();
            }
            //Validate that the credentials are valid in our database
            if(validate(username, password)) {
                Account acc;
                if((acc = Account.getAccountInfo(username)) == null) {
                    return ResponseUtils.createError("Invalid Username or Password", 401, response).toString();
                } else if(acc.isDisabled()) {
                    return ResponseUtils.createError("Account is disabled", 401, response).toString();
                }
                JWTAuthentication.generateMultiFactorToken(username, response);
                //Return successful login response
                try {;
                    if(!acc.accountRequiresMFA()) JWTAuthentication.generateAuthToken(username, new Permissions(username).getPermissionsAsString(), response);
                    return ResponseUtils.createLoginSuccess(acc.accountRequiresMFA(), response).toString();
                } catch (SQLException e) {
                    //Log error
                    BackendServer.serverLogger.warn("Error on handling login after validation: {}", e.getMessage());
                    return ResponseUtils.createError("Server was unable to handle this request, Try again later.", 500, response).toString();
                }
            } else {
                //Invalid login, return error
                return ResponseUtils.createError("Invalid Username or Password", 401, response).toString();
            }
        } catch (SQLException e) {
            //Set status to 500 Internal Server Error
            response.status(500);
            //Log error
            BackendServer.serverLogger.warn("Error on handling login: {}", e.getMessage());
            //Return error due to SQL related error (most likely going to be an issue with the server being unresponsive)
            return ResponseUtils.createError("Server was unable to handle this request, Try again later.", 500, response).toString();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            //Set status to 401 Unauthorized
            response.status(500);
            //Log error
            BackendServer.serverLogger.error("Error on handling login: {}", e.getMessage());
            //Return error due to Issues related to the Java runtime environment
            return ResponseUtils.createError("Error with java runtime environment", 500, response).toString();
        } catch (JSONException e) {
            //Set status to 400 Bad Request
            return ResponseUtils.createError("Invalid request", 400, response).toString();
        }
    }


    /**
     * Validates the clients requested username and password
     * Verifying that none are missing or empty and that the
     * expected password is correct for the username provided
     * @param username The username as given by the client
     * @param password The password as given by the client
     * @return true if the client should be given a JWT token for the requested user, false if the
     * username, password is missing, empty, or incorrect
     * @throws SQLException If the Database request fails to respond
     * @throws NoSuchAlgorithmException If the cryptography algorithm does not exist
     * @throws InvalidKeySpecException If the security spec is invalid
     */
    private boolean validate(String username, String password) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        if(username == null || password == null || username.equals("") || password.equals("")) return false;
        //Check database if the user exists with Stored procedure
        Connection databaseConnection = BackendServer.database.getDatabase().getConnection();
        CallableStatement getUser = databaseConnection.prepareCall("CALL GET_USER(?)");
        getUser.setString(1, username);
        ResultSet rs;
        //Execute Stored procedure call
        if((rs = getUser.executeQuery()).next()) {
            //Pull out the correct password hash from row (since we only store hashes in the database with salt)
            byte[] correctPasswordHash = rs.getBytes("password_hash");
            //Pull out salt from row
            byte[] salt = rs.getBytes("salt");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 64 * 8);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            //Get the password hash of the password received from the client
            byte[] password_hash = factory.generateSecret(spec).getEncoded();
            //Get the difference and verify that the password is correct
            int diff = correctPasswordHash.length ^ password_hash.length;
            for(int i = 0; i < correctPasswordHash.length && i < password_hash.length; i++)
            {
                diff |= correctPasswordHash[i] ^ password_hash[i];
            }
            //Close callable statement
            getUser.close();
            databaseConnection.close();
            //return if the difference is 0
            return diff == 0;
        } else {
            //If there is no rows return false
            return false;
        }
    }

}
