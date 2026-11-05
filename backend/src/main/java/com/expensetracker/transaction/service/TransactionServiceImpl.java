package com.expensetracker.transaction.service;

import com.expensetracker.category.entity.Category;
import com.expensetracker.category.entity.CategoryType;
import com.expensetracker.category.repository.CategoryRepository;
import com.expensetracker.common.exception.BadRequestException;
import com.expensetracker.common.exception.ResourceNotFoundException;
import com.expensetracker.common.response.PagedResponse;
import com.expensetracker.transaction.dto.*;
import com.expensetracker.transaction.entity.PaymentMethod;
import com.expensetracker.transaction.entity.Transaction;
import com.expensetracker.transaction.repository.CategorySpendingProjection;
import com.expensetracker.transaction.repository.TransactionRepository;
import com.expensetracker.transaction.repository.TransactionSpecification;
import com.expensetracker.user.entity.User;
import com.expensetracker.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  CategoryRepository categoryRepository,
                                  UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Override
    public TransactionResponse createTransaction(UUID userId, CreateTransactionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Category category = validateAndGetCategory(request.categoryId(), userId, request.type());

        Transaction transaction = new Transaction(
                user,
                category,
                request.amount().setScale(2, RoundingMode.HALF_UP),
                request.type(),
                request.transactionDate(),
                request.description().trim(),
                request.merchant() != null ? request.merchant().trim() : null,
                request.paymentMethod() != null ? request.paymentMethod() : PaymentMethod.CARD,
                request.notes()
        );

        if (Boolean.TRUE.equals(request.isRecurring())) {
            transaction.setRecurring(true);
        }
        if (Boolean.TRUE.equals(request.aiCategorized())) {
            transaction.setAiCategorized(true);
        }

        Transaction saved = transactionRepository.save(transaction);
        return TransactionResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(UUID userId, UUID transactionId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transactionId));
        return TransactionResponse.fromEntity(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TransactionResponse> getTransactions(
            UUID userId,
            Instant startDate,
            Instant endDate,
            CategoryType type,
            UUID categoryId,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String query,
            Pageable pageable) {

        Specification<Transaction> spec = TransactionSpecification.withFilters(
                userId, startDate, endDate, type, categoryId, minAmount, maxAmount, query
        );

        Page<Transaction> page = transactionRepository.findAll(spec, pageable);
        return PagedResponse.fromPage(page.map(TransactionResponse::fromEntity));
    }

    @Override
    public TransactionResponse updateTransaction(UUID userId, UUID transactionId, UpdateTransactionRequest request) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transactionId));

        Category category = validateAndGetCategory(request.categoryId(), userId, request.type());

        transaction.setCategory(category);
        transaction.setAmount(request.amount().setScale(2, RoundingMode.HALF_UP));
        transaction.setType(request.type());
        transaction.setTransactionDate(request.transactionDate());
        transaction.setDescription(request.description().trim());
        transaction.setMerchant(request.merchant() != null ? request.merchant().trim() : null);
        transaction.setPaymentMethod(request.paymentMethod() != null ? request.paymentMethod() : PaymentMethod.CARD);
        transaction.setNotes(request.notes());

        if (request.isRecurring() != null) {
            transaction.setRecurring(request.isRecurring());
        }
        if (request.aiCategorized() != null) {
            transaction.setAiCategorized(request.aiCategorized());
        }

        Transaction updated = transactionRepository.save(transaction);
        return TransactionResponse.fromEntity(updated);
    }

    @Override
    public void deleteTransaction(UUID userId, UUID transactionId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transactionId));
        transactionRepository.delete(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionSummaryResponse getTransactionSummary(UUID userId, Instant startDate, Instant endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        BigDecimal totalIncome;
        BigDecimal totalExpense;
        long transactionCount;

        if (startDate != null && endDate != null) {
            totalIncome = transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                    userId, CategoryType.INCOME, startDate, endDate);
            totalExpense = transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                    userId, CategoryType.EXPENSE, startDate, endDate);
            transactionCount = transactionRepository.countByUserIdAndDateRange(userId, startDate, endDate);
        } else {
            totalIncome = transactionRepository.sumTotalAmountByUserIdAndType(userId, CategoryType.INCOME);
            totalExpense = transactionRepository.sumTotalAmountByUserIdAndType(userId, CategoryType.EXPENSE);
            transactionCount = transactionRepository.count(
                    TransactionSpecification.withFilters(userId, null, null, null, null, null, null, null));
        }

        BigDecimal netBalance = totalIncome.subtract(totalExpense);
        BigDecimal savingsRatePercent = BigDecimal.ZERO;
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            savingsRatePercent = netBalance
                    .divide(totalIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return new TransactionSummaryResponse(
                totalIncome.setScale(2, RoundingMode.HALF_UP),
                totalExpense.setScale(2, RoundingMode.HALF_UP),
                netBalance.setScale(2, RoundingMode.HALF_UP),
                savingsRatePercent,
                transactionCount,
                user.getCurrency()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategorySpendingResponse> getCategorySpendingBreakdown(UUID userId, Instant startDate, Instant endDate) {
        List<CategorySpendingProjection> projections;

        if (startDate != null && endDate != null) {
            projections = transactionRepository.findCategorySpendingByUserIdAndDateRange(userId, startDate, endDate);
        } else {
            projections = transactionRepository.findAllTimeCategorySpendingByUserId(userId);
        }

        BigDecimal totalExpense = projections.stream()
                .map(CategorySpendingProjection::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return projections.stream().map(proj -> {
            Double percentage = 0.0;
            if (totalExpense.compareTo(BigDecimal.ZERO) > 0) {
                percentage = proj.getTotalAmount()
                        .divide(totalExpense, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
            }
            return new CategorySpendingResponse(
                    proj.getCategoryId(),
                    proj.getCategoryName(),
                    proj.getColorHex(),
                    proj.getIconName(),
                    proj.getTotalAmount().setScale(2, RoundingMode.HALF_UP),
                    percentage,
                    proj.getTransactionCount()
            );
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpendingTrendResponse> getSpendingTrends(UUID userId, int months) {
        int monthCount = Math.min(Math.max(months, 1), 24); // between 1 and 24 months
        YearMonth currentYearMonth = YearMonth.now(ZoneOffset.UTC);
        YearMonth startYearMonth = currentYearMonth.minusMonths(monthCount - 1);

        Instant startDate = startYearMonth.atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endDate = currentYearMonth.plusMonths(1).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<Transaction> transactions = transactionRepository.findByUserIdAndDateRange(userId, startDate, endDate);

        // Pre-populate trend buckets in chronological order
        Map<YearMonth, BigDecimal[]> trendMap = new LinkedHashMap<>();
        for (int i = 0; i < monthCount; i++) {
            YearMonth ym = startYearMonth.plusMonths(i);
            trendMap.put(ym, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO}); // [income, expense]
        }

        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);

        for (Transaction tx : transactions) {
            ZonedDateTime zdt = tx.getTransactionDate().atZone(ZoneOffset.UTC);
            YearMonth txYm = YearMonth.from(zdt);
            if (trendMap.containsKey(txYm)) {
                BigDecimal[] totals = trendMap.get(txYm);
                if (tx.getType() == CategoryType.INCOME) {
                    totals[0] = totals[0].add(tx.getAmount());
                } else {
                    totals[1] = totals[1].add(tx.getAmount());
                }
            }
        }

        List<SpendingTrendResponse> responses = new ArrayList<>();
        for (Map.Entry<YearMonth, BigDecimal[]> entry : trendMap.entrySet()) {
            YearMonth ym = entry.getKey();
            BigDecimal[] totals = entry.getValue();
            String label = ym.format(monthFormatter);
            responses.add(new SpendingTrendResponse(
                    label,
                    totals[0].setScale(2, RoundingMode.HALF_UP),
                    totals[1].setScale(2, RoundingMode.HALF_UP)
            ));
        }

        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getRecentTransactions(UUID userId, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 50);
        Pageable pageable = PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "transactionDate"));
        return transactionRepository.findByUserId(userId, pageable)
                .map(TransactionResponse::fromEntity)
                .getContent();
    }

    private Category validateAndGetCategory(UUID categoryId, UUID userId, CategoryType expectedType) {
        Category category = categoryRepository.findAvailableById(categoryId, userId)
                .orElseThrow(() -> new BadRequestException("Category not found or not accessible"));

        if (category.getType() != expectedType) {
            throw new BadRequestException("Category '" + category.getName() + "' is of type " +
                                          category.getType() + " and cannot be used for " + expectedType + " transactions");
        }

        return category;
    }
}
