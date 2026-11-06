package com.expensetracker.goal.service;

import com.expensetracker.goal.dto.*;
import com.expensetracker.goal.entity.GoalStatus;

import java.util.List;
import java.util.UUID;

public interface FinancialGoalService {

    FinancialGoalResponse createGoal(UUID userId, CreateGoalRequest request);

    List<FinancialGoalResponse> getGoals(UUID userId, GoalStatus status);

    FinancialGoalResponse getGoalById(UUID userId, UUID goalId);

    FinancialGoalResponse updateGoal(UUID userId, UUID goalId, UpdateGoalRequest request);

    void deleteGoal(UUID userId, UUID goalId);

    FinancialGoalResponse recordContribution(UUID userId, UUID goalId, GoalContributionRequest request);

    List<GoalContributionResponse> getGoalContributions(UUID userId, UUID goalId);

    GoalSummaryResponse getGoalSummary(UUID userId);
}
