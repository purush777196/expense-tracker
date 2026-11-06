package com.expensetracker.recurring.service;

import com.expensetracker.category.entity.Category;
import com.expensetracker.category.repository.CategoryRepository;
import com.expensetracker.common.exception.BadRequestException;
import com.expensetracker.common.exception.ResourceNotFoundException;
import com.expensetracker.recurring.dto.*;
import com.expensetracker.recurring.entity.RecurringExecution;
import com.expensetracker.recurring.entity.RecurringStatus;
import com.expensetracker.recurring.entity.RecurringTransaction;
import com.expensetracker.recurring.repository.RecurringExecutionRepository;
import com.expensetracker.recurring.repository.RecurringTransactionRepository;
import com.expensetracker.transaction.entity.Transaction;
import com.expensetracker.transaction.repository.TransactionRepository;
import com.expensetracker.user.entity.User;
import com.expensetracker.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RecurringTransactionServiceImpl implements RecurringTransactionService {

    private static final Logger log = LoggerFactory.getLogger(RecurringTransactionServiceImpl.class);

    private final RecurringTransactionRepository recurringRepository;
    private final RecurringExecutionRepository executionRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public RecurringTransactionServiceImpl(RecurringTransactionRepository recurringRepository,
                                          RecurringExecutionRepository executionRepository,
                                          TransactionRepository transactionRepository,
                                          CategoryRepository categoryRepository,
                                          UserRepository userRepository) {
        this.recurringRepository = recurringRepository;
        this.executionRepository = executionRepository;
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Override
    public RecurringTransactionResponse createRecurringTransaction(UUID userId, CreateRecurringTransactionRequest request) {
        User user = getUserOrThrow(userId);

        Category category = categoryRepository.findAvailableById(request.categoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found or not accessible"));

        if (category.getType() != request.type()) {
            throw new BadRequestException("Transaction type (" + request.type() +
                    ") must match category type (" + category.getType() + ")");
        }

        if (request.endDate() != null && request.startDate().isAfter(request.endDate())) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        int interval = (request.intervalValue() != null && request.intervalValue() > 0)
                ? request.intervalValue()
                : 1;

        LocalDate nextOccurrence = request.startDate();

        RecurringTransaction recurring = new RecurringTransaction(
                user,
                category,
                request.type(),
                request.amount(),
                request.description().trim(),
                request.merchant() != null ? request.merchant().trim() : null,
                request.paymentMethod(),
                request.notes() != null ? request.notes().trim() : null,
                request.frequency(),
                interval,
                request.startDate(),
                request.endDate(),
                nextOccurrence
        );

        RecurringTransaction saved = recurringRepository.save(recurring);
        return RecurringTransactionResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecurringTransactionResponse> getRecurringTransactions(UUID userId, RecurringStatus status) {
        List<RecurringTransaction> list;
        if (status != null) {
            list = recurringRepository.findByUserIdAndStatusOrderByNextOccurrenceDateAsc(userId, status);
        } else {
            list = recurringRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }
        return list.stream().map(RecurringTransactionResponse::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RecurringTransactionResponse getRecurringTransactionById(UUID userId, UUID id) {
        RecurringTransaction rt = getRecurringOrThrow(id, userId);
        return RecurringTransactionResponse.fromEntity(rt);
    }

    @Override
    public RecurringTransactionResponse updateRecurringTransaction(UUID userId, UUID id, UpdateRecurringTransactionRequest request) {
        RecurringTransaction rt = getRecurringOrThrow(id, userId);

        if (request.categoryId() != null) {
            Category category = categoryRepository.findAvailableById(request.categoryId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found or not accessible"));
            if (category.getType() != rt.getType()) {
                throw new BadRequestException("New category type must match recurring transaction type");
            }
            rt.setCategory(category);
        }

        if (request.amount() != null) {
            rt.setAmount(request.amount());
        }
        if (request.description() != null && !request.description().trim().isEmpty()) {
            rt.setDescription(request.description().trim());
        }
        if (request.merchant() != null) {
            rt.setMerchant(request.merchant().trim());
        }
        if (request.paymentMethod() != null) {
            rt.setPaymentMethod(request.paymentMethod());
        }
        if (request.notes() != null) {
            rt.setNotes(request.notes().trim());
        }
        if (request.frequency() != null) {
            rt.setFrequency(request.frequency());
        }
        if (request.intervalValue() != null && request.intervalValue() > 0) {
            rt.setIntervalValue(request.intervalValue());
        }
        if (request.endDate() != null) {
            if (rt.getStartDate().isAfter(request.endDate())) {
                throw new BadRequestException("End date cannot be before start date");
            }
            rt.setEndDate(request.endDate());
        }
        if (request.status() != null) {
            rt.setStatus(request.status());
        }

        RecurringTransaction updated = recurringRepository.save(rt);
        return RecurringTransactionResponse.fromEntity(updated);
    }

    @Override
    public void deleteRecurringTransaction(UUID userId, UUID id) {
        RecurringTransaction rt = getRecurringOrThrow(id, userId);
        recurringRepository.delete(rt);
    }

    @Override
    public RecurringTransactionResponse pauseRecurringTransaction(UUID userId, UUID id) {
        RecurringTransaction rt = getRecurringOrThrow(id, userId);
        if (rt.getStatus() != RecurringStatus.ACTIVE) {
            throw new BadRequestException("Only ACTIVE recurring transactions can be paused");
        }
        rt.setStatus(RecurringStatus.PAUSED);
        return RecurringTransactionResponse.fromEntity(recurringRepository.save(rt));
    }

    @Override
    public RecurringTransactionResponse resumeRecurringTransaction(UUID userId, UUID id) {
        RecurringTransaction rt = getRecurringOrThrow(id, userId);
        if (rt.getStatus() != RecurringStatus.PAUSED) {
            throw new BadRequestException("Only PAUSED recurring transactions can be resumed");
        }
        rt.setStatus(RecurringStatus.ACTIVE);
        return RecurringTransactionResponse.fromEntity(recurringRepository.save(rt));
    }

    @Override
    public RecurringTransactionResponse cancelRecurringTransaction(UUID userId, UUID id) {
        RecurringTransaction rt = getRecurringOrThrow(id, userId);
        rt.setStatus(RecurringStatus.CANCELLED);
        return RecurringTransactionResponse.fromEntity(recurringRepository.save(rt));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UpcomingRecurringResponse> getUpcomingRecurring(UUID userId, int daysAhead) {
        int safeDays = daysAhead > 0 ? daysAhead : 30;
        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusDays(safeDays);

        List<RecurringTransaction> list = recurringRepository.findUpcomingByUserIdAndDateRange(userId, today, maxDate);
        List<UpcomingRecurringResponse> responses = new ArrayList<>();

        for (RecurringTransaction rt : list) {
            long daysUntil = ChronoUnit.DAYS.between(today, rt.getNextOccurrenceDate());
            responses.add(new UpcomingRecurringResponse(
                    rt.getId(),
                    rt.getDescription(),
                    rt.getMerchant(),
                    rt.getAmount(),
                    rt.getType(),
                    com.expensetracker.category.dto.CategoryResponse.fromEntity(rt.getCategory()),
                    rt.getPaymentMethod(),
                    rt.getFrequency(),
                    rt.getNextOccurrenceDate(),
                    daysUntil
            ));
        }

        return responses;
    }

    @Override
    public int processDueRecurringTransactions(LocalDate asOfDate) {
        LocalDate targetDate = asOfDate != null ? asOfDate : LocalDate.now();
        log.info("Processing due recurring transactions as of {}", targetDate);

        List<RecurringTransaction> dueSchedules = recurringRepository.findDueRecurringTransactions(targetDate);
        int generatedCount = 0;

        for (RecurringTransaction rt : dueSchedules) {
            int originalDay = rt.getStartDate().getDayOfMonth();

            while (rt.getStatus() == RecurringStatus.ACTIVE && !rt.getNextOccurrenceDate().isAfter(targetDate)) {
                LocalDate occurrenceDate = rt.getNextOccurrenceDate();

                if (rt.getEndDate() != null && occurrenceDate.isAfter(rt.getEndDate())) {
                    rt.setStatus(RecurringStatus.COMPLETED);
                    break;
                }

                if (!executionRepository.existsByRecurringTransactionIdAndScheduledDate(rt.getId(), occurrenceDate)) {
                    Instant txInstant = occurrenceDate.atTime(12, 0).atZone(ZoneOffset.UTC).toInstant();

                    Transaction tx = new Transaction(
                            rt.getUser(),
                            rt.getCategory(),
                            rt.getAmount(),
                            rt.getType(),
                            txInstant,
                            rt.getDescription(),
                            rt.getMerchant(),
                            rt.getPaymentMethod(),
                            rt.getNotes() != null ? rt.getNotes() : "Auto-generated from recurring schedule"
                    );
                    tx.setRecurring(true);
                    Transaction savedTx = transactionRepository.save(tx);

                    RecurringExecution execution = new RecurringExecution(rt, savedTx, occurrenceDate);
                    executionRepository.save(execution);

                    generatedCount++;
                    log.info("Generated transaction {} for recurring schedule {} on {}",
                            savedTx.getId(), rt.getId(), occurrenceDate);
                }

                rt.setLastGeneratedDate(occurrenceDate);
                LocalDate nextDate = RecurringDateUtils.calculateNextOccurrence(
                        occurrenceDate, rt.getFrequency(), rt.getIntervalValue(), originalDay);
                rt.setNextOccurrenceDate(nextDate);

                if (rt.getEndDate() != null && nextDate.isAfter(rt.getEndDate())) {
                    rt.setStatus(RecurringStatus.COMPLETED);
                    break;
                }
            }

            recurringRepository.save(rt);
        }

        log.info("Completed recurring transaction processing. Generated {} transactions.", generatedCount);
        return generatedCount;
    }

    private User getUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private RecurringTransaction getRecurringOrThrow(UUID id, UUID userId) {
        return recurringRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found"));
    }
}
