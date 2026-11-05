package com.expensetracker.auth;

import com.expensetracker.auth.dto.AuthResponse;
import com.expensetracker.auth.dto.LoginRequest;
import com.expensetracker.auth.dto.RefreshTokenRequest;
import com.expensetracker.auth.dto.RegisterRequest;
import com.expensetracker.auth.service.AuthService;
import com.expensetracker.common.exception.DuplicateResourceException;
import com.expensetracker.common.exception.InvalidTokenException;
import com.expensetracker.security.JwtService;
import com.expensetracker.user.entity.Role;
import com.expensetracker.user.entity.User;
import com.expensetracker.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService, authenticationManager);
    }

    @Test
    @DisplayName("Should successfully register a new user and return tokens")
    void shouldRegisterNewUser() {
        RegisterRequest request = new RegisterRequest("Alice Smith", "alice@example.com", "SecurePass123#", "USD");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("SecurePass123#")).thenReturn("$2a$12$hashedPassword");

        User savedUser = new User("alice@example.com", "$2a$12$hashedPassword", "Alice Smith", "USD");
        savedUser.setId(UUID.randomUUID());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        when(jwtService.generateAccessToken(eq(savedUser.getId()), eq("alice@example.com"), eq("ROLE_USER"), eq("Alice Smith")))
                .thenReturn("mock-access-token");
        when(jwtService.generateRefreshToken(eq(savedUser.getId()), eq("alice@example.com")))
                .thenReturn("mock-refresh-token");
        when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(900L);

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mock-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("mock-refresh-token");
        assertThat(response.getUser().getEmail()).isEqualTo("alice@example.com");
        assertThat(response.getUser().getFullName()).isEqualTo("Alice Smith");

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email is already registered")
    void shouldRejectDuplicateEmail() {
        RegisterRequest request = new RegisterRequest("Alice Smith", "alice@example.com", "SecurePass123#", "USD");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should successfully login and return tokens")
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest("alice@example.com", "SecurePass123#");

        User user = new User("alice@example.com", "$2a$12$hashedPassword", "Alice Smith", "USD");
        user.setId(UUID.randomUUID());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(eq(user.getId()), eq("alice@example.com"), eq("ROLE_USER"), eq("Alice Smith")))
                .thenReturn("mock-access-token");
        when(jwtService.generateRefreshToken(eq(user.getId()), eq("alice@example.com")))
                .thenReturn("mock-refresh-token");
        when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(900L);

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mock-access-token");
        assertThat(response.getUser().getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    @DisplayName("Should throw BadCredentialsException when account is inactive")
    void shouldRejectLoginForInactiveAccount() {
        LoginRequest request = new LoginRequest("inactive@example.com", "SecurePass123#");

        User user = new User("inactive@example.com", "$2a$12$hashedPassword", "Inactive User", "USD");
        user.setId(UUID.randomUUID());
        user.setActive(false);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByEmail("inactive@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("deactivated");
    }

    @Test
    @DisplayName("Should refresh access token when valid refresh token is supplied")
    void shouldRefreshAccessToken() {
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");

        User user = new User("alice@example.com", "$2a$12$hashedPassword", "Alice Smith", "USD");
        user.setId(UUID.randomUUID());

        when(jwtService.validateToken("valid-refresh-token")).thenReturn(true);
        when(jwtService.extractTokenType("valid-refresh-token")).thenReturn("REFRESH");
        when(jwtService.extractUsername("valid-refresh-token")).thenReturn("alice@example.com");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(eq(user.getId()), eq("alice@example.com"), eq("ROLE_USER"), eq("Alice Smith")))
                .thenReturn("new-access-token");
        when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(900L);

        AuthResponse response = authService.refreshToken(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
    }

    @Test
    @DisplayName("Should throw InvalidTokenException when refresh token is invalid")
    void shouldRejectInvalidRefreshToken() {
        RefreshTokenRequest request = new RefreshTokenRequest("bad-token");

        when(jwtService.validateToken("bad-token")).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Invalid or expired");
    }
}
