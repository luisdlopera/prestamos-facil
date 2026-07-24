INSERT INTO loan_types (id, name, annual_interest_rate, automatic_validation_enabled, minimum_amount, maximum_amount, active)
VALUES
    ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'Libre Inversión', 18.50, TRUE, 1000000, 50000000, TRUE),
    ('b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Vivienda', 12.00, FALSE, 5000000, 500000000, TRUE),
    ('c3d4e5f6-a7b8-9012-cdef-123456789012', 'Educativo', 10.00, TRUE, 500000, 30000000, TRUE),
    ('d4e5f6a7-b8c9-0123-defa-234567890123', 'Compra de Vehículo', 16.00, TRUE, 5000000, 150000000, TRUE),
    ('e5f6a7b8-c9d0-1234-efab-345678901234', 'Microcrédito', 24.00, TRUE, 200000, 5000000, TRUE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (id, name)
VALUES
    ('f6a7b8c9-d0e1-2345-fabc-456789012345', 'ADMIN'),
    ('a7b8c9d0-e1f2-3456-abcd-567890123456', 'ASESOR'),
    ('b8c9d0e1-f2a3-4567-bcde-678901234567', 'COBRADOR')
ON CONFLICT (name) DO NOTHING;

INSERT INTO users (id, name, email, password_hash, enabled)
VALUES (
    'c9d0e1f2-a3b4-5678-cdef-789012345678',
    'Admin',
    'admin@prestamosfacil.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    TRUE
) ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT 'c9d0e1f2-a3b4-5678-cdef-789012345678', id FROM roles WHERE name = 'ADMIN'
ON CONFLICT DO NOTHING;
