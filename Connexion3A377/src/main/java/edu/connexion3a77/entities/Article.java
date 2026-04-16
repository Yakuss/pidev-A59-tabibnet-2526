package edu.connexion3a77.entities;

import java.time.LocalDateTime;

public class Article {

    private int id;
    private String titre;
    private String resume;
    private String auteur;
    private LocalDateTime datePub;
    private String summary;
    private String statut;
    private String image;
    private int views = 0;
    private Magazine magazine;

    // ==================== CONSTRUCTEUR PRINCIPAL (utilisé dans MainController) ====================
    public Article(String titre, String resume, String auteur, LocalDateTime datePub,
                   String summary, String statut, String image) {
        this.titre = titre;
        this.resume = resume;
        this.auteur = auteur;
        this.datePub = datePub != null ? datePub : LocalDateTime.now();
        this.summary = summary;
        this.statut = statut;
        this.image = image;
        this.views = 0;
    }

    // ==================== CONSTRUCTEUR COMPLET (utilisé par ServiceArticle) ====================
    public Article(int id, String titre, String resume, String auteur, LocalDateTime datePub,
                   String summary, String statut, String image, int views) {
        this.id = id;
        this.titre = titre;
        this.resume = resume;
        this.auteur = auteur;
        this.datePub = datePub;
        this.summary = summary;
        this.statut = statut;
        this.image = image;
        this.views = views;
    }

    public void incrementViews() {
        this.views++;
    }

    // ==================== GETTERS & SETTERS ====================
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getResume() { return resume; }
    public void setResume(String resume) { this.resume = resume; }

    public String getAuteur() { return auteur; }
    public void setAuteur(String auteur) { this.auteur = auteur; }

    public LocalDateTime getDatePub() { return datePub; }
    public void setDatePub(LocalDateTime datePub) { this.datePub = datePub; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }

    public Magazine getMagazine() { return magazine; }
    public void setMagazine(Magazine magazine) { this.magazine = magazine; }

    @Override
    public String toString() {
        return "Article{id=" + id + ", titre='" + titre + "', views=" + views + '}';
    }
}