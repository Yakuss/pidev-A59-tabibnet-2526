package com.pidev.services;

import com.pidev.models.Medecin;
import com.pidev.utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD service for Medecin entity using JDBC.
 */
public class MedecinService implements IService<Medecin> {

    private final Connection conn = DataSource.getInstance().getConnection();

    @Override
    public void add(Medecin medecin) throws SQLException {
        String sql = "INSERT INTO medecins (email, password, first_name, last_name, age, gender, is_active, roles, " +
                     "phone_number, specialty, cin, address, governorate, education, experience, is_verified, " +
                     "ai_average_score) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, medecin.getEmail());
        ps.setString(2, medecin.getPassword());
        ps.setString(3, medecin.getFirstName());
        ps.setString(4, medecin.getLastName());
        ps.setInt(5, medecin.getAge());
        ps.setString(6, medecin.getGender());
        ps.setBoolean(7, medecin.isActive());
        ps.setString(8, medecin.getRoles() != null ? medecin.getRoles() : "[\"ROLE_MEDECIN\"]");
        ps.setString(9, medecin.getPhoneNumber());
        ps.setString(10, medecin.getSpecialty());
        ps.setString(11, medecin.getCin());
        ps.setString(12, medecin.getAddress());
        ps.setString(13, medecin.getGovernorate());
        ps.setString(14, medecin.getEducation());
        ps.setString(15, medecin.getExperience());
        ps.setBoolean(16, medecin.isVerified());
        ps.setObject(17, medecin.getAiAverageScore());
        ps.executeUpdate();
        System.out.println("✅ Medecin added to table 'medecins'!");
    }

    @Override
    public void update(Medecin medecin) throws SQLException {
        String sql = "UPDATE medecins SET email=?, first_name=?, last_name=?, age=?, gender=?, is_active=?, " +
                     "phone_number=?, specialty=?, cin=?, address=?, governorate=?, education=?, " +
                     "experience=?, is_verified=?, ai_average_score=? WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, medecin.getEmail());
        ps.setString(2, medecin.getFirstName());
        ps.setString(3, medecin.getLastName());
        ps.setInt(4, medecin.getAge());
        ps.setString(5, medecin.getGender());
        ps.setBoolean(6, medecin.isActive());
        ps.setString(7, medecin.getPhoneNumber());
        ps.setString(8, medecin.getSpecialty());
        ps.setString(9, medecin.getCin());
        ps.setString(10, medecin.getAddress());
        ps.setString(11, medecin.getGovernorate());
        ps.setString(12, medecin.getEducation());
        ps.setString(13, medecin.getExperience());
        ps.setBoolean(14, medecin.isVerified());
        ps.setObject(15, medecin.getAiAverageScore());
        ps.setInt(16, medecin.getId());
        ps.executeUpdate();
        System.out.println("✅ Medecin updated in 'medecins'!");
    }

    /** Updates only the password column for a given medecin id. */
    public void updatePassword(int id, String hashedPassword) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "UPDATE medecins SET password=? WHERE id=?");
        ps.setString(1, hashedPassword);
        ps.setInt(2, id);
        ps.executeUpdate();
        System.out.println("✅ Medecin password updated!");
    }

    @Override
    public void delete(int id) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("DELETE FROM medecins WHERE id=?");
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("✅ Medecin deleted from 'medecins'!");
    }

    @Override
    public List<Medecin> getAll() throws SQLException {
        List<Medecin> medecins = new ArrayList<>();
        String sql = "SELECT * FROM medecins ORDER BY id DESC";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            medecins.add(mapResultSet(rs));
        }
        return medecins;
    }

    @Override
    public Medecin getById(int id) throws SQLException {
        String sql = "SELECT * FROM medecins WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return mapResultSet(rs);
        }
        return null;
    }

    private Medecin mapResultSet(ResultSet rs) throws SQLException {
        Medecin m = new Medecin();
        m.setId(rs.getInt("id"));
        m.setEmail(rs.getString("email"));
        m.setPassword(rs.getString("password"));
        m.setFirstName(rs.getString("first_name"));
        m.setLastName(rs.getString("last_name"));
        m.setAge(rs.getInt("age"));
        m.setGender(rs.getString("gender"));
        m.setActive(rs.getBoolean("is_active"));
        m.setRoles(rs.getString("roles"));
        m.setPhoneNumber(rs.getString("phone_number"));
        m.setSpecialty(rs.getString("specialty"));
        m.setCin(rs.getString("cin"));
        m.setAddress(rs.getString("address"));
        m.setGovernorate(rs.getString("governorate"));
        m.setEducation(rs.getString("education"));
        m.setExperience(rs.getString("experience"));
        m.setVerified(rs.getBoolean("is_verified"));
        m.setAiAverageScore(rs.getObject("ai_average_score") != null ?
                rs.getDouble("ai_average_score") : null);
        return m;
    }
}
