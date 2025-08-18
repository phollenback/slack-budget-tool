package com.slackbuidler.controllers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.slackbuidler.models.TransactionModel;
import com.slackbuidler.services.TransactionService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.util.Optional;

/**
 * Test class for TransactionController
 */
@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;
    
    private TransactionController transactionController;

    @BeforeEach
    void setUp() {
        transactionController = new TransactionController(transactionService);
    }

    @Nested
    @DisplayName("GET /transactions/")
    class GetTransactionsTests {

        @Test
        @DisplayName("should return transactions as list")
        void shouldReturnTransactionsAsList() {
            // Arrange
            TransactionModel mockTransaction = new TransactionModel(
                1L, 100.0, "Test Vendor", "expense", "2024-01-01", "food", "test notes", 1
            );
            when(transactionService.getTransactions(1)).thenReturn(List.of(mockTransaction));
            
            // Act
            ResponseEntity<List<TransactionModel>> response = transactionController.getTransactions(1);
            
            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).isNotEmpty();
        }

        @Test
        @DisplayName("should handle empty transactions list")
        void shouldHandleEmptyTransactionsList() {
            // Arrange
            when(transactionService.getTransactions(999)).thenReturn(List.of());
            
            // Act
            ResponseEntity<List<TransactionModel>> response = transactionController.getTransactions(999); // Non-existent user
            
            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("should handle non-existent user gracefully")
        void shouldHandleNonExistentUserGracefully() {
            // Arrange
            int nonExistentUserId = 99;
            when(transactionService.getTransactions(nonExistentUserId)).thenReturn(List.of());
            
            // Act
            ResponseEntity<List<TransactionModel>> response = transactionController.getTransactions(nonExistentUserId);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("should handle non-existent transaction id gracefully")
        void shouldHandleNonExistentTransactionIdGracefully() {
            // Arrange
            Long nonExistentId = 999L;
            
            // Act
            ResponseEntity<TransactionModel> response = transactionController.getTransactionById(nonExistentId);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNull();
        }

        @Test
        @DisplayName("should handle invalid transaction id gracefully")
        void shouldHandleInvalidTransactionIdGracefully() {
            // Arrange
            Long invalidId = 0L;
            
            // Act
            ResponseEntity<TransactionModel> response = transactionController.getTransactionById(invalidId);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNull();
        }

        @Test
        @DisplayName("should handle negative transaction id gracefully")
        void shouldHandleNegativeTransactionIdGracefully() {
            // Arrange
            Long negativeId = -1L;
            
            // Act
            ResponseEntity<TransactionModel> response = transactionController.getTransactionById(negativeId);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNull();
        }

        @Test
        @DisplayName("should handle invalid user ID")
        void shouldHandleInvalidUserId() {
            // Arrange
            when(transactionService.getTransactions(-1)).thenThrow(new IllegalArgumentException("Invalid user ID"));
            
            // Act
            ResponseEntity<List<TransactionModel>> response = transactionController.getTransactions(-1);
            
            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("GET /transactions/{id}")
    class GetTransactionByIdTests {

        @Test
        @DisplayName("should return transaction by ID")
        void shouldReturnTransactionById() {
            // Arrange
            TransactionModel mockTransaction = new TransactionModel(
                1L, 100.0, "Test Vendor", "expense", "2024-01-01", "food", "test notes", 1
            );
            when(transactionService.getTransactionById(1L)).thenReturn(Optional.of(mockTransaction));
            
            // Act
            ResponseEntity<TransactionModel> response = transactionController.getTransactionById(1L);
            
            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should return 404 for non-existent transaction")
        void shouldReturn404ForNonExistentTransaction() {
            // Arrange
            when(transactionService.getTransactionById(999L)).thenReturn(Optional.empty());
            
            // Act
            ResponseEntity<TransactionModel> response = transactionController.getTransactionById(999L);
            
            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("POST /transactions/")
    class CreateTransactionTests {

        @Test
        @DisplayName("should create a transaction successfully")
        void shouldCreateTransactionSuccessfully() {
            // Given
            TransactionModel transaction = new TransactionModel(
                null, 100.0, "Test Vendor", "expense", "2024-01-01", "food", "test notes", 1
            );
            
            TransactionModel createdTransaction = new TransactionModel(
                1L, 100.0, "Test Vendor", "expense", "2024-01-01", "food", "test notes", 1
            );
            
            // Mock the service to return the created transaction
            when(transactionService.createTransaction(any(TransactionModel.class))).thenReturn(createdTransaction);

            // When
            ResponseEntity<TransactionModel> response = transactionController.createTransaction(transaction);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getVendor()).isEqualTo("Test Vendor");
        }

        @Test
        @DisplayName("should handle null transaction input")
        void shouldHandleNullTransactionInput() {
            // When
            ResponseEntity<TransactionModel> response = transactionController.createTransaction(null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("should handle invalid date format")
        void shouldHandleInvalidDateFormat() {
            // Given
            TransactionModel transaction = new TransactionModel(
                1L, 100.0, "Test Vendor", "expense", "invalid-date", "food", "test notes", 1
            );
            
            when(transactionService.createTransaction(any(TransactionModel.class)))
                .thenThrow(new IllegalArgumentException("Invalid date format"));

            // When
            ResponseEntity<TransactionModel> response = transactionController.createTransaction(transaction);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("should handle null vendor")
        void shouldHandleNullVendor() {
            // Given
            TransactionModel transaction = new TransactionModel(
                1L, 100.0, null, "expense", "2024-01-01", "food", "test notes", 1
            );
            
            when(transactionService.createTransaction(any(TransactionModel.class)))
                .thenThrow(new IllegalArgumentException("Vendor cannot be null"));

            // When
            ResponseEntity<TransactionModel> response = transactionController.createTransaction(transaction);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("should handle empty vendor")
        void shouldHandleEmptyVendor() {
            // Given
            TransactionModel transaction = new TransactionModel(
                1L, 100.0, "", "expense", "2024-01-01", "food", "test notes", 1
            );
            
            when(transactionService.createTransaction(any(TransactionModel.class)))
                .thenThrow(new IllegalArgumentException("Vendor cannot be empty"));

            // When
            ResponseEntity<TransactionModel> response = transactionController.createTransaction(transaction);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("should handle null type")
        void shouldHandleNullType() {
            // Given
            TransactionModel transaction = new TransactionModel(
                1L, 100.0, "Test Vendor", null, "2024-01-01", "food", "test notes", 1
            );
            
            when(transactionService.createTransaction(any(TransactionModel.class)))
                .thenThrow(new IllegalArgumentException("Type cannot be null"));

            // When
            ResponseEntity<TransactionModel> response = transactionController.createTransaction(transaction);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("should handle empty type")
        void shouldHandleEmptyType() {
            // Given
            TransactionModel transaction = new TransactionModel(
                1L, 100.0, "Test Vendor", "", "2024-01-01", "food", "test notes", 1
            );
            
            when(transactionService.createTransaction(any(TransactionModel.class)))
                .thenThrow(new IllegalArgumentException("Type cannot be empty"));

            // When
            ResponseEntity<TransactionModel> response = transactionController.createTransaction(transaction);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("should handle null amount")
        void shouldHandleNullAmount() {
            // Given
            TransactionModel transaction = new TransactionModel(
                1L, null, "Test Vendor", "expense", "2024-01-01", "food", "test notes", 1
            );
            
            when(transactionService.createTransaction(any(TransactionModel.class)))
                .thenThrow(new IllegalArgumentException("Amount cannot be null"));

            // When
            ResponseEntity<TransactionModel> response = transactionController.createTransaction(transaction);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("should handle invalid user ID")
        void shouldHandleInvalidUserId() {
            // Given
            TransactionModel transaction = new TransactionModel(
                1L, 100.0, "Test Vendor", "expense", "2024-01-01", "food", "test notes", 0
            );
            
            when(transactionService.createTransaction(any(TransactionModel.class)))
                .thenThrow(new IllegalArgumentException("Invalid user ID"));

            // When
            ResponseEntity<TransactionModel> response = transactionController.createTransaction(transaction);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("should allow null category")
        void shouldAllowNullCategory() {
            // Given
            TransactionModel transaction = new TransactionModel(
                1L, 100.0, "Test Vendor", "expense", "2024-01-01", null, "test notes", 1
            );
            
            TransactionModel createdTransaction = new TransactionModel(
                1L, 100.0, "Test Vendor", "expense", "2024-01-01", null, "test notes", 1
            );
            
            // Mock the service to return the created transaction
            when(transactionService.createTransaction(any(TransactionModel.class))).thenReturn(createdTransaction);

            // When
            ResponseEntity<TransactionModel> response = transactionController.createTransaction(transaction);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCategory()).isNull();
        }

        @Test
        @DisplayName("should allow null notes")
        void shouldAllowNullNotes() {
            // Given
            TransactionModel transaction = new TransactionModel(
                1L, 100.0, "Test Vendor", "expense", "2024-01-01", "food", null, 1
            );
            
            TransactionModel createdTransaction = new TransactionModel(
                1L, 100.0, "Test Vendor", "expense", "2024-01-01", "food", null, 1
            );
            
            // Mock the service to return the created transaction
            when(transactionService.createTransaction(any(TransactionModel.class))).thenReturn(createdTransaction);

            // When
            ResponseEntity<TransactionModel> response = transactionController.createTransaction(transaction);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getNotes()).isNull();
        }

        @Test
        @DisplayName("should allow empty category")
        void shouldAllowEmptyCategory() {
            // Given
            TransactionModel transaction = new TransactionModel(
                1L, 100.0, "Test Vendor", "expense", "2024-01-01", "", "test notes", 1
            );
            
            TransactionModel createdTransaction = new TransactionModel(
                1L, 100.0, "Test Vendor", "expense", "2024-01-01", null, "test notes", 1
            );
            
            // Mock the service to return the created transaction
            when(transactionService.createTransaction(any(TransactionModel.class))).thenReturn(createdTransaction);

            // When
            ResponseEntity<TransactionModel> response = transactionController.createTransaction(transaction);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCategory()).isNull();
        }

        @Test
        @DisplayName("should allow empty notes")
        void shouldAllowEmptyNotes() {
            // Given
            TransactionModel transaction = new TransactionModel(
                1L, 100.0, "Test Vendor", "expense", "2024-01-01", "food", "", 1
            );
            
            TransactionModel createdTransaction = new TransactionModel(
                1L, 100.0, "Test Vendor", "expense", "2024-01-01", "food", "", 1
            );
            
            // Mock the service to return the created transaction
            when(transactionService.createTransaction(any(TransactionModel.class))).thenReturn(createdTransaction);

            // When
            ResponseEntity<TransactionModel> response = transactionController.createTransaction(transaction);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getNotes()).isEqualTo("");
        }
    }

    @Nested
    @DisplayName("PUT /transactions/{id}")
    class UpdateTransactionTests {

        @Test
        @DisplayName("should update transaction successfully")
        void shouldUpdateTransactionSuccessfully() {
            // Given
            Long id = 1L;
            TransactionModel transaction = new TransactionModel(
                1L, 150.0, "Updated Vendor", "income", "2024-01-02", "salary", "updated notes", 1
            );
            
            TransactionModel updatedTransaction = new TransactionModel(
                1L, 150.0, "Updated Vendor", "income", "2024-01-02", "salary", "updated notes", 1
            );
            
            // Mock the service to return the updated transaction
            when(transactionService.updateTransaction(id, transaction)).thenReturn(updatedTransaction);

            // When
            ResponseEntity<TransactionModel> response = transactionController.updateTransaction(id, transaction);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getVendor()).isEqualTo("Updated Vendor");
        }

        @Test
        @DisplayName("should handle null transaction input")
        void shouldHandleNullTransactionInput() {
            // Given
            Long id = 1L;

            // When
            ResponseEntity<TransactionModel> response = transactionController.updateTransaction(id, null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("should handle null id")
        void shouldHandleNullId() {
            // Given
            TransactionModel transaction = new TransactionModel(
                1L, 100.0, "Test Vendor", "expense", "2024-01-01", "food", "test notes", 1
            );

            // When
            ResponseEntity<TransactionModel> response = transactionController.updateTransaction(null, transaction);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("should handle invalid transaction data in update")
        void shouldHandleInvalidTransactionDataInUpdate() {
            // Given
            Long id = 1L;
            TransactionModel invalidTransaction = new TransactionModel(
                1L, null, "", "", "invalid-date", "food", "test notes", 0
            );
            
            when(transactionService.updateTransaction(id, invalidTransaction))
                .thenThrow(new IllegalArgumentException("Invalid transaction data"));

            // When
            ResponseEntity<TransactionModel> response = transactionController.updateTransaction(id, invalidTransaction);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("should allow null category in update")
        void shouldAllowNullCategoryInUpdate() {
            // Given
            Long id = 1L;
            TransactionModel transaction = new TransactionModel(
                1L, 100.0, "Test Vendor", "expense", "2024-01-01", null, "test notes", 1
            );
            
            TransactionModel updatedTransaction = new TransactionModel(
                1L, 100.0, "Test Vendor", "expense", "2024-01-01", null, "test notes", 1
            );
            
            // Mock the service to return the updated transaction
            when(transactionService.updateTransaction(id, transaction)).thenReturn(updatedTransaction);

            // When
            ResponseEntity<TransactionModel> response = transactionController.updateTransaction(id, transaction);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCategory()).isNull();
        }

        @Test
        @DisplayName("should allow null notes in update")
        void shouldAllowNullNotesInUpdate() {
            // Given
            Long id = 1L;
            TransactionModel transaction = new TransactionModel(
                1L, 100.0, "Test Vendor", "expense", "2024-01-01", "food", null, 1
            );
            
            TransactionModel updatedTransaction = new TransactionModel(
                1L, 100.0, "Test Vendor", "expense", "2024-01-01", "food", null, 1
            );
            
            // Mock the service to return the updated transaction
            when(transactionService.updateTransaction(id, transaction)).thenReturn(updatedTransaction);

            // When
            ResponseEntity<TransactionModel> response = transactionController.updateTransaction(id, transaction);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getNotes()).isNull();
        }
    }

    @Nested
    @DisplayName("DELETE /transactions/{id}")
    class DeleteTransactionTests {

        @Test
        @DisplayName("should delete transaction successfully")
        void shouldDeleteTransactionSuccessfully() {
            // Given
            Long id = 1L;
            
            // Mock the service to return true for successful deletion
            when(transactionService.deleteTransaction(id)).thenReturn(true);

            // When
            ResponseEntity<Void> response = transactionController.deleteTransaction(id);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }

        @Test
        @DisplayName("should handle null id")
        void shouldHandleNullId() {
            // When
            ResponseEntity<Void> response = transactionController.deleteTransaction(null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
        
        @Test
        @DisplayName("should handle non-existent transaction for deletion")
        void shouldHandleNonExistentTransactionForDeletion() {
            // Given
            Long id = 999L;
            
            // Mock the service to return false for non-existent transaction
            when(transactionService.deleteTransaction(id)).thenReturn(false);

            // When
            ResponseEntity<Void> response = transactionController.deleteTransaction(id);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("should handle transaction with empty required fields")
        void shouldHandleTransactionWithEmptyRequiredFields() {
            // Given
            TransactionModel emptyTransaction = new TransactionModel(
                1L, 0.0, "", "", "2024-01-01", "food", "test notes", 1
            );
            
            when(transactionService.createTransaction(any(TransactionModel.class)))
                .thenThrow(new IllegalArgumentException("Required fields cannot be empty"));

            // When
            ResponseEntity<TransactionModel> response = transactionController.createTransaction(emptyTransaction);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("should handle zero id")
        void shouldHandleZeroId() {
            // Given
            Long zeroId = 0L;
            TransactionModel transaction = new TransactionModel(
                1L, 100.0, "Test Vendor", "expense", "2024-01-01", "food", "test notes", 1
            );

            // When
            ResponseEntity<TransactionModel> response = transactionController.updateTransaction(zeroId, transaction);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("should handle negative id")
        void shouldHandleNegativeId() {
            // Given
            Long negativeId = -1L;

            // When
            ResponseEntity<Void> response = transactionController.deleteTransaction(negativeId);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }
}
