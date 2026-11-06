package com.expensetracker.recurring.dto;

import com.expensetracker.category.dto.CategoryResponse;
import com.expensetracker.category.entity.CategoryType;
import com.expensetracker.recurring.entity.RecurringFrequency;
import com.expensetracker.recurring.entity.RecurringStatus;
import com.expensetracker.recurring.entity.RecurringTransaction;
import com.expensetracker.transaction.entity.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record RecurringTransactionResponse(
        UUID id,
        CategoryResponse category,
        CategoryType type,
        BigDecimal amount,
        String description,
        String merchant,
        PaymentMethod paymentMethod,
        String notes,
        RecurringFrequency frequency,
        int intervalValue,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate nextOccurrenceDate,
        LocalDate lastGeneratedDate,
        RecurringStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static RecurringTransactionResponse fromEntity(RecurringTransaction rt) {
        if (rt == null) return null;
        return new RecurringTransactionResponse(
                rt.getId(),
                CategoryResponse.fromEntity(rt.getCategory()),
                rt.getType(),
                rt.getAmount(),
                rt.getDescription(),
                rt.getMerchant(),
                rt.getPaymentMethod(),
                rt.getNotes(),
                rt.getFrequency(),
                rt.getIntervalValue(),
                rt.getStartDate(),
                rt.getEndDate(),
                rt.getNextOccurrenceDate(),
                rt.getLastGeneratedDate(),
                rt.getStatus(),
                rt.getCreatedAt(),
                rt.getUpdatedAt()
        );
    }
}
