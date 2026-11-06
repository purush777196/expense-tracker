-- ==============================================================================
-- Expense Tracker Database Migration: V6__create_recurring_transactions_table.sql
-- Module: Recurring Transactions & Idempotent Scheduler
-- Target: PostgreSQL 16 (Compatible with ANSI SQL & H2 PostgreSQL mode)
-- ==============================================================================

CREATE TABLE recurring_transactions (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL,
    category_id UUID NOT NULL,
    type VARCHAR(20) NOT NULL,
    amount NUMERIC(15, 2) NOT NULL,
    description VARCHAR(255) NOT NULL,
    merchant VARCHAR(150),
    payment_method VARCHAR(50) NOT NULL DEFAULT 'CARD',
    notes TEXT,
    frequency VARCHAR(20) NOT NULL,
    interval_value INT NOT NULL DEFAULT 1,
    start_date DATE NOT NULL,
    end_date DATE,
    next_occurrence_date DATE NOT NULL,
    last_generated_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_recurring_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_recurring_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    CONSTRAINT chk_recurring_amount CHECK (amount > 0),
    CONSTRAINT chk_recurring_type CHECK (type IN ('INCOME', 'EXPENSE')),
    CONSTRAINT chk_recurring_payment_method CHECK (payment_method IN ('CASH', 'CARD', 'BANK_TRANSFER', 'UPI', 'OTHER')),
    CONSTRAINT chk_recurring_frequency CHECK (frequency IN ('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY')),
    CONSTRAINT chk_recurring_interval CHECK (interval_value > 0),
    CONSTRAINT chk_recurring_dates CHECK (end_date IS NULL OR start_date <= end_date),
    CONSTRAINT chk_recurring_status CHECK (status IN ('ACTIVE', 'PAUSED', 'COMPLETED', 'CANCELLED'))
);

CREATE INDEX idx_recurring_user_status ON recurring_transactions(user_id, status);
CREATE INDEX idx_recurring_next_occurrence ON recurring_transactions(status, next_occurrence_date);
CREATE INDEX idx_recurring_category ON recurring_transactions(category_id);

CREATE TABLE recurring_executions (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    recurring_transaction_id UUID NOT NULL,
    transaction_id UUID NOT NULL,
    scheduled_date DATE NOT NULL,
    executed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_executions_recurring FOREIGN KEY (recurring_transaction_id) REFERENCES recurring_transactions(id) ON DELETE CASCADE,
    CONSTRAINT fk_executions_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE,
    CONSTRAINT uq_recurring_execution UNIQUE (recurring_transaction_id, scheduled_date)
);

CREATE INDEX idx_executions_recurring ON recurring_executions(recurring_transaction_id, scheduled_date);

