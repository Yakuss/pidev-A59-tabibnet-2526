package edu.connexion3a77.services;

import edu.connexion3a77.entities.Article;
import edu.connexion3a77.entities.Magazine;
import edu.connexion3a77.tools.MyConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceArticle {

    private final Connection cnx;
    public static final int PAGE_SIZE = 10;

    public ServiceArticle() {
        this.cnx = MyConnection.getInstance().getCnx();
    }

    // ====================== CREATE ======================
    public void ajouter(Article article) throws SQLException {
        String sql = "INSERT INTO article (titre, resume, auteur, datePub, summary, statut, image, views, id_magazine, publicCible, pdfFile) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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

    // ====================== READ ALL (rétrocompatible) ======================
    public List<Article> afficherTout() throws SQLException {
        return rechercherAvecFiltres(null, null, "recent", 1, null);
    }

    /** Surcharge sans filtre magazine (appelée depuis articleAdmin) */
    public List<Article> rechercherAvecFiltres(String recherche, String publicCible,
                                                String tri, int page) throws SQLException {
        return rechercherAvecFiltres(recherche, publicCible, tri, page, null);
    }

    /** Surcharge sans filtre magazine pour compter */
    public int compterAvecFiltres(String recherche, String publicCible) throws SQLException {
        return compterAvecFiltres(recherche, publicCible, null);
    }

    /**
     * Recherche paginée avec filtres.
     * @param recherche    texte libre sur titre/resume, null = pas de filtre
     * @param publicCible  "Enfants" | "Jeunes" | "Adultes" | null = tous
     * @param tri          "recent" | "populaire"
     * @param page         numéro de page (1-based)
     * @param magazineId   filtre par magazine, null = tous
     */
    public List<Article> rechercherAvecFiltres(String recherche, String publicCible,
                                                String tri, int page, Integer magazineId) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT a.id, a.titre, a.resume, a.auteur, a.datePub, a.summary, " +
            "a.statut, a.image, a.views, a.id_magazine, a.publicCible, a.pdfFile, " +
            "m.titre as magazine_titre " +
            "FROM article a LEFT JOIN magazine m ON a.id_magazine = m.id WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (recherche != null && !recherche.isBlank()) {
            sql.append(" AND (LOWER(a.titre) LIKE ? OR LOWER(a.resume) LIKE ?)");
            String like = "%" + recherche.toLowerCase() + "%";
            params.add(like);
            params.add(like);
        }
        if (publicCible != null && !publicCible.isBlank()) {
            sql.append(" AND a.publicCible = ?");
            params.add(publicCible);
        }
        if (magazineId != null) {
            sql.append(" AND a.id_magazine = ?");
            params.add(magazineId);
        }

        sql.append("populaire".equals(tri) ? " ORDER BY a.views DESC" : " ORDER BY a.datePub DESC");

        int offset = (page - 1) * PAGE_SIZE;
        sql.append(" LIMIT ? OFFSET ?");
        params.add(PAGE_SIZE);
        params.add(offset);

        List<Article> articles = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) articles.add(mapRow(rs));
            }
        }
        return articles;
    }

    /** Compte total pour la pagination */
    public int compterAvecFiltres(String recherche, String publicCible, Integer magazineId) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM article a WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (recherche != null && !recherche.isBlank()) {
            sql.append(" AND (LOWER(a.titre) LIKE ? OR LOWER(a.resume) LIKE ?)");
            String like = "%" + recherche.toLowerCase() + "%";
            params.add(like);
            params.add(like);
        }
        if (publicCible != null && !publicCible.isBlank()) {
            sql.append(" AND a.publicCible = ?");
            params.add(publicCible);
        }
        if (magazineId != null) {
            sql.append(" AND a.id_magazine = ?");
            params.add(magazineId);
        }

        try (PreparedStatement ps = cnx.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // ====================== UPDATE ======================
    public void modifier(Article article) throws SQLException {
        String sql = "UPDATE article SET titre=?, resume=?, auteur=?, datePub=?, summary=?, " +
                     "statut=?, image=?, views=?, id_magazine=?, publicCible=?, pdfFile=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
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
        String sql = "DELETE FROM article WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ====================== VUES ======================
    public void incrementerVue(int id) throws SQLException {
        String sql = "UPDATE article SET views = views + 1 WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ====================== MAPPING ======================
    private Article mapRow(ResultSet rs) throws SQLException {
        Article article = new Article(
                rs.getInt("id"),
                rs.getString("titre"),
                rs.getString("resume"),
                rs.getString("auteur"),
                rs.getTimestamp("datePub") != null ? rs.getTimestamp("datePub").toLocalDateTime() : null,
                rs.getString("summary"),
                rs.getString("statut"),
                rs.getString("image"),
                rs.getInt("views"),
                rs.getString("publicCible"),
                rs.getString("pdfFile")
        );
        int magId = rs.getInt("id_magazine");
        if (!rs.wasNull()) {
            Magazine magazine = new Magazine(magId, rs.getString("magazine_titre"), "", "", null, "", null);
            article.setMagazine(magazine);
        }
        return article;
    }
}
