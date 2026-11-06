package com.expensetracker.recurring.dto;

import com.expensetracker.category.dto.CategoryResponse;
import com.expensetracker.category.entity.CategoryType;
import com.expensetracker.recurring.entity.RecurringFrequency;
import com.expensetracker.transaction.entity.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpcomingRecurringResponse(
        UUID recurringId,
        String description,
        String merchant,
        BigDecimal amount,
        CategoryType type,
        CategoryResponse category,
        PaymentMethod paymentMethod,
        RecurringFrequency frequency,
        LocalDate dueDate,
        long daysUntilDue
) {
}
