package com.prestamosfacil.domain.user.port.out;

public interface PasswordEncoderDomainPort {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}
