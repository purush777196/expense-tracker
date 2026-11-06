package com.expensetracker.goal.dto;

import com.expensetracker.goal.entity.FinancialGoal;
import com.expensetracker.goal.entity.GoalStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public record FinancialGoalResponse(
        UUID id,
        String name,
        String description,
        BigDecimal targetAmount,
        BigDecimal currentAmount,
        BigDecimal remainingAmount,
        BigDecimal progressPercentage,
        LocalDate targetDate,
        long daysRemaining,
        boolean isCompleted,
        GoalStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static FinancialGoalResponse fromEntity(FinancialGoal goal) {
        if (goal == null) return null;

        BigDecimal target = goal.getTargetAmount();
        BigDecimal current = goal.getCurrentAmount();
        BigDecimal remaining = target.subtract(current).max(BigDecimal.ZERO);

        BigDecimal progress = target.compareTo(BigDecimal.ZERO) > 0
                ? current.multiply(new BigDecimal("100")).divide(target, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        LocalDate now = LocalDate.now();
        long daysRemaining = now.isAfter(goal.getTargetDate()) ? 0 : ChronoUnit.DAYS.between(now, goal.getTargetDate());
        boolean completed = goal.getStatus() == GoalStatus.COMPLETED || current.compareTo(target) >= 0;

        return new FinancialGoalResponse(
                goal.getId(),
                goal.getName(),
                goal.getDescription(),
                target,
                current,
                remaining,
                progress,
                goal.getTargetDate(),
                daysRemaining,
                completed,
                goal.getStatus(),
                goal.getCreatedAt(),
                goal.getUpdatedAt()
        );
    }
}
