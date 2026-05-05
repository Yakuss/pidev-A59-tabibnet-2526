package com.pidev.utils;

import java.sql.*;

public class DataChecker {
    public static void main(String[] args) {
        try {
            Connection conn = DataSource.getInstance().getConnection();
            String[] tables = {"medecins", "patients", "appointment", "rendez_vous", "feedback"};
            for (String table : tables) {
                try {
                    ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM " + table);
                    if (rs.next()) {
                        System.out.println("Table '" + table + "': " + rs.getInt(1) + " rows");
                    }
                } catch (Exception e) {
                    System.out.println("Table '" + table + "' NOT FOUND or error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
