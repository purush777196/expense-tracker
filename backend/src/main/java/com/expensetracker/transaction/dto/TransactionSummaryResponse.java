package com.expensetracker.transaction.dto;

import java.math.BigDecimal;

public record TransactionSummaryResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netBalance,
        BigDecimal savingsRatePercent,
        long transactionCount,
        String currency
) {
}
