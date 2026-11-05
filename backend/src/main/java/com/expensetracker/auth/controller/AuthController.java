package com.expensetracker.auth.controller;

import com.expensetracker.auth.dto.AuthResponse;
import com.expensetracker.auth.dto.LoginRequest;
import com.expensetracker.auth.dto.RefreshTokenRequest;
import com.expensetracker.auth.dto.RegisterRequest;
import com.expensetracker.auth.service.AuthService;
import com.expensetracker.common.response.ApiResponse;
import com.expensetracker.security.UserPrincipal;
import com.expensetracker.user.dto.UserProfileResponse;
import com.expensetracker.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration, authentication, token refresh, and profile retrieval")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user account", description = "Registers user, hashes password via BCrypt, and issues initial JWT access and refresh tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "User registered successfully"));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user credentials", description = "Validates user email and password, returning JWT access and refresh tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh expired access token", description = "Validates active refresh token and returns a newly minted JWT access token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Get currently authenticated user profile", description = "Retrieves profile of the token owner without exposing password hashes")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal principal) {
        UserProfileResponse profile = userService.getUserProfile(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(profile, "Profile retrieved successfully"));
    }
}
