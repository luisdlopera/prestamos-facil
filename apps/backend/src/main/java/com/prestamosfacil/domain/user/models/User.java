package com.prestamosfacil.domain.user.models;

import com.prestamosfacil.domain.customer.models.DocumentNumber;
import com.prestamosfacil.domain.shared.EmailAddress;
import com.prestamosfacil.domain.shared.Money;
import com.prestamosfacil.domain.shared.enums.Messages;
import com.prestamosfacil.domain.user.utils.PasswordPolicy;
import com.prestamosfacil.domain.user.constants.UserConstants;
import com.prestamosfacil.domain.user.port.out.PasswordEncoderDomainPort;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class User {

    @Builder.Default
    private UUID id = UUID.randomUUID();
    private String firstName;
    private String lastName;
    private EmailAddress email;
    private String passwordHash;
    private DocumentNumber documentNumber;
    private Money baseSalary;

    @Builder.Default
    private String role = UserConstants.DEFAULT_ROLE;
    private int failedLoginAttempts;
    private Instant blockedUntil;
    private Instant lastLoginAt;

    @Builder.Default
    private boolean enabled = true;

    private static final BigDecimal MAX_SALARY = new BigDecimal("15000000.00");

    public String getName() {
        String fn = firstName != null ? firstName : "";
        String ln = lastName != null ? lastName : "";
        return (fn + " " + ln).trim();
    }

    public boolean isBlocked() {
        return blockedUntil != null && Instant.now().isBefore(blockedUntil);
    }

    public void incrementFailedAttempts() {
        failedLoginAttempts++;
        if (failedLoginAttempts >= UserConstants.MAX_FAILED_ATTEMPTS) {
            blockedUntil = Instant.now().plusSeconds(UserConstants.LOCKOUT_MINUTES * 60);
        }
    }

    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
        this.blockedUntil = null;
    }

    public void markLogin() {
        this.lastLoginAt = Instant.now();
    }

    public void changePassword(String rawPassword, PasswordEncoderDomainPort encoder) {
        PasswordPolicy.validate(rawPassword);
        this.passwordHash = encoder.encode(rawPassword);
    }

    public void validate() {
        if (email == null) {
            throw new IllegalArgumentException(Messages.USER_EMAIL_REQUIRED.getValue());
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException(Messages.USER_PASSWORD_HASH_REQUIRED.getValue());
        }
        if (getName().isBlank()) {
            throw new IllegalArgumentException(Messages.USER_NAME_REQUIRED.getValue());
        }
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException(Messages.USER_ROLE_REQUIRED.getValue());
        }
        if (documentNumber == null) {
            throw new IllegalArgumentException(Messages.USER_DOCUMENT_REQUIRED.getValue());
        }
        if (baseSalary != null) {
            BigDecimal salary = baseSalary.getAmount();
            if (salary.compareTo(BigDecimal.ZERO) < 0 || salary.compareTo(MAX_SALARY) > 0) {
                throw new IllegalArgumentException(Messages.AUTH_SALARY_RANGE.getValue());
            }
        }
    }
}
