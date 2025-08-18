-- Clean up duplicate budget data
-- This script will help eliminate the duplicate budget entries you're seeing

-- First, let's see what duplicates we have
SELECT 
    name, 
    user_id, 
    start_date, 
    end_date, 
    COUNT(*) as duplicate_count
FROM budgets 
GROUP BY name, user_id, start_date, end_date 
HAVING COUNT(*) > 1
ORDER BY name, user_id;

-- Create a temporary table to keep only the first occurrence of each budget
CREATE TEMP TABLE temp_budgets AS
SELECT DISTINCT ON (name, user_id, start_date, end_date) *
FROM budgets
ORDER BY name, user_id, start_date, end_date, id;

-- Show what we're keeping
SELECT * FROM temp_budgets ORDER BY name, user_id;

-- Backup the original budgets table (optional but recommended)
-- CREATE TABLE budgets_backup AS SELECT * FROM budgets;

-- Delete all budgets
DELETE FROM budgets;

-- Insert the deduplicated data back
INSERT INTO budgets 
SELECT * FROM temp_budgets;

-- Clean up temporary table
DROP TABLE temp_budgets;

-- Verify the cleanup
SELECT 
    name, 
    user_id, 
    start_date, 
    end_date, 
    COUNT(*) as count_after_cleanup
FROM budgets 
GROUP BY name, user_id, start_date, end_date 
HAVING COUNT(*) > 1;

-- Show final result
SELECT 
    id,
    name,
    user_id,
    start_date,
    end_date,
    total_planned,
    total_spent,
    is_active
FROM budgets 
ORDER BY name, user_id, start_date;

-- Update budget totals to ensure consistency
-- This will recalculate totals based on actual category data
UPDATE budgets 
SET 
    total_planned = (
        SELECT COALESCE(SUM(planned_amount), 0) 
        FROM budget_categories 
        WHERE budget_categories.budget_id = budgets.id
    ),
    total_spent = (
        SELECT COALESCE(SUM(spent_amount), 0) 
        FROM budget_categories 
        WHERE budget_categories.budget_id = budgets.id
    ),
    updated_at = CURRENT_TIMESTAMP; 