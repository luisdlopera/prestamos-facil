package com.prestamosfacil.domain.user.utils;

import com.prestamosfacil.domain.shared.enums.Messages;
import com.prestamosfacil.domain.user.constants.UserConstants;
import java.util.regex.Pattern;

public final class PasswordPolicy {

    private static final Pattern STRONG_PASSWORD =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$");

    private PasswordPolicy() {}

    public static void validate(String password) {
        if (password == null || password.length() < UserConstants.PASSWORD_MIN_LENGTH) {
            throw new IllegalArgumentException(Messages.AUTH_PASSWORD_TOO_SHORT.getValue());
        }
        if (!STRONG_PASSWORD.matcher(password).matches()) {
            throw new IllegalArgumentException(Messages.AUTH_PASSWORD_WEAK.getValue());
        }
    }
}
