package com.expensetracker.recurring.dto;

import com.expensetracker.recurring.entity.RecurringFrequency;
import com.expensetracker.recurring.entity.RecurringStatus;
import com.expensetracker.transaction.entity.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateRecurringTransactionRequest(
        UUID categoryId,

        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount,

        @Size(max = 255, message = "Description must not exceed 255 characters")
        String description,

        @Size(max = 150, message = "Merchant must not exceed 150 characters")
        String merchant,

        PaymentMethod paymentMethod,

        String notes,

        RecurringFrequency frequency,

        @Min(value = 1, message = "Interval must be at least 1")
        Integer intervalValue,

        LocalDate endDate,

        RecurringStatus status
) {
}
