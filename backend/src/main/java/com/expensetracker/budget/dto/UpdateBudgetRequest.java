package com.expensetracker.budget.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateBudgetRequest(
        @Size(max = 100, message = "Budget name must not exceed 100 characters")
        String name,

        @DecimalMin(value = "0.01", message = "Budget amount must be greater than zero")
        BigDecimal amount,

        @DecimalMin(value = "1.00", message = "Alert threshold must be at least 1%")
        @DecimalMax(value = "100.00", message = "Alert threshold cannot exceed 100%")
        BigDecimal alertThresholdPercentage,

        Boolean isActive
) {
}
