-- Seed: usuario admin para desarrollo. Password: admin123
INSERT INTO app_user (email, password, full_name, role, plan, onboarding_complete)
VALUES (
    'admin@cts.dev',
    '$2a$10$bEeGEZjKABymE8rNj8pdR.qVrXqAEd/o6Y0x3vmJLB7ewBQNISn3G',
    'Admin',
    'ADMIN',
    'FREE',
    TRUE
) ON CONFLICT (email) DO NOTHING;