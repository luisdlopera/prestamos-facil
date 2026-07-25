-- ============================================================
-- V14: Centralized Users Schema & Unified Roles (No Staff Table)
-- ============================================================

-- 1. Create central users table
CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    user_type VARCHAR(20) NOT NULL CHECK (user_type IN ('CUSTOMER', 'STAFF')),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    blocked_until TIMESTAMP WITH TIME ZONE,
    last_login_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uk_users_email_lower ON users (LOWER(email));

-- 2. Create user_roles relationship table
CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- 3. Migrate staff into users and user_roles if staff table exists
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'staff') THEN
        INSERT INTO users (id, name, email, password_hash, user_type, enabled, created_at, updated_at)
        SELECT id, name, LOWER(email), password_hash, 'STAFF', enabled, created_at, updated_at
        FROM staff
        ON CONFLICT (email) DO NOTHING;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'staff_roles') THEN
        INSERT INTO user_roles (user_id, role_id)
        SELECT staff_id, role_id
        FROM staff_roles
        ON CONFLICT DO NOTHING;
    END IF;
END $$;

-- 4. Migrate customers into users
INSERT INTO users (id, name, email, password_hash, user_type, enabled, failed_login_attempts, blocked_until, last_login_at, created_at, updated_at)
SELECT id, first_name || ' ' || last_name, LOWER(email), COALESCE(password_hash, '$2b$12$kEdmixFOOPiHyH3G1eRG3uQT2GLg.lUdUbaSzc8Rb7OnTeHSl9x.K'), 'CUSTOMER', TRUE, failed_login_attempts, blocked_until, last_login_at, created_at, updated_at
FROM customers
ON CONFLICT (email) DO NOTHING;

-- 5. Add user_id FK to customers table and drop auth columns
ALTER TABLE customers
    ADD COLUMN IF NOT EXISTS user_id UUID UNIQUE;

UPDATE customers c
SET user_id = u.id
FROM users u
WHERE LOWER(c.email) = u.email;

ALTER TABLE customers
    ALTER COLUMN user_id SET NOT NULL,
    ADD CONSTRAINT fk_customers_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    DROP COLUMN IF EXISTS password_hash,
    DROP COLUMN IF EXISTS failed_login_attempts,
    DROP COLUMN IF EXISTS blocked_until,
    DROP COLUMN IF EXISTS last_login_at;

-- 6. Re-point FK constraints from staff(id) to users(id)
ALTER TABLE loan_applications
    DROP CONSTRAINT IF EXISTS fk_loan_applications_evaluator;

ALTER TABLE loan_applications
    ADD CONSTRAINT fk_loan_applications_evaluator
        FOREIGN KEY (evaluated_by) REFERENCES users(id);

ALTER TABLE loans
    DROP CONSTRAINT IF EXISTS fk_loans_approver;

ALTER TABLE loans
    ADD CONSTRAINT fk_loans_approver
        FOREIGN KEY (approved_by) REFERENCES users(id);

-- 7. Drop redundant staff & staff_roles tables
DROP TABLE IF EXISTS staff_roles CASCADE;
DROP TABLE IF EXISTS staff CASCADE;

-- 8. Unify refresh_tokens table to reference users(id)
ALTER TABLE refresh_tokens
    ADD COLUMN IF NOT EXISTS user_id UUID;

UPDATE refresh_tokens rt
SET user_id = c.user_id
FROM customers c
WHERE rt.customer_id = c.id;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'staff_refresh_tokens') THEN
        INSERT INTO refresh_tokens (id, user_id, token_hash, expires_at, revoked, created_at, updated_at)
        SELECT srt.id, srt.staff_id, srt.token_hash, srt.expires_at, srt.revoked, srt.created_at, srt.updated_at
        FROM staff_refresh_tokens srt;

        DROP TABLE IF EXISTS staff_refresh_tokens CASCADE;
    END IF;
END $$;

ALTER TABLE refresh_tokens
    DROP COLUMN IF EXISTS customer_id;

ALTER TABLE refresh_tokens
    ALTER COLUMN user_id SET NOT NULL,
    ADD CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user ON refresh_tokens(user_id);
