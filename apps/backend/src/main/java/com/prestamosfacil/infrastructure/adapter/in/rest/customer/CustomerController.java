package com.prestamosfacil.infrastructure.adapter.in.rest.customer;

import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.customer.port.in.CustomerUseCase;
import com.prestamosfacil.domain.shared.PageResult;
import com.prestamosfacil.domain.shared.enums.Messages;
import com.prestamosfacil.domain.shared.exception.NotFoundException;
import com.prestamosfacil.domain.user.enums.UserType;
import com.prestamosfacil.infrastructure.adapter.in.rest.customer.dto.request.CreateCustomerRequest;
import com.prestamosfacil.infrastructure.adapter.in.rest.customer.dto.response.CustomerResponse;
import com.prestamosfacil.infrastructure.adapter.in.rest.customer.mapper.CustomerResponseMapper;
import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.SwaggerDocs;
import com.prestamosfacil.infrastructure.security.principal.AuthPrincipal;
import com.prestamosfacil.infrastructure.shared.rest.ApiResponse;
import com.prestamosfacil.infrastructure.shared.rest.PaginatedResponse;
import com.prestamosfacil.infrastructure.adapter.in.rest.shared.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
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
@RequestMapping("/api/v1/customers")
@Tag(name = SwaggerDocs.TAG_CUSTOMERS, description = SwaggerDocs.TAG_CUSTOMERS_DESC)
public class CustomerController {

    private final CustomerUseCase customerUseCase;
    private final CustomerResponseMapper customerResponseMapper;

    public CustomerController(CustomerUseCase customerUseCase,
                              CustomerResponseMapper customerResponseMapper) {
        this.customerUseCase = customerUseCase;
        this.customerResponseMapper = customerResponseMapper;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CUSTOMER_CREATE')")
    @Operation(summary = SwaggerDocs.OP_CUSTOMER_CREATE_SUM, description = SwaggerDocs.OP_CUSTOMER_CREATE_DESC)
    public ResponseEntity<ApiResponse<CustomerResponse>> create(@Valid @RequestBody CreateCustomerRequest request) {
        Customer customer = customerUseCase.createCustomer(
            request.firstName(), request.lastName(), request.email(),
            request.documentType(), request.documentNumber(),
            request.baseSalary());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(customerResponseMapper.toResponse(customer), Messages.SUCCESS_CUSTOMER_CREATED));
    }

    @GetMapping("/{id}")
    @Operation(summary = SwaggerDocs.OP_CUSTOMER_FIND_BY_ID_SUM, description = SwaggerDocs.OP_CUSTOMER_FIND_BY_ID_DESC)
    public ResponseEntity<ApiResponse<CustomerResponse>> findById(
            @AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID id) {
        if (principal != null && principal.userType() == UserType.CUSTOMER) {
            var self = findCustomerByUserId(principal.userId())
                .orElseThrow(() -> new NotFoundException(Messages.CUSTOMER_NOT_FOUND.getValue()));
            if (!self.getId().equals(id)) {
                throw new NotFoundException(Messages.CUSTOMER_NOT_FOUND.getValue());
            }
        }
        return customerUseCase.findById(id)
            .map(c -> ResponseEntity.ok(ApiResponse.success(customerResponseMapper.toResponse(c))))
            .orElseThrow(() -> new NotFoundException(Messages.CUSTOMER_NOT_FOUND.getValue()));
    }

    @GetMapping
    @Operation(summary = SwaggerDocs.OP_CUSTOMER_FIND_ALL_SUM, description = SwaggerDocs.OP_CUSTOMER_FIND_ALL_DESC)
    public ResponseEntity<PaginatedResponse<CustomerResponse>> findAll(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(defaultValue = "0") @jakarta.validation.constraints.Min(0) int page,
            @RequestParam(defaultValue = "20") @jakarta.validation.constraints.Min(1) @jakarta.validation.constraints.Max(100) int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        if (principal != null && principal.userType() == UserType.CUSTOMER) {
            var self = findCustomerByUserId(principal.userId())
                .orElseThrow(() -> new NotFoundException(Messages.CUSTOMER_NOT_FOUND.getValue()));
            var content = java.util.List.of(customerResponseMapper.toResponse(self));
            var pageResult = new PageResult<>(content, 0, 1, 1);
            return ResponseEntity.ok(ResponseHelper.paginated(pageResult, PageRequest.of(0, 1)));
        }
        PageResult<Customer> result = search != null
            ? customerUseCase.findBySearch(search, page, size, sortBy, sortDir)
            : customerUseCase.findAll(page, size, sortBy, sortDir);
        var content = result.getContent().stream().map(customerResponseMapper::toResponse).toList();
        var pageResult = new PageResult<>(content, page, size, result.getTotal());
        return ResponseEntity.ok(ResponseHelper.paginated(pageResult, PageRequest.of(page, size)));
    }

    private Optional<Customer> findCustomerByUserId(UUID userId) {
        return customerUseCase.findByUserId(userId);
    }
}
