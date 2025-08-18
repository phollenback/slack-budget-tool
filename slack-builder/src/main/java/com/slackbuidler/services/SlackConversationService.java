package com.slackbuidler.services;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slackbuidler.dao.TransactionDAO;
import com.slackbuidler.models.TransactionModel;
import com.slackbuidler.models.Category;
import com.slackbuidler.models.TransactionType;


@Service
public class SlackConversationService {
    
    @Autowired
    private SlackBot slackBot;
    
    @Autowired
    private TransactionDAO transactionsDAO;
    
    private final Map<String, ConversationState> userConversations = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Conversation flow states
    public enum ConversationStep {
        WAITING_FOR_AMOUNT,
        WAITING_FOR_VENDOR,
        WAITING_FOR_CATEGORY,
        COMPLETE
    }
    
    private static class ConversationState {
        ConversationStep step = ConversationStep.WAITING_FOR_AMOUNT;
        Double amount;
        String vendor;
        String type;
        String category;
        String userId; // User ID from Slack
        String channelId; // Channel ID (could be DM or channel)
        boolean isDirectMessage; // Flag to indicate if this is a DM
        
        public void reset() {
            step = ConversationStep.WAITING_FOR_AMOUNT;
            amount = null;
            vendor = null;
            type = null;
            category = null;
        }
        
        @Override
        public String toString() {
            return "ConversationState{" +
                    "step=" + step +
                    ", amount=" + amount +
                    ", vendor='" + vendor + '\'' +
                    ", type='" + type + '\'' +
                    ", category='" + category + '\'' +
                    ", userId='" + userId + '\'' +
                    ", channelId='" + channelId + '\'' +
                    ", isDirectMessage=" + isDirectMessage +
                    '}';
        }
    }
    
    public String handleSlackEvent(String payload) {
        try {
            // Handle null and empty/whitespace payloads
            if (payload == null || payload.trim().isEmpty()) {
                return "Error";
            }
            
            JsonNode event = objectMapper.readTree(payload);
            
            // Handle URL verification challenge
            if (event.has("challenge")) {
                return event.get("challenge").asText();
            }
            
            // Handle incoming message
            if (event.has("event") && event.get("event").has("type")) {
                JsonNode slackEvent = event.get("event");
                String eventType = slackEvent.get("type").asText();
                
                if ("message".equals(eventType) && slackEvent.has("text") && slackEvent.has("user")) {
                    String userId = slackEvent.get("user").asText();
                    String text = slackEvent.get("text").asText();
                    String channelId = slackEvent.get("channel").asText();
                    
                    // Skip bot messages
                    if (slackEvent.has("bot_id")) {
                        return "OK";
                    }
                    
                    // Check if this is a direct message (DM channels start with 'D')
                    boolean isDirectMessage = channelId.startsWith("D");
                    
                    // Handle the message
                    handleUserMessage(userId, text, channelId, isDirectMessage);
                }
            }
            
            return "OK";
        } catch (Exception e) {
            // Log error without stack trace for cleaner test output
            System.err.println("Error handling Slack event: " + e.getMessage());
            return "Error";
        }
    }
    
