package edu.connexion3a77.services;

import edu.connexion3a77.entities.Article;
import edu.connexion3a77.entities.Magazine;
import edu.connexion3a77.tools.MyConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceArticle {

    private final Connection cnx;

    public ServiceArticle() {
        this.cnx = MyConnection.getInstance().getCnx();
    }

    // ====================== CREATE ======================
    public void ajouter(Article article) throws SQLException {
        String sql = """
            INSERT INTO article (titre, resume, auteur, datePub, summary, statut, image, views, id_magazine)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, article.getTitre());
            ps.setString(2, article.getResume());
            ps.setString(3, article.getAuteur());
            ps.setTimestamp(4, article.getDatePub() != null ? Timestamp.valueOf(article.getDatePub()) : null);
            ps.setString(5, article.getSummary());
            ps.setString(6, article.getStatut());
            ps.setString(7, article.getImage());
            ps.setInt(8, article.getViews());

            // Gestion de la relation avec Magazine
            if (article.getMagazine() != null) {
                ps.setInt(9, article.getMagazine().getId());
            } else {
                ps.setNull(9, Types.INTEGER);
            }

            ps.executeUpdate();

            // Récupérer l'ID généré automatiquement
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    article.setId(rs.getInt(1));
                }
            }
        }
    }

    // ====================== READ ALL ======================
    public List<Article> afficherTout() throws SQLException {
        String sql = """
            SELECT a.id, a.titre, a.resume, a.auteur, a.datePub, a.summary, 
                   a.statut, a.image, a.views, a.id_magazine, m.titre as magazine_titre
            FROM article a 
            LEFT JOIN magazine m ON a.id_magazine = m.id
            """;

        List<Article> articles = new ArrayList<>();

        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Article article = new Article(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("resume"),
                        rs.getString("auteur"),
                        rs.getTimestamp("datePub") != null ? rs.getTimestamp("datePub").toLocalDateTime() : null,
                        rs.getString("summary"),
                        rs.getString("statut"),
                        rs.getString("image"),
                        rs.getInt("views")
                );

                // Gestion de la relation Magazine
                int magId = rs.getInt("id_magazine");
                if (!rs.wasNull()) {
                    Magazine magazine = new Magazine(
                            magId,
                            rs.getString("magazine_titre"),
                            "", "", null, "", null
                    );
                    article.setMagazine(magazine);
                }

                articles.add(article);
            }
        }
        return articles;
    }

    // ====================== UPDATE ======================
    public void modifier(Article article) throws SQLException {
        String sql = """
            UPDATE article 
            SET titre = ?, resume = ?, auteur = ?, datePub = ?, summary = ?, 
                statut = ?, image = ?, views = ?, id_magazine = ? 
            WHERE id = ?
            """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setString(1, article.getTitre());
            ps.setString(2, article.getResume());
            ps.setString(3, article.getAuteur());
            ps.setTimestamp(4, article.getDatePub() != null ? Timestamp.valueOf(article.getDatePub()) : null);
            ps.setString(5, article.getSummary());
            ps.setString(6, article.getStatut());
            ps.setString(7, article.getImage());
            ps.setInt(8, article.getViews());

            if (article.getMagazine() != null) {
                ps.setInt(9, article.getMagazine().getId());
            } else {
                ps.setNull(9, Types.INTEGER);
            }

            ps.setInt(10, article.getId());
            ps.executeUpdate();
        }
    }

    // ====================== DELETE ======================
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM article WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ====================== BONUS ======================
    public void incrementerVue(int id) throws SQLException {
        String sql = "UPDATE article SET views = views + 1 WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}