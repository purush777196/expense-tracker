package com.expensetracker.budget.entity;

import com.expensetracker.user.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "budget_alerts")
public class BudgetAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id", nullable = false)
    private Budget budget;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "threshold_percentage", precision = 5, scale = 2, nullable = false)
    private BigDecimal thresholdPercentage;

    @Column(name = "current_spending", precision = 15, scale = 2, nullable = false)
    private BigDecimal currentSpending;

    @Column(name = "budget_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal budgetAmount;

    @Column(name = "alert_message", nullable = false, length = 255)
    private String alertMessage;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public BudgetAlert() {
    }

    public BudgetAlert(Budget budget, User user, BigDecimal thresholdPercentage,
                       BigDecimal currentSpending, BigDecimal budgetAmount, String alertMessage) {
        this.budget = budget;
        this.user = user;
        this.thresholdPercentage = thresholdPercentage;
        this.currentSpending = currentSpending;
        this.budgetAmount = budgetAmount;
        this.alertMessage = alertMessage;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Budget getBudget() {
        return budget;
    }

    public void setBudget(Budget budget) {
        this.budget = budget;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BigDecimal getThresholdPercentage() {
        return thresholdPercentage;
    }

    public void setThresholdPercentage(BigDecimal thresholdPercentage) {
        this.thresholdPercentage = thresholdPercentage;
    }

    public BigDecimal getCurrentSpending() {
        return currentSpending;
    }

    public void setCurrentSpending(BigDecimal currentSpending) {
        this.currentSpending = currentSpending;
    }

    public BigDecimal getBudgetAmount() {
        return budgetAmount;
    }

    public void setBudgetAmount(BigDecimal budgetAmount) {
        this.budgetAmount = budgetAmount;
    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
