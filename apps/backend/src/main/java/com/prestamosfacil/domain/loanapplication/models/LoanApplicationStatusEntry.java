package com.prestamosfacil.domain.loanapplication.models;

import com.prestamosfacil.domain.loanapplication.enums.LoanApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class LoanApplicationStatusEntry {

    @Builder.Default
    private final UUID id = UUID.randomUUID();

    private final UUID loanApplicationId;
    private final LoanApplicationStatus status;
    private final String decisionReason;
    private final UUID evaluatedBy;
    private final Instant openedAt;
    private final Instant closedAt;

    public boolean isActive() {
        return closedAt == null;
    }

    /**
     * Cierra este estado en el instante dado.
     * Retorna una nueva instancia inmutable con {@code closedAt} establecido.
     */
    public LoanApplicationStatusEntry close(Instant at) {
        return LoanApplicationStatusEntry.builder()
            .id(this.id)
            .loanApplicationId(this.loanApplicationId)
            .status(this.status)
            .decisionReason(this.decisionReason)
            .evaluatedBy(this.evaluatedBy)
            .openedAt(this.openedAt)
            .closedAt(at)
            .build();
    }

    /**
     * Factory: crea una entrada inicial (PENDING_REVIEW) para una nueva solicitud.
     */
    public static LoanApplicationStatusEntry initialEntry(UUID loanApplicationId) {
        return LoanApplicationStatusEntry.builder()
            .loanApplicationId(loanApplicationId)
            .status(LoanApplicationStatus.PENDING_REVIEW)
            .openedAt(Instant.now())
            .build();
    }

    /**
     * Factory: crea una nueva entrada de transición de estado.
     */
    public static LoanApplicationStatusEntry transitionTo(UUID loanApplicationId,
                                                          LoanApplicationStatus status,
                                                          String decisionReason,
                                                          UUID evaluatedBy) {
        return LoanApplicationStatusEntry.builder()
            .loanApplicationId(loanApplicationId)
            .status(status)
            .decisionReason(decisionReason)
            .evaluatedBy(evaluatedBy)
            .openedAt(Instant.now())
            .build();
    }
}
