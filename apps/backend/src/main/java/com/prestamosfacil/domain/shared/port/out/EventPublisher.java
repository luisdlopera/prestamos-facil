package com.prestamosfacil.domain.shared.port.out;

public interface EventPublisher {
    void publish(Object event);
}
