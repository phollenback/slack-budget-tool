package com.slackbuidler.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.slackbuidler.models.BudgetCategoryModel;
import com.slackbuidler.models.BudgetDashboardResponse;
import com.slackbuidler.models.BudgetModel;
import com.slackbuidler.models.BudgetSummaryDTO;
import com.slackbuidler.services.BudgetService;

@RestController
@RequestMapping("/budgets")
public class BudgetController {
    
    private final BudgetService budgetService;

    @Autowired
    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    // Budget endpoints
    @GetMapping("/")
    public ResponseEntity<List<BudgetModel>> getBudgets(@RequestParam(defaultValue = "1") int userId) {
        try {
            List<BudgetModel> budgets = budgetService.getBudgetsByUserId(userId);

            return ResponseEntity.ok(budgets);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetModel> getBudgetById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            Optional<BudgetModel> budget = budgetService.getBudgetById(id);
            if (budget.isPresent()) {
                return ResponseEntity.ok(budget.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/")
    public ResponseEntity<BudgetModel> createBudget(@RequestBody BudgetModel budget) {
        if (budget == null) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            BudgetModel createdBudget = budgetService.createBudget(budget);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBudget);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetModel> updateBudget(@PathVariable Long id, @RequestBody BudgetModel budget) {
        if (budget == null) {
            return ResponseEntity.badRequest().build();
        }
        
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            BudgetModel updatedBudget = budgetService.updateBudget(id, budget);
            return ResponseEntity.ok(updatedBudget);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            boolean deleted = budgetService.deleteBudget(id);
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

    // Budget category endpoints
    @GetMapping("/{budgetId}/categories")
    public ResponseEntity<List<BudgetCategoryModel>> getBudgetCategories(@PathVariable Long budgetId) {
        if (budgetId == null || budgetId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            List<BudgetCategoryModel> categories = budgetService.getCategoriesByBudgetId(budgetId);
            return ResponseEntity.ok(categories);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<BudgetCategoryModel> getCategoryById(@PathVariable Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            Optional<BudgetCategoryModel> category = budgetService.getCategoryById(categoryId);
            if (category.isPresent()) {
                return ResponseEntity.ok(category.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{budgetId}/categories")
    public ResponseEntity<BudgetCategoryModel> createCategory(@PathVariable Long budgetId, @RequestBody BudgetCategoryModel category) {
        if (category == null) {
            return ResponseEntity.badRequest().build();
        }
        
        if (budgetId == null || budgetId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            category.setBudgetId(budgetId);
            BudgetCategoryModel createdCategory = budgetService.createCategory(category);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/categories/{categoryId}")
    public ResponseEntity<BudgetCategoryModel> updateCategory(@PathVariable Long categoryId, @RequestBody BudgetCategoryModel category) {
        if (category == null) {
            return ResponseEntity.badRequest().build();
        }
        
        if (categoryId == null || categoryId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            BudgetCategoryModel updatedCategory = budgetService.updateCategory(categoryId, category);
            return ResponseEntity.ok(updatedCategory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            boolean deleted = budgetService.deleteCategory(categoryId);
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

    // Business logic endpoints
    @PostMapping("/{budgetId}/update-totals")
    public ResponseEntity<Void> updateBudgetTotals(@PathVariable Long budgetId) {
        if (budgetId == null || budgetId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            budgetService.updateBudgetTotals(budgetId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/over-budget")
    public ResponseEntity<List<BudgetModel>> getOverBudgetBudgets(@RequestParam(defaultValue = "1") int userId) {
        try {
            List<BudgetModel> budgets = budgetService.getOverBudgetBudgets(userId);
            return ResponseEntity.ok(budgets);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/near-limit")
    public ResponseEntity<List<BudgetModel>> getNearLimitBudgets(@RequestParam(defaultValue = "1") int userId) {
        try {
            List<BudgetModel> budgets = budgetService.getNearLimitBudgets(userId);
            return ResponseEntity.ok(budgets);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{budgetId}/over-budget-categories")
    public ResponseEntity<List<BudgetCategoryModel>> getOverBudgetCategories(@PathVariable Long budgetId) {
        if (budgetId == null || budgetId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            List<BudgetCategoryModel> categories = budgetService.getOverBudgetCategories(budgetId);
            return ResponseEntity.ok(categories);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{budgetId}/near-limit-categories")
    public ResponseEntity<List<BudgetCategoryModel>> getNearLimitCategories(@PathVariable Long budgetId) {
        if (budgetId == null || budgetId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            List<BudgetCategoryModel> categories = budgetService.getNearLimitCategories(budgetId);
            return ResponseEntity.ok(categories);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // New comprehensive budget summary endpoints
    @GetMapping("/summary")
    public ResponseEntity<List<BudgetSummaryDTO>> getBudgetSummaries(@RequestParam(defaultValue = "1") int userId) {
        try {
            List<BudgetSummaryDTO> summaries = budgetService.getBudgetSummariesByUserId(userId);
            return ResponseEntity.ok(summaries);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<BudgetSummaryDTO> getBudgetSummaryById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            Optional<BudgetSummaryDTO> summary = budgetService.getBudgetSummaryById(id);
            if (summary.isPresent()) {
                return ResponseEntity.ok(summary.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Dashboard endpoint for comprehensive view
    @GetMapping("/dashboard")
    public ResponseEntity<BudgetDashboardResponse> getBudgetDashboard(@RequestParam(defaultValue = "1") int userId) {
        try {
            List<BudgetSummaryDTO> summaries = budgetService.getBudgetSummariesByUserId(userId);
            BudgetDashboardResponse dashboard = new BudgetDashboardResponse(summaries);
            return ResponseEntity.ok(dashboard);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 