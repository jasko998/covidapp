package com.turtleshelldevelopment.endpoints;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.turtleshelldevelopment.BackendServer;
import com.turtleshelldevelopment.JWTAuthentication;
import com.turtleshelldevelopment.utils.EnvironmentType;
import com.turtleshelldevelopment.utils.ResponseUtils;
import com.turtleshelldevelopment.utils.permissions.Permissions;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.time.SystemTimeProvider;
import org.json.JSONException;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MfaEndpoint implements Route {
    @Override
    public Object handle(Request request, Response response) throws JSONException {
        String jwtQuery = request.cookie("token");
        JSONObject bodyJSON = new JSONObject(request.body());
        if (!bodyJSON.has("code")) {
            JSONObject failure = new JSONObject();
            failure.put("success", false);
            failure.put("message", "Missing code in Body");
            return failure;
        }
        try {
            String code = (String) bodyJSON.get("code");
            DecodedJWT jwt = JWT.decode(jwtQuery);
            BackendServer.serverLogger.info("Payload is: " + jwt.getClaims().toString());
            JSONObject success = new JSONObject();
            BackendServer.JWT_ALGO.verify(jwt);

            String username = jwt.getSubject();
            SystemTimeProvider timeProvider = new SystemTimeProvider();
            DefaultCodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA1, 6);
            DefaultCodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
            verifier.setAllowedTimePeriodDiscrepancy(2);
            if (BackendServer.environment.equals(EnvironmentType.DEVEL)) {
                response.removeCookie("/", "token");
                Permissions perms = new Permissions(username);

                JWTAuthentication.generateAuthToken(username, perms.getPermissionsAsString(), response);
                return ResponseUtils.createMFASuccess(response);
            }

            //Get Secret from Database
            try (
                    Connection databaseConnection = BackendServer.database.getDatabase().getConnection();
                    PreparedStatement getSecret = databaseConnection.prepareStatement("SELECT 2fa_secret FROM User WHERE username = ?;")
            ) {
                getSecret.setString(1, username);
                ResultSet set;
                if (!(set = getSecret.executeQuery()).next()) {
                    JSONObject error = new JSONObject();
                    error.put("success", false);
                    error.put("message", "Nonexistent user");
                    error.put("retry", false);
                    response.status(401);
                    return error;
                }
                String secret = set.getString("2fa_secret");

                BackendServer.serverLogger.info("Secret is: " + secret);
                BackendServer.serverLogger.info("Is Code (" + code + ") valid: " + verifier.isValidCode(secret, code));

                set.close();
                getSecret.close();
                if (verifier.isValidCode(secret, code)) {
                    success.put("success", true);
                    response.removeCookie("/", "token");
                    Permissions perms = new Permissions(username);

                    JWTAuthentication.generateAuthToken(username, perms.getPermissionsAsString(), response);
                    return success;
                } else {
                    success.put("success", false);
                    success.put("message", "Invalid Two Factor Code");
                    success.put("retry", true);
                }
                return success;
            } catch (SQLException e) {
                return ResponseUtils.createError("Information could not be loaded for the user", 500, response);
            }
        } catch (SignatureVerificationException e) {
            JSONObject error = new JSONObject();
            error.put("success", false);
            error.put("message", "Invalid Signature");
            error.put("retry", false);
            response.status(401);
            return error.toString();
        } catch (NullPointerException e) {
            JSONObject error = new JSONObject();
            error.put("success", false);
            error.put("message", "Token is no longer valid");
            error.put("retry", false);
            response.status(401);
            return error.toString();
        } catch (SQLException e) {
            e.printStackTrace();
            JSONObject error = new JSONObject();
            error.put("success", false);
            error.put("message", "Database failed with an error");
            error.put("retry", false);
            response.status(401);
            return error.toString();
        }

    }

}
