package com.slackbuidler.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TransactionType Tests")
public class TransactionTypeTest {

    @Nested
    @DisplayName("Display Name Resolution Tests")
    class DisplayNameResolutionTests {
        
        @Test
        @DisplayName("should resolve exact display name matches")
        void shouldResolveExactDisplayNameMatches() {
            assertEquals(TransactionType.PURCHASE, TransactionType.fromDisplayName("purchase"));
            assertEquals(TransactionType.REFUND, TransactionType.fromDisplayName("refund"));
            assertEquals(TransactionType.EXPENSE, TransactionType.fromDisplayName("expense"));
            assertEquals(TransactionType.INCOME, TransactionType.fromDisplayName("income"));
            assertEquals(TransactionType.TRANSFER, TransactionType.fromDisplayName("transfer"));
            assertEquals(TransactionType.OTHER, TransactionType.fromDisplayName("other"));
        }
        
        @Test
        @DisplayName("should resolve case insensitive matches")
        void shouldResolveCaseInsensitiveMatches() {
            assertEquals(TransactionType.PURCHASE, TransactionType.fromDisplayName("PURCHASE"));
            assertEquals(TransactionType.REFUND, TransactionType.fromDisplayName("Refund"));
        }
        
        @Test
        @DisplayName("should resolve common variations correctly")
        void shouldResolveCommonVariationsCorrectly() {
            assertEquals(TransactionType.PURCHASE, TransactionType.fromDisplayName("buy"));
            assertEquals(TransactionType.PURCHASE, TransactionType.fromDisplayName("shopping"));
            assertEquals(TransactionType.REFUND, TransactionType.fromDisplayName("return"));
            assertEquals(TransactionType.EXPENSE, TransactionType.fromDisplayName("cost"));
            assertEquals(TransactionType.INCOME, TransactionType.fromDisplayName("salary"));
            assertEquals(TransactionType.TRANSFER, TransactionType.fromDisplayName("move"));
            assertEquals(TransactionType.OTHER, TransactionType.fromDisplayName("misc"));
        }
        
        @Test
        @DisplayName("should handle invalid display names gracefully")
        void shouldHandleInvalidDisplayNamesGracefully() {
            assertNull(TransactionType.fromDisplayName("invalid"));
            assertNull(TransactionType.fromDisplayName(""));
            assertNull(TransactionType.fromDisplayName(null));
        }
    }

    @Nested
    @DisplayName("Transaction Type Validation Tests")
    class TransactionTypeValidationTests {
        
        @Test
        @DisplayName("should validate all valid transaction types")
        void shouldValidateAllValidTransactionTypes() {
            assertTrue(TransactionType.isValidTransactionType("purchase"));
            assertTrue(TransactionType.isValidTransactionType("refund"));
            assertTrue(TransactionType.isValidTransactionType("expense"));
            assertTrue(TransactionType.isValidTransactionType("income"));
            assertTrue(TransactionType.isValidTransactionType("transfer"));
            assertTrue(TransactionType.isValidTransactionType("other"));
        }
        
        @Test
        @DisplayName("should reject invalid transaction types")
        void shouldRejectInvalidTransactionTypes() {
            assertFalse(TransactionType.isValidTransactionType("invalid"));
            assertFalse(TransactionType.isValidTransactionType(""));
            assertFalse(TransactionType.isValidTransactionType(null));
        }
    }

    @Nested
    @DisplayName("Display Name Retrieval Tests")
    class DisplayNameRetrievalTests {
        
        @Test
        @DisplayName("should return all display names correctly")
        void shouldReturnAllDisplayNamesCorrectly() {
            String[] names = TransactionType.getAllDisplayNames();
            assertEquals(6, names.length);
            
            // Check that all expected names are present
            assertTrue(contains(names, "purchase"));
            assertTrue(contains(names, "refund"));
            assertTrue(contains(names, "expense"));
            assertTrue(contains(names, "income"));
            assertTrue(contains(names, "transfer"));
            assertTrue(contains(names, "other"));
        }

        @Test
        @DisplayName("should return correct display name for each type")
        void shouldReturnCorrectDisplayNameForEachType() {
            assertEquals("purchase", TransactionType.PURCHASE.getDisplayName());
            assertEquals("refund", TransactionType.REFUND.getDisplayName());
            assertEquals("expense", TransactionType.EXPENSE.getDisplayName());
            assertEquals("income", TransactionType.INCOME.getDisplayName());
            assertEquals("transfer", TransactionType.TRANSFER.getDisplayName());
            assertEquals("other", TransactionType.OTHER.getDisplayName());
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