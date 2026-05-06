package com.pidev.services;

import com.pidev.models.Article;
import com.pidev.models.Magazine;
import com.pidev.utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD service for Article entity.
 * Colonnes BDD : id, title, resume, auteur, date_pub, statut, image, magazine_id
 *              + summary, views, public_cible, pdf_file (ajoutés via ALTER TABLE)
 */
public class ServiceArticle {

    private Connection getConnection() {
        return DataSource.getInstance().getConnection();
    }
    public static final int PAGE_SIZE = 10;

    // ====================== CREATE ======================
    public void ajouter(Article article) throws SQLException {
        String sql = "INSERT INTO article (title, resume, auteur, date_pub, summary, statut, image, views, magazine_id, public_cible, pdf_file) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, article.getTitre());
            ps.setString(2, article.getResume());
            ps.setString(3, article.getAuteur());
            ps.setTimestamp(4, article.getDatePub() != null ? Timestamp.valueOf(article.getDatePub()) : null);
            ps.setString(5, article.getSummary());
            ps.setString(6, article.getStatut());
            ps.setString(7, article.getImage());
            ps.setInt(8, article.getViews());
            if (article.getMagazine() != null) ps.setInt(9, article.getMagazine().getId());
            else ps.setNull(9, Types.INTEGER);
            ps.setString(10, article.getPublicCible());
            ps.setString(11, article.getPdfFile());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) article.setId(rs.getInt(1));
            }
        }
    }

    // ====================== READ ALL ======================
    public List<Article> afficherTout() throws SQLException {
        return rechercherAvecFiltres(null, null, "recent", 1, null);
    }

    public List<Article> rechercherAvecFiltres(String recherche, String publicCible,
                                                String tri, int page) throws SQLException {
        return rechercherAvecFiltres(recherche, publicCible, tri, page, null);
    }

    public int compterAvecFiltres(String recherche, String publicCible) throws SQLException {
        return compterAvecFiltres(recherche, publicCible, null);
    }

    public List<Article> rechercherAvecFiltres(String recherche, String publicCible,
                                                String tri, int page, Integer magazineId) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT a.id, a.title, a.resume, a.auteur, a.date_pub, a.summary, " +
            "a.statut, a.image, a.views, a.magazine_id, a.public_cible, a.pdf_file, " +
            "m.title AS magazine_title " +
            "FROM article a LEFT JOIN magazine m ON a.magazine_id = m.id WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (recherche != null && !recherche.isBlank()) {
            sql.append(" AND (LOWER(a.title) LIKE ? OR LOWER(a.resume) LIKE ?)");
            String like = "%" + recherche.toLowerCase() + "%";
            params.add(like); params.add(like);
        }
        if (publicCible != null && !publicCible.isBlank()) {
            sql.append(" AND a.public_cible = ?");
            params.add(publicCible);
        }
        if (magazineId != null) {
            sql.append(" AND a.magazine_id = ?");
            params.add(magazineId);
        }

        sql.append("populaire".equals(tri) ? " ORDER BY a.views DESC" : " ORDER BY a.date_pub DESC");
        int offset = (page - 1) * PAGE_SIZE;
        sql.append(" LIMIT ? OFFSET ?");
        params.add(PAGE_SIZE); params.add(offset);

        List<Article> articles = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) articles.add(mapRow(rs));
            }
        }
        return articles;
    }

    public int compterAvecFiltres(String recherche, String publicCible, Integer magazineId) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM article a WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (recherche != null && !recherche.isBlank()) {
            sql.append(" AND (LOWER(a.title) LIKE ? OR LOWER(a.resume) LIKE ?)");
            String like = "%" + recherche.toLowerCase() + "%";
            params.add(like); params.add(like);
        }
        if (publicCible != null && !publicCible.isBlank()) {
            sql.append(" AND a.public_cible = ?");
            params.add(publicCible);
        }
        if (magazineId != null) {
            sql.append(" AND a.magazine_id = ?");
            params.add(magazineId);
        }

        try (PreparedStatement ps = getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // ====================== UPDATE ======================
    public void modifier(Article article) throws SQLException {
        String sql = "UPDATE article SET title=?, resume=?, auteur=?, date_pub=?, summary=?, " +
                     "statut=?, image=?, views=?, magazine_id=?, public_cible=?, pdf_file=? WHERE id=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, article.getTitre());
            ps.setString(2, article.getResume());
            ps.setString(3, article.getAuteur());
            ps.setTimestamp(4, article.getDatePub() != null ? Timestamp.valueOf(article.getDatePub()) : null);
            ps.setString(5, article.getSummary());
            ps.setString(6, article.getStatut());
            ps.setString(7, article.getImage());
            ps.setInt(8, article.getViews());
            if (article.getMagazine() != null) ps.setInt(9, article.getMagazine().getId());
            else ps.setNull(9, Types.INTEGER);
            ps.setString(10, article.getPublicCible());
            ps.setString(11, article.getPdfFile());
            ps.setInt(12, article.getId());
            ps.executeUpdate();
        }
    }

    // ====================== DELETE ======================
    public void supprimer(int id) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement("DELETE FROM article WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ====================== VUES ======================
    public void incrementerVue(int id) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement("UPDATE article SET views = views + 1 WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ====================== MAPPING ======================
    private Article mapRow(ResultSet rs) throws SQLException {
        Article article = new Article(
                rs.getInt("id"),
                rs.getString("title"),       // colonne BDD : title
                rs.getString("resume"),
                rs.getString("auteur"),
                rs.getTimestamp("date_pub") != null ? rs.getTimestamp("date_pub").toLocalDateTime() : null,
                rs.getString("summary"),
                rs.getString("statut"),
                rs.getString("image"),
                rs.getInt("views"),
                rs.getString("public_cible"),
                rs.getString("pdf_file")
        );
        int magId = rs.getInt("magazine_id");  // colonne BDD : magazine_id
        if (!rs.wasNull()) {
            Magazine mag = new Magazine(magId, rs.getString("magazine_title"), "", "", null, "", null);
            article.setMagazine(mag);
        }
        return article;
    }
}
