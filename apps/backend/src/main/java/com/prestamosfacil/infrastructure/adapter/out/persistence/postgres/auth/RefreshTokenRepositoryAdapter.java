package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.auth;

import com.prestamosfacil.domain.auth.enums.TokenType;
import com.prestamosfacil.domain.auth.models.RefreshToken;
import com.prestamosfacil.domain.auth.port.out.RefreshTokenRepository;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.auth.entity.AuthTokenEntity;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.auth.entity.UserEntity;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.auth.repository.AuthTokenJpaRepository;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.auth.repository.UserJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.UUID;

@Component
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {
    private final AuthTokenJpaRepository jpaRepository;
    private final UserJpaRepository userJpaRepository;

    public RefreshTokenRepositoryAdapter(AuthTokenJpaRepository jpaRepository, UserJpaRepository userJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.userJpaRepository = userJpaRepository;
    }

    @Override public Optional<RefreshToken> findById(UUID id) { return jpaRepository.findById(id).map(this::toDomain); }
    @Override public Optional<RefreshToken> findByTokenHash(String tokenHash) { return jpaRepository.findByTokenHash(tokenHash).map(this::toDomain); }
    @Override public RefreshToken save(RefreshToken refreshToken) { return toDomain(jpaRepository.save(toEntity(refreshToken))); }
    @Override @Transactional public void revokeByTokenHash(String tokenHash) { jpaRepository.revokeByTokenHash(tokenHash); }
    @Override @Transactional public void revokeAllForUser(UUID userId) { jpaRepository.revokeAllForUser(userId); }

    private RefreshToken toDomain(AuthTokenEntity entity) {
        UserEntity userEntity = entity.getUser();
        if (userEntity == null) return null;
        com.prestamosfacil.domain.user.models.User user = com.prestamosfacil.domain.user.models.User.builder()
                .id(userEntity.getId())
                .email(new com.prestamosfacil.domain.shared.EmailAddress(userEntity.getEmail()))
                .role(userEntity.getRole())
                .passwordHash(userEntity.getPasswordHash()).failedLoginAttempts(userEntity.getFailedLoginAttempts())
                .blockedUntil(userEntity.getBlockedUntil()).lastLoginAt(userEntity.getLastLoginAt()).enabled(userEntity.isEnabled()).build();
        boolean isStaff = "STAFF_REFRESH".equals(entity.getType());
        return RefreshToken.builder().id(entity.getId()).tokenType(isStaff ? TokenType.STAFF : TokenType.CUSTOMER)
                .user(user).tokenHash(entity.getTokenHash())
                .expiresAt(entity.getExpiresAt()).revoked(entity.isRevoked()).build();
    }

    private AuthTokenEntity toEntity(RefreshToken domain) {
        AuthTokenEntity entity = new AuthTokenEntity();
        entity.setId(domain.getId());
        UUID userId = domain.getUser() != null ? domain.getUser().getId() : (domain.getCustomer() != null ? domain.getCustomer().getUser().getId() : null);
        if (userId != null) entity.setUser(userJpaRepository.getReferenceById(userId));
        entity.setType(domain.getTokenType() == TokenType.STAFF ? "STAFF_REFRESH" : "REFRESH");
        entity.setTokenHash(domain.getTokenHash()); entity.setExpiresAt(domain.getExpiresAt());
        entity.setRevoked(domain.isRevoked()); entity.setUsed(false);
        return entity;
    }
}
