package com.prestamosfacil.infrastructure.adapter.in.rest.configuration;

public final class SwaggerDocs {

    private SwaggerDocs() {}

    // ========== OpenAPI Info ==========
    public static final String API_TITLE = "Prestamos Fácil API";
    public static final String API_VERSION = "1.0.0";
    public static final String API_DESCRIPTION = """
        API REST para el sistema de gestión de solicitudes de préstamos.

        ## Endpoints principales
        - POST /api/v1/customers - Registrar cliente
        - POST /api/v1/loan-applications - Crear solicitud
        - POST /api/v1/loan-applications/{id}/approve - Aprobar solicitud manualmente
        - POST /api/v1/loan-applications/{id}/reject - Rechazar solicitud manualmente
        - POST /api/v1/loan-applications/{id}/automatic-evaluation - Evaluación automática
        - GET /api/v1/loans/{id}/payment-plan - Plan de pagos
        - GET /api/v1/reports/approved-loans/total - Reporte de aprobados

        ## Stored Procedure
        La evaluación automática utiliza el procedimiento almacenado
        sp_evaluate_loan_application en PostgreSQL.

        ## Autenticación
        La API utiliza autenticación basada en cookies HttpOnly para los tokens JWT.
        Los endpoints que requieren autenticación deben incluir las cookies
        `prestamos_access` y `prestamos_refresh` obtenidas tras el inicio de sesión.
        """;
    public static final String LICENSE_NAME = "Proprietary";
    public static final String LICENSE_URL = "https://prestamosfacil.com";
    public static final String SECURITY_SCHEME_DESC = "Token JWT obtenido al iniciar sesión. Se envía como cookie HttpOnly.";
    public static final String SERVER_URL = "http://localhost:4010";
    public static final String SERVER_DESC = "Desarrollo local";
    public static final String EXTERNAL_DOCS_DESC = "Documentación del proyecto";
    public static final String EXTERNAL_DOCS_URL = "https://github.com/anomalyco/prestamos-facil";

    // ========== Tags ==========
    public static final String TAG_AUTH = "Autenticación";
    public static final String TAG_AUTH_DESC = "Endpoints de registro, inicio de sesión y gestión de cuenta";
    public static final String TAG_STAFF = "Staff";
    public static final String TAG_STAFF_DESC = "Autenticación y gestión de personal administrativo";
    public static final String TAG_CUSTOMERS = "Clientes";
    public static final String TAG_CUSTOMERS_DESC = "Gestión de clientes del sistema";
    public static final String TAG_LOAN_APPLICATIONS = "Solicitudes de Préstamo";
    public static final String TAG_LOAN_APPLICATIONS_DESC = "Gestión de solicitudes de préstamo, incluyendo creación, evaluación y decisión";
    public static final String TAG_LOANS = "Préstamos";
    public static final String TAG_LOANS_DESC = "Gestión de préstamos aprobados y planes de pago";
    public static final String TAG_LOAN_TYPES = "Tipos de Préstamo";
    public static final String TAG_LOAN_TYPES_DESC = "Administración y consulta de tipos de préstamo";
    public static final String TAG_REPORTS = "Reportes";
    public static final String TAG_REPORTS_DESC = "Reportes y estadísticas del sistema";

