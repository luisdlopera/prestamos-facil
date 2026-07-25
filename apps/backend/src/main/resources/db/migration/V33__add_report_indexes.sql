-- V32: Add indexes for reports and queries performance
-- Purpose:
--   1. Index on loans.approved_at for the reports aggregation query (WHERE approved_at IS NOT NULL)
--   2. Index on loan_applications.created_at for reports ordering/sorting

CREATE INDEX IF NOT EXISTS idx_loans_approved_at ON loans (approved_at);
CREATE INDEX IF NOT EXISTS idx_loan_applications_created_at ON loan_applications (created_at);
