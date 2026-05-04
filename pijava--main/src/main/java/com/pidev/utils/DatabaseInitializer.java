package com.pidev.utils;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Initializes the database schema by creating necessary tables if they don't exist.
 */
public class DatabaseInitializer {
    
    public static void initializeDatabase() {
        try {
            Connection conn = DataSource.getInstance().getConnection();
            Statement stmt = conn.createStatement();
            
            // Create question table
            String createQuestionTable = "CREATE TABLE IF NOT EXISTS question (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "titre VARCHAR(255) NOT NULL," +
                    "description LONGTEXT NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "likes INT DEFAULT 0," +
                    "status VARCHAR(50) DEFAULT 'active'," +
                    "image_name VARCHAR(255)," +
                    "specialite_id INT," +
                    "patient_id INT," +
                    "FOREIGN KEY (specialite_id) REFERENCES specialite(id) ON DELETE SET NULL," +
                    "FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE," +
                    "INDEX idx_specialite (specialite_id)," +
                    "INDEX idx_patient (patient_id)," +
                    "INDEX idx_created_at (created_at)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            
            stmt.execute(createQuestionTable);
            System.out.println("✅ Question table initialized!");
            
            // Create reponse table
            String createReponseTable = "CREATE TABLE IF NOT EXISTS reponse (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "question_id INT NOT NULL," +
                    "medecin_id INT," +
                    "patient_id INT," +
                    "contenu LONGTEXT NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "likes INT DEFAULT 0," +
                    "FOREIGN KEY (question_id) REFERENCES question(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (medecin_id) REFERENCES medecins(id) ON DELETE SET NULL," +
                    "FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE SET NULL," +
                    "INDEX idx_question (question_id)," +
                    "INDEX idx_medecin (medecin_id)," +
                    "INDEX idx_patient (patient_id)," +
                    "INDEX idx_created_at (created_at)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            
            stmt.execute(createReponseTable);
            System.out.println("✅ Reponse table initialized!");
            
            stmt.close();
        } catch (Exception e) {
            System.err.println("❌ Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
