package com.prestamosfacil.domain.customer.models;

import com.prestamosfacil.domain.shared.ValueObject;
import com.prestamosfacil.domain.shared.enums.Messages;

public record PhoneNumber(String countryCode, String number) implements ValueObject {

    public PhoneNumber {
        if (countryCode == null || countryCode.isBlank()) {
            throw new IllegalArgumentException(Messages.PHONE_COUNTRY_CODE_REQUIRED.getValue());
        }
        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException(Messages.PHONE_NUMBER_REQUIRED.getValue());
        }
        countryCode = countryCode.trim().replaceAll("^\\+", "");
        number = number.trim().replaceAll("\\s+", "");
    }

    public static PhoneNumber empty() {
        return new PhoneNumber("00", "000000000");
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getNumber() {
        return number;
    }

    public String getFullNumber() {
        return "+" + countryCode + number;
    }

    @Override
    public String toString() {
        return getFullNumber();
    }
}
