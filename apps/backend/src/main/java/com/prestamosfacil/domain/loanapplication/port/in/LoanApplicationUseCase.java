package com.prestamosfacil.domain.loanapplication.port.in;

import com.prestamosfacil.domain.loanapplication.enums.ManualDecision;
import com.prestamosfacil.domain.loanapplication.models.AutomaticEvaluationOutcome;
import com.prestamosfacil.domain.loanapplication.models.LoanApplication;
import com.prestamosfacil.domain.shared.PageResult;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface LoanApplicationUseCase {

    LoanApplication createApplication(UUID customerId, UUID loanTypeId,
                                      BigDecimal requestedAmount, int termInMonths);

    Optional<LoanApplication> findById(UUID id);

    Optional<LoanApplication> findByIdForUser(UUID applicationId, UUID customerId);

    PageResult<LoanApplication> findAll(int page, int perPage, String sortBy, String sortDir);

    PageResult<LoanApplication> findByFilters(UUID customerId, String statusCode, String search, int page, int perPage, String sortBy, String sortDir);

    LoanApplication manualDecision(UUID applicationId, ManualDecision decision, String reason);

    LoanApplication approve(UUID id);

    LoanApplication reject(UUID id, String reason);

    AutomaticEvaluationOutcome evaluateAutomatically(UUID applicationId);
}
