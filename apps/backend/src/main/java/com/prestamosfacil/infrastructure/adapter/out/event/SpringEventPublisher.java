package com.prestamosfacil.infrastructure.adapter.out.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prestamosfacil.domain.shared.port.out.EventPublisher;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.shared.OutboxEventEntity;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.shared.OutboxEventJpaRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class SpringEventPublisher implements EventPublisher {

    private final OutboxEventJpaRepository outboxEventJpaRepository;
    private final ObjectMapper objectMapper;

    public SpringEventPublisher(OutboxEventJpaRepository outboxEventJpaRepository,
                                 ObjectMapper objectMapper) {
        this.outboxEventJpaRepository = outboxEventJpaRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(Object event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            String eventType = event.getClass().getName();
            String aggregateId = extractAggregateId(event);

            OutboxEventEntity outboxEvent = new OutboxEventEntity(
                UUID.randomUUID(), aggregateId, eventType, payload);
            outboxEventJpaRepository.save(outboxEvent);
        } catch (Exception e) {
            throw new RuntimeException("Failed to persist outbox event", e);
        }
    }

    private String extractAggregateId(Object event) {
        try {
            var m = event.getClass().getMethod("applicationId");
            Object id = m.invoke(event);
            return id != null ? id.toString() : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }
}
