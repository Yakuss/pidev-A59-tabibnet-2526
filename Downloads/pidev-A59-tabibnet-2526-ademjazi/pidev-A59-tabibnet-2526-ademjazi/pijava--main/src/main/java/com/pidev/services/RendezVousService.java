package com.pidev.services;

import com.pidev.models.RendezVous;
import com.pidev.utils.DataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing Rendezvous (Appointments)
 * Updated to support extended fields for the Calendar module.
 */
public class RendezVousService {

    private Connection getConnection() {
        return DataSource.getInstance().getConnection();
    }

    /**
     * Add a new appointment
     */
    public void ajouter(RendezVous r) throws SQLException {
        String query = "INSERT INTO rendezvous (patientId, medecinId, date, heure, statut, duration_minutes, department, message) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, r.getPatientId());
            pst.setInt(2, r.getMedecinId());
            pst.setString(3, r.getDate());
            pst.setString(4, r.getHeure());
            pst.setString(5, r.getStatut() != null ? r.getStatut() : "En attente");
            pst.setInt(6, r.getDurationMinutes());
            pst.setString(7, r.getDepartment());
            pst.setString(8, r.getMessage());
            pst.executeUpdate();
            
            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    r.setId(rs.getInt(1));
                }
            }
            System.out.println("✅ Appointment added successfully");
        }
    }

    /**
     * Update an existing appointment
     */
    public void modifier(RendezVous r) throws SQLException {
        String query = "UPDATE rendezvous SET patientId=?, medecinId=?, date=?, heure=?, statut=?, " +
                       "duration_minutes=?, department=?, message=? WHERE id=?";
        try (PreparedStatement pst = getConnection().prepareStatement(query)) {
            pst.setInt(1, r.getPatientId());
            pst.setInt(2, r.getMedecinId());
            pst.setString(3, r.getDate());
            pst.setString(4, r.getHeure());
            pst.setString(5, r.getStatut());
            pst.setInt(6, r.getDurationMinutes());
            pst.setString(7, r.getDepartment());
            pst.setString(8, r.getMessage());
            pst.setInt(9, r.getId());
            pst.executeUpdate();
            System.out.println("✅ Appointment updated successfully");
        }
    }

    /**
     * Legacy update method for backward compatibility
     */
    public void modifier(RendezVous ancien, String date, String heure, String statut) {
        ancien.setDate(date);
        ancien.setHeure(heure);
        ancien.setStatut(statut);
        try {
            modifier(ancien);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete an appointment
     */
    public void supprimer(RendezVous r) throws SQLException {
        String query = "DELETE FROM rendezvous WHERE id = ?";
        try (PreparedStatement pst = getConnection().prepareStatement(query)) {
            pst.setInt(1, r.getId());
            pst.executeUpdate();
            System.out.println("✅ Appointment deleted successfully");
        }
    }

    /**
     * Get all appointments
     */
    public ObservableList<RendezVous> getAll() {
        ObservableList<RendezVous> list = FXCollections.observableArrayList();
        String query = "SELECT * FROM rendezvous ORDER BY id DESC";
        try (Statement st = getConnection().createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Standard CRUD aliases for transition
     */
    public void add(RendezVous r) throws SQLException {
        ajouter(r);
    }

    public void update(RendezVous r) throws SQLException {
        modifier(r);
    }

    public void delete(int id) throws SQLException {
        String query = "DELETE FROM rendezvous WHERE id = ?";
        try (PreparedStatement pst = getConnection().prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("✅ Appointment deleted successfully");
        }
    }

    /**
     * Get appointment by ID
     */
    public RendezVous findById(int id) {
        String query = "SELECT * FROM rendezvous WHERE id = ?";
        try (PreparedStatement pst = getConnection().prepareStatement(query)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get all appointments for a specific patient
     */
    public ObservableList<RendezVous> getByPatientId(int patientId) {
        ObservableList<RendezVous> list = FXCollections.observableArrayList();
        String query = "SELECT * FROM rendezvous WHERE patientId = ? ORDER BY id DESC";
        try (PreparedStatement pst = getConnection().prepareStatement(query)) {
            pst.setInt(1, patientId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ObservableList<RendezVous> getByPatient(int patientId) {
        return getByPatientId(patientId);
    }

    /**
     * Get all appointments for a specific doctor
     */
    public ObservableList<RendezVous> getByMedecinId(int medecinId) {
        ObservableList<RendezVous> list = FXCollections.observableArrayList();
        String query = "SELECT * FROM rendezvous WHERE medecinId = ? ORDER BY id DESC";
        try (PreparedStatement pst = getConnection().prepareStatement(query)) {
            pst.setInt(1, medecinId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Compatibility methods for transition from Appointment model
     */
    public List<RendezVous> getAppointmentsByPatientAndDoctor(int patientId, int medecinId) {
        List<RendezVous> list = new ArrayList<>();
        String query = "SELECT * FROM rendezvous WHERE patientId = ? AND medecinId = ? ORDER BY date DESC, heure DESC";
        try (PreparedStatement pst = getConnection().prepareStatement(query)) {
            pst.setInt(1, patientId);
            pst.setInt(2, medecinId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<RendezVous> getAppointmentsByDoctor(int medecinId) {
        return getByMedecinId(medecinId);
    }

    private RendezVous mapResultSet(ResultSet rs) throws SQLException {
        RendezVous rdv = new RendezVous();
        rdv.setId(rs.getInt("id"));
        rdv.setPatientId(rs.getInt("patientId"));
        rdv.setMedecinId(rs.getInt("medecinId"));
        rdv.setDate(rs.getString("date"));
        rdv.setHeure(rs.getString("heure"));
        rdv.setStatut(rs.getString("statut"));
        rdv.setDurationMinutes(rs.getInt("duration_minutes"));
        rdv.setDepartment(rs.getString("department"));
        rdv.setMessage(rs.getString("message"));
        return rdv;
    }
}