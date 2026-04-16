package edu.connexion3a77.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Magazine {

    private int id;
    private String titre;
    private String description;
    private String image;
    private LocalDateTime dateCreate;
    private String statut; // draft, published, archived
    private List<Article> articles;
    private String pdfFile;

    // Constructeur pour ajout via formulaire
    public Magazine(String titre, String description, String statut, String pdfFile) {
        this.titre = titre;
        this.description = description;
        this.statut = statut;
        this.pdfFile = pdfFile;
        this.dateCreate = LocalDateTime.now();
        this.articles = new ArrayList<>();
    }

    // Constructeur complet (depuis base de données)
    public Magazine(int id, String titre, String description, String image,
            LocalDateTime dateCreate, String statut, String pdfFile) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.image = image;
        this.dateCreate = dateCreate;
        this.statut = statut;
        this.pdfFile = pdfFile;
        this.articles = new ArrayList<>();
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public LocalDateTime getDateCreate() {
        return dateCreate;
    }

    public void setDateCreate(LocalDateTime dateCreate) {
        this.dateCreate = dateCreate;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public List<Article> getArticles() {
        return articles;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles != null ? articles : new ArrayList<>();
    }

    public String getPdfFile() {
        return pdfFile;
    }

    public void setPdfFile(String pdfFile) {
        this.pdfFile = pdfFile;
    }

    @Override
    public String toString() {
        return titre;
    }
}