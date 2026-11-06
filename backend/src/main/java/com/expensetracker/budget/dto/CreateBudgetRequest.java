package com.expensetracker.budget.dto;

import com.expensetracker.budget.entity.BudgetPeriod;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateBudgetRequest(
        @NotBlank(message = "Budget name is required")
        @Size(max = 100, message = "Budget name must not exceed 100 characters")
        String name,

        @NotNull(message = "Budget amount is required")
        @DecimalMin(value = "0.01", message = "Budget amount must be greater than zero")
        BigDecimal amount,

        @NotNull(message = "Budget period is required")
        BudgetPeriod period,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        LocalDate endDate,

        UUID categoryId,

        @DecimalMin(value = "1.00", message = "Alert threshold must be at least 1%")
        @DecimalMax(value = "100.00", message = "Alert threshold cannot exceed 100%")
        BigDecimal alertThresholdPercentage
) {
}
