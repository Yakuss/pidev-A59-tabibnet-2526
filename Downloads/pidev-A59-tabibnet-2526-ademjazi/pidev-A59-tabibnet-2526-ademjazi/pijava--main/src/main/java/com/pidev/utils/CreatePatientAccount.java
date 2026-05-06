package com.pidev.utils;

import java.sql.*;

/**
 * Utility to create a patient test account with proper BCrypt hashing
 */
public class CreatePatientAccount {
    
    public static void main(String[] args) {
        Connection conn = DataSource.getInstance().getConnection();
        
        String email = "patient@test.com";
        String plainPassword = "patient123";
        
        // Generate hash using BCrypt
        String salt = BCrypt.gensalt();
        String hashedPassword = BCrypt.hashpw(plainPassword, salt);
        
        System.out.println("===========================================");
        System.out.println("  Création du compte patient");
        System.out.println("===========================================");
        System.out.println("Email: " + email);
        System.out.println("Mot de passe: " + plainPassword);
        System.out.println("Hash: " + hashedPassword);
        System.out.println();
        
        try {
            // Check if exists
            String checkSql = "SELECT id FROM patients WHERE email = ?";
            PreparedStatement checkPs = conn.prepareStatement(checkSql);
            checkPs.setString(1, email);
            ResultSet rs = checkPs.executeQuery();
            
            if (rs.next()) {
                // Update existing
                String updateSql = "UPDATE patients SET password = ?, first_name = ?, last_name = ?, " +
                                   "phone = ?, is_active = 1 WHERE email = ?";
                PreparedStatement updatePs = conn.prepareStatement(updateSql);
                updatePs.setString(1, hashedPassword);
                updatePs.setString(2, "Fatma");
                updatePs.setString(3, "Trabelsi");
                updatePs.setString(4, "+216 22 987 654");
                updatePs.setString(5, email);
                updatePs.executeUpdate();
                System.out.println("✅ Compte patient mis à jour avec succès!");
            } else {
                // Insert new
                String insertSql = "INSERT INTO patients (email, password, roles, first_name, last_name, " +
                                   "phone, is_active) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement insertPs = conn.prepareStatement(insertSql);
                insertPs.setString(1, email);
                insertPs.setString(2, hashedPassword);
                insertPs.setString(3, "[\"ROLE_PATIENT\"]");
                insertPs.setString(4, "Fatma");
                insertPs.setString(5, "Trabelsi");
                insertPs.setString(6, "+216 22 987 654");
                insertPs.setInt(7, 1);
                insertPs.executeUpdate();
                System.out.println("✅ Compte patient créé avec succès!");
            }
            
            System.out.println();
            System.out.println("===========================================");
            System.out.println("  INFORMATIONS DE CONNEXION");
            System.out.println("===========================================");
            System.out.println("Email       : patient@test.com");
            System.out.println("Mot de passe: patient123");
            System.out.println("===========================================");
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
