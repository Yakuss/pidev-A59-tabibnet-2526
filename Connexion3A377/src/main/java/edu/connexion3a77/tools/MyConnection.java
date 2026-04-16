package edu.connexion3a77.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {

    private final String url = "jdbc:mysql://localhost:3306/pidev_1";
    private final String login = "root";
    private final String pwd = "";
    private Connection cnx;
    public static MyConnection instance;

    private MyConnection() {
        try {
            cnx = DriverManager.getConnection(url, login, pwd);
            System.out.println("Connexion etablie !");
        } catch (SQLException e) {
            throw new IllegalStateException("Impossible de se connecter a la base de donnees : " + e.getMessage(), e);
        }
    }

    public Connection getCnx() {
        if (cnx == null) {
            throw new IllegalStateException("La connexion a la base de donnees n'est pas disponible.");
        }
        return cnx;
    }

    public static MyConnection getInstance() {
        if (instance == null) {
            instance = new MyConnection();
        }
        return instance;
    }
}
