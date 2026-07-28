package com.prestamosfacil.infrastructure.adapter.in.rest.reports;

import com.prestamosfacil.domain.reporting.models.ApprovedLoansTotal;
import com.prestamosfacil.domain.reporting.port.out.ApprovedLoanReportPort;
import com.prestamosfacil.infrastructure.adapter.in.rest.reports.dto.response.ApprovedLoansTotalResponse;
import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.SwaggerDocs;
import com.prestamosfacil.infrastructure.shared.rest.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = SwaggerDocs.TAG_REPORTS, description = SwaggerDocs.TAG_REPORTS_DESC)
public class ReportsController {

    private final ApprovedLoanReportPort approvedLoanReportPort;

    public ReportsController(ApprovedLoanReportPort approvedLoanReportPort) {
        this.approvedLoanReportPort = approvedLoanReportPort;
    }

    @GetMapping("/approved-loans/total")
    @PreAuthorize("hasAuthority('REPORT_APPROVED_LOANS_READ')")
    @Operation(summary = SwaggerDocs.OP_REPORT_APPROVED_TOTAL_SUM, description = SwaggerDocs.OP_REPORT_APPROVED_TOTAL_DESC)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = SwaggerDocs.RESP_REPORT_APPROVED_200)
    public ResponseEntity<ApiResponse<ApprovedLoansTotalResponse>> getTotalApproved() {
        ApprovedLoansTotal summary = approvedLoanReportPort.getTotalApproved();
        return ResponseEntity.ok(ApiResponse.success(new ApprovedLoansTotalResponse(
            summary.totalApprovedAmount(),
            summary.approvedLoansCount(),
            summary.generatedAt()
        )));
    }
}
