package com.expensetracker.category.dto;

import com.expensetracker.category.entity.Category;
import com.expensetracker.category.entity.CategoryType;

import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        CategoryType type,
        String colorHex,
        String iconName,
        boolean isSystemDefault,
        Instant createdAt
) {
    public static CategoryResponse fromEntity(Category category) {
        if (category == null) return null;
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getType(),
                category.getColorHex(),
                category.getIconName(),
                category.isSystemDefault(),
                category.getCreatedAt()
        );
    }
}
