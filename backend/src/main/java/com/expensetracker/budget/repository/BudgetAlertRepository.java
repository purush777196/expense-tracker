package com.expensetracker.budget.repository;

import com.expensetracker.budget.entity.BudgetAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BudgetAlertRepository extends JpaRepository<BudgetAlert, UUID> {

    List<BudgetAlert> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<BudgetAlert> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(UUID userId);

    List<BudgetAlert> findByBudgetIdOrderByThresholdPercentageAsc(UUID budgetId);

    boolean existsByBudgetIdAndThresholdPercentage(UUID budgetId, BigDecimal thresholdPercentage);

    Optional<BudgetAlert> findByIdAndUserId(UUID id, UUID userId);
}
