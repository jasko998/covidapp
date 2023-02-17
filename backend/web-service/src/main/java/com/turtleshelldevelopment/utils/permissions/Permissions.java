package com.turtleshelldevelopment.utils.permissions;

import com.turtleshelldevelopment.BackendServer;
import com.turtleshelldevelopment.utils.EnvironmentType;
import org.json.JSONArray;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Permissions {
    JSONArray permissions = new JSONArray();
    public Permissions(String user) throws SQLException {
        if(BackendServer.environment.equals(EnvironmentType.DEVEL)) {
            permissions.put("READ_PATIENT");
            permissions.put("WRITE_PATIENT");
            permissions.put("EDIT_PATIENT");
            permissions.put("REPORTS");
            permissions.put("ADD_USER");
            permissions.put("EDIT_USER");
            permissions.put("ADD_SITE");
            permissions.put("REQUEST_RECORDS");
        }
        try (Connection databaseConnection = BackendServer.database.getDatabase().getConnection();
             CallableStatement getPermissions = databaseConnection.prepareCall("CALL GET_PERMISSIONS(?)")) {
            getPermissions.setString(1, user);
            ResultSet set = getPermissions.executeQuery();
            if (set.next()) {
                if (set.getBoolean("read_patient")) permissions.put("READ_PATIENT");
                if (set.getBoolean("write_patient")) permissions.put("WRITE_PATIENT");
                if (set.getBoolean("edit_patient")) permissions.put("EDIT_PATIENT");
                if (set.getBoolean("reports")) permissions.put("REPORTS");
                if (set.getBoolean("add_user")) permissions.put("ADD_USER");
                if (set.getBoolean("edit_user")) permissions.put("EDIT_USER");
                if (set.getBoolean("add_site")) permissions.put("ADD_SITE");
                if (set.getBoolean("request_records")) permissions.put("REQUEST_RECORDS");
            }
            set.close();
        }
    }

    public String[] getPermissionsAsString() {
        String[] perms = new String[permissions.length()];
        for(int i = 0; i < permissions.length();i++) {
            perms[i] = (String) permissions.get(i);
        }
        return perms;
    }

}
