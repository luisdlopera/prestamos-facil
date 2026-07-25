package com.prestamosfacil.domain.shared.exception;

import com.prestamosfacil.domain.shared.enums.Messages;

/** Signals an authentication failure that must be exposed as HTTP 401. */
public class UnauthorizedApplicationException extends ApplicationException {
    public UnauthorizedApplicationException(Messages message) {
        super(message);
    }
}
