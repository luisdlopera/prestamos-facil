package com.prestamosfacil.infrastructure.adapter.in.rest.loanapplication;

import com.prestamosfacil.domain.loanapplication.models.AutomaticEvaluationOutcome;
import com.prestamosfacil.domain.loanapplication.models.LoanApplication;
import com.prestamosfacil.domain.loanapplication.port.in.LoanApplicationUseCase;
import com.prestamosfacil.domain.shared.PageResult;
import com.prestamosfacil.domain.shared.enums.Messages;
import com.prestamosfacil.domain.shared.exception.NotFoundException;
import com.prestamosfacil.domain.user.enums.UserType;
import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.SwaggerDocs;
import com.prestamosfacil.infrastructure.shared.rest.ApiResponse;
import com.prestamosfacil.infrastructure.shared.rest.PaginatedResponse;
import com.prestamosfacil.infrastructure.adapter.in.rest.shared.CustomerContextResolver;
import com.prestamosfacil.infrastructure.adapter.in.rest.shared.ResponseHelper;
import com.prestamosfacil.infrastructure.adapter.in.rest.loanapplication.dto.response.AutomaticEvaluationResponse;
import com.prestamosfacil.infrastructure.adapter.in.rest.loanapplication.dto.request.CreateLoanApplicationRequest;
import com.prestamosfacil.infrastructure.adapter.in.rest.loanapplication.dto.response.LoanApplicationResponse;
import com.prestamosfacil.infrastructure.adapter.in.rest.loanapplication.mapper.LoanApplicationResponseMapper;
import com.prestamosfacil.infrastructure.adapter.in.rest.loanapplication.dto.request.RejectRequest;
import com.prestamosfacil.infrastructure.security.principal.AuthPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/loan-applications")
@Validated
@Tag(name = SwaggerDocs.TAG_LOAN_APPLICATIONS, description = SwaggerDocs.TAG_LOAN_APPLICATIONS_DESC)
public class LoanApplicationController {

    private final LoanApplicationUseCase loanApplicationUseCase;
    private final LoanApplicationResponseMapper loanApplicationResponseMapper;
    private final CustomerContextResolver customerContextResolver;

