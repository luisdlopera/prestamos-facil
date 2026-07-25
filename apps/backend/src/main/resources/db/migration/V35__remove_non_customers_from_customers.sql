-- V35: Remove non-customer accounts from customers table
DELETE FROM customers
WHERE user_id IN (
    SELECT id FROM users WHERE UPPER(role) NOT IN ('CUSTOMER')
);
