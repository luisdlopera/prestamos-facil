-- ============================================================
-- V20: Drop user_type — role is now the single discriminator
-- Seed users: admin@ / analista@ / cliente@ (password: !Pass.1234)
-- ============================================================

-- 1. Migrate old emails to new convention (for existing DBs)
UPDATE users SET email = 'cliente@prestamosfacil.com' WHERE email = 'demo@prestamosfacil.com';
UPDATE users SET email = 'analista@prestamosfacil.com' WHERE email = 'asesor@prestamosfacil.com';
UPDATE customers SET email = 'cliente@prestamosfacil.com' WHERE email = 'demo@prestamosfacil.com';

-- 2. Normalize role values (copy from user_type if role is missing)
UPDATE users SET role = 'CUSTOMER'
WHERE (role IS NULL OR role = '') AND (user_type = 'CUSTOMER' OR user_type IS NULL);

UPDATE users SET role = 'ADMIN'
WHERE (role IS NULL OR role = '') AND email = 'admin@prestamosfacil.com';

UPDATE users SET role = 'ANALYST'
WHERE (role IS NULL OR role = '') AND user_type = 'STAFF';

-- 3. Drop user_type column
ALTER TABLE users DROP COLUMN IF EXISTS user_type;

-- 4. Set NOT NULL on role
ALTER TABLE users ALTER COLUMN role SET NOT NULL;

-- 5. Seed users (id-based conflict to avoid duplicates)
INSERT INTO users (id, name, email, password_hash, role, enabled, created_at, updated_at)
VALUES
    ('f1a2b3c4-d5e6-4789-a0b1-c2d3e4f5a6b7', 'Admin', 'admin@prestamosfacil.com',
     '$2b$12$kEdmixFOOPiHyH3G1eRG3uQT2GLg.lUdUbaSzc8Rb7OnTeHSl9x.K',
     'ADMIN', TRUE, NOW(), NOW()),
    ('a2b3c4d5-e6f7-4890-b1c2-d3e4f5a6b7c8', 'Asesor de Crédito', 'analista@prestamosfacil.com',
     '$2b$12$kEdmixFOOPiHyH3G1eRG3uQT2GLg.lUdUbaSzc8Rb7OnTeHSl9x.K',
     'ANALYST', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, email = EXCLUDED.email, role = EXCLUDED.role, password_hash = EXCLUDED.password_hash;

-- 6. Seed demo customer
INSERT INTO users (id, name, email, password_hash, role, enabled, created_at, updated_at)
VALUES ('c1a2b3c4-d5e6-4789-a0b1-c2d3e4f5a6b8', 'Cliente Demo', 'cliente@prestamosfacil.com',
        '$2b$12$kEdmixFOOPiHyH3G1eRG3uQT2GLg.lUdUbaSzc8Rb7OnTeHSl9x.K',
        'CUSTOMER', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, email = EXCLUDED.email, role = 'CUSTOMER', password_hash = EXCLUDED.password_hash;

-- 7. Ensure customers table has cliente record
INSERT INTO customers (id, user_id, first_name, last_name, email, document_type, document_number, base_salary, created_at, updated_at)
SELECT u.id, u.id, 'Cliente', 'Demo', u.email,
       'CC', '1098765432', 5000000.00, NOW(), NOW()
FROM users u
WHERE u.email = 'cliente@prestamosfacil.com'
  AND NOT EXISTS (SELECT 1 FROM customers c WHERE c.email = 'cliente@prestamosfacil.com')
  AND EXISTS (SELECT 1 FROM document_types WHERE code = 'CC');

-- 8. Ensure user_roles for seed users
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'admin@prestamosfacil.com' AND r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'analista@prestamosfacil.com' AND r.name = 'ASESOR'
ON CONFLICT DO NOTHING;

-- 9. Update customers.user_id for any records still missing it
UPDATE customers
SET user_id = u.id
FROM users u
WHERE customers.email = u.email AND customers.user_id IS NULL;
