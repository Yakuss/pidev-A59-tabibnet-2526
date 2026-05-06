package com.pidev.services;

import com.pidev.models.Rapport;
import com.pidev.utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RapportService implements IService<Rapport> {

    private Connection getConnection() {
        return DataSource.getInstance().getConnection();
    }

    private void checkSchema() {
        try (Statement st = getConnection().createStatement()) {
            st.execute("ALTER TABLE rapport ADD COLUMN document_id INT DEFAULT NULL");
        } catch (SQLException e) {
            // Ignore if exists
        }
    }

    @Override
    public void add(Rapport rapport) throws SQLException {
        checkSchema();
        String requete = "INSERT INTO rapport (consultation_reason, diagnosis, observations, recommendations, treatments, created_at, updated_at, patient_id, medecin_id, appointment_id, document_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = getConnection().prepareStatement(requete)) {
            pst.setString(1, rapport.getConsultationReason());
            pst.setString(2, rapport.getDiagnosis());
            pst.setString(3, rapport.getObservations());
            pst.setString(4, rapport.getRecommendations());
            pst.setString(5, rapport.getTreatments());
            pst.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            pst.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            pst.setInt(8, rapport.getPatientId());
            pst.setInt(9, rapport.getMedecinId());
            if (rapport.getAppointmentId() != null) pst.setInt(10, rapport.getAppointmentId()); else pst.setNull(10, Types.INTEGER);
            if (rapport.getDocumentId() != null) pst.setInt(11, rapport.getDocumentId()); else pst.setNull(11, Types.INTEGER);
            pst.executeUpdate();
        }
    }

    @Override
    public void update(Rapport rapport) throws SQLException {
        String requete = "UPDATE rapport SET consultation_reason = ?, diagnosis = ?, observations = ?, recommendations = ?, treatments = ?, updated_at = ?, appointment_id = ?, document_id = ? WHERE id = ?";
        try (PreparedStatement pst = getConnection().prepareStatement(requete)) {
            pst.setString(1, rapport.getConsultationReason());
            pst.setString(2, rapport.getDiagnosis());
            pst.setString(3, rapport.getObservations());
            pst.setString(4, rapport.getRecommendations());
            pst.setString(5, rapport.getTreatments());
            pst.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            if (rapport.getAppointmentId() != null) pst.setInt(7, rapport.getAppointmentId()); else pst.setNull(7, Types.INTEGER);
            if (rapport.getDocumentId() != null) pst.setInt(8, rapport.getDocumentId()); else pst.setNull(8, Types.INTEGER);
            pst.setInt(9, rapport.getId());
            pst.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String requete = "DELETE FROM rapport WHERE id = ?";
        try (PreparedStatement pst = getConnection().prepareStatement(requete)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }

    @Override
    public Rapport getById(int id) throws SQLException {
        String requete = "SELECT * FROM rapport WHERE id = ?";
        try (PreparedStatement pst = getConnection().prepareStatement(requete)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return mapResultSetToRapport(rs);
            }
        }
        return null;
    }

    public Rapport findById(int id) throws SQLException { return getById(id); }

    @Override
    public List<Rapport> getAll() throws SQLException {
        List<Rapport> list = new ArrayList<>();
        String requete = "SELECT * FROM rapport ORDER BY created_at DESC";
        try (Statement st = getConnection().createStatement();
             ResultSet rs = st.executeQuery(requete)) {
            while (rs.next()) {
                list.add(mapResultSetToRapport(rs));
            }
        }
        return list;
    }

    public List<Rapport> findAll() throws SQLException { return getAll(); }

    public List<Rapport> findByDocumentId(int documentId) throws SQLException {
        List<Rapport> list = new ArrayList<>();
        String requete = "SELECT * FROM rapport WHERE document_id = ?";
        try (PreparedStatement pst = getConnection().prepareStatement(requete)) {
            pst.setInt(1, documentId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToRapport(rs));
            }
        }
        return list;
    }
    
    public List<Rapport> getByDocument(int documentId) throws SQLException { return findByDocumentId(documentId); }
    public List<Rapport> getByDocumentId(int documentId) throws SQLException { return findByDocumentId(documentId); }

    public void linkOrphanedItems(int documentId) throws SQLException {
        String requete = "UPDATE rapport SET document_id = ? WHERE document_id IS NULL";
        try (PreparedStatement pst = getConnection().prepareStatement(requete)) {
            pst.setInt(1, documentId);
            pst.executeUpdate();
        }
    }

    public List<Rapport> findOrphans() throws SQLException {
        List<Rapport> list = new ArrayList<>();
        String requete = "SELECT * FROM rapport WHERE document_id IS NULL";
        try (Statement st = getConnection().createStatement();
             ResultSet rs = st.executeQuery(requete)) {
            while (rs.next()) list.add(mapResultSetToRapport(rs));
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
        r.setPatientId(rs.getInt("patient_id"));
        r.setMedecinId(rs.getInt("medecin_id"));
        int appId = rs.getInt("appointment_id");
        if (!rs.wasNull()) r.setAppointmentId(appId);
        int docId = rs.getInt("document_id");
        if (!rs.wasNull()) r.setDocumentId(docId);
        
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) r.setCreatedAt(ts.toLocalDateTime());
        Timestamp uat = rs.getTimestamp("updated_at");
        if (uat != null) r.setUpdatedAt(uat.toLocalDateTime());
        
        return r;
    }

    public List<Rapport> findByPatientAndMedecin(int patientId, int medecinId) throws SQLException {
        List<Rapport> list = new ArrayList<>();
        String requete = "SELECT * FROM rapport WHERE patient_id = ? AND medecin_id = ?";
        try (PreparedStatement pst = getConnection().prepareStatement(requete)) {
            pst.setInt(1, patientId);
            pst.setInt(2, medecinId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToRapport(rs));
            }
        }
        return list;
    }

    public List<Rapport> getByPatientAndDoctor(int patientId, int doctorId) throws SQLException {
        return findByPatientAndMedecin(patientId, doctorId);
    }

    public List<Rapport> getByPatientAndMedecin(int patientId, int medecinId, Integer appointmentId) throws SQLException {
        if (appointmentId != null && appointmentId > 0) {
            return findByPatientMedecinAndAppointment(patientId, medecinId, appointmentId);
        } else {
            return findByPatientAndMedecin(patientId, medecinId);
        }
    }

    public List<Rapport> getByPatient(int patientId) throws SQLException {
        List<Rapport> list = new ArrayList<>();
        String requete = "SELECT * FROM rapport WHERE patient_id = ?";
        try (PreparedStatement pst = getConnection().prepareStatement(requete)) {
            pst.setInt(1, patientId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToRapport(rs));
            }
        }
        return list;
    }

    private List<Rapport> findByPatientMedecinAndAppointment(int patientId, int medecinId, int appointmentId) throws SQLException {
        List<Rapport> list = new ArrayList<>();
        String requete = "SELECT * FROM rapport WHERE patient_id = ? AND medecin_id = ? AND appointment_id = ?";
        try (PreparedStatement pst = getConnection().prepareStatement(requete)) {
            pst.setInt(1, patientId);
            pst.setInt(2, medecinId);
            pst.setInt(3, appointmentId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToRapport(rs));
            }
        }
        return list;
    }
}
