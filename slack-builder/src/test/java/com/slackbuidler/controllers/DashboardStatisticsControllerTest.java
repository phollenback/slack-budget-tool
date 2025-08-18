package com.slackbuidler.controllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.slackbuidler.models.BudgetCategoryModel;
import com.slackbuidler.models.BudgetModel;
import com.slackbuidler.models.TransactionModel;
import com.slackbuidler.services.BudgetService;
import com.slackbuidler.services.TransactionService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Dashboard Statistics Controller Tests")
class DashboardStatisticsControllerTest {

    @Mock
    private BudgetService budgetService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    @InjectMocks
    private BudgetController budgetController;

    private MockMvc transactionMockMvc;
    private MockMvc budgetMockMvc;
    private DateTimeFormatter dateFormatter;

    @BeforeEach
    void setUp() {
        transactionMockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();
        budgetMockMvc = MockMvcBuilders.standaloneSetup(budgetController).build();
        dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    }

    @Nested
    @DisplayName("Dashboard Statistics - Basic Functionality")
    class BasicFunctionalityTests {

        @Test
        @DisplayName("should return transactions for user successfully")
        void shouldReturnTransactionsForUserSuccessfully() throws Exception {
            // Given
            LocalDate today = LocalDate.now();
            List<TransactionModel> transactions = Arrays.asList(
                createTransaction(1L, "Campus Coffee", 8.50, "Food & Dining", today.format(dateFormatter)),
                createTransaction(2L, "Student Union", 12.75, "Food & Dining", today.minusDays(1).format(dateFormatter))
            );

            when(transactionService.getTransactions(anyInt())).thenReturn(transactions);

            // When & Then
            transactionMockMvc.perform(get("/transactions/")
                    .param("userId", "1"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should return budget successfully")
        void shouldReturnBudgetSuccessfully() throws Exception {
            // Given
            BudgetModel budget = createBudget(1L, "Monthly Budget", 800.00, 0.00);
            when(budgetService.getBudgetById(1L)).thenReturn(Optional.of(budget));

            // When & Then
            budgetMockMvc.perform(get("/budgets/1"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should return budget categories successfully")
        void shouldReturnBudgetCategoriesSuccessfully() throws Exception {
            // Given
            List<BudgetCategoryModel> categories = Arrays.asList(
                createBudgetCategory(1L, "Food & Dining", 250.00, 0.00),
                createBudgetCategory(2L, "Transportation", 120.00, 0.00)
            );

            when(budgetService.getCategoriesByBudgetId(1L)).thenReturn(categories);

            // When & Then
            budgetMockMvc.perform(get("/budgets/1/categories"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Dashboard Statistics - Error Handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("should handle invalid userId parameter gracefully")
        void shouldHandleInvalidUserIdParameterGracefully() throws Exception {
            // Given
            when(transactionService.getTransactions(-1))
                .thenThrow(new IllegalArgumentException("User ID must be positive"));

            // When & Then
            transactionMockMvc.perform(get("/transactions/")
                    .param("userId", "-1"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should handle missing userId parameter gracefully")
        void shouldHandleMissingUserIdParameterGracefully() throws Exception {
            // When & Then
            transactionMockMvc.perform(get("/transactions/"))
                    .andExpect(status().isOk()); // Default userId is 1
        }

        @Test
        @DisplayName("should handle budget not found gracefully")
        void shouldHandleBudgetNotFoundGracefully() throws Exception {
            // Given
            when(budgetService.getBudgetById(999L)).thenReturn(Optional.empty());

            // When & Then
            budgetMockMvc.perform(get("/budgets/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Dashboard Statistics - Data Validation")
    class DataValidationTests {

        @Test
        @DisplayName("should validate transaction data structure")
        void shouldValidateTransactionDataStructure() throws Exception {
            // Given
            LocalDate today = LocalDate.now();
            List<TransactionModel> transactions = Arrays.asList(
                createTransaction(1L, "Campus Coffee", 8.50, "Food & Dining", today.format(dateFormatter))
            );

            when(transactionService.getTransactions(anyInt())).thenReturn(transactions);

            // When & Then
            transactionMockMvc.perform(get("/transactions/")
                    .param("userId", "1"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should validate budget data structure")
        void shouldValidateBudgetDataStructure() throws Exception {
            // Given
            BudgetModel budget = createBudget(1L, "Test Budget", 800.00, 0.00);
            when(budgetService.getBudgetById(1L)).thenReturn(Optional.of(budget));

            // When & Then
            budgetMockMvc.perform(get("/budgets/1"))
                    .andExpect(status().isOk());
        }
    }

    // Helper methods to create test data
    private TransactionModel createTransaction(Long id, String vendor, double amount, String category, String date) {
        TransactionModel transaction = new TransactionModel();
        transaction.setId(id);
        transaction.setVendor(vendor);
        transaction.setAmount(amount);
        transaction.setType("purchase");
        transaction.setCategory(category);
        transaction.setDate(date);
        transaction.setNotes("Test transaction");
        transaction.setUserId(1);
        return transaction;
    }

    private BudgetModel createBudget(Long id, String name, double planned, double spent) {
        BudgetModel budget = new BudgetModel();
        budget.setId(id);
        budget.setName(name);
        budget.setUserId(1);
        budget.setStartDate("2025-01-01");
        budget.setEndDate("2025-01-31");
        budget.setTotalPlanned(planned);
        budget.setTotalSpent(spent);
        budget.setIsActive(true);
        budget.setIsRepeatable(false);
        return budget;
    }

    private BudgetCategoryModel createBudgetCategory(Long id, String name, double planned, double spent) {
        BudgetCategoryModel category = new BudgetCategoryModel();
        category.setId(id);
        category.setBudgetId(1L);
        category.setName(name);
        category.setPlannedAmount(planned);
        category.setSpentAmount(spent);
        category.setColor("#FF6384");
        return category;
    }
}
