package edu.connexion3a77.services;

import edu.connexion3a77.entities.Document;
import edu.connexion3a77.interfaces.IService;
import edu.connexion3a77.tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DocumentService implements IService<Document> {

    private void checkSchema() {
        String[] columns = {
            "ALTER TABLE document ADD COLUMN type VARCHAR(255)",
            "ALTER TABLE document ADD COLUMN taille VARCHAR(50)",
            "ALTER TABLE document ADD COLUMN description TEXT",
            "ALTER TABLE document ADD COLUMN date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP",
            "ALTER TABLE document ADD COLUMN date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP",
            "ALTER TABLE document ADD COLUMN nb_rapports INT DEFAULT 0",
            "ALTER TABLE document ADD COLUMN nb_ordonnances INT DEFAULT 0"
        };
        
        for (String sql : columns) {
            try (Statement st = MyConnection.getInstance().getCnx().createStatement()) {
                st.execute(sql);
                System.out.println("Migration : " + sql + " effectuée.");
            } catch (SQLException e) {
                // La colonne existe probablement déjà, on ignore
            }
        }
    }

    @Override
    public void add(Document document) throws SQLException {
        checkSchema(); // Vérifie et répare la table avant l'insertion
        String requete = "INSERT INTO document (nom_fichier, chemin_fichier, type, taille, description, date_creation, date_modification, nb_rapports, nb_ordonnances) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, document.getNomFichier());
            pst.setString(2, document.getCheminFichier() != null ? document.getCheminFichier() : "");
            pst.setString(3, document.getType());
            pst.setString(4, document.getTaille());
            pst.setString(5, document.getDescription());
            pst.setTimestamp(6, new Timestamp(document.getDateCreation().getTime()));
            pst.setTimestamp(7, new Timestamp(document.getDateModification().getTime()));
            pst.setInt(8, document.getNbRapports());
            pst.setInt(9, document.getNbOrdonnances());
            pst.executeUpdate();
            
            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    document.setId(generatedKeys.getInt(1));
                }
            }
            System.out.println("Document ajouté ! ID=" + document.getId());
        }
    }

    @Override
    public void update(Document document) throws SQLException {
        String requete = "UPDATE document SET nom_fichier = ?, type = ?, taille = ?, description = ?, date_modification = ?, nb_rapports = ?, nb_ordonnances = ? WHERE id = ?";
        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            pst.setString(1, document.getNomFichier());
            pst.setString(2, document.getType());
            pst.setString(3, document.getTaille());
            pst.setString(4, document.getDescription());
            pst.setTimestamp(5, new Timestamp(new java.util.Date().getTime()));
            pst.setInt(6, document.getNbRapports());
            pst.setInt(7, document.getNbOrdonnances());
            pst.setInt(8, document.getId());
            pst.executeUpdate();
            System.out.println("Document modifié !");
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String requete = "DELETE FROM document WHERE id = ?";
        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Document supprimé !");
        }
    }

    @Override
    public Document findById(int id) throws SQLException {
        String requete = "SELECT * FROM document WHERE id = ?";
        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDocument(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Document> findAll() throws SQLException {
        List<Document> list = new ArrayList<>();
        String requete = "SELECT * FROM document ORDER BY date_creation DESC";
        try (Statement st = MyConnection.getInstance().getCnx().createStatement();
             ResultSet rs = st.executeQuery(requete)) {
            while (rs.next()) {
                list.add(mapResultSetToDocument(rs));
            }
        }
        return list;
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
        return d;
    }
}
