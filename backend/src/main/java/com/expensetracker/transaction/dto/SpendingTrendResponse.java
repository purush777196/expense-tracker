package com.expensetracker.transaction.dto;

import java.math.BigDecimal;

public record SpendingTrendResponse(
        String period,
        BigDecimal income,
        BigDecimal expenses
) {
}