    // ========== Operation summaries & descriptions ==========
    // Auth
    public static final String OP_REGISTER_SUM = "Registrar nuevo cliente";
    public static final String OP_REGISTER_DESC = "Crea una cuenta de cliente con datos personales y credenciales. No crea sesión; use /auth/login después del registro";
    public static final String OP_LOGIN_SUM = "Iniciar sesión";
    public static final String OP_LOGIN_DESC = "Autentica con correo y contraseña, devuelve cookies httpOnly";
    public static final String OP_REFRESH_SUM = "Renovar token de acceso";
    public static final String OP_REFRESH_DESC = "Usa el refresh token de la cookie para emitir nuevos tokens";
    public static final String OP_LOGOUT_SUM = "Cerrar sesión";
    public static final String OP_LOGOUT_DESC = "Revoca el refresh token de la sesión actual y limpia las cookies";
    public static final String OP_ME_SUM = "Obtener perfil actual";
    public static final String OP_ME_DESC = "Devuelve los datos del cliente o staff autenticado";
    public static final String OP_UPDATE_PROFILE_SUM = "Actualizar perfil";
    public static final String OP_UPDATE_PROFILE_DESC = "Actualiza nombre, apellido y/o salario base del cliente autenticado";
    public static final String OP_REQUEST_PASSWORD_RESET_SUM = "Solicitar restablecimiento de contraseña";
    public static final String OP_REQUEST_PASSWORD_RESET_DESC = "Genera un token de recuperación";
    public static final String OP_CONFIRM_PASSWORD_RESET_SUM = "Confirmar restablecimiento de contraseña";
    public static final String OP_CONFIRM_PASSWORD_RESET_DESC = "Usa el token de recuperación para establecer una nueva contraseña";
    public static final String OP_CHANGE_PASSWORD_SUM = "Cambiar contraseña";
    public static final String OP_CHANGE_PASSWORD_DESC = "Cambia la contraseña del cliente autenticado (requiere contraseña actual)";

    // Staff
    public static final String OP_STAFF_REGISTER_SUM = "Registrar nuevo staff";
    public static final String OP_STAFF_REGISTER_DESC = "Crea una cuenta de personal administrativo (requiere rol ADMIN)";
    public static final String OP_STAFF_LOGIN_SUM = "Iniciar sesión staff";
    public static final String OP_STAFF_LOGIN_DESC = "Autentica personal administrativo, devuelve cookies httpOnly y token en respuesta";
    public static final String OP_STAFF_REFRESH_SUM = "Renovar token staff";
    public static final String OP_STAFF_REFRESH_DESC = "Usa el refresh token de la cookie para emitir nuevos tokens";
    public static final String OP_STAFF_LOGOUT_SUM = "Cerrar sesión staff";
    public static final String OP_STAFF_LOGOUT_DESC = "Revoca el refresh token activo";
    public static final String OP_STAFF_ME_SUM = "Obtener perfil staff actual";
    public static final String OP_STAFF_ME_DESC = "Devuelve los datos del staff autenticado";

    // Customers
    public static final String OP_CUSTOMER_CREATE_SUM = "Registrar cliente";
    public static final String OP_CUSTOMER_CREATE_DESC = "Crea un nuevo cliente en el sistema con sus datos personales y financieros (requiere CUSTOMER_CREATE)";
    public static final String OP_CUSTOMER_FIND_BY_ID_SUM = "Obtener cliente por ID";
    public static final String OP_CUSTOMER_FIND_BY_ID_DESC = "Devuelve los datos de un cliente específico. Clientes solo ven su propio perfil";
    public static final String OP_CUSTOMER_FIND_ALL_SUM = "Listar clientes";
    public static final String OP_CUSTOMER_FIND_ALL_DESC = "Devuelve una lista paginada de clientes con búsqueda opcional. Clientes solo ven su propio perfil";

    // Loan Applications
    public static final String OP_LOAN_APP_CREATE_SUM = "Crear solicitud de préstamo";
    public static final String OP_LOAN_APP_CREATE_DESC = "Crea una nueva solicitud de préstamo. Clientes usan su propia cuenta, staff puede especificar customerId";
    public static final String OP_LOAN_APP_FIND_ALL_SUM = "Listar solicitudes";
    public static final String OP_LOAN_APP_FIND_ALL_DESC = "Devuelve una lista paginada de solicitudes con filtros opcionales. Clientes solo ven sus propias solicitudes";
    public static final String OP_LOAN_APP_FIND_BY_ID_SUM = "Obtener solicitud por ID";
    public static final String OP_LOAN_APP_FIND_BY_ID_DESC = "Devuelve los detalles de una solicitud de préstamo específica. Clientes solo ven sus propias solicitudes";
    public static final String OP_LOAN_APP_AUTO_EVAL_SUM = "Evaluación automática";
    public static final String OP_LOAN_APP_AUTO_EVAL_DESC = "Ejecuta el stored procedure sp_evaluate_loan_application (requiere LOAN_APPLICATION_EVALUATE)";
    public static final String OP_LOAN_APP_APPROVE_SUM = "Aprobar solicitud";
    public static final String OP_LOAN_APP_APPROVE_DESC = "Aprueba manualmente una solicitud y genera el préstamo asociado (requiere LOAN_APPLICATION_APPROVE)";
    public static final String OP_LOAN_APP_REJECT_SUM = "Rechazar solicitud";
    public static final String OP_LOAN_APP_REJECT_DESC = "Rechaza una solicitud con un motivo (requiere LOAN_APPLICATION_REJECT)";

