package com.slackbuidler.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slackbuidler.dao.BudgetCategoryDAO;
import com.slackbuidler.dao.BudgetDAO;
import com.slackbuidler.models.BudgetCategoryModel;
import com.slackbuidler.models.BudgetCategorySummaryDTO;
import com.slackbuidler.models.BudgetModel;
import com.slackbuidler.models.BudgetSummaryDTO;

@Service
public class BudgetService {
    
    private final BudgetDAO budgetDAO;
    private final BudgetCategoryDAO budgetCategoryDAO;

    @Autowired
    public BudgetService(BudgetDAO budgetDAO, BudgetCategoryDAO budgetCategoryDAO) {
        this.budgetDAO = budgetDAO;
        this.budgetCategoryDAO = budgetCategoryDAO;
    }

    // Budget operations
    public List<BudgetModel> getBudgetsByUserId(Integer userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        List<BudgetModel> budgets = budgetDAO.findByUserIdAndIsActiveTrue(userId);
        
        // Load categories for each budget
        for (BudgetModel budget : budgets) {
            List<BudgetCategoryModel> categories = budgetCategoryDAO.findByBudgetIdOrderByName(budget.getId());
            // Note: We can't directly set categories on BudgetModel since it doesn't have that field
            // The frontend will need to make a separate call to get categories
        }
        
        return budgets;
    }

    public Optional<BudgetModel> getBudgetById(Long budgetId) {
        if (budgetId == null || budgetId <= 0) {
            throw new IllegalArgumentException("Budget ID must be positive");
        }
        return budgetDAO.findById(budgetId);
    }

    public BudgetModel createBudget(BudgetModel budget) {
        if (budget == null) {
            throw new IllegalArgumentException("Budget cannot be null");
        }
        if (budget.getName() == null || budget.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Budget name cannot be empty");
        }
        if (budget.getUserId() == null || budget.getUserId() <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        if (budget.getStartDate() == null || budget.getStartDate().trim().isEmpty()) {
            throw new IllegalArgumentException("Start date cannot be empty");
        }
        if (budget.getEndDate() == null || budget.getEndDate().trim().isEmpty()) {
            throw new IllegalArgumentException("End date cannot be empty");
        }

        // Check if budget with same name already exists for this user
        if (budgetDAO.existsByNameAndUserId(budget.getName(), budget.getUserId())) {
            throw new IllegalArgumentException("Budget with name '" + budget.getName() + "' already exists for this user");
        }

        return budgetDAO.save(budget);
    }

    public BudgetModel updateBudget(Long budgetId, BudgetModel budget) {
        if (budgetId == null || budgetId <= 0) {
            throw new IllegalArgumentException("Budget ID must be positive");
        }
        if (budget == null) {
            throw new IllegalArgumentException("Budget cannot be null");
        }

        Optional<BudgetModel> existingBudget = budgetDAO.findById(budgetId);
        if (existingBudget.isEmpty()) {
            throw new IllegalArgumentException("Budget with ID " + budgetId + " not found");
        }

        BudgetModel existing = existingBudget.get();
        
        // Update fields
        if (budget.getName() != null && !budget.getName().trim().isEmpty()) {
            existing.setName(budget.getName());
        }
        if (budget.getStartDate() != null && !budget.getStartDate().trim().isEmpty()) {
            existing.setStartDate(budget.getStartDate());
        }
        if (budget.getEndDate() != null && !budget.getEndDate().trim().isEmpty()) {
            existing.setEndDate(budget.getEndDate());
        }
        if (budget.getTotalPlanned() != null) {
            existing.setTotalPlanned(budget.getTotalPlanned());
        }
        if (budget.getIsActive() != null) {
            existing.setIsActive(budget.getIsActive());
        }
        if (budget.getIsRepeatable() != null) {
            existing.setIsRepeatable(budget.getIsRepeatable());
        }
        if (budget.getRepeatInterval() != null) {
            existing.setRepeatInterval(budget.getRepeatInterval());
        }
        if (budget.getRepeatCount() != null) {
            existing.setRepeatCount(budget.getRepeatCount());
        }

        existing.setUpdatedAt(java.time.LocalDateTime.now());
        return budgetDAO.save(existing);
    }

    public boolean deleteBudget(Long budgetId) {
        if (budgetId == null || budgetId <= 0) {
            throw new IllegalArgumentException("Budget ID must be positive");
        }

        Optional<BudgetModel> budget = budgetDAO.findById(budgetId);
        if (budget.isEmpty()) {
            return false;
        }

        // Delete all categories first (cascade)
        budgetCategoryDAO.deleteByBudgetId(budgetId);
        
        // Delete the budget
        budgetDAO.deleteById(budgetId);
        return true;
    }

    // Budget category operations
    public List<BudgetCategoryModel> getCategoriesByBudgetId(Long budgetId) {
        if (budgetId == null || budgetId <= 0) {
            throw new IllegalArgumentException("Budget ID must be positive");
        }
        return budgetCategoryDAO.findByBudgetIdOrderByName(budgetId);
    }

    public Optional<BudgetCategoryModel> getCategoryById(Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            throw new IllegalArgumentException("Category ID must be positive");
        }
        return budgetCategoryDAO.findById(categoryId);
    }

    public BudgetCategoryModel createCategory(BudgetCategoryModel category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        if (category.getBudgetId() == null || category.getBudgetId() <= 0) {
            throw new IllegalArgumentException("Budget ID must be positive");
        }
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
        if (category.getPlannedAmount() == null || category.getPlannedAmount() <= 0) {
            throw new IllegalArgumentException("Planned amount must be positive");
        }

        // Check if budget exists
        if (!budgetDAO.existsById(category.getBudgetId())) {
            throw new IllegalArgumentException("Budget with ID " + category.getBudgetId() + " not found");
        }

        // Check if category with same name already exists for this budget
        if (budgetCategoryDAO.existsByBudgetIdAndName(category.getBudgetId(), category.getName())) {
            throw new IllegalArgumentException("Category with name '" + category.getName() + "' already exists for this budget");
        }

        return budgetCategoryDAO.save(category);
    }

