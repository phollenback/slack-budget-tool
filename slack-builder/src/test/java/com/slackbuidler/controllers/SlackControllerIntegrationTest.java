package com.slackbuidler.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.slackbuidler.services.SlackBot;
import com.slackbuidler.services.SlackConversationService;

@ExtendWith(MockitoExtension.class)
@DisplayName("SlackController Integration Tests")
class SlackControllerIntegrationTest {

    @Mock
    private SlackBot slackBot;

    @Mock
    private SlackConversationService slackConversationService;

    @InjectMocks
    private SlackController slackController;

    @Nested
    @DisplayName("Slack Events Endpoint Tests")
    class SlackEventsEndpointTests {
        
        @Test
        @DisplayName("should handle basic slack events correctly")
        void shouldHandleBasicSlackEventsCorrectly() {
            String payload = "{\"event\":{\"type\":\"message\",\"text\":\"25.50\",\"user\":\"U123456789\",\"channel\":\"C1234567890\",\"ts\":\"1234567890\"}}";
            
            when(slackConversationService.handleSlackEvent(payload)).thenReturn("OK");
            
            ResponseEntity<String> result = slackController.handleSlackEvents(payload);
            assertEquals("OK", result.getBody());
            assertEquals(200, result.getStatusCodeValue());
        }
    }

    @Nested
    @DisplayName("Message Endpoint Tests")
    class MessageEndpointTests {
        
        @Test
        @DisplayName("should post message successfully")
        void shouldPostMessageSuccessfully() {
            String message = "Hello";
            doNothing().when(slackBot).postMessage(message);
            
            ResponseEntity<String> result = slackController.postMessage(message);
            assertEquals("Message sent to Slack successfully!", result.getBody());
            assertEquals(200, result.getStatusCodeValue());
        }
    }

    @Nested
    @DisplayName("Test Endpoint Tests")
    class TestEndpointTests {
        
        @Test
        @DisplayName("should send test message successfully")
        void shouldSendTestMessageSuccessfully() {
            doNothing().when(slackBot).postMessage("Would you like to submit a purchase?");
            
            ResponseEntity<String> result = slackController.testMessage();
            assertEquals("Test message sent to Slack successfully!", result.getBody());
            assertEquals(200, result.getStatusCodeValue());
        }
    }
} 