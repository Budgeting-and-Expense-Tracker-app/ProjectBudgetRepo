package com.budgettracker.buddgettracker;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    private final FirestoreContext firestoreContext = new FirestoreContext();

    @FXML
    public void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            setMessage("Please enter email and password.");
            return;
        }

        Firestore db = firestoreContext.firebase();
        if (db == null) {
            setMessage("Cannot connect to database.");
            return;
        }

        try {
            DocumentReference docRef = db.collection("users").document(email);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot snapshot = future.get();

            if (!snapshot.exists()) {
                setMessage("Account not found.");
                return;
            }

            String storedPassword = snapshot.getString("password");

            if (storedPassword != null && storedPassword.equals(password)) {
                
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard_view.fxml"));
                Parent root = loader.load();

                DashboardController controller = loader.getController();
                controller.setCurrentUserEmail(email);   

                stage.setScene(new Scene(root));
                stage.show();
            } else {
                setMessage("Invalid email or password.");
            }


        } catch (Exception e) {
            e.printStackTrace();
            setMessage("Error while logging in.");
        }
    }

    @FXML
    public void goToRegister(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            SceneSwitcher.switchTo(stage, "register_view.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            setMessage("Cannot open register screen.");
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
