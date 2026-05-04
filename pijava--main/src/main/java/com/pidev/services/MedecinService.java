package com.pidev.services;

import com.pidev.constant.Governorate;
import com.pidev.constant.Specialty;
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
        String sql = "INSERT INTO medecins (email, password, first_name, last_name, gender, is_active, roles, " +
                     "phone_number, specialty, cin, address, governorate, education, experience, is_verified, " +
                     "ai_average_score) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, medecin.getEmail());
        ps.setString(2, medecin.getPassword());
        ps.setString(3, medecin.getFirstName());
        ps.setString(4, medecin.getLastName());
        ps.setString(5, medecin.getGender());
        ps.setBoolean(6, medecin.isActive());
        ps.setString(7, medecin.getRoles() != null ? medecin.getRoles() : "[\"ROLE_MEDECIN\"]");
        ps.setString(8, medecin.getPhoneNumber());
        ps.setString(9, medecin.getSpecialty() != null ? medecin.getSpecialty().name() : null);
        ps.setString(10, medecin.getCin());
        ps.setString(11, medecin.getAddress());
        ps.setString(12, medecin.getGovernorate() != null ? medecin.getGovernorate().name() : null);
        ps.setString(13, medecin.getEducation());
        ps.setString(14, medecin.getExperience());
        ps.setBoolean(15, medecin.isVerified());
        ps.setObject(16, medecin.getAiAverageScore());
        ps.executeUpdate();
        System.out.println("✅ Medecin added to table 'medecins'!");
    }

    @Override
    public void update(Medecin medecin) throws SQLException {
        String sql = "UPDATE medecins SET email=?, first_name=?, last_name=?, gender=?, is_active=?, " +
                     "phone_number=?, specialty=?, cin=?, address=?, governorate=?, education=?, " +
                     "experience=?, is_verified=?, ai_average_score=? WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, medecin.getEmail());
        ps.setString(2, medecin.getFirstName());
        ps.setString(3, medecin.getLastName());
        ps.setString(4, medecin.getGender());
        ps.setBoolean(5, medecin.isActive());
        ps.setString(6, medecin.getPhoneNumber());
        ps.setString(7, medecin.getSpecialty() != null ? medecin.getSpecialty().name() : null);
        ps.setString(8, medecin.getCin());
        ps.setString(9, medecin.getAddress());
        ps.setString(10, medecin.getGovernorate() != null ? medecin.getGovernorate().name() : null);
        ps.setString(11, medecin.getEducation());
        ps.setString(12, medecin.getExperience());
        ps.setBoolean(13, medecin.isVerified());
        ps.setObject(14, medecin.getAiAverageScore());
        ps.setInt(15, medecin.getId());
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
        try { m.setAge(rs.getInt("age")); } catch (SQLException e) { m.setAge(0); }
        try { m.setGender(rs.getString("gender")); } catch (SQLException e) { m.setGender(null); }
        m.setActive(rs.getBoolean("is_active"));
        m.setRoles(rs.getString("roles"));
        try { m.setPhoneNumber(rs.getString("phone_number")); } catch (SQLException e) { m.setPhoneNumber(null); }
        // Parse specialty — stored as enum name (e.g. "CARDIOLOGIE") or display name
        String specialtyRaw = rs.getString("specialty");
        if (specialtyRaw != null) {
            Specialty spec = null;
            try { spec = Specialty.valueOf(specialtyRaw); } catch (IllegalArgumentException ignored) {}
            if (spec == null) spec = Specialty.fromDisplayName(specialtyRaw);
            m.setSpecialty(spec);
        }
        try { m.setCin(rs.getString("cin")); } catch (SQLException e) { m.setCin(null); }
        try { m.setAddress(rs.getString("address")); } catch (SQLException e) { m.setAddress(null); }
        // Parse governorate — stored as enum name (e.g. "TUNIS") or display name
        String govRaw = rs.getString("governorate");
        if (govRaw != null) {
            Governorate gov = null;
            try { gov = Governorate.valueOf(govRaw); } catch (IllegalArgumentException ignored) {}
            if (gov == null) gov = Governorate.fromDisplayName(govRaw);
            m.setGovernorate(gov);
        }
        try { m.setEducation(rs.getString("education")); } catch (SQLException e) { m.setEducation(null); }
        try { m.setExperience(rs.getString("experience")); } catch (SQLException e) { m.setExperience(null); }
        try { m.setVerified(rs.getBoolean("is_verified")); } catch (SQLException e) { m.setVerified(false); }
        try { m.setAiAverageScore(rs.getObject("ai_average_score") != null ?
                rs.getDouble("ai_average_score") : null); } catch (SQLException e) { m.setAiAverageScore(null); }
        
        // Parse rating fields
        try { m.setAverageRating(rs.getObject("averageRating") != null ?
                rs.getDouble("averageRating") : 0.0); } catch (SQLException e) { m.setAverageRating(0.0); }
        try { m.setTotalReviews(rs.getObject("totalReviews") != null ?
                rs.getInt("totalReviews") : 0); } catch (SQLException e) { m.setTotalReviews(0); }
        
        return m;
    }

    /**
     * Toggle the active status of a medecin account (for admin use)
     */
    public void toggleActiveStatus(int medecinId) throws SQLException {
        String sql = "UPDATE medecins SET is_active = NOT is_active WHERE id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, medecinId);
        int rowsAffected = ps.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("✅ Medecin active status toggled for ID: " + medecinId);
        } else {
            throw new SQLException("Medecin not found with ID: " + medecinId);
        }
    }

    /**
     * Set the active status of a medecin account (for admin use)
     */
    public void setActiveStatus(int medecinId, boolean isActive) throws SQLException {
        String sql = "UPDATE medecins SET is_active = ? WHERE id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setBoolean(1, isActive);
        ps.setInt(2, medecinId);
        int rowsAffected = ps.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("✅ Medecin active status set to " + isActive + " for ID: " + medecinId);
        } else {
            throw new SQLException("Medecin not found with ID: " + medecinId);
        }
    }

    /**
     * Get count of active and inactive medecins
     */
    public int[] getActiveInactiveCounts() throws SQLException {
        String sql = "SELECT " +
                     "SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END) as active_count, " +
                     "SUM(CASE WHEN is_active = 0 THEN 1 ELSE 0 END) as inactive_count " +
                     "FROM medecins";
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new int[]{rs.getInt("active_count"), rs.getInt("inactive_count")};
        }
        return new int[]{0, 0};
    }

    /**
     * Update doctor's rating based on all their feedback
     * Calculates average rating from all feedback for this doctor
     */
    public void updateDoctorRating(int medecinId) throws SQLException {
        String sql = "UPDATE medecins m " +
                     "SET averageRating = ( " +
                     "    SELECT COALESCE(AVG(f.note), 0) " +
                     "    FROM feedback f " +
                     "    INNER JOIN rendezvous r ON f.rendezVousId = r.id " +
                     "    WHERE r.medecinId = ? " +
                     "), " +
                     "totalReviews = ( " +
                     "    SELECT COUNT(*) " +
                     "    FROM feedback f " +
                     "    INNER JOIN rendezvous r ON f.rendezVousId = r.id " +
                     "    WHERE r.medecinId = ? " +
                     ") " +
                     "WHERE m.id = ?";
        
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, medecinId);
        ps.setInt(2, medecinId);
        ps.setInt(3, medecinId);
        int rowsAffected = ps.executeUpdate();
        
        if (rowsAffected > 0) {
            System.out.println("✅ Doctor rating updated for ID: " + medecinId);
        }
    }

    /**
     * Get doctor's current rating information
     */
    public String getRatingInfo(int medecinId) throws SQLException {
        String sql = "SELECT averageRating, totalReviews FROM medecins WHERE id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, medecinId);
        ResultSet rs = ps.executeQuery();
        
        if (rs.next()) {
            double avgRating = rs.getDouble("averageRating");
            int totalReviews = rs.getInt("totalReviews");
            return String.format("%.2f ⭐ (%d avis)", avgRating, totalReviews);
        }
        return "Aucun avis";
    }
}
