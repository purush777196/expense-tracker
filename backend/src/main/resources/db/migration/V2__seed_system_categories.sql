-- ==============================================================================
-- Expense Tracker Database Migration: V2__seed_system_categories.sql
-- Description: Seed predefined default categories for Income and Expense
-- ==============================================================================

-- Predefined Expense Categories
INSERT INTO categories (id, user_id, name, type, color_hex, icon_name, is_system_default)
VALUES
    (gen_random_uuid(), NULL, 'Housing & Rent', 'EXPENSE', '#EF4444', 'Home', TRUE),
    (gen_random_uuid(), NULL, 'Groceries', 'EXPENSE', '#10B981', 'ShoppingCart', TRUE),
    (gen_random_uuid(), NULL, 'Utilities', 'EXPENSE', '#F59E0B', 'Zap', TRUE),
    (gen_random_uuid(), NULL, 'Transportation', 'EXPENSE', '#3B82F6', 'Car', TRUE),
    (gen_random_uuid(), NULL, 'Dining & Food', 'EXPENSE', '#8B5CF6', 'Utensils', TRUE),
    (gen_random_uuid(), NULL, 'Healthcare', 'EXPENSE', '#EC4899', 'Activity', TRUE),
    (gen_random_uuid(), NULL, 'Entertainment', 'EXPENSE', '#6366F1', 'Film', TRUE),
    (gen_random_uuid(), NULL, 'Shopping & Retail', 'EXPENSE', '#14B8A6', 'ShoppingBag', TRUE),
    (gen_random_uuid(), NULL, 'Other Expense', 'EXPENSE', '#64748B', 'MoreHorizontal', TRUE);

-- Predefined Income Categories
INSERT INTO categories (id, user_id, name, type, color_hex, icon_name, is_system_default)
VALUES
    (gen_random_uuid(), NULL, 'Salary', 'INCOME', '#10B981', 'Briefcase', TRUE),
    (gen_random_uuid(), NULL, 'Freelance & Contracts', 'INCOME', '#06B6D4', 'Laptop', TRUE),
    (gen_random_uuid(), NULL, 'Investments & Dividends', 'INCOME', '#8B5CF6', 'TrendingUp', TRUE),
    (gen_random_uuid(), NULL, 'Other Income', 'INCOME', '#64748B', 'PlusCircle', TRUE);
