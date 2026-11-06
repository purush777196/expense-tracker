package com.expensetracker.goal;

import com.expensetracker.common.exception.BadRequestException;
import com.expensetracker.common.exception.ResourceNotFoundException;
import com.expensetracker.goal.dto.*;
import com.expensetracker.goal.entity.ContributionType;
import com.expensetracker.goal.entity.FinancialGoal;
import com.expensetracker.goal.entity.GoalContribution;
import com.expensetracker.goal.entity.GoalStatus;
import com.expensetracker.goal.repository.FinancialGoalRepository;
import com.expensetracker.goal.repository.GoalContributionRepository;
import com.expensetracker.goal.service.FinancialGoalServiceImpl;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialGoalServiceTest {

    @Mock
    private FinancialGoalRepository goalRepository;

    @Mock
    private GoalContributionRepository contributionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FinancialGoalServiceImpl goalService;

    private User testUser;
    private FinancialGoal testGoal;
    private UUID userId;
    private UUID goalId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        goalId = UUID.randomUUID();

        testUser = new User("goaluser@example.com", "hash", "Goal User", "USD");
        testUser.setId(userId);

        testGoal = new FinancialGoal(
                testUser,
                "Emergency Fund",
                "3 months buffer",
                new BigDecimal("3000.00"),
                new BigDecimal("1000.00"),
                LocalDate.of(2027, 12, 31)
        );
        testGoal.setId(goalId);
    }

    @Test
    @DisplayName("Should create goal with initial contribution audit")
    void shouldCreateGoalWithInitialContribution() {
        CreateGoalRequest request = new CreateGoalRequest(
                "Emergency Fund",
                "3 months buffer",
                new BigDecimal("3000.00"),
                new BigDecimal("500.00"),
                LocalDate.of(2027, 12, 31)
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(goalRepository.save(any(FinancialGoal.class))).thenAnswer(i -> {
            FinancialGoal g = i.getArgument(0);
            g.setId(goalId);
            return g;
        });

        FinancialGoalResponse response = goalService.createGoal(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Emergency Fund");
        assertThat(response.targetAmount()).isEqualByComparingTo(new BigDecimal("3000.00"));
        assertThat(response.currentAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(response.isCompleted()).isFalse();

        // Verify contribution audit record created
        verify(contributionRepository, times(1)).save(any(GoalContribution.class));
    }

    @Test
    @DisplayName("Should record contribution and auto-complete when target reached")
    void shouldRecordContributionAndCompleteGoal() {
        GoalContributionRequest request = new GoalContributionRequest(
                new BigDecimal("2000.00"),
                ContributionType.CONTRIBUTION,
                "Year-end bonus"
        );

        // Current = $1000 + $2000 = $3000 (Target = $3000)
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(goalRepository.findWithLockByIdAndUserId(goalId, userId)).thenReturn(Optional.of(testGoal));
        when(goalRepository.save(any(FinancialGoal.class))).thenReturn(testGoal);

        FinancialGoalResponse response = goalService.recordContribution(userId, goalId, request);

        assertThat(response).isNotNull();
        assertThat(testGoal.getCurrentAmount()).isEqualByComparingTo(new BigDecimal("3000.00"));
        assertThat(testGoal.getStatus()).isEqualTo(GoalStatus.COMPLETED);
        verify(contributionRepository).save(any(GoalContribution.class));
    }

    @Test
    @DisplayName("Should reject withdrawal exceeding current saved amount")
    void shouldRejectOverWithdrawal() {
        GoalContributionRequest request = new GoalContributionRequest(
                new BigDecimal("1500.00"), // Current is 1000
                ContributionType.WITHDRAWAL,
                "Emergency repair"
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(goalRepository.findWithLockByIdAndUserId(goalId, userId)).thenReturn(Optional.of(testGoal));

        assertThatThrownBy(() -> goalService.recordContribution(userId, goalId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot withdraw more than current saved amount");
    }

    @Test
    @DisplayName("Should compute aggregated goal summary accurately")
    void shouldComputeGoalSummary() {
        FinancialGoal goal2 = new FinancialGoal(
                testUser,
                "Vacation",
                "Trip to Japan",
                new BigDecimal("2000.00"),
                new BigDecimal("2000.00"),
                LocalDate.of(2027, 6, 1)
        );
        goal2.setStatus(GoalStatus.COMPLETED);

        when(goalRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(testGoal, goal2));

        GoalSummaryResponse summary = goalService.getGoalSummary(userId);

        assertThat(summary).isNotNull();
        // Total target: 3000 + 2000 = 5000
        assertThat(summary.totalTargetAmount()).isEqualByComparingTo(new BigDecimal("5000.00"));
        // Total saved: 1000 + 2000 = 3000
        assertThat(summary.totalSavedAmount()).isEqualByComparingTo(new BigDecimal("3000.00"));
        // Total remaining: 2000
        assertThat(summary.totalRemainingAmount()).isEqualByComparingTo(new BigDecimal("2000.00"));
        // Progress: 60%
        assertThat(summary.overallProgressPercentage()).isEqualByComparingTo(new BigDecimal("60.00"));
        assertThat(summary.activeGoalsCount()).isEqualTo(1);
        assertThat(summary.completedGoalsCount()).isEqualTo(1);
    }
}
