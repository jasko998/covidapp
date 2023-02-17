package com.turtleshelldevelopment.utils.db;

import com.turtleshelldevelopment.BackendServer;
import com.turtleshelldevelopment.utils.Issuers;
import com.turtleshelldevelopment.utils.mfa.MultiFactorResponse;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

public class Account {
    protected byte[] salt;
    protected byte[] passwordHash;
    protected SecretKeyFactory factory;
    private int userId, siteId, userType;
    private final String username;
    private String firstName;
    private String lastName;
    private String email;
    private boolean firstLogin;
    private boolean needsMFA, canLogin;
    private String mfaSecret;

    public static Account getAccountInfo(String username) throws SQLException {
        try (Connection databaseConnection = BackendServer.database.getDatabase().getConnection(); PreparedStatement accountCall = databaseConnection.prepareStatement("SELECT user_id, username, first_name, last_name, site_id, user_type, email, onboarding, 2fa_secret, allow_login FROM User WHERE username = ?;")) {
            accountCall.setString(1, username);
            ResultSet set = accountCall.executeQuery();
            if (set.next()) {
                Account acc = new Account(set.getInt("user_id"), set.getString("username"),
                        set.getString("first_name"), set.getString("last_name"),
                        set.getInt("site_id"), set.getInt("user_type"),
                        set.getString("email"), set.getBoolean("onboarding"),
                        set.getString("2fa_secret"), set.getBoolean("allow_login"));
                accountCall.close();
                set.close();
                return acc;
            } else {
                accountCall.close();
                set.close();
                return null;
            }
        }
    }

    private Account(int userID, String username, String firstName, String lastName, int siteId, int userType, String email, boolean onboarding, String mfaSecret, boolean canLogin) {
        this.userId = userID;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.siteId = siteId;
        this.userType = userType;
        this.email = email;
        this.firstLogin = onboarding;
        this.needsMFA = !(onboarding || (mfaSecret != null && mfaSecret.isEmpty()) || mfaSecret == null);
        this.mfaSecret = mfaSecret;
        this.canLogin = canLogin;
    }

    public Account(String username, String password) {
        this.username = username;
        try {
            salt = getSalt();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 64 * 8);
        try {
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        try {
            passwordHash = factory.generateSecret(spec).getEncoded();
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Updates this Account instance with user information from the database
     *
     * @return This Account Instance with Account Information
     */
    public Account getAccountInfo() {
        try (Connection databaseConnection = BackendServer.database.getDatabase().getConnection(); CallableStatement accountCall = databaseConnection.prepareCall("CALL GET_USER(?)")) {
            accountCall.setString(1, username);
            ResultSet set = accountCall.executeQuery();
            if (set.next()) {
                setUserId(set.getInt("user_id"));
                setFirstName(set.getString("first_name"));
                setLastName(set.getString("last_name"));
                setSiteId(set.getInt("site_id"));
                setUserType(set.getInt("user_type"));
                setEmail(set.getString("email"));
            }
        } catch (SQLException e) {
            return this;
        }
        return this;
    }

    public byte[] getPasswordSalt() {
        return this.salt;
    }

    public byte[] getPasswordHash() {
        return this.passwordHash;
    }

    public boolean isOnboarding() {
        return this.firstLogin;
    }

    public boolean accountRequiresMFA() {
        return this.needsMFA;
    }

    private static byte[] getSalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt;
    }

    public MultiFactorResponse generateTOTPMultiFactor() throws QrGenerationException {
        SecretGenerator secretGenerator = new DefaultSecretGenerator();
        String secret = secretGenerator.generate();

        QrData qr = new QrData.Builder()
                .label("Covid-19 Dashboard: " + this.username)
                .secret(secret)
                .issuer(Issuers.AUTHENTICATION.getIssuer())
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();
        QrGenerator generator = new ZxingPngQrGenerator();
        byte[] imageData = generator.generate(qr);
        String mimeType = generator.getImageMimeType();
        String dataUri = getDataUriForImage(imageData, mimeType);
        return new MultiFactorResponse(secret, dataUri, qr.getUri());
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getUserType() {
        try (Connection conn = BackendServer.database.getDatabase().getConnection();
            PreparedStatement getType = conn.prepareStatement("SELECT type_name FROM UserType WHERE user_type_id = ? LIMIT 1")) {
            getType.setInt(1, userType);
            ResultSet set = getType.executeQuery();
            set.next();
            return set.getString("type_name");
        } catch (SQLException e) {
            System.out.println("Failed to get user type");
            return null;
        }
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public int getSiteId() {
        return siteId;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }

    public int getUserId() {
        return userId;
    }

    private void setUserId(int userId) {
        this.userId = userId;
    }

    public String getMFASecret() {
        return mfaSecret;
    }

    public boolean isDisabled() {
        return !this.canLogin;
    }

}
