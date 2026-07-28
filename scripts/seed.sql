-- ============================================================
-- Prestamos Fácil - Database Reset & Seeder Script
-- ============================================================

-- 1. Truncate operational and user tables (cascade resets FK constraints)
TRUNCATE TABLE 
    payment_installments, 
    loans,
    loan_application_status_history, 
    loan_applications, 
    customers, 
    user_roles, 
    users, 
    auth_tokens 
RESTART IDENTITY CASCADE;

-- 2. Seed Reference Lookup Tables
INSERT INTO document_types (code, name, description) VALUES
    ('CC', 'Cédula de Ciudadanía', 'Documento de identidad colombiano para mayores de edad'),
    ('CE', 'Cédula de Extranjería', 'Documento de identidad para extranjeros residentes'),
    ('NIT', 'Número de Identificación Tributaria', 'Identificación fiscal para empresas'),
    ('TI', 'Tarjeta de Identidad', 'Documento de identidad para menores de edad'),
    ('PP', 'Pasaporte', 'Pasaporte extranjero'),
    ('PEP', 'Permiso Especial de Permanencia', 'Permiso temporal para migrantes')
ON CONFLICT (code) DO NOTHING;

INSERT INTO loan_application_statuses (code, name, description) VALUES
    ('PENDING_REVIEW', 'Pendiente de Revisión', 'Solicitud creada, pendiente de evaluación inicial'),
    ('MANUAL_REVIEW', 'Revisión Manual', 'Solicitud enviada a revisión manual por un analista'),
    ('APPROVED', 'Aprobado', 'Solicitud aprobada'),
    ('REJECTED', 'Rechazado', 'Solicitud rechazada')
ON CONFLICT (code) DO NOTHING;

-- 3. Seed Roles
INSERT INTO roles (id, name) VALUES
    ('f6a7b8c9-d0e1-2345-fabc-456789012345', 'ADMIN'),
    ('a7b8c9d0-e1f2-3456-abcd-567890123456', 'ASESOR'),
    ('c8d9e0f1-a2b3-4567-cdef-890123456789', 'ANALYST'),
    ('b8c9d0e1-f2a3-4567-bcde-678901234567', 'COBRADOR')
ON CONFLICT (name) DO NOTHING;

-- 4. Seed Loan Types (Tipos de Crédito)
INSERT INTO loan_types (id, name, annual_interest_rate, automatic_validation_enabled, minimum_amount, maximum_amount, active)
VALUES
    ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'Libre Inversión', 18.50, TRUE, 1000000, 50000000, TRUE),
    ('b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Vivienda', 12.00, FALSE, 5000000, 500000000, TRUE),
    ('c3d4e5f6-a7b8-9012-cdef-123456789012', 'Educativo', 10.00, TRUE, 500000, 30000000, TRUE),
    ('d4e5f6a7-b8c9-0123-defa-234567890123', 'Compra de Vehículo', 16.00, TRUE, 5000000, 150000000, TRUE),
    ('e5f6a7b8-c9d0-1234-efab-345678901234', 'Microcrédito', 24.00, TRUE, 200000, 5000000, TRUE)
ON CONFLICT (name) DO NOTHING;

-- 5. Seed Users (All account passwords: !Pass.1234 -> $2b$12$kEdmixFOOPiHyH3G1eRG3uQT2GLg.lUdUbaSzc8Rb7OnTeHSl9x.K)
INSERT INTO users (id, email, password_hash, role, enabled)
VALUES
    ('f1a2b3c4-d5e6-4789-a0b1-c2d3e4f5a6b7', 'admin@prestamosfacil.com', '$2b$12$kEdmixFOOPiHyH3G1eRG3uQT2GLg.lUdUbaSzc8Rb7OnTeHSl9x.K', 'ADMIN', TRUE),
    ('a2b3c4d5-e6f7-4890-b1c2-d3e4f5a6b7c8', 'analista@prestamosfacil.com', '$2b$12$kEdmixFOOPiHyH3G1eRG3uQT2GLg.lUdUbaSzc8Rb7OnTeHSl9x.K', 'ANALYST', TRUE),
    ('a3c4d5e6-f7a8-4901-c2d3-e4f5a6b7c8d9', 'cliente@prestamosfacil.com', '$2b$12$kEdmixFOOPiHyH3G1eRG3uQT2GLg.lUdUbaSzc8Rb7OnTeHSl9x.K', 'CUSTOMER', TRUE)
ON CONFLICT (email) DO UPDATE SET password_hash = EXCLUDED.password_hash;

-- 6. Seed User Roles
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.email = 'admin@prestamosfacil.com' AND r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.email = 'analista@prestamosfacil.com' AND r.name = 'ANALYST'
ON CONFLICT DO NOTHING;

-- 6b. Seed Customer profile linked to the demo customer's user account
INSERT INTO customers (id, user_id, first_name, last_name, document_type, document_number, base_salary)
VALUES (
    'c1a2b3c4-d5e6-4789-a0b1-c2d3e4f5a6b8',
    'a3c4d5e6-f7a8-4901-c2d3-e4f5a6b7c8d9',
    'Juan', 'Pérez', 'CC', '1234567890', 3500000.00
)
ON CONFLICT (user_id) DO NOTHING;

-- 7. Seed Loan Application
INSERT INTO loan_applications (id, customer_id, loan_type_id, requested_amount, term_in_months, annual_interest_rate, created_at, updated_at)
VALUES (
    'd1a2b3c4-d5e6-4789-a0b1-c2d3e4f5a6b9',
    'c1a2b3c4-d5e6-4789-a0b1-c2d3e4f5a6b8',
    'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
    10000000.00,
    12,
    18.50,
    NOW(),
    NOW()
)
ON CONFLICT (id) DO NOTHING;

-- 8. Seed Status History
INSERT INTO loan_application_status_history (id, loan_application_id, status, decision_reason, evaluated_by, opened_at, created_at, updated_at)
VALUES (
    'f2a3b4c5-d6e7-4890-b1c2-d3e4f5a6b7c9',
    'd1a2b3c4-d5e6-4789-a0b1-c2d3e4f5a6b9',
    'APPROVED',
    'Aprobado automáticamente por perfil crediticio óptimo',
    NULL,
    NOW(),
    NOW(),
    NOW()
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO loans (id, loan_application_id, customer_id, principal_amount, annual_interest_rate, term_in_months, monthly_payment, approved_at, created_at, updated_at)
VALUES (
    'e1a2b3c4-d5e6-4789-a0b1-c2d3e4f5a6c0',
    'd1a2b3c4-d5e6-4789-a0b1-c2d3e4f5a6b9',
    'c1a2b3c4-d5e6-4789-a0b1-c2d3e4f5a6b8',
    10000000.00,
    18.50,
    12,
    919000.00,
    NOW(),
    NOW(),
    NOW()
)
ON CONFLICT (id) DO NOTHING;
