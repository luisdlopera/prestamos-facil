package com.prestamosfacil.domain.auth.port.in;

import java.util.UUID;

public interface PasswordResetUseCase {
    String requestPasswordReset(String email);
    void confirmPasswordReset(String token, String newPassword);
    void changePassword(UUID customerId, String currentPassword, String newPassword);
}
