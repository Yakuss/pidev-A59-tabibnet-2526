package com.pidev.utils;

import java.sql.*;

/**
 * Temporary utility to print the actual column names of the question and reponse tables.
 */
public class SchemaChecker {
    public static void main(String[] args) {
        Connection conn = DataSource.getInstance().getConnection();
        try {
            System.out.println("=== QUESTION TABLE COLUMNS ===");
            ResultSet rs = conn.getMetaData().getColumns(null, null, "question", null);
            while (rs.next()) {
                System.out.println("  " + rs.getString("COLUMN_NAME") + " (" + rs.getString("TYPE_NAME") + ")");
            }

            System.out.println("\n=== REPONSE TABLE COLUMNS ===");
            rs = conn.getMetaData().getColumns(null, null, "reponse", null);
            while (rs.next()) {
                System.out.println("  " + rs.getString("COLUMN_NAME") + " (" + rs.getString("TYPE_NAME") + ")");
            }

            System.out.println("\n=== SPECIALITE TABLE COLUMNS ===");
            rs = conn.getMetaData().getColumns(null, null, "specialite", null);
            while (rs.next()) {
                System.out.println("  " + rs.getString("COLUMN_NAME") + " (" + rs.getString("TYPE_NAME") + ")");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
