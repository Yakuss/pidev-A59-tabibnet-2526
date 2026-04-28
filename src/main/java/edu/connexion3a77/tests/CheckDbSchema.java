package edu.connexion3a77.tests;

import edu.connexion3a77.tools.MyConnection;
import java.sql.*;

public class CheckDbSchema {
    public static void main(String[] args) {
        try (Connection cnx = MyConnection.getInstance().getCnx()) {
            DatabaseMetaData meta = cnx.getMetaData();
            
            System.out.println("Checking rapport table...");
            ResultSet rs = meta.getColumns(null, null, "rapport", "document_id");
            if (rs.next()) {
                System.out.println("Column document_id exists in rapport.");
            } else {
                System.out.println("Column document_id MISSING in rapport.");
            }
            
            System.out.println("Checking ordonnances table...");
            rs = meta.getColumns(null, null, "ordonnances", "document_id");
            if (rs.next()) {
                System.out.println("Column document_id exists in ordonnances.");
            } else {
                System.out.println("Column document_id MISSING in ordonnances.");
            }
            
            System.out.println("Checking document table...");
            rs = meta.getColumns(null, null, "document", "nb_rapports");
            if (rs.next()) {
                System.out.println("Column nb_rapports exists in document.");
            } else {
                System.out.println("Column nb_rapports MISSING in document.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
