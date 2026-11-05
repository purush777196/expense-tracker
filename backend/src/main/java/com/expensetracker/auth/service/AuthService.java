package com.expensetracker.auth.service;

import com.expensetracker.auth.dto.AuthResponse;
import com.expensetracker.auth.dto.LoginRequest;
import com.expensetracker.auth.dto.RefreshTokenRequest;
import com.expensetracker.auth.dto.RegisterRequest;
import com.expensetracker.common.exception.DuplicateResourceException;
import com.expensetracker.common.exception.InvalidTokenException;
import com.expensetracker.security.JwtService;
import com.expensetracker.user.dto.UserProfileResponse;
import com.expensetracker.user.entity.User;
import com.expensetracker.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("An account with email '" + normalizedEmail + "' already exists");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(
                normalizedEmail,
                encodedPassword,
                request.getFullName().trim(),
                request.getCurrency()
        );

        User savedUser = userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                savedUser.getFullName()
        );
        String refreshToken = jwtService.generateRefreshToken(savedUser.getId(), savedUser.getEmail());

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.getAccessTokenExpirationSeconds(),
                UserProfileResponse.fromEntity(savedUser)
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
        );

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!user.isActive()) {
            throw new BadCredentialsException("Account is deactivated. Please contact support.");
        }

        String accessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getFullName()
        );
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.getAccessTokenExpirationSeconds(),
                UserProfileResponse.fromEntity(user)
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String token = request.getRefreshToken();

        if (!jwtService.validateToken(token)) {
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        String tokenType = jwtService.extractTokenType(token);
        if (!"REFRESH".equals(tokenType)) {
            throw new InvalidTokenException("Supplied token is not a refresh token");
        }

        String email = jwtService.extractUsername(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidTokenException("User associated with refresh token no longer exists"));

        if (!user.isActive()) {
            throw new InvalidTokenException("Account is deactivated");
        }

        String newAccessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getFullName()
        );

        return new AuthResponse(
                newAccessToken,
                token, // keep existing refresh token or rotate
                "Bearer",
                jwtService.getAccessTokenExpirationSeconds(),
                UserProfileResponse.fromEntity(user)
        );
    }
}
