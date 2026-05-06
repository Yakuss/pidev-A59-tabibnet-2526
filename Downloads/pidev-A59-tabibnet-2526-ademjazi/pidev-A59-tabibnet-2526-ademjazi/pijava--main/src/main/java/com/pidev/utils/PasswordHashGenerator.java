package com.pidev.utils;

/**
 * Utility to generate BCrypt password hashes for test accounts
 */
public class PasswordHashGenerator {
    
    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("  Génération des hash BCrypt");
        System.out.println("===========================================\n");
        
        // Générer les hash pour les mots de passe de test
        String[] passwords = {"admin123", "medecin123", "patient123"};
        String[] labels = {"Admin", "Medecin", "Patient"};
        
        for (int i = 0; i < passwords.length; i++) {
            String salt = BCrypt.gensalt();
            String hash = BCrypt.hashpw(passwords[i], salt);
            
            System.out.println(labels[i] + ":");
            System.out.println("  Mot de passe: " + passwords[i]);
            System.out.println("  Hash BCrypt : " + hash);
            System.out.println();
        }
        
        System.out.println("===========================================");
    }
}
