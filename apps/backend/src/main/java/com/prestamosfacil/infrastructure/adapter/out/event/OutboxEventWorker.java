package com.prestamosfacil.infrastructure.adapter.out.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.shared.OutboxEventEntity;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.shared.OutboxEventJpaRepository;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxEventWorker {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventWorker.class);

    private final OutboxEventJpaRepository outboxEventJpaRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectMapper objectMapper;
    private OutboxEventWorker self;

    public OutboxEventWorker(OutboxEventJpaRepository outboxEventJpaRepository,
                              ApplicationEventPublisher applicationEventPublisher,
                              ObjectMapper objectMapper) {
        this.outboxEventJpaRepository = outboxEventJpaRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.objectMapper = objectMapper;
        this.self = this;
    }

    // Self-reference re-injected as the Spring AOP proxy so calls to
    // processEvent() from within this class go through the proxy and its
    // @Transactional boundary actually applies (self-invocation otherwise
    // bypasses the proxy, leaving no active session for lazy associations).
    @Autowired
    public void setSelf(@Lazy OutboxEventWorker self) {
        this.self = self;
    }

    @Scheduled(fixedDelay = 10_000)
    public void processPendingEvents() {
        processBatch(outboxEventJpaRepository.findByStatusOrderByCreatedAt("PENDING"));
    }

    @Scheduled(fixedDelay = 60_000)
    public void retryFailedEvents() {
        List<OutboxEventEntity> failed = outboxEventJpaRepository
            .findByStatusOrderByCreatedAt("FAILED")
            .stream()
            .filter(event -> event.getRetryCount() < event.getMaxRetries())
            .toList();
        processBatch(failed);
    }

    /**
     * Publishes the listener synchronously, per event, so an exception thrown while
     * delivering it (e.g. SMTP down) is caught here BEFORE the event is marked
     * processed. This is what makes retries meaningful: a PROCESSED row means the
     * notification was actually handled, not just that we attempted it.
     */
    @Transactional
    public void processEvent(OutboxEventEntity event) {
        try {
            Class<?> eventClass = Class.forName(event.getEventType());
            Object domainEvent = objectMapper.readValue(event.getPayload(), eventClass);
            applicationEventPublisher.publishEvent(domainEvent);
            outboxEventJpaRepository.markProcessed(event.getId(), Instant.now());
        } catch (Exception e) {
            log.error("Failed to process outbox event {} ({}): {}",
                event.getId(), event.getEventType(), e.getMessage(), e);
            outboxEventJpaRepository.markFailed(event.getId(), e.getMessage());
        }
    }

    private void processBatch(List<OutboxEventEntity> events) {
        for (OutboxEventEntity event : events) {
            self.processEvent(event);
        }
    }
}
