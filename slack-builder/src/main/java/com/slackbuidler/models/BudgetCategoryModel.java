package com.slackbuidler.models;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "budget_categories")
public class BudgetCategoryModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "budget_id", nullable = false)
    private Long budgetId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "planned_amount", nullable = false, precision = 10, scale = 2)
    private Double plannedAmount;

    @Column(name = "spent_amount", precision = 10, scale = 2)
    private Double spentAmount;

    @Column(name = "color", length = 7)
    private String color;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Default constructor
    public BudgetCategoryModel() {
        this.spentAmount = 0.0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Constructor with required fields
    public BudgetCategoryModel(Long budgetId, String name, Double plannedAmount) {
        this();
        this.budgetId = budgetId;
        this.name = name;
        this.plannedAmount = plannedAmount;
    }

    // Constructor with all fields
    public BudgetCategoryModel(Long budgetId, String name, Double plannedAmount, Double spentAmount, String color) {
        this(budgetId, name, plannedAmount);
        this.spentAmount = spentAmount != null ? spentAmount : 0.0;
        this.color = color;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(Long budgetId) {
        this.budgetId = budgetId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPlannedAmount() {
        return plannedAmount;
    }

    public void setPlannedAmount(Double plannedAmount) {
        this.plannedAmount = plannedAmount;
    }

    public Double getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(Double spentAmount) {
        this.spentAmount = spentAmount;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
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
    public Double getRemainingAmount() {
        if (plannedAmount == null || spentAmount == null) {
            return 0.0;
        }
        return plannedAmount - spentAmount;
    }

    public boolean isOverBudget() {
        return getRemainingAmount() < 0;
    }

    public double getSpentPercentage() {
        if (plannedAmount == null || plannedAmount == 0) {
            return 0.0;
        }
        return (spentAmount != null ? spentAmount : 0.0) / plannedAmount * 100;
    }

    public boolean isNearLimit() {
        return getSpentPercentage() >= 80.0;
    }

    @Override
    public String toString() {
        return "BudgetCategoryModel{" +
                "id=" + id +
                ", budgetId=" + budgetId +
                ", name='" + name + '\'' +
                ", plannedAmount=" + plannedAmount +
                ", spentAmount=" + spentAmount +
                ", color='" + color + '\'' +
                '}';
    }
} 