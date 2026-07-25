-- V31: Clean up redundant fields (monthly_interest_rate in loans, email in customers)
-- Purpose:
--   1. Drop redundant `monthly_interest_rate` from loans table (calculated dynamically as annual_interest_rate / 12).
--   2. Drop redundant `email` from customers table (unified in users.email via 1:1 user_id FK).

ALTER TABLE loans DROP COLUMN IF EXISTS monthly_interest_rate;
ALTER TABLE customers DROP COLUMN IF EXISTS email;
