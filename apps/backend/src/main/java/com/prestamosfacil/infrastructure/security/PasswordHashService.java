package com.prestamosfacil.infrastructure.security;

import com.prestamosfacil.domain.auth.port.out.HashCalculator;
import com.prestamosfacil.domain.user.port.out.PasswordEncoderDomainPort;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordHashService implements PasswordEncoderDomainPort, HashCalculator {
    private final PasswordEncoder passwordEncoder;
    public PasswordHashService(PasswordEncoder passwordEncoder) { this.passwordEncoder = passwordEncoder; }
    @Override public boolean matches(String raw, String hash) { return passwordEncoder.matches(raw, hash); }
    @Override public String encode(String raw) { return passwordEncoder.encode(raw); }
    @Override
    public String sha256(String text) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception ex) { throw new IllegalStateException("Unable to hash value", ex); }
    }
}
