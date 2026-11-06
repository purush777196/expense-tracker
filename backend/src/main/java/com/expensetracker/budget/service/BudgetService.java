package com.expensetracker.budget.service;

import com.expensetracker.budget.dto.*;

import java.util.List;
import java.util.UUID;

public interface BudgetService {

    BudgetResponse createBudget(UUID userId, CreateBudgetRequest request);

    List<BudgetResponse> getBudgets(UUID userId, Boolean activeOnly);

    BudgetResponse getBudgetById(UUID userId, UUID budgetId);

    BudgetProgressResponse getBudgetProgress(UUID userId, UUID budgetId);

    BudgetOverviewResponse getBudgetOverview(UUID userId);

    BudgetResponse updateBudget(UUID userId, UUID budgetId, UpdateBudgetRequest request);

    void deleteBudget(UUID userId, UUID budgetId);

    List<BudgetAlertResponse> getBudgetAlerts(UUID userId, Boolean unreadOnly);

    void markAlertAsRead(UUID userId, UUID alertId);
}
