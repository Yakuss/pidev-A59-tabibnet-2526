package com.pidev.services;

import com.pidev.models.RendezVous;
import com.pidev.utils.DataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

/**
 * Service for managing Rendezvous (Appointments)
 */
public class RendezVousService {

    public RendezVousService() {
        // Connection is obtained dynamically to avoid connection pool issues
    }

    /**
     * Add a new appointment
     */
    public void ajouter(RendezVous r) {
        String query = "INSERT INTO rendezvous (patientId, medecinId, date, heure, statut) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pst = DataSource.getInstance().getConnection().prepareStatement(query)) {
            pst.setInt(1, r.getPatientId());
            pst.setInt(2, r.getMedecinId());
            pst.setString(3, r.getDate());
            pst.setString(4, r.getHeure());
            pst.setString(5, r.getStatut());
            pst.executeUpdate();
            System.out.println("✅ Appointment added successfully");
        } catch (SQLException e) {
            System.out.println("❌ Error adding appointment: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update an existing appointment (new signature - used by PatientAppointmentsController)
     */
    public void modifier(RendezVous ancien, String date, String heure, String statut) {
        String query = "UPDATE rendezvous SET date = ?, heure = ?, statut = ? WHERE id = ?";
        try (PreparedStatement pst = DataSource.getInstance().getConnection().prepareStatement(query)) {
            pst.setString(1, date);
            pst.setString(2, heure);
            pst.setString(3, statut);
            pst.setInt(4, ancien.getId());
            pst.executeUpdate();
            System.out.println("✅ Appointment updated successfully");
        } catch (SQLException e) {
            System.out.println("❌ Error updating appointment: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update an existing appointment (old signature - used by RendezVousController and ApiServer)
     * Accepts patient name and doctor name for backward compatibility
     */
    public void modifier(RendezVous ancien, String nomPatient, String medecin, String date, String heure, String statut) {
        // Update the RendezVous object with legacy fields
        ancien.setNomPatient(nomPatient);
        ancien.setMedecin(medecin);
        ancien.setDate(date);
        ancien.setHeure(heure);
        ancien.setStatut(statut);
        
        // Update in database
        String query = "UPDATE rendezvous SET date = ?, heure = ?, statut = ? WHERE id = ?";
        try (PreparedStatement pst = DataSource.getInstance().getConnection().prepareStatement(query)) {
            pst.setString(1, date);
            pst.setString(2, heure);
            pst.setString(3, statut);
            pst.setInt(4, ancien.getId());
            pst.executeUpdate();
            System.out.println("✅ Appointment updated successfully");
        } catch (SQLException e) {
            System.out.println("❌ Error updating appointment: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Delete an appointment
     */
    public void supprimer(RendezVous r) {
        String query = "DELETE FROM rendezvous WHERE id = ?";
        try (PreparedStatement pst = DataSource.getInstance().getConnection().prepareStatement(query)) {
            pst.setInt(1, r.getId());
            pst.executeUpdate();
            System.out.println("✅ Appointment deleted successfully");
        } catch (SQLException e) {
            System.out.println("❌ Error deleting appointment: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get all appointments
     */
    public ObservableList<RendezVous> getAll() {
        ObservableList<RendezVous> list = FXCollections.observableArrayList();
        String query = "SELECT * FROM rendezvous ORDER BY date DESC, heure DESC";
        try (Statement st = DataSource.getInstance().getConnection().createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                RendezVous rdv = new RendezVous();
                rdv.setId(rs.getInt("id"));
                rdv.setPatientId(rs.getInt("patientId"));
                rdv.setMedecinId(rs.getInt("medecinId"));
                rdv.setDate(rs.getString("date"));
                rdv.setHeure(rs.getString("heure"));
                rdv.setStatut(rs.getString("statut"));
                list.add(rdv);
            }
        } catch (SQLException e) {
            System.out.println("❌ Error fetching appointments: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Get appointment by ID
     */
    public RendezVous findById(int id) {
        String query = "SELECT * FROM rendezvous WHERE id = ?";
        try (PreparedStatement pst = DataSource.getInstance().getConnection().prepareStatement(query)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    RendezVous rdv = new RendezVous();
                    rdv.setId(rs.getInt("id"));
                    rdv.setPatientId(rs.getInt("patientId"));
                    rdv.setMedecinId(rs.getInt("medecinId"));
                    rdv.setDate(rs.getString("date"));
                    rdv.setHeure(rs.getString("heure"));
                    rdv.setStatut(rs.getString("statut"));
                    return rdv;
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error finding appointment: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get all appointments for a specific patient
     */
    public ObservableList<RendezVous> getByPatientId(int patientId) {
        ObservableList<RendezVous> list = FXCollections.observableArrayList();
        String query = "SELECT * FROM rendezvous WHERE patientId = ? ORDER BY date DESC, heure DESC";
        try (PreparedStatement pst = DataSource.getInstance().getConnection().prepareStatement(query)) {
            pst.setInt(1, patientId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    RendezVous rdv = new RendezVous();
                    rdv.setId(rs.getInt("id"));
                    rdv.setPatientId(rs.getInt("patientId"));
                    rdv.setMedecinId(rs.getInt("medecinId"));
                    rdv.setDate(rs.getString("date"));
                    rdv.setHeure(rs.getString("heure"));
                    rdv.setStatut(rs.getString("statut"));
                    list.add(rdv);
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error fetching patient appointments: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Get all appointments for a specific doctor
     */
    public ObservableList<RendezVous> getByMedecinId(int medecinId) {
        ObservableList<RendezVous> list = FXCollections.observableArrayList();
        String query = "SELECT * FROM rendezvous WHERE medecinId = ? ORDER BY date DESC, heure DESC";
        try (PreparedStatement pst = DataSource.getInstance().getConnection().prepareStatement(query)) {
            pst.setInt(1, medecinId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    RendezVous rdv = new RendezVous();
                    rdv.setId(rs.getInt("id"));
                    rdv.setPatientId(rs.getInt("patientId"));
                    rdv.setMedecinId(rs.getInt("medecinId"));
                    rdv.setDate(rs.getString("date"));
                    rdv.setHeure(rs.getString("heure"));
                    rdv.setStatut(rs.getString("statut"));
                    list.add(rdv);
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error fetching doctor appointments: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }
}