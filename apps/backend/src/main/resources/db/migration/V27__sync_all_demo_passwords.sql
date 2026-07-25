-- Keep all documented local/demo accounts synchronized with README credentials.
-- Password for these accounts: !Pass.1234
UPDATE users
SET password_hash = '$2b$12$kEdmixFOOPiHyH3G1eRG3uQT2GLg.lUdUbaSzc8Rb7OnTeHSl9x.K',
    failed_login_attempts = 0,
    blocked_until = NULL,
    enabled = TRUE,
    updated_at = NOW()
WHERE email IN (
    'admin@prestamosfacil.com',
    'analista@prestamosfacil.com',
    'cliente@prestamosfacil.com'
);
