package com.expensetracker.user.dto;

import com.expensetracker.user.entity.User;

import java.time.Instant;
import java.util.UUID;

public class UserProfileResponse {

    private UUID id;
    private String fullName;
    private String email;
    private String currency;
    private String role;
    private boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;

    public UserProfileResponse() {
    }

    public UserProfileResponse(UUID id, String fullName, String email, String currency, String role, boolean isActive, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.currency = currency;
        this.role = role;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UserProfileResponse fromEntity(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getCurrency(),
                user.getRole().name(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
