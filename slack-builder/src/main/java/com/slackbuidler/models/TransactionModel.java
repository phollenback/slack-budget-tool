package com.slackbuidler.models;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;



@Entity
@Table(name = "transactions")
public class TransactionModel 
{  
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "Amount cannot be null")
    @Column(name = "amount", nullable = false)
    private Double amount;

    @NotNull(message = "Vendor cannot be null")
    @Size(min = 1, message = "Vendor cannot be empty")
    @Column(name = "vendor", nullable = false, length = 255)
    private String vendor;

    @NotNull(message = "Type cannot be null")
    @Size(min = 1, message = "Type cannot be empty")
    @Column(name = "type", nullable = false, length = 50)
    private String type;
    
    @NotNull(message = "Date cannot be null")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date must be in format yyyy-MM-dd")
    @Column(name = "date", nullable = false, length = 10)
    private String date;
    
    @Column(name = "category", length = 100)
    private String category;
    
    @Column(name = "notes", length = 500)
    private String notes;
    
    @NotNull(message = "User ID cannot be null")
    @Min(value = 1, message = "User ID must be greater than 0")
    @Column(name = "user_id", nullable = false)
    private int userId;

    // constructor
    public TransactionModel(Long id, Double amount, String vendor, String type, String date, String category, String notes, int userId) {
        this.id = id;
        this.amount = amount;
        this.vendor = vendor;
        this.type = normalizeType(type);
        this.date = date;
        this.category = normalizeCategory(category);
        this.notes = notes;
        this.userId = userId;
    }

    // no-argument constructor required by JPA/Hibernate
    public TransactionModel() {
    }

    // to string
    @Override
    public String toString() {
        return "TransactionModel [id=" + id + ", amount=" + amount + ", vendor=" + vendor + ", type=" + type + ", date=" + date + ", category=" + category + ", notes=" + notes + ", userId=" + userId + "]";
    }

    // getters 
    public Long getId() {
        return id;
    }

    public Double getAmount() {
        return amount;
    }

    public String getVendor() {
        return vendor;
    }

    public String getType() {
        return type;
    }

    public String getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public String getNotes() {
        return notes;
    }

    public int getUserId() {
        return userId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public void setType(String type) {
        this.type = normalizeType(type);
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setCategory(String category) {
        this.category = normalizeCategory(category);
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * Normalize category to standardized format
     */
    private String normalizeCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return null;
        }
        
        Category normalizedCategory = Category.fromDisplayName(category);
        return normalizedCategory != null ? normalizedCategory.getDisplayName() : category.trim();
    }

    /**
     * Normalize transaction type to standardized format
     */
    private String normalizeType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return null;
        }
        
        TransactionType normalizedType = TransactionType.fromDisplayName(type);
        return normalizedType != null ? normalizedType.getDisplayName() : type.trim().toLowerCase();
    }

    /**
     * Check if the category is valid
     */
    public boolean hasValidCategory() {
        return Category.isValidCategory(this.category);
    }

    /**
     * Check if the transaction type is valid
     */
    public boolean hasValidType() {
        return TransactionType.isValidTransactionType(this.type);
    }
}

