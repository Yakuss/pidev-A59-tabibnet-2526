import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/resources/main.fxml")
        );
        Scene scene = new Scene(loader.load(), 850, 650);
        primaryStage.setTitle("TabibNet Desktop Edition");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        // controller.ApiServer.start(); <-- DÉSACTIVÉ pour faire place au 100% Backend
        launch(args);
    }
}