package com.slackbuidler.models;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for comprehensive budget information
 * This consolidates budget and category data into a single response
 * for easier frontend consumption
 */
public class BudgetSummaryDTO {
    
    private Long id;
    private String name;
    private Integer userId;
    private String startDate;
    private String endDate;
    private Double totalPlanned;
    private Double totalSpent;
    private Double totalRemaining;
    private Boolean isActive;
    private Boolean isRepeatable;
    private String repeatInterval;
    private Integer repeatCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Computed fields for better UX
    private Double spentPercentage;
    private Boolean isOverBudget;
    private Boolean isNearLimit;
    private String status; // "on-track", "near-limit", "over-budget", "completed"
    
    // Daily spending calculations
    private Double dailySpendingAllowance;
    private Long daysRemaining;
    private Long daysElapsed;
    
    // Categories included in the budget
    private List<BudgetCategorySummaryDTO> categories;
    
    // Summary statistics
    private Integer totalCategories;
    private Integer activeCategories;
    private Integer overBudgetCategories;
    private Integer nearLimitCategories;
    
    // Default constructor
    public BudgetSummaryDTO() {}
    
    // Constructor from BudgetModel
    public BudgetSummaryDTO(BudgetModel budget) {
        this.id = budget.getId();
        this.name = budget.getName();
        this.userId = budget.getUserId();
        this.startDate = budget.getStartDate();
        this.endDate = budget.getEndDate();
        this.totalPlanned = budget.getTotalPlanned();
        this.totalSpent = budget.getTotalSpent();
        this.isActive = budget.getIsActive();
        this.isRepeatable = budget.getIsRepeatable();
        this.repeatInterval = budget.getRepeatInterval();
        this.repeatCount = budget.getRepeatCount();
        this.createdAt = budget.getCreatedAt();
        this.updatedAt = budget.getUpdatedAt();
        
        // Compute derived fields
        this.totalRemaining = budget.getTotalRemaining();
        this.spentPercentage = budget.getSpentPercentage();
        this.isOverBudget = budget.isOverBudget();
        this.isNearLimit = (this.spentPercentage != null && this.spentPercentage >= 80.0);
        
        // Compute daily spending fields
        this.dailySpendingAllowance = budget.getDailySpendingAllowance();
        this.daysRemaining = budget.getDaysRemaining();
        this.daysElapsed = budget.getDaysElapsed();
        
        // Set status based on spending
        if (this.isOverBudget) {
            this.status = "over-budget";
        } else if (this.isNearLimit) {
            this.status = "near-limit";
        } else if (this.spentPercentage != null && this.spentPercentage >= 100.0) {
            this.status = "completed";
        } else {
            this.status = "on-track";
        }
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    
    public Double getTotalPlanned() { return totalPlanned; }
    public void setTotalPlanned(Double totalPlanned) { this.totalPlanned = totalPlanned; }
    
    public Double getTotalSpent() { return totalSpent; }
    public void setTotalSpent(Double totalSpent) { this.totalSpent = totalSpent; }
    
    public Double getTotalRemaining() { return totalRemaining; }
    public void setTotalRemaining(Double totalRemaining) { this.totalRemaining = totalRemaining; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public Boolean getIsRepeatable() { return isRepeatable; }
    public void setIsRepeatable(Boolean isRepeatable) { this.isRepeatable = isRepeatable; }
    
    public String getRepeatInterval() { return repeatInterval; }
    public void setRepeatInterval(String repeatInterval) { this.repeatInterval = repeatInterval; }
    
    public Integer getRepeatCount() { return repeatCount; }
    public void setRepeatCount(Integer repeatCount) { this.repeatCount = repeatCount; }
    
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
    
    public Double getDailySpendingAllowance() { return dailySpendingAllowance; }
    public void setDailySpendingAllowance(Double dailySpendingAllowance) { this.dailySpendingAllowance = dailySpendingAllowance; }
    
    public Long getDaysRemaining() { return daysRemaining; }
    public void setDaysRemaining(Long daysRemaining) { this.daysRemaining = daysRemaining; }
    
    public Long getDaysElapsed() { return daysElapsed; }
    public void setDaysElapsed(Long daysElapsed) { this.daysElapsed = daysElapsed; }
    
    public List<BudgetCategorySummaryDTO> getCategories() { return categories; }
    public void setCategories(List<BudgetCategorySummaryDTO> categories) { 
        this.categories = categories; 
        this.updateCategoryStatistics();
    }
    
    public Integer getTotalCategories() { return totalCategories; }
    public void setTotalCategories(Integer totalCategories) { this.totalCategories = totalCategories; }
    
    public Integer getActiveCategories() { return activeCategories; }
    public void setActiveCategories(Integer activeCategories) { this.activeCategories = activeCategories; }
    
    public Integer getOverBudgetCategories() { return overBudgetCategories; }
    public void setOverBudgetCategories(Integer overBudgetCategories) { this.overBudgetCategories = overBudgetCategories; }
    
    public Integer getNearLimitCategories() { return nearLimitCategories; }
    public void setNearLimitCategories(Integer nearLimitCategories) { this.nearLimitCategories = nearLimitCategories; }
    
    // Helper method to update category statistics
    private void updateCategoryStatistics() {
        if (this.categories != null) {
            this.totalCategories = this.categories.size();
            this.activeCategories = (int) this.categories.stream()
                .filter(cat -> cat.getSpentAmount() < cat.getPlannedAmount())
                .count();
            this.overBudgetCategories = (int) this.categories.stream()
                .filter(cat -> cat.getIsOverBudget())
                .count();
            this.nearLimitCategories = (int) this.categories.stream()
                .filter(cat -> cat.getIsNearLimit())
                .count();
        } else {
            this.totalCategories = 0;
            this.activeCategories = 0;
            this.overBudgetCategories = 0;
            this.nearLimitCategories = 0;
        }
    }
    
    /**
     * Get formatted daily spending allowance for display
     * @return Formatted string or null if not available
     */
    public String getFormattedDailySpendingAllowance() {
        if (this.dailySpendingAllowance == null) {
            return null;
        }
        
        if (this.dailySpendingAllowance <= 0) {
            return "Budget exceeded";
        }
        
        return String.format("$%.2f", this.dailySpendingAllowance);
    }
    
    /**
     * Get a human-readable description of the daily spending situation
     * @return Description string
     */
    public String getDailySpendingDescription() {
        if (this.dailySpendingAllowance == null) {
            return "Unable to calculate daily allowance";
        }
        
        if (this.dailySpendingAllowance <= 0) {
            return "Budget exceeded - no daily allowance available";
        }
        
        if (this.daysRemaining != null && this.daysRemaining > 0) {
            return String.format("$%.2f per day for %d days", this.dailySpendingAllowance, this.daysRemaining);
        }
        
        return String.format("$%.2f per day", this.dailySpendingAllowance);
    }
} 