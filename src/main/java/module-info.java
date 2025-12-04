module com.budgettracker.buddgettracker {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.budgettracker.buddgettracker to javafx.fxml;
    exports com.budgettracker.buddgettracker;
}