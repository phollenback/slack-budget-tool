# Daily Spending Allowance Feature

## Overview
The Budget Planner now includes a **Daily Spending Allowance** calculation that shows how much can be spent per day to stay within budget. This feature helps users understand their daily spending limits and make informed financial decisions.

## Features Added

### 1. Backend Data Model Enhancements

#### BudgetModel.java
- **`getDailySpendingAllowance()`**: Calculates daily spending allowance based on remaining budget and days
- **`getDaysRemaining()`**: Returns number of days left in budget period
- **`getDaysElapsed()`**: Returns number of days elapsed in budget period

#### BudgetSummaryDTO.java
- **`dailySpendingAllowance`**: Daily spending amount in dollars
- **`daysRemaining`**: Number of days left in budget period
- **`daysElapsed`**: Number of days elapsed in budget period
- **`formattedDailySpendingAllowance`**: Formatted string for display
- **`dailySpendingDescription`**: Human-readable description of daily spending situation

### 2. Frontend Interface Enhancements

#### Budget List Items
- Shows daily allowance for each budget
- Color-coded indicators:
  - 🟢 **Green**: Normal daily allowance
  - 🟡 **Yellow**: Near limit (less than 10% of total planned)
  - 🔴 **Red**: Over budget (no daily allowance available)

#### Budget Details Modal
- **Daily Allowance Card**: Shows current daily spending limit
- **Days Left Card**: Shows remaining days in budget period
- Enhanced overview with 6 cards instead of 4

#### Responsive Design
- Mobile-optimized layout for daily allowance display
- Consistent styling across all screen sizes

## How It Works

### Daily Allowance Calculation Logic

1. **Future Budgets** (not started yet):
   - `Daily Allowance = Total Planned ÷ Total Days`

2. **Current Budgets** (in progress):
   - `Daily Allowance = Remaining Budget ÷ Remaining Days`

3. **Over Budget**:
   - `Daily Allowance = $0.00` (Budget exceeded)

4. **Completed Budgets**:
   - `Daily Allowance = $0.00` (Period ended)

### Example Scenarios

#### Scenario 1: Monthly Budget
- **Total Planned**: $3,000
- **Period**: 30 days
- **Days Remaining**: 15 days
- **Amount Spent**: $1,500
- **Daily Allowance**: $100.00 per day for 15 days

#### Scenario 2: Over Budget
- **Total Planned**: $1,000
- **Period**: 10 days
- **Days Remaining**: 5 days
- **Amount Spent**: $1,200
- **Daily Allowance**: Budget exceeded

#### Scenario 3: Future Budget
- **Total Planned**: $2,000
- **Period**: 14 days (starts tomorrow)
- **Daily Allowance**: $142.86 per day for 14 days

## Technical Implementation

### Backend
- **Date Handling**: Uses Java 8 `LocalDate` for accurate date calculations
- **Error Handling**: Gracefully handles invalid dates and edge cases
- **Performance**: Efficient calculations with minimal overhead

### Frontend
- **Fallback Logic**: Calculates values locally if backend doesn't provide them
- **Real-time Updates**: Reflects changes immediately when budget data updates
- **Type Safety**: Full TypeScript support with proper interfaces

### Data Flow
1. Backend calculates daily allowance when budget data is requested
2. Frontend receives data via API calls
3. UI displays formatted values with appropriate styling
4. Fallback calculations ensure display even if backend data is incomplete

## Benefits

### For Users
- **Clear Daily Limits**: Know exactly how much to spend each day
- **Better Planning**: Make informed decisions about daily purchases
- **Budget Awareness**: Understand spending patterns and remaining capacity
- **Motivation**: See progress toward budget goals

### For Developers
- **Extensible Design**: Easy to add more time-based calculations
- **Consistent API**: Follows existing data model patterns
- **Test Coverage**: Comprehensive unit tests for all calculations
- **Error Handling**: Robust handling of edge cases and invalid data

## Future Enhancements

### Potential Additions
1. **Weekly Allowance**: Show spending limits for 7-day periods
2. **Category-specific Limits**: Daily limits per budget category
3. **Spending Alerts**: Notifications when approaching daily limits
4. **Historical Tracking**: Compare actual vs. planned daily spending
5. **Rollover Logic**: Handle unused daily allowances

### Integration Opportunities
1. **Transaction Integration**: Link daily spending to actual transactions
2. **Notification System**: Alert users about daily spending status
3. **Reporting**: Include daily allowance data in budget reports
4. **Mobile App**: Optimize daily allowance display for mobile devices

## Testing

### Backend Tests
- **BudgetModelTest**: 7 test cases covering all calculation scenarios
- **Edge Cases**: Invalid dates, over-budget situations, completed budgets
- **Accuracy**: Verifies calculations match expected results

### Frontend Tests
- **Component Tests**: Verify display logic and fallback calculations
- **Integration Tests**: Ensure API data flows correctly to UI
- **Responsive Tests**: Validate mobile and desktop layouts

## Usage Examples

### API Response
```json
{
  "id": 1,
  "name": "Monthly Budget",
  "dailySpendingAllowance": 100.0,
  "daysRemaining": 15,
  "daysElapsed": 15,
  "formattedDailySpendingAllowance": "$100.00",
  "dailySpendingDescription": "$100.00 per day for 15 days"
}
```

### Frontend Display
- **Budget List**: "Daily: $100.00 (15 days)"
- **Details Modal**: "Daily Allowance: $100.00" and "Days Left: 15"
- **Status Indicators**: Color-coded based on spending situation

## Conclusion

The Daily Spending Allowance feature transforms the Budget Planner from a static budget tracker into a dynamic spending guide. Users now have clear, actionable information about their daily spending limits, making it easier to stay within budget and achieve financial goals.

The implementation is robust, well-tested, and follows established patterns, ensuring easy maintenance and future enhancements.
