package com.pidev.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton DataSource - manages the MySQL database connection.
 * Connects to the shared 'pidev' database used by both Symfony and JavaFX.
 */
public class DataSource {
    private static DataSource instance;

    // ⚠️ Update these values to match your local database configuration
// Correct version
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/pidev";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Empty for local XAMPP/WAMP
    private Connection connection;

    private DataSource() {
        try {
            // Explicitly load the driver to avoid issues in some environments
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connected to database successfully!");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
            System.err.println("👉 Check AlwaysData dashboard: Remote access must be enabled for your IP.");
            e.printStackTrace();
        }
    }

    public static DataSource getInstance() {
        if (instance == null) {
            instance = new DataSource();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            System.err.println("❌ Reconnection failed: " + e.getMessage());
        }
        return connection;
    }
}
