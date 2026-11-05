package com.expensetracker.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String TEST_SECRET = "dGVzdC1zZWN1cmUtZXhwZW5zZS10cmFja2VyLWp3dC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLXB1cnBvc2Vz";
    private static final long ACCESS_EXPIRATION_MS = 3600000; // 1 hour
    private static final long REFRESH_EXPIRATION_MS = 86400000; // 24 hours

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, ACCESS_EXPIRATION_MS, REFRESH_EXPIRATION_MS);
    }

    @Test
    @DisplayName("Should reject short secret during initialization")
    void shouldRejectShortSecret() {
        assertThatThrownBy(() -> new JwtService("short-secret", ACCESS_EXPIRATION_MS, REFRESH_EXPIRATION_MS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 32 characters");
    }

    @Test
    @DisplayName("Should generate valid access token with correct claims")
    void shouldGenerateValidAccessToken() {
        UUID userId = UUID.randomUUID();
        String email = "testuser@example.com";
        String role = "ROLE_USER";
        String fullName = "Test User";

        String token = jwtService.generateAccessToken(userId, email, role, fullName);

        assertThat(token).isNotNull().isNotBlank();
        assertThat(jwtService.validateToken(token)).isTrue();
        assertThat(jwtService.extractUsername(token)).isEqualTo(email);
        assertThat(jwtService.extractUserId(token)).isEqualTo(userId);
        assertThat(jwtService.extractTokenType(token)).isEqualTo("ACCESS");
    }

    @Test
    @DisplayName("Should generate valid refresh token")
    void shouldGenerateValidRefreshToken() {
        UUID userId = UUID.randomUUID();
        String email = "testuser@example.com";

        String token = jwtService.generateRefreshToken(userId, email);

        assertThat(token).isNotNull().isNotBlank();
        assertThat(jwtService.validateToken(token)).isTrue();
        assertThat(jwtService.extractUsername(token)).isEqualTo(email);
        assertThat(jwtService.extractUserId(token)).isEqualTo(userId);
        assertThat(jwtService.extractTokenType(token)).isEqualTo("REFRESH");
    }

    @Test
    @DisplayName("Should return false for invalid or tampered token")
    void shouldReturnFalseForTamperedToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateAccessToken(userId, "user@example.com", "ROLE_USER", "User");
        String tamperedToken = token.substring(0, token.length() - 5) + "ABCDE";

        assertThat(jwtService.validateToken(tamperedToken)).isFalse();
    }

    @Test
    @DisplayName("Should return false for expired token")
    void shouldReturnFalseForExpiredToken() {
        // JwtService with 1 millisecond expiration
        JwtService shortLivedService = new JwtService(TEST_SECRET, 1, 1);
        UUID userId = UUID.randomUUID();
        String token = shortLivedService.generateAccessToken(userId, "user@example.com", "ROLE_USER", "User");

        try {
            Thread.sleep(10);
        } catch (InterruptedException ignored) {
        }

        assertThat(shortLivedService.validateToken(token)).isFalse();
    }
}
