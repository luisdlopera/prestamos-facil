ALTER TABLE customers
    ADD COLUMN password_hash VARCHAR(255),
    ADD COLUMN failed_login_attempts INT DEFAULT 0 NOT NULL,
    ADD COLUMN blocked_until TIMESTAMP WITH TIME ZONE,
    ADD COLUMN last_login_at TIMESTAMP WITH TIME ZONE;

CREATE TABLE customer_refresh_tokens (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_customer_refresh_tokens_customer
        FOREIGN KEY (customer_id) REFERENCES customers(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_customer_refresh_tokens_customer
    ON customer_refresh_tokens(customer_id);
CREATE INDEX idx_customer_refresh_tokens_token_hash
    ON customer_refresh_tokens(token_hash);
