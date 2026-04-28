package edu.connexion3a77.entities;

import java.util.Date;

public class Document {
    private int id;
    private String nomFichier;
    private String cheminFichier;
    private String type;
    private String taille;
    private String description;
    private Date dateCreation;
    private Date dateModification;
    private int nbRapports;
    private int nbOrdonnances;

    public Document() {
        this.dateCreation = new Date();
        this.dateModification = new Date();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNomFichier() { return nomFichier; }
    public void setNomFichier(String nomFichier) { this.nomFichier = nomFichier; }

    public String getCheminFichier() { return cheminFichier; }
    public void setCheminFichier(String cheminFichier) { this.cheminFichier = cheminFichier; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTaille() { return taille; }
    public void setTaille(String taille) { this.taille = taille; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getDateCreation() { return dateCreation; }
    public void setDateCreation(Date dateCreation) { this.dateCreation = dateCreation; }

    public Date getDateModification() { return dateModification; }
    public void setDateModification(Date dateModification) { this.dateModification = dateModification; }

    public int getNbRapports() { return nbRapports; }
    public void setNbRapports(int nbRapports) { this.nbRapports = nbRapports; }

    public int getNbOrdonnances() { return nbOrdonnances; }
    public void setNbOrdonnances(int nbOrdonnances) { this.nbOrdonnances = nbOrdonnances; }
}
