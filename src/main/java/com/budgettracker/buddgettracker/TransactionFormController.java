package com.budgettracker.buddgettracker;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class TransactionFormController {

    @FXML private ToggleGroup typeToggleGroup;
    @FXML private ToggleButton expenseToggle;
    @FXML private ToggleButton incomeToggle;

    @FXML private TextField amountField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextField descriptionField;
    @FXML private DatePicker datePicker;
    @FXML private Button submitButton;

    private DashboardController dashboardController;

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @FXML
    public void initialize() {
        categoryCombo.getItems().addAll(
                "Salary", "Groceries", "Rent", "Bills", "Entertainment", "Other"
        );

        datePicker.setValue(LocalDate.now());

        typeToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == incomeToggle) {
                submitButton.setText("Add Income");
            } else {
                submitButton.setText("Add Expense");
            }
        });
    }

    @FXML
    private void handleSubmit() {
        try {
            String type = (typeToggleGroup.getSelectedToggle() == incomeToggle)
                    ? "Income" : "Expense";

            double amount = Double.parseDouble(amountField.getText());

            String category = categoryCombo.getValue();
            if (category == null || category.isBlank()) {
                category = "Uncategorized";
            }

            String description = descriptionField.getText();
            if (description == null) description = "";

            LocalDate date = (datePicker.getValue() != null)
                    ? datePicker.getValue()
                    : LocalDate.now();

            Transaction transaction = new Transaction(
                    type, category, description, amount, date
            );

            if (dashboardController != null) {
                dashboardController.addTransaction(transaction);
            }

            // close popup
            Stage stage = (Stage) submitButton.getScene().getWindow();
            stage.close();

        } catch (NumberFormatException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Please enter a valid number for Amount.",
                    ButtonType.OK);
            alert.showAndWait();
        }
    }
}
