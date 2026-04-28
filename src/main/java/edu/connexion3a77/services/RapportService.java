package edu.connexion3a77.services;

import edu.connexion3a77.entities.Rapport;
import edu.connexion3a77.interfaces.IService;
import edu.connexion3a77.tools.MyConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class RapportService implements IService<Rapport> {

    private void checkSchema() {
        try (Statement st = MyConnection.getInstance().getCnx().createStatement()) {
            st.execute("ALTER TABLE rapport ADD COLUMN document_id INT DEFAULT NULL");
            System.out.println("Migration : document_id ajoutée à rapport.");
        } catch (SQLException e) {
            // Ignore if exists
        }
    }

    @Override
    public void add(Rapport rapport) throws SQLException {
        String requete = "INSERT INTO rapport (consultation_reason, diagnosis, observations, recommendations, treatments, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            pst.setString(1, rapport.getConsultationReason());
            pst.setString(2, rapport.getDiagnosis());
            pst.setString(3, rapport.getObservations());
            pst.setString(4, rapport.getRecommendations());
            pst.setString(5, rapport.getTreatments());
            pst.setTimestamp(6, new Timestamp(System.currentTimeMillis())); // createdAt
            pst.setTimestamp(7, new Timestamp(System.currentTimeMillis())); // updatedAt
            pst.executeUpdate();
            System.out.println("Rapport ajouté !");
        }
    }

    @Override
    public void update(Rapport rapport) throws SQLException {
        String requete = "UPDATE rapport SET consultation_reason = ?, diagnosis = ?, observations = ?, recommendations = ?, treatments = ?, updated_at = ? WHERE id = ?";
        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            pst.setString(1, rapport.getConsultationReason());
            pst.setString(2, rapport.getDiagnosis());
            pst.setString(3, rapport.getObservations());
            pst.setString(4, rapport.getRecommendations());
            pst.setString(5, rapport.getTreatments());
            pst.setTimestamp(6, new Timestamp(System.currentTimeMillis())); // updatedAt
            pst.setInt(7, rapport.getId());
            pst.executeUpdate();
            System.out.println("Rapport modifié !");
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String requete = "DELETE FROM rapport WHERE id = ?";
        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Rapport supprimé !");
        }
    }

    @Override
    public Rapport findById(int id) throws SQLException {
        String requete = "SELECT * FROM rapport WHERE id = ?";
        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRapport(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Rapport> findAll() throws SQLException {
        List<Rapport> list = new ArrayList<>();
        String requete = "SELECT * FROM rapport";
        try (Statement st = MyConnection.getInstance().getCnx().createStatement();
             ResultSet rs = st.executeQuery(requete)) {
            while (rs.next()) {
                list.add(mapResultSetToRapport(rs));
            }
        }
        return list;
    }

    public List<Rapport> findByDocumentId(int documentId) throws SQLException {
        checkSchema();
        List<Rapport> list = new ArrayList<>();
        String requete = "SELECT * FROM rapport WHERE document_id = ?";
        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            pst.setInt(1, documentId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRapport(rs));
                }
            }
        }
        System.out.println("SQL: Found " + list.size() + " rapports for document " + documentId);
        return list;
    }

    public void linkOrphanedItems(int documentId) throws SQLException {
        checkSchema();
        String requete = "UPDATE rapport SET document_id = ? WHERE document_id IS NULL";
        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            pst.setInt(1, documentId);
            int affected = pst.executeUpdate();
            System.out.println("SQL: Linked " + affected + " orphaned rapports to document " + documentId);
        }
    }

    public List<Rapport> findOrphans() throws SQLException {
        checkSchema();
        List<Rapport> list = new ArrayList<>();
        String requete = "SELECT * FROM rapport WHERE document_id IS NULL";
        try (Statement st = MyConnection.getInstance().getCnx().createStatement();
             ResultSet rs = st.executeQuery(requete)) {
            while (rs.next()) {
                list.add(mapResultSetToRapport(rs));
            }
        }
        return list;
    }

    private Rapport mapResultSetToRapport(ResultSet rs) throws SQLException {
        Rapport r = new Rapport();
        r.setId(rs.getInt("id"));
        r.setConsultationReason(rs.getString("consultation_reason"));
        r.setDiagnosis(rs.getString("diagnosis"));
        r.setObservations(rs.getString("observations"));
        r.setRecommendations(rs.getString("recommendations"));
        r.setTreatments(rs.getString("treatments"));
        r.setCreatedAt(rs.getTimestamp("created_at"));
        r.setUpdatedAt(rs.getTimestamp("updated_at"));
        return r;
    }
}
