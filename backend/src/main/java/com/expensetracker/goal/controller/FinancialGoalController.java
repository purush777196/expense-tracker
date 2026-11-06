package com.expensetracker.goal.controller;

import com.expensetracker.common.response.ApiResponse;
import com.expensetracker.goal.dto.*;
import com.expensetracker.goal.entity.GoalStatus;
import com.expensetracker.goal.service.FinancialGoalService;
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
@RequestMapping("/api/v1/goals")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Financial Goals", description = "Endpoints for creating savings goals, recording contributions and withdrawals, and tracking completion progress")
public class FinancialGoalController {

    private final FinancialGoalService goalService;

    public FinancialGoalController(FinancialGoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping
    @Operation(summary = "Create goal", description = "Creates a new savings goal with a target amount and deadline")
    public ResponseEntity<ApiResponse<FinancialGoalResponse>> createGoal(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateGoalRequest request) {
        FinancialGoalResponse response = goalService.createGoal(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Financial goal created successfully"));
    }

    @GetMapping
    @Operation(summary = "List goals", description = "Retrieves all goals for the user, with optional filtering by status")
    public ResponseEntity<ApiResponse<List<FinancialGoalResponse>>> getGoals(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) GoalStatus status) {
        List<FinancialGoalResponse> responses = goalService.getGoals(principal.getId(), status);
        return ResponseEntity.ok(ApiResponse.success(responses, "Financial goals retrieved successfully"));
    }

    @GetMapping("/summary")
    @Operation(summary = "Goal summary", description = "Retrieves aggregate progress metrics across all active and completed goals")
    public ResponseEntity<ApiResponse<GoalSummaryResponse>> getGoalSummary(
            @AuthenticationPrincipal UserPrincipal principal) {
        GoalSummaryResponse response = goalService.getGoalSummary(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Goal summary retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get goal by ID", description = "Retrieves specific goal details")
    public ResponseEntity<ApiResponse<FinancialGoalResponse>> getGoalById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        FinancialGoalResponse response = goalService.getGoalById(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(response, "Goal retrieved successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update goal", description = "Updates a goal's parameters or status")
    public ResponseEntity<ApiResponse<FinancialGoalResponse>> updateGoal(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateGoalRequest request) {
        FinancialGoalResponse response = goalService.updateGoal(principal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Goal updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete goal", description = "Permanently deletes a goal and its contribution audit log")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        goalService.deleteGoal(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(null, "Goal deleted successfully"));
    }

    @PostMapping("/{id}/contributions")
    @Operation(summary = "Record contribution or withdrawal", description = "Safely credits or debits money toward a goal, maintaining full audit history")
    public ResponseEntity<ApiResponse<FinancialGoalResponse>> recordContribution(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody GoalContributionRequest request) {
        FinancialGoalResponse response = goalService.recordContribution(principal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Goal contribution recorded successfully"));
    }

    @GetMapping("/{id}/contributions")
    @Operation(summary = "Get contribution history", description = "Retrieves the audit log of all contributions and withdrawals for a goal")
    public ResponseEntity<ApiResponse<List<GoalContributionResponse>>> getGoalContributions(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        List<GoalContributionResponse> responses = goalService.getGoalContributions(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(responses, "Contribution history retrieved successfully"));
    }
}
