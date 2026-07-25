-- Persistence-level invariants for the public API and domain rules.

ALTER TABLE customers
    ADD CONSTRAINT chk_customers_first_name_not_blank CHECK (length(btrim(first_name)) > 0),
    ADD CONSTRAINT chk_customers_last_name_not_blank CHECK (length(btrim(last_name)) > 0),
    ADD CONSTRAINT chk_customers_salary_range CHECK (base_salary >= 0 AND base_salary <= 15000000.00);

ALTER TABLE loan_types
    ADD CONSTRAINT chk_loan_types_interest_rate_range CHECK (annual_interest_rate >= 0 AND annual_interest_rate <= 100),
    ADD CONSTRAINT chk_loan_types_minimum_amount_positive CHECK (minimum_amount > 0),
    ADD CONSTRAINT chk_loan_types_amount_range CHECK (maximum_amount >= minimum_amount);

ALTER TABLE loan_applications
    ADD CONSTRAINT chk_loan_applications_amount_positive CHECK (requested_amount > 0),
    ADD CONSTRAINT chk_loan_applications_term_positive CHECK (term_in_months > 0);

ALTER TABLE loans
    ADD CONSTRAINT chk_loans_principal_positive CHECK (principal_amount > 0),
    ADD CONSTRAINT chk_loans_interest_rate_range CHECK (annual_interest_rate >= 0 AND annual_interest_rate <= 100),
    ADD CONSTRAINT chk_loans_term_positive CHECK (term_in_months > 0),
    ADD CONSTRAINT chk_loans_monthly_payment_nonnegative CHECK (monthly_payment >= 0);

ALTER TABLE payment_installments
    ADD CONSTRAINT chk_installments_amounts_nonnegative CHECK (
        opening_balance >= 0 AND payment_amount >= 0 AND principal_amount >= 0
        AND interest_amount >= 0 AND closing_balance >= 0
    ),
    ADD CONSTRAINT uq_installments_loan_number UNIQUE (loan_id, installment_number);
