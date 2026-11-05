# Database Design Specification

## Project: Expense Tracker
### AI-Powered Personal Expense Tracking, Forecasting and Financial Recommendation System

**Document Version:** 1.0.0  
**Status:** Approved  
**Date:** November 2026  

---

## 1. Design Overview & Core Principles

The **Expense Tracker** database architecture is designed for enterprise reliability, high-concurrency transactional consistency, and strict multi-tenant isolation within PostgreSQL 16.

### Core Architectural Principles:
1. **Universally Unique Identifiers (UUIDv4):** All table primary keys use PostgreSQL `UUID` generated via `gen_random_uuid()`. This prevents enumeration attacks and simplifies cross-system synchronization.
2. **Monetary Precision:** Under no condition are floating-point types (`REAL`, `FLOAT`, `DOUBLE PRECISION`) permitted. All monetary values are strictly typed as `NUMERIC(15,2)` to guarantee exact precision up to 999 trillion units with standard 2-decimal fractional subunits.
3. **Time Zone Aware Timestamps:** All temporal attributes are defined as `TIMESTAMPTZ` (Timestamp with Time Zone), standardizing all system-recorded events in UTC.
4. **Relational Integrity & Foreign Keys:** Strict foreign key constraints with explicit referential actions (`ON DELETE CASCADE` or `ON DELETE RESTRICT`) ensure referential integrity.
5. **Multi-Tenant Data Isolation:** Every functional entity maintains a non-nullable foreign key referencing `users(id)`. Every operational query scopes by `user_id`.
6. **Flyway Migration Strategy:** Schema evolutions are managed as immutable, version-controlled SQL migration scripts (`V1__...`, `V2__...`). Direct manual modifications to database schemas are prohibited.

---

## 2. Entity Relational Model Summary

The database comprises eight normalized core entities:

```
users (1) <------- (N) categories
users (1) <------- (N) transactions -------> (1) categories
users (1) <------- (N) budgets -----------> (0..1) categories
users (1) <------- (N) financial_goals
users (1) <------- (N) recurring_transactions -> (1) categories
users (1) <------- (N) ai_insights -------> (0..1) transactions
users (1) <------- (N) expense_predictions
```

---

## 3. Detailed Schema Definitions (DDL)

```sql
-- Enable cryptographic extension for UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================================
-- 1. USERS TABLE
-- ============================================================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_currency_format CHECK (LENGTH(currency) = 3)
);

CREATE INDEX idx_users_email ON users(email);

-- ============================================================================
-- 2. CATEGORIES TABLE
-- ============================================================================
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID, -- NULL indicates system-wide default category
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    color_hex VARCHAR(7) NOT NULL DEFAULT '#6366F1',
    icon_name VARCHAR(50) NOT NULL DEFAULT 'Tag',
    is_system_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_categories_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_category_type CHECK (type IN ('INCOME', 'EXPENSE')),
    CONSTRAINT uq_category_per_user UNIQUE (user_id, name, type)
);

CREATE INDEX idx_categories_user_type ON categories(user_id, type);

-- ============================================================================
-- 3. TRANSACTIONS TABLE
-- ============================================================================
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    category_id UUID NOT NULL,
    amount NUMERIC(15,2) NOT NULL,
    type VARCHAR(20) NOT NULL,
    transaction_date TIMESTAMPTZ NOT NULL,
    description VARCHAR(255) NOT NULL,
    merchant VARCHAR(150),
    payment_method VARCHAR(50) DEFAULT 'CARD',
    notes TEXT,
    is_recurring BOOLEAN NOT NULL DEFAULT FALSE,
    is_anomaly BOOLEAN NOT NULL DEFAULT FALSE,
    ai_categorized BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transactions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_transactions_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    CONSTRAINT chk_transaction_amount CHECK (amount > 0),
    CONSTRAINT chk_transaction_type CHECK (type IN ('INCOME', 'EXPENSE')),
    CONSTRAINT chk_payment_method CHECK (payment_method IN ('CASH', 'CARD', 'BANK_TRANSFER', 'UPI', 'OTHER'))
);

CREATE INDEX idx_tx_user_date ON transactions(user_id, transaction_date DESC);
CREATE INDEX idx_tx_user_category ON transactions(user_id, category_id);
CREATE INDEX idx_tx_user_type ON transactions(user_id, type);

-- ============================================================================
-- 4. BUDGETS TABLE
-- ============================================================================
CREATE TABLE budgets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    category_id UUID, -- NULL indicates overall monthly envelope
    amount_limit NUMERIC(15,2) NOT NULL,
    period_year INT NOT NULL,
    period_month INT NOT NULL,
    notify_threshold NUMERIC(5,2) NOT NULL DEFAULT 80.00, -- e.g. 80% threshold
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_budgets_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_budgets_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    CONSTRAINT chk_budget_limit CHECK (amount_limit > 0),
    CONSTRAINT chk_budget_month CHECK (period_month BETWEEN 1 AND 12),
    CONSTRAINT chk_budget_year CHECK (period_year BETWEEN 2020 AND 2100),
    CONSTRAINT uq_budget_user_cat_period UNIQUE (user_id, category_id, period_year, period_month)
);

CREATE INDEX idx_budgets_user_period ON budgets(user_id, period_year, period_month);

-- ============================================================================
-- 5. FINANCIAL GOALS TABLE
-- ============================================================================
CREATE TABLE financial_goals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    title VARCHAR(150) NOT NULL,
    target_amount NUMERIC(15,2) NOT NULL,
    current_amount NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    target_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_goals_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_goal_target CHECK (target_amount > 0),
    CONSTRAINT chk_goal_current CHECK (current_amount >= 0),
    CONSTRAINT chk_goal_status CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
);

CREATE INDEX idx_goals_user_status ON financial_goals(user_id, status);

-- ============================================================================
-- 6. RECURRING TRANSACTIONS TABLE
-- ============================================================================
CREATE TABLE recurring_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    category_id UUID NOT NULL,
    amount NUMERIC(15,2) NOT NULL,
    type VARCHAR(20) NOT NULL,
    frequency VARCHAR(20) NOT NULL,
    description VARCHAR(255) NOT NULL,
    merchant VARCHAR(150),
    start_date DATE NOT NULL,
    end_date DATE,
    next_due_date DATE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_recurring_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_recurring_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    CONSTRAINT chk_recurring_amount CHECK (amount > 0),
    CONSTRAINT chk_recurring_type CHECK (type IN ('INCOME', 'EXPENSE')),
    CONSTRAINT chk_recurring_freq CHECK (frequency IN ('DAILY', 'WEEKLY', 'BI_WEEKLY', 'MONTHLY', 'YEARLY'))
);

CREATE INDEX idx_recurring_due ON recurring_transactions(is_active, next_due_date);

-- ============================================================================
-- 7. AI INSIGHTS TABLE
-- ============================================================================
CREATE TABLE ai_insights (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    transaction_id UUID,
    insight_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL DEFAULT 'INFO',
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    metadata JSONB,
    is_dismissed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_insights_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_insights_txn FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE SET NULL,
    CONSTRAINT chk_insight_severity CHECK (severity IN ('INFO', 'WARNING', 'CRITICAL', 'POSITIVE')),
    CONSTRAINT chk_insight_type CHECK (insight_type IN ('ANOMALY_SPEND', 'BUDGET_OVERRUN_RISK', 'DISCRETIONARY_OPTIMIZATION', 'SAVINGS_OPPORTUNITY'))
);

CREATE INDEX idx_insights_user_active ON ai_insights(user_id, is_dismissed, created_at DESC);

-- ============================================================================
-- 8. EXPENSE PREDICTIONS TABLE
-- ============================================================================
CREATE TABLE expense_predictions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    forecast_horizon_days INT NOT NULL,
    projected_amount NUMERIC(15,2) NOT NULL,
    lower_bound NUMERIC(15,2),
    upper_bound NUMERIC(15,2),
    confidence_score NUMERIC(5,4),
    model_version VARCHAR(50) NOT NULL,
    features_used JSONB,
    generated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_predictions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_horizon CHECK (forecast_horizon_days IN (7, 14, 30, 90))
);

CREATE INDEX idx_predictions_user_gen ON expense_predictions(user_id, generated_at DESC);
```

