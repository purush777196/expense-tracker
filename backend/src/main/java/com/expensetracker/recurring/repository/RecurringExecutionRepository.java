package com.expensetracker.recurring.repository;

import com.expensetracker.recurring.entity.RecurringExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface RecurringExecutionRepository extends JpaRepository<RecurringExecution, UUID> {

    boolean existsByRecurringTransactionIdAndScheduledDate(UUID recurringTransactionId, LocalDate scheduledDate);
}
