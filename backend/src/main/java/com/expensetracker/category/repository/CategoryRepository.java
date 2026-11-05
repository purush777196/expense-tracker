package com.expensetracker.category.repository;

import com.expensetracker.category.entity.Category;
import com.expensetracker.category.entity.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    @Query("SELECT c FROM Category c WHERE (c.user.id = :userId OR c.isSystemDefault = true) ORDER BY c.name ASC")
    List<Category> findAllAvailableForUser(@Param("userId") UUID userId);

    @Query("SELECT c FROM Category c WHERE (c.user.id = :userId OR c.isSystemDefault = true) AND c.type = :type ORDER BY c.name ASC")
    List<Category> findAllAvailableForUserAndType(@Param("userId") UUID userId, @Param("type") CategoryType type);

    @Query("SELECT c FROM Category c WHERE c.id = :id AND (c.user.id = :userId OR c.isSystemDefault = true)")
    Optional<Category> findAvailableById(@Param("id") UUID id, @Param("userId") UUID userId);

    List<Category> findByIsSystemDefaultTrue();

    List<Category> findByUserId(UUID userId);

    Optional<Category> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndNameIgnoreCaseAndType(UUID userId, String name, CategoryType type);

    boolean existsByIsSystemDefaultTrueAndNameIgnoreCaseAndType(String name, CategoryType type);

    @Modifying
    @Transactional
    @Query("DELETE FROM Category c WHERE c.isSystemDefault = false")
    void deleteByIsSystemDefaultFalse();
}
