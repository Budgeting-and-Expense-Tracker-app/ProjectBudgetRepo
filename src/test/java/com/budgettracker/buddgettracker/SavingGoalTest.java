package com.budgettracker.buddgettracker;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SavingGoalTest {

    @Test
    void newSavingGoalStartsWithZeroSaved() {
        LocalDate deadline = LocalDate.of(2025, 12, 31);
        SavingGoal goal = new SavingGoal("Emergency Fund", 500.0, deadline);

        assertEquals("Emergency Fund", goal.getName());
        assertEquals(500.0, goal.getTargetAmount());
        assertEquals(0.0, goal.getSavedAmount());
        assertEquals(deadline, goal.getDeadline());
    }

    @Test
    void addMoneyAccumulatesSavedAmount() {
        SavingGoal goal = new SavingGoal("Vacation", 1000.0, null);

        goal.addMoney(100.0);
        goal.addMoney(50.0);

        assertEquals(150.0, goal.getSavedAmount());
    }
}
