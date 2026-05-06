-- ============================================================
-- Script pour remplir la base de données avec des données de test
-- Au moins 5 entrées par table
-- ============================================================

USE tabibnet;

-- ============================================================
-- 1. MEDECINS (5 médecins)
-- ============================================================
INSERT INTO medecins (email, password, roles, first_name, last_name, specialty, governorate, phone, address, bio, is_active) VALUES
('dr.ahmed@tabibnet.tn', '$2a$10$abcdefghijklmnopqrstu123456789', '["ROLE_MEDECIN"]', 'Dr. Ahmed', 'Ben Ali', 'Cardiologie', 'Tunis', '+216 20 123 456', '15 Avenue Habib Bourguiba, Tunis', 'Cardiologue expérimenté avec 15 ans d\'expérience', 1),
('dr.fatma@tabibnet.tn', '$2a$10$abcdefghijklmnopqrstu123456789', '["ROLE_MEDECIN"]', 'Dr. Fatma', 'Trabelsi', 'Dermatologie', 'Sfax', '+216 22 234 567', '25 Rue de la République, Sfax', 'Spécialiste en dermatologie esthétique', 1),
('dr.mohamed@tabibnet.tn', '$2a$10$abcdefghijklmnopqrstu123456789', '["ROLE_MEDECIN"]', 'Dr. Mohamed', 'Gharbi', 'Pédiatrie', 'Sousse', '+216 23 345 678', '10 Avenue Léopold Sédar Senghor, Sousse', 'Pédiatre passionné par la santé des enfants', 1),
('dr.leila@tabibnet.tn', '$2a$10$abcdefghijklmnopqrstu123456789', '["ROLE_MEDECIN"]', 'Dr. Leila', 'Mansour', 'Gynécologie', 'Tunis', '+216 24 456 789', '30 Rue de Marseille, Tunis', 'Gynécologue-obstétricienne', 1),
('dr.karim@tabibnet.tn', '$2a$10$abcdefghijklmnopqrstu123456789', '["ROLE_MEDECIN"]', 'Dr. Karim', 'Bouazizi', 'Orthopédie', 'Ariana', '+216 25 567 890', '5 Avenue de la Liberté, Ariana', 'Chirurgien orthopédiste', 1),
('medecin@test.com', '$2a$10$abcdefghijklmnopqrstu3fa05c3', '["ROLE_MEDECIN"]', 'Dr. Ahmed', 'Ben Ali', 'Cardiologie', 'Tunis', '+216 20 123 456', '15 Avenue Habib Bourguiba, Tunis', 'Cardiologue de test', 1)
ON DUPLICATE KEY UPDATE first_name=VALUES(first_name);

-- ============================================================
-- 2. PATIENTS (5 patients)
-- ============================================================
INSERT INTO patients (email, password, roles, first_name, last_name, phone, address, is_active) VALUES
('patient1@test.tn', '$2a$10$abcdefghijklmnopqrstu123456789', '["ROLE_PATIENT"]', 'Amira', 'Sassi', '+216 26 111 222', '12 Rue de la Paix, Tunis', 1),
('patient2@test.tn', '$2a$10$abcdefghijklmnopqrstu123456789', '["ROLE_PATIENT"]', 'Youssef', 'Hamdi', '+216 27 222 333', '8 Avenue de Carthage, Sfax', 1),
('patient3@test.tn', '$2a$10$abcdefghijklmnopqrstu123456789', '["ROLE_PATIENT"]', 'Sarra', 'Mejri', '+216 28 333 444', '20 Rue du Lac, Sousse', 1),
('patient4@test.tn', '$2a$10$abcdefghijklmnopqrstu123456789', '["ROLE_PATIENT"]', 'Mehdi', 'Jebali', '+216 29 444 555', '15 Avenue Mongi Slim, Bizerte', 1),
('patient5@test.tn', '$2a$10$abcdefghijklmnopqrstu123456789', '["ROLE_PATIENT"]', 'Nour', 'Karoui', '+216 20 555 666', '25 Rue de France, Nabeul', 1),
('patient@test.com', '$2a$10$abcdefghijklmnopqrstu8523390d', '["ROLE_PATIENT"]', 'Fatma', 'Trabelsi', '+216 22 987 654', '10 Avenue de la Liberté, Tunis', 1)
ON DUPLICATE KEY UPDATE first_name=VALUES(first_name);

-- ============================================================
-- 3. APPOINTMENTS (5 rendez-vous)
-- ============================================================
INSERT INTO appointments (patient_id, medecin_id, appointment_date, status, reason, notes) VALUES
(1, 1, '2026-05-10 09:00:00', 'confirmed', 'Consultation cardiologie', 'Premier rendez-vous'),
(2, 2, '2026-05-11 10:30:00', 'confirmed', 'Problème de peau', 'Acné sévère'),
(3, 3, '2026-05-12 14:00:00', 'pending', 'Vaccination enfant', 'Rappel vaccin'),
(4, 4, '2026-05-13 11:00:00', 'confirmed', 'Suivi grossesse', 'Échographie'),
(5, 5, '2026-05-14 15:30:00', 'pending', 'Douleur genou', 'Consultation orthopédie');

