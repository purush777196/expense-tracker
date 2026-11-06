package com.expensetracker.recurring.dto;

import com.expensetracker.category.entity.CategoryType;
import com.expensetracker.recurring.entity.RecurringFrequency;
import com.expensetracker.transaction.entity.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateRecurringTransactionRequest(
        @NotNull(message = "Category ID is required")
        UUID categoryId,

        @NotNull(message = "Transaction type is required")
        CategoryType type,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotBlank(message = "Description is required")
        @Size(max = 255, message = "Description must not exceed 255 characters")
        String description,

        @Size(max = 150, message = "Merchant must not exceed 150 characters")
        String merchant,

        PaymentMethod paymentMethod,

        String notes,

        @NotNull(message = "Frequency is required")
        RecurringFrequency frequency,

        @Min(value = 1, message = "Interval must be at least 1")
        Integer intervalValue,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        LocalDate endDate
) {
}
