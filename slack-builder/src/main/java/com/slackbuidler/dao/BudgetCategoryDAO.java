package com.slackbuidler.dao;

import com.slackbuidler.models.BudgetCategoryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetCategoryDAO extends JpaRepository<BudgetCategoryModel, Long> {
    
    // Find categories by budget ID
    List<BudgetCategoryModel> findByBudgetId(Long budgetId);
    
    // Find category by budget ID and name
    Optional<BudgetCategoryModel> findByBudgetIdAndName(Long budgetId, String name);
    
    // Check if category exists by budget ID and name
    boolean existsByBudgetIdAndName(Long budgetId, String name);
    
    // Find categories that are over budget
    @Query("SELECT bc FROM BudgetCategoryModel bc WHERE bc.budgetId = :budgetId AND bc.spentAmount > bc.plannedAmount")
    List<BudgetCategoryModel> findOverBudgetCategories(@Param("budgetId") Long budgetId);
    
    // Find categories near their limit (80% or more spent)
    @Query("SELECT bc FROM BudgetCategoryModel bc WHERE bc.budgetId = :budgetId AND (bc.spentAmount / bc.plannedAmount) >= 0.8")
    List<BudgetCategoryModel> findNearLimitCategories(@Param("budgetId") Long budgetId);
    
    // Find categories by budget ID ordered by planned amount descending
    List<BudgetCategoryModel> findByBudgetIdOrderByPlannedAmountDesc(Long budgetId);
    
    // Find categories by budget ID ordered by spent amount descending
    List<BudgetCategoryModel> findByBudgetIdOrderBySpentAmountDesc(Long budgetId);
    
    // Find categories by budget ID ordered by name
    List<BudgetCategoryModel> findByBudgetIdOrderByName(Long budgetId);
    
    // Count categories by budget ID
    long countByBudgetId(Long budgetId);
    
    // Delete all categories by budget ID
    void deleteByBudgetId(Long budgetId);
    
    // Find categories with specific color
    List<BudgetCategoryModel> findByColor(String color);
    
    // Find categories by budget ID and minimum planned amount
    @Query("SELECT bc FROM BudgetCategoryModel bc WHERE bc.budgetId = :budgetId AND bc.plannedAmount >= :minAmount")
    List<BudgetCategoryModel> findByBudgetIdAndPlannedAmountGreaterThanEqual(@Param("budgetId") Long budgetId, @Param("minAmount") Double minAmount);
} 