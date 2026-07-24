-- ============================================================
-- V11: Staff schema + 4NF normalization + cleanup dead tables
-- ============================================================

-- 1. Create staff table (replaces V3 users with clean schema)
CREATE TABLE staff (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_staff_email ON staff(email);

-- 2. Seed roles (V3 roles table is reused, just ensure data)
INSERT INTO roles (id, name) VALUES
    (gen_random_uuid(), 'ADMIN'),
    (gen_random_uuid(), 'ANALYST'),
    (gen_random_uuid(), 'COLLECTOR')
ON CONFLICT (name) DO NOTHING;

-- 3. Create staff_roles join table
CREATE TABLE staff_roles (
    staff_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (staff_id, role_id),
    CONSTRAINT fk_staff_roles_staff FOREIGN KEY (staff_id) REFERENCES staff(id) ON DELETE CASCADE,
    CONSTRAINT fk_staff_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- 4. Create staff_refresh_tokens (separate from customer_refresh_tokens)
CREATE TABLE staff_refresh_tokens (
    id UUID PRIMARY KEY,
    staff_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_staff_refresh_tokens_staff FOREIGN KEY (staff_id) REFERENCES staff(id) ON DELETE CASCADE
);

CREATE INDEX idx_staff_refresh_tokens_staff ON staff_refresh_tokens(staff_id);
CREATE INDEX idx_staff_refresh_tokens_hash ON staff_refresh_tokens(token_hash);

-- 5. Add evaluated_by FK to loan_applications
ALTER TABLE loan_applications
    ADD COLUMN evaluated_by UUID,
    ADD CONSTRAINT fk_loan_applications_evaluator
        FOREIGN KEY (evaluated_by) REFERENCES staff(id);

-- 6. Add approved_by FK to loans
ALTER TABLE loans
    ADD COLUMN approved_by UUID,
    ADD CONSTRAINT fk_loans_approver
        FOREIGN KEY (approved_by) REFERENCES staff(id);

-- 7. Drop dead V3 tables (users, user_roles, refresh_tokens)
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS refresh_tokens CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- 8. Drop dead column from V9
ALTER TABLE customers
    DROP COLUMN IF EXISTS base_salary_check;

-- 9. Rename customer_refresh_tokens to refresh_tokens for clarity
ALTER TABLE customer_refresh_tokens RENAME TO refresh_tokens;
ALTER INDEX idx_customer_refresh_tokens_customer RENAME TO idx_refresh_tokens_owner;
ALTER INDEX idx_customer_refresh_tokens_token_hash RENAME TO idx_refresh_tokens_hash;
