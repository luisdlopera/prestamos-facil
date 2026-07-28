package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.auth.repository;

import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.auth.entity.AuthTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthTokenJpaRepository extends JpaRepository<AuthTokenEntity, UUID> {
    Optional<AuthTokenEntity> findByTokenHashAndType(String tokenHash, String type);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<AuthTokenEntity> findByTokenHash(String tokenHash);
    @Modifying @Query("UPDATE AuthTokenEntity t SET t.revoked = true WHERE t.tokenHash = :tokenHash AND t.type = 'REFRESH' AND t.revoked = false")
    void revokeByTokenHash(String tokenHash);
    @Modifying @Query("UPDATE AuthTokenEntity t SET t.revoked = true WHERE t.user.id = :userId AND t.type = 'REFRESH' AND t.revoked = false")
    void revokeAllForUser(UUID userId);
    @Modifying @Query("DELETE FROM AuthTokenEntity t WHERE t.expiresAt < :now")
    int deleteByExpiresAtBefore(Instant now);
}
