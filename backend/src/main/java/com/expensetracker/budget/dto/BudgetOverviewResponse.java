package com.expensetracker.budget.dto;

import java.math.BigDecimal;
import java.util.List;

public record BudgetOverviewResponse(
        BigDecimal totalBudgeted,
        BigDecimal totalSpent,
        BigDecimal totalRemaining,
        BigDecimal overallUtilizationPercentage,
        int activeBudgetsCount,
        int budgetsAtRiskCount,
        int budgetsExceededCount,
        List<BudgetProgressResponse> budgets
) {
}
