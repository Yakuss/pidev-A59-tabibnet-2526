package com.pidev.controllers;

import com.pidev.models.Rapport;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class RapportDetailsController {

    @FXML private Label idLabel;
    @FXML private Label dateLabel;
    @FXML private Label patientLabel;
    @FXML private Label medecinLabel;
    @FXML private Label motifLabel;
    @FXML private Label diagnosticLabel;
    @FXML private TextArea observationsTA;
    @FXML private TextArea recommendationsTA;
    @FXML private TextArea treatmentsTA;

    public void setRapport(Rapport r) {
        if (r != null) {
            idLabel.setText("Rapport #" + r.getId());
            dateLabel.setText(r.getCreatedAt() != null ? r.getCreatedAt().toString() : "N/A");
            motifLabel.setText(r.getConsultationReason());
            diagnosticLabel.setText(r.getDiagnosis());
            observationsTA.setText(r.getObservations());
            recommendationsTA.setText(r.getRecommendations());
            treatmentsTA.setText(r.getTreatments());
            // patientLabel and medecinLabel can be set if names are available
        }
    }

    @FXML
    private void handleBack() {
        if (MainUserController.getInstance() != null) {
            MainUserController.getInstance().showOrdonnanceRapport();
        }
    }
}
