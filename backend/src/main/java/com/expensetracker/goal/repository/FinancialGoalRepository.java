package com.expensetracker.goal.repository;

import com.expensetracker.goal.entity.FinancialGoal;
import com.expensetracker.goal.entity.GoalStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FinancialGoalRepository extends JpaRepository<FinancialGoal, UUID> {

    Optional<FinancialGoal> findByIdAndUserId(UUID id, UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM FinancialGoal g WHERE g.id = :id AND g.user.id = :userId")
    Optional<FinancialGoal> findWithLockByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    List<FinancialGoal> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<FinancialGoal> findByUserIdAndStatusOrderByTargetDateAsc(UUID userId, GoalStatus status);

    long countByUserIdAndStatus(UUID userId, GoalStatus status);
}
