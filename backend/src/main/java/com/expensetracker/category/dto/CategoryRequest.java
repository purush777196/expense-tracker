package com.expensetracker.category.dto;

import com.expensetracker.category.entity.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "Category name is required")
        @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
        String name,

        @NotNull(message = "Category type (INCOME or EXPENSE) is required")
        CategoryType type,

        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid 6-character hex code, e.g. #6366F1")
        String colorHex,

        @Size(max = 50, message = "Icon name cannot exceed 50 characters")
        String iconName
) {
}
