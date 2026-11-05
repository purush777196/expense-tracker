package com.expensetracker.transaction.controller;

import com.expensetracker.category.entity.CategoryType;
import com.expensetracker.common.response.ApiResponse;
import com.expensetracker.common.response.PagedResponse;
import com.expensetracker.security.UserPrincipal;
import com.expensetracker.transaction.dto.CreateTransactionRequest;
import com.expensetracker.transaction.dto.TransactionResponse;
import com.expensetracker.transaction.dto.TransactionSummaryResponse;
import com.expensetracker.transaction.dto.UpdateTransactionRequest;
import com.expensetracker.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Transaction Management", description = "Endpoints for creating, managing, filtering, and summarizing personal transactions")
public class TransactionController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("transactionDate", "amount", "createdAt", "description");

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    @Operation(summary = "Create transaction", description = "Adds a new income or expense transaction for the authenticated user")
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateTransactionRequest request) {
        TransactionResponse response = transactionService.createTransaction(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Transaction created successfully"));
    }

    @GetMapping
    @Operation(summary = "List transactions", description = "Returns a paginated list of user transactions with multi-criteria filtering")
    public ResponseEntity<ApiResponse<PagedResponse<TransactionResponse>>> getTransactions(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(required = false) CategoryType type,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        String validatedSortBy = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "transactionDate";
        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(direction, validatedSortBy));

        PagedResponse<TransactionResponse> response = transactionService.getTransactions(
                principal.getId(), startDate, endDate, type, categoryId, minAmount, maxAmount, query, pageable
        );

        return ResponseEntity.ok(ApiResponse.success(response, "Transactions retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID", description = "Retrieves details of a specific transaction owned by the authenticated user")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        TransactionResponse response = transactionService.getTransactionById(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(response, "Transaction retrieved successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update transaction", description = "Updates details of an existing transaction owned by the authenticated user")
    public ResponseEntity<ApiResponse<TransactionResponse>> updateTransaction(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTransactionRequest request) {
        TransactionResponse response = transactionService.updateTransaction(principal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Transaction updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete transaction", description = "Deletes a specific transaction owned by the authenticated user")
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        transactionService.deleteTransaction(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(null, "Transaction deleted successfully"));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get transaction summary", description = "Retrieves total income, total expense, net balance, and savings rate")
    public ResponseEntity<ApiResponse<TransactionSummaryResponse>> getTransactionSummary(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {
        TransactionSummaryResponse summary = transactionService.getTransactionSummary(principal.getId(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(summary, "Transaction summary retrieved successfully"));
    }
}
