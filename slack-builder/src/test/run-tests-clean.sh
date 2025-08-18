#!/bin/bash

# Super Clean Test Runner for Slack Builder
# This script runs tests with absolutely minimal output

echo "🧪 Running Tests (Super Clean Mode)"
echo "===================================="

# Change to project root directory (where pom.xml is located)
cd "$(dirname "$0")/../.." || exit 1

echo "📁 Running from: $(pwd)"

# Run tests with maximum suppression
mvn test -q \
    -Dorg.slf4j.simpleLogger.defaultLogLevel=error \
    -Dlogging.level.root=ERROR \
    -Dlogging.level.org.springframework.test=ERROR \
    -Dlogging.level.org.springframework.test.util.ReflectionTestUtils=ERROR \
    -Dlogging.level.com.fasterxml.jackson=ERROR \
    -Dlogging.level.com.slack.api=ERROR \
    -Dlogging.level.org.junit=ERROR \
    -Dlogging.level.org.junit.jupiter=ERROR \
    -Dlogging.level.com.slackbuidler=ERROR \
    -Dorg.slf4j.simpleLogger.log.org.springframework.test=ERROR \
    -Dorg.slf4j.simpleLogger.log.org.springframework.test.util.ReflectionTestUtils=ERROR \
    -Dorg.slf4j.simpleLogger.log.com.fasterxml.jackson=ERROR \
    -Dorg.slf4j.simpleLogger.log.com.slack.api=ERROR \
    -Dorg.slf4j.simpleLogger.log.org.junit=ERROR \
    -Dorg.slf4j.simpleLogger.log.org.junit.jupiter=ERROR \
    -Dorg.slf4j.simpleLogger.log.com.slackbuidler=ERROR \
    -Dorg.slf4j.simpleLogger.logFile=System.out \
    -Dorg.slf4j.simpleLogger.showDateTime=false \
    -Dorg.slf4j.simpleLogger.showThreadName=false \
    -Dorg.slf4j.simpleLogger.showLogName=false \
    -Dorg.slf4j.simpleLogger.showShortLogName=false \
    -Dorg.slf4j.simpleLogger.levelInBrackets=false \
    -Dorg.slf4j.simpleLogger.warnLevelString=WARN \
    -Dorg.slf4j.simpleLogger.errorLevelString=ERROR \
    -Dorg.slf4j.simpleLogger.infoLevelString=INFO \
    -Dorg.slf4j.simpleLogger.debugLevelString=DEBUG \
    -Dorg.slf4j.simpleLogger.traceLevelString=TRACE \
    -Dorg.slf4j.simpleLogger.defaultLogLevel=error \
    -Dorg.slf4j.simpleLogger.log.org.springframework.test=error \
    -Dorg.slf4j.simpleLogger.log.org.springframework.test.util.ReflectionTestUtils=error \
    -Dorg.slf4j.simpleLogger.log.com.fasterxml.jackson=error \
    -Dorg.slf4j.simpleLogger.log.com.slack.api=error \
    -Dorg.slf4j.simpleLogger.log.org.junit=error \
    -Dorg.slf4j.simpleLogger.log.org.junit.jupiter=error \
    -Dorg.slf4j.simpleLogger.log.com.slackbuidler=error

echo ""
echo "✅ Tests completed!"
echo "💡 Use 'mvn test' for full output, or './run-tests.sh verbose' for debugging" 