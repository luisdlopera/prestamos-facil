package com.prestamosfacil.infrastructure.adapter.in.rest.loantype;

import com.prestamosfacil.domain.loantype.models.LoanType;
import com.prestamosfacil.domain.loantype.port.in.LoanTypeUseCase;
import com.prestamosfacil.domain.shared.PageResult;
import com.prestamosfacil.domain.shared.enums.Messages;
import com.prestamosfacil.domain.shared.exception.NotFoundException;
import com.prestamosfacil.infrastructure.adapter.in.rest.loantype.dto.request.CreateLoanTypeRequest;
import com.prestamosfacil.infrastructure.adapter.in.rest.loantype.dto.response.LoanTypeResponse;
import com.prestamosfacil.infrastructure.adapter.in.rest.loantype.mapper.LoanTypeResponseMapper;
import com.prestamosfacil.infrastructure.adapter.in.rest.loantype.dto.request.ReorderLoanTypesRequest;
import com.prestamosfacil.infrastructure.adapter.in.rest.loantype.dto.request.ToggleLoanTypeStatusRequest;
import com.prestamosfacil.infrastructure.adapter.in.rest.loantype.dto.request.UpdateLoanTypeRequest;
import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.SwaggerDocs;
import com.prestamosfacil.infrastructure.shared.rest.ApiResponse;
import com.prestamosfacil.infrastructure.shared.rest.PaginatedResponse;
import com.prestamosfacil.infrastructure.adapter.in.rest.shared.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/loan-types")
@Tag(name = SwaggerDocs.TAG_LOAN_TYPES, description = SwaggerDocs.TAG_LOAN_TYPES_DESC)
public class LoanTypeController {

    private final LoanTypeUseCase loanTypeUseCase;
    private final LoanTypeResponseMapper loanTypeResponseMapper;

    public LoanTypeController(LoanTypeUseCase loanTypeUseCase,
                               LoanTypeResponseMapper loanTypeResponseMapper) {
        this.loanTypeUseCase = loanTypeUseCase;
        this.loanTypeResponseMapper = loanTypeResponseMapper;
    }

    @GetMapping
    @Operation(summary = SwaggerDocs.OP_LOAN_TYPE_FIND_ALL_ACTIVE_SUM, description = SwaggerDocs.OP_LOAN_TYPE_FIND_ALL_ACTIVE_DESC)
    public ResponseEntity<ApiResponse<List<LoanTypeResponse>>> findAllActive() {
        var loanTypes = loanTypeUseCase.findAllActive();
        return ResponseEntity.ok(ApiResponse.success(loanTypeResponseMapper.toResponseList(loanTypes)));
    }

    @GetMapping("/admin")
    @Operation(summary = SwaggerDocs.OP_LOAN_TYPE_FIND_ALL_ADMIN_SUM, description = SwaggerDocs.OP_LOAN_TYPE_FIND_ALL_ADMIN_DESC)
    public ResponseEntity<PaginatedResponse<LoanTypeResponse>> findAllAdmin(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") @jakarta.validation.constraints.Min(0) int page,
            @RequestParam(defaultValue = "10") @jakarta.validation.constraints.Min(1) @jakarta.validation.constraints.Max(100) int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        PageResult<LoanType> result = loanTypeUseCase.findAllAdmin(search, active, page, size, sortBy, sortDir);
        List<LoanTypeResponse> content = loanTypeResponseMapper.toResponseList(result.getContent());
        PageResult<LoanTypeResponse> pageResult = new PageResult<>(content, page, size, result.getTotal());
        return ResponseEntity.ok(ResponseHelper.paginated(pageResult, PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = SwaggerDocs.OP_LOAN_TYPE_FIND_BY_ID_SUM, description = SwaggerDocs.OP_LOAN_TYPE_FIND_BY_ID_DESC)
    public ResponseEntity<ApiResponse<LoanTypeResponse>> findById(@PathVariable UUID id) {
        return loanTypeUseCase.findById(id)
            .map(loanType -> ResponseEntity.ok(ApiResponse.success(loanTypeResponseMapper.toResponse(loanType))))
            .orElseThrow(() -> new NotFoundException(Messages.LOAN_TYPE_NOT_FOUND.getValue()));
    }

    @PostMapping
    @Operation(summary = SwaggerDocs.OP_LOAN_TYPE_CREATE_SUM, description = SwaggerDocs.OP_LOAN_TYPE_CREATE_DESC)
    public ResponseEntity<ApiResponse<LoanTypeResponse>> create(@Valid @RequestBody CreateLoanTypeRequest request) {
        LoanType domainInput = loanTypeResponseMapper.toDomain(request);
        LoanType created = loanTypeUseCase.create(domainInput);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(loanTypeResponseMapper.toResponse(created), Messages.SUCCESS_LOAN_TYPE_CREATED));
    }

    @PutMapping("/{id}")
    @Operation(summary = SwaggerDocs.OP_LOAN_TYPE_UPDATE_SUM, description = SwaggerDocs.OP_LOAN_TYPE_UPDATE_DESC)
    public ResponseEntity<ApiResponse<LoanTypeResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLoanTypeRequest request) {
        LoanType domainInput = loanTypeResponseMapper.toDomain(id, request);
        LoanType updated = loanTypeUseCase.update(id, domainInput);
        return ResponseEntity.ok(ApiResponse.success(loanTypeResponseMapper.toResponse(updated), Messages.SUCCESS_LOAN_TYPE_UPDATED));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = SwaggerDocs.OP_LOAN_TYPE_TOGGLE_STATUS_SUM, description = SwaggerDocs.OP_LOAN_TYPE_TOGGLE_STATUS_DESC)
    public ResponseEntity<ApiResponse<LoanTypeResponse>> toggleStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ToggleLoanTypeStatusRequest request) {
        LoanType updated = loanTypeUseCase.toggleStatus(id, request.active());
        String msg = request.active() ? Messages.LOAN_TYPE_STATUS_ACTIVATED.getValue() : Messages.LOAN_TYPE_STATUS_DEACTIVATED.getValue();
        return ResponseEntity.ok(ApiResponse.success(loanTypeResponseMapper.toResponse(updated), msg));
    }

    @PostMapping("/reorder")
    @Operation(summary = SwaggerDocs.OP_LOAN_TYPE_REORDER_SUM, description = SwaggerDocs.OP_LOAN_TYPE_REORDER_DESC)
    public ResponseEntity<ApiResponse<Void>> reorder(@Valid @RequestBody ReorderLoanTypesRequest request) {
        loanTypeUseCase.reorder(request.orderedIds());
        return ResponseEntity.ok(ApiResponse.success(null, Messages.SUCCESS_LOAN_TYPE_REORDERED.getValue()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = SwaggerDocs.OP_LOAN_TYPE_DELETE_SUM, description = SwaggerDocs.OP_LOAN_TYPE_DELETE_DESC)
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        loanTypeUseCase.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, Messages.SUCCESS_LOAN_TYPE_DELETED.getValue()));
    }
}