    public LoanApplicationController(LoanApplicationUseCase loanApplicationUseCase,
                                     LoanApplicationResponseMapper loanApplicationResponseMapper,
                                     CustomerContextResolver customerContextResolver) {
        this.loanApplicationUseCase = loanApplicationUseCase;
        this.loanApplicationResponseMapper = loanApplicationResponseMapper;
        this.customerContextResolver = customerContextResolver;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('LOAN_APPLICATION_CREATE_SELF','LOAN_APPLICATION_CREATE_FOR_CUSTOMER')")
    @Operation(summary = SwaggerDocs.OP_LOAN_APP_CREATE_SUM, description = SwaggerDocs.OP_LOAN_APP_CREATE_DESC)
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> create(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody CreateLoanApplicationRequest request) {
        UUID customerId = resolveCustomerId(principal, request.customerId());
        LoanApplication application = loanApplicationUseCase.createApplication(
            customerId,
            request.loanTypeId(),
            request.requestedAmount(),
            request.termInMonths());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(loanApplicationResponseMapper.toResponse(application), Messages.SUCCESS_LOAN_APPLICATION_CREATED));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('LOAN_APPLICATION_READ_SELF','LOAN_APPLICATION_READ_ALL')")
    @Operation(summary = SwaggerDocs.OP_LOAN_APP_FIND_ALL_SUM, description = SwaggerDocs.OP_LOAN_APP_FIND_ALL_DESC)
    public ResponseEntity<PaginatedResponse<LoanApplicationResponse>> findAll(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(defaultValue = "0") @jakarta.validation.constraints.Min(0) int page,
            @RequestParam(defaultValue = "20") @jakarta.validation.constraints.Min(1) @jakarta.validation.constraints.Max(100) int size,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        UUID effectiveCustomerId = (principal != null && principal.userType() == UserType.CUSTOMER)
            ? resolveCustomerId(principal, null)
            : customerId;
        PageResult<LoanApplication> result = effectiveCustomerId != null || status != null || search != null
            ? loanApplicationUseCase.findByFilters(effectiveCustomerId, status, search, page, size, sortBy, sortDir)
            : loanApplicationUseCase.findAll(page, size, sortBy, sortDir);
        var content = result.getContent().stream().map(loanApplicationResponseMapper::toResponse).toList();
        var pageResult = new PageResult<>(content, page, size, result.getTotal());
        return ResponseEntity.ok(ResponseHelper.paginated(pageResult, PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('LOAN_APPLICATION_READ_SELF','LOAN_APPLICATION_READ_ALL')")
    @Operation(summary = SwaggerDocs.OP_LOAN_APP_FIND_BY_ID_SUM, description = SwaggerDocs.OP_LOAN_APP_FIND_BY_ID_DESC)
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> findById(
            @AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID id) {
        var app = (principal != null && principal.userType() == UserType.CUSTOMER)
            ? loanApplicationUseCase.findByIdForUser(id, resolveCustomerId(principal, null))
            : loanApplicationUseCase.findById(id);
        return app
            .map(a -> ResponseEntity.ok(ApiResponse.success(loanApplicationResponseMapper.toResponse(a))))
            .orElseThrow(() -> new NotFoundException(Messages.LOAN_APPLICATION_NOT_FOUND.getValue()));
    }

    @PostMapping("/{id}/automatic-evaluation")
    @PreAuthorize("hasAuthority('LOAN_APPLICATION_EVALUATE')")
    @Operation(summary = SwaggerDocs.OP_LOAN_APP_AUTO_EVAL_SUM, description = SwaggerDocs.OP_LOAN_APP_AUTO_EVAL_DESC)
    public ResponseEntity<ApiResponse<AutomaticEvaluationResponse>> automaticEvaluation(@PathVariable UUID id) {
        AutomaticEvaluationOutcome outcome = loanApplicationUseCase.evaluateAutomatically(id);
        return ResponseEntity.ok(ApiResponse.success(new AutomaticEvaluationResponse(
            id, outcome.status().name(), outcome.maxCapacity(), outcome.currentDebt(),
            outcome.newInstallment(), outcome.debtRatio(), outcome.reason())));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('LOAN_APPLICATION_APPROVE')")
    @Operation(summary = SwaggerDocs.OP_LOAN_APP_APPROVE_SUM, description = SwaggerDocs.OP_LOAN_APP_APPROVE_DESC)
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> approve(@PathVariable UUID id) {
        LoanApplication application = loanApplicationUseCase.approve(id);
        return ResponseEntity.ok(ApiResponse.success(loanApplicationResponseMapper.toResponse(application)));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('LOAN_APPLICATION_REJECT')")
    @Operation(summary = SwaggerDocs.OP_LOAN_APP_REJECT_SUM, description = SwaggerDocs.OP_LOAN_APP_REJECT_DESC)
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> reject(
            @PathVariable UUID id, @Valid @RequestBody RejectRequest request) {
        LoanApplication application = loanApplicationUseCase.reject(id, request.reason());
        return ResponseEntity.ok(ApiResponse.success(loanApplicationResponseMapper.toResponse(application)));
    }

    private UUID resolveCustomerId(AuthPrincipal principal, UUID requestCustomerId) {
        if (principal != null && principal.userType() == UserType.CUSTOMER) {
            return customerContextResolver.resolveCustomerId(principal);
        }
        if (requestCustomerId != null) {
            customerContextResolver.validateCustomerExists(requestCustomerId);
            return requestCustomerId;
        }
        throw new IllegalArgumentException(Messages.LOAN_APPLICATION_CUSTOMER_ID_REQUIRED.getValue());
    }
}
