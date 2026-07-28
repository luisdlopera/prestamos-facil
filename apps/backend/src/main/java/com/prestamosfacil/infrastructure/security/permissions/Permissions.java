package com.prestamosfacil.infrastructure.security.permissions;

import java.util.Set;

/** Authorities de aplicación. No se debe usar ROLE_STAFF como permiso de negocio. */
public final class Permissions {
    private Permissions() {}

    public static final String CUSTOMER_CREATE = "CUSTOMER_CREATE";
    public static final String APPLICATION_CREATE_SELF = "LOAN_APPLICATION_CREATE_SELF";
    public static final String APPLICATION_CREATE_FOR_CUSTOMER = "LOAN_APPLICATION_CREATE_FOR_CUSTOMER";
    public static final String APPLICATION_READ_SELF = "LOAN_APPLICATION_READ_SELF";
    public static final String APPLICATION_READ_ALL = "LOAN_APPLICATION_READ_ALL";
    public static final String APPLICATION_EVALUATE = "LOAN_APPLICATION_EVALUATE";
    public static final String APPLICATION_APPROVE = "LOAN_APPLICATION_APPROVE";
    public static final String APPLICATION_REJECT = "LOAN_APPLICATION_REJECT";
    public static final String LOAN_READ_SELF = "LOAN_READ_SELF";
    public static final String LOAN_READ_ALL = "LOAN_READ_ALL";
    public static final String PAYMENT_PLAN_READ_SELF = "PAYMENT_PLAN_READ_SELF";
    public static final String PAYMENT_PLAN_READ_ALL = "PAYMENT_PLAN_READ_ALL";
    public static final String REPORT_APPROVED_LOANS_READ = "REPORT_APPROVED_LOANS_READ";

    public static Set<String> forRole(String role) {
        String normalized = role == null ? "" : role.trim().toUpperCase();
        return switch (normalized) {
            case "ADMIN" -> Set.of(CUSTOMER_CREATE, APPLICATION_CREATE_SELF, APPLICATION_CREATE_FOR_CUSTOMER,
                APPLICATION_READ_SELF, APPLICATION_READ_ALL, APPLICATION_EVALUATE, APPLICATION_APPROVE,
                APPLICATION_REJECT, LOAN_READ_SELF, LOAN_READ_ALL, PAYMENT_PLAN_READ_SELF,
                PAYMENT_PLAN_READ_ALL, REPORT_APPROVED_LOANS_READ);
            case "ANALYST", "CREDIT_ANALYST" -> Set.of(APPLICATION_CREATE_FOR_CUSTOMER, APPLICATION_READ_ALL,
                APPLICATION_EVALUATE, APPLICATION_APPROVE, APPLICATION_REJECT, LOAN_READ_ALL,
                PAYMENT_PLAN_READ_ALL);
            case "SUPERVISOR" -> Set.of(CUSTOMER_CREATE, APPLICATION_CREATE_FOR_CUSTOMER, APPLICATION_READ_ALL,
                APPLICATION_APPROVE, APPLICATION_REJECT, LOAN_READ_ALL, PAYMENT_PLAN_READ_ALL,
                REPORT_APPROVED_LOANS_READ);
            case "AUDITOR" -> Set.of(APPLICATION_READ_ALL, LOAN_READ_ALL, PAYMENT_PLAN_READ_ALL,
                REPORT_APPROVED_LOANS_READ);
            case "CUSTOMER" -> Set.of(APPLICATION_CREATE_SELF, APPLICATION_READ_SELF, LOAN_READ_SELF,
                PAYMENT_PLAN_READ_SELF);
            default -> Set.of();
        };
    }
}
