package com.expensetracker.recurring;

import com.expensetracker.category.entity.Category;
import com.expensetracker.category.entity.CategoryType;
import com.expensetracker.category.repository.CategoryRepository;
import com.expensetracker.common.exception.BadRequestException;
import com.expensetracker.recurring.dto.CreateRecurringTransactionRequest;
import com.expensetracker.recurring.dto.RecurringTransactionResponse;
import com.expensetracker.recurring.dto.UpcomingRecurringResponse;
import com.expensetracker.recurring.entity.RecurringExecution;
import com.expensetracker.recurring.entity.RecurringFrequency;
import com.expensetracker.recurring.entity.RecurringStatus;
import com.expensetracker.recurring.entity.RecurringTransaction;
import com.expensetracker.recurring.repository.RecurringExecutionRepository;
import com.expensetracker.recurring.repository.RecurringTransactionRepository;
import com.expensetracker.recurring.service.RecurringTransactionServiceImpl;
import com.expensetracker.transaction.entity.PaymentMethod;
import com.expensetracker.transaction.entity.Transaction;
import com.expensetracker.transaction.repository.TransactionRepository;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecurringTransactionServiceTest {

    @Mock
    private RecurringTransactionRepository recurringRepository;

    @Mock
    private RecurringExecutionRepository executionRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RecurringTransactionServiceImpl recurringService;

    private User testUser;
    private Category testExpenseCategory;
    private Category testIncomeCategory;
    private RecurringTransaction testRecurring;
    private UUID userId;
    private UUID recurringId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        recurringId = UUID.randomUUID();

        testUser = new User("user@example.com", "hash", "Test User", "USD");
        testUser.setId(userId);

        testExpenseCategory = new Category(testUser, "Rent & Housing", CategoryType.EXPENSE, "#EF4444", "Home", false);
        testExpenseCategory.setId(UUID.randomUUID());

        testIncomeCategory = new Category(testUser, "Salary", CategoryType.INCOME, "#10B981", "DollarSign", false);
        testIncomeCategory.setId(UUID.randomUUID());

        testRecurring = new RecurringTransaction(
                testUser,
                testExpenseCategory,
                CategoryType.EXPENSE,
                new BigDecimal("1200.00"),
                "Apartment Monthly Rent",
                "Landlord Corp",
                PaymentMethod.BANK_TRANSFER,
                "Due by 1st of month",
                RecurringFrequency.MONTHLY,
                1,
                LocalDate.of(2026, 11, 1),
                null,
                LocalDate.of(2026, 11, 1)
        );
        testRecurring.setId(recurringId);
    }

    @Test
    @DisplayName("Should successfully create a recurring expense transaction")
    void shouldCreateRecurringTransaction() {
        CreateRecurringTransactionRequest request = new CreateRecurringTransactionRequest(
                testExpenseCategory.getId(),
                CategoryType.EXPENSE,
                new BigDecimal("1200.00"),
                "Apartment Monthly Rent",
                "Landlord Corp",
                PaymentMethod.BANK_TRANSFER,
                "Due by 1st of month",
                RecurringFrequency.MONTHLY,
                1,
                LocalDate.of(2026, 11, 1),
                null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findAvailableById(testExpenseCategory.getId(), userId))
                .thenReturn(Optional.of(testExpenseCategory));
        when(recurringRepository.save(any(RecurringTransaction.class))).thenAnswer(invocation -> {
            RecurringTransaction saved = invocation.getArgument(0);
            saved.setId(recurringId);
            return saved;
        });

        RecurringTransactionResponse response = recurringService.createRecurringTransaction(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(recurringId);
        assertThat(response.amount()).isEqualByComparingTo("1200.00");
        assertThat(response.frequency()).isEqualTo(RecurringFrequency.MONTHLY);
        assertThat(response.nextOccurrenceDate()).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(response.status()).isEqualTo(RecurringStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should reject recurring transaction when category type mismatches")
    void shouldRejectMismatchedCategoryType() {
        CreateRecurringTransactionRequest request = new CreateRecurringTransactionRequest(
                testIncomeCategory.getId(),
                CategoryType.EXPENSE,
                new BigDecimal("500.00"),
                "Invalid Type",
                null,
                PaymentMethod.CASH,
                null,
                RecurringFrequency.WEEKLY,
                1,
                LocalDate.of(2026, 11, 1),
                null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findAvailableById(testIncomeCategory.getId(), userId))
                .thenReturn(Optional.of(testIncomeCategory));

        assertThatThrownBy(() -> recurringService.createRecurringTransaction(userId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("must match category type");
    }

    @Test
    @DisplayName("Should reject recurring transaction when start date is after end date")
    void shouldRejectStartDateAfterEndDate() {
        CreateRecurringTransactionRequest request = new CreateRecurringTransactionRequest(
                testExpenseCategory.getId(),
                CategoryType.EXPENSE,
                new BigDecimal("50.00"),
                "Subscription",
                null,
                PaymentMethod.CARD,
                null,
                RecurringFrequency.MONTHLY,
                1,
                LocalDate.of(2026, 12, 1),
                LocalDate.of(2026, 11, 1)
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findAvailableById(testExpenseCategory.getId(), userId))
                .thenReturn(Optional.of(testExpenseCategory));

        assertThatThrownBy(() -> recurringService.createRecurringTransaction(userId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Start date cannot be after end date");
    }

    @Test
    @DisplayName("Should process due recurring transactions and skip existing execution idempotently")
    void shouldProcessDueRecurringTransactions() {
        LocalDate asOfDate = LocalDate.of(2026, 11, 1);
        testRecurring.setNextOccurrenceDate(LocalDate.of(2026, 11, 1));

        when(recurringRepository.findDueRecurringTransactions(asOfDate)).thenReturn(List.of(testRecurring));
        when(executionRepository.existsByRecurringTransactionIdAndScheduledDate(recurringId, LocalDate.of(2026, 11, 1)))
                .thenReturn(false);

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            tx.setId(UUID.randomUUID());
            return tx;
        });

        when(executionRepository.save(any(RecurringExecution.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(recurringRepository.save(any(RecurringTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        int processed = recurringService.processDueRecurringTransactions(asOfDate);

        assertThat(processed).isEqualTo(1);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(executionRepository, times(1)).save(any(RecurringExecution.class));
        assertThat(testRecurring.getNextOccurrenceDate()).isEqualTo(LocalDate.of(2026, 12, 1));
        assertThat(testRecurring.getLastGeneratedDate()).isEqualTo(LocalDate.of(2026, 11, 1));
    }

    @Test
    @DisplayName("Should handle pause, resume, and cancel lifecycle")
    void shouldHandleLifecycle() {
        when(recurringRepository.findByIdAndUserId(recurringId, userId)).thenReturn(Optional.of(testRecurring));
        when(recurringRepository.save(any(RecurringTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RecurringTransactionResponse paused = recurringService.pauseRecurringTransaction(userId, recurringId);
        assertThat(paused.status()).isEqualTo(RecurringStatus.PAUSED);

        RecurringTransactionResponse resumed = recurringService.resumeRecurringTransaction(userId, recurringId);
        assertThat(resumed.status()).isEqualTo(RecurringStatus.ACTIVE);

        RecurringTransactionResponse cancelled = recurringService.cancelRecurringTransaction(userId, recurringId);
        assertThat(cancelled.status()).isEqualTo(RecurringStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should get upcoming recurring transactions with correct days count")
    void shouldGetUpcomingRecurring() {
        when(recurringRepository.findUpcomingByUserIdAndDateRange(eq(userId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(testRecurring));

        List<UpcomingRecurringResponse> list = recurringService.getUpcomingRecurring(userId, 30);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).recurringId()).isEqualTo(recurringId);
        assertThat(list.get(0).description()).isEqualTo("Apartment Monthly Rent");
    }
}
