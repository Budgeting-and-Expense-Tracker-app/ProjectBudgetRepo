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

                scene = new Scene(root, 800, 600);
                stage.setScene(scene);
            } else {

                scene.setRoot(root);
            }

            stage.centerOnScreen();

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

