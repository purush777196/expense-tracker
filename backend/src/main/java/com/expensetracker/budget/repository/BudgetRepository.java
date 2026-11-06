package com.expensetracker.budget.repository;

import com.expensetracker.budget.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    Optional<Budget> findByIdAndUserId(UUID id, UUID userId);

    List<Budget> findByUserIdAndIsActiveTrueOrderByStartDateDesc(UUID userId);

    List<Budget> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId AND b.isActive = true " +
           "AND b.category.id = :categoryId " +
           "AND (:excludeId IS NULL OR b.id != :excludeId) " +
           "AND (b.startDate <= :endDate AND b.endDate >= :startDate)")
    List<Budget> findOverlappingCategoryBudgets(
            @Param("userId") UUID userId,
            @Param("categoryId") UUID categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludeId") UUID excludeId);

    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId AND b.isActive = true " +
           "AND b.category IS NULL " +
           "AND (:excludeId IS NULL OR b.id != :excludeId) " +
           "AND (b.startDate <= :endDate AND b.endDate >= :startDate)")
    List<Budget> findOverlappingOverallBudgets(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludeId") UUID excludeId);

    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId AND b.isActive = true " +
           "AND :currentDate BETWEEN b.startDate AND b.endDate")
    List<Budget> findCurrentActiveBudgets(
            @Param("userId") UUID userId,
            @Param("currentDate") LocalDate currentDate);
}
