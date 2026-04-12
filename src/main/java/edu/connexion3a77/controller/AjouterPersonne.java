package edu.connexion3a77.controller;

import edu.connexion3a77.entities.Personne;
import edu.connexion3a77.services.PersonneService;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class AjouterPersonne {
    @javafx.fxml.FXML
    private TextField NomTF;
    @javafx.fxml.FXML
    private TextField AgeTF;
    @javafx.fxml.FXML
    private TextField PrénomFT;

    @javafx.fxml.FXML
    public void AjouterPersonne(ActionEvent actionEvent) {
        Personne p = new Personne();
        p.setPrenom(PrenomTF.getText());
        p.setNom(NomTF.getText());
        p.setAge(ageTF.getText());

        PersonneService ps = new PersonneService();

        try {
            ps.ajouter(p);
        }catch (SQLException) (
             Alert a = new AlertAlert()
        )

    }
}
