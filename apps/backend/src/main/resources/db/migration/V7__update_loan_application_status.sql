ALTER TABLE loan_applications
    DROP CONSTRAINT IF EXISTS chk_loan_applications_status;

UPDATE loan_applications SET status = 'PENDING_REVIEW' WHERE status = 'PENDING';

ALTER TABLE loan_applications
    ADD CONSTRAINT chk_loan_applications_status
        CHECK (status IN ('PENDING_REVIEW', 'MANUAL_REVIEW', 'APPROVED', 'REJECTED'));

ALTER TABLE loan_applications
    ALTER COLUMN status SET DEFAULT 'PENDING_REVIEW';


