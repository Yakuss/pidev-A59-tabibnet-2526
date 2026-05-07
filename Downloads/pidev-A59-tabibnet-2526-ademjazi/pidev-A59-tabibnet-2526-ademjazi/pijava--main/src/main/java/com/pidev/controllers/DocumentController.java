package com.pidev.controllers;

import com.pidev.models.Document;
import com.pidev.models.Rapport;
import com.pidev.models.Ordonnance;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.util.ResourceBundle;

public class DocumentController implements Initializable {

    @FXML private TextField nomField;
    @FXML private TextField typeField;
    @FXML private TextField tailleField;
    @FXML private TextArea descriptionArea;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label badgeLabel;
    @FXML private Label titleLabel;
    @FXML private Label statusLabel;

    private Document document;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    public void initData(Document document, boolean readOnly) {
        this.document = document;
        if (document != null) {
            nomField.setText(document.getNomFichier());
            typeField.setText(document.getType());
            tailleField.setText(document.getTaille());
            descriptionArea.setText(document.getDescription());
            
            if (readOnly) {
                nomField.setEditable(false);
                typeField.setEditable(false);
                tailleField.setEditable(false);
                descriptionArea.setEditable(false);
                saveButton.setDisable(true);
            }
        }
    }

    public void setItemsToLink(ObservableList<Rapport> rapports, ObservableList<Ordonnance> ordonnances) {
    }

    @FXML
    public void handleSave() {
    }

    @FXML
    public void handleCancel() {
    }
}
