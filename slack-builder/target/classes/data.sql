-- College Freshman Spending Data - Realistic amounts and time frames
-- This data covers weekly, monthly, and yearly spending patterns
-- Updated for current date: 08/16

-- Clear existing data (in reverse dependency order)
DELETE FROM budget_categories;
DELETE FROM budgets;
DELETE FROM transactions;

-- Insert realistic college freshman budgets FIRST (before categories)
INSERT INTO budgets (name, user_id, start_date, end_date, total_planned, total_spent, is_active, is_repeatable, repeat_interval, repeat_count) VALUES
('Monthly Budget - August 2025', 1, '2025-08-01', '2025-08-31', 800.00, 0.00, true, true, 'monthly', 12),
('Semester Budget - Fall 2025', 1, '2025-08-15', '2025-12-15', 3000.00, 0.00, true, false, null, 1),
('Emergency Fund', 1, '2025-01-01', '2025-12-31', 2000.00, 0.00, true, false, null, 1);

-- Insert realistic budget categories SECOND (after budgets exist)
-- Use subqueries to get the correct budget IDs dynamically
INSERT INTO budget_categories (budget_id, name, planned_amount, spent_amount, color) VALUES
-- Monthly Budget Categories (Realistic college freshman amounts)
((SELECT id FROM budgets WHERE name = 'Monthly Budget - August 2025' LIMIT 1), 'Food & Dining', 250.00, 0.00, '#FF6384'),
((SELECT id FROM budgets WHERE name = 'Monthly Budget - August 2025' LIMIT 1), 'Transportation', 120.00, 0.00, '#36A2EB'),
((SELECT id FROM budgets WHERE name = 'Monthly Budget - August 2025' LIMIT 1), 'Shopping', 150.00, 0.00, '#FFCE56'),
((SELECT id FROM budgets WHERE name = 'Monthly Budget - August 2025' LIMIT 1), 'Entertainment', 100.00, 0.00, '#4BC0C0'),
((SELECT id FROM budgets WHERE name = 'Monthly Budget - August 2025' LIMIT 1), 'Utilities', 80.00, 0.00, '#9966FF'),
((SELECT id FROM budgets WHERE name = 'Monthly Budget - August 2025' LIMIT 1), 'Healthcare', 50.00, 0.00, '#FF9F40'),
((SELECT id FROM budgets WHERE name = 'Monthly Budget - August 2025' LIMIT 1), 'Education', 50.00, 0.00, '#C9CBCF'),

-- Semester Budget Categories
((SELECT id FROM budgets WHERE name = 'Semester Budget - Fall 2025' LIMIT 1), 'Tuition', 1200.00, 0.00, '#FF6384'),
((SELECT id FROM budgets WHERE name = 'Semester Budget - Fall 2025' LIMIT 1), 'Textbooks', 450.00, 0.00, '#36A2EB'),
((SELECT id FROM budgets WHERE name = 'Semester Budget - Fall 2025' LIMIT 1), 'Supplies', 200.00, 0.00, '#FFCE56'),
((SELECT id FROM budgets WHERE name = 'Semester Budget - Fall 2025' LIMIT 1), 'Transportation', 300.00, 0.00, '#4BC0C0'),
((SELECT id FROM budgets WHERE name = 'Semester Budget - Fall 2025' LIMIT 1), 'Food & Dining', 500.00, 0.00, '#9966FF'),
((SELECT id FROM budgets WHERE name = 'Semester Budget - Fall 2025' LIMIT 1), 'Entertainment', 200.00, 0.00, '#FF9F40'),
((SELECT id FROM budgets WHERE name = 'Semester Budget - Fall 2025' LIMIT 1), 'Other', 150.00, 0.00, '#C9CBCF'),

-- Emergency Fund Categories
((SELECT id FROM budgets WHERE name = 'Emergency Fund' LIMIT 1), 'Medical Emergency', 800.00, 0.00, '#FF6384'),
((SELECT id FROM budgets WHERE name = 'Emergency Fund' LIMIT 1), 'Car Repair', 600.00, 0.00, '#36A2EB'),
((SELECT id FROM budgets WHERE name = 'Emergency Fund' LIMIT 1), 'School Emergency', 400.00, 0.00, '#FFCE56'),
((SELECT id FROM budgets WHERE name = 'Emergency Fund' LIMIT 1), 'Other Emergency', 200.00, 0.00, '#4BC0C0');

