package com.expensetracker.category.service;

import com.expensetracker.category.dto.CategoryRequest;
import com.expensetracker.category.dto.CategoryResponse;
import com.expensetracker.category.entity.Category;
import com.expensetracker.category.entity.CategoryType;
import com.expensetracker.category.repository.CategoryRepository;
import com.expensetracker.common.exception.BadRequestException;
import com.expensetracker.common.exception.DuplicateResourceException;
import com.expensetracker.common.exception.ResourceNotFoundException;
import com.expensetracker.transaction.repository.TransactionRepository;
import com.expensetracker.user.entity.User;
import com.expensetracker.user.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository,
                               UserRepository userRepository,
                               TransactionRepository transactionRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAvailableCategories(UUID userId, CategoryType type) {
        List<Category> categories;
        if (type != null) {
            categories = categoryRepository.findAllAvailableForUserAndType(userId, type);
        } else {
            categories = categoryRepository.findAllAvailableForUser(userId);
        }
        return categories.stream().map(CategoryResponse::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(UUID categoryId, UUID userId) {
        Category category = getCategoryEntityForUser(categoryId, userId);
        return CategoryResponse.fromEntity(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Category getCategoryEntityForUser(UUID categoryId, UUID userId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        if (!category.isSystemDefault() && (category.getUser() == null || !category.getUser().getId().equals(userId))) {
            throw new AccessDeniedException("You do not have permission to access this category");
        }
        return category;
    }

    @Override
    public CategoryResponse createCustomCategory(UUID userId, CategoryRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        String trimmedName = request.name().trim();

        // Check if duplicate exists for user or in system defaults
        if (categoryRepository.existsByUserIdAndNameIgnoreCaseAndType(userId, trimmedName, request.type()) ||
            categoryRepository.existsByIsSystemDefaultTrueAndNameIgnoreCaseAndType(trimmedName, request.type())) {
            throw new DuplicateResourceException(
                    "A category named '" + trimmedName + "' with type '" + request.type() + "' already exists");
        }

        Category category = new Category(
                user,
                trimmedName,
                request.type(),
                request.colorHex(),
                request.iconName(),
                false
        );

        Category saved = categoryRepository.save(category);
        return CategoryResponse.fromEntity(saved);
    }

    @Override
    public CategoryResponse updateCustomCategory(UUID userId, UUID categoryId, CategoryRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        if (category.isSystemDefault()) {
            throw new BadRequestException("System default categories cannot be modified");
        }

        if (category.getUser() == null || !category.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to modify this category");
        }

        String trimmedName = request.name().trim();

        // If name or type is changing, check for collision
        if (!category.getName().equalsIgnoreCase(trimmedName) || category.getType() != request.type()) {
            if (categoryRepository.existsByUserIdAndNameIgnoreCaseAndType(userId, trimmedName, request.type()) ||
                categoryRepository.existsByIsSystemDefaultTrueAndNameIgnoreCaseAndType(trimmedName, request.type())) {
                throw new DuplicateResourceException(
                        "A category named '" + trimmedName + "' with type '" + request.type() + "' already exists");
            }
        }

        category.setName(trimmedName);
        category.setType(request.type());
        if (request.colorHex() != null && !request.colorHex().isBlank()) {
            category.setColorHex(request.colorHex());
        }
        if (request.iconName() != null && !request.iconName().isBlank()) {
            category.setIconName(request.iconName());
        }

        Category updated = categoryRepository.save(category);
        return CategoryResponse.fromEntity(updated);
    }

    @Override
    public void deleteCustomCategory(UUID userId, UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        if (category.isSystemDefault()) {
            throw new BadRequestException("System default categories cannot be deleted");
        }

        if (category.getUser() == null || !category.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to delete this category");
        }

        // Prevent deletion if transactions reference this category
        if (transactionRepository.existsByCategoryId(categoryId)) {
            throw new BadRequestException(
                    "Cannot delete category '" + category.getName() + "' because it is linked to existing transactions");
        }

        categoryRepository.delete(category);
    }
}
