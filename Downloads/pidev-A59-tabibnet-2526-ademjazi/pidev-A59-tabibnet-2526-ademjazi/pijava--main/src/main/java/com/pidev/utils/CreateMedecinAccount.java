package com.pidev.utils;

import java.sql.*;

/**
 * Utility to create a medecin test account with proper BCrypt hashing
 */
public class CreateMedecinAccount {
    
    public static void main(String[] args) {
        Connection conn = DataSource.getInstance().getConnection();
        
        String email = "medecin@test.com";
        String plainPassword = "medecin123";
        
        // Generate hash using BCrypt
        String salt = BCrypt.gensalt();
        String hashedPassword = BCrypt.hashpw(plainPassword, salt);
        
        System.out.println("===========================================");
        System.out.println("  Création du compte médecin");
        System.out.println("===========================================");
        System.out.println("Email: " + email);
        System.out.println("Mot de passe: " + plainPassword);
        System.out.println("Hash: " + hashedPassword);
        System.out.println();
        
        try {
            // Check if exists
            String checkSql = "SELECT id FROM medecins WHERE email = ?";
            PreparedStatement checkPs = conn.prepareStatement(checkSql);
            checkPs.setString(1, email);
            ResultSet rs = checkPs.executeQuery();
            
            if (rs.next()) {
                // Update existing
                String updateSql = "UPDATE medecins SET password = ?, first_name = ?, last_name = ?, " +
                                   "specialty = ?, governorate = ?, phone = ?, is_active = 1 " +
                                   "WHERE email = ?";
                PreparedStatement updatePs = conn.prepareStatement(updateSql);
                updatePs.setString(1, hashedPassword);
                updatePs.setString(2, "Dr. Ahmed");
                updatePs.setString(3, "Ben Ali");
                updatePs.setString(4, "Cardiologie");
                updatePs.setString(5, "Tunis");
                updatePs.setString(6, "+216 20 123 456");
                updatePs.setString(7, email);
                updatePs.executeUpdate();
                System.out.println("✅ Compte médecin mis à jour avec succès!");
            } else {
                // Insert new
                String insertSql = "INSERT INTO medecins (email, password, roles, first_name, last_name, " +
                                   "specialty, governorate, phone, is_active) " +
                                   "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement insertPs = conn.prepareStatement(insertSql);
                insertPs.setString(1, email);
                insertPs.setString(2, hashedPassword);
                insertPs.setString(3, "[\"ROLE_MEDECIN\"]");
                insertPs.setString(4, "Dr. Ahmed");
                insertPs.setString(5, "Ben Ali");
                insertPs.setString(6, "Cardiologie");
                insertPs.setString(7, "Tunis");
                insertPs.setString(8, "+216 20 123 456");
                insertPs.setInt(9, 1);
                insertPs.executeUpdate();
                System.out.println("✅ Compte médecin créé avec succès!");
            }
            
            System.out.println();
            System.out.println("===========================================");
            System.out.println("  INFORMATIONS DE CONNEXION");
            System.out.println("===========================================");
            System.out.println("Email       : medecin@test.com");
            System.out.println("Mot de passe: medecin123");
            System.out.println("===========================================");
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
