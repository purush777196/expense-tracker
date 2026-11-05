package com.expensetracker.category;

import com.expensetracker.category.dto.CategoryRequest;
import com.expensetracker.category.dto.CategoryResponse;
import com.expensetracker.category.entity.Category;
import com.expensetracker.category.entity.CategoryType;
import com.expensetracker.category.repository.CategoryRepository;
import com.expensetracker.category.service.CategoryServiceImpl;
import com.expensetracker.common.exception.BadRequestException;
import com.expensetracker.common.exception.DuplicateResourceException;
import com.expensetracker.common.exception.ResourceNotFoundException;
import com.expensetracker.transaction.repository.TransactionRepository;
import com.expensetracker.user.entity.User;
import com.expensetracker.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private UUID userId;
    private User testUser;
    private Category systemCategory;
    private Category userCategory;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = new User("user@example.com", "hash", "Test User", "USD");
        testUser.setId(userId);

        systemCategory = new Category(null, "Groceries", CategoryType.EXPENSE, "#10B981", "ShoppingCart", true);
        systemCategory.setId(UUID.randomUUID());

        userCategory = new Category(testUser, "Gaming", CategoryType.EXPENSE, "#6366F1", "Gamepad", false);
        userCategory.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Should return available categories for user")
    void shouldReturnAvailableCategories() {
        when(categoryRepository.findAllAvailableForUser(userId)).thenReturn(List.of(systemCategory, userCategory));

        List<CategoryResponse> result = categoryService.getAvailableCategories(userId, null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Groceries");
        assertThat(result.get(0).isSystemDefault()).isTrue();
        assertThat(result.get(1).name()).isEqualTo("Gaming");
        assertThat(result.get(1).isSystemDefault()).isFalse();
    }

    @Test
    @DisplayName("Should create custom category successfully")
    void shouldCreateCustomCategory() {
        CategoryRequest request = new CategoryRequest("Pet Care", CategoryType.EXPENSE, "#F59E0B", "Heart");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(categoryRepository.existsByUserIdAndNameIgnoreCaseAndType(userId, "Pet Care", CategoryType.EXPENSE)).thenReturn(false);
        when(categoryRepository.existsByIsSystemDefaultTrueAndNameIgnoreCaseAndType("Pet Care", CategoryType.EXPENSE)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category c = invocation.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        CategoryResponse response = categoryService.createCustomCategory(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Pet Care");
        assertThat(response.isSystemDefault()).isFalse();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Should reject creating duplicate category")
    void shouldRejectDuplicateCategory() {
        CategoryRequest request = new CategoryRequest("Gaming", CategoryType.EXPENSE, "#6366F1", "Gamepad");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(categoryRepository.existsByUserIdAndNameIgnoreCaseAndType(userId, "Gaming", CategoryType.EXPENSE)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCustomCategory(userId, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should prevent updating system default category")
    void shouldPreventUpdatingSystemCategory() {
        UUID sysCatId = systemCategory.getId();
        CategoryRequest request = new CategoryRequest("New Name", CategoryType.EXPENSE, "#000000", "Tag");

        when(categoryRepository.findById(sysCatId)).thenReturn(Optional.of(systemCategory));

        assertThatThrownBy(() -> categoryService.updateCustomCategory(userId, sysCatId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("System default categories cannot be modified");
    }

    @Test
    @DisplayName("Should prevent updating category owned by another user")
    void shouldPreventUpdatingAnotherUserCategory() {
        UUID otherUserId = UUID.randomUUID();
        User otherUser = new User("other@example.com", "hash", "Other User", "USD");
        otherUser.setId(otherUserId);
        userCategory.setUser(otherUser);

        when(categoryRepository.findById(userCategory.getId())).thenReturn(Optional.of(userCategory));

        CategoryRequest request = new CategoryRequest("Updated Name", CategoryType.EXPENSE, "#000000", "Tag");

        assertThatThrownBy(() -> categoryService.updateCustomCategory(userId, userCategory.getId(), request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("permission");
    }

    @Test
    @DisplayName("Should prevent deleting category referenced by transactions")
    void shouldPreventDeletingCategoryReferencedByTransactions() {
        when(categoryRepository.findById(userCategory.getId())).thenReturn(Optional.of(userCategory));
        when(transactionRepository.existsByCategoryId(userCategory.getId())).thenReturn(true);

        assertThatThrownBy(() -> categoryService.deleteCustomCategory(userId, userCategory.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("linked to existing transactions");

        verify(categoryRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should delete unreferenced custom category")
    void shouldDeleteUnreferencedCustomCategory() {
        when(categoryRepository.findById(userCategory.getId())).thenReturn(Optional.of(userCategory));
        when(transactionRepository.existsByCategoryId(userCategory.getId())).thenReturn(false);

        categoryService.deleteCustomCategory(userId, userCategory.getId());

        verify(categoryRepository).delete(userCategory);
    }
}
