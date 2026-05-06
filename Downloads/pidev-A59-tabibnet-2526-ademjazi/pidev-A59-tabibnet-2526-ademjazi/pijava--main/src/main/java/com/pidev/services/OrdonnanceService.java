package com.pidev.services;

import com.pidev.models.Ordonnance;
import com.pidev.utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrdonnanceService implements IService<Ordonnance> {

    private Connection getConnection() {
        return DataSource.getInstance().getConnection();
    }

    private void checkSchema() {
        try (Statement st = getConnection().createStatement()) {
            st.execute("ALTER TABLE ordonnances ADD COLUMN document_id INT DEFAULT NULL");
        } catch (SQLException e) {
            // Ignore if exists
        }
    }

    @Override
    public void add(Ordonnance ordonnance) throws SQLException {
        checkSchema();
        String requete = "INSERT INTO ordonnances (date_ordonnance, diagnosis, medicament, posologie, notes, instructions, created_at, updated_at, patient_id, medecin_id, appointment_id, document_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = getConnection().prepareStatement(requete)) {
            pst.setTimestamp(1, ordonnance.getDateOrdonnance() != null ? Timestamp.valueOf(ordonnance.getDateOrdonnance()) : new Timestamp(System.currentTimeMillis()));
            pst.setString(2, ordonnance.getDiagnosis());
            pst.setString(3, ordonnance.getMedicament());
            pst.setString(4, ordonnance.getPosologie());
            pst.setString(5, ordonnance.getNotes());
            pst.setString(6, ordonnance.getInstructions());
            pst.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            pst.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
            pst.setInt(9, ordonnance.getPatientId());
            pst.setInt(10, ordonnance.getMedecinId());
            if (ordonnance.getAppointmentId() != null) pst.setInt(11, ordonnance.getAppointmentId()); else pst.setNull(11, Types.INTEGER);
            if (ordonnance.getDocumentId() != null) pst.setInt(12, ordonnance.getDocumentId()); else pst.setNull(12, Types.INTEGER);
            
            pst.executeUpdate();
        }
    }

    @Override
    public void update(Ordonnance ordonnance) throws SQLException {
        String requete = "UPDATE ordonnances SET date_ordonnance = ?, diagnosis = ?, medicament = ?, posologie = ?, notes = ?, instructions = ?, updated_at = ?, appointment_id = ?, document_id = ? WHERE id = ?";
        try (PreparedStatement pst = getConnection().prepareStatement(requete)) {
            pst.setTimestamp(1, ordonnance.getDateOrdonnance() != null ? Timestamp.valueOf(ordonnance.getDateOrdonnance()) : new Timestamp(System.currentTimeMillis()));
            pst.setString(2, ordonnance.getDiagnosis());
            pst.setString(3, ordonnance.getMedicament());
            pst.setString(4, ordonnance.getPosologie());
            pst.setString(5, ordonnance.getNotes());
            pst.setString(6, ordonnance.getInstructions());
            pst.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            if (ordonnance.getAppointmentId() != null) pst.setInt(8, ordonnance.getAppointmentId()); else pst.setNull(8, Types.INTEGER);
            if (ordonnance.getDocumentId() != null) pst.setInt(9, ordonnance.getDocumentId()); else pst.setNull(9, Types.INTEGER);
            pst.setInt(10, ordonnance.getId());
            pst.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String requete = "DELETE FROM ordonnances WHERE id = ?";
        try (PreparedStatement pst = getConnection().prepareStatement(requete)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }

    @Override
    public Ordonnance getById(int id) throws SQLException {
        String requete = "SELECT * FROM ordonnances WHERE id = ?";
        try (PreparedStatement pst = getConnection().prepareStatement(requete)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return mapResultSetToOrdonnance(rs);
            }
        }
        return null;
    }

    public Ordonnance findById(int id) throws SQLException { return getById(id); }

    @Override
    public List<Ordonnance> getAll() throws SQLException {
        List<Ordonnance> list = new ArrayList<>();
        String requete = "SELECT * FROM ordonnances ORDER BY created_at DESC";
        try (Statement st = getConnection().createStatement();
             ResultSet rs = st.executeQuery(requete)) {
            while (rs.next()) {
                list.add(mapResultSetToOrdonnance(rs));
            }
        }
        return list;
    }

    public List<Ordonnance> findAll() throws SQLException { return getAll(); }

    public List<Ordonnance> findByDocumentId(int documentId) throws SQLException {
        List<Ordonnance> list = new ArrayList<>();
        String requete = "SELECT * FROM ordonnances WHERE document_id = ?";
        try (PreparedStatement pst = getConnection().prepareStatement(requete)) {
            pst.setInt(1, documentId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToOrdonnance(rs));
            }
        }
        return list;
    }
    
    public List<Ordonnance> getByDocument(int documentId) throws SQLException { return findByDocumentId(documentId); }
    public List<Ordonnance> getByDocumentId(int documentId) throws SQLException { return findByDocumentId(documentId); }

    public void linkOrphanedItems(int documentId) throws SQLException {
        String requete = "UPDATE ordonnances SET document_id = ? WHERE document_id IS NULL";
        try (PreparedStatement pst = getConnection().prepareStatement(requete)) {
            pst.setInt(1, documentId);
            pst.executeUpdate();
        }
    }

    public List<Ordonnance> findOrphans() throws SQLException {
        List<Ordonnance> list = new ArrayList<>();
        String requete = "SELECT * FROM ordonnances WHERE document_id IS NULL";
        try (Statement st = getConnection().createStatement();
             ResultSet rs = st.executeQuery(requete)) {
            while (rs.next()) list.add(mapResultSetToOrdonnance(rs));
        }
        return list;
    }

    private Ordonnance mapResultSetToOrdonnance(ResultSet rs) throws SQLException {
        Ordonnance o = new Ordonnance();
        o.setId(rs.getInt("id"));
        Timestamp ts = rs.getTimestamp("date_ordonnance");
        if (ts != null) o.setDateOrdonnance(ts.toLocalDateTime());
        o.setDiagnosis(rs.getString("diagnosis"));
        o.setMedicament(rs.getString("medicament"));
        o.setPosologie(rs.getString("posologie"));
        o.setNotes(rs.getString("notes"));
        o.setInstructions(rs.getString("instructions"));
        o.setPatientId(rs.getInt("patient_id"));
        o.setMedecinId(rs.getInt("medecin_id"));
        int appId = rs.getInt("appointment_id");
        if (!rs.wasNull()) o.setAppointmentId(appId);
        int docId = rs.getInt("document_id");
        if (!rs.wasNull()) o.setDocumentId(docId);
        
        Timestamp cat = rs.getTimestamp("created_at");
        if (cat != null) o.setCreatedAt(cat.toLocalDateTime());
        Timestamp uat = rs.getTimestamp("updated_at");
        if (uat != null) o.setUpdatedAt(uat.toLocalDateTime());
        
        return o;
    }

    public List<Ordonnance> findByPatientAndMedecin(int patientId, int medecinId) throws SQLException {
        List<Ordonnance> list = new ArrayList<>();
        String requete = "SELECT * FROM ordonnances WHERE patient_id = ? AND medecin_id = ?";
        try (PreparedStatement pst = getConnection().prepareStatement(requete)) {
            pst.setInt(1, patientId);
            pst.setInt(2, medecinId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToOrdonnance(rs));
            }
        }
        return list;
    }

    public List<Ordonnance> getByPatientAndDoctor(int patientId, int doctorId) throws SQLException {
        return findByPatientAndMedecin(patientId, doctorId);
    }

    public List<Ordonnance> getByPatientAndMedecin(int patientId, int medecinId, Integer appointmentId) throws SQLException {
        if (appointmentId != null && appointmentId > 0) {
            return findByPatientMedecinAndAppointment(patientId, medecinId, appointmentId);
        } else {
            return findByPatientAndMedecin(patientId, medecinId);
        }
    }

    public List<Ordonnance> getByPatient(int patientId) throws SQLException {
        List<Ordonnance> list = new ArrayList<>();
        String requete = "SELECT * FROM ordonnances WHERE patient_id = ?";
        try (PreparedStatement pst = getConnection().prepareStatement(requete)) {
            pst.setInt(1, patientId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToOrdonnance(rs));
            }
        }
        return list;
    }

    private List<Ordonnance> findByPatientMedecinAndAppointment(int patientId, int medecinId, int appointmentId) throws SQLException {
        List<Ordonnance> list = new ArrayList<>();
        String requete = "SELECT * FROM ordonnances WHERE patient_id = ? AND medecin_id = ? AND appointment_id = ?";
        try (PreparedStatement pst = getConnection().prepareStatement(requete)) {
            pst.setInt(1, patientId);
            pst.setInt(2, medecinId);
            pst.setInt(3, appointmentId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToOrdonnance(rs));
            }
        }
        return list;
    }
}
