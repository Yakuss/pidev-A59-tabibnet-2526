package com.pidev.controllers;

import com.pidev.models.Rapport;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.net.URL;
import java.util.ResourceBundle;

public class RapportController implements Initializable {

    @FXML private TextField consultationReasonTF;
    @FXML private TextField diagnosisTF;
    @FXML private TextArea observationsTA;
    @FXML private TextArea recommendationsTA;
    @FXML private TextArea treatmentsTA;
    @FXML private ComboBox<?> rendezVousCB;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Rapport rapport;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    public void initData(Rapport rapport) {
        this.rapport = rapport;
        if (rapport != null) {
            if (consultationReasonTF != null && rapport.getConsultationReason() != null) {
                consultationReasonTF.setText(rapport.getConsultationReason());
            }
            if (diagnosisTF != null && rapport.getDiagnosis() != null) {
                diagnosisTF.setText(rapport.getDiagnosis());
            }
            if (observationsTA != null && rapport.getObservations() != null) {
                observationsTA.setText(rapport.getObservations());
            }
            if (recommendationsTA != null && rapport.getRecommendations() != null) {
                recommendationsTA.setText(rapport.getRecommendations());
            }
            if (treatmentsTA != null && rapport.getTreatments() != null) {
                treatmentsTA.setText(rapport.getTreatments());
            }
        }
    }

    @FXML
    public void ajouterRapport() {
    }

    @FXML
    public void handleCancel() {
    }
}