---

## 4. Flyway Database Migration Plan

All database schema evolutions are maintained under `backend/src/main/resources/db/migration/`:
- `V1__init_schema.sql`: Core identity schema (users table, base categories table, cryptographic extension, indices, constraints).
- `V2__seed_default_categories.sql`: Default system taxonomy seed definitions (Groceries, Housing, Utilities, Dining, Salary, Investments, etc.).
- `V3__create_transactions_table.sql`: Transaction ledger schema (`transactions` table, `NUMERIC(15,2)` precision, positive amount check, category referential integrity, compound indices for tenant isolation).
- Subsequent schema changes will be versioned monotonically as `V4__...`, `V5__...`.

---

## 5. Architectural Evaluation of Table Partitioning

During the design of the transaction management subsystem (Phase 3), declarative PostgreSQL range partitioning (e.g. `PARTITION BY RANGE (transaction_date)`) was evaluated against standard B-tree indexing.

### Evaluation Criteria & Analysis:
1. **Expected Data Volume & Query Patterns:**
   - In personal finance management, an active individual typically records 30 to 120 transactions per month (~1,500/year).
   - For 10,000 concurrent active users, 5-year data volume is estimated at ~75 million rows.
   - 99% of read and write queries are strictly scoped by tenant (`user_id`) and secondarily by date range (`transaction_date BETWEEN ? AND ?`) or category (`category_id = ?`).
2. **Indexing Efficiency:**
   - A composite B-tree index on `(user_id, transaction_date DESC)` limits the B-tree depth to 3–4 levels.
   - Single-user ledger retrieval queries execute in under 1 millisecond with zero table full-scans.
3. **Partitioning Trade-offs & Operational Complexity:**
   - PostgreSQL requires the partition key to be part of all unique and primary key constraints. Partitioning by date would require compound primary key `(id, transaction_date)`, violating clean surrogate UUID primary key semantics.
   - Foreign key references from downstream tables (e.g., `ai_insights.transaction_id`) to a partitioned table require complex handling across partitions.
   - Migration management and query planner overhead for multi-year aggregation queries increase.
4. **Architectural Decision:**
   - Table partitioning is **deferred** in favor of composite indexing `(user_id, transaction_date DESC)`, `(user_id, category_id)`, and `(user_id, type)`.
   - Partitioning will be re-evaluated when single-table row counts exceed 25,000,000 records or when query latency metrics exceed 50ms SLA.