    private void handleUserMessage(String userId, String text, String channelId, boolean isDirectMessage) {
        ConversationState state = userConversations.computeIfAbsent(userId, k -> new ConversationState());
        // Set channel ID and DM flag if not already set
        if (state.channelId == null) {
            state.channelId = channelId;
            state.isDirectMessage = isDirectMessage;
        }
        state.userId = userId;
        
        // Check for commands
        if (text.toLowerCase().startsWith("submit") || text.toLowerCase().startsWith("new")) {
            startNewTransaction(userId, channelId, isDirectMessage);
            return;
        }
        
        if (text.toLowerCase().startsWith("cancel") || text.toLowerCase().startsWith("reset")) {
            cancelTransaction(userId, state);
            return;
        }
        
        // Only auto-detect amount and start transaction if user is not in an active conversation
        if (state.step == ConversationStep.WAITING_FOR_AMOUNT && state.amount == null && isAmountMessage(text)) {
            // This is a new conversation - user just sent amount
            startNewTransaction(userId, channelId, isDirectMessage);
            // Get the fresh state and handle the amount
            ConversationState freshState = userConversations.get(userId);
            handleAmountInput(userId, text, freshState);
            return;
        }
        
        // Handle conversation flow
        switch (state.step) {
            case WAITING_FOR_AMOUNT:
                handleAmountInput(userId, text, state);
                break;
            case WAITING_FOR_VENDOR:
                handleVendorInput(userId, text, state);
                break;
            case WAITING_FOR_CATEGORY:
                handleCategoryInput(userId, text, state);
                break;
            case COMPLETE:
                // Check if user sent an amount to auto-start a new transaction
                if (isAmountMessage(text)) {
                    startNewTransaction(userId, channelId, isDirectMessage);
                    ConversationState freshState = userConversations.get(userId);
                    handleAmountInput(userId, text, freshState);
                } else {
                    String message = getMessagePrefix(state) + "Type 'submit' to start a new transaction or 'cancel' to reset.";
                    slackBot.postMessage(message, state.channelId);
                }
                break;
            default:
                // Check if user sent an amount to auto-start
                if (isAmountMessage(text)) {
                    startNewTransaction(userId, channelId, isDirectMessage);
                    ConversationState freshState = userConversations.get(userId);
                    handleAmountInput(userId, text, freshState);
                } else {
                    String message = getMessagePrefix(state) + "Type 'submit' to start a new transaction or 'cancel' to reset.";
                    slackBot.postMessage(message, state.channelId);
                }
        }
    }
    
    private void startNewTransaction(String userId, String channelId, boolean isDirectMessage) {
        // Always create a fresh state for a new transaction
        ConversationState newState = new ConversationState();
        newState.userId = userId;
        newState.channelId = channelId;
        newState.isDirectMessage = isDirectMessage;
        
        // Replace the existing state
        userConversations.put(userId, newState);
        
        // Send message without user mention in DMs
        String message = getMessagePrefix(newState) + "Let's submit a new transaction! First, what's the amount? (e.g., 25.50)";
        slackBot.postMessage(message, channelId);
    }
    
    private void cancelTransaction(String userId, ConversationState state) {
        if (state != null) {
            String message = getMessagePrefix(state) + "Transaction cancelled. Type 'submit' to start over.";
            slackBot.postMessage(message, state.channelId);
            userConversations.remove(userId);
        }
    }
    
    private void handleAmountInput(String userId, String text, ConversationState state) {
        try {
            double amount = Double.parseDouble(text);
            if (amount <= 0) {
                String message = getMessagePrefix(state) + "Amount must be greater than 0. Please try again.";
                slackBot.postMessage(message, state.channelId);
                return;
            }
            
            state.amount = amount;
            state.step = ConversationStep.WAITING_FOR_VENDOR;
            String message = getMessagePrefix(state) + "Great! Amount: $" + amount + "\nNow, what's the vendor name?";
            slackBot.postMessage(message, state.channelId);
            
        } catch (NumberFormatException e) {
            String message = getMessagePrefix(state) + "Please enter a valid amount (e.g., 25.50)";
            slackBot.postMessage(message, state.channelId);
        }
    }
    
    private void handleVendorInput(String userId, String text, ConversationState state) {
        if (text.trim().isEmpty()) {
            String message = getMessagePrefix(state) + "Vendor name cannot be empty. Please try again.";
            slackBot.postMessage(message, state.channelId);
            return;
        }
        
        state.vendor = text.trim();
        state.step = ConversationStep.WAITING_FOR_CATEGORY;
        
        StringBuilder message = new StringBuilder();
        message.append("Vendor: ").append(state.vendor).append("\n");
        message.append("What category does this transaction fall under? Choose from:\n");
        
        // Show all budget categories - they work with any transaction type
        message.append("(1) Food & Dining\n");
        message.append("(2) Shopping\n");
        message.append("(3) Transportation\n");
        message.append("(4) Entertainment\n");
        message.append("(5) Utilities & Bills\n");
        message.append("(6) Healthcare\n");
        message.append("(7) Education\n");
        message.append("(8) Travel\n");
        
        message.append("\n💡 Tip: You can type the category name exactly as shown above, or use common variations like 'food' for 'Food & Dining'.");
        
        slackBot.postMessage(getMessagePrefix(state) + message.toString(), state.channelId);
    }

