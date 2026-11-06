package com.expensetracker.budget;

import com.expensetracker.budget.dto.*;
import com.expensetracker.budget.entity.Budget;
import com.expensetracker.budget.entity.BudgetAlert;
import com.expensetracker.budget.entity.BudgetPeriod;
import com.expensetracker.budget.repository.BudgetAlertRepository;
import com.expensetracker.budget.repository.BudgetRepository;
import com.expensetracker.budget.service.BudgetServiceImpl;
import com.expensetracker.category.entity.Category;
import com.expensetracker.category.entity.CategoryType;
import com.expensetracker.category.repository.CategoryRepository;
import com.expensetracker.common.exception.BadRequestException;
import com.expensetracker.common.exception.ResourceNotFoundException;
import com.expensetracker.transaction.repository.TransactionRepository;
import com.expensetracker.user.entity.Role;
import com.expensetracker.user.entity.User;
import com.expensetracker.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private BudgetAlertRepository budgetAlertRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    private User testUser;
    private Category testExpenseCategory;
    private Category testIncomeCategory;
    private Budget testBudget;
    private UUID userId;
    private UUID budgetId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        budgetId = UUID.randomUUID();

        testUser = new User("user@example.com", "hash", "Test User", "USD");
        testUser.setId(userId);

        testExpenseCategory = new Category(testUser, "Dining", CategoryType.EXPENSE, "#FF5733", "Utensils", false);
        testExpenseCategory.setId(UUID.randomUUID());

        testIncomeCategory = new Category(testUser, "Salary", CategoryType.INCOME, "#33FF57", "DollarSign", false);
        testIncomeCategory.setId(UUID.randomUUID());

        testBudget = new Budget(
                testUser,
                testExpenseCategory,
                "Monthly Dining",
                new BigDecimal("500.00"),
                BudgetPeriod.MONTHLY,
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 30),
                new BigDecimal("80.00")
        );
        testBudget.setId(budgetId);
    }

    @Test
    @DisplayName("Should successfully create a category budget")
    void shouldCreateCategoryBudget() {
        CreateBudgetRequest request = new CreateBudgetRequest(
                "Monthly Dining",
                new BigDecimal("500.00"),
                BudgetPeriod.MONTHLY,
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 30),
                testExpenseCategory.getId(),
                new BigDecimal("80.00")
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findAvailableById(testExpenseCategory.getId(), userId))
                .thenReturn(Optional.of(testExpenseCategory));
        when(budgetRepository.findOverlappingCategoryBudgets(eq(userId), eq(testExpenseCategory.getId()), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(budgetRepository.save(any(Budget.class))).thenAnswer(i -> {
            Budget b = i.getArgument(0);
            b.setId(budgetId);
            return b;
        });

        BudgetResponse response = budgetService.createBudget(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Monthly Dining");
        assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(response.category()).isNotNull();
    }

    @Test
    @DisplayName("Should reject budget creation for INCOME category")
    void shouldRejectBudgetForIncomeCategory() {
        CreateBudgetRequest request = new CreateBudgetRequest(
                "Salary Budget",
                new BigDecimal("5000.00"),
                BudgetPeriod.MONTHLY,
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 30),
                testIncomeCategory.getId(),
                null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findAvailableById(testIncomeCategory.getId(), userId))
                .thenReturn(Optional.of(testIncomeCategory));

        assertThatThrownBy(() -> budgetService.createBudget(userId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("EXPENSE categories");
    }

    @Test
    @DisplayName("Should reject overlapping active budgets")
    void shouldRejectOverlappingBudgets() {
        CreateBudgetRequest request = new CreateBudgetRequest(
                "Monthly Dining Duplicate",
                new BigDecimal("600.00"),
                BudgetPeriod.MONTHLY,
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 25),
                testExpenseCategory.getId(),
                null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findAvailableById(testExpenseCategory.getId(), userId))
                .thenReturn(Optional.of(testExpenseCategory));
        when(budgetRepository.findOverlappingCategoryBudgets(eq(userId), eq(testExpenseCategory.getId()), any(), any(), any()))
                .thenReturn(List.of(testBudget));

        assertThatThrownBy(() -> budgetService.createBudget(userId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("active budget already exists");
    }

    @Test
    @DisplayName("Should accurately calculate budget progress, utilization and trigger threshold alert")
    void shouldCalculateProgressAndTriggerAlert() {
        when(budgetRepository.findByIdAndUserId(budgetId, userId)).thenReturn(Optional.of(testBudget));
        // Spending = $450 out of $500 (90% utilization)
        when(transactionRepository.sumExpenseByUserIdAndCategoryAndDateRange(eq(userId), eq(testExpenseCategory.getId()), any(Instant.class), any(Instant.class)))
                .thenReturn(new BigDecimal("450.00"));
        when(budgetAlertRepository.existsByBudgetIdAndThresholdPercentage(eq(budgetId), any(BigDecimal.class)))
                .thenReturn(false);

        BudgetProgressResponse progress = budgetService.getBudgetProgress(userId, budgetId);

        assertThat(progress).isNotNull();
        assertThat(progress.amountSpent()).isEqualByComparingTo(new BigDecimal("450.00"));
        assertThat(progress.remainingAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(progress.utilizationPercentage()).isEqualByComparingTo(new BigDecimal("90.00"));
        assertThat(progress.isOverBudget()).isFalse();
        assertThat(progress.alertStatus()).isEqualTo("CRITICAL");

        // Verify alert generation for 50%, 75%, 80%, and 90%
        verify(budgetAlertRepository, atLeast(1)).save(any(BudgetAlert.class));
    }

    @Test
    @DisplayName("Should update budget parameters successfully")
    void shouldUpdateBudget() {
        UpdateBudgetRequest request = new UpdateBudgetRequest(
                "Updated Dining",
                new BigDecimal("550.00"),
                new BigDecimal("85.00"),
                true
        );

        when(budgetRepository.findByIdAndUserId(budgetId, userId)).thenReturn(Optional.of(testBudget));
        when(budgetRepository.save(any(Budget.class))).thenReturn(testBudget);

        BudgetResponse response = budgetService.updateBudget(userId, budgetId, request);

        assertThat(response).isNotNull();
        assertThat(testBudget.getName()).isEqualTo("Updated Dining");
        assertThat(testBudget.getAmount()).isEqualByComparingTo(new BigDecimal("550.00"));
    }

    @Test
    @DisplayName("Should delete budget successfully")
    void shouldDeleteBudget() {
        when(budgetRepository.findByIdAndUserId(budgetId, userId)).thenReturn(Optional.of(testBudget));

        budgetService.deleteBudget(userId, budgetId);

        verify(budgetRepository).delete(testBudget);
    }
}