    // Loans
    public static final String OP_LOAN_FIND_ALL_SUM = "Listar préstamos";
    public static final String OP_LOAN_FIND_ALL_DESC = "Devuelve una lista paginada de préstamos aprobados. Clientes solo ven sus propios préstamos";
    public static final String OP_LOAN_FIND_BY_ID_SUM = "Obtener préstamo por ID";
    public static final String OP_LOAN_FIND_BY_ID_DESC = "Devuelve los detalles de un préstamo específico. Clientes solo ven sus propios préstamos";
    public static final String OP_LOAN_PAYMENT_PLAN_SUM = "Obtener plan de pagos";
    public static final String OP_LOAN_PAYMENT_PLAN_DESC = "Devuelve el plan de pagos detallado de un préstamo. Clientes solo ven sus propios planes";

    // Loan Types
    public static final String OP_LOAN_TYPE_FIND_ALL_ACTIVE_SUM = "Listar tipos de préstamo activos";
    public static final String OP_LOAN_TYPE_FIND_ALL_ACTIVE_DESC = "Devuelve todos los tipos de préstamo disponibles para solicitud, ordenados por orden de visualización";
    public static final String OP_LOAN_TYPE_FIND_ALL_ADMIN_SUM = "Listar tipos de crédito (Administración)";
    public static final String OP_LOAN_TYPE_FIND_ALL_ADMIN_DESC = "Devuelve el listado paginado con filtros para administración (requiere rol ADMIN o STAFF)";
    public static final String OP_LOAN_TYPE_FIND_BY_ID_SUM = "Obtener tipo de préstamo por ID";
    public static final String OP_LOAN_TYPE_FIND_BY_ID_DESC = "Devuelve un tipo de préstamo específico";
    public static final String OP_LOAN_TYPE_CREATE_SUM = "Crear nuevo tipo de crédito";
    public static final String OP_LOAN_TYPE_CREATE_DESC = "Crea un nuevo tipo de crédito en el sistema (requiere rol ADMIN o STAFF)";
    public static final String OP_LOAN_TYPE_UPDATE_SUM = "Editar tipo de crédito";
    public static final String OP_LOAN_TYPE_UPDATE_DESC = "Actualiza los datos de un tipo de crédito (requiere rol ADMIN o STAFF)";
    public static final String OP_LOAN_TYPE_TOGGLE_STATUS_SUM = "Activar o desactivar tipo de crédito";
    public static final String OP_LOAN_TYPE_TOGGLE_STATUS_DESC = "Cambia el estado activo/inactivo de un tipo de crédito (requiere rol ADMIN o STAFF)";
    public static final String OP_LOAN_TYPE_REORDER_SUM = "Reordenar secuencia de tipos de crédito";
    public static final String OP_LOAN_TYPE_REORDER_DESC = "Actualiza el orden de visualización a partir de la lista de IDs ordenada (requiere rol ADMIN o STAFF)";
    public static final String OP_LOAN_TYPE_DELETE_SUM = "Eliminar tipo de crédito";
    public static final String OP_LOAN_TYPE_DELETE_DESC = "Elimina un tipo de crédito únicamente si no cuenta con solicitudes o préstamos asociados (requiere rol ADMIN o STAFF)";

    // Reports
    public static final String OP_REPORT_APPROVED_TOTAL_SUM = "Total de préstamos aprobados";
    public static final String OP_REPORT_APPROVED_TOTAL_DESC = "Devuelve el monto total aprobado, la cantidad de préstamos y la fecha del reporte (requiere REPORT_APPROVED_LOANS_READ)";

