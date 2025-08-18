package com.slackbuidler;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

/**
 * Unit tests for the main App class
 */
@DisplayName("Application Tests")
class AppTest {
    
    @Nested
    @DisplayName("Basic Application Functionality")
    class BasicFunctionalityTests {
        
        @Test
        @DisplayName("should pass basic assertion test")
        void shouldPassBasicAssertionTest() {
            assertTrue(true);
        }
    }
}
