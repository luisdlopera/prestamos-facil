package com.prestamosfacil.domain.loantype.enums;

public enum RateType {
    EA("Efectiva Anual");

    private final String description;

    RateType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static RateType fromString(String value) {
        if (value == null || value.isBlank()) {
            return EA;
        }
        for (RateType type : RateType.values()) {
            if (type.name().equalsIgnoreCase(value.trim())) {
                return type;
            }
        }
        return EA;
    }
}
