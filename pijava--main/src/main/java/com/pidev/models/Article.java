package com.pidev.models;

import java.time.LocalDateTime;

/**
 * Article entity - belongs to a Magazine.
 */
public class Article {
    private int id;
    private String title;
    private String resume;
    private String auteur;
    private LocalDateTime datePub;
    private String statut;
    private String image;
    private int magazineId;
    private String magazineTitle;

    public Article() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getResume() { return resume; }
    public void setResume(String resume) { this.resume = resume; }

    public String getAuteur() { return auteur; }
    public void setAuteur(String auteur) { this.auteur = auteur; }

    public LocalDateTime getDatePub() { return datePub; }
    public void setDatePub(LocalDateTime datePub) { this.datePub = datePub; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public int getMagazineId() { return magazineId; }
    public void setMagazineId(int magazineId) { this.magazineId = magazineId; }

    public String getMagazineTitle() { return magazineTitle; }
    public void setMagazineTitle(String magazineTitle) { this.magazineTitle = magazineTitle; }
}
