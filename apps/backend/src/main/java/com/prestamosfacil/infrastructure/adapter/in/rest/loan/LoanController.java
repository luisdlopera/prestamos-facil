package com.prestamosfacil.infrastructure.adapter.in.rest.loan;

import com.prestamosfacil.domain.loan.port.in.LoanUseCase;
import com.prestamosfacil.domain.paymentplan.port.in.PaymentPlanUseCase;
import com.prestamosfacil.domain.loan.models.Loan;
import com.prestamosfacil.domain.paymentplan.models.PaymentInstallment;
import com.prestamosfacil.domain.shared.PageResult;
import com.prestamosfacil.domain.shared.enums.Messages;
import com.prestamosfacil.domain.shared.exception.NotFoundException;
import com.prestamosfacil.domain.user.enums.UserType;
import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.SwaggerDocs;
import com.prestamosfacil.infrastructure.shared.rest.ApiResponse;
import com.prestamosfacil.infrastructure.shared.rest.PaginatedResponse;
import com.prestamosfacil.infrastructure.adapter.in.rest.shared.CustomerContextResolver;
import com.prestamosfacil.infrastructure.adapter.in.rest.shared.ResponseHelper;
import com.prestamosfacil.infrastructure.adapter.in.rest.loan.dto.response.LoanResponse;
import com.prestamosfacil.infrastructure.adapter.in.rest.loan.mapper.LoanResponseMapper;
import com.prestamosfacil.infrastructure.adapter.in.rest.loan.dto.response.PaymentInstallmentResponse;
import com.prestamosfacil.infrastructure.security.principal.AuthPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/loans")
@Validated
@Tag(name = SwaggerDocs.TAG_LOANS, description = SwaggerDocs.TAG_LOANS_DESC)
public class LoanController {

    private final LoanUseCase loanUseCase;
    private final PaymentPlanUseCase paymentPlanUseCase;
    private final LoanResponseMapper loanResponseMapper;
    private final CustomerContextResolver customerContextResolver;

    public LoanController(LoanUseCase loanUseCase,
                          PaymentPlanUseCase paymentPlanUseCase,
                          LoanResponseMapper loanResponseMapper,
                          CustomerContextResolver customerContextResolver) {
        this.loanUseCase = loanUseCase;
        this.paymentPlanUseCase = paymentPlanUseCase;
        this.loanResponseMapper = loanResponseMapper;
        this.customerContextResolver = customerContextResolver;
    }

    @GetMapping
    @Operation(summary = SwaggerDocs.OP_LOAN_FIND_ALL_SUM, description = SwaggerDocs.OP_LOAN_FIND_ALL_DESC)
    public ResponseEntity<PaginatedResponse<LoanResponse>> findAll(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(defaultValue = "0") @jakarta.validation.constraints.Min(0) int page,
            @RequestParam(defaultValue = "20") @jakarta.validation.constraints.Min(1) @jakarta.validation.constraints.Max(100) int size,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        UUID effectiveCustomerId = (principal != null && principal.userType() == UserType.CUSTOMER)
            ? customerContextResolver.resolveCustomerId(principal)
            : customerId;
        PageResult<Loan> result = (effectiveCustomerId != null || search != null)
            ? loanUseCase.findByFilters(effectiveCustomerId, search, page, size, sortBy, sortDir)
            : loanUseCase.findAll(page, size, sortBy, sortDir);
        var content = result.getContent().stream().map(loanResponseMapper::toResponse).toList();
        var pageResult = new PageResult<>(content, page, size, result.getTotal());
        return ResponseEntity.ok(ResponseHelper.paginated(pageResult, PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = SwaggerDocs.OP_LOAN_FIND_BY_ID_SUM, description = SwaggerDocs.OP_LOAN_FIND_BY_ID_DESC)
    public ResponseEntity<ApiResponse<LoanResponse>> findById(
            @AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID id) {
        var loan = (principal != null && principal.userType() == UserType.CUSTOMER)
            ? loanUseCase.findByIdForUser(id, customerContextResolver.resolveCustomerId(principal))
            : loanUseCase.findById(id);
        return loan
            .map(l -> ResponseEntity.ok(ApiResponse.success(loanResponseMapper.toResponse(l))))
            .orElseThrow(() -> new NotFoundException(Messages.LOAN_NOT_FOUND.getValue()));
    }

    @GetMapping("/{id}/payment-plan")
    @Operation(summary = SwaggerDocs.OP_LOAN_PAYMENT_PLAN_SUM, description = SwaggerDocs.OP_LOAN_PAYMENT_PLAN_DESC)
    public ResponseEntity<ApiResponse<List<PaymentInstallmentResponse>>> getPaymentPlan(
            @AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID id) {
        if (principal != null && principal.userType() == UserType.CUSTOMER) {
            loanUseCase.findByIdForUser(id, customerContextResolver.resolveCustomerId(principal))
                .orElseThrow(() -> new NotFoundException(Messages.LOAN_NOT_FOUND.getValue()));
        }
        List<PaymentInstallment> installments = paymentPlanUseCase.getPaymentPlan(id);
        List<PaymentInstallmentResponse> response = installments.stream()
            .map(loanResponseMapper::toInstallmentResponse).toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