    // ========== @ApiResponse descriptions ==========
    public static final String RESP_STAFF_REGISTER_201 = "Staff registrado exitosamente";
    public static final String RESP_STAFF_REGISTER_400 = "Datos inválidos o correo ya registrado";
    public static final String RESP_STAFF_LOGIN_200 = "Inicio de sesión exitoso";
    public static final String RESP_STAFF_LOGIN_401 = "Credenciales inválidas o cuenta deshabilitada";
    public static final String RESP_STAFF_REFRESH_200 = "Token renovado exitosamente";
    public static final String RESP_STAFF_REFRESH_401 = "Refresh token inválido o expirado";
    public static final String RESP_STAFF_LOGOUT_204 = "Sesión cerrada exitosamente (sin contenido)";
    public static final String RESP_STAFF_ME_200 = "Perfil obtenido exitosamente";
    public static final String RESP_STAFF_ME_401 = "No autenticado o no es staff";
    public static final String RESP_REPORT_APPROVED_200 = "Reporte obtenido exitosamente";

    // ========== Response Envelope ==========
    public static final String API_RESPONSE = "Envoltura genérica de respuesta para todos los endpoints de la API";
    public static final String RESPONSE_OK = "Indica si la operación fue exitosa";
    public static final String RESPONSE_DATA = "Datos de la respuesta (puede ser nulo en caso de error)";
    public static final String RESPONSE_MESSAGE = "Mensaje descriptivo del resultado";
    public static final String RESPONSE_TIMESTAMP = "Marca de tiempo de la respuesta";
    public static final String RESPONSE_ERRORS = "Lista de errores detallados (solo en respuestas de error)";
    public static final String ERROR_DETAIL = "Detalle de un error de validación";
    public static final String ERROR_CODE = "Código del error";
    public static final String ERROR_FIELD = "Campo que originó el error";

    // ========== Pagination ==========
    public static final String PAGINATED_RESPONSE = "Respuesta paginada que extiende ApiResponse con información de paginación";
    public static final String PAGINATION_INFO = "Información de paginación de resultados";
    public static final String PAGE_NUMBER = "Número de página actual (0-based)";
    public static final String PAGE_SIZE = "Cantidad de elementos por página";
    public static final String PAGE_TOTAL = "Total de elementos en la consulta";
    public static final String PAGE_TOTAL_PAGES = "Total de páginas disponibles";
    public static final String PAGE_HAS_NEXT = "Indica si existe una página siguiente";
    public static final String PAGE_HAS_PREVIOUS = "Indica si existe una página anterior";

    // ========== IDs ==========
    public static final String ID = "Identificador único";
    public static final String CUSTOMER_ID = "Identificador del cliente";
    public static final String LOAN_TYPE_ID = "Identificador del tipo de préstamo";
    public static final String LOAN_APPLICATION_ID = "Identificador de la solicitud evaluada";
    public static final String SOURCE_LOAN_APPLICATION_ID = "Identificador de la solicitud que originó el préstamo";
    public static final String STAFF_ID = "Identificador único del staff";
    public static final String LOAN_ID = "Identificador único del préstamo";
    public static final String INSTALLMENT_ID = "Identificador único de la cuota";
    public static final String USER_ID = "Identificador único del usuario";

    // ========== Person Names ==========
    public static final String FIRST_NAME = "Nombre del cliente";
    public static final String LAST_NAME = "Apellido del cliente";
    public static final String USER_FIRST_NAME = "Nombre del usuario";
    public static final String USER_LAST_NAME = "Apellido del usuario";
    public static final String STAFF_NAME = "Nombre del staff";
    public static final String PERSONAL_NAME = "Nombre del personal";
    public static final String CUSTOMER_FULL_NAME = "Nombre completo del cliente";
    public static final String CUSTOMER_NAME = "Nombre del cliente";
    public static final String CUSTOMER_IDENTIFICATION = "Identificación del cliente";
    public static final String NEW_FIRST_NAME = "Nuevo nombre";
    public static final String NEW_LAST_NAME = "Nuevo apellido";

