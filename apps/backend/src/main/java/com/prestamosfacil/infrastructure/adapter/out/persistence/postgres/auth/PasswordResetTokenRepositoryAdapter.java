package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.auth;

import com.prestamosfacil.domain.auth.models.PasswordResetToken;
import com.prestamosfacil.domain.auth.port.out.PasswordResetTokenRepository;
import com.prestamosfacil.domain.shared.EmailAddress;
import com.prestamosfacil.domain.user.models.User;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.auth.entity.AuthTokenEntity;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.auth.entity.UserEntity;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.auth.repository.AuthTokenJpaRepository;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.auth.repository.UserJpaRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
public class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepository {
    private final AuthTokenJpaRepository jpaRepository;
    private final UserJpaRepository userJpaRepository;
    public PasswordResetTokenRepositoryAdapter(AuthTokenJpaRepository jpaRepository, UserJpaRepository userJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.userJpaRepository = userJpaRepository;
    }

    @Override public Optional<PasswordResetToken> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHashAndType(tokenHash, "PASSWORD_RESET").map(this::toDomain);
    }

    @Override public PasswordResetToken save(PasswordResetToken token) { return toDomain(jpaRepository.save(toEntity(token))); }

    private PasswordResetToken toDomain(AuthTokenEntity e) {
        UserEntity u = e.getUser();
        User user = User.builder().id(u.getId())
                .email(new EmailAddress(u.getEmail()))
                .passwordHash(u.getPasswordHash()).role(u.getRole()).failedLoginAttempts(u.getFailedLoginAttempts())
                .blockedUntil(u.getBlockedUntil()).lastLoginAt(u.getLastLoginAt()).enabled(u.isEnabled()).build();
        return PasswordResetToken.builder().id(e.getId()).user(user).tokenHash(e.getTokenHash())
                .expiresAt(e.getExpiresAt()).used(e.isUsed()).build();
    }

    private AuthTokenEntity toEntity(PasswordResetToken d) {
        var e = new AuthTokenEntity();
        e.setId(d.getId()); e.setUser(userJpaRepository.getReferenceById(d.getUser().getId()));
        e.setType("PASSWORD_RESET"); e.setTokenHash(d.getTokenHash()); e.setExpiresAt(d.getExpiresAt());
        e.setRevoked(false); e.setUsed(d.isUsed());
        return e;
    }
}
