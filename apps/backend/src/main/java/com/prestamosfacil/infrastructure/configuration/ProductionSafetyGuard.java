package com.prestamosfacil.infrastructure.configuration;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class ProductionSafetyGuard {

    private static final String DEV_JWT_SECRET = "dev-secret-replace-in-production-at-least-32-chars";
    private static final String DEV_DB_PASSWORD = "prestamos_local";

    private final String jwtSecret;
    private final String dbPassword;

    public ProductionSafetyGuard(@Value("${app.security.jwt-secret}") String jwtSecret,
                                  @Value("${spring.datasource.password}") String dbPassword) {
        this.jwtSecret = jwtSecret;
        this.dbPassword = dbPassword;
    }

    @PostConstruct
    public void verifyNoDevDefaults() {
        if (DEV_JWT_SECRET.equals(jwtSecret)) {
            throw new IllegalStateException(
                "JWT_SECRET is still set to the development default; set a real secret via the JWT_SECRET environment variable before running with the 'prod' profile.");
        }
        if (DEV_DB_PASSWORD.equals(dbPassword)) {
            throw new IllegalStateException(
                "DB_PASSWORD is still set to the development default; set a real password via the DB_PASSWORD environment variable before running with the 'prod' profile.");
        }
    }
}
