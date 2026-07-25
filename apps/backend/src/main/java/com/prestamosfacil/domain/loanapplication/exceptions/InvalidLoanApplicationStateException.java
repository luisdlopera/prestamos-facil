package com.prestamosfacil.domain.loanapplication.exceptions;

public class InvalidLoanApplicationStateException extends RuntimeException {
    public InvalidLoanApplicationStateException(String message) {
        super(message);
    }
}
