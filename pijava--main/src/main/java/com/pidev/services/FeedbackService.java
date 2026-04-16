package com.pidev.services;

import com.pidev.models.Feedback;
import com.pidev.utils.DataSource;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD service for Feedback entity using JDBC.
 */
public class FeedbackService implements IService<Feedback> {

    private final Connection conn = DataSource.getInstance().getConnection();

    @Override
    public void add(Feedback f) throws SQLException {
        String sql = "INSERT INTO feedback (rating, comment, created_at, sentiment_score, " +
                     "patient_id, medecin_id, appointment_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, f.getRating());
        ps.setString(2, f.getComment());
        ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
        ps.setObject(4, f.getSentimentScore());
        ps.setInt(5, f.getPatientId());
        ps.setInt(6, f.getMedecinId());
        ps.setInt(7, f.getAppointmentId());
        ps.executeUpdate();
        System.out.println("✅ Feedback added!");
    }

    @Override
    public void update(Feedback f) throws SQLException {
        String sql = "UPDATE feedback SET rating=?, comment=?, sentiment_score=?, " +
                     "patient_id=?, medecin_id=?, appointment_id=? WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, f.getRating());
        ps.setString(2, f.getComment());
        ps.setObject(3, f.getSentimentScore());
        ps.setInt(4, f.getPatientId());
        ps.setInt(5, f.getMedecinId());
        ps.setInt(6, f.getAppointmentId());
        ps.setInt(7, f.getId());
        ps.executeUpdate();
        System.out.println("✅ Feedback updated!");
    }

    @Override
    public void delete(int id) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("DELETE FROM feedback WHERE id=?");
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("✅ Feedback deleted!");
    }

    @Override
    public List<Feedback> getAll() throws SQLException {
        List<Feedback> list = new ArrayList<>();
        String sql = "SELECT f.*, " +
                     "CONCAT(p.first_name, ' ', p.last_name) AS patient_name, " +
                     "CONCAT(m.first_name, ' ', m.last_name) AS medecin_name " +
                     "FROM feedback f " +
                     "LEFT JOIN patients p ON f.patient_id = p.id " +
                     "LEFT JOIN medecins m ON f.medecin_id = m.id " +
                     "ORDER BY f.id DESC";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        return list;
    }

    @Override
    public Feedback getById(int id) throws SQLException {
        String sql = "SELECT f.*, " +
                     "CONCAT(p.first_name, ' ', p.last_name) AS patient_name, " +
                     "CONCAT(m.first_name, ' ', m.last_name) AS medecin_name " +
                     "FROM feedback f " +
                     "LEFT JOIN patients p ON f.patient_id = p.id " +
                     "LEFT JOIN medecins m ON f.medecin_id = m.id " +
                     "WHERE f.id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return mapResultSet(rs);
        }
        return null;
    }

    private Feedback mapResultSet(ResultSet rs) throws SQLException {
        Feedback f = new Feedback();
        f.setId(rs.getInt("id"));
        f.setRating(rs.getInt("rating"));
        f.setComment(rs.getString("comment"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) f.setCreatedAt(createdAt.toLocalDateTime());
        f.setSentimentScore(rs.getObject("sentiment_score") != null ?
                rs.getDouble("sentiment_score") : null);
        f.setPatientId(rs.getInt("patient_id"));
        f.setMedecinId(rs.getInt("medecin_id"));
        f.setAppointmentId(rs.getInt("appointment_id"));
        try {
            f.setPatientName(rs.getString("patient_name"));
            f.setMedecinName(rs.getString("medecin_name"));
        } catch (SQLException ignored) {}
        return f;
    }
}
