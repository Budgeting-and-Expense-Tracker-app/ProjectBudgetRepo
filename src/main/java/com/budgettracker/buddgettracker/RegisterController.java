package com.budgettracker.buddgettracker;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class RegisterController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;

    private final FirestoreContext firestoreContext = new FirestoreContext();

    @FXML
    public void handleRegister(ActionEvent event) {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            setMessage("Please fill in all fields.");
            return;
        }

        if (!password.equals(confirm)) {
            setMessage("Passwords do not match.");
            return;
        }

        Firestore db = firestoreContext.firebase();
        if (db == null) {
            setMessage("Cannot connect to database.");
            return;
        }

        try {
            DocumentReference docRef = db.collection("users").document(email);


            DocumentSnapshot existing = docRef.get().get();
            if (existing.exists()) {
                setMessage("Account with this email already exists.");
                return;
            }

            Map<String, Object> userData = new HashMap<>();
            userData.put("name", name);
            userData.put("email", email);
            userData.put("password", password);
            ApiFuture<WriteResult> future = docRef.set(userData);
            future.get();

            setMessage("Registration successful. Logging in...");

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            SceneSwitcher.switchTo(stage, "login_view.fxml");

        } catch (Exception e) {
            e.printStackTrace();
            setMessage("Error while registering user.");
        }
    }

    @FXML
    public void handleGoBack(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            SceneSwitcher.switchTo(stage, "login_view.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            setMessage("Cannot open login screen.");
        }
    }

    private void setMessage(String text) {
        if (messageLabel != null) {
            messageLabel.setText(text);
        } else {
            System.out.println(text);
        }
    }
}

