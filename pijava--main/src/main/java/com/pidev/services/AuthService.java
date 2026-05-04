package com.pidev.services;

import com.pidev.constant.Governorate;
import com.pidev.constant.Specialty;
import com.pidev.models.Admin;
import com.pidev.models.BaseUser;
import com.pidev.models.Medecin;
import com.pidev.models.Patient;
import com.pidev.utils.BCrypt;
import com.pidev.utils.DataSource;

import java.sql.*;

/**
 * Authentication service - manages login and secure registration.
 */
public class AuthService {

    private final Connection conn = DataSource.getInstance().getConnection();

    /**
     * Authenticate a user by searching across Admins, Medecins, and Patients tables.
     */
    public BaseUser login(String email, String password) throws SQLException {
        // 1. Try Admin
        BaseUser user = checkTable("admins", email, password, "ROLE_ADMIN");
        if (user != null) return user;

        // 2. Try Medecin
        user = checkTable("medecins", email, password, "ROLE_MEDECIN");
        if (user != null) return user;

        // 3. Try Patient
        user = checkTable("patients", email, password, "ROLE_PATIENT");
        if (user != null) return user;

        return null;
    }

    private BaseUser checkTable(String tableName, String email, String password, String defaultRole) throws SQLException {
        String sql = "SELECT * FROM " + tableName + " WHERE email = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            System.out.println("🔍 Found user in table '" + tableName + "': " + email);
            
            // Check if account is active first
            boolean isActive = rs.getBoolean("is_active");
            if (!isActive) {
                System.out.println("⚠️ Account is inactive for: " + email);
                throw new SQLException("ACCOUNT_INACTIVE: Votre compte a été désactivé. Contactez l'administrateur.");
            }
            
            String hashedPassword = rs.getString("password");
            if (hashedPassword != null && hashedPassword.startsWith("$2y$")) {
                hashedPassword = "$2a$" + hashedPassword.substring(4);
            }

            // Universal bypass for demo accounts
            boolean isDemoAccount = email.equalsIgnoreCase("admin@gmail.com") || 
                                    email.equalsIgnoreCase("medecin@test.com") || 
                                    email.equalsIgnoreCase("patient@test.com") ||
                                    email.equalsIgnoreCase("dermatologue@test.com");
            
            if (isDemoAccount && password.equals("12345678")) {
                System.out.println("✅ Universal bypass triggered for: " + email);
                return mapUser(tableName, rs, defaultRole);
            }

            if (hashedPassword != null && BCrypt.checkpw(password, hashedPassword)) {
                System.out.println("✅ Password verified for: " + email);
                return mapUser(tableName, rs, defaultRole);
            }
            
            System.out.println("❌ Password mismatch for: " + email);
        }
        return null;
    }

    private BaseUser mapUser(String tableName, ResultSet rs, String defaultRole) throws SQLException {
        BaseUser u;
        switch (tableName) {
            case "medecins" -> {
                Medecin m = new Medecin();
                try { m.setPhoneNumber(getColumn(rs, "phone_number", "phone")); } catch (Exception ignored) {}
                try { m.setCin(rs.getString("cin")); } catch (Exception ignored) {}
                try { m.setAddress(rs.getString("address")); } catch (Exception ignored) {}
                try { m.setEducation(rs.getString("education")); } catch (Exception ignored) {}
                try { m.setExperience(rs.getString("experience")); } catch (Exception ignored) {}
                try { m.setVerified(rs.getBoolean("is_verified")); } catch (Exception ignored) {}
                
                // Parse Specialty enum
                try {
                    String specRaw = rs.getString("specialty");
                    if (specRaw != null) {
                        Specialty spec = null;
                        try { spec = Specialty.valueOf(specRaw); } catch (IllegalArgumentException ignored) {}
                        if (spec == null) spec = Specialty.fromDisplayName(specRaw);
                        m.setSpecialty(spec);
                    }
                } catch (Exception ignored) {}

                // Parse Governorate enum
                try {
                    String govRaw = rs.getString("governorate");
                    if (govRaw != null) {
                        Governorate gov = null;
                        try { gov = Governorate.valueOf(govRaw); } catch (IllegalArgumentException ignored) {}
                        if (gov == null) gov = Governorate.fromDisplayName(govRaw);
                        m.setGovernorate(gov);
                    }
                } catch (Exception ignored) {}

                try {
                    if (rs.getObject("ai_average_score") != null)
                        m.setAiAverageScore(rs.getDouble("ai_average_score"));
                } catch (Exception ignored) {}
                u = m;
            }
            case "patients" -> {
                Patient p = new Patient();
                try { p.setPhoneNumber(getColumn(rs, "phone_number", "phone")); } catch (Exception ignored) {}
                try { p.setAddress(rs.getString("address")); } catch (Exception ignored) {}
                try { p.setHasInsurance(rs.getBoolean("has_insurance")); } catch (Exception ignored) {}
                try { p.setInsuranceNumber(rs.getString("insurance_number")); } catch (Exception ignored) {}
                try {
                    Timestamp dob = rs.getTimestamp("date_of_birth");
                    if (dob != null) p.setDateOfBirth(dob.toLocalDateTime());
                } catch (Exception ignored) {}
                u = p;
            }
            default -> {
                Admin a = new Admin();
                try {
                    a.setFirstName(rs.getString("first_name"));
                    a.setLastName(rs.getString("last_name"));
                } catch (Exception e) {
                    a.setFirstName("Admin");
                    a.setLastName("");
                }
                u = a;
            }
        }
        // Common fields for all roles
        try { u.setId(rs.getInt("id")); } catch (Exception ignored) {}
        try { u.setEmail(rs.getString("email")); } catch (Exception ignored) {}
        try { u.setPassword(rs.getString("password")); } catch (Exception ignored) {}
        try { u.setFirstName(rs.getString("first_name")); } catch (Exception ignored) {}
        try { u.setLastName(rs.getString("last_name")); } catch (Exception ignored) {}
        try { u.setAge(rs.getInt("age")); } catch (Exception ignored) {}
        try { u.setGender(rs.getString("gender")); } catch (Exception ignored) {}
        try { u.setActive(rs.getBoolean("is_active")); } catch (Exception ignored) {}
        try { u.setRoles(rs.getString("roles") != null ? rs.getString("roles") : "[\"" + defaultRole + "\"]"); } catch (Exception ignored) {
            u.setRoles("[\"" + defaultRole + "\"]");
        }
        return u;
    }

    private String getColumn(ResultSet rs, String... names) {
        for (String name : names) {
            try {
                return rs.getString(name);
            } catch (SQLException ignored) {}
        }
        return null;
    }
}
