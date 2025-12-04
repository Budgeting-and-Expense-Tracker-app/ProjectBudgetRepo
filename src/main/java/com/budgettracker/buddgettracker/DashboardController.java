package com.budgettracker.buddgettracker;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.net.URL;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.geometry.Pos;
import java.time.LocalDate;

public class DashboardController {
    //images
    @FXML private ImageView walletIcon;

    // Texts from the four summary tiles
    @FXML private Text totalIncomeLabel;
    @FXML private Text totalExpensesLabel;
    @FXML private Text balanceLabel;
    @FXML private Text budgetRemainingLabel;
    @FXML private Button addTransactionButton;

    //Part of Budget Summary Title
    private double totalIncome  = 0.0;
    private double totalExpenses = 0.0;
    private double totalBudgetLimit = 0.0; //sum of budget summary
    @FXML private Label budgetPercentLabel; // "0% used"

    // Savings tab controls
    @FXML private VBox savingsEmptyStateBox;
    @FXML private VBox savingsFormBox;
    @FXML private VBox savingsListBox;
    @FXML private TextField savingsNameField;
    @FXML private TextField savingsAmountField;
    @FXML private DatePicker savingsDeadlinePicker;


    // Budgets tab controls
    @FXML private VBox budgetsEmptyStateBox;
    @FXML private VBox budgetFormBox;
    @FXML private VBox budgetListBox;
    @FXML private ComboBox<String> budgetCategoryCombo;
    @FXML private TextField budgetAmountField;

    // Reports tab controls
    @FXML private ToggleButton byCategoryToggle;
    @FXML private ToggleButton trendsToggle;
    @FXML private PieChart categoryChart;            // was BarChart<String, Number>
    @FXML private LineChart<String, Number> trendsChart;



    @FXML
    //load images and summary titles
    private void initialize() {
        // Load icon
        URL iconUrl = getClass().getResource("images/Wallet.png");
        if (iconUrl != null) {
            walletIcon.setImage(new Image(iconUrl.toExternalForm()));
        } else {
            System.out.println("⚠ Wallet icon not found at images/Wallet.png");
        }
        //existing summary label setup
        updateSummaryLabels();

        //reports tab setup
        setupReportToggle();
    }


    //TOTAL SUMMARY TITLES OF THE TABS
    private void updateSummaryLabels() {
        double balance = totalIncome - totalExpenses;

        double budgetRemaining;
        if (totalBudgetLimit > 0) {
            // Use budgets: remaining = total budget - total expenses
            budgetRemaining = totalBudgetLimit - totalExpenses;
            double usedPercent = (totalExpenses / totalBudgetLimit) * 100.0;
            if (budgetPercentLabel != null) {
                budgetPercentLabel.setText(String.format("%.0f%% used", usedPercent));
            }
        } else {
            // No budgets set yet: fall back to balance
            budgetRemaining = balance;
            if (budgetPercentLabel != null) {
                budgetPercentLabel.setText("0% used");
            }
        }

        totalIncomeLabel.setText(String.format("$%.2f", totalIncome));
        totalExpensesLabel.setText(String.format("$%.2f", totalExpenses));
        balanceLabel.setText(String.format("$%.2f", balance));
        budgetRemainingLabel.setText(String.format("$%.2f", budgetRemaining));
    }


