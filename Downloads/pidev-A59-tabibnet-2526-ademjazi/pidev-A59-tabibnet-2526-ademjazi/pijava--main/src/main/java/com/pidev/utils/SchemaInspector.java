package com.pidev.utils;

import java.sql.*;

public class SchemaInspector {
    public static void main(String[] args) {
        try {
            Connection conn = DataSource.getInstance().getConnection();
            DatabaseMetaData meta = conn.getMetaData();
            
            String[] tables = {"medecins", "patients", "appointment", "rendez_vous", "article", "feedback"};
            
            for (String table : tables) {
                System.out.println("--- Table: " + table + " ---");
                ResultSet columns = meta.getColumns(null, null, table, null);
                while (columns.next()) {
                    System.out.println("  " + columns.getString("COLUMN_NAME") + " (" + columns.getString("TYPE_NAME") + ")");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
