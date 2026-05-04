package com.pidev.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/tabibnet?connectTimeout=5000&socketTimeout=5000";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static Connection connection;

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // Instanciation ou reconnexion si la base de données (XAMPP) a expiré la session
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Connexion à la BD tabibnet réussie.");
            }
        } catch (SQLException e) {
            System.out.println("Erreur de connexion BD : " + e.getMessage());
        }
        return connection;
    }
}
