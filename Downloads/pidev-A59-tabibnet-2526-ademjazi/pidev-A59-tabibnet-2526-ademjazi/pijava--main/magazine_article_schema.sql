-- ============================================================
-- Tables Magazine & Article pour la base pidev
-- ============================================================

CREATE TABLE IF NOT EXISTS magazine (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    titre       VARCHAR(255) NOT NULL,
    description TEXT,
    image       VARCHAR(500),
    date_create DATETIME DEFAULT CURRENT_TIMESTAMP,
    statut      VARCHAR(20) DEFAULT 'draft',   -- draft | published | archived
    pdf_file    VARCHAR(500)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS article (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    titre       VARCHAR(255) NOT NULL,
    resume      TEXT,
    auteur      VARCHAR(150),
    date_pub    DATETIME DEFAULT CURRENT_TIMESTAMP,
    summary     TEXT,
    statut      VARCHAR(20) DEFAULT 'draft',   -- draft | published
    image       VARCHAR(500),
    views       INT DEFAULT 0,
    id_magazine INT,
    public_cible VARCHAR(20),                  -- Enfants | Jeunes | Adultes
    pdf_file    VARCHAR(500),
    CONSTRAINT fk_article_magazine
        FOREIGN KEY (id_magazine) REFERENCES magazine(id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
