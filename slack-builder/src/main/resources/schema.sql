-- Create transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    amount DECIMAL(10,2) NOT NULL,
    vendor VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    date VARCHAR(10) NOT NULL,
    category VARCHAR(100),
    notes VARCHAR(500),
    user_id INTEGER NOT NULL
);

-- Create index on user_id for better query performance
CREATE INDEX IF NOT EXISTS idx_transactions_user_id ON transactions(user_id);

-- Create index on date for date-based queries
CREATE INDEX IF NOT EXISTS idx_transactions_date ON transactions(date);

-- Create budgets table
CREATE TABLE IF NOT EXISTS budgets (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    user_id INTEGER NOT NULL,
    start_date VARCHAR(10) NOT NULL,
    end_date VARCHAR(10) NOT NULL,
    total_planned DECIMAL(10,2) DEFAULT 0,
    total_spent DECIMAL(10,2) DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    is_repeatable BOOLEAN DEFAULT false,
    repeat_interval VARCHAR(20),
    repeat_count INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create budget_categories table
CREATE TABLE IF NOT EXISTS budget_categories (
    id BIGSERIAL PRIMARY KEY,
    budget_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    planned_amount DECIMAL(10,2) NOT NULL,
    spent_amount DECIMAL(10,2) DEFAULT 0,
    color VARCHAR(7),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (budget_id) REFERENCES budgets(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_budgets_user_id ON budgets(user_id);
CREATE INDEX IF NOT EXISTS idx_budgets_active ON budgets(is_active);
CREATE INDEX IF NOT EXISTS idx_budget_categories_budget_id ON budget_categories(budget_id); 