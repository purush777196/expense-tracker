package com.expensetracker.transaction.repository;

import java.math.BigDecimal;
import java.util.UUID;

public interface CategorySpendingProjection {
    UUID getCategoryId();
    String getCategoryName();
    String getColorHex();
    String getIconName();
    BigDecimal getTotalAmount();
    Long getTransactionCount();
}
