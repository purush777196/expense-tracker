package com.expensetracker.budget.dto;

import com.expensetracker.budget.entity.BudgetAlert;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BudgetAlertResponse(
        UUID id,
        UUID budgetId,
        String budgetName,
        BigDecimal thresholdPercentage,
        BigDecimal currentSpending,
        BigDecimal budgetAmount,
        String alertMessage,
        boolean isRead,
        Instant createdAt
) {
    public static BudgetAlertResponse fromEntity(BudgetAlert alert) {
        if (alert == null) return null;
        return new BudgetAlertResponse(
                alert.getId(),
                alert.getBudget().getId(),
                alert.getBudget().getName(),
                alert.getThresholdPercentage(),
                alert.getCurrentSpending(),
                alert.getBudgetAmount(),
                alert.getAlertMessage(),
                alert.isRead(),
                alert.getCreatedAt()
        );
    }
}
