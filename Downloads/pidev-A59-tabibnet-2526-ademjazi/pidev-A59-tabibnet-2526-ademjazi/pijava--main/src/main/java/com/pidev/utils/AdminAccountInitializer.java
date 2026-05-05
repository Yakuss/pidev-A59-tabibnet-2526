package com.pidev.utils;

import java.sql.*;

/**
 * Utility to seed the requested admin account.
 */
public class AdminAccountInitializer {

    public static void main(String[] args) {
        seedAdmin();
    }

    public static void seedAdmin() {
        Connection conn = DataSource.getInstance().getConnection();
        String email = "admin@gmail.com";
        String plainPassword = "12345678";
        
        // Generate hash using the project's BCrypt utility
        String salt = BCrypt.gensalt();
        String hashedPassword = BCrypt.hashpw(plainPassword, salt);

        try {
            // Check if exists
            String checkSql = "SELECT id FROM admins WHERE email = ?";
            PreparedStatement checkPs = conn.prepareStatement(checkSql);
            checkPs.setString(1, email);
            ResultSet rs = checkPs.executeQuery();

            if (rs.next()) {
                System.out.println("ℹ️ Admin '" + email + "' already exists.");
            } else {
                // Insert new admin
                // Exactly matching the columns from phpMyAdmin screenshot
                String insertSql = "INSERT INTO admins (email, roles, password, first_name, last_name, age, gender, is_active, name) " +
                                   "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement insertPs = conn.prepareStatement(insertSql);
                insertPs.setString(1, email);
                insertPs.setString(2, "[\"ROLE_ADMIN\"]");
                insertPs.setString(3, hashedPassword);
                insertPs.setString(4, "Admin");
                insertPs.setString(5, "System");
                insertPs.setInt(6, 0);
                insertPs.setString(7, "M");
                insertPs.setInt(8, 1);
                insertPs.setString(9, "Admin");
                
                insertPs.executeUpdate();
                System.out.println("✅ Admin account created successfully: " + email);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error seeding admin: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
