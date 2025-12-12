package com.budgettracker.buddgettracker;


import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

   public class TransactionTest {

        @Test
        void createsTransactionWithProvidedValues() {
            LocalDate date = LocalDate.of(2025, 1, 15);
            Transaction t = new Transaction("Income", "Food and Dining", "Test salary", 123.45, date);

            assertEquals("Income", t.getType());
            assertEquals("Food and Dining", t.getCategory());
            assertEquals("Test salary", t.getDescription());
            assertEquals(123.45, t.getAmount());
            assertEquals(date, t.getDate());
        }

        @Test
        void canUpdateTransactionFields() {
            LocalDate date1 = LocalDate.of(2025, 1, 1);
            LocalDate date2 = LocalDate.of(2025, 2, 1);

            Transaction t = new Transaction("Expense", "Bills and Utilities", "Electricity", 80.0, date1);

            t.setType("Income");
            t.setCategory("Other Expenses");
            t.setDescription("Refund");
            t.setAmount(50.0);
            t.setDate(date2);

            assertEquals("Income", t.getType());
            assertEquals("Other Expenses", t.getCategory());
            assertEquals("Refund", t.getDescription());
            assertEquals(50.0, t.getAmount());
            assertEquals(date2, t.getDate());
        }
    }


