package com.slackbuidler.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import com.slackbuidler.models.TransactionModel;
import com.slackbuidler.services.TransactionService;

@RequestMapping("/transactions")
@RestController
public class TransactionController {
    
    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/")
    public ResponseEntity<List<TransactionModel>> getTransactions(@RequestParam(defaultValue = "1") int userId) {
        try {
            List<TransactionModel> transactions = transactionService.getTransactions(userId);
            
            if (transactions.isEmpty()) {
                return ResponseEntity.ok(List.of()); // Return empty list with 200 status
            }
            
            return ResponseEntity.ok(transactions);
        } catch (IllegalArgumentException e) {
            // For now, just return bad request without body to avoid type conflicts
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Handle OPTIONS preflight request for CORS
    @RequestMapping(value = "/", method = org.springframework.web.bind.annotation.RequestMethod.OPTIONS)
    public ResponseEntity<Void> handleOptions() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionModel> getTransactionById(@PathVariable Long id) {
        // Handle null or invalid ID gracefully
        if (id == null || id <= 0) {
            // For now, just return bad request without body to avoid type conflicts
            return ResponseEntity.badRequest().build();
        }
        
        try {
            var transaction = transactionService.getTransactionById(id);
            if (transaction.isPresent()) {
                return ResponseEntity.ok(transaction.get());
            } else {
                // Return 404 for non-existent transactions
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/")
    public ResponseEntity<TransactionModel> createTransaction(@RequestBody TransactionModel transaction) {
        // Handle null input
        if (transaction == null) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            TransactionModel createdTransaction = transactionService.createTransaction(transaction);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTransaction);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionModel> updateTransaction(@PathVariable Long id, @RequestBody TransactionModel transaction) {
        // Handle null input
        if (transaction == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // Handle null or invalid ID gracefully
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            TransactionModel updatedTransaction = transactionService.updateTransaction(id, transaction);
            return ResponseEntity.ok(updatedTransaction);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        // Handle null or invalid ID gracefully
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            boolean deleted = transactionService.deleteTransaction(id);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<String[]> getAvailableCategories() {
        try {
            String[] categories = transactionService.getAvailableCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/types")
    public ResponseEntity<String[]> getAvailableTransactionTypes() {
        try {
            String[] types = transactionService.getAvailableTransactionTypes();
            return ResponseEntity.ok(types);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
