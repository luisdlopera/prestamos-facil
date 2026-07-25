-- ============================================================
-- V15: Add role column to users + seed admin, analyst, customer
-- Password for all seed accounts: !Pass.1234
-- ============================================================

-- 1. Add role column
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(50);

-- 2. Populate role from user_roles (take the first role for backward compat)
UPDATE users u
SET role = sub.role_name
FROM (
    SELECT ur.user_id, r.name AS role_name
    FROM user_roles ur
    JOIN roles r ON r.id = ur.role_id
) sub
WHERE u.id = sub.user_id AND u.role IS NULL;

-- 3. Seed admin user (if not exists)
INSERT INTO users (id, name, email, password_hash, user_type, role, enabled, created_at, updated_at)
VALUES (
    'f1a2b3c4-d5e6-4789-a0b1-c2d3e4f5a6b7',
    'Admin',
    'admin@prestamosfacil.com',
    '$2b$12$kEdmixFOOPiHyH3G1eRG3uQT2GLg.lUdUbaSzc8Rb7OnTeHSl9x.K',
    'STAFF', 'ADMIN', TRUE, NOW(), NOW()
)
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, email = EXCLUDED.email, role = 'ADMIN', password_hash = EXCLUDED.password_hash;

-- 4. Seed analyst user (if not exists)
INSERT INTO users (id, name, email, password_hash, user_type, role, enabled, created_at, updated_at)
VALUES (
    'a2b3c4d5-e6f7-4890-b1c2-d3e4f5a6b7c8',
    'Asesor de Crédito',
    'analista@prestamosfacil.com',
    '$2b$12$kEdmixFOOPiHyH3G1eRG3uQT2GLg.lUdUbaSzc8Rb7OnTeHSl9x.K',
    'STAFF', 'ANALYST', TRUE, NOW(), NOW()
)
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, email = EXCLUDED.email, role = 'ANALYST', password_hash = EXCLUDED.password_hash;

-- 5. Update old email references to new ones
UPDATE users SET email = 'cliente@prestamosfacil.com' WHERE email = 'demo@prestamosfacil.com';
UPDATE users SET email = 'analista@prestamosfacil.com' WHERE email = 'asesor@prestamosfacil.com';

-- 6. Update customers table to match
UPDATE customers SET email = 'cliente@prestamosfacil.com' WHERE email = 'demo@prestamosfacil.com';

-- 7. Seed demo customer (if not exists with new email)
INSERT INTO users (id, name, email, password_hash, user_type, role, enabled, created_at, updated_at)
SELECT 'c1a2b3c4-d5e6-4789-a0b1-c2d3e4f5a6b8', 'Cliente Demo', 'cliente@prestamosfacil.com',
       '$2b$12$kEdmixFOOPiHyH3G1eRG3uQT2GLg.lUdUbaSzc8Rb7OnTeHSl9x.K',
       'CUSTOMER', NULL, TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'cliente@prestamosfacil.com');

-- 8. Ensure customers table has demo customer record
INSERT INTO customers (id, user_id, first_name, last_name, email, document_type, document_number, base_salary, created_at, updated_at)
SELECT 'c1a2b3c4-d5e6-4789-a0b1-c2d3e4f5a6b8', 'c1a2b3c4-d5e6-4789-a0b1-c2d3e4f5a6b8',
       'Cliente', 'Demo', 'cliente@prestamosfacil.com',
       'CC',
       '1098765432', 5000000.00, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM customers WHERE email = 'cliente@prestamosfacil.com')
  AND EXISTS (SELECT 1 FROM document_types WHERE code = 'CC');

-- Ensure user_id is set for existing customer records missing it
UPDATE customers
SET user_id = u.id
FROM users u
WHERE customers.email = u.email AND customers.user_id IS NULL;

-- 9. Ensure user_roles entries for seed users
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'admin@prestamosfacil.com' AND r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'analista@prestamosfacil.com' AND r.name = 'ASESOR'
ON CONFLICT DO NOTHING;
