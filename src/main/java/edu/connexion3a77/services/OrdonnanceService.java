package edu.connexion3a77.services;

import edu.connexion3a77.entities.Ordonnance;
import edu.connexion3a77.interfaces.IService;
import edu.connexion3a77.tools.MyConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class OrdonnanceService implements IService<Ordonnance> {

    private void checkSchema() {
        try (Statement st = MyConnection.getInstance().getCnx().createStatement()) {
            st.execute("ALTER TABLE ordonnances ADD COLUMN document_id INT DEFAULT NULL");
            System.out.println("Migration : document_id ajoutée à ordonnances.");
        } catch (SQLException e) {
            // Ignore if exists
        }
    }

    @Override
    public void add(Ordonnance ordonnance) throws SQLException {
        String requete = "INSERT INTO ordonnances (date_ordonnance, diagnosis, medicament, posologie, notes, instructions, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            pst.setTimestamp(1, ordonnance.getDateOrdonnance() != null ? new Timestamp(ordonnance.getDateOrdonnance().getTime()) : null);
            pst.setString(2, ordonnance.getDiagnosis());
            pst.setString(3, ordonnance.getMedicament());
            pst.setString(4, ordonnance.getPosologie());
            pst.setString(5, ordonnance.getNotes());
            pst.setString(6, ordonnance.getInstructions());
            pst.setTimestamp(7, new Timestamp(System.currentTimeMillis())); // createdAt
            pst.setTimestamp(8, new Timestamp(System.currentTimeMillis())); // updatedAt
            pst.executeUpdate();
            System.out.println("Ordonnance ajoutée !");
        }
    }

    @Override
    public void update(Ordonnance ordonnance) throws SQLException {
        String requete = "UPDATE ordonnances SET date_ordonnance = ?, diagnosis = ?, medicament = ?, posologie = ?, notes = ?, instructions = ?, updated_at = ? WHERE id = ?";
        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            pst.setTimestamp(1, ordonnance.getDateOrdonnance() != null ? new Timestamp(ordonnance.getDateOrdonnance().getTime()) : null);
            pst.setString(2, ordonnance.getDiagnosis());
            pst.setString(3, ordonnance.getMedicament());
            pst.setString(4, ordonnance.getPosologie());
            pst.setString(5, ordonnance.getNotes());
            pst.setString(6, ordonnance.getInstructions());
            pst.setTimestamp(7, new Timestamp(System.currentTimeMillis())); // updatedAt
            pst.setInt(8, ordonnance.getId());
            pst.executeUpdate();
            System.out.println("Ordonnance modifiée !");
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String requete = "DELETE FROM ordonnances WHERE id = ?";
        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Ordonnance supprimée !");
        }
    }

    @Override
    public Ordonnance findById(int id) throws SQLException {
        String requete = "SELECT * FROM ordonnances WHERE id = ?";
        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrdonnance(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Ordonnance> findAll() throws SQLException {
        List<Ordonnance> list = new ArrayList<>();
        String requete = "SELECT * FROM ordonnances";
        try (Statement st = MyConnection.getInstance().getCnx().createStatement();
             ResultSet rs = st.executeQuery(requete)) {
            while (rs.next()) {
                list.add(mapResultSetToOrdonnance(rs));
            }
        }
        return list;
    }

    public List<Ordonnance> findByDocumentId(int documentId) throws SQLException {
        checkSchema();
        List<Ordonnance> list = new ArrayList<>();
        String requete = "SELECT * FROM ordonnances WHERE document_id = ?";
        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            pst.setInt(1, documentId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToOrdonnance(rs));
                }
            }
        }
        System.out.println("SQL: Found " + list.size() + " ordonnances for document " + documentId);
        return list;
    }

    public void linkOrphanedItems(int documentId) throws SQLException {
        checkSchema();
        String requete = "UPDATE ordonnances SET document_id = ? WHERE document_id IS NULL";
        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            pst.setInt(1, documentId);
            int affected = pst.executeUpdate();
            System.out.println("SQL: Linked " + affected + " orphaned ordonnances to document " + documentId);
        }
    }

    public List<Ordonnance> findOrphans() throws SQLException {
        checkSchema();
        List<Ordonnance> list = new ArrayList<>();
        String requete = "SELECT * FROM ordonnances WHERE document_id IS NULL";
        try (Statement st = MyConnection.getInstance().getCnx().createStatement();
             ResultSet rs = st.executeQuery(requete)) {
            while (rs.next()) {
                list.add(mapResultSetToOrdonnance(rs));
            }
        }
        return list;
    }

    private Ordonnance mapResultSetToOrdonnance(ResultSet rs) throws SQLException {
        Ordonnance o = new Ordonnance();
        o.setId(rs.getInt("id"));
        o.setDateOrdonnance(rs.getTimestamp("date_ordonnance"));
        o.setDiagnosis(rs.getString("diagnosis"));
        o.setMedicament(rs.getString("medicament"));
        o.setPosologie(rs.getString("posologie"));
        o.setNotes(rs.getString("notes"));
        o.setInstructions(rs.getString("instructions"));
        o.setCreatedAt(rs.getTimestamp("created_at"));
        o.setUpdatedAt(rs.getTimestamp("updated_at"));
        return o;
    }
}
