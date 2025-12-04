package com.budgettracker.buddgettracker;

import com.budgettracker.buddgettracker.SceneSwitcher;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    public void handleRegister() {
        // For now, just go back to login after "registering"
        Stage stage = (Stage) nameField.getScene().getWindow();
        SceneSwitcher.switchTo(stage, "login_view.fxml");
    }

    public void goBack() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        SceneSwitcher.switchTo(stage, "login_view.fxml");
    }
}

