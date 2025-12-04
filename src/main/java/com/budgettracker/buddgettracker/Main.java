package com.budgettracker.buddgettracker;

import com.budgettracker.buddgettracker.SceneSwitcher;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        SceneSwitcher.switchTo(stage, "login_view.fxml");
        stage.setTitle("Budget Tracker");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
