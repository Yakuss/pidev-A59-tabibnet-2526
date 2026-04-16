package edu.connexion3a77.controllers;

import edu.connexion3a77.entities.Article;
import edu.connexion3a77.entities.Magazine;
import edu.connexion3a77.services.ServiceArticle;
import edu.connexion3a77.services.ServiceMagazine;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.Node;
import javafx.util.StringConverter;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.collections.transformation.FilteredList;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public class MainController {

    // ====================== MAGAZINE ======================
    @FXML
    private TableView<Magazine> tableMagazines;
    @FXML
    private TableColumn<Magazine, Integer> colIdMag;
    @FXML
    private TableColumn<Magazine, String> colTitreMag;
    @FXML
    private TableColumn<Magazine, String> colDescriptionMag;
    @FXML
    private TableColumn<Magazine, LocalDateTime> colDateCreateMag;
    @FXML
    private TableColumn<Magazine, String> colStatutMag;
    @FXML
    private TableColumn<Magazine, Void> colActionsMag;

    @FXML
    private TextField tfTitreMag;
    @FXML
    private TextArea taDescriptionMag;
    @FXML
    private ComboBox<String> cbStatutMag;

    // ====================== ARTICLE ======================
    @FXML
    private TableView<Article> tableArticles;
    @FXML
    private TableColumn<Article, Integer> colIdArt;
    @FXML
    private TableColumn<Article, String> colTitreArt;
    @FXML
    private TableColumn<Article, String> colResumeArt;
    @FXML
    private TableColumn<Article, String> colAuteurArt;
    @FXML
    private TableColumn<Article, String> colMagazineArt;
    @FXML
    private TableColumn<Article, LocalDateTime> colDatePubArt;
    @FXML
    private TableColumn<Article, String> colStatutArt;
    @FXML
    private TableColumn<Article, Integer> colViewsArt;
    @FXML
    private TableColumn<Article, Void> colActionsArt;

    @FXML
    private TextField tfTitreArt;
    @FXML
    private TextArea taResumeArt;
    @FXML
    private TextField tfAuteurArt;
    @FXML
    private DatePicker dpDatePubArt;
    @FXML
    private TextArea taSummaryArt;
    @FXML
    private ComboBox<String> cbStatutArt;
    @FXML
    private ComboBox<Magazine> cbMagazineArt;

    // ====================== SERVICES ======================
    private final ServiceMagazine serviceMagazine = new ServiceMagazine();
    private final ServiceArticle serviceArticle = new ServiceArticle();

    private final ObservableList<Magazine> obsMagazines = FXCollections.observableArrayList();
    private final ObservableList<Article> obsArticles = FXCollections.observableArrayList();

    private Magazine selectedMagazine = null;
    private Article selectedArticle = null;

    // ====================== INITIALISATION ======================
    @FXML
    private void initialize() {
        configurerTables();
        chargerDonnees();

        if (cbStatutMag != null) {
            cbStatutMag.getItems().addAll("draft", "published", "archived");
        }
        if (cbStatutArt != null) {
            cbStatutArt.getItems().addAll("draft", "published");
        }
    }

    private void configurerTables() {
        if (colIdMag != null) {
            colIdMag.setCellValueFactory(
                    c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()).asObject());
        }
        if (colTitreMag != null) {
            colTitreMag
                    .setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTitre()));
        }
        if (colDescriptionMag != null) {
            colDescriptionMag.setCellValueFactory(
                    c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDescription()));
        }
        if (colDateCreateMag != null) {
            colDateCreateMag.setCellValueFactory(
                    c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getDateCreate()));
        }
        if (colStatutMag != null) {
            colStatutMag
                    .setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatut()));
            colStatutMag.setCellFactory(column -> new TableCell<Magazine, String>() {
                private final Label badge = new Label();

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        badge.setText(item.substring(0, 1).toUpperCase() + item.substring(1));
                        badge.getStyleClass().setAll("status-badge", item.toLowerCase());
                        setGraphic(badge);
                    }
                }
            });
        }
        if (colActionsMag != null) {
            setupMagazineActionsColumn();
        }

        if (colIdArt != null) {
            colIdArt.setCellValueFactory(
                    c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()).asObject());
        }
        if (colTitreArt != null) {
            colTitreArt
                    .setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTitre()));
        }
        if (colResumeArt != null) {
            colResumeArt
                    .setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getResume()));
        }
        if (colAuteurArt != null) {
            colAuteurArt
                    .setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getAuteur()));
        }
        if (colMagazineArt != null) {
            colMagazineArt.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                    c.getValue().getMagazine() != null ? c.getValue().getMagazine().getTitre() : ""));
        }
        if (colDatePubArt != null) {
            colDatePubArt.setCellValueFactory(
                    c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getDatePub()));
        }
        if (colStatutArt != null) {
            colStatutArt
                    .setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatut()));
            colStatutArt.setCellFactory(column -> new TableCell<Article, String>() {

                private final Label badge = new Label();

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        badge.setText(item.substring(0, 1).toUpperCase() + item.substring(1));
                        badge.getStyleClass().setAll("status-badge", item.toLowerCase());
                        setGraphic(badge);
                    }
                }

            });
        }
        if (colViewsArt != null) {
            colViewsArt.setCellValueFactory(
                    c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getViews()).asObject());
        }
        if (colActionsArt != null) {
            setupArticleActionsColumn();
        }

        if (tableMagazines != null) {
            tableMagazines.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
                selectedMagazine = newVal;
                if (newVal != null) {
                    remplirFormulaireMagazine(newVal);
                }
            });
        }

        if (tableArticles != null) {
            tableArticles.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
                selectedArticle = newVal;
                if (newVal != null) {
                    remplirFormulaireArticle(newVal);
                }
            });
        }
    }

    private void setupMagazineActionsColumn() {
        colActionsMag.setCellFactory(column -> new TableCell<Magazine, Void>() {
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");
            private final HBox actionBox = new HBox(8, editButton, deleteButton);

            {
                editButton.getStyleClass().add("action-edit-button");
                deleteButton.getStyleClass().add("action-delete-button");
                editButton.setOnAction(evt -> {
                    Magazine magazine = getTableView().getItems().get(getIndex());
                    editMagazine(magazine);
                });
                deleteButton.setOnAction(evt -> {
                    Magazine magazine = getTableView().getItems().get(getIndex());
                    deleteMagazine(magazine);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionBox);
            }
        });
    }

    private void setupArticleActionsColumn() {
        colActionsArt.setCellFactory(column -> new TableCell<Article, Void>() {
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");
            private final HBox actionBox = new HBox(8, editButton, deleteButton);

            {
                editButton.getStyleClass().add("action-edit-button");
                deleteButton.getStyleClass().add("action-delete-button");
                editButton.setOnAction(evt -> {
                    Article article = getTableView().getItems().get(getIndex());
                    if (article != null) {
                        showArticleEditDialog(article);
                    }
                });
                deleteButton.setOnAction(evt -> {
                    Article article = getTableView().getItems().get(getIndex());
                    if (article != null) {
                        selectedArticle = article;
                        supprimerArticle();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionBox);
            }
        });
    }

    private void showArticleEditDialog(Article article) {
        Stage dialog = new Stage();
        dialog.setTitle("Modifier l'article");
        dialog.initModality(Modality.APPLICATION_MODAL);

        Label headingLabel = new Label("Modifier l'article");
        headingLabel.getStyleClass().add("form-header-title");
        Label descriptionLabel = new Label("Modifiez les informations de l'article et enregistrez.");
        descriptionLabel.getStyleClass().add("form-subtitle");

        TextField titreField = new TextField(article.getTitre());
        titreField.setPromptText("Entrez le titre de l'article");
        titreField.getStyleClass().add("form-field");

        TextField auteurField = new TextField(article.getAuteur());
        auteurField.setPromptText("Entrez le nom de l'auteur");
        auteurField.getStyleClass().add("form-field");

        DatePicker datePicker = new DatePicker(
                article.getDatePub() != null ? article.getDatePub().toLocalDate() : null);
        datePicker.getStyleClass().add("form-combobox");

        ComboBox<String> statutBox = new ComboBox<>();
        statutBox.getItems().addAll("draft", "published");
        statutBox.setValue(article.getStatut());
        statutBox.getStyleClass().add("form-combobox");

        ComboBox<Magazine> magazineBox = new ComboBox<>();
        magazineBox.getItems().setAll(obsMagazines);
        configureMagazineComboBox(magazineBox);
        magazineBox.setValue(article.getMagazine());
        magazineBox.getStyleClass().add("form-combobox");

        TextArea summaryArea = new TextArea(article.getSummary());
        summaryArea.setPromptText("Enter a brief summary of your article...");
        summaryArea.setPrefRowCount(3);
        summaryArea.getStyleClass().add("form-textarea");

        TextArea resumeArea = new TextArea(article.getResume());
        resumeArea.setPromptText("Write your article content here...");
        resumeArea.setPrefRowCount(6);
        resumeArea.getStyleClass().add("form-textarea");

        GridPane grid = new GridPane();
        grid.setHgap(24);
        grid.setVgap(16);
        grid.getColumnConstraints().addAll(
                new javafx.scene.layout.ColumnConstraints(170),
                new javafx.scene.layout.ColumnConstraints(510));

        grid.add(new Label("Titre de l'article"), 0, 0);
        grid.add(titreField, 1, 0);
        grid.add(new Label("Auteur"), 0, 1);
        grid.add(auteurField, 1, 1);
        grid.add(new Label("Statut"), 0, 2);
        grid.add(statutBox, 1, 2);
        grid.add(new Label("Magazine parent"), 0, 3);
        grid.add(magazineBox, 1, 3);
        grid.add(new Label("Summary"), 0, 4);
        grid.add(summaryArea, 1, 4);
        grid.add(new Label("Main Content (Resume)"), 0, 5);
        grid.add(resumeArea, 1, 5);
        grid.add(new Label("Date de publication"), 0, 6);
        grid.add(datePicker, 1, 6);

        Button saveButton = new Button("Enregistrer");
        saveButton.getStyleClass().addAll("primary-button", "dialog-button");
        Button cancelButton = new Button("Annuler");
        cancelButton.getStyleClass().addAll("secondary-button", "dialog-button");
        HBox buttonRow = new HBox(12, cancelButton, saveButton);
        buttonRow.setStyle("-fx-alignment: center-right;");

        VBox root = new VBox(18, headingLabel, descriptionLabel, grid, buttonRow);
        root.getStyleClass().add("form-dialog");
        root.setPadding(new Insets(24));
        root.setPrefWidth(780);

        saveButton.setOnAction(evt -> {
            String titre = titreField.getText().trim();
            String resume = resumeArea.getText().trim();
            String summary = summaryArea.getText().trim();
            String auteur = auteurField.getText().trim();
            String statut = statutBox.getValue();
            Magazine selectedMagazine = magazineBox.getValue();

            if (titre.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le titre est obligatoire.");
                return;
            }
            if (resume.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le contenu est obligatoire.");
                return;
            }
            if (summary.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le résumé est obligatoire.");
                return;
            }
            if (auteur.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "L'auteur est obligatoire.");
                return;
            }
            if (statut == null || statut.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le statut est obligatoire.");
                return;
            }

            article.setTitre(titre);
            article.setResume(resume);
            article.setSummary(summary);
            article.setAuteur(auteur);
            article.setStatut(statut);
            article.setDatePub(
                    datePicker.getValue() != null ? datePicker.getValue().atStartOfDay() : article.getDatePub());
            article.setMagazine(selectedMagazine);

            try {
                serviceArticle.modifier(article);
                if (tableArticles != null) {
                    tableArticles.refresh();
                }
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Article modifié avec succès !");
                dialog.close();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la modification : " + e.getMessage());
            }
        });

        cancelButton.setOnAction(evt -> dialog.close());

        Scene scene = new Scene(root);
        URL css = getClass().getResource("/styles/application.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void configureMagazineComboBox(ComboBox<Magazine> comboBox) {
        if (comboBox == null) {
            return;
        }
        comboBox.setCellFactory(listView -> new ListCell<Magazine>() {
            @Override
            protected void updateItem(Magazine item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitre());
            }
        });
        comboBox.setConverter(new StringConverter<Magazine>() {
            @Override
            public String toString(Magazine magazine) {
                return magazine == null ? null : magazine.getTitre();
            }

            @Override
            public Magazine fromString(String string) {
                if (string == null) {
                    return null;
                }
                for (Magazine magazine : comboBox.getItems()) {
                    if (string.equals(magazine.getTitre())) {
                        return magazine;
                    }
                }
                return null;
            }
        });
    }

    private void chargerDonnees() {
        try {
            obsMagazines.clear();
            obsMagazines.addAll(serviceMagazine.afficherTout());
            if (tableMagazines != null) {
                tableMagazines.setItems(obsMagazines);
            }

            obsArticles.clear();
            obsArticles.addAll(serviceArticle.afficherTout());
            if (tableArticles != null) {
                tableArticles.setItems(obsArticles);
            }

            if (cbMagazineArt != null) {
                cbMagazineArt.getItems().clear();
                cbMagazineArt.getItems().addAll(obsMagazines);
                configureMagazineComboBox(cbMagazineArt);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les données : " + e.getMessage());
        }
    }

    // ====================== CRUD MAGAZINE ======================
    @FXML
    private void ajouterMagazine() {
        if (isMagazineFormAvailable()) {
            if (!validerMagazine()) {
                return;
            }
            try {
                Magazine m = new Magazine(tfTitreMag.getText().trim(), taDescriptionMag.getText().trim(),
                        cbStatutMag.getValue(), null);
                serviceMagazine.ajouter(m);
                obsMagazines.add(m);
                clearFormMagazine();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Magazine ajouté !");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        } else {
            showMagazineAddDialog();
        }
    }

    @FXML
    private void modifierMagazine() {
        if (selectedMagazine == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez un magazine à modifier !");
            return;
        }
        editMagazine(selectedMagazine);
    }

    @FXML
    private void supprimerMagazine() {
        if (selectedMagazine == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez un magazine à supprimer !");
            return;
        }
        deleteMagazine(selectedMagazine);
    }

    // ====================== CRUD ARTICLE ======================
    @FXML
    private void ajouterArticle() {
        if (isArticleFormAvailable()) {
            if (!validerArticle()) {
                return;
            }

            try {
                Article article = new Article(
                        tfTitreArt.getText().trim(),
                        taResumeArt.getText().trim(),
                        tfAuteurArt.getText().trim(),
                        dpDatePubArt.getValue() != null ? dpDatePubArt.getValue().atStartOfDay() : LocalDateTime.now(),
                        taSummaryArt.getText().trim(),
                        cbStatutArt.getValue(),
                        null // image (pour l'instant)
                );

                if (cbMagazineArt.getValue() != null) {
                    article.setMagazine(cbMagazineArt.getValue());
                }

                serviceArticle.ajouter(article);
                obsArticles.add(article);
                clearFormArticle();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Article ajouté avec succès !");

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec d'ajout : " + e.getMessage());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur inattendue", e.getMessage());
            }
        } else {
            showArticleAddDialog();
        }
    }

    @FXML
    private void modifierArticle() {
        if (tfTitreArt == null || taResumeArt == null || tfAuteurArt == null || dpDatePubArt == null
                || taSummaryArt == null || cbStatutArt == null || cbMagazineArt == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le formulaire d'édition d'article n'est pas disponible ici.");
            return;
        }
        if (selectedArticle == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez un article !");
            return;
        }
        if (!validerArticle())
            return;

        try {
            selectedArticle.setTitre(tfTitreArt.getText().trim());
            selectedArticle.setResume(taResumeArt.getText().trim());
            selectedArticle.setAuteur(tfAuteurArt.getText().trim());
            selectedArticle.setDatePub(dpDatePubArt.getValue() != null ? dpDatePubArt.getValue().atStartOfDay() : null);
            selectedArticle.setSummary(taSummaryArt.getText().trim());
            selectedArticle.setStatut(cbStatutArt.getValue());

            if (cbMagazineArt.getValue() != null) {
                selectedArticle.setMagazine(cbMagazineArt.getValue());
            }

            serviceArticle.modifier(selectedArticle);

            if (tableArticles != null) {
                int index = obsArticles.indexOf(selectedArticle);
                if (index >= 0) {
                    obsArticles.set(index, selectedArticle);
                }
                tableArticles.refresh();
                tableArticles.getSelectionModel().clearSelection();
            }

            clearFormArticle();
            selectedArticle = null;
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Article modifié !");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    private void supprimerArticle() {
        if (selectedArticle == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez un article !");
            return;
        }
        try {
            serviceArticle.supprimer(selectedArticle.getId());
            obsArticles.remove(selectedArticle);
            clearFormArticle();
            selectedArticle = null;
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Article supprimé !");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    // ====================== VALIDATIONS ======================
    private boolean validerMagazine() {
        if (tfTitreMag == null || taDescriptionMag == null || cbStatutMag == null) {
            return true;
        }
        return validerMagazine(tfTitreMag.getText().trim(), taDescriptionMag.getText().trim(), cbStatutMag.getValue());
    }

    private boolean validerMagazine(String titre, String description, String statut) {
        if (titre.isEmpty() || titre.length() < 2 || titre.length() > 255) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Titre invalide (2-255 caractères)");
            return false;
        }
        if (description.trim().length() < 5) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Description minimum 5 caractères");
            return false;
        }
        if (statut == null || statut.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Choisissez un statut");
            return false;
        }
        return true;
    }

    private boolean isMagazineFormAvailable() {
        return tfTitreMag != null && taDescriptionMag != null && cbStatutMag != null;
    }

    private boolean isArticleFormAvailable() {
        return tfTitreArt != null && taResumeArt != null && tfAuteurArt != null
                && dpDatePubArt != null && taSummaryArt != null && cbStatutArt != null;
    }

    private boolean validerArticle() {
        if (tfTitreArt == null || taResumeArt == null || tfAuteurArt == null || cbStatutArt == null) {
            return true;
        }
        return validerArticle(tfTitreArt.getText().trim(), taResumeArt.getText().trim(),
                tfAuteurArt.getText().trim(), cbStatutArt.getValue());
    }

    private boolean validerArticle(String titre, String resume, String auteur, String statut) {
        if (titre.isEmpty() || titre.length() < 2 || titre.length() > 255) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Titre invalide (2-255 caractères)");
            return false;
        }
        if (resume.trim().length() < 5) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Résumé minimum 5 caractères");
            return false;
        }
        if (auteur.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "L'auteur est obligatoire");
            return false;
        }
        if (statut == null || statut.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Choisissez un statut");
            return false;
        }
        return true;
    }

    private void showMagazineAddDialog() {
        Stage dialog = new Stage(StageStyle.UNDECORATED);
        dialog.initModality(Modality.APPLICATION_MODAL);

        Label header = new Label("Nouveau Magazine");
        header.getStyleClass().add("form-header-title");

        Button closeBtn = new Button("✕");
        closeBtn.getStyleClass().add("modal-close-button");
        closeBtn.setOnAction(evt -> dialog.close());

        HBox headerBar = new HBox(header, new Region(), closeBtn);
        headerBar.getStyleClass().add("modal-header");
        HBox.setHgrow(headerBar.getChildren().get(1), Priority.ALWAYS);

        Label subtitle = new Label("Remplissez les informations pour créer un nouveau magazine");
        subtitle.getStyleClass().add("form-subtitle");
        subtitle.setWrapText(true);

        TextField titleField = new TextField();
        titleField.setPromptText("Entrez le titre du magazine (3-255 caractères)");
        titleField.getStyleClass().add("form-field");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Entrez une description (minimum 10 caractères)");
        descriptionArea.setPrefRowCount(6);
        descriptionArea.getStyleClass().add("form-textarea");

        TextField imageField = new TextField();
        imageField.setPromptText("Sélectionnez une image...");
        imageField.getStyleClass().add("form-field");
        imageField.setEditable(false);

        Button browseButton = new Button("Parcourir");
        browseButton.getStyleClass().addAll("secondary-button", "dialog-button");
        browseButton.setOnAction(evt -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choisir une image");
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                    new FileChooser.ExtensionFilter("Tous les fichiers", "*.*"));
            File file = chooser.showOpenDialog(dialog);
            if (file != null) {
                imageField.setText(file.toURI().toString());
            }
        });

        HBox imageRow = new HBox(10, imageField, browseButton);
        imageRow.setAlignment(Pos.CENTER_LEFT);
        imageRow.setFillHeight(false);
        imageRow.setPrefWidth(520);

        TextField pdfField = new TextField();
        pdfField.setPromptText("Sélectionnez un PDF ou collez une URL...");
        pdfField.getStyleClass().add("form-field");
        pdfField.setEditable(false);

        Button pdfBrowseButton = new Button("Parcourir");
        pdfBrowseButton.getStyleClass().addAll("secondary-button", "dialog-button");
        pdfBrowseButton.setOnAction(evt -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choisir un PDF");
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("PDF", "*.pdf"),
                    new FileChooser.ExtensionFilter("Tous les fichiers", "*.*"));
            File file = chooser.showOpenDialog(dialog);
            if (file != null) {
                pdfField.setText(file.toURI().toString());
            }
        });

        HBox pdfRow = new HBox(10, pdfField, pdfBrowseButton);
        pdfRow.setAlignment(Pos.CENTER_LEFT);
        pdfRow.setFillHeight(false);
        pdfRow.setPrefWidth(520);

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("draft", "published", "archived");
        statusBox.getSelectionModel().selectFirst();
        statusBox.getStyleClass().add("form-combobox");
        statusBox.setPrefWidth(520);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(16);
        grid.setPadding(new Insets(10, 0, 0, 0));
        grid.add(new Label("Titre *"), 0, 0);
        grid.add(titleField, 0, 1);
        grid.add(new Label("Description *"), 0, 2);
        grid.add(descriptionArea, 0, 3);
        grid.add(new Label("Image (optionnel)"), 0, 4);
        grid.add(imageRow, 0, 5);
        grid.add(new Label("PDF (optionnel)"), 0, 6);
        grid.add(pdfRow, 0, 7);
        grid.add(new Label("Statut *"), 0, 8);
        grid.add(statusBox, 0, 9);

        Label note = new Label("Note : L'ID et la date de création seront générés automatiquement.");
        note.getStyleClass().add("form-note");
        note.setWrapText(true);

        Button saveBtn = new Button("Créer le Magazine");
        saveBtn.getStyleClass().addAll("primary-button", "dialog-button");
        Button cancelBtn = new Button("Annuler");
        cancelBtn.getStyleClass().addAll("secondary-button", "dialog-button");
        cancelBtn.setOnAction(evt -> dialog.close());

        HBox actionRow = new HBox(12, cancelBtn, saveBtn);
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        VBox content = new VBox(18, headerBar, subtitle, grid, note, actionRow);
        content.getStyleClass().add("form-dialog");
        content.setPadding(new Insets(20));
        content.setPrefWidth(560);

        saveBtn.setOnAction(evt -> {
            Magazine magazine = new Magazine(titleField.getText().trim(), descriptionArea.getText().trim(),
                    statusBox.getValue(), pdfField.getText().trim());
            magazine.setImage(imageField.getText().trim());
            if (!validerMagazine(magazine.getTitre(), magazine.getDescription(), magazine.getStatut())) {
                return;
            }
            try {
                serviceMagazine.ajouter(magazine);
                obsMagazines.add(magazine);
                if (tableMagazines != null) {
                    tableMagazines.refresh();
                }
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Magazine ajouté !");
                dialog.close();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        });

        Scene scene = new Scene(content);
        URL css = getClass().getResource("/styles/application.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void editMagazine(Magazine magazine) {
        Optional<Magazine> result = showMagazineEditDialog(magazine);
        result.ifPresent(updated -> {
            try {
                serviceMagazine.modifier(updated);
                if (tableMagazines != null) {
                    tableMagazines.refresh();
                }
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Magazine modifié avec succès !");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        });
    }

    private void deleteMagazine(Magazine magazine) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer le magazine");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous vraiment supprimer ce magazine ?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceMagazine.supprimer(magazine.getId());
                obsMagazines.remove(magazine);
                if (tableMagazines != null) {
                    tableMagazines.refresh();
                }
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Magazine supprimé !");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        }
    }

    private Optional<Magazine> showMagazineEditDialog(Magazine existing) {
        Stage dialog = new Stage(StageStyle.UNDECORATED);
        dialog.initModality(Modality.APPLICATION_MODAL);

        Label header = new Label("Modifier Magazine");
        header.getStyleClass().add("form-header-title");

        Button closeBtn = new Button("✕");
        closeBtn.getStyleClass().add("modal-close-button");
        closeBtn.setOnAction(evt -> dialog.close());

        HBox headerBar = new HBox(header, new Region(), closeBtn);
        headerBar.getStyleClass().add("modal-header");
        HBox.setHgrow(headerBar.getChildren().get(1), Priority.ALWAYS);

        Label subtitle = new Label("Modifiez les détails de ce magazine et enregistrez vos changements.");
        subtitle.getStyleClass().add("form-subtitle");
        subtitle.setWrapText(true);

        TextField titleField = new TextField(existing.getTitre());
        titleField.setPromptText("Entrez le titre du magazine (3-255 caractères)");
        titleField.getStyleClass().add("form-field");

        TextArea descriptionArea = new TextArea(existing.getDescription());
        descriptionArea.setPromptText("Entrez une description (minimum 10 caractères)");
        descriptionArea.setPrefRowCount(6);
        descriptionArea.getStyleClass().add("form-textarea");

        TextField imageField = new TextField(existing.getImage());
        imageField.setPromptText("Sélectionnez une image...");
        imageField.getStyleClass().add("form-field");
        imageField.setEditable(false);

        Button browseButton = new Button("Parcourir");
        browseButton.getStyleClass().addAll("secondary-button", "dialog-button");
        browseButton.setOnAction(evt -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choisir une image");
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                    new FileChooser.ExtensionFilter("Tous les fichiers", "*.*"));
            File file = chooser.showOpenDialog(dialog);
            if (file != null) {
                imageField.setText(file.toURI().toString());
            }
        });

        HBox imageRow = new HBox(10, imageField, browseButton);
        imageRow.setAlignment(Pos.CENTER_LEFT);
        imageRow.setFillHeight(false);
        imageRow.setPrefWidth(520);

        TextField pdfField = new TextField(existing.getPdfFile());
        pdfField.setPromptText("Sélectionnez un PDF ou collez une URL...");
        pdfField.getStyleClass().add("form-field");
        pdfField.setEditable(false);

        Button pdfBrowseButton = new Button("Parcourir");
        pdfBrowseButton.getStyleClass().addAll("secondary-button", "dialog-button");
        pdfBrowseButton.setOnAction(evt -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choisir un PDF");
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("PDF", "*.pdf"),
                    new FileChooser.ExtensionFilter("Tous les fichiers", "*.*"));
            File file = chooser.showOpenDialog(dialog);
            if (file != null) {
                pdfField.setText(file.toURI().toString());
            }
        });

        HBox pdfRow = new HBox(10, pdfField, pdfBrowseButton);
        pdfRow.setAlignment(Pos.CENTER_LEFT);
        pdfRow.setFillHeight(false);
        pdfRow.setPrefWidth(520);

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("draft", "published", "archived");
        statusBox.setValue(existing.getStatut());
        statusBox.getStyleClass().add("form-combobox");
        statusBox.setPrefWidth(520);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(16);
        grid.setPadding(new Insets(10, 0, 0, 0));
        grid.add(new Label("Titre *"), 0, 0);
        grid.add(titleField, 0, 1);
        grid.add(new Label("Description *"), 0, 2);
        grid.add(descriptionArea, 0, 3);
        grid.add(new Label("Image (optionnel)"), 0, 4);
        grid.add(imageRow, 0, 5);
        grid.add(new Label("PDF (optionnel)"), 0, 6);
        grid.add(pdfRow, 0, 7);
        grid.add(new Label("Statut *"), 0, 8);
        grid.add(statusBox, 0, 9);

        Label note = new Label("Note : L'ID et la date de création ne seront pas modifiés.");
        note.getStyleClass().add("form-note");
        note.setWrapText(true);

        Button saveBtn = new Button("Enregistrer");
        saveBtn.getStyleClass().addAll("primary-button", "dialog-button");
        Button cancelBtn = new Button("Annuler");
        cancelBtn.getStyleClass().addAll("secondary-button", "dialog-button");
        cancelBtn.setOnAction(evt -> dialog.close());

        HBox actionRow = new HBox(12, cancelBtn, saveBtn);
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        VBox content = new VBox(18, headerBar, subtitle, grid, note, actionRow);
        content.getStyleClass().add("form-dialog");
        content.setPadding(new Insets(20));
        content.setPrefWidth(560);

        saveBtn.setOnAction(evt -> {
            Magazine updated = new Magazine(existing.getId(), titleField.getText().trim(),
                    descriptionArea.getText().trim(), imageField.getText().trim(), existing.getDateCreate(),
                    statusBox.getValue(), pdfField.getText().trim());
            updated.setArticles(existing.getArticles());
            if (!validerMagazine(updated.getTitre(), updated.getDescription(), updated.getStatut())) {
                return;
            }
            try {
                serviceMagazine.modifier(updated);
                if (tableMagazines != null) {
                    tableMagazines.refresh();
                }
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Magazine modifié avec succès !");
                dialog.close();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        });

        Scene scene = new Scene(content);
        URL css = getClass().getResource("/styles/application.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
        dialog.setScene(scene);
        dialog.showAndWait();
        return Optional.empty();
    }

    private void showArticleAddDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Rédaction d'Article");
        dialog.initModality(Modality.APPLICATION_MODAL);

        Label titleLabel = new Label("Sélectionner un magazine parent");
        ComboBox<Magazine> magazineBox = new ComboBox<>();
        magazineBox.getItems().setAll(obsMagazines);
        configureMagazineComboBox(magazineBox);
        magazineBox.setPromptText("Choisissez un magazine...");
        magazineBox.setMaxWidth(Double.MAX_VALUE);

        Label articleTitleLabel = new Label("Titre de l'article");
        TextField titreField = new TextField();
        titreField.setPromptText("Entrez le titre de l'article");

        Label summaryLabel = new Label("Summary");
        TextArea summaryArea = new TextArea();
        summaryArea.setPromptText("Enter a brief summary of your article...");
        summaryArea.setPrefRowCount(4);

        Label contentLabel = new Label("Main Content (Resume)");
        TextArea resumeArea = new TextArea();
        resumeArea.setPromptText("Write your article content here...");
        resumeArea.setPrefRowCount(10);

        Button publishButton = new Button("Publier l'article");
        publishButton.getStyleClass().add("primary-button");
        Button cancelButton = new Button("Annuler");
        HBox buttonRow = new HBox(10, cancelButton, publishButton);
        buttonRow.setStyle("-fx-alignment: center-right;");

        VBox root = new VBox(12,
                titleLabel, magazineBox,
                articleTitleLabel, titreField,
                summaryLabel, summaryArea,
                contentLabel, resumeArea,
                buttonRow);
        root.setPadding(new Insets(20));
        root.setPrefWidth(700);

        publishButton.setOnAction(evt -> {
            String titre = titreField.getText().trim();
            String resume = resumeArea.getText().trim();
            String summary = summaryArea.getText().trim();
            Magazine selectedMagazine = magazineBox.getValue();
            String statut = "published";
            String auteur = "Admin";

            if (selectedMagazine == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Sélectionnez un magazine parent.");
                return;
            }
            if (titre.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le titre est obligatoire.");
                return;
            }
            if (resume.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le contenu est obligatoire.");
                return;
            }
            if (summary.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le résumé est obligatoire.");
                return;
            }

            Article article = new Article(
                    titre,
                    resume,
                    auteur,
                    LocalDate.now().atStartOfDay(),
                    summary,
                    statut,
                    null);
            article.setMagazine(selectedMagazine);

            try {
                serviceArticle.ajouter(article);
                obsArticles.add(article);
                if (tableArticles != null) {
                    tableArticles.refresh();
                }
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Article ajouté avec succès !");
                dialog.close();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec d'ajout : " + e.getMessage());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur inattendue", e.getMessage());
            }
        });

        cancelButton.setOnAction(evt -> dialog.close());

        Scene scene = new Scene(root);
        URL css = getClass().getResource("/styles/application.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    // ====================== UTILITAIRES ======================
    private void remplirFormulaireMagazine(Magazine m) {
        /* reste le même */ }

    private void clearFormMagazine() {
        /* reste le même */ }

    private void remplirFormulaireArticle(Article a) {
        tfTitreArt.setText(a.getTitre());
        taResumeArt.setText(a.getResume());
        tfAuteurArt.setText(a.getAuteur());
        if (a.getDatePub() != null)
            dpDatePubArt.setValue(a.getDatePub().toLocalDate());
        taSummaryArt.setText(a.getSummary());
        cbStatutArt.setValue(a.getStatut());
        cbMagazineArt.setValue(a.getMagazine());
    }

    @FXML
    private void clearFormArticle() {
        tfTitreArt.clear();
        taResumeArt.clear();
        tfAuteurArt.clear();
        dpDatePubArt.setValue(null);
        taSummaryArt.clear();
        cbStatutArt.setValue(null);
        cbMagazineArt.setValue(null);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void ouvrirFormulaireAjoutMagazine() {
        showMagazineAddDialog();
    }

    @FXML
    private void ouvrirFormulaireAjoutArticle() {
        showArticleAddDialog();
    }

    @FXML
    private void ouvrirPageMagazines(ActionEvent event) {
        naviguerVers(event, "/Views/backoffice/magazineAdmin.fxml");
    }

    @FXML
    private void ouvrirPageArticles(ActionEvent event) {
        naviguerVers(event, "/Views/backoffice/articleAdmin.fxml");
    }

    private void naviguerVers(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();
            if (scene == null) {
                scene = new Scene(root);
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page : " + e.getMessage());
        }
    }
}