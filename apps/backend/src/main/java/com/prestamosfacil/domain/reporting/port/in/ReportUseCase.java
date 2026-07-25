package com.prestamosfacil.domain.reporting.port.in;

import com.prestamosfacil.domain.reporting.models.ApprovedLoanSummary;

public interface ReportUseCase {

    ApprovedLoanSummary getApprovedLoansSummary();
}
