package com.expensetracker.goal.repository;

import com.expensetracker.goal.entity.GoalContribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GoalContributionRepository extends JpaRepository<GoalContribution, UUID> {

    List<GoalContribution> findByGoalIdOrderByCreatedAtDesc(UUID goalId);

    List<GoalContribution> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
