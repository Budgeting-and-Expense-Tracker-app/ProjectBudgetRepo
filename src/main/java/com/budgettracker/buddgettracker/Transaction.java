package com.budgettracker.buddgettracker;

import java.time.LocalDate;

public class Transaction {

    private String type;
    private String category;
    private String description;
    private double amount;
    private LocalDate date;
    private String id;

    public Transaction(String type, String category, String description, double amount, LocalDate date) {
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

    public void setType(String type) { this.type = type; }
    public void setCategory(String category) { this.category = category; }
    public void setDescription(String description) { this.description = description; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setDate(LocalDate date) { this.date = date; }


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}

