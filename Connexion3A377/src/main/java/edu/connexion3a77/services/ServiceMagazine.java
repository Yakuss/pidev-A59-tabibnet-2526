package edu.connexion3a77.services;

import edu.connexion3a77.entities.Magazine;
import edu.connexion3a77.tools.MyConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceMagazine {

    private final Connection cnx;

    public ServiceMagazine() {
        this.cnx = MyConnection.getInstance().getCnx();
    }

    public void ajouter(Magazine magazine) throws SQLException {
        String sql = "INSERT INTO magazine (titre, description, image, statut, pdfFile) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, magazine.getTitre());
            ps.setString(2, magazine.getDescription());
            ps.setString(3, magazine.getImage());
            ps.setString(4, magazine.getStatut());
            ps.setString(5, magazine.getPdfFile());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) magazine.setId(rs.getInt(1));
            }
        }
    }

    public List<Magazine> afficherTout() throws SQLException {
        String sql = "SELECT * FROM magazine";
        List<Magazine> magazines = new ArrayList<>();

        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Magazine m = new Magazine(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getString("image"),
                        rs.getTimestamp("dateCreate").toLocalDateTime(),
                        rs.getString("statut"),
                        rs.getString("pdfFile")
                );
                magazines.add(m);
            }
        }
        return magazines;
    }

    public void modifier(Magazine magazine) throws SQLException {
        String sql = "UPDATE magazine SET titre=?, description=?, image=?, statut=?, pdfFile=? WHERE id=?";
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

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM magazine WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}