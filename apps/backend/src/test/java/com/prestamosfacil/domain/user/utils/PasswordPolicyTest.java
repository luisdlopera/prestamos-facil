package com.prestamosfacil.domain.user.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordPolicyTest {

    @Test
    void shouldValidateStrongPassword() {
        assertDoesNotThrow(() -> PasswordPolicy.validate("Strong1@Pass"));
    }

    @Test
    void shouldAcceptPasswordWithAllRequirements() {
        assertDoesNotThrow(() -> PasswordPolicy.validate("Test@1234"));
    }

    @Test
    void shouldThrowOnNullPassword() {
        assertThrows(IllegalArgumentException.class,
            () -> PasswordPolicy.validate(null));
    }

    @Test
    void shouldThrowOnTooShortPassword() {
        assertThrows(IllegalArgumentException.class,
            () -> PasswordPolicy.validate("Ab1@"));
    }

    @Test
    void shouldThrowOnPasswordWithoutUppercase() {
        assertThrows(IllegalArgumentException.class,
            () -> PasswordPolicy.validate("lowercase1@"));
    }

    @Test
    void shouldThrowOnPasswordWithoutLowercase() {
        assertThrows(IllegalArgumentException.class,
            () -> PasswordPolicy.validate("UPPERCASE1@"));
    }

    @Test
    void shouldThrowOnPasswordWithoutDigit() {
        assertThrows(IllegalArgumentException.class,
            () -> PasswordPolicy.validate("NoDigits@!"));
    }

    @Test
    void shouldThrowOnPasswordWithoutSpecialChar() {
        assertThrows(IllegalArgumentException.class,
            () -> PasswordPolicy.validate("NoSpecial1"));
    }

    @Test
    void shouldThrowOnSevenCharPassword() {
        assertThrows(IllegalArgumentException.class,
            () -> PasswordPolicy.validate("Ab1@567"));
    }
}
