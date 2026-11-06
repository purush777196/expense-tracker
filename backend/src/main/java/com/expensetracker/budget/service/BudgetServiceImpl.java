package com.expensetracker.budget.service;

import com.expensetracker.budget.dto.*;
import com.expensetracker.budget.entity.Budget;
import com.expensetracker.budget.entity.BudgetAlert;
import com.expensetracker.budget.repository.BudgetAlertRepository;
import com.expensetracker.budget.repository.BudgetRepository;
import com.expensetracker.category.entity.Category;
import com.expensetracker.category.entity.CategoryType;
import com.expensetracker.category.repository.CategoryRepository;
import com.expensetracker.common.exception.BadRequestException;
import com.expensetracker.common.exception.ResourceNotFoundException;
import com.expensetracker.transaction.repository.TransactionRepository;
import com.expensetracker.user.entity.User;
import com.expensetracker.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class BudgetServiceImpl implements BudgetService {

    private static final BigDecimal[] DEFAULT_THRESHOLDS = {
            new BigDecimal("50.00"),
            new BigDecimal("75.00"),
            new BigDecimal("90.00"),
            new BigDecimal("100.00")
    };

    private final BudgetRepository budgetRepository;
    private final BudgetAlertRepository budgetAlertRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public BudgetServiceImpl(BudgetRepository budgetRepository,
                             BudgetAlertRepository budgetAlertRepository,
                             CategoryRepository categoryRepository,
                             TransactionRepository transactionRepository,
                             UserRepository userRepository) {
        this.budgetRepository = budgetRepository;
        this.budgetAlertRepository = budgetAlertRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public BudgetResponse createBudget(UUID userId, CreateBudgetRequest request) {
        User user = getUserOrThrow(userId);

        if (request.startDate().isAfter(request.endDate())) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        Category category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findAvailableById(request.categoryId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found or not accessible"));

            if (category.getType() != CategoryType.EXPENSE) {
                throw new BadRequestException("Budgets can only be assigned to EXPENSE categories");
            }

            List<Budget> overlapping = budgetRepository.findOverlappingCategoryBudgets(
                    userId, category.getId(), request.startDate(), request.endDate(), null);
            if (!overlapping.isEmpty()) {
                throw new BadRequestException("An active budget already exists for this category during the selected period");
            }
        } else {
            List<Budget> overlapping = budgetRepository.findOverlappingOverallBudgets(
                    userId, request.startDate(), request.endDate(), null);
            if (!overlapping.isEmpty()) {
                throw new BadRequestException("An active overall budget already exists during the selected period");
            }
        }

        BigDecimal threshold = request.alertThresholdPercentage() != null
                ? request.alertThresholdPercentage()
                : new BigDecimal("80.00");

        Budget budget = new Budget(
                user,
                category,
                request.name().trim(),
                request.amount(),
                request.period(),
                request.startDate(),
                request.endDate(),
                threshold
        );

        Budget saved = budgetRepository.save(budget);
        return BudgetResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgets(UUID userId, Boolean activeOnly) {
        List<Budget> budgets;
        if (Boolean.TRUE.equals(activeOnly)) {
            budgets = budgetRepository.findByUserIdAndIsActiveTrueOrderByStartDateDesc(userId);
        } else {
            budgets = budgetRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }
        return budgets.stream().map(BudgetResponse::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetResponse getBudgetById(UUID userId, UUID budgetId) {
        Budget budget = getBudgetOrThrow(budgetId, userId);
        return BudgetResponse.fromEntity(budget);
    }

    @Override
    public BudgetProgressResponse getBudgetProgress(UUID userId, UUID budgetId) {
        Budget budget = getBudgetOrThrow(budgetId, userId);
        return calculateProgressAndCheckAlerts(budget, userId);
    }

    @Override
    public BudgetOverviewResponse getBudgetOverview(UUID userId) {
        List<Budget> activeBudgets = budgetRepository.findByUserIdAndIsActiveTrueOrderByStartDateDesc(userId);

        BigDecimal totalBudgeted = BigDecimal.ZERO;
        BigDecimal totalSpent = BigDecimal.ZERO;
        int atRiskCount = 0;
        int exceededCount = 0;

        List<BudgetProgressResponse> progressList = new ArrayList<>();
        for (Budget b : activeBudgets) {
            BudgetProgressResponse progress = calculateProgressAndCheckAlerts(b, userId);
            progressList.add(progress);

            totalBudgeted = totalBudgeted.add(b.getAmount());
            totalSpent = totalSpent.add(progress.amountSpent());

            if (progress.isOverBudget()) {
                exceededCount++;
            } else if (progress.utilizationPercentage().compareTo(new BigDecimal("75.00")) >= 0) {
                atRiskCount++;
            }
        }

        BigDecimal totalRemaining = totalBudgeted.subtract(totalSpent).max(BigDecimal.ZERO);
        BigDecimal overallUtilization = totalBudgeted.compareTo(BigDecimal.ZERO) > 0
                ? totalSpent.multiply(new BigDecimal("100")).divide(totalBudgeted, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new BudgetOverviewResponse(
                totalBudgeted,
                totalSpent,
                totalRemaining,
                overallUtilization,
                activeBudgets.size(),
                atRiskCount,
                exceededCount,
                progressList
        );
    }

    @Override
    public BudgetResponse updateBudget(UUID userId, UUID budgetId, UpdateBudgetRequest request) {
        Budget budget = getBudgetOrThrow(budgetId, userId);

        if (request.name() != null && !request.name().trim().isEmpty()) {
            budget.setName(request.name().trim());
        }
        if (request.amount() != null) {
            budget.setAmount(request.amount());
        }
        if (request.alertThresholdPercentage() != null) {
            budget.setAlertThresholdPercentage(request.alertThresholdPercentage());
        }
        if (request.isActive() != null) {
            budget.setActive(request.isActive());
        }

        Budget updated = budgetRepository.save(budget);
        return BudgetResponse.fromEntity(updated);
    }

    @Override
    public void deleteBudget(UUID userId, UUID budgetId) {
        Budget budget = getBudgetOrThrow(budgetId, userId);
        budgetRepository.delete(budget);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetAlertResponse> getBudgetAlerts(UUID userId, Boolean unreadOnly) {
        List<BudgetAlert> alerts;
        if (Boolean.TRUE.equals(unreadOnly)) {
            alerts = budgetAlertRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        } else {
            alerts = budgetAlertRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }
        return alerts.stream().map(BudgetAlertResponse::fromEntity).toList();
    }

    @Override
    public void markAlertAsRead(UUID userId, UUID alertId) {
        BudgetAlert alert = budgetAlertRepository.findByIdAndUserId(alertId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget alert not found"));
        alert.setRead(true);
        budgetAlertRepository.save(alert);
    }

    private BudgetProgressResponse calculateProgressAndCheckAlerts(Budget budget, UUID userId) {
        Instant startInstant = budget.getStartDate().atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endInstant = budget.getEndDate().atTime(LocalTime.MAX).atZone(ZoneOffset.UTC).toInstant();

        BigDecimal spent;
        if (budget.getCategory() != null) {
            spent = transactionRepository.sumExpenseByUserIdAndCategoryAndDateRange(
                    userId, budget.getCategory().getId(), startInstant, endInstant);
        } else {
            spent = transactionRepository.sumExpenseByUserIdAndDateRange(
                    userId, startInstant, endInstant);
        }
        if (spent == null) spent = BigDecimal.ZERO;

        BigDecimal budgetAmount = budget.getAmount();
        BigDecimal remaining = budgetAmount.subtract(spent).max(BigDecimal.ZERO);
        BigDecimal overBudget = spent.subtract(budgetAmount).max(BigDecimal.ZERO);
        boolean isOver = spent.compareTo(budgetAmount) > 0;

        BigDecimal utilization = budgetAmount.compareTo(BigDecimal.ZERO) > 0
                ? spent.multiply(new BigDecimal("100")).divide(budgetAmount, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        String alertStatus;
        if (utilization.compareTo(new BigDecimal("100.00")) >= 0) {
            alertStatus = "EXCEEDED";
        } else if (utilization.compareTo(new BigDecimal("90.00")) >= 0) {
            alertStatus = "CRITICAL";
        } else if (utilization.compareTo(new BigDecimal("75.00")) >= 0) {
            alertStatus = "WARNING";
        } else {
            alertStatus = "NORMAL";
        }

        LocalDate now = LocalDate.now();
        long daysRemaining = now.isAfter(budget.getEndDate()) ? 0 : ChronoUnit.DAYS.between(now, budget.getEndDate());

        // Process threshold alerts
        if (budget.isActive()) {
            checkAndGenerateAlerts(budget, spent, utilization);
        }

        return new BudgetProgressResponse(
                BudgetResponse.fromEntity(budget),
                spent,
                remaining,
                utilization,
                overBudget,
                isOver,
                alertStatus,
                daysRemaining
        );
    }

    private void checkAndGenerateAlerts(Budget budget, BigDecimal spent, BigDecimal utilization) {
        List<BigDecimal> thresholdsToCheck = new ArrayList<>(List.of(DEFAULT_THRESHOLDS));
        if (budget.getAlertThresholdPercentage() != null && !thresholdsToCheck.contains(budget.getAlertThresholdPercentage())) {
            thresholdsToCheck.add(budget.getAlertThresholdPercentage());
        }

        for (BigDecimal threshold : thresholdsToCheck) {
            if (utilization.compareTo(threshold) >= 0) {
                if (!budgetAlertRepository.existsByBudgetIdAndThresholdPercentage(budget.getId(), threshold)) {
                    String message = String.format("Budget '%s' has reached %s%% utilization ($%s of $%s spent)",
                            budget.getName(), threshold.stripTrailingZeros().toPlainString(),
                            spent.toPlainString(), budget.getAmount().toPlainString());
                    BudgetAlert alert = new BudgetAlert(
                            budget,
                            budget.getUser(),
                            threshold,
                            spent,
                            budget.getAmount(),
                            message
                    );
                    budgetAlertRepository.save(alert);
                }
            }
        }
    }

    private User getUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Budget getBudgetOrThrow(UUID budgetId, UUID userId) {
        return budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
    }
}
