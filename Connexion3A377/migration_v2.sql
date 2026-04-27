-- Migration v2 : ajouter publicCible et pdfFile à la table article
-- Compatible MySQL 5.7+ et MySQL 8+
-- Exécuter une seule fois sur la base pidev_1

ALTER TABLE article
    ADD COLUMN publicCible VARCHAR(20) NULL AFTER views,
    ADD COLUMN pdfFile     VARCHAR(500) NULL AFTER publicCible;
