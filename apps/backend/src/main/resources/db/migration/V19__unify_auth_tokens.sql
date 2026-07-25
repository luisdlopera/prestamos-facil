-- V18: Unificar refresh_tokens y password_reset_tokens en auth_tokens
-- Rationale: Ambos tipos de tokens pertenecen conceptualmente a la identidad central (users)
-- y comparten estructura (user_id, token_hash, expires_at, estado). Unificarlos simplifica
-- el esquema y desvincula la seguridad de la tabla de clientes (customers).

CREATE TABLE auth_tokens (
    id           UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id      UUID         NOT NULL,
    type         VARCHAR(30)  NOT NULL, -- 'REFRESH', 'PASSWORD_RESET'
    token_hash   VARCHAR(255) NOT NULL,
    expires_at   TIMESTAMPTZ  NOT NULL,
    revoked      BOOLEAN      NOT NULL DEFAULT false,
    used         BOOLEAN      NOT NULL DEFAULT false,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_auth_tokens PRIMARY KEY (id),
    CONSTRAINT fk_auth_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_auth_tokens_hash ON auth_tokens(token_hash);
CREATE INDEX idx_auth_tokens_user_type ON auth_tokens(user_id, type);

-- Migrar datos de refresh_tokens
INSERT INTO auth_tokens (id, user_id, type, token_hash, expires_at, revoked, created_at, updated_at)
SELECT id, user_id, 'REFRESH', token_hash, expires_at, revoked, created_at, updated_at
FROM refresh_tokens;

-- Migrar datos de password_reset_tokens asociándolos al user_id correspondiente
INSERT INTO auth_tokens (id, user_id, type, token_hash, expires_at, used, created_at, updated_at)
SELECT prt.id, c.user_id, 'PASSWORD_RESET', prt.token_hash, prt.expires_at, prt.used, prt.created_at, prt.updated_at
FROM password_reset_tokens prt
JOIN customers c ON c.id = prt.customer_id
WHERE c.user_id IS NOT NULL;

-- Eliminar las tablas anteriores ya consolidadas
DROP TABLE password_reset_tokens;
DROP TABLE refresh_tokens;
