package com.slackbuidler.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.slackbuidler.models.TransactionModel;
import com.slackbuidler.models.TransactionType;

@DisplayName("TransactionService Tests")
public class TransactionServiceTest {

    @Nested
    @DisplayName("Transaction Type Validation Tests")
    class TransactionTypeValidationTests {
        
        @Test
        @DisplayName("should validate TransactionType enum accessibility")
        void shouldValidateTransactionTypeEnumAccessibility() {
            assertNotNull(TransactionType.EXPENSE);
            assertEquals("expense", TransactionType.EXPENSE.getDisplayName());
        }
        
        @Test
        @DisplayName("should validate transaction type strings correctly")
        void shouldValidateTransactionTypeStringsCorrectly() {
            assertTrue(TransactionType.isValidTransactionType("expense"));
            assertTrue(TransactionType.isValidTransactionType("income"));
            assertFalse(TransactionType.isValidTransactionType("invalid"));
        }
        
        @Test
        @DisplayName("should return all display names correctly")
        void shouldReturnAllDisplayNamesCorrectly() {
            String[] names = TransactionType.getAllDisplayNames();
            assertNotNull(names);
            assertTrue(names.length > 0);
            
            // Verify specific names are present without console output
            assertTrue(contains(names, "expense"));
            assertTrue(contains(names, "income"));
            assertTrue(contains(names, "purchase"));
            assertTrue(contains(names, "refund"));
            assertTrue(contains(names, "transfer"));
            assertTrue(contains(names, "other"));
            
            // Verify exact count
            assertEquals(6, names.length);
        }
    }

    @Nested
    @DisplayName("Transaction Model Validation Tests")
    class TransactionModelValidationTests {
        
        @Test
        @DisplayName("should create valid TransactionModel with expense type")
        void shouldCreateValidTransactionModelWithExpenseType() {
            TransactionModel transaction = new TransactionModel(
                null, 100.0, "Test Vendor", "expense", "2024-01-01", "Food & Dining", "test notes", 1
            );
            
            assertNotNull(transaction.getType());
            assertEquals("expense", transaction.getType());
            assertTrue(transaction.hasValidType());
            
            // Test that TransactionType validation works on this transaction
            assertTrue(TransactionType.isValidTransactionType(transaction.getType()));
        }
    }
    
    private boolean contains(String[] array, String value) {
        for (String item : array) {
            if (item.equals(value)) {
                return true;
            }
        }
        return false;
    }
} 