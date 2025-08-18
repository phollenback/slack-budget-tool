package com.slackbuidler.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TransactionModel Tests")
public class TransactionModelTest {

    @Nested
    @DisplayName("Transaction Type Normalization Tests")
    class TransactionTypeNormalizationTests {
        
        @Test
        @DisplayName("should normalize expense type correctly")
        void shouldNormalizeExpenseTypeCorrectly() {
            TransactionModel transaction = new TransactionModel(
                null, 100.0, "Test Vendor", "expense", "2024-01-01", "food", "test notes", 1
            );
            
            assertNotNull(transaction.getType());
            assertEquals("expense", transaction.getType());
            assertTrue(transaction.hasValidType());
        }

        @Test
        @DisplayName("should normalize income type correctly")
        void shouldNormalizeIncomeTypeCorrectly() {
            TransactionModel transaction = new TransactionModel(
                null, 100.0, "Test Vendor", "income", "2024-01-01", "food", "test notes", 1
            );
            
            assertNotNull(transaction.getType());
            assertEquals("income", transaction.getType());
            assertTrue(transaction.hasValidType());
        }
    }
} 