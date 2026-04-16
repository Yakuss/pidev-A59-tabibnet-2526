CREATE DATABASE IF NOT EXISTS tabibnet;
USE tabibnet;

CREATE TABLE IF NOT EXISTS rendezvous (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nomPatient VARCHAR(100) NOT NULL,
    medecin VARCHAR(100) NOT NULL,
    date VARCHAR(20) NOT NULL,
    heure VARCHAR(10) NOT NULL,
    statut VARCHAR(20) DEFAULT 'En attente'
);

CREATE TABLE IF NOT EXISTS feedback (
    id INT AUTO_INCREMENT PRIMARY KEY,
    commentaire TEXT NOT NULL,
    note INT NOT NULL,
    rendezVousId INT NOT NULL,
    FOREIGN KEY (rendezVousId) REFERENCES rendezvous(id) ON DELETE CASCADE
);
