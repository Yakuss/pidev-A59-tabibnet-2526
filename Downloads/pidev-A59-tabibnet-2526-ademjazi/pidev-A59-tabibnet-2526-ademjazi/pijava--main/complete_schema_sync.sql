-- ============================================================
-- COMPLETE SCHEMA SYNCHRONIZATION
-- Includes: Forum, Medical Records, and Health Magazine
-- ============================================================

USE pidev;

-- 1. Forum Specialities
CREATE TABLE IF NOT EXISTS specialite (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    description TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Forum Questions
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Forum Responses
CREATE TABLE IF NOT EXISTS reponse (
    id INT AUTO_INCREMENT PRIMARY KEY,
    contenu TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    likes INT DEFAULT 0,
    question_id INT,
    medecin_id INT,
    CONSTRAINT fk_reponse_question FOREIGN KEY (question_id) REFERENCES question(id) ON DELETE CASCADE,
    CONSTRAINT fk_reponse_medecin FOREIGN KEY (medecin_id) REFERENCES medecins(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Medical Documents (Container for Rapports/Ordonnances)
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Medical Reports
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Prescriptions
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Magazine
CREATE TABLE IF NOT EXISTS magazine (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    titre       VARCHAR(255) NOT NULL,
    description TEXT,
    image       VARCHAR(500),
    date_create DATETIME DEFAULT CURRENT_TIMESTAMP,
    statut      VARCHAR(20) DEFAULT 'draft',
    pdf_file    VARCHAR(500)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. Articles
CREATE TABLE IF NOT EXISTS article (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    titre       VARCHAR(255) NOT NULL,
    resume      TEXT,
    auteur      VARCHAR(150),
    date_pub    DATETIME DEFAULT CURRENT_TIMESTAMP,
    summary     TEXT,
    statut      VARCHAR(20) DEFAULT 'draft',
    image       VARCHAR(500),
    views       INT DEFAULT 0,
    id_magazine INT,
    public_cible VARCHAR(20),
    pdf_file    VARCHAR(500),
    CONSTRAINT fk_article_magazine FOREIGN KEY (id_magazine) REFERENCES magazine(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
