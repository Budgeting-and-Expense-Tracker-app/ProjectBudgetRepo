package com.budgettracker.buddgettracker;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import com.google.cloud.firestore.Firestore;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import com.google.cloud.firestore.DocumentReference;
import javafx.scene.paint.Color;





import java.net.URL;

import java.util.HashMap;
import java.util.Map;

public class DashboardController {

    
    @FXML private ImageView walletIcon;
    @FXML private Button addTransactionButton;
    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, LocalDate> colDate;
    @FXML private TableColumn<Transaction, String> colType;
    @FXML private TableColumn<Transaction, String> colCategory;
    @FXML private TableColumn<Transaction, String> colDescription;
    @FXML private TableColumn<Transaction, Double> colAmount;
    @FXML private TextField billNameField;
    @FXML private TextField billAmountField;
    @FXML private ComboBox<String> billCategoryCombo;
    @FXML private DatePicker billDueDatePicker;
    @FXML private CheckBox billRecurringCheckBox;
    @FXML private VBox billsListBox;
    private final FirestoreContext firestoreContext = new FirestoreContext();
    private Firestore db;

    
    @FXML private javafx.scene.text.Text totalIncomeLabel;
    @FXML private javafx.scene.text.Text totalExpensesLabel;
    @FXML private javafx.scene.text.Text balanceLabel;
    @FXML private javafx.scene.text.Text budgetRemainingLabel;
    @FXML private Label budgetPercentLabel;

    
    @FXML private VBox budgetsEmptyStateBox;
    @FXML private VBox budgetFormBox;
    @FXML private ComboBox<String> budgetCategoryCombo;
    @FXML private TextField budgetAmountField;
    @FXML private VBox budgetListBox;

    
    @FXML private VBox savingsEmptyStateBox;
    @FXML private VBox savingsFormBox;
    @FXML private TextField savingsNameField;
    @FXML private TextField savingsAmountField;
    @FXML private DatePicker savingsDeadlinePicker;
    @FXML private VBox savingsListBox;

    
    @FXML private ToggleButton byCategoryToggle;
    @FXML private ToggleButton trendsToggle;
    @FXML private ToggleGroup reportViewToggle;
    @FXML private PieChart categoryChart;
    @FXML private LineChart<String, Number> trendsChart;

    
    private final ObservableList<Transaction> transactions = FXCollections.observableArrayList();
    private double totalIncome = 0;
    private double totalExpenses = 0;

    
    private final Map<String, Double> budgetLimits = new HashMap<>();
    private double totalBudgetLimit = 0;
    private final ObservableList<Bill> bills = FXCollections.observableArrayList();
    private final ObservableList<SavingGoal> savingGoals = FXCollections.observableArrayList();
    private String currentUserEmail;

    public void setCurrentUserEmail(String email) {
        this.currentUserEmail = email;
        if (db != null) {
            loadUserData();
        }
    }



