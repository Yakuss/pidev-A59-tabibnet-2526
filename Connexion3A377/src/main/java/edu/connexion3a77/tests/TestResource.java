package edu.connexion3a77.tests;

public class TestResource {
    public static void main(String[] args) {
        System.out.println("Resource URL: " + TestResource.class.getResource("/Views/frontoffice/articlePatient.fxml"));
        System.out.println("Other Resource: " + TestResource.class.getResource("/Views/frontoffice/magazinePatient.fxml"));
    }
}
