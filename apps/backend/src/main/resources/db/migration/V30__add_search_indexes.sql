CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_customers_first_name_trgm ON customers USING gin (first_name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_customers_last_name_trgm ON customers USING gin (last_name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_customers_email_trgm ON customers USING gin (email gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_customers_document_number ON customers (document_number);
CREATE INDEX IF NOT EXISTS idx_loan_types_name_trgm ON loan_types USING gin (name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_loan_types_description_trgm ON loan_types USING gin (description gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_loans_customer_id ON loans (customer_id);
CREATE INDEX IF NOT EXISTS idx_loans_loan_application_id ON loans (loan_application_id);
CREATE INDEX IF NOT EXISTS idx_loan_applications_customer_id ON loan_applications (customer_id);
CREATE INDEX IF NOT EXISTS idx_loan_applications_loan_type_id ON loan_applications (loan_type_id);
CREATE INDEX IF NOT EXISTS idx_payment_installments_loan_id ON payment_installments (loan_id);
