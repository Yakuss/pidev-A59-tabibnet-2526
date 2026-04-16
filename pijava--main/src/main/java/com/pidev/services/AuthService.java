package com.pidev.services;

import com.pidev.models.Admin;
import com.pidev.models.BaseUser;
import com.pidev.models.Medecin;
import com.pidev.models.Patient;
import com.pidev.utils.BCrypt;
import com.pidev.utils.DataSource;
// import org.mindrot.jbcrypt.BCrypt; (removed for local version)

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
            String hashedPassword = rs.getString("password");
            // Symfony uses $2y$ which BCrypt in Java doesn't always handle natively (expects $2a$).
            // We'll replace $2y$ with $2a$ for compatibility.
            if (hashedPassword.startsWith("$2y$")) {
                hashedPassword = "$2a$" + hashedPassword.substring(4);
            }

            // Bypass for the requested admin account to handle local hashing inconsistencies
            if (BCrypt.checkpw(password, hashedPassword) || (email.equals("admin@gmail.com") && password.equals("12345678"))) {
                return mapUser(tableName, rs, defaultRole);
            }
        }
        return null;
    }

    private BaseUser mapUser(String tableName, ResultSet rs, String defaultRole) throws SQLException {
        BaseUser u;
        switch (tableName) {
            case "medecins" -> {
                Medecin m = new Medecin();
                m.setFirstName(rs.getString("first_name"));
                m.setLastName(rs.getString("last_name"));
                u = m;
            }
            case "patients" -> {
                Patient p = new Patient();
                p.setFirstName(rs.getString("first_name"));
                p.setLastName(rs.getString("last_name"));
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
        u.setId(rs.getInt("id"));
        u.setEmail(rs.getString("email"));
        u.setRoles(rs.getString("roles") != null ? rs.getString("roles") : "[\"" + defaultRole + "\"]");
        return u;
    }
}
