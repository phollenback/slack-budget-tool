package com.slackbuidler.models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BudgetModelTest {

    private BudgetModel budget;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @BeforeEach
    void setUp() {
        budget = new BudgetModel();
        budget.setId(1L);
        budget.setName("Test Budget");
        budget.setUserId(1);
        budget.setTotalPlanned(1000.0);
        budget.setTotalSpent(0.0);
    }

    @Test
    void testDailySpendingAllowanceForFutureBudget() {
        // Budget starts tomorrow and ends in 10 days
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate endDate = tomorrow.plusDays(9);
        
        budget.setStartDate(tomorrow.format(formatter));
        budget.setEndDate(endDate.format(formatter));
        
        Double dailyAllowance = budget.getDailySpendingAllowance();
        assertNotNull(dailyAllowance);
        assertEquals(100.0, dailyAllowance, 0.01); // 1000 / 10 days
    }

    @Test
    void testDailySpendingAllowanceForCurrentBudget() {
        // Budget started 5 days ago and ends in 5 days (10 day total)
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(5);
        
        budget.setStartDate(startDate.format(formatter));
        budget.setEndDate(endDate.format(formatter));
        budget.setTotalSpent(300.0); // Spent 300, remaining 700
        
        Double dailyAllowance = budget.getDailySpendingAllowance();
        assertNotNull(dailyAllowance);
        // Remaining days: 6 (including today), so 700 / 6 = 116.67
        assertEquals(116.67, dailyAllowance, 0.01);
    }

    @Test
    void testDailySpendingAllowanceForOverBudget() {
        // Budget started 5 days ago and ends in 5 days
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(5);
        
        budget.setStartDate(startDate.format(formatter));
        budget.setEndDate(endDate.format(formatter));
        budget.setTotalSpent(1200.0); // Spent 1200, over budget by 200
        
        Double dailyAllowance = budget.getDailySpendingAllowance();
        assertNotNull(dailyAllowance);
        assertEquals(0.0, dailyAllowance, 0.01); // Over budget, no daily allowance
    }

    @Test
    void testDailySpendingAllowanceForEndedBudget() {
        // Budget ended yesterday
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now().minusDays(1);
        
        budget.setStartDate(startDate.format(formatter));
        budget.setEndDate(endDate.format(formatter));
        
        Double dailyAllowance = budget.getDailySpendingAllowance();
        assertNotNull(dailyAllowance);
        assertEquals(0.0, dailyAllowance, 0.01); // Budget ended, no daily allowance
    }

    @Test
    void testDaysRemaining() {
        // Budget ends in 7 days
        LocalDate startDate = LocalDate.now().minusDays(3);
        LocalDate endDate = LocalDate.now().plusDays(7);
        
        budget.setStartDate(startDate.format(formatter));
        budget.setEndDate(endDate.format(formatter));
        
        Long daysRemaining = budget.getDaysRemaining();
        assertNotNull(daysRemaining);
        assertEquals(8L, daysRemaining); // Including today
    }

    @Test
    void testDaysElapsed() {
        // Budget started 5 days ago
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(5);
        
        budget.setStartDate(startDate.format(formatter));
        budget.setEndDate(endDate.format(formatter));
        
        Long daysElapsed = budget.getDaysElapsed();
        assertNotNull(daysElapsed);
        assertEquals(6L, daysElapsed); // Including today
    }

    @Test
    void testInvalidDates() {
        budget.setStartDate("invalid-date");
        budget.setEndDate("invalid-date");
        
        assertNull(budget.getDailySpendingAllowance());
        assertNull(budget.getDaysRemaining());
        assertNull(budget.getDaysElapsed());
    }
}
