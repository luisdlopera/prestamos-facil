package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.auth.job;

import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.auth.repository.AuthTokenJpaRepository;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RefreshTokenCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenCleanupJob.class);
    private final AuthTokenJpaRepository jpaRepository;

    public RefreshTokenCleanupJob(AuthTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void purgeExpiredTokens() {
        int deleted = jpaRepository.deleteByExpiresAtBefore(Instant.now());
        if (deleted > 0) {
            log.info("Purged {} expired auth tokens", deleted);
        }
    }
}