    private void handleCategoryInput(String userId, String text, ConversationState state) {
        if (text.trim().isEmpty()) {
            String message = getMessagePrefix(state) + "Category cannot be empty. Please try again.";
            slackBot.postMessage(message, state.channelId);
            return;
        }
        
        // Validate that the category is one of the budget categories
        Category selectedCategory = Category.fromDisplayName(text.trim());
        if (selectedCategory == null) {
            StringBuilder message = new StringBuilder();
            message.append("❌ Invalid category: '").append(text.trim()).append("'\n\n");
            message.append("Please choose from one of these budget categories:\n");
            message.append("(1) Food & Dining\n");
            message.append("(2) Shopping\n");
            message.append("(3) Transportation\n");
            message.append("(4) Entertainment\n");
            message.append("(5) Utilities & Bills\n");
            message.append("(6) Healthcare\n");
            message.append("(7) Education\n");
            message.append("(8) Travel\n");
            
            message.append("\n💡 Tip: You can type the category name exactly as shown above, or use common variations like 'food' for 'Food & Dining'.");
            
            slackBot.postMessage(getMessagePrefix(state) + message.toString(), state.channelId);
            return;
        }
        
        // Automatically derive the transaction type from the category
        state.category = selectedCategory.getDisplayName();
        state.type = selectedCategory.getDefaultTransactionType();
        
        state.step = ConversationStep.COMPLETE;
        
        // Complete the transaction
        completeTransaction(userId, state);
    }
    
    private void completeTransaction(String userId, ConversationState state) {
        // Get current date automatically
        String currentDate = java.time.LocalDate.now().toString();
        
        // Create and save the transaction
        try {
            TransactionModel transaction = new TransactionModel(
                null, // ID will be auto-generated
                state.amount,
                state.vendor,
                state.type, // Use the derived type from category
                currentDate,
                state.category, // Use the selected category
                null, // notes is null as requested
                1 // Default user ID for now
            );
            
            TransactionModel savedTransaction = transactionsDAO.createTransaction(transaction);
            
        } catch (Exception e) {
            System.err.println("Error saving transaction to database: " + e.getMessage());
            e.printStackTrace();
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("✅ Transaction submitted successfully!\n\n");
        summary.append("📊 Summary:\n");
        summary.append("💰 Amount: $").append(state.amount).append("\n");
        summary.append("🏪 Vendor: ").append(state.vendor).append("\n");
        summary.append("📝 Type: ").append(state.type).append("\n");
        summary.append("🏷️ Category: ").append(state.category).append("\n");
        summary.append("📅 Date: ").append(currentDate).append(" (auto-generated)\n");
        summary.append("📄 Notes: None\n");
        
        String summaryMessage = getMessagePrefix(state) + summary.toString();
        slackBot.postMessage(summaryMessage, state.channelId);
        
        // Send final message before cleaning up
        String finalMessage = getMessagePrefix(state) + "Type 'submit' to submit another transaction!";
        slackBot.postMessage(finalMessage, state.channelId);
        
        // Clean up the conversation after sending final message
        userConversations.remove(userId);
    }
    
    /**
     * Returns the appropriate message prefix based on whether it's a DM or channel message
     */
    private String getMessagePrefix(ConversationState state) {
        if (state.isDirectMessage) {
            return ""; // No prefix needed for DMs
        } else {
            return "<@" + state.userId + "> "; // Keep user mention for channel messages
        }
    }
    
    /**
     * Checks if a message contains a valid amount (number)
     * @param text The message text to check
     * @return true if the text represents a valid amount
     */
    private boolean isAmountMessage(String text) {
        try {
            double amount = Double.parseDouble(text.trim());
            return amount > 0; // Only positive amounts
        } catch (NumberFormatException e) {
            return false;
        }
    }
} 