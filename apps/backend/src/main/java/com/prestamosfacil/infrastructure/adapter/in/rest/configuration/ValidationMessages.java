package com.prestamosfacil.infrastructure.adapter.in.rest.configuration;

public final class ValidationMessages {

    private ValidationMessages() {}

    // ========== Loan Type ==========
    public static final String LOAN_TYPE_NAME_REQUIRED = "El nombre es obligatorio";
    public static final String LOAN_TYPE_INTEREST_RATE_REQUIRED = "La tasa de interés es obligatoria";
    public static final String LOAN_TYPE_INTEREST_RATE_NEGATIVE = "La tasa de interés no puede ser negativa";
    public static final String LOAN_TYPE_INTEREST_RATE_MAX = "La tasa de interés no puede ser superior a 100.00%";
    public static final String LOAN_TYPE_RATE_TYPE_REQUIRED = "El tipo de tasa es obligatorio (EA)";
    public static final String LOAN_TYPE_MIN_AMOUNT_REQUIRED = "El monto mínimo es obligatorio";
    public static final String LOAN_TYPE_MIN_AMOUNT_POSITIVE = "El monto mínimo debe ser mayor a 0";
    public static final String LOAN_TYPE_MAX_AMOUNT_REQUIRED = "El monto máximo es obligatorio";
    public static final String LOAN_TYPE_MAX_AMOUNT_POSITIVE = "El monto máximo debe ser mayor a 0";
    public static final String LOAN_TYPE_MIN_TERM_REQUIRED = "El plazo mínimo debe ser al menos de 1 mes";
    public static final String LOAN_TYPE_MAX_TERM_REQUIRED = "El plazo máximo debe ser al menos de 1 mes";
    public static final String LOAN_TYPE_DISPLAY_ORDER_MIN = "El orden de visualización debe ser mayor o igual a 0";
    public static final String LOAN_TYPE_STATUS_REQUIRED = "El estado activo/inactivo es obligatorio";
    public static final String LOAN_TYPE_ORDERED_IDS_REQUIRED = "La lista de IDs ordenados no puede estar vacía";

    // ========== Auth ==========
    public static final String PASSWORD_PATTERN = "Debe incluir mayuscula, minuscula, digito y caracter especial (@#$%^&+=!)";
    public static final String FIRST_NAME_REQUIRED = "El nombre es obligatorio";
    public static final String LAST_NAME_REQUIRED = "Los apellidos son obligatorios";
    public static final String EMAIL_REQUIRED = "El correo electrónico es obligatorio";
    public static final String EMAIL_INVALID = "El formato del correo electrónico no es válido";
    public static final String DOCUMENT_TYPE_REQUIRED = "El tipo de documento es obligatorio";
    public static final String DOCUMENT_NUMBER_REQUIRED = "El número de documento es obligatorio";
    public static final String BASE_SALARY_REQUIRED = "El salario base es obligatorio";
    public static final String BASE_SALARY_RANGE = "El salario debe estar entre 0 y 15.000.000";
}
