package com.turtleshelldevelopment.utils.db;

public record Contact(int id, int patientId, String address, String phoneNumber, String phoneType) {
}
