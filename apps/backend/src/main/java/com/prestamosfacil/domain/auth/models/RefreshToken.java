package com.prestamosfacil.domain.auth.models;

import com.prestamosfacil.domain.auth.enums.TokenType;
import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.user.models.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class RefreshToken {

    @Builder.Default
    private final UUID id = UUID.randomUUID();
    private final TokenType tokenType;
    private final Customer customer;
    private final User user;
    private final String tokenHash;
    private final Instant expiresAt;
    private final boolean revoked;

    public RefreshToken(Customer customer, String tokenHash, Instant expiresAt) {
        this(UUID.randomUUID(), TokenType.CUSTOMER, customer, customer != null ? customer.getUser() : null,
             tokenHash, expiresAt, false);
    }

    public boolean isValid() {
        return !revoked && Instant.now().isBefore(expiresAt);
    }

    public RefreshToken revoke() {
        return RefreshToken.builder()
                .id(id).tokenType(tokenType).customer(customer).user(user)
                .tokenHash(tokenHash).expiresAt(expiresAt).revoked(true)
                .build();
    }
}
