package edu.connexion3a77.tests;

import edu.connexion3a77.tools.MyConnection;
import java.sql.*;

public class ListColumns {
    public static void main(String[] args) {
        try (Connection cnx = MyConnection.getInstance().getCnx()) {
            DatabaseMetaData meta = cnx.getMetaData();
            
            String[] tables = {"rapport", "ordonnances", "document"};
            for (String table : tables) {
                System.out.println("--- Table: " + table + " ---");
                ResultSet rs = meta.getColumns(null, null, table, null);
                while (rs.next()) {
                    System.out.println("Column: " + rs.getString("COLUMN_NAME") + " | Type: " + rs.getString("TYPE_NAME"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
