package com.pidev;

import com.pidev.utils.AdminAccountInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LoginView.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setTitle("PiDev Medical - Connexion");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
            
            System.out.println("🚀 PiDev Medical Authentication started!");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Failed to start application: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Automatically ensure the admin account exists in the database on startup
        try {
            AdminAccountInitializer.seedAdmin();
        } catch (Exception e) {
            System.err.println("⚠️ Could not auto-seed admin: " + e.getMessage());
        }
        launch(args);
    }
}
