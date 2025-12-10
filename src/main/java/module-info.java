module com.budgettracker.buddgettracker {
    requires javafx.controls;
    requires javafx.fxml;


    requires firebase.admin;
    requires com.google.auth;
    requires com.google.auth.oauth2;
    requires google.cloud.firestore;
    requires google.cloud.core;
    requires com.google.api.apicommon;

    opens com.budgettracker.buddgettracker to javafx.fxml;
    exports com.budgettracker.buddgettracker;
}
