package com.slackbuidler.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slackbuidler.dao.TransactionDAO;
import com.slackbuidler.models.TransactionModel;
import com.slackbuidler.models.Category;
import com.slackbuidler.models.TransactionType;

@Service
public class TransactionService {
    private final TransactionDAO transactionDAO;

    @Autowired
    public TransactionService(TransactionDAO transactionDAO) {
        this.transactionDAO = transactionDAO;
    }

    public List<TransactionModel> getTransactions(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        
        return transactionDAO.getTransactionsByUserId(userId);
    }

    public List<TransactionModel> getAllTransactions() {
        return transactionDAO.getAllTransactions();
    }

    public Optional<TransactionModel> getTransactionById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Transaction ID must be positive");
        }
        
        return transactionDAO.getTransactionById(id);
    }

    public TransactionModel createTransaction(TransactionModel transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }
        
        // Validate business rules
        validateTransaction(transaction);
        
        return transactionDAO.createTransaction(transaction);
    }

    public TransactionModel updateTransaction(Long id, TransactionModel transaction) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Transaction ID must be positive");
        }
        
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }
        
        // Check if transaction exists
        if (!transactionDAO.transactionExists(id)) {
            throw new IllegalArgumentException("Transaction with ID " + id + " not found");
        }
        
        // Validate business rules
        validateTransaction(transaction);
        
        return transactionDAO.updateTransaction(id, transaction);
    }

    public boolean deleteTransaction(Long id) {
        if (id == null || id <= 0) {
            return false;
        }
        
        return transactionDAO.deleteTransaction(id);
    }

    /**
     * Get all available categories
     */
    public String[] getAvailableCategories() {
        return Category.getAllDisplayNames();
    }

    /**
     * Validate if a category is valid
     */
    public boolean isValidCategory(String category) {
        return Category.isValidCategory(category);
    }

    /**
     * Normalize a category string to standardized format
     */
    public String normalizeCategory(String category) {
        Category normalized = Category.fromDisplayName(category);
        return normalized != null ? normalized.getDisplayName() : null;
    }

        
    /**
     * Get all available transaction types
     */
    public String[] getAvailableTransactionTypes() {
        return TransactionType.getAllDisplayNames();
    }

    /**
     * Validate if a transaction type is valid
     */
    public boolean isValidTransactionType(String type) {
        return TransactionType.isValidTransactionType(type);
    }

    /**
     * Normalize a transaction type string to standardized format
     */
    public String normalizeTransactionType(String type) {
        TransactionType normalized = TransactionType.fromDisplayName(type);
        return normalized != null ? normalized.getDisplayName() : null;
    }

    private void validateTransaction(TransactionModel transaction) {
        if (transaction.getAmount() == null || transaction.getAmount() <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }
        
        if (transaction.getVendor() == null || transaction.getVendor().trim().isEmpty()) {
            throw new IllegalArgumentException("Vendor cannot be empty");
        }
        
        if (transaction.getType() == null || transaction.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction type cannot be empty");
        }
        
        // Validate transaction type
        if (!TransactionType.isValidTransactionType(transaction.getType())) {
            throw new IllegalArgumentException("Invalid transaction type: " + transaction.getType() + 
                ". Valid types are: " + String.join(", ", TransactionType.getAllDisplayNames()));
        }
        
        if (transaction.getDate() == null || transaction.getDate().trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction date cannot be empty");
        }
        
        // Validate category if provided
        if (transaction.getCategory() != null && !transaction.getCategory().trim().isEmpty()) {
            if (!Category.isValidCategory(transaction.getCategory())) {
                throw new IllegalArgumentException("Invalid category: " + transaction.getCategory() + 
                    ". Valid categories are: " + String.join(", ", Category.getAllDisplayNames()));
            }
        }
        
        if (transaction.getUserId() <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
    }

    public void saveFromMessage(String text, String userId) {
        // parse text
        // save transaction
        System.out.println("Saving transaction from message: " + text + " for user: " + userId);
    }

    public boolean transactionExists(Long id) {
        if (id == null || id <= 0) {
            return false;
        }
        
        return transactionDAO.transactionExists(id);
    }
}
