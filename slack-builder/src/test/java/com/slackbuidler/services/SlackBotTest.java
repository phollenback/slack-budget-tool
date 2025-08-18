package com.slackbuidler.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

@DisplayName("SlackBot Service Tests")
class SlackBotTest {

    @Nested
    @DisplayName("Bot Initialization Tests")
    class BotInitializationTests {
        
        @Test
        @DisplayName("should create SlackBot instance successfully")
        void shouldCreateSlackBotInstanceSuccessfully() {
            SlackBot bot = new SlackBot();
            assertNotNull(bot);
        }
    }

    @Nested
    @DisplayName("Message Handling Tests")
    class MessageHandlingTests {
        
        @Test
        @DisplayName("should handle null channel gracefully")
        void shouldHandleNullChannelGracefully() {
            SlackBot bot = new SlackBot();
            // Test that no exception is thrown when channel is null
            assertDoesNotThrow(() -> {
                bot.postMessage("Test message", null);
            });
        }

        @Test
        @DisplayName("should handle empty message gracefully")
        void shouldHandleEmptyMessageGracefully() {
            SlackBot bot = new SlackBot();
            // Test that no exception is thrown when message is empty
            assertDoesNotThrow(() -> {
                bot.postMessage("", "C123");
            });
        }
        
        @Test
        @DisplayName("should handle null message gracefully")
        void shouldHandleNullMessageGracefully() {
            SlackBot bot = new SlackBot();
            // Test that no exception is thrown when message is null
            assertDoesNotThrow(() -> {
                bot.postMessage(null, "C123");
            });
        }
        
        @Test
        @DisplayName("should handle valid message and channel")
        void shouldHandleValidMessageAndChannel() {
            SlackBot bot = new SlackBot();
            // Test that no exception is thrown with valid parameters
            assertDoesNotThrow(() -> {
                bot.postMessage("Valid test message", "C123456");
            });
        }
    }
}
