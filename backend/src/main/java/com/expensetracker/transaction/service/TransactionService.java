package com.expensetracker.transaction.service;

import com.expensetracker.category.entity.CategoryType;
import com.expensetracker.common.response.PagedResponse;
import com.expensetracker.transaction.dto.*;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TransactionService {

    TransactionResponse createTransaction(UUID userId, CreateTransactionRequest request);

    TransactionResponse getTransactionById(UUID userId, UUID transactionId);

    PagedResponse<TransactionResponse> getTransactions(
            UUID userId,
            Instant startDate,
            Instant endDate,
            CategoryType type,
            UUID categoryId,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String query,
            Pageable pageable);

    TransactionResponse updateTransaction(UUID userId, UUID transactionId, UpdateTransactionRequest request);

    void deleteTransaction(UUID userId, UUID transactionId);

    TransactionSummaryResponse getTransactionSummary(UUID userId, Instant startDate, Instant endDate);

    List<CategorySpendingResponse> getCategorySpendingBreakdown(UUID userId, Instant startDate, Instant endDate);

    List<SpendingTrendResponse> getSpendingTrends(UUID userId, int months);

    List<TransactionResponse> getRecentTransactions(UUID userId, int limit);
}
