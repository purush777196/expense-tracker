package com.expensetracker.goal.dto;

import com.expensetracker.goal.entity.ContributionType;
import com.expensetracker.goal.entity.GoalContribution;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record GoalContributionResponse(
        UUID id,
        UUID goalId,
        BigDecimal amount,
        ContributionType type,
        String notes,
        Instant createdAt
) {
    public static GoalContributionResponse fromEntity(GoalContribution gc) {
        if (gc == null) return null;
        return new GoalContributionResponse(
                gc.getId(),
                gc.getGoal().getId(),
                gc.getAmount(),
                gc.getType(),
                gc.getNotes(),
                gc.getCreatedAt()
        );
    }
}
