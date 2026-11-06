package com.expensetracker.budget.dto;

import java.math.BigDecimal;

public record BudgetProgressResponse(
        BudgetResponse budget,
        BigDecimal amountSpent,
        BigDecimal remainingAmount,
        BigDecimal utilizationPercentage,
        BigDecimal overBudgetAmount,
        boolean isOverBudget,
        String alertStatus,
        long daysRemaining
) {
}
