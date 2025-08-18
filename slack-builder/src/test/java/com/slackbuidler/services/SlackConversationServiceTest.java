package com.slackbuidler.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.slackbuidler.dao.TransactionDAO;

@ExtendWith(MockitoExtension.class)
@DisplayName("SlackConversationService Tests")
class SlackConversationServiceTest {

    @Mock private SlackBot slackBot;
    @Mock private TransactionDAO transactionDAO;
    
    private SlackConversationService conversationService;

    @BeforeEach
    void setUp() {
        conversationService = new SlackConversationService();
        // Use reflection to set the mocked dependencies
        org.springframework.test.util.ReflectionTestUtils.setField(conversationService, "slackBot", slackBot);
        org.springframework.test.util.ReflectionTestUtils.setField(conversationService, "transactionsDAO", transactionDAO);
    }

    @Nested
    @DisplayName("URL Verification Tests")
    class UrlVerificationTests {
        
        @Test
        @DisplayName("should handle URL verification challenge correctly")
        void shouldHandleUrlVerificationChallengeCorrectly() {
            String payload = "{\"type\":\"url_verification\",\"challenge\":\"challenge_123\"}";
            String result = conversationService.handleSlackEvent(payload);
            assertEquals("challenge_123", result);
        }
    }

    @Nested
    @DisplayName("Event Handling Tests")
    class EventHandlingTests {
        
        @Test
        @DisplayName("should handle basic conversation flow")
        void shouldHandleBasicConversationFlow() {
            String userId = "U123456789";
            String channelId = "C1234567890";
            
            // Test URL verification
            String urlVerificationPayload = "{\"type\":\"url_verification\",\"challenge\":\"test_challenge\"}";
            String result = conversationService.handleSlackEvent(urlVerificationPayload);
            assertEquals("test_challenge", result);
        }

        @Test
        @DisplayName("should ignore bot messages")
        void shouldIgnoreBotMessages() {
            String payload = "{\"event\":{\"type\":\"message\",\"bot_id\":\"B123\",\"text\":\"bot message\"}}";
            String result = conversationService.handleSlackEvent(payload);
            assertEquals("OK", result);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("should handle malformed JSON gracefully")
        void shouldHandleMalformedJsonGracefully() {
            // Test with malformed JSON that will cause parsing error
            String invalidJson = "{\"invalid\": json}";
            String result = conversationService.handleSlackEvent(invalidJson);
            assertEquals("Error", result);
        }
        
        @Test
        @DisplayName("should handle null payload gracefully")
        void shouldHandleNullPayloadGracefully() {
            String result = conversationService.handleSlackEvent(null);
            assertEquals("Error", result);
        }
        
        @Test
        @DisplayName("should handle empty payload gracefully")
        void shouldHandleEmptyPayloadGracefully() {
            // Empty string should be treated as invalid JSON
            String result = conversationService.handleSlackEvent("");
            assertEquals("Error", result);
        }
        
        @Test
        @DisplayName("should handle malformed event structure gracefully")
        void shouldHandleMalformedEventStructureGracefully() {
            // Test with JSON that has wrong structure
            String malformedJson = "{\"event\":{\"type\":\"message\"}}";
            String result = conversationService.handleSlackEvent(malformedJson);
            assertEquals("OK", result);
        }
        
        @Test
        @DisplayName("should handle whitespace-only payload gracefully")
        void shouldHandleWhitespaceOnlyPayloadGracefully() {
            // Whitespace-only string should be treated as invalid
            String result = conversationService.handleSlackEvent("   ");
            assertEquals("Error", result);
        }
    }
} 