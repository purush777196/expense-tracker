package com.expensetracker.category.controller;

import com.expensetracker.category.dto.CategoryRequest;
import com.expensetracker.category.dto.CategoryResponse;
import com.expensetracker.category.entity.CategoryType;
import com.expensetracker.category.service.CategoryService;
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
@RequestMapping("/api/v1/categories")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Category Management", description = "Endpoints for managing default and custom income/expense categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @Operation(summary = "List categories", description = "Retrieves all categories available to the user (system defaults + custom categories)")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) CategoryType type) {
        List<CategoryResponse> categories = categoryService.getAvailableCategories(principal.getId(), type);
        return ResponseEntity.ok(ApiResponse.success(categories, "Categories retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieves a specific category if it is a system default or owned by the user")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        CategoryResponse category = categoryService.getCategoryById(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(category, "Category retrieved successfully"));
    }

    @PostMapping
    @Operation(summary = "Create custom category", description = "Creates a new custom category scoped to the authenticated user")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse category = categoryService.createCustomCategory(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(category, "Category created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update custom category", description = "Updates a user's custom category")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse category = categoryService.updateCustomCategory(principal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success(category, "Category updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete custom category", description = "Deletes a user's custom category if not linked to any transactions")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        categoryService.deleteCustomCategory(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(null, "Category deleted successfully"));
    }
}
