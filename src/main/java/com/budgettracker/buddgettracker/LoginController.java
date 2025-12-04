package com.budgettracker.buddgettracker;

import com.budgettracker.buddgettracker.SceneSwitcher;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    public void handleLogin() {
        // TEMPORARY — always succeeds = testing
        Stage stage = (Stage) emailField.getScene().getWindow();
        SceneSwitcher.switchTo(stage, "dashboard_view.fxml");
    }

    public void goToRegister() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        SceneSwitcher.switchTo(stage, "register_view.fxml");
    }
}