    // ========== Contact ==========
    public static final String EMAIL = "Correo electrónico";
    public static final String CUSTOMER_EMAIL = "Correo del cliente";
    public static final String USER_EMAIL = "Correo electrónico del usuario";
    public static final String STAFF_EMAIL = "Correo electrónico del staff";
    public static final String NEW_EMAIL = "Nuevo correo electrónico";
    public static final String PHONE_COUNTRY_CODE = "Código de país del teléfono";
    public static final String PHONE_NUMBER = "Número de teléfono";

    // ========== Documents ==========
    public static final String DOCUMENT_TYPE = "Tipo de documento";
    public static final String DOCUMENT_NUMBER = "Número de documento";
    public static final String USER_DOCUMENT_TYPE = "Tipo de documento";

    // ========== Auth / Security ==========
    public static final String PASSWORD = "Contraseña";
    public static final String PASSWORD_MIN_8 = "Nueva contraseña (mín. 8 caracteres)";
    public static final String PASSWORD_PATTERN_HINT = "Contraseña (mín. 8 caracteres, mayúscula, minúscula, dígito y carácter especial)";
    public static final String STAFF_PASSWORD = "Contraseña (mín. 6 caracteres)";
    public static final String CURRENT_PASSWORD = "Contraseña actual";
    public static final String NEW_PASSWORD = "Nueva contraseña (mín. 8 caracteres)";
    public static final String RESET_TOKEN = "Token de recuperación recibido por correo";
    public static final String USER_TYPE = "Tipo de usuario";
    public static final String STAFF_ROLE = "Rol del staff";

    // ========== Financial ==========
    public static final String BASE_SALARY = "Salario base mensual";
    public static final String CUSTOMER_BASE_SALARY = "Salario base del cliente";
    public static final String NEW_BASE_SALARY = "Nuevo salario base";
    public static final String INTEREST_RATE = "Tasa de interés anual";
    public static final String INTEREST_RATE_PERCENTAGE = "Tasa de interés anual en porcentaje";
    public static final String REQUESTED_AMOUNT = "Monto solicitado";
    public static final String PRINCIPAL_AMOUNT = "Monto principal del préstamo";
    public static final String MIN_AMOUNT = "Monto mínimo del préstamo";
    public static final String MAX_AMOUNT = "Monto máximo del préstamo";
    public static final String MONTHLY_PAYMENT = "Valor de la cuota mensual";
    public static final String TOTAL_APPROVED_AMOUNT = "Monto total aprobado acumulado";
    public static final String APPROVED_LOANS_COUNT = "Cantidad de préstamos aprobados";
    public static final String MAX_CAPACITY = "Capacidad máxima de endeudamiento calculada";
    public static final String CURRENT_DEBT = "Deuda actual del cliente";
    public static final String DEBT_RATIO = "Relación deuda/ingreso calculada";

    // ========== Loan Terms ==========
    public static final String TERM_MONTHS = "Plazo en meses";
    public static final String MIN_TERM_MONTHS = "Plazo mínimo en meses";
    public static final String MAX_TERM_MONTHS = "Plazo máximo en meses";
    public static final String INSTALLMENT_NUMBER = "Número de cuota (1-based)";

    // ========== Status / Decision ==========
    public static final String LOAN_STATUS = "Estado de la solicitud";
    public static final String DECISION = "Decisión de la evaluación";
    public static final String DECISION_REASON = "Motivo de la decisión";
    public static final String DECISION_REASON_WITH_CONTEXT = "Motivo de la decisión (aplica si fue aprobada o rechazada)";
    public static final String REJECT_REASON = "Motivo del rechazo";
    public static final String LOAN_TYPE_ACTIVE = "Indica si el tipo de préstamo está activo";
    public static final String AUTO_VALIDATION_ENABLED = "Indica si la validación automática está habilitada para este tipo";
    public static final String STAFF_ENABLED = "Indica si la cuenta está habilitada";

