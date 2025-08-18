package com.slackbuidler;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Test Utilities for Clean Test Execution
 * 
 * This class provides utilities to reduce verbose output and clean up test logs.
 */
public class TestUtils {
    
    /**
     * JUnit 5 extension to set up clean test environment
     */
    public static class CleanTestEnvironment implements BeforeAllCallback {
        
        @Override
        public void beforeAll(ExtensionContext context) throws Exception {
            // Set system properties to reduce verbose logging during tests
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "warn");
            System.setProperty("logging.level.org.springframework.test", "WARN");
            System.setProperty("logging.level.org.springframework.test.util.ReflectionTestUtils", "WARN");
            System.setProperty("logging.level.com.fasterxml.jackson", "WARN");
            System.setProperty("logging.level.com.slack.api", "WARN");
            
            // Disable console output for tests
            System.setProperty("java.util.logging.config.file", "src/test/resources/logging.properties");
        }
    }
    
    /**
     * Suppress System.err output during test execution
     */
    public static void suppressSystemErr() {
        System.setErr(new java.io.PrintStream(new java.io.ByteArrayOutputStream()) {
            @Override
            public void write(byte[] buf, int off, int len) {
                // Suppress error output during tests
            }
        });
    }
    
    /**
     * Suppress System.out output during test execution
     */
    public static void suppressSystemOut() {
        System.setOut(new java.io.PrintStream(new java.io.ByteArrayOutputStream()) {
            @Override
            public void write(byte[] buf, int off, int len) {
                // Suppress standard output during tests
            }
        });
    }
    
    /**
     * Restore original System.out and System.err
     */
    public static void restoreSystemStreams() {
        System.setOut(System.out);
        System.setErr(System.err);
    }
} 