package com.budgettracker.buddgettracker;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneSwitcher {

    public static void switchTo(Stage stage, String fxml) {
        try {
            Parent root = FXMLLoader.load(SceneSwitcher.class.getResource(fxml));

            Scene scene = stage.getScene();
            if (scene == null) {
                // first time: create the scene
                scene = new Scene(root, 800, 600);
                stage.setScene(scene);
            } else {
                // later: just swap the root, keep size/maximized
                scene.setRoot(root);
            }

            //center the window on the screen
            stage.centerOnScreen();

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

