package com.pidev.models;

import java.time.LocalDateTime;
import java.util.Date;

public class Document {
    private int id;
    private String nomFichier;
    private String cheminFichier;
    private String type;
    private String taille;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int nbRapports;
    private int nbOrdonnances;
    
    // Compatibility with existing code
    private int medecinId;
    private int patientId;

    public Document() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Date versions for legacy code compatibility
    public Date getDateCreation() { 
        return createdAt != null ? java.sql.Timestamp.valueOf(createdAt) : null; 
    }
    public void setDateCreation(Date date) { 
        if (date != null) this.createdAt = new java.sql.Timestamp(date.getTime()).toLocalDateTime(); 
    }
    
    public Date getDateModification() { 
        return updatedAt != null ? java.sql.Timestamp.valueOf(updatedAt) : null; 
    }
    public void setDateModification(Date date) { 
        if (date != null) this.updatedAt = new java.sql.Timestamp(date.getTime()).toLocalDateTime(); 
    }

    public int getNbRapports() { return nbRapports; }
    public void setNbRapports(int nbRapports) { this.nbRapports = nbRapports; }

    public int getNbOrdonnances() { return nbOrdonnances; }
    public void setNbOrdonnances(int nbOrdonnances) { this.nbOrdonnances = nbOrdonnances; }

    public int getMedecinId() { return medecinId; }
    public void setMedecinId(int medecinId) { this.medecinId = medecinId; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    
    // Compatibility aliases
    public String getNom() { return nomFichier; }
    public void setNom(String nom) { this.nomFichier = nom; }
    public String getChemin() { return cheminFichier; }
    public void setChemin(String chemin) { this.cheminFichier = chemin; }
}
