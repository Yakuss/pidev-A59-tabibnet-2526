package com.pidev.services;

import com.pidev.models.Question;
import com.pidev.utils.DataSource;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD service for Question entity using JDBC.
 * Joins with patients and specialite tables for display names.
 */
public class QuestionService implements IService<Question> {

    private final Connection conn = DataSource.getInstance().getConnection();

    @Override
    public void add(Question question) throws SQLException {
        String sql = "INSERT INTO question (titre, description, created_at, specialite_id, patient_id, image_name) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, question.getTitre());
        ps.setString(2, question.getDescription());
        ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
        ps.setInt(4, question.getSpecialiteId());
        ps.setInt(5, question.getPatientId());
        ps.setString(6, question.getImageName()); // Save image name
        ps.executeUpdate();

        ResultSet keys = ps.getGeneratedKeys();
        if (keys.next()) {
            question.setId(keys.getInt(1));
        }
        System.out.println("✅ Question ajoutée !");
    }

    @Override
    public void update(Question question) throws SQLException {
        String sql = "UPDATE question SET titre=?, description=?, specialite_id=? WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, question.getTitre());
        ps.setString(2, question.getDescription());
        ps.setInt(3, question.getSpecialiteId());
        ps.setInt(4, question.getId());
        ps.executeUpdate();
        System.out.println("✅ Question modifiée !");
    }

    @Override
    public void delete(int id) throws SQLException {
        // First delete all associated responses
        PreparedStatement psReponses = conn.prepareStatement("DELETE FROM reponse WHERE question_id=?");
        psReponses.setInt(1, id);
        psReponses.executeUpdate();

        PreparedStatement ps = conn.prepareStatement("DELETE FROM question WHERE id=?");
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("✅ Question supprimée !");
    }

    @Override
    public List<Question> getAll() throws SQLException {
        List<Question> list = new ArrayList<>();
        String sql = "SELECT q.*, " +
                     "COALESCE(CONCAT(p.first_name, ' ', p.last_name), 'Anonyme') AS patient_name, " +
                     "COALESCE(s.nom, 'Non classée') AS specialite_nom, " +
                     "(SELECT COUNT(*) FROM reponse r WHERE r.question_id = q.id) AS answer_count " +
                     "FROM question q " +
                     "LEFT JOIN patients p ON q.patient_id = p.id " +
                     "LEFT JOIN specialite s ON q.specialite_id = s.id " +
                     "ORDER BY q.created_at DESC";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        return list;
    }

    public List<Question> getBySpecialite(int specialiteId) throws SQLException {
        List<Question> list = new ArrayList<>();
        String sql = "SELECT q.*, " +
                     "COALESCE(CONCAT(p.first_name, ' ', p.last_name), 'Anonyme') AS patient_name, " +
                     "COALESCE(s.nom, 'Non classée') AS specialite_nom, " +
                     "(SELECT COUNT(*) FROM reponse r WHERE r.question_id = q.id) AS answer_count " +
                     "FROM question q " +
                     "LEFT JOIN patients p ON q.patient_id = p.id " +
                     "LEFT JOIN specialite s ON q.specialite_id = s.id " +
                     "WHERE q.specialite_id = ? " +
                     "ORDER BY q.created_at DESC";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, specialiteId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        return list;
    }

    /**
     * Get all questions by a specific patient (for gamification)
     * @param patientId Patient ID
     * @return List of questions by this patient
     * @throws SQLException if database error occurs
     */
    public List<Question> getByPatient(int patientId) throws SQLException {
        List<Question> list = new ArrayList<>();
        String sql = "SELECT q.*, " +
                     "COALESCE(CONCAT(p.first_name, ' ', p.last_name), 'Anonyme') AS patient_name, " +
                     "COALESCE(s.nom, 'Non classée') AS specialite_nom, " +
                     "(SELECT COUNT(*) FROM reponse r WHERE r.question_id = q.id) AS answer_count " +
                     "FROM question q " +
                     "LEFT JOIN patients p ON q.patient_id = p.id " +
                     "LEFT JOIN specialite s ON q.specialite_id = s.id " +
                     "WHERE q.patient_id = ? " +
                     "ORDER BY q.created_at DESC";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, patientId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        return list;
    }

    @Override
    public Question getById(int id) throws SQLException {
        String sql = "SELECT q.*, " +
                     "COALESCE(CONCAT(p.first_name, ' ', p.last_name), 'Anonyme') AS patient_name, " +
                     "COALESCE(s.nom, 'Non classée') AS specialite_nom, " +
                     "(SELECT COUNT(*) FROM reponse r WHERE r.question_id = q.id) AS answer_count " +
                     "FROM question q " +
                     "LEFT JOIN patients p ON q.patient_id = p.id " +
                     "LEFT JOIN specialite s ON q.specialite_id = s.id " +
                     "WHERE q.id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return mapResultSet(rs);
        }
        return null;
    }

    public int getTotalAnswers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM reponse";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        if (rs.next()) return rs.getInt(1);
        return 0;
    }

    private Question mapResultSet(ResultSet rs) throws SQLException {
        Question q = new Question();
        q.setId(rs.getInt("id"));
        q.setTitre(rs.getString("titre"));
        q.setDescription(rs.getString("description"));

        try {
            Timestamp createdTs = rs.getTimestamp("created_at");
            if (createdTs != null) q.setCreatedAt(createdTs.toLocalDateTime());
        } catch (SQLException ignored) {}

        try {
            Timestamp updatedTs = rs.getTimestamp("updated_at");
            if (updatedTs != null) q.setUpdatedAt(updatedTs.toLocalDateTime());
        } catch (SQLException ignored) {}

        try { q.setLikes(rs.getInt("likes")); } catch (SQLException ignored) {}
        try { q.setStatus(rs.getString("status")); } catch (SQLException ignored) {}
        try { q.setImageName(rs.getString("image_name")); } catch (SQLException ignored) {}
        q.setSpecialiteId(rs.getInt("specialite_id"));
        q.setPatientId(rs.getInt("patient_id"));
        q.setPatientName(rs.getString("patient_name"));
        q.setSpecialiteNom(rs.getString("specialite_nom"));

        try {
            q.setAnswerCount(rs.getInt("answer_count"));
        } catch (SQLException ignored) {
            q.setAnswerCount(0);
        }

        return q;
    }
}
