package com.turtleshelldevelopment.endpoints;

import com.turtleshelldevelopment.BackendServer;
import com.turtleshelldevelopment.utils.Issuers;
import com.turtleshelldevelopment.utils.ResponseUtils;
import com.turtleshelldevelopment.utils.TokenUtils;
import com.turtleshelldevelopment.utils.db.Account;
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
import java.sql.SQLException;

public class ValidateGeneratedMFAEndpoint implements Route {
    @Override
    public Object handle(Request request, Response response) {
        try(Connection databaseConnection = BackendServer.database.getDatabase().getConnection(); ) {
            String mfaCode = new JSONObject(request.body()).getString("mfaCode");
            BackendServer.serverLogger.info("Given code was: " + mfaCode);
            TokenUtils token = new TokenUtils(request.cookie("token"), Issuers.AUTHENTICATION.getIssuer());
            Account userAccount = Account.getAccountInfo(token.getDecodedJWT().getSubject());

            if (userAccount == null) {
                return ResponseUtils.createError("User account could not be loaded, Please try again", 500, response);
            }
            if (userAccount.isOnboarding()) {
                SystemTimeProvider timeProvider = new SystemTimeProvider();
                DefaultCodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA1, 6);
                DefaultCodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
                verifier.setAllowedTimePeriodDiscrepancy(2);
                System.out.println("Secret is: " + userAccount.getMFASecret());
                if (verifier.isValidCode(userAccount.getMFASecret(), mfaCode)) {
                    //Success set validated to true and return success
                    PreparedStatement statement = databaseConnection.prepareStatement("UPDATE User SET mfa_validated = ?, onboarding = ? WHERE user_id = ?");
                    statement.setBoolean(1, true);
                    statement.setBoolean(2, false);
                    statement.setInt(3, userAccount.getUserId());
                    int updated = statement.executeUpdate();
                    statement.close();
                    if (updated == 1) {
                        return ResponseUtils.createSuccess("Successfully Validated Multi-factor Authentication", response);
                    } else {
                        return ResponseUtils.createError("Failed to update user, Please try again.", 500, response);
                    }
                } else {
                    //Failed incorrect code
                    return ResponseUtils.createError("Multi-factor code was incorrect, Please try again.", 400, response);
                }
            }
            return ResponseUtils.createError("User is not being onboarded", 400, response);
        } catch (JSONException e) {
            return ResponseUtils.createError("Invalid body", 400, response);
        } catch (SQLException e) {
            return ResponseUtils.createError("Server could not handle the request, Please try again later", 500, response);
        }
    }
}
