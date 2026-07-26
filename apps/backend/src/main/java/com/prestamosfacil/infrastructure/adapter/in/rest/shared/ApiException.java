package com.prestamosfacil.infrastructure.adapter.in.rest.shared;

import com.prestamosfacil.domain.shared.exception.ApplicationException;
import org.springframework.http.HttpStatus;

public class ApiException extends ApplicationException {
    private final HttpStatus status;

    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }
}
