package edu.connexion3a77.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MyConnection {

    private String baseUrl = "jdbc:mysql://localhost:3306/";
    private String dbName = "3a37";
    private String login = "root";
    private String pwd = "";

    private Connection cnx;
    public static MyConnection instance;

    private MyConnection() {
        try {
            // 1. Initialiser le driver et créer la BD si elle n'existe pas
            Connection initCnx = DriverManager.getConnection(baseUrl, login, pwd);
            Statement st = initCnx.createStatement();
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + dbName + "`");
            initCnx.close();

            // 2. Se connecter sur la BD
            cnx = DriverManager.getConnection(baseUrl + dbName, login, pwd);
            System.out.println("Connexion établie à la base de données !");

            // 3. Générer les tables automatiquement
            createTables();

        } catch (SQLException e) {
            System.out.println("Erreur de connexion : " + e.getMessage());
        }
    }

    private void createTables() {
        try {
            Statement st = cnx.createStatement();
            
            st.executeUpdate("CREATE TABLE IF NOT EXISTS personne (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nom VARCHAR(255) NOT NULL, " +
                    "prenom VARCHAR(255) NOT NULL)");
                    
            st.executeUpdate("CREATE TABLE IF NOT EXISTS ordonnances (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "date_ordonnance DATETIME NOT NULL, " +
                    "diagnosis VARCHAR(255) NOT NULL, " +
                    "medicament VARCHAR(255) NOT NULL, " +
                    "posologie VARCHAR(255) NOT NULL, " +
                    "notes TEXT, " +
                    "instructions TEXT, " +
                    "created_at DATETIME, " +
                    "updated_at DATETIME)");
                    
            st.executeUpdate("CREATE TABLE IF NOT EXISTS rapport (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "consultation_reason VARCHAR(255) NOT NULL, " +
                    "diagnosis VARCHAR(255) NOT NULL, " +
                    "observations TEXT NOT NULL, " +
                    "recommendations TEXT NOT NULL, " +
                    "treatments TEXT NOT NULL, " +
                    "created_at DATETIME, " +
                    "updated_at DATETIME)");
                    
            st.executeUpdate("CREATE TABLE IF NOT EXISTS document (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nom_fichier VARCHAR(255) NOT NULL, " +
                    "chemin_fichier VARCHAR(255) NOT NULL, " +
                    "type VARCHAR(50) DEFAULT 'pdf')");
                    
            System.out.println("Tables vérifiées/créées avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur de création des tables : " + e.getMessage());
        }
    }

    public Connection getCnx() {
        return cnx;
    }

    public static MyConnection getInstance() {
        if (instance == null) {
            instance = new MyConnection();
        }
        return instance;
    }
}
