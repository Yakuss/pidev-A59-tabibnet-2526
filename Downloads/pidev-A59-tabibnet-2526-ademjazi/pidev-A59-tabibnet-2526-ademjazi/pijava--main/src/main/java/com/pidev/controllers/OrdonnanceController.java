package com.pidev.controllers;

import com.pidev.models.Ordonnance;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.util.ResourceBundle;

public class OrdonnanceController implements Initializable {

    @FXML private DatePicker dateOrdonnanceDP;
    @FXML private TextField diagnosisTF;
    @FXML private TextArea medicamentTF;
    @FXML private TextArea posologieTF;
    @FXML private TextArea instructionsTA;
    @FXML private TextArea notesTA;
    @FXML private ComboBox<?> rendezVousCB;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Ordonnance ordonnance;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    public void initData(Ordonnance ordonnance) {
        this.ordonnance = ordonnance;
        if (ordonnance != null) {
            if (ordonnance.getDateOrdonnance() != null) {
                dateOrdonnanceDP.setValue(ordonnance.getDateOrdonnance().toLocalDate());
            }
            diagnosisTF.setText(ordonnance.getDiagnosis());
            medicamentTF.setText(ordonnance.getMedicament());
            posologieTF.setText(ordonnance.getPosologie());
            instructionsTA.setText(ordonnance.getInstructions());
            notesTA.setText(ordonnance.getNotes());
        }
    }

    @FXML
    public void ajouterOrdonnance() {
    }

    @FXML
    public void handleCancel() {
    }
}
