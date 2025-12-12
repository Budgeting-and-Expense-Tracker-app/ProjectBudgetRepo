package com.budgettracker.buddgettracker;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class BillTest {

    @Test
    void createsBillWithProvidedValues() {
        LocalDate dueDate = LocalDate.of(2025, 3, 10);
        Bill bill = new Bill("Internet", 60.0, "Bills and Utilities", dueDate, true);

        assertEquals("Internet", bill.getName());
        assertEquals(60.0, bill.getAmount());
        assertEquals("Bills and Utilities", bill.getCategory());
        assertEquals(dueDate, bill.getDueDate());
        assertTrue(bill.isRecurring());
        assertFalse(bill.isPaid());
        assertNull(bill.getPaidDate());
    }

    @Test
    void markBillAsPaidSetsStatusAndDate() {
        LocalDate dueDate = LocalDate.of(2025, 3, 10);
        Bill bill = new Bill("Internet", 60.0, "Bills and Utilities", dueDate, false);

        LocalDate paidDate = LocalDate.of(2025, 3, 5);
        bill.setPaid(true);
        bill.setPaidDate(paidDate);

        assertTrue(bill.isPaid());
        assertEquals(paidDate, bill.getPaidDate());
    }
}
