package com.expensetracker.category.repository;

import com.expensetracker.category.entity.Category;
import com.expensetracker.category.entity.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    @Query("SELECT c FROM Category c WHERE c.user.id = :userId OR c.isSystemDefault = true ORDER BY c.name ASC")
    List<Category> findAllAvailableForUser(@Param("userId") UUID userId);

    List<Category> findByIsSystemDefaultTrue();

    List<Category> findByUserId(UUID userId);

    Optional<Category> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndNameIgnoreCaseAndType(UUID userId, String name, CategoryType type);

    boolean existsByIsSystemDefaultTrueAndNameIgnoreCaseAndType(String name, CategoryType type);
}
