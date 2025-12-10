package com.budgettracker.buddgettracker;

import java.time.LocalDate;

public class Bill {
    private final String name;
    private final double amount;
    private final String category;
    private final LocalDate dueDate;
    private final boolean recurring;

    private boolean paid;
    private LocalDate paidDate;

    public Bill(String name, double amount, String category,
                LocalDate dueDate, boolean recurring) {
        this.name = name;
        this.amount = amount;
        this.category = category;
        this.dueDate = dueDate;
        this.recurring = recurring;
        this.paid = false;
        this.paidDate = null;
    }

    public String getName() { return name; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public LocalDate getDueDate() { return dueDate; }
    public boolean isRecurring() { return recurring; }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }

    public LocalDate getPaidDate() { return paidDate; }
    public void setPaidDate(LocalDate paidDate) { this.paidDate = paidDate; }
}

