package com.pidev.controllers;

import com.pidev.models.Ordonnance;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class OrdonnanceDetailsController {

    @FXML private TextField dateField;
    @FXML private TextField diagnosisField;
    @FXML private TextArea medicamentsArea;
    @FXML private TextArea posologieArea;
    @FXML private TextArea notesArea;

    private Ordonnance currentOrdonnance;

    public void setOrdonnance(Ordonnance ordonnance) {
        this.currentOrdonnance = ordonnance;
        if (ordonnance != null) {
            dateField.setText(ordonnance.getDateOrdonnance() != null ? ordonnance.getDateOrdonnance().toString() : "N/A");
            diagnosisField.setText(ordonnance.getDiagnosis());
            medicamentsArea.setText(ordonnance.getMedicament());
            posologieArea.setText(ordonnance.getPosologie());
            notesArea.setText(ordonnance.getNotes());
        }
    }

    @FXML
    private void handleClose() {
        if (MainUserController.getInstance() != null) {
            MainUserController.getInstance().showOrdonnanceRapport();
        }
    }
}