    public BudgetCategoryModel updateCategory(Long categoryId, BudgetCategoryModel category) {
        if (categoryId == null || categoryId <= 0) {
            throw new IllegalArgumentException("Category ID must be positive");
        }
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }

        Optional<BudgetCategoryModel> existingCategory = budgetCategoryDAO.findById(categoryId);
        if (existingCategory.isEmpty()) {
            throw new IllegalArgumentException("Category with ID " + categoryId + " not found");
        }

        BudgetCategoryModel existing = existingCategory.get();
        
        // Update fields
        if (category.getName() != null && !category.getName().trim().isEmpty()) {
            existing.setName(category.getName());
        }
        if (category.getPlannedAmount() != null && category.getPlannedAmount() > 0) {
            existing.setPlannedAmount(category.getPlannedAmount());
        }
        if (category.getSpentAmount() != null) {
            existing.setSpentAmount(category.getSpentAmount());
        }
        if (category.getColor() != null) {
            existing.setColor(category.getColor());
        }

        existing.setUpdatedAt(java.time.LocalDateTime.now());
        return budgetCategoryDAO.save(existing);
    }

    public boolean deleteCategory(Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            throw new IllegalArgumentException("Category ID must be positive");
        }

        if (!budgetCategoryDAO.existsById(categoryId)) {
            return false;
        }

        budgetCategoryDAO.deleteById(categoryId);
        return true;
    }

    // Business logic methods
    public void updateBudgetTotals(Long budgetId) {
        if (budgetId == null || budgetId <= 0) {
            throw new IllegalArgumentException("Budget ID must be positive");
        }

        Optional<BudgetModel> budgetOpt = budgetDAO.findById(budgetId);
        if (budgetOpt.isEmpty()) {
            throw new IllegalArgumentException("Budget with ID " + budgetId + " not found");
        }

        BudgetModel budget = budgetOpt.get();
        List<BudgetCategoryModel> categories = budgetCategoryDAO.findByBudgetId(budgetId);

        double totalPlanned = categories.stream()
                .mapToDouble(cat -> cat.getPlannedAmount() != null ? cat.getPlannedAmount() : 0.0)
                .sum();

        double totalSpent = categories.stream()
                .mapToDouble(cat -> cat.getSpentAmount() != null ? cat.getSpentAmount() : 0.0)
                .sum();

        budget.setTotalPlanned(totalPlanned);
        budget.setTotalSpent(totalSpent);
        budget.setUpdatedAt(java.time.LocalDateTime.now());

        budgetDAO.save(budget);
    }

    public List<BudgetModel> getOverBudgetBudgets(Integer userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        return budgetDAO.findOverBudgetBudgets(userId);
    }

    public List<BudgetModel> getNearLimitBudgets(Integer userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        return budgetDAO.findNearLimitBudgets(userId);
    }

    public List<BudgetCategoryModel> getOverBudgetCategories(Long budgetId) {
        if (budgetId == null || budgetId <= 0) {
            throw new IllegalArgumentException("Budget ID must be positive");
        }
        return budgetCategoryDAO.findOverBudgetCategories(budgetId);
    }

    public List<BudgetCategoryModel> getNearLimitCategories(Long budgetId) {
        if (budgetId == null || budgetId <= 0) {
            throw new IllegalArgumentException("Budget ID must be positive");
        }
        return budgetCategoryDAO.findNearLimitCategories(budgetId);
    }
    
    /**
     * Get comprehensive budget summaries including categories for a user
     * This consolidates budget and category data into a single response
     */
    public List<BudgetSummaryDTO> getBudgetSummariesByUserId(Integer userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        
        List<BudgetModel> budgets = budgetDAO.findByUserIdAndIsActiveTrue(userId);
        List<BudgetSummaryDTO> summaries = new java.util.ArrayList<>();
        
        for (BudgetModel budget : budgets) {
            BudgetSummaryDTO summary = new BudgetSummaryDTO(budget);
            
            // Get categories for this budget
            List<BudgetCategoryModel> categories = budgetCategoryDAO.findByBudgetIdOrderByName(budget.getId());
            List<BudgetCategorySummaryDTO> categorySummaries = categories.stream()
                .map(BudgetCategorySummaryDTO::new)
                .collect(java.util.stream.Collectors.toList());
            
            summary.setCategories(categorySummaries);
            summaries.add(summary);
        }
        
        return summaries;
    }
    
    /**
     * Get a single comprehensive budget summary by ID
     */
    public Optional<BudgetSummaryDTO> getBudgetSummaryById(Long budgetId) {
        if (budgetId == null || budgetId <= 0) {
            throw new IllegalArgumentException("Budget ID must be positive");
        }
        
        Optional<BudgetModel> budgetOpt = budgetDAO.findById(budgetId);
        if (budgetOpt.isEmpty()) {
            return Optional.empty();
        }
        
        BudgetModel budget = budgetOpt.get();
        BudgetSummaryDTO summary = new BudgetSummaryDTO(budget);
        
        // Get categories for this budget
        List<BudgetCategoryModel> categories = budgetCategoryDAO.findByBudgetIdOrderByName(budgetId);
        List<BudgetCategorySummaryDTO> categorySummaries = categories.stream()
            .map(BudgetCategorySummaryDTO::new)
            .collect(java.util.stream.Collectors.toList());
        
        summary.setCategories(categorySummaries);
        return Optional.of(summary);
    }
} 