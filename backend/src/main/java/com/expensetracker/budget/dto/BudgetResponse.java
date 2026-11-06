package com.expensetracker.budget.dto;

import com.expensetracker.budget.entity.Budget;
import com.expensetracker.budget.entity.BudgetPeriod;
import com.expensetracker.category.dto.CategoryResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BudgetResponse(
        UUID id,
        String name,
        BigDecimal amount,
        BudgetPeriod period,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal alertThresholdPercentage,
        boolean isActive,
        CategoryResponse category,
        boolean isOverall,
        Instant createdAt,
        Instant updatedAt
) {
    public static BudgetResponse fromEntity(Budget budget) {
        if (budget == null) return null;
        return new BudgetResponse(
                budget.getId(),
                budget.getName(),
                budget.getAmount(),
                budget.getPeriod(),
                budget.getStartDate(),
                budget.getEndDate(),
                budget.getAlertThresholdPercentage(),
                budget.isActive(),
                budget.getCategory() != null ? CategoryResponse.fromEntity(budget.getCategory()) : null,
                budget.getCategory() == null,
                budget.getCreatedAt(),
                budget.getUpdatedAt()
        );
    }
}