    // ========== Payment Plan ==========
    public static final String INSTALLMENT_DESC = "Cuota o instalment del plan de pagos de un préstamo";
    public static final String DUE_DATE = "Fecha de vencimiento";
    public static final String OPENING_BALANCE = "Saldo inicial antes del pago";
    public static final String PAYMENT_AMOUNT = "Monto total del pago";
    public static final String PRINCIPAL_PAYMENT = "Porción del pago que reduce el principal";
    public static final String INTEREST_PAYMENT = "Porción del pago correspondiente a intereses";
    public static final String CLOSING_BALANCE = "Saldo restante después del pago";
    public static final String NEW_INSTALLMENT_AMOUNT = "Valor de la nueva cuota";

    // ========== Timestamps ==========
    public static final String APPROVED_AT = "Fecha de aprobación del préstamo";
    public static final String CREATED_AT = "Fecha de creación de la solicitud";
    public static final String EVALUATED_AT = "Fecha de evaluación";
    public static final String GENERATED_AT = "Fecha y hora de generación del reporte";
    public static final String EXPIRES_IN = "Tiempo de expiración del token en segundos";
    public static final String EXPIRES_IN_NEW = "Tiempo de expiración del nuevo token en segundos";

    // ========== DTO Descriptions ==========
    public static final String AUTH_USER_RESPONSE = "Datos del usuario autenticado";
    public static final String LOGIN_RESPONSE = "Respuesta de inicio de sesión exitoso";
    public static final String LOGIN_RESPONSE_STAFF = "Respuesta de inicio de sesión exitoso para staff";
    public static final String REFRESH_RESPONSE = "Respuesta de renovación de token";
    public static final String REFRESH_RESPONSE_STAFF = "Respuesta de renovación de token para staff";
    public static final String STAFF_USER_RESPONSE = "Datos del staff autenticado";
    public static final String REGISTER_REQUEST = "Solicitud de registro de nuevo cliente";
    public static final String LOGIN_REQUEST = "Solicitud de inicio de sesión";
    public static final String LOGIN_REQUEST_STAFF = "Solicitud de inicio de sesión para staff";
    public static final String PASSWORD_RESET_REQUEST = "Solicitud de restablecimiento de contraseña";
    public static final String PASSWORD_RESET_CONFIRM = "Confirmación de restablecimiento de contraseña";
    public static final String CHANGE_PASSWORD_REQUEST = "Solicitud de cambio de contraseña";
    public static final String UPDATE_PROFILE_REQUEST = "Solicitud de actualización de perfil (todos los campos son opcionales)";
    public static final String CUSTOMER_RESPONSE = "Datos completos de un cliente";
    public static final String CREATE_CUSTOMER_REQUEST = "Solicitud de registro de nuevo cliente";
    public static final String LOAN_APPLICATION_RESPONSE = "Datos completos de una solicitud de préstamo";
    public static final String CREATE_LOAN_APPLICATION_REQUEST = "Solicitud de creación de préstamo";
    public static final String AUTOMATIC_EVALUATION_RESPONSE = "Resultado de la evaluación automática de una solicitud";
    public static final String REJECT_REQUEST = "Motivo de rechazo de una solicitud de préstamo";
    public static final String LOAN_RESPONSE = "Datos de un préstamo aprobado";
    public static final String PAYMENT_INSTALLMENT_RESPONSE = "Cuota o instalment del plan de pagos de un préstamo";
    public static final String LOAN_TYPE_NAME = "Nombre del tipo de préstamo";
    public static final String LOAN_TYPE_DESCRIPTION = "Descripción detallada del tipo de crédito";
    public static final String RATE_TYPE = "Tipo de tasa de interés (EA, MV)";
    public static final String DISPLAY_ORDER = "Orden de visualización";
    public static final String LOAN_TYPE_RESPONSE = "Tipo de préstamo disponible en el sistema";
    public static final String CREATE_LOAN_TYPE_REQUEST = "Petición para la creación de un nuevo tipo de crédito";
    public static final String UPDATE_LOAN_TYPE_REQUEST = "Petición para actualización de un tipo de crédito existente";
    public static final String REORDER_LOAN_TYPES_REQUEST = "Petición para reordenar la secuencia de visualización de tipos de crédito";
    public static final String TOGGLE_LOAN_TYPE_STATUS_REQUEST = "Petición para activar o desactivar un tipo de crédito";
    public static final String UPDATED_AT = "Fecha de actualización";
    public static final String APPROVED_LOANS_TOTAL_RESPONSE = "Reporte de total de préstamos aprobados";
    public static final String STAFF_REGISTER_REQUEST = "Solicitud de registro de nuevo staff";

