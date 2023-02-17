package com.turtleshelldevelopment.utils;

import com.turtleshelldevelopment.utils.permissions.PermissionType;
import com.turtleshelldevelopment.utils.permissions.Permissions;

import java.util.HashMap;

public class DevelopmentTokenUtils {

    public HashMap<PermissionType, Boolean> getPermissions() {
        HashMap<PermissionType, Boolean> permissionData = new HashMap<>();
        for(PermissionType type : PermissionType.values()) {
            if(type.equals(PermissionType.EDIT_PATIENT)) {
                permissionData.put(type,false);
                continue;
            }
            permissionData.put(type, true);
        }
        return permissionData;
    }

    public boolean isInvalid() {
        return false;
    }
}
