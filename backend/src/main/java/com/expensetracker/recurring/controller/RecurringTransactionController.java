package com.expensetracker.recurring.controller;

import com.expensetracker.common.response.ApiResponse;
import com.expensetracker.recurring.dto.*;
import com.expensetracker.recurring.entity.RecurringStatus;
import com.expensetracker.recurring.service.RecurringTransactionService;
import com.expensetracker.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/recurring-transactions")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Recurring Transactions", description = "Endpoints for scheduling recurring expenses/income, pausing, resuming, and tracking upcoming payments")
public class RecurringTransactionController {

    private final RecurringTransactionService recurringService;

    public RecurringTransactionController(RecurringTransactionService recurringService) {
        this.recurringService = recurringService;
    }

    @PostMapping
    @Operation(summary = "Create recurring schedule", description = "Creates a new recurring transaction schedule")
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> createRecurringTransaction(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateRecurringTransactionRequest request) {
        RecurringTransactionResponse response = recurringService.createRecurringTransaction(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Recurring schedule created successfully"));
    }

    @GetMapping
    @Operation(summary = "List recurring schedules", description = "Retrieves all recurring schedules for the user, with optional status filter")
    public ResponseEntity<ApiResponse<List<RecurringTransactionResponse>>> getRecurringTransactions(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) RecurringStatus status) {
        List<RecurringTransactionResponse> responses = recurringService.getRecurringTransactions(principal.getId(), status);
        return ResponseEntity.ok(ApiResponse.success(responses, "Recurring schedules retrieved successfully"));
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming payments", description = "Forecasts upcoming payments and debits within the next specified days (default: 30)")
    public ResponseEntity<ApiResponse<List<UpcomingRecurringResponse>>> getUpcomingRecurring(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "30") int daysAhead) {
        List<UpcomingRecurringResponse> responses = recurringService.getUpcomingRecurring(principal.getId(), daysAhead);
        return ResponseEntity.ok(ApiResponse.success(responses, "Upcoming recurring payments retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get recurring schedule by ID", description = "Retrieves a specific recurring transaction schedule")
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> getRecurringTransactionById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        RecurringTransactionResponse response = recurringService.getRecurringTransactionById(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(response, "Recurring schedule retrieved successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update recurring schedule", description = "Updates frequency, amount, or parameters of an existing recurring schedule")
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> updateRecurringTransaction(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRecurringTransactionRequest request) {
        RecurringTransactionResponse response = recurringService.updateRecurringTransaction(principal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Recurring schedule updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete recurring schedule", description = "Permanently removes a recurring schedule")
    public ResponseEntity<ApiResponse<Void>> deleteRecurringTransaction(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        recurringService.deleteRecurringTransaction(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(null, "Recurring schedule deleted successfully"));
    }

    @PostMapping("/{id}/pause")
    @Operation(summary = "Pause recurring schedule", description = "Pauses automatic transaction generation for a schedule")
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> pauseRecurringTransaction(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        RecurringTransactionResponse response = recurringService.pauseRecurringTransaction(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(response, "Recurring schedule paused"));
    }

    @PostMapping("/{id}/resume")
    @Operation(summary = "Resume recurring schedule", description = "Resumes a paused recurring schedule")
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> resumeRecurringTransaction(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        RecurringTransactionResponse response = recurringService.resumeRecurringTransaction(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(response, "Recurring schedule resumed"));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel recurring schedule", description = "Cancels a recurring schedule permanently")
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> cancelRecurringTransaction(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        RecurringTransactionResponse response = recurringService.cancelRecurringTransaction(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(response, "Recurring schedule cancelled"));
    }

    @PostMapping("/process-due")
    @Operation(summary = "Process due recurring transactions", description = "Triggers immediate execution of due recurring transactions (for admin or testing)")
    public ResponseEntity<ApiResponse<Integer>> processDue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        int count = recurringService.processDueRecurringTransactions(asOfDate != null ? asOfDate : LocalDate.now());
        return ResponseEntity.ok(ApiResponse.success(count, "Processed " + count + " recurring transactions"));
    }
}
