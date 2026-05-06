-- ============================================================
-- Création d'un compte médecin de test
-- ============================================================

USE tabibnet;

-- Insérer un médecin de test
-- Mot de passe: medecin123 (hashé avec BCrypt)
INSERT INTO medecins (email, password, roles, first_name, last_name, specialty, governorate, phone, is_active) 
VALUES (
    'medecin@test.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    '["ROLE_MEDECIN"]',
    'Dr. Ahmed',
    'Ben Ali',
    'Cardiologie',
    'Tunis',
    '+216 20 123 456',
    1
)
ON DUPLICATE KEY UPDATE 
    password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    first_name = 'Dr. Ahmed',
    last_name = 'Ben Ali',
    specialty = 'Cardiologie',
    governorate = 'Tunis',
    phone = '+216 20 123 456',
    is_active = 1;

-- Vérifier que le compte a été créé
SELECT 
    id,
    email,
    first_name,
    last_name,
    specialty,
    governorate,
    is_active,
    created_at
FROM medecins 
WHERE email = 'medecin@test.com';

SELECT 'Compte medecin cree avec succes!' AS Message;
SELECT 'Email: medecin@test.com' AS Info;
SELECT 'Mot de passe: medecin123' AS Info;
