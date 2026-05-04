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
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/pidev?connectTimeout=5000&socketTimeout=30000&autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Empty for local XAMPP/WAMP
    private Connection connection;

    private DataSource() {
        connect();
    }

    private void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            DriverManager.setLoginTimeout(10);
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connected to database successfully!");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
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
                System.out.println("🔄 Reconnecting to database...");
                connect();
            }
            // Test connection with a lightweight ping
            if (connection != null) {
                connection.isValid(2);
            }
        } catch (SQLException e) {
            System.err.println("❌ Reconnection failed: " + e.getMessage());
            connect();
        }
        return connection;
    }
}
