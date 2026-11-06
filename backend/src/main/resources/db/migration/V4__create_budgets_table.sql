-- ==============================================================================
-- Expense Tracker Database Migration: V4__create_budgets_table.sql
-- Module: Budget Management & Threshold Alerts
-- Target: PostgreSQL 16 (Compatible with ANSI SQL & H2 PostgreSQL mode)
-- ==============================================================================

CREATE TABLE budgets (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL,
    category_id UUID,
    name VARCHAR(100) NOT NULL,
    amount NUMERIC(15, 2) NOT NULL,
    period VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    alert_threshold_percentage NUMERIC(5, 2) NOT NULL DEFAULT 80.00,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_budgets_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_budgets_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    CONSTRAINT chk_budget_amount CHECK (amount > 0),
    CONSTRAINT chk_budget_period CHECK (period IN ('WEEKLY', 'MONTHLY', 'YEARLY')),
    CONSTRAINT chk_budget_dates CHECK (start_date <= end_date),
    CONSTRAINT chk_budget_threshold CHECK (alert_threshold_percentage > 0 AND alert_threshold_percentage <= 100)
);

CREATE INDEX idx_budgets_user_active ON budgets(user_id, is_active);
CREATE INDEX idx_budgets_user_dates ON budgets(user_id, start_date, end_date);
CREATE INDEX idx_budgets_category ON budgets(category_id);

CREATE TABLE budget_alerts (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    budget_id UUID NOT NULL,
    user_id UUID NOT NULL,
    threshold_percentage NUMERIC(5, 2) NOT NULL,
    current_spending NUMERIC(15, 2) NOT NULL,
    budget_amount NUMERIC(15, 2) NOT NULL,
    alert_message VARCHAR(255) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_budget_alerts_budget FOREIGN KEY (budget_id) REFERENCES budgets(id) ON DELETE CASCADE,
    CONSTRAINT fk_budget_alerts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_budget_alert_threshold UNIQUE (budget_id, threshold_percentage)
);

CREATE INDEX idx_budget_alerts_user ON budget_alerts(user_id, is_read, created_at DESC);