-- Insert realistic college freshman transactions LAST (after all budget data exists)
INSERT INTO transactions (amount, vendor, type, date, category, notes, user_id) VALUES
-- RECENT WEEKLY TRANSACTIONS (Last 7 days - small amounts)
(8.50, 'Campus Coffee Shop', 'purchase', '2025-08-16', 'Food & Dining', 'Morning coffee', 1),
(12.75, 'Student Union Cafeteria', 'purchase', '2025-08-15', 'Food & Dining', 'Lunch', 1),
(6.25, 'Vending Machine', 'purchase', '2025-08-14', 'Food & Dining', 'Snacks', 1),
(15.00, 'Pizza Place', 'purchase', '2025-08-13', 'Food & Dining', 'Dinner with friends', 1),
(3.50, 'Campus Store', 'purchase', '2025-08-12', 'Shopping', 'Notebook', 1),
(22.00, 'Gas Station', 'purchase', '2025-08-11', 'Transportation', 'Gas for car', 1),
(8.99, 'Netflix', 'purchase', '2025-08-10', 'Entertainment', 'Monthly subscription', 1),
(18.50, 'Movie Theater', 'purchase', '2025-08-09', 'Entertainment', 'Movie with roommate', 1),

-- MONTHLY TRANSACTIONS (Last 30 days - moderate amounts)
(45.00, 'Textbook Store', 'purchase', '2025-08-05', 'Education', 'Chemistry textbook', 1),
(35.00, 'Campus Parking', 'purchase', '2025-08-01', 'Transportation', 'Monthly parking pass', 1),
(120.00, 'Phone Bill', 'purchase', '2025-07-25', 'Utilities', 'Cell phone service', 1),
(85.00, 'Internet Service', 'purchase', '2025-07-20', 'Utilities', 'Dorm internet', 1),
(65.00, 'Grocery Store', 'purchase', '2025-07-18', 'Food & Dining', 'Weekly groceries', 1),
(25.00, 'Laundry Service', 'purchase', '2025-07-15', 'Utilities', 'Laundry money', 1),
(40.00, 'Target', 'purchase', '2025-07-12', 'Shopping', 'Dorm supplies', 1),
(15.00, 'CVS', 'purchase', '2025-07-10', 'Healthcare', 'Cold medicine', 1),
(30.00, 'Campus Gym', 'purchase', '2025-07-08', 'Entertainment', 'Gym membership', 1),
(50.00, 'Amazon', 'purchase', '2025-07-05', 'Shopping', 'Desk lamp', 1),
(28.00, 'Restaurant', 'purchase', '2025-07-01', 'Food & Dining', 'Birthday dinner', 1),
(12.00, 'Starbucks', 'purchase', '2025-06-30', 'Food & Dining', 'Study coffee', 1),
(75.00, 'Gas Station', 'purchase', '2025-06-28', 'Transportation', 'Multiple gas fill-ups', 1),
(20.00, 'Campus Bookstore', 'purchase', '2025-06-25', 'Shopping', 'School supplies', 1),
(45.00, 'Pizza Delivery', 'purchase', '2025-06-22', 'Food & Dining', 'Study group dinner', 1),
(35.00, 'Walmart', 'purchase', '2025-06-20', 'Shopping', 'Cleaning supplies', 1),
(15.00, 'Campus Vending', 'purchase', '2025-06-18', 'Food & Dining', 'Various snacks', 1),
(60.00, 'Restaurant', 'purchase', '2025-06-15', 'Food & Dining', 'Family visit dinner', 1),
(25.00, 'Gas Station', 'purchase', '2025-06-12', 'Transportation', 'Gas', 1),
(40.00, 'Target', 'purchase', '2025-06-10', 'Shopping', 'Storage bins', 1),
(18.00, 'Campus Cafeteria', 'purchase', '2025-06-08', 'Food & Dining', 'Meal plan usage', 1),
(30.00, 'Amazon', 'purchase', '2025-06-05', 'Shopping', 'Phone charger', 1),
(22.00, 'Restaurant', 'purchase', '2025-06-02', 'Food & Dining', 'Lunch with friends', 1),
(50.00, 'Gas Station', 'purchase', '2025-05-30', 'Transportation', 'Gas', 1),
(15.00, 'Campus Store', 'purchase', '2025-05-28', 'Shopping', 'Notebooks', 1),
(35.00, 'Restaurant', 'purchase', '2025-05-25', 'Food & Dining', 'Dinner', 1),
(20.00, 'Walmart', 'purchase', '2025-05-22', 'Shopping', 'Toiletries', 1),
(12.00, 'Campus Coffee', 'purchase', '2025-05-20', 'Food & Dining', 'Coffee', 1),

