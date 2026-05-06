package com.pidev.controllers;

import com.pidev.models.Magazine;
import com.pidev.services.ServiceMagazine;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Vue magazine patient — affichage en cartes (FlowPane).
 */
public class MagazinePatientController {

    @FXML private TextField tfSearch;
    @FXML private FlowPane  flowMagazines;

    private StackPane contentArea;

    private final ServiceMagazine serviceMagazine = new ServiceMagazine();
    private final ObservableList<Magazine> obsMagazines = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        chargerDonnees();
        if (tfSearch != null) {
            tfSearch.textProperty().addListener((obs, o, n) -> filtrerCartes(n));
        }
    }

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    private void chargerDonnees() {
        try {
            obsMagazines.setAll(serviceMagazine.afficherTout());
            afficherCartes(obsMagazines);
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void rechercherMagazine() {
        filtrerCartes(tfSearch != null ? tfSearch.getText() : "");
    }

    private void filtrerCartes(String texte) {
        if (texte == null || texte.isBlank()) {
            afficherCartes(obsMagazines);
            return;
        }
        String lower = texte.toLowerCase();
        ObservableList<Magazine> filtered = obsMagazines.filtered(m ->
                m.getTitre().toLowerCase().contains(lower) ||
                (m.getDescription() != null && m.getDescription().toLowerCase().contains(lower)));
        afficherCartes(filtered);
    }

    private void afficherCartes(Iterable<Magazine> magazines) {
        if (flowMagazines == null) return;
        flowMagazines.getChildren().clear();
        for (Magazine m : magazines) {
            flowMagazines.getChildren().add(creerCarte(m));
        }
    }

    private VBox creerCarte(Magazine magazine) {
        VBox card = new VBox(12);
        card.setPrefWidth(220);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color:#1e293b;" +
            "-fx-background-radius:12;" +
            "-fx-border-color:#334155;" +
            "-fx-border-width:1;" +
            "-fx-border-radius:12;" +
            "-fx-cursor:hand;"
        );

        // Icône + titre
        Label lblIcon = new Label("🗞");
        lblIcon.setStyle("-fx-font-size:28px;");

        Label lblTitre = new Label(magazine.getTitre());
        lblTitre.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#f1f5f9;");
        lblTitre.setWrapText(true);

        // Description courte
        String desc = magazine.getDescription() != null ? magazine.getDescription() : "";
        if (desc.length() > 80) desc = desc.substring(0, 80) + "...";
        Label lblDesc = new Label(desc);
        lblDesc.setStyle("-fx-text-fill:#94a3b8;-fx-font-size:12px;");
        lblDesc.setWrapText(true);
        VBox.setVgrow(lblDesc, Priority.ALWAYS);

        // Badge statut
        String statut = magazine.getStatut() != null ? magazine.getStatut() : "";
        Label lblStatut = new Label(statut);
        String badgeColor = "published".equals(statut) ? "#bbf7d0;-fx-text-fill:#166534"
                          : "archived".equals(statut)  ? "#fde68a;-fx-text-fill:#92400e"
                          : "#e2e8f0;-fx-text-fill:#475569";
        lblStatut.setStyle("-fx-background-color:" + badgeColor + ";-fx-background-radius:999;" +
                           "-fx-padding:3 10;-fx-font-size:11px;-fx-font-weight:bold;");

        // Bouton Voir
        Button btnVoir = new Button("Voir →");
        btnVoir.setMaxWidth(Double.MAX_VALUE);
        btnVoir.setStyle(
            "-fx-background-color:#6366f1;-fx-text-fill:white;-fx-font-weight:bold;" +
            "-fx-padding:8;-fx-background-radius:8;-fx-cursor:hand;-fx-font-size:13px;"
        );
        btnVoir.setOnAction(e -> ouvrirArticlesDuMagazine(magazine));

        card.getChildren().addAll(lblIcon, lblTitre, lblDesc, lblStatut, btnVoir);

        // Hover
        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color:#263348;" +
            "-fx-background-radius:12;" +
            "-fx-border-color:#6366f1;" +
            "-fx-border-width:1;" +
            "-fx-border-radius:12;" +
            "-fx-cursor:hand;" +
            "-fx-effect:dropshadow(gaussian,rgba(99,102,241,0.3),12,0,0,4);"
        ));
        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color:#1e293b;" +
            "-fx-background-radius:12;" +
            "-fx-border-color:#334155;" +
            "-fx-border-width:1;" +
            "-fx-border-radius:12;" +
            "-fx-cursor:hand;"
        ));

        return card;
    }

    private void ouvrirArticlesDuMagazine(Magazine magazine) {
        if (contentArea == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ArticlePatientView.fxml"));
            Node view = loader.load();
            ArticlePatientController ctrl = loader.getController();
            ctrl.setContentArea(contentArea);
            ctrl.filtrerParMagazine(magazine.getId());
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Impossible d'ouvrir les articles : " + e.getMessage()).showAndWait();
        }
    }
}
