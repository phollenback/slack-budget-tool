package com.slackbuidler.models;

import java.util.List;

/**
 * Comprehensive response wrapper for budget dashboard data
 * This eliminates duplicate data and provides high-level insights
 */
public class BudgetDashboardResponse {
    
    private List<BudgetSummaryDTO> budgets;
    private DashboardSummary summary;
    private List<BudgetSummaryDTO> overBudgetBudgets;
    private List<BudgetSummaryDTO> nearLimitBudgets;
    private List<BudgetSummaryDTO> completedBudgets;
    
    // Default constructor
    public BudgetDashboardResponse() {}
    
    // Constructor with budgets
    public BudgetDashboardResponse(List<BudgetSummaryDTO> budgets) {
        this.budgets = budgets;
        this.processBudgets();
    }
    
    // Process budgets to create summary and categorize them
    private void processBudgets() {
        if (this.budgets == null || this.budgets.isEmpty()) {
            this.summary = new DashboardSummary();
            return;
        }
        
        // Create summary
        this.summary = new DashboardSummary();
        this.summary.setTotalBudgets(this.budgets.size());
        
        double totalPlanned = 0.0;
        double totalSpent = 0.0;
        int activeBudgets = 0;
        int overBudgetCount = 0;
        int nearLimitCount = 0;
        int completedCount = 0;
        
        for (BudgetSummaryDTO budget : this.budgets) {
            if (budget.getTotalPlanned() != null) {
                totalPlanned += budget.getTotalPlanned();
            }
            if (budget.getTotalSpent() != null) {
                totalSpent += budget.getTotalSpent();
            }
            
            if (budget.getIsActive()) {
                activeBudgets++;
            }
            
            if (budget.getIsOverBudget()) {
                overBudgetCount++;
            } else if (budget.getIsNearLimit()) {
                nearLimitCount++;
            } else if ("completed".equals(budget.getStatus())) {
                completedCount++;
            }
        }
        
        this.summary.setTotalPlanned(totalPlanned);
        this.summary.setTotalSpent(totalSpent);
        this.summary.setTotalRemaining(totalPlanned - totalSpent);
        this.summary.setActiveBudgets(activeBudgets);
        this.summary.setOverBudgetCount(overBudgetCount);
        this.summary.setNearLimitCount(nearLimitCount);
        this.summary.setCompletedCount(completedCount);
        
        if (totalPlanned > 0) {
            this.summary.setOverallSpentPercentage((totalSpent / totalPlanned) * 100);
        }
        
        // Categorize budgets
        this.overBudgetBudgets = this.budgets.stream()
            .filter(b -> b.getIsOverBudget())
            .collect(java.util.stream.Collectors.toList());
            
        this.nearLimitBudgets = this.budgets.stream()
            .filter(b -> b.getIsNearLimit() && !b.getIsOverBudget())
            .collect(java.util.stream.Collectors.toList());
            
        this.completedBudgets = this.budgets.stream()
            .filter(b -> "completed".equals(b.getStatus()))
            .collect(java.util.stream.Collectors.toList());
    }
    
    // Getters and Setters
    public List<BudgetSummaryDTO> getBudgets() { return budgets; }
    public void setBudgets(List<BudgetSummaryDTO> budgets) { 
        this.budgets = budgets; 
        this.processBudgets();
    }
    
    public DashboardSummary getSummary() { return summary; }
    public void setSummary(DashboardSummary summary) { this.summary = summary; }
    
    public List<BudgetSummaryDTO> getOverBudgetBudgets() { return overBudgetBudgets; }
    public void setOverBudgetBudgets(List<BudgetSummaryDTO> overBudgetBudgets) { this.overBudgetBudgets = overBudgetBudgets; }
    
    public List<BudgetSummaryDTO> getNearLimitBudgets() { return nearLimitBudgets; }
    public void setNearLimitBudgets(List<BudgetSummaryDTO> nearLimitBudgets) { this.nearLimitBudgets = nearLimitBudgets; }
    
    public List<BudgetSummaryDTO> getCompletedBudgets() { return completedBudgets; }
    public void setCompletedBudgets(List<BudgetSummaryDTO> completedBudgets) { this.completedBudgets = completedBudgets; }
    
    /**
     * Inner class for dashboard summary statistics
     */
    public static class DashboardSummary {
        private Integer totalBudgets;
        private Integer activeBudgets;
        private Double totalPlanned;
        private Double totalSpent;
        private Double totalRemaining;
        private Double overallSpentPercentage;
        private Integer overBudgetCount;
        private Integer nearLimitCount;
        private Integer completedCount;
        
        // Default constructor
        public DashboardSummary() {
            this.totalBudgets = 0;
            this.activeBudgets = 0;
            this.totalPlanned = 0.0;
            this.totalSpent = 0.0;
            this.totalRemaining = 0.0;
            this.overallSpentPercentage = 0.0;
            this.overBudgetCount = 0;
            this.nearLimitCount = 0;
            this.completedCount = 0;
        }
        
        // Getters and Setters
        public Integer getTotalBudgets() { return totalBudgets; }
        public void setTotalBudgets(Integer totalBudgets) { this.totalBudgets = totalBudgets; }
        
        public Integer getActiveBudgets() { return activeBudgets; }
        public void setActiveBudgets(Integer activeBudgets) { this.activeBudgets = activeBudgets; }
        
        public Double getTotalPlanned() { return totalPlanned; }
        public void setTotalPlanned(Double totalPlanned) { this.totalPlanned = totalPlanned; }
        
        public Double getTotalSpent() { return totalSpent; }
        public void setTotalSpent(Double totalSpent) { this.totalSpent = totalSpent; }
        
        public Double getTotalRemaining() { return totalRemaining; }
        public void setTotalRemaining(Double totalRemaining) { this.totalRemaining = totalRemaining; }
        
        public Double getOverallSpentPercentage() { return overallSpentPercentage; }
        public void setOverallSpentPercentage(Double overallSpentPercentage) { this.overallSpentPercentage = overallSpentPercentage; }
        
        public Integer getOverBudgetCount() { return overBudgetCount; }
        public void setOverBudgetCount(Integer overBudgetCount) { this.overBudgetCount = overBudgetCount; }
        
        public Integer getNearLimitCount() { return nearLimitCount; }
        public void setNearLimitCount(Integer nearLimitCount) { this.nearLimitCount = nearLimitCount; }
        
        public Integer getCompletedCount() { return completedCount; }
        public void setCompletedCount(Integer completedCount) { this.completedCount = completedCount; }
    }
} 