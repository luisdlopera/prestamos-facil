package com.prestamosfacil.infrastructure.security.constants;

public final class SecurityConstants {
    private SecurityConstants() {}

    // Cookie
    public static final String COOKIE_ACCESS_NAME = "prestamos_access";
    public static final String COOKIE_REFRESH_NAME = "prestamos_refresh";
    public static final String COOKIE_PATH = "/";
    public static final boolean COOKIE_HTTP_ONLY = true;
    public static final String COOKIE_SAME_SITE = "Lax";

    // API
    public static final String API_V1_PREFIX = "/api/v1";
    public static final String API_AUTH_PATH = "/api/v1/auth";
    public static final String API_STAFF_PATH = "/api/v1/staff";

    // Rate limiting
    public static final int LOGIN_RATE_LIMIT_MAX = 20;
    public static final int LOGIN_RATE_LIMIT_WINDOW_MINUTES = 15;
    public static final int RATE_LIMIT_CACHE_MAX_SIZE = 10_000;
}
