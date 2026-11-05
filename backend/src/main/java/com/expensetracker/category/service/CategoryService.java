package com.expensetracker.category.service;

import com.expensetracker.category.dto.CategoryRequest;
import com.expensetracker.category.dto.CategoryResponse;
import com.expensetracker.category.entity.Category;
import com.expensetracker.category.entity.CategoryType;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    List<CategoryResponse> getAvailableCategories(UUID userId, CategoryType type);

    CategoryResponse getCategoryById(UUID categoryId, UUID userId);

    Category getCategoryEntityForUser(UUID categoryId, UUID userId);

    CategoryResponse createCustomCategory(UUID userId, CategoryRequest request);

    CategoryResponse updateCustomCategory(UUID userId, UUID categoryId, CategoryRequest request);

    void deleteCustomCategory(UUID userId, UUID categoryId);
}
