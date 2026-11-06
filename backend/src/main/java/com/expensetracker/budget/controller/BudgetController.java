package com.expensetracker.budget.controller;

import com.expensetracker.budget.dto.*;
import com.expensetracker.budget.service.BudgetService;
import com.expensetracker.common.response.ApiResponse;
import com.expensetracker.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/budgets")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Budget Management", description = "Endpoints for creating, managing, and tracking overall and category spending budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    @Operation(summary = "Create budget", description = "Creates a new overall or category-specific spending budget")
    public ResponseEntity<ApiResponse<BudgetResponse>> createBudget(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateBudgetRequest request) {
        BudgetResponse response = budgetService.createBudget(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Budget created successfully"));
    }

    @GetMapping
    @Operation(summary = "List budgets", description = "Retrieves all budgets for the authenticated user")
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getBudgets(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Boolean activeOnly) {
        List<BudgetResponse> responses = budgetService.getBudgets(principal.getId(), activeOnly);
        return ResponseEntity.ok(ApiResponse.success(responses, "Budgets retrieved successfully"));
    }

    @GetMapping("/overview")
    @Operation(summary = "Budget overview", description = "Retrieves an aggregated utilization and progress summary across active budgets")
    public ResponseEntity<ApiResponse<BudgetOverviewResponse>> getBudgetOverview(
            @AuthenticationPrincipal UserPrincipal principal) {
        BudgetOverviewResponse response = budgetService.getBudgetOverview(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Budget overview retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get budget by ID", description = "Retrieves a specific budget by its identifier")
    public ResponseEntity<ApiResponse<BudgetResponse>> getBudgetById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        BudgetResponse response = budgetService.getBudgetById(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(response, "Budget retrieved successfully"));
    }

    @GetMapping("/{id}/progress")
    @Operation(summary = "Get budget progress", description = "Calculates actual spend, utilization percentage, and alert status for a budget")
    public ResponseEntity<ApiResponse<BudgetProgressResponse>> getBudgetProgress(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        BudgetProgressResponse response = budgetService.getBudgetProgress(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(response, "Budget progress calculated successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update budget", description = "Updates budget details such as name, amount, threshold, and active status")
    public ResponseEntity<ApiResponse<BudgetResponse>> updateBudget(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBudgetRequest request) {
        BudgetResponse response = budgetService.updateBudget(principal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Budget updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete budget", description = "Permanently removes a budget")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        budgetService.deleteBudget(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(null, "Budget deleted successfully"));
    }

    @GetMapping("/alerts")
    @Operation(summary = "List budget alerts", description = "Retrieves threshold warning alerts generated for the user's budgets")
    public ResponseEntity<ApiResponse<List<BudgetAlertResponse>>> getBudgetAlerts(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Boolean unreadOnly) {
        List<BudgetAlertResponse> response = budgetService.getBudgetAlerts(principal.getId(), unreadOnly);
        return ResponseEntity.ok(ApiResponse.success(response, "Budget alerts retrieved successfully"));
    }

    @PatchMapping("/alerts/{alertId}/read")
    @Operation(summary = "Mark alert as read", description = "Marks a specific budget alert as read")
    public ResponseEntity<ApiResponse<Void>> markAlertAsRead(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID alertId) {
        budgetService.markAlertAsRead(principal.getId(), alertId);
        return ResponseEntity.ok(ApiResponse.success(null, "Alert marked as read"));
    }
}
