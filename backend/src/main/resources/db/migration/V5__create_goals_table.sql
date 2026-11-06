-- ==============================================================================
-- Expense Tracker Database Migration: V5__create_goals_table.sql
-- Module: Financial Goals & Auditable Contribution History
-- Target: PostgreSQL 16 (Compatible with ANSI SQL & H2 PostgreSQL mode)
-- ==============================================================================

CREATE TABLE financial_goals (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    target_amount NUMERIC(15, 2) NOT NULL,
    current_amount NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    target_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_goals_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_goal_target_amount CHECK (target_amount > 0),
    CONSTRAINT chk_goal_current_amount CHECK (current_amount >= 0),
    CONSTRAINT chk_goal_status CHECK (status IN ('ACTIVE', 'COMPLETED', 'CANCELLED'))
);

CREATE INDEX idx_goals_user_status ON financial_goals(user_id, status);
CREATE INDEX idx_goals_user_target_date ON financial_goals(user_id, target_date);

CREATE TABLE goal_contributions (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    goal_id UUID NOT NULL,
    user_id UUID NOT NULL,
    amount NUMERIC(15, 2) NOT NULL,
    type VARCHAR(20) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_contributions_goal FOREIGN KEY (goal_id) REFERENCES financial_goals(id) ON DELETE CASCADE,
    CONSTRAINT fk_contributions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_contribution_amount CHECK (amount > 0),
    CONSTRAINT chk_contribution_type CHECK (type IN ('CONTRIBUTION', 'WITHDRAWAL'))
);

CREATE INDEX idx_contributions_goal_created ON goal_contributions(goal_id, created_at DESC);
CREATE INDEX idx_contributions_user ON goal_contributions(user_id, created_at DESC);

