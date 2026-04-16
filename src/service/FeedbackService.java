package service;

import entity.Feedback;
import utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class FeedbackService {

    public FeedbackService() {
        // Obtenir la connexion dynamiquement
    }

    public void ajouter(Feedback f) {
        String query = "INSERT INTO feedback (commentaire, note, rendezVousId) VALUES (?, ?, ?)";
        try (PreparedStatement pst = DatabaseConnection.getConnection().prepareStatement(query)) {
            pst.setString(1, f.getCommentaire());
            pst.setInt(2, f.getNote());
            pst.setInt(3, f.getRendezVousId());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur ajout Feedback: " + e.getMessage());
        }
    }

    public void modifier(Feedback f, String commentaire, int note, int rdvId) {
        String query = "UPDATE feedback SET commentaire = ?, note = ?, rendezVousId = ? WHERE id = ?";
        try (PreparedStatement pst = DatabaseConnection.getConnection().prepareStatement(query)) {
            pst.setString(1, commentaire);
            pst.setInt(2, note);
            pst.setInt(3, rdvId);
            pst.setInt(4, f.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur modif Feedback: " + e.getMessage());
        }
    }

    public void supprimer(Feedback f) {
        String query = "DELETE FROM feedback WHERE id = ?";
        try (PreparedStatement pst = DatabaseConnection.getConnection().prepareStatement(query)) {
            pst.setInt(1, f.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur supp Feedback: " + e.getMessage());
        }
    }

    public ObservableList<Feedback> getAll() {
        ObservableList<Feedback> list = FXCollections.observableArrayList();
        String query = "SELECT * FROM feedback";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                list.add(new Feedback(
                        rs.getInt("id"),
                        rs.getString("commentaire"),
                        rs.getInt("note"),
                        rs.getInt("rendezVousId")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Erreur getAll Feedback: " + e.getMessage());
        }
        return list;
    }

    public ObservableList<Feedback> getByRendezVous(int rdvId) {
        ObservableList<Feedback> list = FXCollections.observableArrayList();
        String query = "SELECT * FROM feedback WHERE rendezVousId = ?";
        try (PreparedStatement pst = DatabaseConnection.getConnection().prepareStatement(query)) {
            pst.setInt(1, rdvId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(new Feedback(
                            rs.getInt("id"),
                            rs.getString("commentaire"),
                            rs.getInt("note"),
                            rs.getInt("rendezVousId")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur getByRDV: " + e.getMessage());
        }
        return list;
    }
}