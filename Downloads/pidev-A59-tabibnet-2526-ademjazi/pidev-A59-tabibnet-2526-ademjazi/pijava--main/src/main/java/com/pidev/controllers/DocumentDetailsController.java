package com.pidev.controllers;

import com.pidev.models.Document;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class DocumentDetailsController {

    @FXML private TextField nomField;
    @FXML private TextField typeField;
    @FXML private TextField dateField;
    @FXML private TextArea descriptionArea;

    private Document currentDocument;

    public void setDocument(Document document) {
        this.currentDocument = document;
        if (document != null) {
            nomField.setText(document.getNomFichier());
            typeField.setText(document.getType());
            dateField.setText(document.getDateCreation() != null ? document.getDateCreation().toString() : "N/A");
            descriptionArea.setText(document.getDescription() != null ? document.getDescription() : "Aucune description.");
        }
    }

    @FXML
    private void handleClose() {
        if (MainUserController.getInstance() != null) {
            MainUserController.getInstance().showDossierMedical();
        }
    }
}
