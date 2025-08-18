package com.slackbuidler.models;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "budgets")
public class BudgetModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "start_date", nullable = false, length = 10)
    private String startDate;

    @Column(name = "end_date", nullable = false, length = 10)
    private String endDate;

    @Column(name = "total_planned", precision = 10, scale = 2)
    private Double totalPlanned;

    @Column(name = "total_spent", precision = 10, scale = 2)
    private Double totalSpent;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "is_repeatable")
    private Boolean isRepeatable;

    @Column(name = "repeat_interval", length = 20)
    private String repeatInterval;

    @Column(name = "repeat_count")
    private Integer repeatCount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Default constructor
    public BudgetModel() {
        this.isActive = true;
        this.isRepeatable = false;
        this.repeatCount = 1;
        this.totalPlanned = 0.0;
        this.totalSpent = 0.0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Constructor with required fields
    public BudgetModel(String name, Integer userId, String startDate, String endDate) {
        this();
        this.name = name;
        this.userId = userId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Double getTotalPlanned() {
        return totalPlanned;
    }

    public void setTotalPlanned(Double totalPlanned) {
        this.totalPlanned = totalPlanned;
    }

    public Double getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(Double totalSpent) {
        this.totalSpent = totalSpent;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsRepeatable() {
        return isRepeatable;
    }

    public void setIsRepeatable(Boolean isRepeatable) {
        this.isRepeatable = isRepeatable;
    }

    public String getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(String repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    public Integer getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(Integer repeatCount) {
        this.repeatCount = repeatCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public Double getTotalRemaining() {
        if (totalPlanned == null || totalSpent == null) {
            return 0.0;
        }
        return totalPlanned - totalSpent;
    }

    public boolean isOverBudget() {
        return getTotalRemaining() < 0;
    }

    public double getSpentPercentage() {
        if (totalPlanned == null || totalPlanned == 0) {
            return 0.0;
        }
        return (totalSpent != null ? totalSpent : 0.0) / totalPlanned * 100;
    }

    /**
     * Calculate daily spending allowance to stay within budget
     * @return Daily spending allowance, or null if dates are invalid
     */
    public Double getDailySpendingAllowance() {
        try {
            if (startDate == null || endDate == null || totalPlanned == null) {
                return null;
            }
            
            java.time.LocalDate start = java.time.LocalDate.parse(startDate);
            java.time.LocalDate end = java.time.LocalDate.parse(endDate);
            java.time.LocalDate today = java.time.LocalDate.now();
            
            // If budget has ended, return 0
            if (today.isAfter(end)) {
                return 0.0;
            }
            
            // If budget hasn't started yet, calculate based on full period
            if (today.isBefore(start)) {
                long totalDays = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
                return totalPlanned / totalDays;
            }
            
            // Calculate remaining days from today to end date
            long remainingDays = java.time.temporal.ChronoUnit.DAYS.between(today, end) + 1;
            
            // If no remaining days, return 0
            if (remainingDays <= 0) {
                return 0.0;
            }
            
            // Calculate remaining budget and divide by remaining days
            Double remaining = getTotalRemaining();
            if (remaining <= 0) {
                return 0.0; // Already over budget
            }
            
            return remaining / remainingDays;
            
        } catch (Exception e) {
            // If date parsing fails, return null
            return null;
        }
    }

    /**
     * Get the number of days remaining in the budget period
     * @return Days remaining, or null if dates are invalid
     */
    public Long getDaysRemaining() {
        try {
            if (startDate == null || endDate == null) {
                return null;
            }
            
            java.time.LocalDate start = java.time.LocalDate.parse(startDate);
            java.time.LocalDate end = java.time.LocalDate.parse(endDate);
            java.time.LocalDate today = java.time.LocalDate.now();
            
            // If budget has ended, return 0
            if (today.isAfter(end)) {
                return 0L;
            }
            
            // If budget hasn't started yet, return full period
            if (today.isBefore(start)) {
                return java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
            }
            
            // Calculate remaining days from today to end date
            long remainingDays = java.time.temporal.ChronoUnit.DAYS.between(today, end) + 1;
            return Math.max(0, remainingDays);
            
        } catch (Exception e) {
            // If date parsing fails, return null
            return null;
        }
    }

    /**
     * Get the number of days elapsed in the budget period
     * @return Days elapsed, or null if dates are invalid
     */
    public Long getDaysElapsed() {
        try {
            if (startDate == null || endDate == null) {
                return null;
            }
            
            java.time.LocalDate start = java.time.LocalDate.parse(startDate);
            java.time.LocalDate end = java.time.LocalDate.parse(endDate);
            java.time.LocalDate today = java.time.LocalDate.now();
            
            // If budget hasn't started yet, return 0
            if (today.isBefore(start)) {
                return 0L;
            }
            
            // If budget has ended, return full period
            if (today.isAfter(end)) {
                return java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
            }
            
            // Calculate elapsed days from start to today
            long elapsedDays = java.time.temporal.ChronoUnit.DAYS.between(start, today) + 1;
            return Math.max(0, elapsedDays);
            
        } catch (Exception e) {
            // If date parsing fails, return null
            return null;
        }
    }

    @Override
    public String toString() {
        return "BudgetModel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", userId=" + userId +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", totalPlanned=" + totalPlanned +
                ", totalSpent=" + totalSpent +
                ", isActive=" + isActive +
                ", isRepeatable=" + isRepeatable +
                ", repeatInterval='" + repeatInterval + '\'' +
                ", repeatCount=" + repeatCount +
                '}';
    }
} 