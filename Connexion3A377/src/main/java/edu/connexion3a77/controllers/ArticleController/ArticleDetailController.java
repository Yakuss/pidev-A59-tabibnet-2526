package edu.connexion3a77.controllers.ArticleController;

import edu.connexion3a77.entities.Article;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ArticleDetailController {

    @FXML
    private Label labelTitre;
    @FXML
    private Label labelDate;
    @FXML
    private Label labelViews;
    @FXML
    private Text textSummary;
    @FXML
    private Text textFullContent;
    @FXML
    private Button retourButton;

    private Article article;

    public void setArticle(Article article) {
        this.article = article;
        if (article != null) {
            afficherArticle();
        }
    }

    private void afficherArticle() {
        if (labelTitre != null) {
            labelTitre.setText(article.getTitre());
        }
        if (labelDate != null) {
            labelDate.setText(
                    article.getDatePub() != null ? article.getDatePub().toLocalDate().toString() : "Date indisponible");
        }
        if (labelViews != null) {
            labelViews.setText(article.getViews() + " vues");
        }
        if (textSummary != null) {
            textSummary.setText(article.getSummary() != null ? article.getSummary() : "");
        }
        if (textFullContent != null) {
            textFullContent.setText(article.getResume() != null ? article.getResume() : "");
        }
    }

    @FXML
    private void retourBibliotheque(ActionEvent event) {
        if (event != null && event.getSource() instanceof Node sourceNode) {
            Stage stage = (Stage) sourceNode.getScene().getWindow();
            stage.close();
        }
    }
}
