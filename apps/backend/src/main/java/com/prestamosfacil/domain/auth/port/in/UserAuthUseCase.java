package com.prestamosfacil.domain.auth.port.in;

import com.prestamosfacil.domain.auth.models.LoginResult;

import java.math.BigDecimal;
import java.util.UUID;

public interface UserAuthUseCase {
    LoginResult login(String email, String password);
    LoginResult registerCustomer(String firstName, String lastName, String email,
                                  String documentType, String documentNumber,
                                  BigDecimal baseSalary, String password);
    LoginResult refresh(String refreshTokenValue);
    void logoutByToken(String refreshToken);
    void logoutAll(UUID userId);
}
