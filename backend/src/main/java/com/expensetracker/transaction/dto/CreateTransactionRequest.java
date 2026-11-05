package com.expensetracker.transaction.dto;

import com.expensetracker.category.entity.CategoryType;
import com.expensetracker.transaction.entity.PaymentMethod;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateTransactionRequest(
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        @Digits(integer = 13, fraction = 2, message = "Amount can have at most 2 decimal places")
        BigDecimal amount,

        @NotNull(message = "Transaction type (INCOME or EXPENSE) is required")
        CategoryType type,

        @NotNull(message = "Category ID is required")
        UUID categoryId,

        @NotNull(message = "Transaction date is required")
        Instant transactionDate,

        @NotBlank(message = "Description is required")
        @Size(min = 2, max = 255, message = "Description must be between 2 and 255 characters")
        String description,

        @Size(max = 150, message = "Merchant name cannot exceed 150 characters")
        String merchant,

        PaymentMethod paymentMethod,

        String notes,

        Boolean isRecurring,

        Boolean aiCategorized
) {
}
