package com.expensetracker.goal.dto;

import com.expensetracker.goal.entity.GoalStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateGoalRequest(
        @Size(max = 150, message = "Goal name must not exceed 150 characters")
        String name,

        String description,

        @DecimalMin(value = "0.01", message = "Target amount must be greater than zero")
        BigDecimal targetAmount,

        LocalDate targetDate,

        GoalStatus status
) {
}