-- YEARLY TRANSACTIONS (Last 365 days - larger amounts)
(1200.00, 'University', 'purchase', '2025-05-15', 'Education', 'Summer semester tuition', 1),
(800.00, 'University', 'purchase', '2025-01-15', 'Education', 'Spring semester tuition', 1),
(450.00, 'Bookstore', 'purchase', '2025-05-10', 'Education', 'Textbooks for summer', 1),
(380.00, 'Bookstore', 'purchase', '2025-01-10', 'Education', 'Textbooks for spring', 1),
(200.00, 'Computer Store', 'purchase', '2025-04-20', 'Shopping', 'Laptop repair', 1),
(150.00, 'Insurance Company', 'purchase', '2025-04-15', 'Insurance', 'Car insurance', 1),
(300.00, 'Dental Office', 'purchase', '2025-03-20', 'Healthcare', 'Dental cleaning', 1),
(180.00, 'Doctor Office', 'purchase', '2025-03-15', 'Healthcare', 'Physical exam', 1),
(250.00, 'Car Repair Shop', 'purchase', '2025-02-25', 'Transportation', 'Oil change and brakes', 1),
(120.00, 'Phone Store', 'purchase', '2025-02-20', 'Shopping', 'Phone case and screen protector', 1),
(400.00, 'Clothing Store', 'purchase', '2025-01-30', 'Shopping', 'Back to school clothes', 1),
(75.00, 'Amazon', 'purchase', '2025-01-25', 'Shopping', 'Dorm decorations', 1),
(90.00, 'Target', 'purchase', '2025-01-20', 'Shopping', 'Dorm essentials', 1),
(120.00, 'Gas Station', 'purchase', '2025-01-15', 'Transportation', 'Gas for move-in', 1),
(60.00, 'Restaurant', 'purchase', '2025-01-10', 'Food & Dining', 'Move-in celebration', 1),
(950.00, 'University', 'purchase', '2024-12-15', 'Education', 'Fall semester tuition', 1),
(320.00, 'Bookstore', 'purchase', '2024-12-10', 'Education', 'Textbooks for fall', 1),
(180.00, 'Computer Store', 'purchase', '2024-11-20', 'Shopping', 'Software licenses', 1),
(220.00, 'Insurance Company', 'purchase', '2024-11-15', 'Insurance', 'Health insurance', 1),
(160.00, 'Doctor Office', 'purchase', '2024-10-20', 'Healthcare', 'Eye exam', 1),
(140.00, 'Pharmacy', 'purchase', '2024-10-15', 'Healthcare', 'Prescription medication', 1),
(280.00, 'Car Repair Shop', 'purchase', '2024-09-25', 'Transportation', 'Tire replacement', 1),
(95.00, 'Phone Store', 'purchase', '2024-09-20', 'Shopping', 'Phone accessories', 1),
(350.00, 'Clothing Store', 'purchase', '2024-08-30', 'Shopping', 'Seasonal clothes', 1),
(110.00, 'Amazon', 'purchase', '2024-08-25', 'Shopping', 'Electronics', 1),
(85.00, 'Target', 'purchase', '2024-08-20', 'Shopping', 'Home supplies', 1),
(95.00, 'Gas Station', 'purchase', '2024-08-15', 'Transportation', 'Gas for travel', 1),
(70.00, 'Restaurant', 'purchase', '2025-08-10', 'Food & Dining', 'Travel meals', 1),

-- INCOME TRANSACTIONS
(500.00, 'Part-time Job', 'income', '2025-08-14', 'Income', 'Weekly paycheck', 1),
(500.00, 'Part-time Job', 'income', '2025-08-07', 'Income', 'Weekly paycheck', 1),
(500.00, 'Part-time Job', 'income', '2025-07-31', 'Income', 'Weekly paycheck', 1),
(500.00, 'Part-time Job', 'income', '2025-07-24', 'Income', 'Weekly paycheck', 1),
(2000.00, 'Parents', 'income', '2025-07-20', 'Income', 'Monthly allowance', 1),
(2000.00, 'Parents', 'income', '2025-06-20', 'Income', 'Monthly allowance', 1),
(2000.00, 'Parents', 'income', '2025-05-20', 'Income', 'Monthly allowance', 1),
(2000.00, 'Parents', 'income', '2025-04-20', 'Income', 'Monthly allowance', 1),
(2000.00, 'Parents', 'income', '2025-03-20', 'Income', 'Monthly allowance', 1),
(2000.00, 'Parents', 'income', '2025-02-20', 'Income', 'Monthly allowance', 1),
(2000.00, 'Parents', 'income', '2025-01-20', 'Income', 'Monthly allowance', 1),
(2000.00, 'Parents', 'income', '2024-12-20', 'Income', 'Monthly allowance', 1),
(2000.00, 'Parents', 'income', '2024-11-20', 'Income', 'Monthly allowance', 1),
(2000.00, 'Parents', 'income', '2024-10-20', 'Income', 'Monthly allowance', 1),
(2000.00, 'Parents', 'income', '2024-09-20', 'Income', 'Monthly allowance', 1); 