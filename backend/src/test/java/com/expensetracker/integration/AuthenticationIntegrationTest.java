package com.expensetracker.integration;

import com.expensetracker.auth.dto.LoginRequest;
import com.expensetracker.auth.dto.RefreshTokenRequest;
import com.expensetracker.auth.dto.RegisterRequest;
import com.expensetracker.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/auth/register should register user, hash password, and return JWT tokens")
    void registerValidUserReturnsTokens() throws Exception {
        RegisterRequest request = new RegisterRequest("David Miller", "david@example.com", "SecurePass123#", "USD");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.user.email").value("david@example.com"))
                .andExpect(jsonPath("$.data.user.fullName").value("David Miller"))
                .andExpect(jsonPath("$.data.user.passwordHash").doesNotExist()); // Verify password hash is never exposed
    }

    @Test
    @DisplayName("POST /api/v1/auth/register should return 400 Bad Request when validation fails")
    void registerInvalidDataReturns400() throws Exception {
        // Missing name, invalid email, weak password
        RegisterRequest request = new RegisterRequest("", "not-an-email", "weak", "INVALID_CURRENCY");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register should return 409 Conflict when email is already registered")
    void registerDuplicateEmailReturns409() throws Exception {
        RegisterRequest request1 = new RegisterRequest("User One", "shared@example.com", "Password123#", "USD");
        RegisterRequest request2 = new RegisterRequest("User Two", "shared@example.com", "Password456#", "EUR");

        // First registration
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        // Duplicate registration
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("An account with email 'shared@example.com' already exists"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login should authenticate credentials and return JWT tokens")
    void loginWithValidCredentials() throws Exception {
        RegisterRequest registerReq = new RegisterRequest("Emma Watson", "emma@example.com", "Password123#", "USD");
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        LoginRequest loginReq = new LoginRequest("emma@example.com", "Password123#");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value("emma@example.com"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login should return 401 Unauthorized for incorrect password")
    void loginWithWrongPassword() throws Exception {
        RegisterRequest registerReq = new RegisterRequest("Emma Watson", "emma@example.com", "Password123#", "USD");
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        LoginRequest loginReq = new LoginRequest("emma@example.com", "WrongPassword123#");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh should issue new access token when given valid refresh token")
    void refreshAccessTokenSuccessfully() throws Exception {
        RegisterRequest registerReq = new RegisterRequest("Frank Ocean", "frank@example.com", "Password123#", "USD");
        MvcResult regResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode responseNode = objectMapper.readTree(regResult.getResponse().getContentAsString());
        String refreshToken = responseNode.get("data").get("refreshToken").asText();

        RefreshTokenRequest refreshReq = new RefreshTokenRequest(refreshToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh should return 401 Unauthorized for invalid refresh token")
    void refreshWithInvalidToken() throws Exception {
        RefreshTokenRequest refreshReq = new RefreshTokenRequest("invalid.jwt.token");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}
