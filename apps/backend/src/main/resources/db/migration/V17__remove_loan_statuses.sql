-- V16: Remove loan_statuses catalog and status column from loans
-- Rationale: Loan lifecycle management (ACTIVE, PAID, DEFAULTED, WRITTEN_OFF) is
-- outside the scope of this technical test. Once a loan_application is APPROVED,
-- the resulting Loan is simply a financial record. No further status transitions
-- are modeled in this bounded context.

-- 1. Drop the FK constraint from loans to loan_statuses
ALTER TABLE loans DROP CONSTRAINT IF EXISTS fk_loans_status;
ALTER TABLE loans DROP CONSTRAINT IF EXISTS chk_loans_status;

-- 2. Remove the status column from loans
ALTER TABLE loans DROP COLUMN IF EXISTS status;

-- 3. Drop loan_statuses catalog table
DROP TABLE IF EXISTS loan_statuses;
