package com.expensetracker.transaction.dto;

import com.expensetracker.category.dto.CategoryResponse;
import com.expensetracker.category.entity.CategoryType;
import com.expensetracker.transaction.entity.PaymentMethod;
import com.expensetracker.transaction.entity.Transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        BigDecimal amount,
        CategoryType type,
        Instant transactionDate,
        String description,
        String merchant,
        PaymentMethod paymentMethod,
        String notes,
        CategoryResponse category,
        boolean isRecurring,
        boolean isAnomaly,
        boolean aiCategorized,
        Instant createdAt,
        Instant updatedAt
) {
    public static TransactionResponse fromEntity(Transaction tx) {
        if (tx == null) return null;
        return new TransactionResponse(
                tx.getId(),
                tx.getAmount(),
                tx.getType(),
                tx.getTransactionDate(),
                tx.getDescription(),
                tx.getMerchant(),
                tx.getPaymentMethod(),
                tx.getNotes(),
                CategoryResponse.fromEntity(tx.getCategory()),
                tx.isRecurring(),
                tx.isAnomaly(),
                tx.aiCategorized(),
                tx.getCreatedAt(),
                tx.getUpdatedAt()
        );
    }
}
