package com.expensetracker.transaction.controller;

import com.expensetracker.common.response.ApiResponse;
import com.expensetracker.security.UserPrincipal;
import com.expensetracker.transaction.dto.CategorySpendingResponse;
import com.expensetracker.transaction.dto.SpendingTrendResponse;
import com.expensetracker.transaction.dto.TransactionResponse;
import com.expensetracker.transaction.dto.TransactionSummaryResponse;
import com.expensetracker.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Dashboard & Analytics", description = "Endpoints for aggregated financial KPIs, trends, and dashboard metrics")
public class DashboardController {

    private final TransactionService transactionService;

    public DashboardController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get monthly financial summary", description = "Retrieves income, expense, cashflow, and savings rate for a given month or the current month")
    public ResponseEntity<ApiResponse<TransactionSummaryResponse>> getDashboardSummary(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        YearMonth targetMonth = resolveYearMonth(year, month);
        Instant startDate = targetMonth.atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endDate = targetMonth.plusMonths(1).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        TransactionSummaryResponse summary = transactionService.getTransactionSummary(principal.getId(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(summary, "Dashboard summary retrieved successfully"));
    }

    @GetMapping("/category-breakdown")
    @Operation(summary = "Get category expense breakdown", description = "Retrieves categorized spending and percentage allocations for a given month")
    public ResponseEntity<ApiResponse<List<CategorySpendingResponse>>> getCategoryBreakdown(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        YearMonth targetMonth = resolveYearMonth(year, month);
        Instant startDate = targetMonth.atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endDate = targetMonth.plusMonths(1).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<CategorySpendingResponse> breakdown = transactionService.getCategorySpendingBreakdown(principal.getId(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(breakdown, "Category breakdown retrieved successfully"));
    }

    @GetMapping("/spending-trend")
    @Operation(summary = "Get spending trends", description = "Retrieves monthly income vs expense trends over the last N months")
    public ResponseEntity<ApiResponse<List<SpendingTrendResponse>>> getSpendingTrends(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "6") int months) {

        List<SpendingTrendResponse> trends = transactionService.getSpendingTrends(principal.getId(), months);
        return ResponseEntity.ok(ApiResponse.success(trends, "Spending trends retrieved successfully"));
    }

    @GetMapping("/recent-transactions")
    @Operation(summary = "Get recent transactions", description = "Retrieves the most recent transactions for the dashboard ledger")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getRecentTransactions(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "5") int limit) {

        List<TransactionResponse> recent = transactionService.getRecentTransactions(principal.getId(), limit);
        return ResponseEntity.ok(ApiResponse.success(recent, "Recent transactions retrieved successfully"));
    }

    private YearMonth resolveYearMonth(Integer year, Integer month) {
        if (year != null && month != null && month >= 1 && month <= 12) {
            return YearMonth.of(year, month);
        }
        return YearMonth.now(ZoneOffset.UTC);
    }
}
