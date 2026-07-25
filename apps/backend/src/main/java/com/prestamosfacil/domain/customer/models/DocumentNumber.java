package com.prestamosfacil.domain.customer.models;

import com.prestamosfacil.domain.shared.ValueObject;
import com.prestamosfacil.domain.shared.enums.Messages;

public record DocumentNumber(String type, String number) implements ValueObject {

    public DocumentNumber {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException(Messages.DOCUMENT_TYPE_REQUIRED.getValue());
        }
        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException(Messages.DOCUMENT_NUMBER_REQUIRED.getValue());
        }
        type = type.trim().toUpperCase();
        number = number.trim();
    }

    public String getType() {
        return type;
    }

    public String getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return type + " " + number;
    }
}
