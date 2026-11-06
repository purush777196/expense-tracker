package com.expensetracker.goal.dto;

import java.math.BigDecimal;
import java.util.List;

public record GoalSummaryResponse(
        BigDecimal totalTargetAmount,
        BigDecimal totalSavedAmount,
        BigDecimal totalRemainingAmount,
        BigDecimal overallProgressPercentage,
        int activeGoalsCount,
        int completedGoalsCount,
        List<FinancialGoalResponse> goals
) {
}
