package com.turtleshelldevelopment;

import com.turtleshelldevelopment.utils.db.Account;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang.RandomStringUtils;

import java.sql.*;

public class Database {
    private static final HikariConfig config = new HikariConfig();
    static HikariDataSource db;


    public Database(String url, String username, String password) {
        config.setMaximumPoolSize(25);
        config.setConnectionTimeout(30000);
        config.setLeakDetectionThreshold(15000);
        config.setIdleTimeout(30000);
        config.setJdbcUrl("jdbc:mariadb://" + url + "/is241_mo_vat");
        config.setUsername(username);
        config.setPassword(password);
        db = new HikariDataSource(config);

        //Checks if there is a user in the database which essentially verifies that there is someone in the database and
        // if not create a new superuser
        try {
            BackendServer.serverLogger.info("Checking Account...");
            Connection dbConnection = db.getConnection();
            PreparedStatement checkForUser = dbConnection.prepareStatement("SELECT user_id FROM User LIMIT 1;");
            ResultSet set = checkForUser.executeQuery();
            if(set.next()) {
                System.out.println("Found account: " + set.getInt("user_id") + " on database " + url);
            } else {
                BackendServer.serverLogger.info("Creating System Admin Account...");
                String adminPassword = RandomStringUtils.random(16, true, true);
                String systemAdminUsername = "admin";
                Account admin = new Account(systemAdminUsername, adminPassword);
                CallableStatement statement = dbConnection.prepareCall("CALL ADD_USER(?,?,?,?,?,?,?,?)");
                statement.setString(1, systemAdminUsername);
                statement.setBytes(2, admin.getPasswordHash());
                statement.setBytes(3, admin.getPasswordSalt());
                statement.setString(4, "admin");
                statement.setString(5, "admin");
                statement.setInt(6, 1);
                statement.setInt(7, 1);
                statement.setString(8, "unconfigured@example.com");
                if(statement.executeUpdate() == 1) {
                    BackendServer.serverLogger.info("Password: " + adminPassword);
                    BackendServer.serverLogger.info("Remember this Information, This information will not show on next startup!");
                }

                statement.close();
            }
            checkForUser.close();
            dbConnection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public Database() {
        this(BackendServer.env.get("PROD_URL"), BackendServer.env.get("PROD_USERNAME", "db_team"), BackendServer.env.get("PROD_PASSWORD", ""));
    }

    public HikariDataSource getDatabase() {
        //BackendServer.serverLogger.info("Hikari (Active: " + db.getHikariPoolMXBean().getActiveConnections() + ", Idle: " + db.getHikariPoolMXBean().getIdleConnections() + ", Total: " + db.getHikariPoolMXBean().getTotalConnections() + ")");
        return db;
    }

}
