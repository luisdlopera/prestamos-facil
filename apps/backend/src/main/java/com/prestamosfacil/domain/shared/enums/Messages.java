package com.prestamosfacil.domain.shared.enums;

public enum Messages {

    // ========== Auth ==========
    AUTH_INVALID_CREDENTIALS("Credenciales inválidas"),
    AUTH_ACCOUNT_BLOCKED("Cuenta bloqueada por múltiples intentos fallidos. Intente de nuevo más tarde."),
    AUTH_EMAIL_EXISTS("El email ya está registrado: %s"),
    AUTH_DOCUMENT_EXISTS("El número de documento ya está registrado: %s"),
    AUTH_PASSWORD_TOO_SHORT("La contraseña debe tener al menos 8 caracteres"),
    AUTH_PASSWORD_WEAK("La contraseña debe incluir mayúscula, minúscula, dígito y caracter especial (@#$%^&+=!)"),
    AUTH_SALARY_RANGE("El salario debe estar entre 0 y %s"),
    AUTH_SESSION_EXPIRED("Sesión expirada. Inicie sesión nuevamente."),
    AUTH_SESSION_INVALID("Sesión inválida o expirada"),
    AUTH_TOKEN_INVALID("Token inválido"),
    AUTH_TOKEN_REFRESH_INVALID("Token de refresco inválido o expirado"),
    AUTH_USER_NOT_FOUND("Usuario no encontrado"),
    AUTH_NOT_AUTHENTICATED("No autenticado"),
    AUTH_RATE_LIMITED("Demasiados intentos. Intente de nuevo en 15 minutos."),
    AUTH_CURRENT_PASSWORD_WRONG("Contraseña actual incorrecta"),
    AUTH_EMAIL_ALREADY_STAFF("El correo ya esta registrado como staff"),
    AUTH_STAFF_NOT_FOUND("Staff no encontrado"),

    // ========== Success ==========
    SUCCESS_REGISTER("Registro exitoso"),
    SUCCESS_LOGIN("Inicio de sesión exitoso"),
    SUCCESS_REFRESH("Sesión renovada"),
    SUCCESS_OPERATION("Operación exitosa"),
    SUCCESS_RECORDS_RETRIEVED("Registros recuperados exitosamente"),
    SUCCESS_PROFILE_UPDATED("Perfil actualizado"),
    SUCCESS_PASSWORD_RESET_REQUESTED("Si el correo existe, recibirá un enlace de recuperación"),
    SUCCESS_PASSWORD_RESET_CONFIRMED("Contraseña restablecida exitosamente"),
    SUCCESS_PASSWORD_CHANGED("Contraseña cambiada exitosamente"),
    SUCCESS_CUSTOMER_CREATED("Cliente creado exitosamente"),
    SUCCESS_LOAN_APPLICATION_CREATED("Solicitud creada exitosamente"),
    SUCCESS_LOAN_TYPE_CREATED("Tipo de crédito creado exitosamente"),
    SUCCESS_LOAN_TYPE_UPDATED("Tipo de crédito actualizado exitosamente"),
    SUCCESS_LOAN_TYPE_DELETED("Tipo de crédito eliminado exitosamente"),
    SUCCESS_LOAN_TYPE_REORDERED("Orden actualizado exitosamente"),
    SUCCESS_OK("OK"),
    SUCCESS_STAFF_REGISTERED("Staff registrado"),

    // ========== General / Access ==========
    ACCESS_DENIED("Acceso denegado"),
    ACCESS_ORIGIN_NOT_ALLOWED("Origen no permitido"),
    VALIDATION_ERROR("Error de validación"),
    INTERNAL_SERVER_ERROR("Error interno del servidor"),
    DATA_INTEGRITY_VIOLATION("Los datos no cumplen con las restricciones de integridad (correo o documento duplicado, o valores fuera de rango)"),

    // ========== Customer ==========
    CUSTOMER_NOT_FOUND("Cliente no encontrado"),
    CUSTOMER_NOT_FOUND_ID("Cliente no encontrado: %s"),
    CUSTOMER_REGISTER_ERROR("Error al obtener cliente registrado"),
    CUSTOMER_SALARY_INVALID("El salario base debe estar entre 0 y 15,000,000"),

