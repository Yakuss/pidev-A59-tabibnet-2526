package edu.connexion3a77.tests;

import edu.connexion3a77.entities.Personne;
import edu.connexion3a77.services.PersonneService;
import edu.connexion3a77.tools.MyConnection;

public class MainClass {

    public static void main(String[] args) {
        //MyConnection mc = new MyConnection();
        Personne p = new Personne("Tounsi","Abdelhamid");
        PersonneService ps = new PersonneService();
       // ps.addEntity2(p);
        try {
            System.out.println(ps.findAll());
        } catch (java.sql.SQLException e) {
            System.out.println(e.getMessage());
        }

        MyConnection m1 = MyConnection.getInstance();
        MyConnection m2 = MyConnection.getInstance();
        System.out.println(m1.hashCode() +" - "+m2.hashCode());
    }
}   
