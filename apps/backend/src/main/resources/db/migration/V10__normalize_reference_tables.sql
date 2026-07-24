-- Document types lookup table
CREATE TABLE document_types (
    code VARCHAR(10) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255)
);

INSERT INTO document_types (code, name, description) VALUES
    ('CC', 'Cédula de Ciudadanía', 'Documento de identidad colombiano para mayores de edad'),
    ('CE', 'Cédula de Extranjería', 'Documento de identidad para extranjeros residentes'),
    ('NIT', 'Número de Identificación Tributaria', 'Identificación fiscal para empresas'),
    ('TI', 'Tarjeta de Identidad', 'Documento de identidad para menores de edad'),
    ('PP', 'Pasaporte', 'Pasaporte extranjero'),
    ('PEP', 'Permiso Especial de Permanencia', 'Permiso temporal para migrantes')
ON CONFLICT (code) DO NOTHING;

-- Loan application statuses lookup table
CREATE TABLE loan_application_statuses (
    code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255)
);

INSERT INTO loan_application_statuses (code, name, description) VALUES
    ('PENDING_REVIEW', 'Pendiente de Revisión', 'Solicitud creada, pendiente de evaluación inicial'),
    ('MANUAL_REVIEW', 'Revisión Manual', 'Solicitud enviada a revisión manual por un analista'),
    ('APPROVED', 'Aprobado', 'Solicitud aprobada'),
    ('REJECTED', 'Rechazado', 'Solicitud rechazada')
ON CONFLICT (code) DO NOTHING;

-- Loan statuses lookup table
CREATE TABLE loan_statuses (
    code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255)
);

INSERT INTO loan_statuses (code, name, description) VALUES
    ('ACTIVE', 'Activo', 'Préstamo vigente con pagos pendientes'),
    ('PAID', 'Pagado', 'Préstamo cancelado en su totalidad'),
    ('DEFAULTED', 'En Mora', 'Préstamo con pagos vencidos no cubiertos'),
    ('WRITTEN_OFF', 'Castigado', 'Préstamo dado de baja por incobrabilidad')
ON CONFLICT (code) DO NOTHING;

-- Add foreign key constraints
ALTER TABLE customers
    ADD CONSTRAINT fk_customers_document_type
    FOREIGN KEY (document_type) REFERENCES document_types(code);

ALTER TABLE loan_applications
    ADD CONSTRAINT fk_loan_applications_status
    FOREIGN KEY (status) REFERENCES loan_application_statuses(code);

ALTER TABLE loans
    ADD CONSTRAINT fk_loans_status
    FOREIGN KEY (status) REFERENCES loan_statuses(code);
