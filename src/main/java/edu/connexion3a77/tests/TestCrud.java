package edu.connexion3a77.tests;

import edu.connexion3a77.entities.Personne;
import edu.connexion3a77.services.PersonneService;

public class TestCrud {

    public static void main(String[] args) {
        System.out.println("--- Début des tests CRUD ---");

        try {
            PersonneService personneService = new PersonneService();
            
            // Test CREATE
            System.out.println("\n1. Test Ajout (CREATE):");
            Personne p1 = new Personne("TestNom", "TestPrenom");
            personneService.add(p1);
            
            // Test READ
            System.out.println("\n2. Test Lecture (READ):");
            personneService.findAll().forEach(personne -> 
                System.out.println("Personne trouvée : " + personne.getNom() + " " + personne.getPrenom())
            );

        } catch (Exception e) {
            System.err.println("❌ Erreur pendant le test CRUD ! Avez-vous importé la base de données (database_schema.sql) et lancé MySQL ?");
            e.printStackTrace();
        }
        
        System.out.println("\n--- Fin des tests ---");
    }
}