    //IF Press + ACTION CLICK FOR TRANSACTION THEN POPUP ....
    /**   ADD to transaction tab (figma)*/
    @FXML
    private void openAddTransactionPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("transaction_form.fxml"));
            Scene scene = new Scene(loader.load());

            // Give the popup a reference to this controller
            TransactionFormController controller = loader.getController();
            controller.setDashboardController(this);

            Stage popup = new Stage();
            Stage owner = (Stage) addTransactionButton
                    .getScene()
                    .getWindow();
            popup.initOwner(owner);
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setTitle("Add Transaction");
            popup.setScene(scene);
            popup.setResizable(false);
            popup.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /** Called from TransactionFormController when the form is submitted */
    public void addTransaction(Transaction transaction) {
        if (transaction == null) return;

        if ("Income".equalsIgnoreCase(transaction.getType())) {
            totalIncome += transaction.getAmount();
        } else {
            totalExpenses += transaction.getAmount();
        }
        updateSummaryLabels();
    }


    // Budget tab
    @FXML
    private void showBudgetForm() {
        // Show the form
        budgetFormBox.setVisible(true);
        budgetFormBox.setManaged(true);

        // Hide the empty state (if it was showing)
        budgetsEmptyStateBox.setVisible(false);
        budgetsEmptyStateBox.setManaged(false);
    }
    @FXML
    private void cancelBudgetForm() {
        // Hide the form
        budgetFormBox.setVisible(false);
        budgetFormBox.setManaged(false);

        // If there are still no budgets, show the empty message again (figma)
        if (budgetListBox.getChildren().isEmpty()) {
            budgetsEmptyStateBox.setVisible(true);
            budgetsEmptyStateBox.setManaged(true);
        }
    }
    @FXML
    private void handleAddBudget() {
        String category = budgetCategoryCombo.getValue();
        String amountText = budgetAmountField.getText();

        if (category == null || category.isBlank()) {
            // you could show an Alert here if need
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException ex) {
            // invalid number, you could show an Alert
            return;
        }
        // Increase the total budget limit
        totalBudgetLimit += amount;
        // Create a simple "card" for the budget
        HBox card = new HBox(10);
        card.setStyle("-fx-background-color: #f7f7f7; -fx-padding: 12; -fx-background-radius: 8;");
        // Store the limit for this card
        card.setUserData(amount);
        VBox textBox = new VBox(3);
        Label nameLabel = new Label(category);
        nameLabel.setStyle("-fx-font-weight: bold;");
        Label amountLabel = new Label(String.format("$0.00 of $%.2f", amount));
        Label percentLabel = new Label("0% used");
        textBox.getChildren().addAll(nameLabel, amountLabel, percentLabel);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteBtn = new Button("🗑");
        deleteBtn.setOnAction(e -> {
            // subtract this card's budget from total
            Object data = card.getUserData();
            if (data instanceof Double) {
                totalBudgetLimit -= (Double) data;
                if (totalBudgetLimit < 0) totalBudgetLimit = 0;
                updateSummaryLabels();
            }

            budgetListBox.getChildren().remove(card);
            if (budgetListBox.getChildren().isEmpty()) {
                budgetsEmptyStateBox.setVisible(true);
                budgetsEmptyStateBox.setManaged(true);
            }
        });
        card.getChildren().addAll(textBox, spacer, deleteBtn);
        // Add the card to the list
        budgetListBox.getChildren().add(card);
        // Show the list, hide empty state & hide form
        budgetListBox.setVisible(true);
        budgetListBox.setManaged(true);
        budgetsEmptyStateBox.setVisible(false);
        budgetsEmptyStateBox.setManaged(false);
        budgetFormBox.setVisible(false);
        budgetFormBox.setManaged(false);
        // Clear form for next time
        budgetAmountField.clear();
        //updates the summary of budget
        updateSummaryLabels();
    }



    // SAVING METHODS: Show savings creation form
    @FXML
    private void showSavingsForm() {
        if (savingsFormBox != null) {
            savingsFormBox.setVisible(true);
            savingsFormBox.setManaged(true);
        }
        if (savingsEmptyStateBox != null) {
            savingsEmptyStateBox.setVisible(false);
            savingsEmptyStateBox.setManaged(false);
        }
        if (savingsListBox != null) {
            boolean hasItems = !savingsListBox.getChildren().isEmpty();
            savingsListBox.setVisible(hasItems);
            savingsListBox.setManaged(hasItems);
        }
    }

    // Cancel savings form (return to empty state if nothing exists)
    @FXML
    private void cancelSavingsForm() {
        if (savingsFormBox != null) {
            savingsFormBox.setVisible(false);
            savingsFormBox.setManaged(false);
        }
        if (savingsListBox != null && savingsListBox.getChildren().isEmpty()) {
            savingsEmptyStateBox.setVisible(true);
            savingsEmptyStateBox.setManaged(true);
        } else if (savingsListBox != null) {
            boolean has = !savingsListBox.getChildren().isEmpty();
            savingsListBox.setVisible(has);
            savingsListBox.setManaged(has);
        }
    }

    // Add a saving goal (creates card with progress + add funds)
    @FXML
    private void handleAddSaving() {
        String name = (savingsNameField != null) ? savingsNameField.getText() : null;
        String amountStr = (savingsAmountField != null) ? savingsAmountField.getText() : null;
        LocalDate deadline = (savingsDeadlinePicker != null) ? savingsDeadlinePicker.getValue() : null;

        if (name == null || name.isBlank()) return;

        double target;
        try {
            target = Double.parseDouble((amountStr == null || amountStr.isBlank()) ? "0" : amountStr);
        } catch (NumberFormatException ex) {
            return;
        }

        // card container
        VBox card = new VBox(6);
        card.setStyle("-fx-background-color: #f7f7f7; -fx-padding: 12; -fx-background-radius: 8; -fx-border-radius:8;");

        // header: title, optional deadline, delete
        HBox header = new HBox(8);
        VBox titleBox = new VBox(2);
        Label title = new Label(name);
        title.setStyle("-fx-font-weight: bold;");
        titleBox.getChildren().add(title);
        if (deadline != null) {
            Label dl = new Label("Deadline: " + deadline.toString());
            dl.setStyle("-fx-font-size: 11px; -fx-text-fill:#666;");
            titleBox.getChildren().add(dl);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteBtn = new Button("🗑");
        deleteBtn.setOnAction(e -> {
            savingsListBox.getChildren().remove(card);
            if (savingsListBox.getChildren().isEmpty()) {
                savingsEmptyStateBox.setVisible(true);
                savingsEmptyStateBox.setManaged(true);
            }
        });

        header.getChildren().addAll(titleBox, spacer, deleteBtn);

        // amount label + progress
        Label amountLabel = new Label(String.format("$0.00 / $%.2f", target));
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(600);
        Label pctLabel = new Label("0%");
        pctLabel.setStyle("-fx-text-fill: #666;");

        HBox progressRow = new HBox(10, progressBar, pctLabel);
        progressRow.setAlignment(Pos.CENTER_LEFT);

        // add funds controls
        HBox addFundsRow = new HBox(8);
        TextField addField = new TextField();
        addField.setPromptText("Add amount");
        addField.setPrefWidth(200);
        Button addFundsBtn = new Button("Add Funds");
        addFundsRow.getChildren().addAll(addField, addFundsBtn);

        // store state: double[0]=saved, double[1]=target
        double[] state = new double[]{0.0, target};
        card.setUserData(state);

        addFundsBtn.setOnAction(ev -> {
            String toAddStr = addField.getText();
            double toAdd;
            try {
                toAdd = Double.parseDouble((toAddStr == null || toAddStr.isBlank()) ? "0" : toAddStr);
            } catch (NumberFormatException ex) {
                return;
            }
            if (toAdd <= 0) return;
            Object ud = card.getUserData();
            if (ud instanceof double[]) {
                double[] st = (double[]) ud;
                st[0] += toAdd;
                if (st[0] > st[1]) st[0] = st[1];
                amountLabel.setText(String.format("$%.2f / $%.2f", st[0], st[1]));
                double pct = (st[1] <= 0) ? 0 : (st[0] / st[1]);
                progressBar.setProgress(Math.max(0.0, Math.min(1.0, pct)));
                pctLabel.setText(String.format("%d%%", (int) Math.round(pct * 100.0)));
                addField.clear();
            }
        });

        card.getChildren().addAll(header, amountLabel, progressRow, addFundsRow);

        savingsListBox.getChildren().add(card);
        savingsListBox.setVisible(true);
        savingsListBox.setManaged(true);

        // hide form & empty state
        savingsFormBox.setVisible(false);
        savingsFormBox.setManaged(false);
        savingsEmptyStateBox.setVisible(false);
        savingsEmptyStateBox.setManaged(false);

        // clear form
        savingsNameField.clear();
        savingsAmountField.clear();
        savingsDeadlinePicker.setValue(null);
    }



    //method for the report tab
    private void setupReportToggle() {
        if (categoryChart != null && trendsChart != null) {
            // default
            showCategoryChart();

            byCategoryToggle.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    showCategoryChart();
                }
            });

            trendsToggle.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    showTrendsChart();
                }
            });
        }
    }
    private void showCategoryChart() {
        categoryChart.setVisible(true);
        categoryChart.setManaged(true);
        trendsChart.setVisible(false);
        trendsChart.setManaged(false);
    }
    private void showTrendsChart() {
        trendsChart.setVisible(true);
        trendsChart.setManaged(true);
        categoryChart.setVisible(false);
        categoryChart.setManaged(false);
    }

}
