-- ============================================================
-- Création de tous les comptes de test
-- ============================================================

USE tabibnet;

-- ============================================================
-- COMPTE MEDECIN
-- Email: medecin@test.com
-- Mot de passe: medecin123
-- ============================================================
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

-- ============================================================
-- COMPTE PATIENT
-- Email: patient@test.com
-- Mot de passe: patient123
-- ============================================================
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

-- ============================================================
-- VERIFICATION
-- ============================================================
SELECT '========================================' AS '';
SELECT 'COMPTES DE TEST CREES AVEC SUCCES!' AS '';
SELECT '========================================' AS '';

SELECT '' AS '';
SELECT '--- COMPTE ADMINISTRATEUR ---' AS '';
SELECT 'Email: admin@gmail.com' AS '';
SELECT 'Mot de passe: admin123' AS '';

SELECT '' AS '';
SELECT '--- COMPTE MEDECIN ---' AS '';
SELECT 'Email: medecin@test.com' AS '';
SELECT 'Mot de passe: medecin123' AS '';
SELECT CONCAT('Nom: ', first_name, ' ', last_name) AS '' FROM medecins WHERE email = 'medecin@test.com';
SELECT CONCAT('Specialite: ', specialty) AS '' FROM medecins WHERE email = 'medecin@test.com';

SELECT '' AS '';
SELECT '--- COMPTE PATIENT ---' AS '';
SELECT 'Email: patient@test.com' AS '';
SELECT 'Mot de passe: patient123' AS '';
SELECT CONCAT('Nom: ', first_name, ' ', last_name) AS '' FROM patients WHERE email = 'patient@test.com';

SELECT '' AS '';
SELECT '========================================' AS '';
