package com.slackbuidler.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.slackbuidler.services.SlackBot;
import com.slackbuidler.services.SlackConversationService;

@RestController
@RequestMapping("/slack")
public class SlackController {

    @Autowired
    private SlackBot slackBot;
    
    @Autowired
    private SlackConversationService conversationService;

    @PostMapping("/message")
    public ResponseEntity<String> postMessage(@RequestBody String message) {
        try {
            slackBot.postMessage(message);
            return ResponseEntity.ok("Message sent to Slack successfully!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to send message: " + e.getMessage());
        }
    }

    @PostMapping("/test")
    public ResponseEntity<String> testMessage() {
        try {
            slackBot.postMessage("Would you like to submit a purchase?");
            return ResponseEntity.ok("Test message sent to Slack successfully!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to send test message: " + e.getMessage());
        }
    }
    
    @PostMapping("/events")
    public ResponseEntity<String> handleSlackEvents(@RequestBody String payload) {
        try {
            String response = conversationService.handleSlackEvent(payload);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to handle Slack event: " + e.getMessage());
        }
    }
} 