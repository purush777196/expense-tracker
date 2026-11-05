package com.expensetracker.transaction.repository;

import com.expensetracker.category.entity.CategoryType;
import com.expensetracker.transaction.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);

    Page<Transaction> findByUserId(UUID userId, Pageable pageable);

    List<Transaction> findTop5ByUserIdOrderByTransactionDateDesc(UUID userId);

    boolean existsByCategoryId(UUID categoryId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = :type " +
           "AND t.transactionDate >= :startDate AND t.transactionDate < :endDate")
    BigDecimal sumAmountByUserIdAndTypeAndDateRange(
            @Param("userId") UUID userId,
            @Param("type") CategoryType type,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = :type")
    BigDecimal sumTotalAmountByUserIdAndType(
            @Param("userId") UUID userId,
            @Param("type") CategoryType type);

    @Query("SELECT COUNT(t) FROM Transaction t " +
           "WHERE t.user.id = :userId " +
           "AND t.transactionDate >= :startDate AND t.transactionDate < :endDate")
    long countByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    @Query("SELECT t.category.id AS categoryId, t.category.name AS categoryName, " +
           "t.category.colorHex AS colorHex, t.category.iconName AS iconName, " +
           "SUM(t.amount) AS totalAmount, COUNT(t) AS transactionCount " +
           "FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = 'EXPENSE' " +
           "AND t.transactionDate >= :startDate AND t.transactionDate < :endDate " +
           "GROUP BY t.category.id, t.category.name, t.category.colorHex, t.category.iconName " +
           "ORDER BY SUM(t.amount) DESC")
    List<CategorySpendingProjection> findCategorySpendingByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    @Query("SELECT t.category.id AS categoryId, t.category.name AS categoryName, " +
           "t.category.colorHex AS colorHex, t.category.iconName AS iconName, " +
           "SUM(t.amount) AS totalAmount, COUNT(t) AS transactionCount " +
           "FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = 'EXPENSE' " +
           "GROUP BY t.category.id, t.category.name, t.category.colorHex, t.category.iconName " +
           "ORDER BY SUM(t.amount) DESC")
    List<CategorySpendingProjection> findAllTimeCategorySpendingByUserId(@Param("userId") UUID userId);

    @Query("SELECT t FROM Transaction t " +
           "WHERE t.user.id = :userId " +
           "AND t.transactionDate >= :startDate AND t.transactionDate < :endDate " +
           "ORDER BY t.transactionDate ASC")
    List<Transaction> findByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);
}
