package com.budgettracker.buddgettracker;
import java.time.LocalDate;
public class SavingGoal {
    private final String id;          
    private final String name;
    private final double targetAmount;
    private double savedAmount;
    private final LocalDate deadline;

    public SavingGoal(String name, double targetAmount, LocalDate deadline) {
        this.id = name;              
        this.name = name;
        this.targetAmount = targetAmount;
        this.savedAmount = 0.0;
        this.deadline = deadline;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getTargetAmount() { return targetAmount; }
    public double getSavedAmount() { return savedAmount; }
    public LocalDate getDeadline() { return deadline; }

    public void addMoney(double amount) {
        this.savedAmount += amount;
    }
}

