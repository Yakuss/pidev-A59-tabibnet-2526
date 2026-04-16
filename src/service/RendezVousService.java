package service;

import entity.RendezVous;
import utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class RendezVousService {

    public RendezVousService() {
        // La connexion est désormais demandée dynamiquement pour éviter les déséquilibres (timeouts)
    }

    public void ajouter(RendezVous r) {
        String query = "INSERT INTO rendezvous (nomPatient, medecin, date, heure, statut) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pst = DatabaseConnection.getConnection().prepareStatement(query)) {
            pst.setString(1, r.getNomPatient());
            pst.setString(2, r.getMedecin());
            pst.setString(3, r.getDate());
            pst.setString(4, r.getHeure());
            pst.setString(5, r.getStatut());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur ajout RDV: " + e.getMessage());
        }
    }

    public void modifier(RendezVous ancien, String nom, String medecin, String date, String heure, String statut) {
        String query = "UPDATE rendezvous SET nomPatient = ?, medecin = ?, date = ?, heure = ?, statut = ? WHERE id = ?";
        try (PreparedStatement pst = DatabaseConnection.getConnection().prepareStatement(query)) {
            pst.setString(1, nom);
            pst.setString(2, medecin);
            pst.setString(3, date);
            pst.setString(4, heure);
            pst.setString(5, statut);
            pst.setInt(6, ancien.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur modif RDV: " + e.getMessage());
        }
    }

    // Compatibilite avec ancien code
    public void modifier(RendezVous ancien, String nom, String date, String heure) {
        modifier(ancien, nom, ancien.getMedecin(), date, heure, ancien.getStatut());
    }

    public void supprimer(RendezVous r) {
        String query = "DELETE FROM rendezvous WHERE id = ?";
        try (PreparedStatement pst = DatabaseConnection.getConnection().prepareStatement(query)) {
            pst.setInt(1, r.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur supp RDV: " + e.getMessage());
        }
    }

    public ObservableList<RendezVous> getAll() {
        ObservableList<RendezVous> list = FXCollections.observableArrayList();
        String query = "SELECT * FROM rendezvous";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                list.add(new RendezVous(
                        rs.getInt("id"),
                        rs.getString("nomPatient"),
                        rs.getString("medecin"),
                        rs.getString("date"),
                        rs.getString("heure"),
                        rs.getString("statut")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Erreur getAll RDV: " + e.getMessage());
        }
        return list;
    }

    public RendezVous findById(int id) {
        String query = "SELECT * FROM rendezvous WHERE id = ?";
        try (PreparedStatement pst = DatabaseConnection.getConnection().prepareStatement(query)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new RendezVous(
                            rs.getInt("id"),
                            rs.getString("nomPatient"),
                            rs.getString("medecin"),
                            rs.getString("date"),
                            rs.getString("heure"),
                            rs.getString("statut")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur findById: " + e.getMessage());
        }
        return null;
    }
}