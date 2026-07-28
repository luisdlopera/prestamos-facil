INSERT INTO document_types (code, name, created_at, updated_at) VALUES
    ('CC', 'Cédula de Ciudadanía', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('CE', 'Cédula de Extranjería', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('NIT', 'Número de Identificación Tributaria', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('TI', 'Tarjeta de Identidad', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('PP', 'Pasaporte', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('PEP', 'Permiso Especial de Permanencia', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO loan_application_statuses (code, name, created_at, updated_at) VALUES
    ('PENDING_REVIEW', 'Pendiente de Revisión', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('MANUAL_REVIEW', 'Revisión Manual', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('APPROVED', 'Aprobado', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('REJECTED', 'Rechazado', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

