package com.turtleshelldevelopment.endpoints;

import com.turtleshelldevelopment.BackendServer;
import com.turtleshelldevelopment.utils.Issuers;
import com.turtleshelldevelopment.utils.ResponseUtils;
import com.turtleshelldevelopment.utils.TokenUtils;
import com.turtleshelldevelopment.utils.db.Account;
import com.turtleshelldevelopment.utils.mfa.MultiFactorResponse;
import spark.Request;
import spark.Response;
import spark.Route;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class GenerateMFAEndpoint implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {
        //Get user from token and create a mfa then send back the info via mfaQR and mfaCode to client
        try(Connection databaseConnection = BackendServer.database.getDatabase().getConnection();
            PreparedStatement statement = databaseConnection.prepareStatement("UPDATE User SET `2fa_secret` = ?, `mfa_validated` = ? WHERE user_id = ?; ")) {
            TokenUtils tokenVerifier = new TokenUtils(request.cookie("token"), Issuers.AUTHENTICATION.getIssuer());
            if(!tokenVerifier.isInvalid()) {
                Account userAccount = Account.getAccountInfo(tokenVerifier.getDecodedJWT().getSubject());
                MultiFactorResponse multiFactorCode = userAccount.generateTOTPMultiFactor();
                statement.setString(1, multiFactorCode.secret());
                statement.setBoolean(2, false);
                statement.setInt(3, userAccount.getUserId());
                int updatedUser = statement.executeUpdate();
                if(updatedUser == 1) {
                    return ResponseUtils.createMFACreationSuccess(multiFactorCode.qr_code(), multiFactorCode.secret(), response);
                } else {
                    return ResponseUtils.createError("Failed to create multifactor code, Please try again!", 500, response);
                }
            } else {
                return ResponseUtils.createError("Invalid Token", 401, response);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            BackendServer.serverLogger.error("Failed to generate MFA");
            return ResponseUtils.createError("Failed to generate MFA", 500, response);
        }
    }
}