    @FXML
    public void initialize() {
        
        URL iconUrl = getClass().getResource("images/Wallet.png");
        if (iconUrl != null) {
            walletIcon.setImage(new Image(iconUrl.toExternalForm()));
        }
        setupTransactionsTable();
        
        setVisibleManaged(budgetFormBox, false);
        setVisibleManaged(budgetListBox, false);
        setVisibleManaged(savingsFormBox, false);
        setVisibleManaged(savingsListBox, false);
        setVisibleManaged(budgetsEmptyStateBox, true);
        setVisibleManaged(savingsEmptyStateBox, true);

        
        if (byCategoryToggle != null) {
            byCategoryToggle.setSelected(true);
        }
        if (reportViewToggle != null) {
            reportViewToggle.selectedToggleProperty().addListener((obs, oldT, newT) -> updateReportView());
        }
        updateReportView();

        updateSummaryLabels();
        updateCharts();
        db = firestoreContext.firebase();
    }
    private void setupTransactionsTable() {
        if (transactionsTable == null) return;

        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));

        transactionsTable.setItems(transactions);
    }

    
    private void setVisibleManaged(Region node, boolean value) {
        if (node != null) {
            node.setVisible(value);
            node.setManaged(value);
        }
    }
    private void loadUserData() {
        if (db == null || currentUserEmail == null) return;

        try {
            loadTransactionsFromFirestore();
            loadBudgetsFromFirestore();
            loadBillsFromFirestore();
            loadSavingsFromFirestore();

            updateSummaryLabels();
            updateBudgetProgress();
            updateCharts();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Firebase", "Error loading your data from the cloud.");
        }
    }
    private void loadTransactionsFromFirestore() throws Exception {
        ApiFuture<QuerySnapshot> future = db.collection("transactions")
                .whereEqualTo("userEmail", currentUserEmail)
                .get();
        QuerySnapshot snapshot = future.get();

        transactions.clear();
        totalIncome = 0;
        totalExpenses = 0;

        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            String type = doc.getString("type");
            String category = doc.getString("category");
            String description = doc.getString("description");
            Double amount = doc.getDouble("amount");
            String dateStr = doc.getString("date");

            if (amount == null) amount = 0.0;
            java.time.LocalDate date = (dateStr != null && !dateStr.isBlank())
                    ? java.time.LocalDate.parse(dateStr)
                    : java.time.LocalDate.now();

            Transaction t = new Transaction(type, category, description, amount, date);


            t.setId(doc.getId());

            transactions.add(t);

            if ("Income".equalsIgnoreCase(type)) {
                totalIncome += amount;
            } else {
                totalExpenses += amount;
            }
        }

        if (transactionsTable != null) {
            transactionsTable.setItems(transactions);
        }
    }

    private void loadBudgetsFromFirestore() throws Exception {
        ApiFuture<QuerySnapshot> future = db.collection("budgets")
                .whereEqualTo("userEmail", currentUserEmail)
                .get();
        QuerySnapshot snapshot = future.get();

        budgetLimits.clear();
        totalBudgetLimit = 0;

        if (budgetListBox != null) {
            budgetListBox.getChildren().clear();
        }

        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            String category = doc.getString("category");
            Double limit = doc.getDouble("limit");
            if (category == null || limit == null) continue;

            budgetLimits.put(category, limit);
            totalBudgetLimit += limit;

            
            addBudgetCard(category, limit);
        }

        if (budgetListBox != null) {
            boolean empty = budgetListBox.getChildren().isEmpty();
            setVisibleManaged(budgetsEmptyStateBox, empty);
            setVisibleManaged(budgetListBox, !empty);
        }
    }
    private void loadBillsFromFirestore() throws Exception {
        ApiFuture<QuerySnapshot> future = db.collection("bills")
                .whereEqualTo("userEmail", currentUserEmail)
                .get();
        QuerySnapshot snapshot = future.get();

        bills.clear();
        if (billsListBox != null) {
            billsListBox.getChildren().clear();
        }

        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            String name = doc.getString("name");
            Double amount = doc.getDouble("amount");
            String category = doc.getString("category");
            String dueStr = doc.getString("dueDate");
            Boolean recurring = doc.getBoolean("recurring");
            Boolean paid = doc.getBoolean("paid");
            String paidDateStr = doc.getString("paidDate");

            if (name == null || amount == null || category == null) continue;

            LocalDate dueDate = (dueStr != null && !dueStr.equals("noDate") && !dueStr.isBlank())
                    ? LocalDate.parse(dueStr)
                    : null;

            Bill bill = new Bill(name, amount, category, dueDate,
                    recurring != null && recurring);

            if (paid != null && paid) {
                bill.setPaid(true);
                if (paidDateStr != null && !paidDateStr.isBlank()) {
                    bill.setPaidDate(LocalDate.parse(paidDateStr));
                }
            }

            bills.add(bill);
            addBillCard(bill);   
        }
    }
    private void loadSavingsFromFirestore() throws Exception {
        ApiFuture<QuerySnapshot> future = db.collection("savings")
                .whereEqualTo("userEmail", currentUserEmail)
                .get();
        QuerySnapshot snapshot = future.get();

        savingGoals.clear();
        if (savingsListBox != null) {
            savingsListBox.getChildren().clear();
        }

        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            String name = doc.getString("name");
            Double target = doc.getDouble("targetAmount");
            Double savedAmount = doc.getDouble("savedAmount");
            String deadlineStr = doc.getString("deadline");

            if (name == null || target == null) continue;

            LocalDate deadline = (deadlineStr != null && !deadlineStr.isBlank())
                    ? LocalDate.parse(deadlineStr)
                    : null;

            SavingGoal goal = new SavingGoal(name, target, deadline);
            if (savedAmount != null && savedAmount > 0) {
                goal.addMoney(savedAmount);    
            }

            savingGoals.add(goal);
            addSavingGoalCard(goal);          
        }

        if (savingsListBox != null) {
            boolean empty = savingsListBox.getChildren().isEmpty();
            setVisibleManaged(savingsEmptyStateBox, empty);
            setVisibleManaged(savingsListBox, !empty);
        }
    }

    

    @FXML
    private void onAddTransaction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Transaction_form.fxml"));
            VBox formRoot = loader.load();

            TransactionFormController controller = loader.getController();
            controller.setDashboardController(this);

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Add Transaction");
            popupStage.setScene(new Scene(formRoot));
            popupStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);

        if ("Income".equalsIgnoreCase(transaction.getType())) {
            totalIncome += transaction.getAmount();
        } else {
            totalExpenses += transaction.getAmount();
        }

        if (transactionsTable != null) {
            transactionsTable.setItems(transactions);
        }

        updateSummaryLabels();
        updateBudgetProgress();
        updateCharts();

        saveTransactionToFirestore(transaction);
    }

    private void saveTransactionToFirestore(Transaction t) {
        if (db == null) return;

        try {
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("type", t.getType());
            data.put("category", t.getCategory());
            data.put("description", t.getDescription());
            data.put("amount", t.getAmount());
            data.put("date", t.getDate().toString());

            if (currentUserEmail != null) {
                data.put("userEmail", currentUserEmail);
            }

            ApiFuture<DocumentReference> future =
                    db.collection("transactions").add(data);

            DocumentReference docRef = future.get();
            t.setId(docRef.getId());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void updateSummaryLabels() {
        double balance = totalIncome - totalExpenses;
        double budgetRemaining;
        if (totalBudgetLimit > 0) {
            budgetRemaining = totalBudgetLimit - totalExpenses;
        } else {
            budgetRemaining = balance;
        }

        if (totalIncomeLabel != null) {
            totalIncomeLabel.setText(String.format("$%.2f", totalIncome));
        }
        if (totalExpensesLabel != null) {
            totalExpensesLabel.setText(String.format("$%.2f", totalExpenses));
        }
        if (balanceLabel != null) {
            balanceLabel.setText(String.format("$%.2f", balance));
            if (balance < 0) {
                balanceLabel.setFill(Color.RED);
            } else {
                balanceLabel.setFill(Color.web("#27c62a"));
            }
        }
        if (budgetRemainingLabel != null) {
            budgetRemainingLabel.setText(String.format("$%.2f", budgetRemaining));
            if (budgetRemaining < 0) {
                budgetRemainingLabel.setFill(Color.RED);
            } else {
                budgetRemainingLabel.setFill(Color.BLACK);
            }
        }
        if (budgetPercentLabel != null) {
            if (totalBudgetLimit > 0) {
                double usedPercent = (totalExpenses / totalBudgetLimit) * 100.0;
                budgetPercentLabel.setText(String.format("%.0f%% used", usedPercent));
            } else {
                budgetPercentLabel.setText("0% used");
            }
        }
    }




    @FXML
    private void onAddBudget() {
        
        setVisibleManaged(budgetFormBox, true);
        setVisibleManaged(budgetsEmptyStateBox, false);
    }

    @FXML
    private void handleAddBudget() {
        String category = budgetCategoryCombo.getValue();
        String amountText = budgetAmountField.getText().trim();

        if (category == null || category.isBlank() || amountText.isBlank()) {
            showAlert("Budget", "Please select category and enter amount.");
            return;
        }


        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            showAlert("Budget", "Amount must be a valid number.");
            return;
        }

        if (amount <= 0) {
            showAlert("Budget", "Amount must be greater than zero.");
            return;
        }

        
        budgetLimits.put(category, budgetLimits.getOrDefault(category, 0.0) + amount);
        totalBudgetLimit = budgetLimits.values().stream().mapToDouble(Double::doubleValue).sum();

        addBudgetCard(category, budgetLimits.get(category));

        
        saveBudgetToFirestore(category, budgetLimits.get(category));

        budgetCategoryCombo.getSelectionModel().clearSelection();
        budgetAmountField.clear();

        setVisibleManaged(budgetFormBox, false);
        setVisibleManaged(budgetListBox, true);

        updateSummaryLabels();
        updateBudgetProgress();
        updateCharts();
    }



    private void deleteTransactionFromFirestore(Transaction t) {
        if (db == null) return;
        if (t.getId() == null || t.getId().isBlank()) return;

        try {
            db.collection("transactions").document(t.getId()).delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveBudgetToFirestore(String category, double limit) {
        if (db == null) return;

        try {
            Map<String, Object> data = new HashMap<>();
            data.put("category", category);
            data.put("limit", limit);

            if (currentUserEmail != null) {
                data.put("userEmail", currentUserEmail);
            }

            String docId = (currentUserEmail != null ? currentUserEmail + "_" : "") + category;

            db.collection("budgets").document(docId).set(data);  

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void cancelBudgetForm() {
        setVisibleManaged(budgetFormBox, false);
        if (budgetListBox.getChildren().isEmpty()) {
            setVisibleManaged(budgetsEmptyStateBox, true);
        }
    }

    private void addBudgetCard(String category, double limit) {
        if (budgetListBox == null) return;

        HBox card = new HBox(10);
        card.setStyle("-fx-background-color: #f7f7f7; -fx-padding: 10; -fx-background-radius: 8;");

        VBox textBox = new VBox(3);
        Label nameLabel = new Label(category);
        nameLabel.setStyle("-fx-font-weight: bold;");
        Label amountLabel = new Label(String.format("$0.00 of $%.2f", limit));
        Label percentLabel = new Label("0% used");
        textBox.getChildren().addAll(nameLabel, amountLabel, percentLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> deleteBudget(category, limit, card));

        card.setUserData(limit);

        card.getChildren().addAll(textBox, spacer, deleteButton);
        budgetListBox.getChildren().add(card);
    }
    private void deleteBudget(String category, double limit, HBox card) {
        if (budgetListBox != null) {
            budgetListBox.getChildren().remove(card);
        }
        budgetLimits.remove(category);
        totalBudgetLimit -= limit;
        if (budgetListBox != null && budgetListBox.getChildren().isEmpty()) {
            setVisibleManaged(budgetListBox, false);
            setVisibleManaged(budgetsEmptyStateBox, true);
        }
        updateSummaryLabels();
        updateBudgetProgress();
        deleteBudgetFromFirestore(category);
    }
    private void deleteBudgetFromFirestore(String category) {
        if (db == null) return;
        try {
            String docId = (currentUserEmail != null ? currentUserEmail + "_" : "") + category;
            db.collection("budgets").document(docId).delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void updateBudgetProgress() {
        if (budgetListBox == null) return;

        
        Map<String, Double> spentByCategory = new HashMap<>();
        for (Transaction t : transactions) {
            if (!"Expense".equalsIgnoreCase(t.getType())) continue;
            spentByCategory.merge(t.getCategory(), t.getAmount(), Double::sum);
        }

        for (javafx.scene.Node node : budgetListBox.getChildren()) {
            if (!(node instanceof HBox card)) continue;
            if (card.getChildren().isEmpty()) continue;

            VBox textBox = (VBox) card.getChildren().get(0);
            Label nameLabel = (Label) textBox.getChildren().get(0);
            Label amountLabel = (Label) textBox.getChildren().get(1);
            Label percentLabel = (Label) textBox.getChildren().get(2);

            String category = nameLabel.getText();
            double limit = ((Number) card.getUserData()).doubleValue();
            double spent = spentByCategory.getOrDefault(category, 0.0);

            double percent = limit > 0 ? (spent / limit) * 100.0 : 0.0;

            amountLabel.setText(String.format("$%.2f of $%.2f", spent, limit));
            percentLabel.setText(String.format("%.0f%% used", percent));
        }
    }

    

    @FXML
    private void showSavingsForm() {
        setVisibleManaged(savingsFormBox, true);
        setVisibleManaged(savingsEmptyStateBox, false);
    }

    @FXML
    private void handleAddSaving() {
        String name = savingsNameField.getText().trim();
        String amountText = savingsAmountField.getText().trim();
        LocalDate deadline = savingsDeadlinePicker.getValue();

        if (name.isEmpty() || amountText.isEmpty()) {
            showAlert("Savings", "Please enter goal name and amount.");
            return;
        }

        double target;
        try {
            target = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            showAlert("Savings", "Amount must be a valid number.");
            return;
        }

        SavingGoal goal = new SavingGoal(name, target, deadline);
        savingGoals.add(goal);

        
        addSavingGoalCard(goal);

        
        saveSavingGoalToFirestore(goal);

        
        savingsNameField.clear();
        savingsAmountField.clear();
        savingsDeadlinePicker.setValue(null);

        setVisibleManaged(savingsFormBox, false);
        setVisibleManaged(savingsListBox, true);

    }
    private double getCurrentBalance() {
        return totalIncome - totalExpenses;
    }

    private void updateSavingProgressLabel(SavingGoal goal, Label progressLabel) {
        double saved = goal.getSavedAmount();
        double target = goal.getTargetAmount();
        double percent = target > 0 ? (saved / target) * 100.0 : 0.0;

        progressLabel.setText(
                String.format("Saved: $%.2f of $%.2f (%.0f%%)", saved, target, percent)
        );
    }

    private void showAddMoneyDialog(SavingGoal goal, Label progressLabel) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Money");
        dialog.setHeaderText("Add money to: " + goal.getName());
        dialog.setContentText("Amount to add:");

        dialog.showAndWait().ifPresent(input -> {
            try {
                double amount = Double.parseDouble(input);
                if (amount <= 0) {
                    showAlert("Savings", "Amount must be greater than 0.");
                    return;
                }

                double balance = getCurrentBalance();
                if (amount > balance) {
                    showAlert("Savings",
                            "Not enough balance. Available: $" + String.format("%.2f", balance));
                    return;
                }

                
                Transaction tx = new Transaction(
                        "Expense",
                        "Savings",                     
                        "Saving goal: " + goal.getName(),
                        amount,
                        java.time.LocalDate.now()
                );
                addTransaction(tx); 

                
                goal.addMoney(amount);
                updateSavingProgressLabel(goal, progressLabel);

                
                saveSavingGoalToFirestore(goal);

            } catch (NumberFormatException ex) {
                showAlert("Savings", "Please enter a valid number.");
            }
        });
    }
    @FXML
    private void handleDeleteTransaction() {
        if (transactionsTable == null) return;

        Transaction selected = transactionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Transactions", "Please select a transaction to delete.");
            return;
        }

        if ("Income".equalsIgnoreCase(selected.getType())) {
            totalIncome -= selected.getAmount();
        } else {
            totalExpenses -= selected.getAmount();
        }

        transactions.remove(selected);

        updateSummaryLabels();
        updateBudgetProgress();
        updateCharts();

        deleteTransactionFromFirestore(selected);
    }


    private void addSavingGoalCard(SavingGoal goal) {
        if (savingsListBox == null) return;

        HBox card = new HBox(10);
        card.setStyle("-fx-background-color: #f7f7f7; -fx-padding: 10; -fx-background-radius: 8;");

        VBox textBox = new VBox(3);
        Label nameLabel = new Label(goal.getName());
        nameLabel.setStyle("-fx-font-weight: bold;");
        Label targetLabel = new Label(String.format("Target: $%.2f", goal.getTargetAmount()));
        Label deadlineLabel = new Label(goal.getDeadline() != null ? "Deadline: " + goal.getDeadline() : "No deadline");
        Label progressLabel = new Label();
        updateSavingProgressLabel(goal, progressLabel);
        textBox.getChildren().addAll(nameLabel, targetLabel, deadlineLabel, progressLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addMoneyButton = new Button("Add Money");
        addMoneyButton.setOnAction(e -> showAddMoneyDialog(goal, progressLabel));

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> deleteSavingGoal(goal, card));

        card.getChildren().addAll(textBox, spacer, addMoneyButton, deleteButton);
        savingsListBox.getChildren().add(card);
    }

    private void deleteSavingGoal(SavingGoal goal, HBox card) {
        savingGoals.remove(goal);
        if (savingsListBox != null) {
            savingsListBox.getChildren().remove(card);
            if (savingsListBox.getChildren().isEmpty()) {
                setVisibleManaged(savingsListBox, false);
                setVisibleManaged(savingsEmptyStateBox, true);
            }
        }
        deleteSavingGoalFromFirestore(goal);
    }

    private void deleteSavingGoalFromFirestore(SavingGoal goal) {
        if (db == null) return;
        try {
            String docId = (currentUserEmail != null ? currentUserEmail + "_" : "") + goal.getId();
            db.collection("savings").document(docId).delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveSavingGoalToFirestore(SavingGoal goal) {
        if (db == null) {
            System.out.println("Firestore not initialized; skipping save.");
            return;
        }

        try {
            Map<String, Object> data = new HashMap<>();
            data.put("name", goal.getName());
            data.put("targetAmount", goal.getTargetAmount());
            data.put("savedAmount", goal.getSavedAmount());
            data.put("deadline",
                    goal.getDeadline() != null ? goal.getDeadline().toString() : null);

            if (currentUserEmail != null) {
                data.put("userEmail", currentUserEmail);
            }

            String docId = (currentUserEmail != null ? currentUserEmail + "_" : "") + goal.getId();

            db.collection("savings").document(docId).set(data);  

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Firebase", "Error saving saving goal to Firestore.");
        }
    }





    @FXML
    private void cancelSavingsForm() {
        setVisibleManaged(savingsFormBox, false);
        if (savingsListBox.getChildren().isEmpty()) {
            setVisibleManaged(savingsEmptyStateBox, true);
        }
    }

    

    private void updateReportView() {
        boolean showCategory = true;
        if (reportViewToggle != null && reportViewToggle.getSelectedToggle() != null) {
            showCategory = reportViewToggle.getSelectedToggle() == byCategoryToggle;
        }

        setVisibleManaged(categoryChart, showCategory);
        setVisibleManaged(trendsChart, !showCategory);
    }

    private void updateCharts() {
        updateCategoryChart();
        updateTrendsChart();
    }

    private void updateCategoryChart() {
        if (categoryChart == null) return;

        categoryChart.getData().clear();

        Map<String, Double> byCategory = new HashMap<>();
        for (Transaction t : transactions) {
            if (!"Expense".equalsIgnoreCase(t.getType())) continue;
            byCategory.merge(t.getCategory(), t.getAmount(), Double::sum);
        }

        for (Map.Entry<String, Double> e : byCategory.entrySet()) {
            categoryChart.getData().add(new PieChart.Data(e.getKey(), e.getValue()));
        }
    }

    private void updateTrendsChart() {
        if (trendsChart == null) return;

        trendsChart.getData().clear();

        Map<String, Double> byDate = new HashMap<>();
        for (Transaction t : transactions) {
            if (!"Expense".equalsIgnoreCase(t.getType())) continue;
            String key = t.getDate().toString();
            byDate.merge(key, t.getAmount(), Double::sum);
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Expenses");

        byDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> series.getData().add(
                        new XYChart.Data<>(e.getKey(), e.getValue()))
                );

        trendsChart.getData().add(series);
    }



    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("login_view.fxml"));
            Parent root = loader.load();

            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddBill() {
        String name = billNameField.getText().trim();
        String amountText = billAmountField.getText().trim();
        String category = billCategoryCombo.getValue();
        LocalDate dueDate = billDueDatePicker.getValue();
        boolean recurring = billRecurringCheckBox.isSelected();

        if (name.isEmpty() || amountText.isEmpty() || category == null) {
            showAlert("Bills", "Please fill in name, amount, and category.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            showAlert("Bills", "Amount must be a valid number.");
            return;
        }

        Bill bill = new Bill(name, amount, category, dueDate, recurring);
        bills.add(bill);
        addBillCard(bill);
        saveBillToFirestore(bill);
        
        billNameField.clear();
        billAmountField.clear();
        billCategoryCombo.getSelectionModel().clearSelection();
        billDueDatePicker.setValue(null);
        billRecurringCheckBox.setSelected(false);
    }
    private void saveBillToFirestore(Bill bill) {
        if (db == null) return;

        try {
            Map<String, Object> data = new HashMap<>();
            data.put("name", bill.getName());
            data.put("amount", bill.getAmount());
            data.put("category", bill.getCategory());
            data.put("dueDate",
                    bill.getDueDate() != null ? bill.getDueDate().toString() : null);
            data.put("recurring", bill.isRecurring());
            data.put("paid", bill.isPaid());
            data.put("paidDate",
                    bill.getPaidDate() != null ? bill.getPaidDate().toString() : null);

            if (currentUserEmail != null) {
                data.put("userEmail", currentUserEmail);
            }

            
            String keyDate = bill.getDueDate() != null ? bill.getDueDate().toString() : "noDate";
            String docId = (currentUserEmail != null ? currentUserEmail + "_" : "")
                    + bill.getName() + "_" + keyDate;

            db.collection("bills").document(docId).set(data);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void cancelBill() {
        billNameField.clear();
        billAmountField.clear();
        billCategoryCombo.getSelectionModel().clearSelection();
        billDueDatePicker.setValue(null);
        billRecurringCheckBox.setSelected(false);
    }
    private void payBill(Bill bill, Label statusLabel) {
        if (bill.isPaid()) {
            showAlert("Bills", "This bill is already paid.");
            return;
        }

        double amount = bill.getAmount();
        double balance = getCurrentBalance();

        if (amount > balance) {
            showAlert("Bills",
                    "Not enough balance to pay this bill. Available: $" + String.format("%.2f", balance));
            return;
        }

        
        Transaction tx = new Transaction(
                "Expense",
                bill.getCategory(),
                "Bill payment: " + bill.getName(),
                amount,
                java.time.LocalDate.now()
        );
        addTransaction(tx);


        bill.setPaid(true);
        bill.setPaidDate(java.time.LocalDate.now());
        statusLabel.setText("Status: PAID");


        saveBillToFirestore(bill);
    }

    private void addBillCard(Bill bill) {
        if (billsListBox == null) return;

        HBox card = new HBox(10);
        card.setStyle("-fx-background-color: #f7f7f7; -fx-padding: 10; -fx-background-radius: 8;");

        VBox textBox = new VBox(3);
        Label nameLabel = new Label(bill.getName());
        nameLabel.setStyle("-fx-font-weight: bold;");
        Label amountLabel = new Label(String.format("$%.2f", bill.getAmount()));
        Label categoryLabel = new Label("Category: " + bill.getCategory());
        String dueStr = bill.getDueDate() != null ? bill.getDueDate().toString() : "No due date";
        Label dueLabel = new Label("Due: " + dueStr);
        Label statusLabel = new Label(bill.isPaid() ? "Status: PAID" : "Status: UNPAID");
        textBox.getChildren().addAll(nameLabel, amountLabel, categoryLabel, dueLabel, statusLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button payButton = new Button("Pay");
        payButton.setOnAction(e -> payBill(bill, statusLabel));

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> deleteBill(bill, card));

        card.getChildren().addAll(textBox, spacer, payButton, deleteButton);
        billsListBox.getChildren().add(card);
    }
    private void deleteBill(Bill bill, HBox card) {
        bills.remove(bill);
        if (billsListBox != null) {
            billsListBox.getChildren().remove(card);
        }
        deleteBillFromFirestore(bill);
    }
    private void deleteBillFromFirestore(Bill bill) {
        if (db == null) return;
        try {
            String keyDate = bill.getDueDate() != null ? bill.getDueDate().toString() : "noDate";
            String docId = (currentUserEmail != null ? currentUserEmail + "_" : "") + bill.getName() + "_" + keyDate;
            db.collection("bills").document(docId).delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

