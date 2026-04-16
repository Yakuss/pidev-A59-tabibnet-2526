CREATE DATABASE IF NOT EXISTS 3a37;
USE 3a37;

CREATE TABLE IF NOT EXISTS personne (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    prenom VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS ordonnances (
    id INT AUTO_INCREMENT PRIMARY KEY,
    date_ordonnance DATETIME NOT NULL,
    diagnosis VARCHAR(255) NOT NULL,
    medicament VARCHAR(255) NOT NULL,
    posologie VARCHAR(255) NOT NULL,
    notes TEXT,
    instructions TEXT,
    created_at DATETIME,
    updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS rapport (
    id INT AUTO_INCREMENT PRIMARY KEY,
    consultation_reason VARCHAR(255) NOT NULL,
    diagnosis VARCHAR(255) NOT NULL,
    observations TEXT NOT NULL,
    recommendations TEXT NOT NULL,
    treatments TEXT NOT NULL,
    created_at DATETIME,
    updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS document (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom_fichier VARCHAR(255) NOT NULL,
    chemin_fichier VARCHAR(255) NOT NULL,
    type VARCHAR(50) DEFAULT 'pdf'
);