-- ============================================================
-- 4. QUESTIONS (5 questions du forum)
-- ============================================================
INSERT INTO question (titre, description, patient_id, specialite_id, status, likes) VALUES
('Comment prévenir les maladies cardiovasculaires?', 'Je voudrais savoir quelles sont les meilleures pratiques pour maintenir un cœur en bonne santé.', 1, 1, 'active', 12),
('Traitement naturel pour l\'acné', 'Existe-t-il des remèdes naturels efficaces contre l\'acné?', 2, 2, 'active', 8),
('Calendrier de vaccination pour bébé', 'Quels sont les vaccins obligatoires pour un nouveau-né en Tunisie?', 3, 3, 'active', 15),
('Exercices pendant la grossesse', 'Quels exercices sont recommandés pendant la grossesse?', 4, 8, 'active', 10),
('Douleur au genou après le sport', 'J\'ai mal au genou après chaque séance de sport, que faire?', 5, 5, 'active', 6);

-- ============================================================
-- 5. REPONSES (5 réponses)
-- ============================================================
INSERT INTO reponse (question_id, medecin_id, contenu, likes) VALUES
(1, 1, 'Pour prévenir les maladies cardiovasculaires, il est essentiel de maintenir une alimentation équilibrée, faire de l\'exercice régulièrement, éviter le tabac et gérer le stress.', 8),
(2, 2, 'Les remèdes naturels comme le miel, l\'aloe vera et le thé vert peuvent aider, mais consultez un dermatologue pour un traitement adapté.', 5),
(3, 3, 'En Tunisie, les vaccins obligatoires incluent le BCG, le DTC, la polio et l\'hépatite B. Consultez votre pédiatre pour le calendrier complet.', 12),
(4, 4, 'La marche, la natation et le yoga prénatal sont excellents pendant la grossesse. Évitez les sports à risque de chute.', 7),
(5, 5, 'La douleur au genou peut être due à une surcharge. Repos, glace et consultation orthopédique sont recommandés.', 4);

-- ============================================================
-- 6. MAGAZINES (5 magazines)
-- ============================================================
INSERT INTO magazine (titre, description, statut, date_create) VALUES
('Santé & Bien-être', 'Magazine mensuel sur la santé et le bien-être', 'published', NOW()),
('Nutrition Moderne', 'Tout sur l\'alimentation saine et équilibrée', 'published', NOW()),
('Pédiatrie Pratique', 'Guide pour les parents', 'published', NOW()),
('Cardiologie Aujourd\'hui', 'Actualités en cardiologie', 'published', NOW()),
('Dermatologie Esthétique', 'Soins de la peau et beauté', 'published', NOW());

-- ============================================================
-- 7. ARTICLES (5 articles)
-- ============================================================
INSERT INTO article (titre, resume, auteur, id_magazine, statut, public_cible, views) VALUES
('Les bienfaits de la marche quotidienne', 'Découvrez pourquoi marcher 30 minutes par jour améliore votre santé', 'Dr. Ahmed Ben Ali', 1, 'published', 'Adultes', 150),
('Alimentation anti-inflammatoire', 'Les aliments qui réduisent l\'inflammation dans le corps', 'Dr. Fatma Trabelsi', 2, 'published', 'Adultes', 200),
('Sommeil de bébé: Guide complet', 'Comment aider votre bébé à bien dormir', 'Dr. Mohamed Gharbi', 3, 'published', 'Enfants', 180),
('Prévention de l\'infarctus', 'Les signes avant-coureurs et la prévention', 'Dr. Ahmed Ben Ali', 4, 'published', 'Adultes', 220),
('Routine de soins de la peau', 'Les étapes essentielles pour une peau saine', 'Dr. Fatma Trabelsi', 5, 'published', 'Jeunes', 175);

-- ============================================================
-- 8. FEEDBACK (5 évaluations)
-- ============================================================
INSERT INTO feedback (patient_id, medecin_id, appointment_id, rating, comment) VALUES
(1, 1, 1, 5, 'Excellent médecin, très à l\'écoute et professionnel'),
(2, 2, 2, 4, 'Bonne consultation, traitement efficace'),
(3, 3, 3, 5, 'Très bon pédiatre, mon enfant était à l\'aise'),
(4, 4, 4, 5, 'Suivi de grossesse impeccable, je recommande'),
(5, 5, 5, 4, 'Bon diagnostic, traitement en cours');

-- ============================================================
-- VERIFICATION
-- ============================================================
SELECT '========================================' AS '';
SELECT 'BASE DE DONNEES REMPLIE AVEC SUCCES!' AS '';
SELECT '========================================' AS '';

SELECT '' AS '';
SELECT CONCAT('Médecins: ', COUNT(*), ' entrées') AS '' FROM medecins;
SELECT CONCAT('Patients: ', COUNT(*), ' entrées') AS '' FROM patients;
SELECT CONCAT('Spécialités: ', COUNT(*), ' entrées') AS '' FROM specialite;
SELECT CONCAT('Rendez-vous: ', COUNT(*), ' entrées') AS '' FROM appointments;
SELECT CONCAT('Questions: ', COUNT(*), ' entrées') AS '' FROM question;
SELECT CONCAT('Réponses: ', COUNT(*), ' entrées') AS '' FROM reponse;
SELECT CONCAT('Magazines: ', COUNT(*), ' entrées') AS '' FROM magazine;
SELECT CONCAT('Articles: ', COUNT(*), ' entrées') AS '' FROM article;
SELECT CONCAT('Évaluations: ', COUNT(*), ' entrées') AS '' FROM feedback;

SELECT '' AS '';
SELECT '========================================' AS '';
