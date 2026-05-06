package com.pidev.services;

import com.pidev.models.Specialite;
import com.pidev.utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD service for Specialite entity using JDBC.
 */
public class SpecialiteService implements IService<Specialite> {

    private Connection getConnection() {
        return DataSource.getInstance().getConnection();
    }

    @Override
    public void add(Specialite specialite) throws SQLException {
        String sql = "INSERT INTO specialite (nom, description) VALUES (?, ?)";
        PreparedStatement ps = getConnection().prepareStatement(sql);
        ps.setString(1, specialite.getNom());
        ps.setString(2, specialite.getDescription());
        ps.executeUpdate();
        System.out.println("✅ Spécialité ajoutée !");
    }

    @Override
    public void update(Specialite specialite) throws SQLException {
        String sql = "UPDATE specialite SET nom=?, description=? WHERE id=?";
        PreparedStatement ps = getConnection().prepareStatement(sql);
        ps.setString(1, specialite.getNom());
        ps.setString(2, specialite.getDescription());
        ps.setInt(3, specialite.getId());
        ps.executeUpdate();
        System.out.println("✅ Spécialité modifiée !");
    }

    @Override
    public void delete(int id) throws SQLException {
        PreparedStatement ps = getConnection().prepareStatement("DELETE FROM specialite WHERE id=?");
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("✅ Spécialité supprimée !");
    }

    @Override
    public List<Specialite> getAll() throws SQLException {
        List<Specialite> list = new ArrayList<>();
        String sql = "SELECT * FROM specialite ORDER BY id DESC";
        Statement st = getConnection().createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        return list;
    }

    @Override
    public Specialite getById(int id) throws SQLException {
        String sql = "SELECT * FROM specialite WHERE id=?";
        PreparedStatement ps = getConnection().prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return mapResultSet(rs);
        }
        return null;
    }

    private Specialite mapResultSet(ResultSet rs) throws SQLException {
        Specialite s = new Specialite();
        s.setId(rs.getInt("id"));
        s.setNom(rs.getString("nom"));
        s.setDescription(rs.getString("description"));
        return s;
    }
}
