CREATE TABLE customers (
    id UUID PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    document_type VARCHAR(10) NOT NULL,
    document_number VARCHAR(50) NOT NULL UNIQUE,
    base_salary DECIMAL(15, 2) NOT NULL CHECK (base_salary >= 0),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_customers_document_number ON customers(document_number);

CREATE TABLE loan_types (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    annual_interest_rate DECIMAL(5, 2) NOT NULL,
    automatic_validation_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    minimum_amount DECIMAL(15, 2) NOT NULL,
    maximum_amount DECIMAL(15, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE loan_applications (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    loan_type_id UUID NOT NULL,
    requested_amount DECIMAL(15, 2) NOT NULL,
    term_in_months INT NOT NULL CHECK (term_in_months > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    monthly_payment DECIMAL(15, 2),
    decision_reason VARCHAR(500),
    evaluated_at TIMESTAMP WITH TIME ZONE,
    version INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_loan_applications_customer
        FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_loan_applications_loan_type
        FOREIGN KEY (loan_type_id) REFERENCES loan_types(id),
    CONSTRAINT chk_loan_applications_status
        CHECK (status IN ('PENDING', 'MANUAL_REVIEW', 'APPROVED', 'REJECTED'))
);

CREATE INDEX idx_loan_applications_customer ON loan_applications(customer_id);
CREATE INDEX idx_loan_applications_status ON loan_applications(status);

CREATE TABLE loans (
    id UUID PRIMARY KEY,
    loan_application_id UUID NOT NULL UNIQUE,
    customer_id UUID NOT NULL,
    principal_amount DECIMAL(15, 2) NOT NULL,
    annual_interest_rate DECIMAL(5, 2) NOT NULL,
    monthly_interest_rate DECIMAL(8, 6) NOT NULL,
    term_in_months INT NOT NULL,
    monthly_payment DECIMAL(15, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    approved_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_loans_application
        FOREIGN KEY (loan_application_id) REFERENCES loan_applications(id),
    CONSTRAINT fk_loans_customer
        FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT chk_loans_status
        CHECK (status IN ('ACTIVE', 'PAID', 'DEFAULTED', 'WRITTEN_OFF'))
);

CREATE INDEX idx_loans_customer ON loans(customer_id);
CREATE INDEX idx_loans_status ON loans(status);

CREATE TABLE payment_installments (
    id UUID PRIMARY KEY,
    loan_id UUID NOT NULL,
    installment_number INT NOT NULL,
    due_date DATE NOT NULL,
    opening_balance DECIMAL(15, 2) NOT NULL,
    payment_amount DECIMAL(15, 2) NOT NULL,
    principal_amount DECIMAL(15, 2) NOT NULL,
    interest_amount DECIMAL(15, 2) NOT NULL,
    closing_balance DECIMAL(15, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_payment_installments_loan
        FOREIGN KEY (loan_id) REFERENCES loans(id) ON DELETE CASCADE,
    CONSTRAINT chk_installment_number
        CHECK (installment_number > 0)
);

CREATE INDEX idx_payment_installments_loan ON payment_installments(loan_id);
