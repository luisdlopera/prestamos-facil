package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.shared;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, UUID> {

    List<OutboxEventEntity> findByStatusOrderByCreatedAt(String status);

    @Modifying
    @Query("UPDATE OutboxEventEntity e SET e.status = 'PROCESSED', e.processedAt = :now WHERE e.id = :id")
    void markProcessed(UUID id, Instant now);

    @Modifying
    @Query("UPDATE OutboxEventEntity e SET e.status = 'FAILED', e.retryCount = e.retryCount + 1, e.lastError = :error WHERE e.id = :id")
    void markFailed(UUID id, String error);
}
