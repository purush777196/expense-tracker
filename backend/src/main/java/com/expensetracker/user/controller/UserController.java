package com.expensetracker.user.controller;

import com.expensetracker.common.response.ApiResponse;
import com.expensetracker.security.UserPrincipal;
import com.expensetracker.user.dto.ChangePasswordRequest;
import com.expensetracker.user.dto.UpdateProfileRequest;
import com.expensetracker.user.dto.UserProfileResponse;
import com.expensetracker.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "User Management", description = "Endpoints for managing the authenticated user's account profile and credentials")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get user profile", description = "Retrieves the authenticated user's own profile using the validated JWT claims")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        UserProfileResponse profile = userService.getUserProfile(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(profile, "User profile retrieved successfully"));
    }

    @PutMapping("/me")
    @Operation(summary = "Update user profile", description = "Updates full name and/or currency preference for the authenticated user")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse profile = userService.updateProfile(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(profile, "Profile updated successfully"));
    }

    @PutMapping("/me/password")
    @Operation(summary = "Change password", description = "Allows the authenticated user to change their password by validating the current password first")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password updated successfully"));
    }
}
