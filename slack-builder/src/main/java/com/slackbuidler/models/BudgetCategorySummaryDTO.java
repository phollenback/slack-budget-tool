package com.slackbuidler.models;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for budget category summary information
 * This provides a clean view of category data for the frontend
 */
public class BudgetCategorySummaryDTO {
    
    private Long id;
    private Long budgetId;
    private String name;
    private Double plannedAmount;
    private Double spentAmount;
    private Double remainingAmount;
    private String color;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Computed fields for better UX
    private Double spentPercentage;
    private Boolean isOverBudget;
    private Boolean isNearLimit;
    private String status; // "on-track", "near-limit", "over-budget", "completed"
    
    // Default constructor
    public BudgetCategorySummaryDTO() {}
    
    // Constructor from BudgetCategoryModel
    public BudgetCategorySummaryDTO(BudgetCategoryModel category) {
        this.id = category.getId();
        this.budgetId = category.getBudgetId();
        this.name = category.getName();
        this.plannedAmount = category.getPlannedAmount();
        this.spentAmount = category.getSpentAmount();
        this.color = category.getColor();
        this.createdAt = category.getCreatedAt();
        this.updatedAt = category.getUpdatedAt();
        
        // Compute derived fields
        this.remainingAmount = (this.plannedAmount != null && this.spentAmount != null) 
            ? this.plannedAmount - this.spentAmount : 0.0;
        
        this.spentPercentage = (this.plannedAmount != null && this.plannedAmount > 0) 
            ? (this.spentAmount != null ? this.spentAmount : 0.0) / this.plannedAmount * 100 : 0.0;
        
        this.isOverBudget = this.spentAmount != null && this.plannedAmount != null 
            && this.spentAmount > this.plannedAmount;
        
        this.isNearLimit = this.spentPercentage >= 80.0;
        
        // Set status based on spending
        if (this.isOverBudget) {
            this.status = "over-budget";
        } else if (this.isNearLimit) {
            this.status = "near-limit";
        } else if (this.spentPercentage >= 100.0) {
            this.status = "completed";
        } else {
            this.status = "on-track";
        }
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getBudgetId() { return budgetId; }
    public void setBudgetId(Long budgetId) { this.budgetId = budgetId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Double getPlannedAmount() { return plannedAmount; }
    public void setPlannedAmount(Double plannedAmount) { this.plannedAmount = plannedAmount; }
    
    public Double getSpentAmount() { return spentAmount; }
    public void setSpentAmount(Double spentAmount) { this.spentAmount = spentAmount; }
    
    public Double getRemainingAmount() { return remainingAmount; }
    public void setRemainingAmount(Double remainingAmount) { this.remainingAmount = remainingAmount; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Double getSpentPercentage() { return spentPercentage; }
    public void setSpentPercentage(Double spentPercentage) { this.spentPercentage = spentPercentage; }
    
    public Boolean getIsOverBudget() { return isOverBudget; }
    public void setIsOverBudget(Boolean isOverBudget) { this.isOverBudget = isOverBudget; }
    
    public Boolean getIsNearLimit() { return isNearLimit; }
    public void setIsNearLimit(Boolean isNearLimit) { this.isNearLimit = isNearLimit; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
} 