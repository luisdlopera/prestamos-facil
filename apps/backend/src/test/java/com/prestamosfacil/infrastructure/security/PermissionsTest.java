package com.prestamosfacil.infrastructure.security;

import com.prestamosfacil.infrastructure.security.permissions.Permissions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionsTest {
    @Test
    void customerOnlyGetsSelfServiceAuthorities() {
        var permissions = Permissions.forRole("CUSTOMER");
        assertThat(permissions).contains(
            Permissions.APPLICATION_CREATE_SELF,
            Permissions.APPLICATION_READ_SELF,
            Permissions.LOAN_READ_SELF,
            Permissions.PAYMENT_PLAN_READ_SELF);
        assertThat(permissions).doesNotContain(
            Permissions.CUSTOMER_CREATE,
            Permissions.APPLICATION_READ_ALL,
            Permissions.APPLICATION_APPROVE,
            Permissions.REPORT_APPROVED_LOANS_READ);
    }

    @Test
    void analystCannotReadReportsOrCreateAdminUsers() {
        var permissions = Permissions.forRole("ANALYST");
        assertThat(permissions).contains(
            Permissions.APPLICATION_READ_ALL,
            Permissions.APPLICATION_EVALUATE,
            Permissions.APPLICATION_APPROVE);
        assertThat(permissions).doesNotContain(
            Permissions.CUSTOMER_CREATE,
            Permissions.REPORT_APPROVED_LOANS_READ);
    }
}
