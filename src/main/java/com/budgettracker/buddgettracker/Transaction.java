package com.budgettracker.buddgettracker;

import java.time.LocalDate;

public class Transaction {
    private final String type;        // "Income" or "Expense"
    private final String category;
    private final String description;
    private final double amount;
    private final LocalDate date;

    public Transaction(String type, String category, String description,
                       double amount, LocalDate date) {
        this.type = type;
        this.category = category;
        this.description = description;
        this.amount = amount;
        this.date = date;
    }

    public String getType() { return type; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public LocalDate getDate() { return date; }
}
