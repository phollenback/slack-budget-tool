package com.slackbuidler.models;

/**
 * Standardized transaction types that correspond to Slack offered types
 */
public enum TransactionType {
    PURCHASE("purchase"),
    REFUND("refund"),
    EXPENSE("expense"),
    INCOME("income"),
    TRANSFER("transfer"),
    OTHER("other");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get transaction type from display name
     */
    public static TransactionType fromDisplayName(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            return null;
        }
        
        for (TransactionType type : values()) {
            if (type.displayName.equalsIgnoreCase(displayName.trim())) {
                return type;
            }
        }
        
        // Try to match common variations
        String normalized = displayName.trim().toLowerCase();
        switch (normalized) {
            case "buy":
            case "bought":
            case "shopping":
                return PURCHASE;
            case "return":
            case "money back":
                return REFUND;
            case "cost":
            case "payment":
            case "bill":
                return EXPENSE;
            case "salary":
            case "wage":
            case "earnings":
            case "revenue":
                return INCOME;
            case "move":
            case "shift":
            case "exchange":
                return TRANSFER;
            case "misc":
            case "miscellaneous":
                return OTHER;
            default:
                return null;
        }
    }

    /**
     * Check if a string is a valid transaction type
     */
    public static boolean isValidTransactionType(String typeName) {
        return fromDisplayName(typeName) != null;
    }

    /**
     * Get all display names as an array
     */
    public static String[] getAllDisplayNames() {
        TransactionType[] types = values();
        String[] names = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            names[i] = types[i].displayName;
        }
        return names;
    }
} 