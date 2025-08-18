# Slack Builder - TDD Practice Project

This project is designed to help you practice Test-Driven Development (TDD) with Java and Maven. It includes a comprehensive test suite for a Calculator class that you can use to learn and practice TDD principles.

## What is TDD?

Test-Driven Development is a software development approach where you write tests before writing the actual code. The TDD cycle follows these three steps:

1. **Red** - Write a failing test
2. **Green** - Write minimal code to make the test pass
3. **Refactor** - Improve the code while keeping tests green

## Project Structure

```
src/
├── main/java/com/slackbuidler/
│   ├── App.java                 # Main application class
│   └── Calculator.java          # Simple calculator for TDD practice
└── test/java/com/slackbuidler/
    ├── AppTest.java            # Basic test (JUnit 5)
    └── CalculatorTest.java     # Comprehensive calculator tests
```

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven 3.6 or higher

### Setup

1. Clone the repository
2. Navigate to the project directory
3. Run the tests to see them fail (Red phase):

```bash
mvn test
```

You should see test failures because the implementation classes have TODO comments and return default values.

## TDD Practice Exercise: Calculator Class

The `Calculator` class is your main TDD practice target. The tests are already written, but the implementation is incomplete.

**TDD Steps:**

1. **Red**: Run `CalculatorTest` - all tests should fail
2. **Green**: Implement the `add` method to make addition tests pass
3. **Refactor**: Clean up the code while keeping tests green
4. Repeat for `subtract`, `multiply`, and `divide` methods

**Implementation Order:**
1. Start with `add` method
2. Move to `subtract` method
3. Implement `multiply` method
4. Finally implement `divide` method with proper exception handling

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=CalculatorTest
```

### Run Tests with Coverage
```bash
mvn clean test jacoco:report
```

### Run Tests in Continuous Mode
```bash
mvn test -Dmaven.test.failure.ignore=true
```

## Test Framework Features

This project uses modern Java testing tools:

- **JUnit 5** - Modern testing framework with nested tests and parameterized tests
- **AssertJ** - Fluent assertion library for readable test assertions
- **Mockito** - Mocking framework for testing dependencies
- **Maven Surefire** - Test execution plugin

## TDD Best Practices

### 1. Write Tests First
Always write the test before implementing the feature. This ensures you understand the requirements.

### 2. Keep Tests Simple
Each test should verify one specific behavior. Use descriptive test names.

### 3. Follow the Red-Green-Refactor Cycle
- **Red**: Write a failing test
- **Green**: Write minimal code to pass the test
- **Refactor**: Clean up the code

### 4. Test Edge Cases
Don't just test the happy path. Test boundary conditions, invalid inputs, and error scenarios.

### 5. Use Descriptive Test Names
Test method names should clearly describe what they're testing:
```java
@Test
@DisplayName("should throw exception when dividing by zero")
void shouldThrowExceptionWhenDividingByZero() {
    // test implementation
}
```

### 6. Use Given-When-Then Structure
Structure your tests clearly:
```java
@Test
void shouldAddTwoPositiveNumbers() {
    // Given - setup test data
    int a = 5;
    int b = 3;
    
    // When - execute the method under test
    int result = calculator.add(a, b);
    
    // Then - verify the result
    assertThat(result).isEqualTo(8);
}
```

## Example TDD Session

Here's how a typical TDD session might look for implementing the `add` method:

### Step 1: Write First Test (Red)
```java
@Test
void shouldAddTwoPositiveNumbers() {
    int result = calculator.add(5, 3);
    assertThat(result).isEqualTo(8);
}
```

### Step 2: Run Test (Red)
The test fails because `add` returns 0.

### Step 3: Implement Minimal Code (Green)
```java
public int add(int a, int b) {
    return 8; // Hardcoded to make test pass
}
```

### Step 4: Add More Tests (Red)
```java
@Test
void shouldAddPositiveAndNegativeNumbers() {
    int result = calculator.add(10, -3);
    assertThat(result).isEqualTo(7);
}
```

### Step 5: Implement Proper Logic (Green)
```java
public int add(int a, int b) {
    return a + b;
}
```

### Step 6: Refactor
The code is already clean, so no refactoring needed.

## Common TDD Patterns

### 1. Parameterized Tests
Use `@ParameterizedTest` for testing multiple input combinations:
```java
@ParameterizedTest
@CsvSource({
    "0, 0, 0",
    "1, 1, 2",
    "100, 200, 300"
})
void shouldAddVariousNumberCombinations(int a, int b, int expected) {
    int result = calculator.add(a, b);
    assertThat(result).isEqualTo(expected);
}
```

### 2. Nested Tests
Use `@Nested` to organize related tests:
```java
@Nested
@DisplayName("Addition Tests")
class AdditionTests {
    // addition-related tests
}
```

### 3. Exception Testing
Test that exceptions are thrown correctly:
```java
@Test
void shouldThrowExceptionWhenDividingByZero() {
    assertThatThrownBy(() -> calculator.divide(10, 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cannot divide by zero");
}
```

## Current Status

**Calculator Class - ALL TESTS PASSING!**
- **Results**: 24 tests run, 0 failures, 0 errors, 0 skipped
- **Addition Tests**: 8/8 passing ✅
- **Subtraction Tests**: 4/4 passing ✅  
- **Multiplication Tests**: 4/4 passing ✅
- **Division Tests**: 5/5 passing ✅
- **Edge Cases**: 2/2 passing ✅

## Next Steps

After completing the Calculator exercise:

1. **Add More Features**: Extend the Calculator with new functionality
2. **Create New Classes**: Practice TDD with new business logic classes
3. **Learn Mocking**: Use Mockito to test classes with dependencies
4. **Integration Testing**: Test how components work together
5. **Performance Testing**: Add performance-related tests

## Resources

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [TDD by Example - Kent Beck](https://www.amazon.com/Test-Driven-Development-Kent-Beck/dp/0321146530)

## Contributing

Feel free to:
- Add new test cases
- Improve existing tests
- Add new classes for TDD practice
- Share your TDD experiences and tips

Happy TDD practicing! 🚀 