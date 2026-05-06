-- ============================================================
-- Script de configuration complète de la base de données TabibNet
-- Compatible avec les services Java (MedecinService, PatientService, etc.)
-- ============================================================

-- 1. Créer la base de données
CREATE DATABASE IF NOT EXISTS pidev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pidev;

-- 2. Table des Médecins (Medecins)
CREATE TABLE IF NOT EXISTS medecins (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    age INT,
    gender VARCHAR(20),
    is_active TINYINT(1) DEFAULT 1,
    roles JSON,
    phone_number VARCHAR(20),
    specialty VARCHAR(100),
    cin VARCHAR(20),
    address TEXT,
    governorate VARCHAR(100),
    education TEXT,
    experience TEXT,
    is_verified TINYINT(1) DEFAULT 0,
    ai_average_score DOUBLE,
    averageRating DOUBLE DEFAULT 0,
    totalReviews INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 3. Table des Patients (Patients)
CREATE TABLE IF NOT EXISTS patients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    age INT,
    gender VARCHAR(20),
    is_active TINYINT(1) DEFAULT 1,
    roles JSON,
    phone_number VARCHAR(20),
    address TEXT,
    date_of_birth DATETIME,
    has_insurance TINYINT(1) DEFAULT 0,
    insurance_number VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 4. Table des Administrateurs (Admins)
CREATE TABLE IF NOT EXISTS admins (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    age INT,
    gender VARCHAR(20),
    is_active TINYINT(1) DEFAULT 1,
    roles JSON,
    name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 5. Table des Spécialités (Specialite)
CREATE TABLE IF NOT EXISTS specialite (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS rendezvous (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patientId INT,
    medecinId INT,
    date VARCHAR(20),
    heure VARCHAR(20),
    statut VARCHAR(50) DEFAULT 'En attente',
    duration_minutes INT DEFAULT 30,
    department VARCHAR(100),
    message TEXT,
    FOREIGN KEY (patientId) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (medecinId) REFERENCES medecins(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 6.1 Tables pour le Calendrier
CREATE TABLE IF NOT EXISTS indisponibilite (
    id INT AUTO_INCREMENT PRIMARY KEY,
    date DATE NOT NULL,
    doctor_id INT NOT NULL,
    is_emergency TINYINT(1) DEFAULT 0,
    FOREIGN KEY (doctor_id) REFERENCES medecins(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS temps_travail (
    id INT AUTO_INCREMENT PRIMARY KEY,
    day_of_week VARCHAR(20),
    start_time TIME,
    end_time TIME,
    doctor_id INT,
    specific_date DATE,
    FOREIGN KEY (doctor_id) REFERENCES medecins(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS calendar_setting (
    id INT AUTO_INCREMENT PRIMARY KEY,
    slot_duration INT DEFAULT 30,
    pause_start TIME,
    pause_end TIME,
    doctor_id INT,
    FOREIGN KEY (doctor_id) REFERENCES medecins(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 7. Table des Évaluations (Feedback)
CREATE TABLE IF NOT EXISTS feedback (
    id INT AUTO_INCREMENT PRIMARY KEY,
    rendezVousId INT,
    commentaire TEXT,
    note INT,
    dateCreation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (rendezVousId) REFERENCES rendezvous(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 8. Table des Magazines
CREATE TABLE IF NOT EXISTS magazine (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    image VARCHAR(500),
    date_create DATETIME DEFAULT CURRENT_TIMESTAMP,
    statut VARCHAR(20) DEFAULT 'draft',
    pdf_file VARCHAR(500)
) ENGINE=InnoDB;

-- 9. Table des Articles
CREATE TABLE IF NOT EXISTS article (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    resume TEXT,
    auteur VARCHAR(150),
    date_pub DATETIME DEFAULT CURRENT_TIMESTAMP,
    summary TEXT,
    statut VARCHAR(20) DEFAULT 'draft',
    image VARCHAR(500),
    views INT DEFAULT 0,
    id_magazine INT,
    public_cible VARCHAR(20),
    pdf_file VARCHAR(500),
    FOREIGN KEY (id_magazine) REFERENCES magazine(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 10. Forum Questions
CREATE TABLE IF NOT EXISTS question (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    likes INT DEFAULT 0,
    status VARCHAR(50) DEFAULT 'Ouvert',
    image_name VARCHAR(255),
    specialite_id INT,
    patient_id INT,
    CONSTRAINT fk_question_specialite FOREIGN KEY (specialite_id) REFERENCES specialite(id) ON DELETE SET NULL,
    CONSTRAINT fk_question_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 11. Forum Responses
CREATE TABLE IF NOT EXISTS reponse (
    id INT AUTO_INCREMENT PRIMARY KEY,
    contenu TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    likes INT DEFAULT 0,
    question_id INT,
    medecin_id INT,
    CONSTRAINT fk_reponse_question FOREIGN KEY (question_id) REFERENCES question(id) ON DELETE CASCADE,
    CONSTRAINT fk_reponse_medecin FOREIGN KEY (medecin_id) REFERENCES medecins(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 12. Medical Documents
CREATE TABLE IF NOT EXISTS documents (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom_fichier VARCHAR(255) NOT NULL DEFAULT '',
    chemin_fichier VARCHAR(255) DEFAULT '',
    type VARCHAR(255),
    taille VARCHAR(50),
    description TEXT,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    nb_rapports INT DEFAULT 0,
    nb_ordonnances INT DEFAULT 0,
    medecin_id INT,
    patient_id INT,
    CONSTRAINT fk_doc_medecin FOREIGN KEY (medecin_id) REFERENCES medecins(id) ON DELETE SET NULL,
    CONSTRAINT fk_doc_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 13. Medical Reports
CREATE TABLE IF NOT EXISTS rapport (
    id INT AUTO_INCREMENT PRIMARY KEY,
    consultation_reason VARCHAR(255),
    diagnosis TEXT,
    observations TEXT,
    recommendations TEXT,
    treatments TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    patient_id INT,
    medecin_id INT,
    appointment_id INT,
    document_id INT,
    CONSTRAINT fk_rapport_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_rapport_medecin FOREIGN KEY (medecin_id) REFERENCES medecins(id) ON DELETE SET NULL,
    CONSTRAINT fk_rapport_rdv FOREIGN KEY (appointment_id) REFERENCES rendezvous(id) ON DELETE SET NULL,
    CONSTRAINT fk_rapport_doc FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 14. Prescriptions
CREATE TABLE IF NOT EXISTS ordonnances (
    id INT AUTO_INCREMENT PRIMARY KEY,
    date_ordonnance TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    diagnosis TEXT,
    medicament TEXT,
    posologie TEXT,
    instructions TEXT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    patient_id INT,
    medecin_id INT,
    appointment_id INT,
    document_id INT,
    CONSTRAINT fk_ord_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_ord_medecin FOREIGN KEY (medecin_id) REFERENCES medecins(id) ON DELETE SET NULL,
    CONSTRAINT fk_ord_rdv FOREIGN KEY (appointment_id) REFERENCES rendezvous(id) ON DELETE SET NULL,
    CONSTRAINT fk_ord_doc FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 15. Insérer des spécialités de test
INSERT IGNORE INTO specialite (nom, description) VALUES
('Cardiologie', 'Maladies du cœur'),
('Dermatologie', 'Maladies de la peau'),
('Pédiatrie', 'Santé des enfants'),
('Gynécologie', 'Santé de la femme'),
('Ophtalmologie', 'Santé des yeux');

-- 11. Insérer un compte Admin par défaut (Mot de passe: 12345678)
INSERT IGNORE INTO admins (email, password, first_name, last_name, roles, is_active, name) 
VALUES ('admin@gmail.com', '$2a$10$89x.X0e8Y3y6Yy3y6Yy3yO.y3y6Yy3y6Yy3y6Yy3y6Yy3y6Yy', 'Admin', 'System', '["ROLE_ADMIN"]', 1, 'Admin');

SELECT 'Base de données PIDEV configurée avec succès !' AS Message;