    // ========== Loan Application ==========
    LOAN_APPLICATION_NOT_FOUND("Solicitud no encontrada"),
    LOAN_APPLICATION_NOT_FOUND_ID("Solicitud no encontrada: %s"),
    LOAN_APPLICATION_CUSTOMER_REQUIRED("El cliente de la solicitud es obligatorio."),
    LOAN_APPLICATION_LOAN_TYPE_REQUIRED("El tipo de crédito es obligatorio."),
    LOAN_APPLICATION_AMOUNT_REQUIRED("El monto solicitado debe ser mayor a cero."),
    LOAN_APPLICATION_MIN_TERM("El plazo en meses debe ser de al menos 1 mes."),
    LOAN_APPLICATION_CANNOT_APPROVE("No se puede aprobar una solicitud en estado: %s"),
    LOAN_APPLICATION_CANNOT_REJECT("No se puede rechazar una solicitud en estado: %s"),
    LOAN_APPLICATION_CANNOT_DECIDE("No se puede decidir una solicitud en estado: %s"),
    LOAN_APPLICATION_CANNOT_MANUAL_REVIEW("Solo se puede marcar para revision manual una solicitud en PENDING_REVIEW"),
    LOAN_APPLICATION_AUTO_EVAL_INVALID_STATE("La evaluación automática solo está disponible para solicitudes en PENDING_REVIEW"),
    LOAN_APPLICATION_UNEXPECTED_SP_DECISION("Decisión inesperada del SP: %s"),
    LOAN_APPLICATION_CUSTOMER_ID_REQUIRED("customerId es requerido para staff/admin"),

    // ========== Loan ==========
    LOAN_NOT_FOUND("Préstamo no encontrado"),
    LOAN_NOT_FOUND_ID("Préstamo no encontrado: %s"),
    LOAN_REQUEST_NOT_APPROVED("La solicitud debe estar aprobada para crear un préstamo"),
    LOAN_ALREADY_EXISTS("Ya existe un préstamo para esta solicitud"),

    // ========== Loan Type ==========
    LOAN_TYPE_NOT_FOUND("Tipo de préstamo no encontrado"),
    LOAN_TYPE_NOT_FOUND_CREDIT("Tipo de crédito no encontrado."),
    LOAN_TYPE_NOT_FOUND_CREDIT_ID("Tipo de crédito no encontrado con el ID especificado."),
    LOAN_TYPE_NOT_FOUND_ID("Tipo de préstamo no encontrado: %s"),
    LOAN_TYPE_NAME_REQUIRED("El nombre del tipo de crédito es obligatorio."),
    LOAN_TYPE_DATA_REQUIRED("Los datos del tipo de crédito no pueden ser nulos."),
    LOAN_TYPE_NAME_EXISTS("Ya existe un tipo de crédito con el nombre '%s'."),
    LOAN_TYPE_ID_AND_DATA_REQUIRED("Identificador y datos del tipo de crédito requeridos."),
    LOAN_TYPE_INTEREST_RATE_INVALID("La tasa de interés debe estar entre 0.00% y 100.00%."),
    LOAN_TYPE_MIN_AMOUNT_REQUIRED("El monto mínimo debe ser mayor a cero."),
    LOAN_TYPE_MAX_AMOUNT_INVALID("El monto máximo debe ser mayor o igual al monto mínimo."),
    LOAN_TYPE_MIN_TERM_REQUIRED("El plazo mínimo debe ser de al menos 1 mes."),
    LOAN_TYPE_MAX_TERM_INVALID("El plazo máximo debe ser mayor o igual al plazo mínimo."),
    LOAN_TYPE_NOT_ACTIVE("El tipo de préstamo no está activo"),
    LOAN_TYPE_AMOUNT_BELOW_MINIMUM("El monto solicitado es menor al mínimo permitido"),
    LOAN_TYPE_AMOUNT_EXCEEDS_MAXIMUM("El monto solicitado excede el máximo permitido"),
    LOAN_TYPE_TERM_BELOW_MINIMUM("El plazo mínimo para este tipo de préstamo es %s meses"),
    LOAN_TYPE_TERM_EXCEEDS_MAXIMUM("El plazo máximo para este tipo de préstamo es %s meses"),
    LOAN_TYPE_DELETE_HAS_RELATIONS("No es posible eliminar el tipo de crédito '%s' porque ya se encuentra asociado a solicitudes o préstamos existentes. Utilice la desactivación lógica en su lugar."),
    LOAN_TYPE_AUTO_VALIDATION_DISABLED("El tipo de préstamo no tiene habilitada la validación automática"),
    LOAN_TYPE_STATUS_ACTIVATED("Tipo de crédito activado exitosamente"),
    LOAN_TYPE_STATUS_DEACTIVATED("Tipo de crédito desactivado exitosamente"),

