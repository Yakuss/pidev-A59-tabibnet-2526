-- ============================================================
-- Script d'initialisation complète de la base de données TabibNet
-- ============================================================

-- Créer la base de données si elle n'existe pas
CREATE DATABASE IF NOT EXISTS tabibnet CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE tabibnet;

-- ============================================================
-- Table: specialite
-- ============================================================
CREATE TABLE IF NOT EXISTS specialite (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Table: patients
-- ============================================================
CREATE TABLE IF NOT EXISTS patients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    roles JSON,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(20),
    date_of_birth DATE,
    address TEXT,
    is_active TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Table: medecins
-- ============================================================
CREATE TABLE IF NOT EXISTS medecins (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    roles JSON,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    specialty VARCHAR(100),
    governorate VARCHAR(100),
    phone VARCHAR(20),
    address TEXT,
    bio TEXT,
    consultation_fee DECIMAL(10,2),
    is_active TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_specialty (specialty),
    INDEX idx_governorate (governorate),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Table: admins
-- ============================================================
CREATE TABLE IF NOT EXISTS admins (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    roles JSON,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    is_active TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Table: appointments (rendez-vous)
-- ============================================================
CREATE TABLE IF NOT EXISTS appointments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    medecin_id INT NOT NULL,
    appointment_date DATETIME NOT NULL,
    status VARCHAR(50) DEFAULT 'pending',
    reason TEXT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (medecin_id) REFERENCES medecins(id) ON DELETE CASCADE,
    INDEX idx_patient (patient_id),
    INDEX idx_medecin (medecin_id),
    INDEX idx_date (appointment_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Table: question (Forum)
-- ============================================================
CREATE TABLE IF NOT EXISTS question (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    description LONGTEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    likes INT DEFAULT 0,
    status VARCHAR(50) DEFAULT 'active',
    image_name VARCHAR(255),
    specialite_id INT,
    patient_id INT,
    FOREIGN KEY (specialite_id) REFERENCES specialite(id) ON DELETE SET NULL,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    INDEX idx_specialite (specialite_id),
    INDEX idx_patient (patient_id),
    INDEX idx_created_at (created_at),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Table: reponse (Réponses du forum)
-- ============================================================
CREATE TABLE IF NOT EXISTS reponse (
    id INT AUTO_INCREMENT PRIMARY KEY,
    question_id INT NOT NULL,
    medecin_id INT,
    patient_id INT,
    contenu LONGTEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    likes INT DEFAULT 0,
    FOREIGN KEY (question_id) REFERENCES question(id) ON DELETE CASCADE,
    FOREIGN KEY (medecin_id) REFERENCES medecins(id) ON DELETE SET NULL,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE SET NULL,
    INDEX idx_question (question_id),
    INDEX idx_medecin (medecin_id),
    INDEX idx_patient (patient_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Table: magazine
-- ============================================================
CREATE TABLE IF NOT EXISTS magazine (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    image VARCHAR(500),
    date_create DATETIME DEFAULT CURRENT_TIMESTAMP,
    statut VARCHAR(20) DEFAULT 'draft',
    pdf_file VARCHAR(500),
    INDEX idx_statut (statut),
    INDEX idx_date (date_create)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Table: article
-- ============================================================
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
    CONSTRAINT fk_article_magazine
        FOREIGN KEY (id_magazine) REFERENCES magazine(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_magazine (id_magazine),
    INDEX idx_statut (statut),
    INDEX idx_date (date_pub)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Table: feedback
-- ============================================================
CREATE TABLE IF NOT EXISTS feedback (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT,
    medecin_id INT,
    appointment_id INT,
    rating INT CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (medecin_id) REFERENCES medecins(id) ON DELETE CASCADE,
    FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE,
    INDEX idx_patient (patient_id),
    INDEX idx_medecin (medecin_id),
    INDEX idx_appointment (appointment_id),
    INDEX idx_rating (rating)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Données initiales: Spécialités médicales
-- ============================================================
INSERT IGNORE INTO specialite (nom, description) VALUES
('Cardiologie', 'Spécialité médicale qui traite les maladies du cœur et des vaisseaux'),
('Dermatologie', 'Spécialité médicale qui traite les maladies de la peau'),
('Pédiatrie', 'Spécialité médicale qui traite les enfants'),
('Neurologie', 'Spécialité médicale qui traite les maladies du système nerveux'),
('Orthopédie', 'Spécialité médicale qui traite les maladies des os et des articulations'),
('Ophtalmologie', 'Spécialité médicale qui traite les maladies des yeux'),
('ORL', 'Oto-rhino-laryngologie - Spécialité qui traite les maladies des oreilles, du nez et de la gorge'),
('Gynécologie', 'Spécialité médicale qui traite les maladies de l\'appareil génital féminin'),
('Psychiatrie', 'Spécialité médicale qui traite les maladies mentales'),
('Médecine Générale', 'Médecine de premier recours');

-- ============================================================
-- Message de confirmation
-- ============================================================
SELECT 'Base de données TabibNet initialisée avec succès!' AS Message;
SELECT COUNT(*) AS 'Nombre de spécialités' FROM specialite;
