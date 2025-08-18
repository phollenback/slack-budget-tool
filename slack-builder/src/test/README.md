# Test Organization Guide

This document describes the organization and structure of tests in the Slack Builder application.

## Test Structure Overview

All tests are organized using JUnit 5's `@Nested` and `@DisplayName` annotations to provide clear, hierarchical organization and readable test output.

## Test Organization Principles

### 1. **Descriptive Test Names**
- All test methods use descriptive names that clearly indicate what they test
- Test names follow the pattern: `should[ExpectedBehavior]When[Condition]`
- Example: `shouldReturnTransactionsAsList()`, `shouldHandleNullChannelGracefully()`

### 2. **Logical Test Grouping**
- Tests are grouped into logical suites using `@Nested` classes
- Each nested class represents a functional area or test category
- Nested classes use descriptive names with `@DisplayName`

### 3. **Clean Test Output**
- No console logging (`System.out.println`, `System.err.println`) in tests
- All test information is conveyed through assertions and test names
- Test failures provide clear, actionable information

## Test Organization by Package

### **Application Tests** (`com.slackbuidler`)
- **AppTest**: Basic application functionality tests
  - `BasicFunctionalityTests`: Core application behavior

### **Controller Tests** (`com.slackbuidler.controllers`)
- **SlackControllerIntegrationTest**: Slack API endpoint tests
  - `SlackEventsEndpointTests`: Event handling functionality
  - `MessageEndpointTests`: Message posting functionality
  - `TestEndpointTests`: Test endpoint functionality

- **TransactionControllerTest**: Transaction management endpoint tests
  - `GetTransactionsTests`: Transaction retrieval functionality
  - `GetTransactionByIdTests`: Individual transaction retrieval
  - `CreateTransactionTests`: Transaction creation functionality
  - `UpdateTransactionTests`: Transaction modification functionality
  - `DeleteTransactionTests`: Transaction deletion functionality

### **Service Tests** (`com.slackbuidler.services`)
- **SlackBotTest**: Slack bot functionality tests
  - `BotInitializationTests`: Bot creation and setup
  - `MessageHandlingTests`: Message processing behavior

- **SlackConversationServiceTest**: Conversation flow tests
  - `UrlVerificationTests`: Slack URL verification handling
  - `EventHandlingTests`: Event processing functionality
  - `ErrorHandlingTests`: Error scenario handling

- **TransactionServiceTest**: Transaction business logic tests
  - `TransactionTypeValidationTests`: Type validation logic
  - `TransactionModelValidationTests`: Model validation logic

### **Model Tests** (`com.slackbuidler.models`)
- **TransactionModelTest**: Transaction model behavior tests
  - `TransactionTypeNormalizationTests`: Type normalization logic

- **TransactionTypeTest**: Transaction type enum tests
  - `DisplayNameResolutionTests`: Display name resolution logic
  - `TransactionTypeValidationTests`: Type validation functionality
  - `DisplayNameRetrievalTests`: Display name retrieval methods

## Running Tests

### **Run All Tests**
```bash
mvn test
```

### **Run Specific Test Class**
```bash
mvn test -Dtest=SlackBotTest
```

### **Run Specific Test Method**
```bash
mvn test -Dtest=SlackBotTest#shouldCreateSlackBotInstanceSuccessfully
```

### **Run Tests with Verbose Output**
```bash
mvn test -Dtest=*Test -Dsurefire.useFile=false
```

## Test Output Format

When tests run, you'll see output like:

```
Application Tests
└── Basic Application Functionality
    └── should pass basic assertion test

SlackBot Service Tests
├── Bot Initialization Tests
│   └── should create SlackBot instance successfully
└── Message Handling Tests
    ├── should handle null channel gracefully
    └── should handle empty message gracefully

SlackConversationService Tests
├── URL Verification Tests
│   └── should handle URL verification challenge correctly
├── Event Handling Tests
│   ├── should handle basic conversation flow
│   └── should ignore bot messages
└── Error Handling Tests
    └── should handle invalid JSON gracefully
```

## Best Practices

### **Test Naming**
- Use descriptive method names that explain the expected behavior
- Include the condition or context in the test name
- Use consistent naming patterns across all test classes

### **Test Organization**
- Group related tests into logical nested classes
- Use `@DisplayName` to provide human-readable test descriptions
- Keep test methods focused on a single behavior or scenario

### **Assertions**
- Use specific assertions that clearly indicate what is being tested
- Avoid generic assertions like `assertTrue(true)`
- Include meaningful error messages in assertions when appropriate

### **Test Data**
- Use realistic test data that represents actual usage scenarios
- Create helper methods for common test data setup
- Keep test data minimal but sufficient for the test case

## Adding New Tests

When adding new tests:

1. **Follow the naming convention**: `should[Behavior]When[Condition]`
2. **Use appropriate nested classes** for logical grouping
3. **Add descriptive `@DisplayName`** annotations
4. **Avoid console logging** - use assertions instead
5. **Group related tests** into the same nested class
6. **Update this documentation** when adding new test categories

## Troubleshooting

### **Test Not Running**
- Ensure the test class name ends with `Test`
- Check that the test method is properly annotated with `@Test`
- Verify the test class is in the correct package structure

### **Test Organization Issues**
- Ensure nested classes use `@DisplayName` for clear labeling
- Check that test methods are in the appropriate nested class
- Verify test method names follow the naming convention

### **Build Issues**
- Run `mvn clean test` to ensure clean test execution
- Check for compilation errors in test classes
- Verify all required dependencies are available 