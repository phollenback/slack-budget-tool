package com.slackbuidler.models;

/**
 * Standardized categories for transactions with automatic type derivation
 */
public enum Category {
    FOOD_AND_DINING("Food & Dining", "purchase"),
    SHOPPING("Shopping", "purchase"),
    TRANSPORTATION("Transportation", "purchase"),
    ENTERTAINMENT("Entertainment", "purchase"),
    UTILITIES_AND_BILLS("Utilities & Bills", "expense"),
    HEALTHCARE("Healthcare", "expense"),
    EDUCATION("Education", "expense"),
    TRAVEL("Travel", "purchase");

    private final String displayName;
    private final String defaultTransactionType;

    Category(String displayName, String defaultTransactionType) {
        this.displayName = displayName;
        this.defaultTransactionType = defaultTransactionType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDefaultTransactionType() {
        return defaultTransactionType;
    }

    /**
     * Get category from display name
     */
    public static Category fromDisplayName(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            return null;
        }
        
        for (Category category : values()) {
            if (category.displayName.equalsIgnoreCase(displayName.trim())) {
                return category;
            }
        }
        
        // Try to match common variations
        String normalized = displayName.trim().toLowerCase();
        switch (normalized) {
            case "food":
            case "dining":
            case "restaurant":
            case "groceries":
                return FOOD_AND_DINING;
            case "shopping":
            case "retail":
            case "clothing":
            case "electronics":
                return SHOPPING;
            case "transport":
            case "gas":
            case "fuel":
            case "uber":
            case "lyft":
            case "taxi":
                return TRANSPORTATION;
            case "entertainment":
            case "movies":
            case "games":
            case "hobbies":
                return ENTERTAINMENT;
            case "utilities":
            case "bills":
            case "electricity":
            case "water":
            case "internet":
            case "phone":
                return UTILITIES_AND_BILLS;
            case "health":
            case "medical":
            case "pharmacy":
            case "doctor":
                return HEALTHCARE;
            case "education":
            case "school":
            case "training":
            case "courses":
                return EDUCATION;
            case "travel":
            case "vacation":
            case "hotel":
            case "airline":
                return TRAVEL;
            default:
                return null;
        }
    }

    /**
     * Check if a string is a valid category
     */
    public static boolean isValidCategory(String categoryName) {
        return fromDisplayName(categoryName) != null;
    }

    /**
     * Get all display names as an array
     */
    public static String[] getAllDisplayNames() {
        Category[] categories = values();
        String[] names = new String[categories.length];
        for (int i = 0; i < categories.length; i++) {
            names[i] = categories[i].displayName;
        }
        return names;
    }
} 