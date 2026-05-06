package com.pidev.services;

import com.pidev.models.Feedback;
import com.pidev.utils.DataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

/**
 * Service for managing Feedback
 */
public class FeedbackService {

    public FeedbackService() {
        // Connection is obtained dynamically
    }

    /**
     * Add new feedback and update doctor's AI score
     */
    public void ajouter(Feedback f) {
        String query = "INSERT INTO feedback (rendezVousId, commentaire, note) VALUES (?, ?, ?)";
        try (PreparedStatement pst = DataSource.getInstance().getConnection().prepareStatement(query)) {
            pst.setInt(1, f.getRendezVousId());
            pst.setString(2, f.getCommentaire());
            pst.setInt(3, f.getNote());
            pst.executeUpdate();
            System.out.println("✅ Feedback added successfully");
            
            // Automatically update doctor's AI score
            int medecinId = getMedecinIdFromRendezVous(f.getRendezVousId());
            if (medecinId > 0) {
                // Run in background thread to avoid blocking UI
                new Thread(() -> {
                    double aiScore = calculateAndUpdateDoctorAIScore(medecinId);
                    if (aiScore >= 0) {
                        System.out.println("✅ Doctor AI score recalculated: " + aiScore);
                    }
                }).start();
            }
        } catch (SQLException e) {
            System.out.println("❌ Error adding feedback: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update feedback
     */
    public void modifier(Feedback f, String commentaire, int note, int rdvId) {
        String query = "UPDATE feedback SET commentaire = ?, note = ?, rendezVousId = ? WHERE id = ?";
        try (PreparedStatement pst = DataSource.getInstance().getConnection().prepareStatement(query)) {
            pst.setString(1, commentaire);
            pst.setInt(2, note);
            pst.setInt(3, rdvId);
            pst.setInt(4, f.getId());
            pst.executeUpdate();
            System.out.println("✅ Feedback updated successfully");
        } catch (SQLException e) {
            System.out.println("❌ Error updating feedback: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Delete feedback
     */
    public void supprimer(Feedback f) {
        String query = "DELETE FROM feedback WHERE id = ?";
        try (PreparedStatement pst = DataSource.getInstance().getConnection().prepareStatement(query)) {
            pst.setInt(1, f.getId());
            pst.executeUpdate();
            System.out.println("✅ Feedback deleted successfully");
        } catch (SQLException e) {
            System.out.println("❌ Error deleting feedback: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get all feedback
     */
    public ObservableList<Feedback> getAll() {
        ObservableList<Feedback> list = FXCollections.observableArrayList();
        String query = "SELECT * FROM feedback ORDER BY dateCreation DESC";
        try (Statement st = DataSource.getInstance().getConnection().createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                Feedback f = new Feedback();
                f.setId(rs.getInt("id"));
                f.setCommentaire(rs.getString("commentaire"));
                f.setNote(rs.getInt("note"));
                f.setRendezVousId(rs.getInt("rendezVousId"));
                list.add(f);
            }
        } catch (SQLException e) {
            System.out.println("❌ Error fetching feedback: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Get feedback for a specific appointment
     */
    public ObservableList<Feedback> getByRendezVous(int rdvId) {
        ObservableList<Feedback> list = FXCollections.observableArrayList();
        String query = "SELECT * FROM feedback WHERE rendezVousId = ? ORDER BY dateCreation DESC";
        try (PreparedStatement pst = DataSource.getInstance().getConnection().prepareStatement(query)) {
            pst.setInt(1, rdvId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Feedback f = new Feedback();
                    f.setId(rs.getInt("id"));
                    f.setCommentaire(rs.getString("commentaire"));
                    f.setNote(rs.getInt("note"));
                    f.setRendezVousId(rs.getInt("rendezVousId"));
                    list.add(f);
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error fetching feedback for appointment: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Get feedback by ID
     */
    public Feedback findById(int id) {
        String query = "SELECT * FROM feedback WHERE id = ?";
        try (PreparedStatement pst = DataSource.getInstance().getConnection().prepareStatement(query)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    Feedback f = new Feedback();
                    f.setId(rs.getInt("id"));
                    f.setCommentaire(rs.getString("commentaire"));
                    f.setNote(rs.getInt("note"));
                    f.setRendezVousId(rs.getInt("rendezVousId"));
                    return f;
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error finding feedback: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get all feedbacks for a specific doctor (medecin)
     * @param medecinId The doctor's ID
     * @return List of feedbacks for this doctor
     */
    public ObservableList<Feedback> getByMedecin(int medecinId) {
        ObservableList<Feedback> list = FXCollections.observableArrayList();
        String query = "SELECT f.* FROM feedback f " +
                      "INNER JOIN rendezvous r ON f.rendezVousId = r.id " +
                      "WHERE r.medecinId = ? " +
                      "ORDER BY f.dateCreation DESC";
        try (PreparedStatement pst = DataSource.getInstance().getConnection().prepareStatement(query)) {
            pst.setInt(1, medecinId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Feedback f = new Feedback();
                    f.setId(rs.getInt("id"));
                    f.setCommentaire(rs.getString("commentaire"));
                    f.setNote(rs.getInt("note"));
                    f.setRendezVousId(rs.getInt("rendezVousId"));
                    list.add(f);
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error fetching feedback for doctor: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Calculate and update AI sentiment score for a doctor
     * @param medecinId The doctor's ID
     * @return The calculated AI score, or -1 if error
     */
    public double calculateAndUpdateDoctorAIScore(int medecinId) {
        try {
            // Get all feedbacks for this doctor
            ObservableList<Feedback> feedbacks = getByMedecin(medecinId);
            
            if (feedbacks.isEmpty()) {
                System.out.println("ℹ️ No feedbacks found for doctor ID: " + medecinId);
                // Update to 0.0 if no feedbacks
                updateDoctorAIScore(medecinId, 0.0);
                return 0.0;
            }

            // Prepare feedback data for sentiment analysis
            SentimentAnalysisService sentimentService = new SentimentAnalysisService();
            
            // Check if API is healthy
            if (!sentimentService.isApiHealthy()) {
                System.err.println("❌ Sentiment API is not available");
                return -1;
            }

            // Convert feedbacks to FeedbackData list
            java.util.List<SentimentAnalysisService.FeedbackData> feedbackDataList = new java.util.ArrayList<>();
            for (Feedback fb : feedbacks) {
                feedbackDataList.add(new SentimentAnalysisService.FeedbackData(
                    fb.getCommentaire(),
                    fb.getNote()
                ));
            }

            // Calculate AI sentiment score
            double aiScore = sentimentService.calculateDoctorSentimentScore(feedbackDataList);
            
            // Update doctor's AI score in database
            updateDoctorAIScore(medecinId, aiScore);
            
            System.out.println("✅ AI score updated for doctor ID " + medecinId + ": " + aiScore);
            return aiScore;

        } catch (Exception e) {
            System.err.println("❌ Error calculating AI score for doctor: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Update doctor's AI average score in database
     * @param medecinId The doctor's ID
     * @param aiScore The AI sentiment score (0-5)
     */
    private void updateDoctorAIScore(int medecinId, double aiScore) {
        String query = "UPDATE medecins SET ai_average_score = ? WHERE id = ?";
        try (PreparedStatement pst = DataSource.getInstance().getConnection().prepareStatement(query)) {
            pst.setDouble(1, aiScore);
            pst.setInt(2, medecinId);
            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Doctor AI score updated in database");
            } else {
                System.out.println("⚠️ No doctor found with ID: " + medecinId);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error updating doctor AI score: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get doctor ID from appointment ID
     * @param rendezVousId The appointment ID
     * @return The doctor's ID, or -1 if not found
     */
    public int getMedecinIdFromRendezVous(int rendezVousId) {
        String query = "SELECT medecinId FROM rendezvous WHERE id = ?";
        try (PreparedStatement pst = DataSource.getInstance().getConnection().prepareStatement(query)) {
            pst.setInt(1, rendezVousId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("medecinId");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting doctor ID from appointment: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }
}