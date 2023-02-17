package com.turtleshelldevelopment.utils.mfa;

public record MultiFactorResponse(String secret, String qr_code, String qr_data) {}
