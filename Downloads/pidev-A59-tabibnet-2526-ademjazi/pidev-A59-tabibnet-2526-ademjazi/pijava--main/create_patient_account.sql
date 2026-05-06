-- ============================================================
-- Création d'un compte patient de test
-- ============================================================

USE tabibnet;

-- Insérer un patient de test
-- Mot de passe: patient123 (hashé avec BCrypt)
INSERT INTO patients (email, password, roles, first_name, last_name, phone, is_active) 
VALUES (
    'patient@test.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    '["ROLE_PATIENT"]',
    'Fatma',
    'Trabelsi',
    '+216 22 987 654',
    1
)
ON DUPLICATE KEY UPDATE 
    password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    first_name = 'Fatma',
    last_name = 'Trabelsi',
    phone = '+216 22 987 654',
    is_active = 1;

-- Vérifier que le compte a été créé
SELECT 
    id,
    email,
    first_name,
    last_name,
    phone,
    is_active,
    created_at
FROM patients 
WHERE email = 'patient@test.com';

SELECT 'Compte patient cree avec succes!' AS Message;
SELECT 'Email: patient@test.com' AS Info;
SELECT 'Mot de passe: patient123' AS Info;
