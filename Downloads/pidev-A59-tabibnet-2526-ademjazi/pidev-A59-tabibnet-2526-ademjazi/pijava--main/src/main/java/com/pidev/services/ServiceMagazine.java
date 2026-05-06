package com.pidev.services;

import com.pidev.models.Magazine;
import com.pidev.utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD service for Magazine entity.
 * Colonnes BDD : id, title, description, image, date_create, statut, pdf_file
 */
public class ServiceMagazine {

    private final Connection cnx = DataSource.getInstance().getConnection();

    // ====================== CREATE ======================
    public void ajouter(Magazine magazine) throws SQLException {
        String sql = "INSERT INTO magazine (title, description, image, date_create, statut, pdf_file) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, magazine.getTitre());
            ps.setString(2, magazine.getDescription());
            ps.setString(3, magazine.getImage());
            ps.setTimestamp(4, magazine.getDateCreate() != null
                    ? Timestamp.valueOf(magazine.getDateCreate())
                    : Timestamp.valueOf(java.time.LocalDateTime.now()));
            ps.setString(5, magazine.getStatut());
            ps.setString(6, magazine.getPdfFile());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) magazine.setId(rs.getInt(1));
            }
        }
    }

    // ====================== READ ALL ======================
    public List<Magazine> afficherTout() throws SQLException {
        List<Magazine> list = new ArrayList<>();
        String sql = "SELECT id, title, description, image, date_create, statut, pdf_file FROM magazine ORDER BY date_create DESC";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ====================== READ BY ID ======================
    public Magazine getById(int id) throws SQLException {
        String sql = "SELECT id, title, description, image, date_create, statut, pdf_file FROM magazine WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // ====================== UPDATE ======================
    public void modifier(Magazine magazine) throws SQLException {
        String sql = "UPDATE magazine SET title=?, description=?, image=?, statut=?, pdf_file=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, magazine.getTitre());
            ps.setString(2, magazine.getDescription());
            ps.setString(3, magazine.getImage());
            ps.setString(4, magazine.getStatut());
            ps.setString(5, magazine.getPdfFile());
            ps.setInt(6, magazine.getId());
            ps.executeUpdate();
        }
    }

    // ====================== DELETE ======================
    public void supprimer(int id) throws SQLException {
        try (PreparedStatement ps = cnx.prepareStatement("DELETE FROM magazine WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ====================== MAPPING ======================
    private Magazine mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("date_create");
        return new Magazine(
                rs.getInt("id"),
                rs.getString("title"),       // colonne BDD : title
                rs.getString("description"),
                rs.getString("image"),
                ts != null ? ts.toLocalDateTime() : null,
                rs.getString("statut"),
                rs.getString("pdf_file")
        );
    }
}
