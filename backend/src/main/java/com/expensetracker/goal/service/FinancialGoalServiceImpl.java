package com.expensetracker.goal.service;

import com.expensetracker.common.exception.BadRequestException;
import com.expensetracker.common.exception.ResourceNotFoundException;
import com.expensetracker.goal.dto.*;
import com.expensetracker.goal.entity.ContributionType;
import com.expensetracker.goal.entity.FinancialGoal;
import com.expensetracker.goal.entity.GoalContribution;
import com.expensetracker.goal.entity.GoalStatus;
import com.expensetracker.goal.repository.FinancialGoalRepository;
import com.expensetracker.goal.repository.GoalContributionRepository;
import com.expensetracker.user.entity.User;
import com.expensetracker.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FinancialGoalServiceImpl implements FinancialGoalService {

    private final FinancialGoalRepository goalRepository;
    private final GoalContributionRepository contributionRepository;
    private final UserRepository userRepository;

    public FinancialGoalServiceImpl(FinancialGoalRepository goalRepository,
                                    GoalContributionRepository contributionRepository,
                                    UserRepository userRepository) {
        this.goalRepository = goalRepository;
        this.contributionRepository = contributionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public FinancialGoalResponse createGoal(UUID userId, CreateGoalRequest request) {
        User user = getUserOrThrow(userId);

        BigDecimal initialAmount = request.initialAmount() != null
                ? request.initialAmount()
                : BigDecimal.ZERO;

        if (initialAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Initial amount cannot be negative");
        }

        FinancialGoal goal = new FinancialGoal(
                user,
                request.name().trim(),
                request.description() != null ? request.description().trim() : null,
                request.targetAmount(),
                initialAmount,
                request.targetDate()
        );

        if (initialAmount.compareTo(request.targetAmount()) >= 0) {
            goal.setStatus(GoalStatus.COMPLETED);
        }

        FinancialGoal saved = goalRepository.save(goal);

        if (initialAmount.compareTo(BigDecimal.ZERO) > 0) {
            GoalContribution contribution = new GoalContribution(
                    saved,
                    user,
                    initialAmount,
                    ContributionType.CONTRIBUTION,
                    "Initial goal balance"
            );
            contributionRepository.save(contribution);
        }

        return FinancialGoalResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinancialGoalResponse> getGoals(UUID userId, GoalStatus status) {
        List<FinancialGoal> goals;
        if (status != null) {
            goals = goalRepository.findByUserIdAndStatusOrderByTargetDateAsc(userId, status);
        } else {
            goals = goalRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }
        return goals.stream().map(FinancialGoalResponse::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FinancialGoalResponse getGoalById(UUID userId, UUID goalId) {
        FinancialGoal goal = getGoalOrThrow(goalId, userId);
        return FinancialGoalResponse.fromEntity(goal);
    }

    @Override
    public FinancialGoalResponse updateGoal(UUID userId, UUID goalId, UpdateGoalRequest request) {
        FinancialGoal goal = getGoalOrThrow(goalId, userId);

        if (request.name() != null && !request.name().trim().isEmpty()) {
            goal.setName(request.name().trim());
        }
        if (request.description() != null) {
            goal.setDescription(request.description().trim());
        }
        if (request.targetAmount() != null) {
            goal.setTargetAmount(request.targetAmount());
            if (goal.getCurrentAmount().compareTo(request.targetAmount()) >= 0
                    && goal.getStatus() == GoalStatus.ACTIVE) {
                goal.setStatus(GoalStatus.COMPLETED);
            }
        }
        if (request.targetDate() != null) {
            goal.setTargetDate(request.targetDate());
        }
        if (request.status() != null) {
            goal.setStatus(request.status());
        }

        FinancialGoal updated = goalRepository.save(goal);
        return FinancialGoalResponse.fromEntity(updated);
    }

    @Override
    public void deleteGoal(UUID userId, UUID goalId) {
        FinancialGoal goal = getGoalOrThrow(goalId, userId);
        goalRepository.delete(goal);
    }

    @Override
    public FinancialGoalResponse recordContribution(UUID userId, UUID goalId, GoalContributionRequest request) {
        User user = getUserOrThrow(userId);

        // Pessimistic write lock ensures safe concurrency
        FinancialGoal goal = goalRepository.findWithLockByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Financial goal not found"));

        if (goal.getStatus() == GoalStatus.CANCELLED) {
            throw new BadRequestException("Cannot add contributions or withdrawals to a cancelled goal");
        }

        BigDecimal currentAmount = goal.getCurrentAmount();
        BigDecimal newAmount;

        if (request.type() == ContributionType.CONTRIBUTION) {
            newAmount = currentAmount.add(request.amount());
            if (newAmount.compareTo(goal.getTargetAmount()) >= 0) {
                goal.setStatus(GoalStatus.COMPLETED);
            }
        } else if (request.type() == ContributionType.WITHDRAWAL) {
            if (request.amount().compareTo(currentAmount) > 0) {
                throw new BadRequestException("Cannot withdraw more than current saved amount ($" + currentAmount + ")");
            }
            newAmount = currentAmount.subtract(request.amount());
            if (newAmount.compareTo(goal.getTargetAmount()) < 0 && goal.getStatus() == GoalStatus.COMPLETED) {
                goal.setStatus(GoalStatus.ACTIVE);
            }
        } else {
            throw new BadRequestException("Unknown contribution type: " + request.type());
        }

        goal.setCurrentAmount(newAmount);
        FinancialGoal saved = goalRepository.save(goal);

        GoalContribution contribution = new GoalContribution(
                saved,
                user,
                request.amount(),
                request.type(),
                request.notes() != null ? request.notes().trim() : null
        );
        contributionRepository.save(contribution);

        return FinancialGoalResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoalContributionResponse> getGoalContributions(UUID userId, UUID goalId) {
        // Verify ownership
        getGoalOrThrow(goalId, userId);
        List<GoalContribution> list = contributionRepository.findByGoalIdOrderByCreatedAtDesc(goalId);
        return list.stream().map(GoalContributionResponse::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GoalSummaryResponse getGoalSummary(UUID userId) {
        List<FinancialGoal> allGoals = goalRepository.findByUserIdOrderByCreatedAtDesc(userId);

        BigDecimal totalTarget = BigDecimal.ZERO;
        BigDecimal totalSaved = BigDecimal.ZERO;
        int activeCount = 0;
        int completedCount = 0;

        for (FinancialGoal g : allGoals) {
            if (g.getStatus() == GoalStatus.ACTIVE || g.getStatus() == GoalStatus.COMPLETED) {
                totalTarget = totalTarget.add(g.getTargetAmount());
                totalSaved = totalSaved.add(g.getCurrentAmount());
                if (g.getStatus() == GoalStatus.COMPLETED) {
                    completedCount++;
                } else {
                    activeCount++;
                }
            }
        }

        BigDecimal totalRemaining = totalTarget.subtract(totalSaved).max(BigDecimal.ZERO);
        BigDecimal progress = totalTarget.compareTo(BigDecimal.ZERO) > 0
                ? totalSaved.multiply(new BigDecimal("100")).divide(totalTarget, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<FinancialGoalResponse> goalResponses = allGoals.stream()
                .map(FinancialGoalResponse::fromEntity)
                .toList();

        return new GoalSummaryResponse(
                totalTarget,
                totalSaved,
                totalRemaining,
                progress,
                activeCount,
                completedCount,
                goalResponses
        );
    }

    private User getUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private FinancialGoal getGoalOrThrow(UUID goalId, UUID userId) {
        return goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Financial goal not found"));
    }
}
