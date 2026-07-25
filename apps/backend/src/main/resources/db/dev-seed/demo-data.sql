-- ============================================================
-- V13: Demo credentials for local/demo environments (staff & customer schema)
-- Password for all seed accounts: !Pass.1234
-- ============================================================

-- Admin and Asesor de Crédito staff accounts
INSERT INTO staff (id, name, email, password_hash, enabled)
VALUES
    ('f1a2b3c4-d5e6-4789-a0b1-c2d3e4f5a6b7', 'Admin', 'admin@prestamosfacil.com', '$2b$12$kEdmixFOOPiHyH3G1eRG3uQT2GLg.lUdUbaSzc8Rb7OnTeHSl9x.K', TRUE),
     ('a2b3c4d5-e6f7-4890-b1c2-d3e4f5a6b7c8', 'Asesor de Crédito', 'analista@prestamosfacil.com', '$2b$12$kEdmixFOOPiHyH3G1eRG3uQT2GLg.lUdUbaSzc8Rb7OnTeHSl9x.K', TRUE)
ON CONFLICT (email) DO UPDATE SET password_hash = EXCLUDED.password_hash;

INSERT INTO staff_roles (staff_id, role_id)
SELECT s.id, r.id FROM staff s, roles r WHERE s.email = 'admin@prestamosfacil.com' AND r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO staff_roles (staff_id, role_id)
SELECT s.id, r.id FROM staff s, roles r WHERE s.email = 'analista@prestamosfacil.com' AND r.name = 'ANALYST'
ON CONFLICT DO NOTHING;

-- Insert or update demo customer with password !Pass.1234
INSERT INTO customers (id, first_name, last_name, email, document_type, document_number, base_salary, password_hash, failed_login_attempts, blocked_until)
VALUES (
    'c1a2b3c4-d5e6-4789-a0b1-c2d3e4f5a6b8',
    'Cliente',
    'Demo',
    'cliente@prestamosfacil.com',
    'CC',
    '1098765432',
    5000000.00,
    '$2b$12$kEdmixFOOPiHyH3G1eRG3uQT2GLg.lUdUbaSzc8Rb7OnTeHSl9x.K',
    0,
    NULL
)
ON CONFLICT (email) DO UPDATE
SET password_hash = EXCLUDED.password_hash,
    failed_login_attempts = 0,
    blocked_until = NULL;

