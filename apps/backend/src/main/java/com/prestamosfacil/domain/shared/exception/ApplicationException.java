package com.prestamosfacil.domain.shared.exception;

import com.prestamosfacil.domain.shared.enums.Messages;

public class ApplicationException extends RuntimeException {
    public ApplicationException(String message) {
        super(message);
    }

    public ApplicationException(Messages message) {
        super(message.getValue());
    }

    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApplicationException(Messages message, Throwable cause) {
        super(message.getValue(), cause);
    }
}
