package com.prestamosfacil.domain.user.constants;

public final class UserConstants {
    private UserConstants() {}

    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final long LOCKOUT_MINUTES = 30;
    public static final int PASSWORD_MIN_LENGTH = 8;

    public static final String DEFAULT_ROLE = "CUSTOMER";
    public static final String ROLE_CUSTOMER = "CUSTOMER";
}
