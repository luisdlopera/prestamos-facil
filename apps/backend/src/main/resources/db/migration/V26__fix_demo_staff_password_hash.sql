-- V25 used an invalid/truncated bcrypt value. Regenerate the documented demo hash.
-- Password for both accounts: !Pass.1234
UPDATE users
SET password_hash = '$2b$12$aXAYb8C92XPyBmsC3SEBaO0OaAPNAQydpRICdktQhZkfAxAJPmF3.',
    failed_login_attempts = 0,
    blocked_until = NULL,
    enabled = TRUE,
    updated_at = NOW()
WHERE email IN ('admin@prestamosfacil.com', 'analista@prestamosfacil.com');
