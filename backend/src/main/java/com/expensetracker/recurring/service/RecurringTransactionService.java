package com.expensetracker.recurring.service;

import com.expensetracker.recurring.dto.*;
import com.expensetracker.recurring.entity.RecurringStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface RecurringTransactionService {

    RecurringTransactionResponse createRecurringTransaction(UUID userId, CreateRecurringTransactionRequest request);

    List<RecurringTransactionResponse> getRecurringTransactions(UUID userId, RecurringStatus status);

    RecurringTransactionResponse getRecurringTransactionById(UUID userId, UUID id);

    RecurringTransactionResponse updateRecurringTransaction(UUID userId, UUID id, UpdateRecurringTransactionRequest request);

    void deleteRecurringTransaction(UUID userId, UUID id);

    RecurringTransactionResponse pauseRecurringTransaction(UUID userId, UUID id);

    RecurringTransactionResponse resumeRecurringTransaction(UUID userId, UUID id);

    RecurringTransactionResponse cancelRecurringTransaction(UUID userId, UUID id);

    List<UpcomingRecurringResponse> getUpcomingRecurring(UUID userId, int daysAhead);

    int processDueRecurringTransactions(LocalDate asOfDate);
}
