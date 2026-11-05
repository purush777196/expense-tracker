package com.expensetracker.transaction;

import com.expensetracker.category.entity.Category;
import com.expensetracker.category.entity.CategoryType;
import com.expensetracker.category.repository.CategoryRepository;
import com.expensetracker.common.exception.BadRequestException;
import com.expensetracker.common.exception.ResourceNotFoundException;
import com.expensetracker.transaction.dto.CreateTransactionRequest;
import com.expensetracker.transaction.dto.TransactionResponse;
import com.expensetracker.transaction.dto.TransactionSummaryResponse;
import com.expensetracker.transaction.dto.UpdateTransactionRequest;
import com.expensetracker.transaction.entity.PaymentMethod;
import com.expensetracker.transaction.entity.Transaction;
import com.expensetracker.transaction.repository.TransactionRepository;
import com.expensetracker.transaction.service.TransactionServiceImpl;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private UUID userId;
    private User testUser;
    private Category expenseCategory;
    private Category incomeCategory;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = new User("user@example.com", "hash", "Test User", "USD");
        testUser.setId(userId);

        expenseCategory = new Category(null, "Dining", CategoryType.EXPENSE, "#EF4444", "Utensils", true);
        expenseCategory.setId(UUID.randomUUID());

        incomeCategory = new Category(null, "Salary", CategoryType.INCOME, "#10B981", "Briefcase", true);
        incomeCategory.setId(UUID.randomUUID());

        testTransaction = new Transaction(
                testUser,
                expenseCategory,
                new BigDecimal("45.50"),
                CategoryType.EXPENSE,
                Instant.now(),
                "Dinner with friends",
                "Italian Bistro",
                PaymentMethod.CARD,
                "Celebration"
        );
        testTransaction.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Should create transaction successfully when valid")
    void shouldCreateTransactionSuccessfully() {
        CreateTransactionRequest request = new CreateTransactionRequest(
                new BigDecimal("45.50"),
                CategoryType.EXPENSE,
                expenseCategory.getId(),
                Instant.now(),
                "Dinner with friends",
                "Italian Bistro",
                PaymentMethod.CARD,
                "Celebration",
                false,
                false
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findAvailableById(expenseCategory.getId(), userId)).thenReturn(Optional.of(expenseCategory));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            tx.setId(UUID.randomUUID());
            return tx;
        });

        TransactionResponse response = transactionService.createTransaction(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("45.50"));
        assertThat(response.description()).isEqualTo("Dinner with friends");
        assertThat(response.category().name()).isEqualTo("Dining");
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should reject transaction when category type mismatches transaction type")
    void shouldRejectTypeMismatch() {
        // Attempting to create an INCOME transaction using an EXPENSE category
        CreateTransactionRequest request = new CreateTransactionRequest(
                new BigDecimal("100.00"),
                CategoryType.INCOME,
                expenseCategory.getId(),
                Instant.now(),
                "Bonus payout",
                "Employer",
                PaymentMethod.BANK_TRANSFER,
                null,
                false,
                false
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findAvailableById(expenseCategory.getId(), userId)).thenReturn(Optional.of(expenseCategory));

        assertThatThrownBy(() -> transactionService.createTransaction(userId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot be used for INCOME transactions");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject transaction when category is not found or inaccessible")
    void shouldRejectInaccessibleCategory() {
        UUID nonExistentId = UUID.randomUUID();
        CreateTransactionRequest request = new CreateTransactionRequest(
                new BigDecimal("50.00"),
                CategoryType.EXPENSE,
                nonExistentId,
                Instant.now(),
                "Supplies",
                null,
                PaymentMethod.CASH,
                null,
                false,
                false
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findAvailableById(nonExistentId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.createTransaction(userId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Category not found or not accessible");
    }

    @Test
    @DisplayName("Should retrieve transaction by ID for owner")
    void shouldRetrieveTransactionByIdForOwner() {
        when(transactionRepository.findByIdAndUserId(testTransaction.getId(), userId))
                .thenReturn(Optional.of(testTransaction));

        TransactionResponse response = transactionService.getTransactionById(userId, testTransaction.getId());

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(testTransaction.getId());
        assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("45.50"));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when accessing another user's transaction")
    void shouldThrowNotFoundForCrossUserAccess() {
        UUID txId = UUID.randomUUID();
        when(transactionRepository.findByIdAndUserId(txId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransactionById(userId, txId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Transaction not found");
    }

    @Test
    @DisplayName("Should update transaction successfully")
    void shouldUpdateTransactionSuccessfully() {
        UpdateTransactionRequest request = new UpdateTransactionRequest(
                new BigDecimal("60.00"),
                CategoryType.EXPENSE,
                expenseCategory.getId(),
                Instant.now(),
                "Updated description",
                "New Merchant",
                PaymentMethod.CARD,
                "Updated notes",
                false,
                false
        );

        when(transactionRepository.findByIdAndUserId(testTransaction.getId(), userId))
                .thenReturn(Optional.of(testTransaction));
        when(categoryRepository.findAvailableById(expenseCategory.getId(), userId))
                .thenReturn(Optional.of(expenseCategory));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TransactionResponse response = transactionService.updateTransaction(userId, testTransaction.getId(), request);

        assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("60.00"));
        assertThat(response.description()).isEqualTo("Updated description");
    }

    @Test
    @DisplayName("Should delete transaction when owned by user")
    void shouldDeleteTransactionWhenOwned() {
        when(transactionRepository.findByIdAndUserId(testTransaction.getId(), userId))
                .thenReturn(Optional.of(testTransaction));

        transactionService.deleteTransaction(userId, testTransaction.getId());

        verify(transactionRepository).delete(testTransaction);
    }

    @Test
    @DisplayName("Should compute financial summary accurately")
    void shouldComputeFinancialSummaryAccurately() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(transactionRepository.sumTotalAmountByUserIdAndType(userId, CategoryType.INCOME))
                .thenReturn(new BigDecimal("5000.00"));
        when(transactionRepository.sumTotalAmountByUserIdAndType(userId, CategoryType.EXPENSE))
                .thenReturn(new BigDecimal("2000.00"));
        when(transactionRepository.count(any(org.springframework.data.jpa.domain.Specification.class))).thenReturn(15L);

        TransactionSummaryResponse summary = transactionService.getTransactionSummary(userId, null, null);

        assertThat(summary.totalIncome()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(summary.totalExpense()).isEqualByComparingTo(new BigDecimal("2000.00"));
        assertThat(summary.netBalance()).isEqualByComparingTo(new BigDecimal("3000.00"));
        // Savings rate = (3000 / 5000) * 100 = 60.00%
        assertThat(summary.savingsRatePercent()).isEqualByComparingTo(new BigDecimal("60.00"));
        assertThat(summary.transactionCount()).isEqualTo(15L);
        assertThat(summary.currency()).isEqualTo("USD");
    }
}
