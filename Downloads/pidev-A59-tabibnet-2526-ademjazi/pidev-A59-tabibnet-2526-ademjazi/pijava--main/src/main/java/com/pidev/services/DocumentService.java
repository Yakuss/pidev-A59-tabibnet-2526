package com.pidev.services;

import com.pidev.models.Document;
import com.pidev.utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DocumentService implements IService<Document> {

    private Connection getConnection() {
        return DataSource.getInstance().getConnection();
    }

    private void checkSchema() {
        try (Statement st = getConnection().createStatement()) {
            // Création de la table si elle n'existe pas
            st.execute("CREATE TABLE IF NOT EXISTS documents (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY" +
                    ") ENGINE=InnoDB");
            System.out.println("Vérification table 'documents' terminée.");
        } catch (SQLException e) {
            System.err.println("Erreur création table documents: " + e.getMessage());
        }

        // Ajout de TOUTES les colonnes une par une pour être sûr qu'elles existent
        String[] columns = {
                "ALTER TABLE documents ADD COLUMN nom_fichier VARCHAR(255) NOT NULL DEFAULT ''",
                "ALTER TABLE documents ADD COLUMN chemin_fichier VARCHAR(255) DEFAULT ''",
                "ALTER TABLE documents ADD COLUMN type VARCHAR(255)",
                "ALTER TABLE documents ADD COLUMN taille VARCHAR(50)",
                "ALTER TABLE documents ADD COLUMN description TEXT",
                "ALTER TABLE documents ADD COLUMN date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP",
                "ALTER TABLE documents ADD COLUMN date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP",
                "ALTER TABLE documents ADD COLUMN nb_rapports INT DEFAULT 0",
                "ALTER TABLE documents ADD COLUMN nb_ordonnances INT DEFAULT 0",
                "ALTER TABLE documents ADD COLUMN medecin_id INT",
                "ALTER TABLE documents ADD COLUMN patient_id INT",
                "ALTER TABLE documents MODIFY COLUMN nom VARCHAR(255) DEFAULT NULL", // Fix pour l'ancienne colonne 'nom'
                "ALTER TABLE documents MODIFY COLUMN chemin VARCHAR(255) DEFAULT NULL" // Fix pour l'ancienne colonne 'chemin'
        };

        for (String sql : columns) {
            try (Statement st = getConnection().createStatement()) {
                st.execute(sql);
            } catch (SQLException e) {
                // Ignore si déjà présent
            }
        }
    }

    @Override
    public void add(Document document) throws SQLException {
        checkSchema();
        String requete = "INSERT INTO documents (nom_fichier, chemin_fichier, type, taille, description, date_creation, date_modification, nb_rapports, nb_ordonnances, medecin_id, patient_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = getConnection().prepareStatement(requete, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, document.getNomFichier());
            pst.setString(2, document.getCheminFichier() != null ? document.getCheminFichier() : "");
            pst.setString(3, document.getType());
            pst.setString(4, document.getTaille());
            pst.setString(5, document.getDescription());
            pst.setTimestamp(6, new Timestamp(document.getDateCreation().getTime()));
            pst.setTimestamp(7, new Timestamp(document.getDateModification().getTime()));
            pst.setInt(8, document.getNbRapports());
            pst.setInt(9, document.getNbOrdonnances());
            pst.setInt(10, document.getMedecinId());
            pst.setInt(11, document.getPatientId());
            pst.executeUpdate();

            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    document.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    @Override
    public void update(Document document) throws SQLException {
        String requete = "UPDATE documents SET nom_fichier = ?, type = ?, taille = ?, description = ?, date_modification = ?, nb_rapports = ?, nb_ordonnances = ?, medecin_id = ?, patient_id = ? WHERE id = ?";
        try (PreparedStatement pst = getConnection().prepareStatement(requete)) {
            pst.setString(1, document.getNomFichier());
            pst.setString(2, document.getType());
            pst.setString(3, document.getTaille());
            pst.setString(4, document.getDescription());
            pst.setTimestamp(5, new Timestamp(new java.util.Date().getTime()));
            pst.setInt(6, document.getNbRapports());
            pst.setInt(7, document.getNbOrdonnances());
            pst.setInt(8, document.getMedecinId());
            pst.setInt(9, document.getPatientId());
            pst.setInt(10, document.getId());
            pst.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String requete = "DELETE FROM documents WHERE id = ?";
        try (PreparedStatement pst = getConnection().prepareStatement(requete)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }

    @Override
    public Document getById(int id) throws SQLException {
        String requete = "SELECT * FROM documents WHERE id = ?";
        try (PreparedStatement pst = getConnection().prepareStatement(requete)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDocument(rs);
                }
            }
        }
        return null;
    }
    
    // Alias for findById
    public Document findById(int id) throws SQLException {
        return getById(id);
    }

    @Override
    public List<Document> getAll() throws SQLException {
        List<Document> list = new ArrayList<>();
        String requete = "SELECT * FROM documents ORDER BY date_creation DESC";
        try (Statement st = getConnection().createStatement();
             ResultSet rs = st.executeQuery(requete)) {
            while (rs.next()) {
                list.add(mapResultSetToDocument(rs));
            }
        }
        return list;
    }
    
    // Alias for findAll
    public List<Document> findAll() throws SQLException {
        return getAll();
    }

    private Document mapResultSetToDocument(ResultSet rs) throws SQLException {
        Document d = new Document();
        d.setId(rs.getInt("id"));
        d.setNomFichier(rs.getString("nom_fichier"));
        d.setCheminFichier(rs.getString("chemin_fichier"));
        d.setType(rs.getString("type"));
        d.setTaille(rs.getString("taille"));
        d.setDescription(rs.getString("description"));
        d.setDateCreation(rs.getTimestamp("date_creation"));
        d.setDateModification(rs.getTimestamp("date_modification"));
        d.setNbRapports(rs.getInt("nb_rapports"));
        d.setNbOrdonnances(rs.getInt("nb_ordonnances"));
        d.setMedecinId(rs.getInt("medecin_id"));
        d.setPatientId(rs.getInt("patient_id"));
        return d;
    }

    public Document findByPatientAndMedecin(int patientId, int medecinId) throws SQLException {
        String requete = "SELECT * FROM documents WHERE patient_id = ? AND medecin_id = ? OR (nom_fichier LIKE ?)";
        // Fallback or specific search
        try (PreparedStatement pst = getConnection().prepareStatement("SELECT * FROM documents WHERE id IN (SELECT document_id FROM rapport WHERE patient_id=? AND medecin_id=? UNION SELECT document_id FROM ordonnances WHERE patient_id=? AND medecin_id=?) LIMIT 1")) {
             pst.setInt(1, patientId);
             pst.setInt(2, medecinId);
             pst.setInt(3, patientId);
             pst.setInt(4, medecinId);
             try (ResultSet rs = pst.executeQuery()) {
                 if (rs.next()) return mapResultSetToDocument(rs);
             }
        }
        return null;
    }

    public List<Document> findByPatient(int patientId) throws SQLException {
        List<Document> list = new ArrayList<>();
        // Get documents directly by patient_id OR by linked rapports/ordonnances
        String requete = "SELECT DISTINCT d.* FROM documents d " +
                        "WHERE d.patient_id = ? " +
                        "OR d.id IN (SELECT DISTINCT document_id FROM rapport WHERE patient_id = ? AND document_id IS NOT NULL) " +
                        "OR d.id IN (SELECT DISTINCT document_id FROM ordonnances WHERE patient_id = ? AND document_id IS NOT NULL) " +
                        "ORDER BY d.date_creation DESC";
        try (PreparedStatement pst = getConnection().prepareStatement(requete)) {
            pst.setInt(1, patientId);
            pst.setInt(2, patientId);
            pst.setInt(3, patientId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDocument(rs));
                }
            }
        }
        return list;
    }

    public void create(Document document) throws SQLException {
        add(document);
    }

    /**
     * Vérifie si un document existe déjà pour ce patient et ce médecin
     * @param patientId ID du patient
     * @param medecinId ID du médecin
     * @return true si un document existe, false sinon
     */
    public boolean documentExists(int patientId, int medecinId) throws SQLException {
        String requete = "SELECT COUNT(*) as count FROM documents WHERE patient_id = ? AND medecin_id = ?";
        try (PreparedStatement pst = getConnection().prepareStatement(requete)) {
            pst.setInt(1, patientId);
            pst.setInt(2, medecinId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("count");
                    System.out.println("📄 Documents trouvés pour Patient " + patientId + " + Médecin " + medecinId + ": " + count);
                    return count > 0;
                }
            }
        }
        return false;
    }
}