    // ========== Examples: Personas ==========
    public static final String EX_FIRST_NAME = "Juan";
    public static final String EX_LAST_NAME = "Pérez";
    public static final String EX_FIRST_NAME_ALT = "María";
    public static final String EX_LAST_NAME_ALT = "García";
    public static final String EX_FULL_NAME = "María García";
    public static final String EX_STAFF_NAME = "Carlos";

    // ========== Examples: Contacto ==========
    public static final String EX_EMAIL = "juan.perez@email.com";
    public static final String EX_EMAIL_ALT = "maria.garcia@email.com";
    public static final String EX_EMAIL_STAFF = "carlos@prestamosfacil.com";
    public static final String EX_EMAIL_NEW = "juan.nuevo@email.com";
    public static final String EX_PHONE_CODE = "+57";
    public static final String EX_PHONE_NUMBER = "3001234567";

    // ========== Examples: Auth ==========
    public static final String EX_PASSWORD = "Password1!";
    public static final String EX_PASSWORD_NEW = "NuevaPass1!";
    public static final String EX_RESET_TOKEN = "abc123def456";

    // ========== Examples: Documentos ==========
    public static final String EX_DOC_TYPE = "CC";
    public static final String EX_DOC_NUMBER = "1234567890";
    public static final String EX_DOC_NUMBER_ALT = "987654321";

    // ========== Examples: Financiero ==========
    public static final String EX_SALARY = "2500000";
    public static final String EX_SALARY_NEW = "3000000";
    public static final String EX_AMOUNT = "5000000";
    public static final String EX_AMOUNT_MIN = "500000";
    public static final String EX_AMOUNT_MAX = "50000000";
    public static final String EX_AMOUNT_TOTAL = "150000000";
    public static final String EX_INTEREST = "18.5";
    public static final String EX_INSTALLMENT = "250000";
    public static final String EX_DEBT_RATIO = "0.35";
    public static final String EX_MAX_CAPACITY = "3000000";
    public static final String EX_CURRENT_DEBT = "500000";

    // ========== Examples: Payment Plan ==========
    public static final String EX_DUE_DATE = "2025-08-15";
    public static final String EX_OPENING_BALANCE = "5000000";
    public static final String EX_PAYMENT_AMOUNT = "250000";
    public static final String EX_PRINCIPAL_PAYMENT = "175000";
    public static final String EX_INTEREST_PAYMENT = "75000";
    public static final String EX_CLOSING_BALANCE = "4825000";

    // ========== Examples: Plazos ==========
    public static final String EX_TERM = "24";
    public static final String EX_TERM_MIN = "6";
    public static final String EX_TERM_MAX = "60";

    // ========== Examples: Paginación ==========
    public static final String EX_PAGE = "0";
    public static final String EX_PAGE_SIZE = "20";
    public static final String EX_TOTAL = "150";
    public static final String EX_PAGES = "8";

    // ========== Examples: Misc ==========
    public static final String EX_EXPIRES = "900";
    public static final String EX_COUNT = "45";
    public static final String EX_INSTALLMENT_NUM = "1";
    public static final String EX_LOAN_TYPE_NAME = "Libre Inversión";
    public static final String EX_ROLE = "AGENT";
    public static final String EX_MSG_SUCCESS = "Operación exitosa";
    public static final String EX_ERROR_CODE = "INVALID_FORMAT";
    public static final String EX_ERROR_MSG = "El formato del correo no es válido";
    public static final String EX_ERROR_FIELD = "email";
    public static final String EX_REASON_APPROVED = "Capacidad de pago suficiente";
    public static final String EX_REASON_REJECTED = "Historial crediticio insuficiente";
    public static final String EX_USER_TYPE = "customer";
    public static final String EX_TRUE = "true";
    public static final String EX_FALSE = "false";
}
