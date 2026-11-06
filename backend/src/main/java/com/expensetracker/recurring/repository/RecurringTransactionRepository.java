package com.expensetracker.recurring.repository;

import com.expensetracker.recurring.entity.RecurringStatus;
import com.expensetracker.recurring.entity.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, UUID> {

    Optional<RecurringTransaction> findByIdAndUserId(UUID id, UUID userId);

    List<RecurringTransaction> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<RecurringTransaction> findByUserIdAndStatusOrderByNextOccurrenceDateAsc(UUID userId, RecurringStatus status);

    @Query("SELECT r FROM RecurringTransaction r WHERE r.status = 'ACTIVE' AND r.nextOccurrenceDate <= :today")
    List<RecurringTransaction> findDueRecurringTransactions(@Param("today") LocalDate today);

    @Query("SELECT r FROM RecurringTransaction r WHERE r.user.id = :userId AND r.status = 'ACTIVE' " +
           "AND r.nextOccurrenceDate BETWEEN :startDate AND :endDate " +
           "ORDER BY r.nextOccurrenceDate ASC")
    List<RecurringTransaction> findUpcomingByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
