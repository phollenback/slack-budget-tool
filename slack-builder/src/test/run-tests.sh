#!/bin/bash

# Slack Builder Test Runner Script
# This script provides convenient ways to run tests in organized groups with clean output

# Change to project root directory (where pom.xml is located)
cd "$(dirname "$0")/../.." || exit 1

echo "🚀 Slack Builder Test Runner"
echo "=============================="
echo "📁 Running from: $(pwd)"
echo ""

# Function to run tests with clean output
run_tests() {
    local test_pattern="$1"
    local description="$2"
    
    echo "🧪 Running $description..."
    echo "----------------------------------------"
    
    if [ -n "$test_pattern" ]; then
        # Use clean test execution with aggressive logging suppression
        mvn test -Dtest="$test_pattern" -q \
            -Dorg.slf4j.simpleLogger.defaultLogLevel=error \
            -Dlogging.level.root=ERROR \
            -Dlogging.level.org.springframework.test=ERROR \
            -Dlogging.level.org.springframework.test.util.ReflectionTestUtils=ERROR \
            -Dlogging.level.com.fasterxml.jackson=ERROR \
            -Dlogging.level.com.slack.api=ERROR \
            -Dlogging.level.org.junit=ERROR \
            -Dlogging.level.org.junit.jupiter=ERROR
            
    else
        # Run all tests with clean output
        mvn test -q \
            -Dorg.slf4j.simpleLogger.defaultLogLevel=error \
            -Dlogging.level.root=ERROR \
            -Dlogging.level.org.springframework.test=ERROR \
            -Dlogging.level.org.springframework.test.util.ReflectionTestUtils=ERROR \
            -Dlogging.level.com.fasterxml.jackson=ERROR \
            -Dlogging.level.com.slack.api=ERROR \
            -Dlogging.level.org.junit=ERROR \
            -Dlogging.level.org.junit.jupiter=ERROR
    fi
    
    echo "----------------------------------------"
    echo ""
}

# Function to run tests with verbose output (for debugging)
run_tests_verbose() {
    local test_pattern="$1"
    local description="$2"
    
    echo "🔍 Running $description with verbose output..."
    echo "----------------------------------------"
    
    if [ -n "$test_pattern" ]; then
        mvn test -Dtest="$test_pattern" -Dorg.slf4j.simpleLogger.defaultLogLevel=info
    else
        mvn test -Dorg.slf4j.simpleLogger.defaultLogLevel=info
    fi
    
    echo "----------------------------------------"
    echo ""
}

# Function to show test organization
show_test_organization() {
    echo "📋 Test Organization Overview:"
    echo "=============================="
    echo ""
    echo "🔧 Application Tests:"
    echo "  • AppTest - Basic application functionality"
    echo ""
    echo "🎮 Controller Tests:"
    echo "  • SlackControllerIntegrationTest - Slack API endpoints"
    echo "  • TransactionControllerTest - Transaction management"
    echo ""
    echo "⚙️  Service Tests:"
    echo "  • SlackBotTest - Bot functionality"
    echo "  • SlackConversationServiceTest - Conversation flow"
    echo "  • TransactionServiceTest - Transaction business logic"
    echo ""
    echo "📊 Model Tests:"
    echo "  • TransactionModelTest - Transaction model behavior"
    echo "  • TransactionTypeTest - Transaction type functionality"
    echo ""
}

# Function to show available commands
show_help() {
    echo "📖 Available Commands:"
    echo "======================"
    echo ""
    echo "  all          - Run all tests (clean output)"
    echo "  controllers  - Run all controller tests"
    echo "  services     - Run all service tests"
    echo "  models       - Run all model tests"
    echo "  slack        - Run Slack-related tests"
    echo "  transactions - Run transaction-related tests"
    echo "  verbose      - Run all tests with verbose output"
    echo "  help         - Show this help message"
    echo "  info         - Show test organization"
    echo ""
    echo "Examples:"
    echo "  ./run-tests.sh all"
    echo "  ./run-tests.sh controllers"
    echo "  ./run-tests.sh verbose"
    echo ""
    echo "💡 Clean Output Options:"
    echo "  • Default mode reduces verbose logging and stack traces"
    echo "  • Use 'verbose' mode for debugging test issues"
    echo "  • Check src/test/README.md for detailed documentation"
    echo ""
}

# Main script logic
case "${1:-help}" in
    "all")
        echo "🎯 Running ALL Tests (Clean Output)"
        echo "==================================="
        echo ""
        run_tests "*Test" "all tests"
        ;;
    "controllers")
        echo "🎮 Running Controller Tests"
        echo "==========================="
        echo ""
        run_tests "*Controller*Test" "controller tests"
        ;;
    "services")
        echo "⚙️  Running Service Tests"
        echo "========================"
        echo ""
        run_tests "*Service*Test" "service tests"
        ;;
    "models")
        echo "📊 Running Model Tests"
        echo "======================"
        echo ""
        run_tests "*Model*Test" "model tests"
        ;;
    "slack")
        echo "💬 Running Slack-Related Tests"
        echo "=============================="
        echo ""
        run_tests "*Slack*Test" "Slack-related tests"
        ;;
    "transactions")
        echo "💰 Running Transaction-Related Tests"
        echo "==================================="
        echo ""
        run_tests "*Transaction*Test" "transaction-related tests"
        ;;
    "verbose")
        echo "🔍 Running ALL Tests (Verbose Output)"
        echo "====================================="
        echo ""
        run_tests_verbose "*Test" "all tests with verbose output"
        ;;
    "info")
        show_test_organization
        ;;
    "help"|*)
        show_help
        ;;
esac

echo "✅ Test execution completed!"
echo ""
echo "💡 Tips:"
echo "  • Use 'clean' mode (default) for reduced output"
echo "  • Use 'verbose' mode for debugging test issues"
echo "  • Check src/test/README.md for detailed documentation"
echo "  • Use 'mvn test -q' for minimal Maven output" 