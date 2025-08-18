package com.slackbuidler.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SlackBot {
    
    @Value("${slack.bot.token:}")
    private String slackBotToken;
    
    @Value("${slack.channel.id:}")
    private String channelId;
    
    private static final String SLACK_API_URL = "https://slack.com/api/chat.postMessage";
    
    public void postMessage(String message) {
        postMessage(message, null);
    }
    
    public void postMessage(String message, String channelId) {
        // Handle null and empty messages gracefully
        if (message == null) {
            System.out.println("⚠️ Warning: Attempted to post null message to Slack - skipping");
            return;
        }
        
        if (message.trim().isEmpty()) {
            System.out.println("⚠️ Warning: Attempted to post empty message to Slack - skipping");
            return;
        }
        
        System.out.println("Attempting to post message to Slack: " + message);
        
        if (slackBotToken == null || slackBotToken.isEmpty()) {
            System.err.println("ERROR: Slack bot token is not configured!");
            return;
        }
        
        if (channelId == null || channelId.isEmpty()) {
            System.err.println("ERROR: Channel ID is not provided!");
            return;
        }
        
        try {
            URL url = new URL(SLACK_API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + slackBotToken);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            
            // Build JSON payload - channelId can be either a channel ID or user ID for DMs
            String jsonPayload = String.format("{\"channel\":\"%s\",\"text\":\"%s\"}", 
                channelId, 
                message.replace("\"", "\\\"").replace("\n", "\\n"));
            
            System.out.println("Sending payload: " + jsonPayload);
            System.out.println("Using API endpoint: " + SLACK_API_URL);
            
            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                outputStream.write(input, 0, input.length);
            }
            
            int responseCode = connection.getResponseCode();
            System.out.println("HTTP Response Code: " + responseCode);
            
            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                        responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream(), 
                        StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }
            
            String slackResponse = response.toString();
            System.out.println("Slack API Response: " + slackResponse);
            
            // Validate Slack API response
            if (responseCode == 200) {
                // Check if Slack API actually succeeded
                if (slackResponse.contains("\"ok\":true")) {
                    System.out.println("✅ Message posted successfully to Slack!");
                } else if (slackResponse.contains("\"ok\":false")) {
                    // Extract error details from Slack response
                    String errorMessage = "Unknown Slack API error";
                    if (slackResponse.contains("\"error\":")) {
                        int errorStart = slackResponse.indexOf("\"error\":") + 9;
                        int errorEnd = slackResponse.indexOf("\"", errorStart);
                        if (errorEnd > errorStart) {
                            errorMessage = slackResponse.substring(errorStart, errorEnd);
                        }
                    }
                    System.err.println("❌ Slack API failed: " + errorMessage);
                    System.err.println("Full response: " + slackResponse);
                    
                    // Log specific error details
                    if (slackResponse.contains("invalid_auth")) {
                        System.err.println("🔐 Authentication failed - check your bot token");
                    } else if (slackResponse.contains("unknown_method")) {
                        System.err.println("🔧 API method not found - check the endpoint URL");
                    } else if (slackResponse.contains("channel_not_found")) {
                        System.err.println("📢 Channel not found - check your channel ID");
                    } else if (slackResponse.contains("missing_scope")) {
                        System.err.println("🔑 Missing permissions - check your bot's OAuth scopes");
                    } else if (slackResponse.contains("cannot_dm_user")) {
                        System.err.println("💬 Cannot DM user - user may have DMs disabled or bot lacks permissions");
                    }
                } else {
                    System.err.println("⚠️ Unexpected Slack API response format");
                    System.err.println("Response: " + slackResponse);
                }
            } else {
                System.err.println("❌ HTTP request failed. Response code: " + responseCode);
                System.err.println("Error response: " + slackResponse);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error posting message to Slack: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Getter methods for testing
    public String getSlackBotToken() {
        return slackBotToken;
    }
    
    public String getChannelId() {
        return channelId;
    }
}
