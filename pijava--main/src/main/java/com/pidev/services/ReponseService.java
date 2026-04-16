package com.pidev.services;

import com.pidev.models.Reponse;
import com.pidev.utils.DataSource;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD service for Reponse entity using JDBC.
 */
public class ReponseService implements IService<Reponse> {

    private final Connection conn = DataSource.getInstance().getConnection();

    @Override
    public void add(Reponse reponse) throws SQLException {
        ensureColumnExists();
        String sql = "INSERT INTO reponse (contenu, created_at, question_id, medecin_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, reponse.getContenu());
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(3, reponse.getQuestionId());
            ps.setInt(4, reponse.getMedecinId());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                reponse.setId(keys.getInt(1));
            }
        }
    }

    private void ensureColumnExists() {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getColumns(null, null, "reponse", "medecin_id");
            if (!rs.next()) {
                System.out.println("⚠️ Column 'medecin_id' missing in 'reponse' table. Adding it now...");
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate("ALTER TABLE reponse ADD COLUMN medecin_id INT NULL");
                    System.out.println("✅ Column 'medecin_id' added successfully!");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Failed to verify/add column: " + e.getMessage());
        }
    }

    @Override
    public void update(Reponse reponse) throws SQLException {
        String sql = "UPDATE reponse SET contenu=? WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, reponse.getContenu());
        ps.setInt(2, reponse.getId());
        ps.executeUpdate();
        System.out.println("✅ Réponse modifiée !");
    }

    @Override
    public void delete(int id) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("DELETE FROM reponse WHERE id=?");
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("✅ Réponse supprimée !");
    }

    @Override
    public List<Reponse> getAll() throws SQLException {
        List<Reponse> list = new ArrayList<>();
        String sql = "SELECT r.*, COALESCE(CONCAT(m.first_name, ' ', m.last_name), 'Anonyme') AS medecin_name " +
                     "FROM reponse r " +
                     "LEFT JOIN medecins m ON r.medecin_id = m.id " +
                     "ORDER BY r.created_at DESC";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        return list;
    }

    public List<Reponse> getByQuestion(int questionId) throws SQLException {
        List<Reponse> list = new ArrayList<>();
        String sql = "SELECT r.*, COALESCE(CONCAT(m.first_name, ' ', m.last_name), 'Anonyme') AS medecin_name " +
                     "FROM reponse r " +
                     "LEFT JOIN medecins m ON r.medecin_id = m.id " +
                     "WHERE r.question_id = ? " +
                     "ORDER BY r.created_at ASC";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, questionId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        return list;
    }

    public int getCountByQuestion(int questionId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reponse WHERE question_id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, questionId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getInt(1);
        return 0;
    }

    @Override
    public Reponse getById(int id) throws SQLException {
        String sql = "SELECT r.*, COALESCE(CONCAT(m.first_name, ' ', m.last_name), 'Anonyme') AS medecin_name " +
                     "FROM reponse r " +
                     "LEFT JOIN medecins m ON r.medecin_id = m.id " +
                     "WHERE r.id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return mapResultSet(rs);
        }
        return null;
    }

    private Reponse mapResultSet(ResultSet rs) throws SQLException {
        Reponse r = new Reponse();
        r.setId(rs.getInt("id"));
        r.setContenu(rs.getString("contenu"));

        try {
            Timestamp ts = rs.getTimestamp("created_at");
            if (ts != null) r.setCreatedAt(ts.toLocalDateTime());
        } catch (SQLException ignored) {}

        try { r.setLikes(rs.getInt("likes")); } catch (SQLException ignored) {}
        r.setQuestionId(rs.getInt("question_id"));
        try { r.setMedecinId(rs.getInt("medecin_id")); } catch (SQLException ignored) {}
        r.setMedecinName(rs.getString("medecin_name"));
        return r;
    }
}
