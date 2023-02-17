package com.turtleshelldevelopment.utils.permissions;

public enum PermissionType {
    ADD_SITE("add_site"),
    READ_PATIENT("read_patient"),
    WRITE_PATIENT("write_patient"),
    EDIT_PATIENT("edit_patient"),
    REPORTS("reports"),
    ADD_USER("add_user"),
    EDIT_USER("edit_user"),
    REQUEST_RECORDS("request_records");

    private final String value;
    PermissionType(String value) {
        this.value = value;
    }

    public String getVal() {
        return value.toUpperCase();
    }
}
