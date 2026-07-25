package com.prestamosfacil.domain.shared;

import com.prestamosfacil.domain.shared.enums.Messages;
import java.util.regex.Pattern;

public record EmailAddress(String value) implements ValueObject {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public EmailAddress {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(Messages.EMAIL_REQUIRED.getValue());
        }
        value = value.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(Messages.EMAIL_INVALID_FORMAT.format(value));
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
