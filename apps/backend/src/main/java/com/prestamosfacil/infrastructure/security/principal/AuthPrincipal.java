package com.prestamosfacil.infrastructure.security.principal;

import com.prestamosfacil.domain.user.enums.UserType;
import java.util.Collection;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;

public record AuthPrincipal(
    UUID userId,
    String email,
    UserType userType,
    Collection<? extends GrantedAuthority> authorities
) {}
