package com.slackbuidler.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.slackbuidler.models.TransactionModel;

@Repository
public interface TransactionDAO extends JpaRepository<TransactionModel, Long> {
    
    // Custom query methods
    List<TransactionModel> findByUserId(int userId);
    
    Optional<TransactionModel> findById(Long id);
    
    boolean existsById(Long id);
    
    // Legacy method names for backward compatibility
    default List<TransactionModel> getAllTransactions() {
        return findAll();
    }
    
    default List<TransactionModel> getTransactionsByUserId(int userId) {
        return findByUserId(userId);
    }
    
    default Optional<TransactionModel> getTransactionById(Long id) {
        return findById(id);
    }
    
    default boolean transactionExists(Long id) {
        return existsById(id);
    }
    
    default TransactionModel createTransaction(TransactionModel transaction) {
        return save(transaction);
    }
    
    default TransactionModel updateTransaction(Long id, TransactionModel transaction) {
        if (existsById(id)) {
            transaction.setId(id);
            return save(transaction);
        }
        throw new IllegalArgumentException("Transaction with ID " + id + " not found");
    }
    
    default boolean deleteTransaction(Long id) {
        if (existsById(id)) {
            deleteById(id);
            return true;
        }
        return false;
    }
} 