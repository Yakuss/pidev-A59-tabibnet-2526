package edu.connexion3a77.entities;

public class Medecin extends Personne {

    private String specialite;

    public Medecin() {
        super();
    }

    public Medecin(String nom, String prenom, String specialite) {
        super(nom, prenom);
        this.specialite = specialite;
    }

    public String getSpecialite() {
        return specialite;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }
}
