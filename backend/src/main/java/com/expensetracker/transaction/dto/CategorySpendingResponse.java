package com.expensetracker.transaction.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CategorySpendingResponse(
        UUID categoryId,
        String categoryName,
        String colorHex,
        String iconName,
        BigDecimal amount,
        Double percentage,
        Long transactionCount
) {
}