    // ========== User ==========
    USER_EMAIL_REQUIRED("El email del usuario es obligatorio."),
    USER_PASSWORD_HASH_REQUIRED("El hash de la contraseña es obligatorio."),
    USER_NAME_REQUIRED("El nombre del usuario es obligatorio."),
    USER_ROLE_REQUIRED("El rol del usuario es obligatorio."),
    USER_DOCUMENT_REQUIRED("El tipo y número de documento del usuario son obligatorios."),

    // ========== Money / Value Objects ==========
    MONEY_AMOUNT_REQUIRED("El monto no puede ser nulo."),
    MONEY_CURRENCY_REQUIRED("La moneda no puede ser nula."),
    MONEY_AMOUNT_NEGATIVE("El monto no puede ser negativo."),
    MONEY_CURRENCY_MISMATCH("Las monedas no coinciden: %s vs %s"),
    EMAIL_REQUIRED("El correo electrónico es obligatorio."),
    EMAIL_INVALID_FORMAT("Formato de correo electrónico inválido: %s"),
    PHONE_COUNTRY_CODE_REQUIRED("El código de país es obligatorio."),
    PHONE_NUMBER_REQUIRED("El número de teléfono es obligatorio."),
    DOCUMENT_TYPE_REQUIRED("El tipo de documento es obligatorio."),
    DOCUMENT_NUMBER_REQUIRED("El número de documento es obligatorio."),

    // ========== Financial / Loan Calculation ==========
    LOAN_PRINCIPAL_REQUIRED("El capital debe ser mayor a cero."),
    LOAN_RATE_NON_NEGATIVE("La tasa de interés no puede ser negativa."),
    LOAN_TERM_REQUIRED("El plazo debe ser mayor a cero."),

    // ========== Email Notifications ==========
    EMAIL_BUTTON_VIEW_DETAILS("Ver Detalles del Préstamo"),
    EMAIL_BUTTON_VIEW_STATUS("Ver Estado de Solicitud"),
    EMAIL_TITLE_RECEIVED("Solicitud Recibida"),
    EMAIL_SUBTITLE_RECEIVED("Estamos trabajando en tu crédito"),
    EMAIL_FOOTER("Préstamos Fácil - Tu solución financiera"),
    EMAIL_SUBJECT_RECEIVED("Solicitud Recibida - Préstamos Fácil"),
    EMAIL_TITLE_APPROVED("¡Felicidades!"),
    EMAIL_SUBTITLE_APPROVED("Tu crédito ha sido aprobado"),
    EMAIL_SUBJECT_APPROVED("Crédito Aprobado - Préstamos Fácil"),
    EMAIL_STATUS_APPROVED("APROBADO"),
    EMAIL_TITLE_REJECTED("Estado de Solicitud"),
    EMAIL_SUBTITLE_REJECTED("Información sobre tu crédito"),
    EMAIL_SUBJECT_REJECTED("Respuesta a tu Solicitud - Préstamos Fácil"),
    EMAIL_STATUS_REJECTED("RECHAZADO"),
    EMAIL_DEFAULT_REJECT_REASON("No se cumplen los requisitos"),
    EMAIL_TITLE_MANUAL_REVIEW("Solicitud en Revisión"),
    EMAIL_SUBTITLE_MANUAL_REVIEW("Requiere evaluación manual"),
    EMAIL_SUBJECT_MANUAL_REVIEW("Solicitud en Revisión Manual - Préstamos Fácil"),
    EMAIL_STATUS_MANUAL_REVIEW("REVISION MANUAL");

    private final String value;

    Messages(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String format(Object... args) {
        return String.format(value, args);
    }

    @Override
    public String toString() {
        return value;
    }
}
