-- Keep the documented local/demo staff credentials in sync with the seeded users.
-- Password for both accounts: !Pass.1234
UPDATE users
SET password_hash = '$2b$12$kEdmixFOOPiH3G1eRG3uQT2GLg.lUdUbaSzc8Rb7OnTeHSl9x.K',
    failed_login_attempts = 0,
    blocked_until = NULL,
    enabled = TRUE,
    updated_at = NOW()
WHERE email IN ('admin@prestamosfacil.com', 'analista@prestamosfacil.com');
