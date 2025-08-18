package com.slackbuidler.dao;

import com.slackbuidler.models.BudgetModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetDAO extends JpaRepository<BudgetModel, Long> {
    
    // Find budgets by user ID
    List<BudgetModel> findByUserId(Integer userId);
    
    // Find active budgets by user ID
    List<BudgetModel> findByUserIdAndIsActiveTrue(Integer userId);
    
    // Find budgets by user ID and active status
    List<BudgetModel> findByUserIdAndIsActive(Integer userId, Boolean isActive);
    
    // Find budgets by user ID and repeatable status
    List<BudgetModel> findByUserIdAndIsRepeatable(Integer userId, Boolean isRepeatable);
    
    // Find budgets by user ID and repeat interval
    List<BudgetModel> findByUserIdAndRepeatInterval(Integer userId, String repeatInterval);
    
    // Find budgets by name and user ID
    Optional<BudgetModel> findByNameAndUserId(String name, Integer userId);
    
    // Check if budget exists by name and user ID
    boolean existsByNameAndUserId(String name, Integer userId);
    
    // Find budgets that are over budget
    @Query("SELECT b FROM BudgetModel b WHERE b.userId = :userId AND b.totalSpent > b.totalPlanned")
    List<BudgetModel> findOverBudgetBudgets(@Param("userId") Integer userId);
    
    // Find budgets near their limit (80% or more spent)
    @Query("SELECT b FROM BudgetModel b WHERE b.userId = :userId AND (b.totalSpent / b.totalPlanned) >= 0.8")
    List<BudgetModel> findNearLimitBudgets(@Param("userId") Integer userId);
    
    // Find budgets by date range
    @Query("SELECT b FROM BudgetModel b WHERE b.userId = :userId AND b.startDate <= :date AND b.endDate >= :date")
    List<BudgetModel> findActiveBudgetsByDate(@Param("userId") Integer userId, @Param("date") String date);
    
    // Count budgets by user ID
    long countByUserId(Integer userId);
    
    // Count active budgets by user ID
    long countByUserIdAndIsActiveTrue(Integer userId);
} 