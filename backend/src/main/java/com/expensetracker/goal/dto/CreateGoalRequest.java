package com.expensetracker.goal.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateGoalRequest(
        @NotBlank(message = "Goal name is required")
        @Size(max = 150, message = "Goal name must not exceed 150 characters")
        String name,

        String description,

        @NotNull(message = "Target amount is required")
        @DecimalMin(value = "0.01", message = "Target amount must be greater than zero")
        BigDecimal targetAmount,

        @DecimalMin(value = "0.00", message = "Initial amount cannot be negative")
        BigDecimal initialAmount,

        @NotNull(message = "Target date is required")
        LocalDate targetDate
) {
}
