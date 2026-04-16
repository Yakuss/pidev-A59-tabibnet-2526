package com.pidev.services;

import com.pidev.constants.Governorate;
import com.pidev.constants.Specialty;
import com.pidev.models.Admin;
import com.pidev.models.BaseUser;
import com.pidev.models.Medecin;
import com.pidev.models.Patient;
import com.pidev.utils.DataSource;
import com.pidev.utils.PasswordUtils;

import java.nio.charset.StandardCharsets;
import java.sql.*;

public class AuthService {

    private Connection getConnection() throws SQLException {
        return DataSource.getInstance().getConnection();
    }

    public BaseUser login(String email, String plainPassword) throws Exception {
        BaseUser user = findInTable("admins", email, plainPassword, "admin");
        if (user != null) return user;
        user = findInTable("medecins", email, plainPassword, "medecin");
        if (user != null) return user;
        user = findInTable("patients", email, plainPassword, "patient");
        return user;
    }

    private BaseUser findInTable(String table, String email, String plainPassword, String discriminator) throws SQLException {
        String sql = "SELECT * FROM " + table + " WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String storedHash = rs.getString("password");
                    boolean isActive = rs.getBoolean("is_active");

                    System.out.println("========== LOGIN VERIFICATION ==========");
                    System.out.println("Table: " + table + ", ID: " + id);
                    System.out.println("Stored hash: [" + storedHash + "]");
                    System.out.println("Plain password entered: [" + plainPassword + "]");
                    System.out.println("Entered password bytes (UTF-8): " + bytesToHex(plainPassword.getBytes(StandardCharsets.UTF_8)));

                    String computedHash = PasswordUtils.hashPassword(plainPassword);
                    System.out.println("Computed hash: [" + computedHash + "]");
                    System.out.println("Stored equals computed? " + storedHash.equals(computedHash));
                    System.out.println("========================================");

                    if (!isActive) return null;

                    if (PasswordUtils.verifyPassword(plainPassword, storedHash)) {
                        switch (discriminator) {
                            case "admin": return mapAdmin(rs);
                            case "medecin": return mapMedecin(rs);
                            case "patient": return mapPatient(rs);
                        }
                    }
                }
            }
        }
        return null;
    }

    // Add this helper to AuthService (same bytesToHex method)
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM admins WHERE LOWER(email) = LOWER(?) " +
                "UNION SELECT 1 FROM medecins WHERE LOWER(email) = LOWER(?) " +
                "UNION SELECT 1 FROM patients WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, email);
            stmt.setString(3, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ---------- Mapping methods (unchanged) ----------
    private Admin mapAdmin(ResultSet rs) throws SQLException {
        Admin admin = new Admin();
        admin.setId(rs.getInt("id"));
        admin.setEmail(rs.getString("email"));
        admin.setPassword(rs.getString("password"));
        admin.setFirstName(rs.getString("first_name"));
        admin.setLastName(rs.getString("last_name"));
        admin.setAge(rs.getInt("age"));
        admin.setGender(rs.getString("gender"));
        admin.setActive(rs.getBoolean("is_active"));
        admin.setRoles(rs.getString("roles"));
        admin.setName(rs.getString("name"));
        admin.setDiscriminator("admin");
        return admin;
    }

    private Medecin mapMedecin(ResultSet rs) throws SQLException {
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
        m.setDiscriminator("medecin");
        m.setPhoneNumber(rs.getString("phone_number"));

        String specialtyStr = rs.getString("specialty");
        if (specialtyStr != null && !specialtyStr.isEmpty()) {
            try { m.setSpecialty(Specialty.valueOf(specialtyStr)); } catch (IllegalArgumentException e) {}
        }
        m.setCin(rs.getString("cin"));
        m.setAddress(rs.getString("address"));

        String govStr = rs.getString("governorate");
        if (govStr != null && !govStr.isEmpty()) {
            try { m.setGovernorate(Governorate.valueOf(govStr)); } catch (IllegalArgumentException e) {}
        }
        m.setEducation(rs.getString("education"));
        m.setExperience(rs.getString("experience"));
        m.setVerified(rs.getBoolean("is_verified"));
        double fee = rs.getDouble("consultation_fee");
        if (!rs.wasNull()) m.setConsultationFee(fee);
        return m;
    }

    private Patient mapPatient(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setId(rs.getInt("id"));
        p.setEmail(rs.getString("email"));
        p.setPassword(rs.getString("password"));
        p.setFirstName(rs.getString("first_name"));
        p.setLastName(rs.getString("last_name"));
        p.setAge(rs.getInt("age"));
        p.setGender(rs.getString("gender"));
        p.setActive(rs.getBoolean("is_active"));
        p.setRoles(rs.getString("roles"));
        p.setDiscriminator("patient");
        p.setPhoneNumber(rs.getString("phone_number"));
        p.setAddress(rs.getString("address"));
        Timestamp dob = rs.getTimestamp("date_of_birth");
        p.setDateOfBirth(dob != null ? dob.toLocalDateTime() : null);
        p.setHasInsurance(rs.getBoolean("has_insurance"));
        p.setInsuranceNumber(rs.getString("insurance_number"));
        return p;
    }
}