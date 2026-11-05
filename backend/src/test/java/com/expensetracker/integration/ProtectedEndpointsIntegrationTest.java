package com.expensetracker.integration;

import com.expensetracker.auth.dto.RegisterRequest;
import com.expensetracker.user.dto.ChangePasswordRequest;
import com.expensetracker.user.dto.UpdateProfileRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProtectedEndpointsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private String validAccessToken;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();

        RegisterRequest registerReq = new RegisterRequest("Grace Hopper", "grace@example.com", "P@ssword123", "USD");
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode responseNode = objectMapper.readTree(result.getResponse().getContentAsString());
        validAccessToken = responseNode.get("data").get("accessToken").asText();
    }

    @Test
    @DisplayName("GET /api/v1/users/me without Authorization header should return 401 Unauthorized")
    void getMeWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("GET /api/v1/users/me with valid JWT token should return 200 OK and user profile")
    void getMeWithValidTokenReturns200() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + validAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("grace@example.com"))
                .andExpect(jsonPath("$.data.fullName").value("Grace Hopper"))
                .andExpect(jsonPath("$.data.currency").value("USD"))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/v1/auth/me with valid JWT token should return 200 OK and user profile")
    void getAuthMeWithValidTokenReturns200() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + validAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("grace@example.com"))
                .andExpect(jsonPath("$.data.fullName").value("Grace Hopper"));
    }

    @Test
    @DisplayName("GET /api/v1/users/me with tampered token should return 401 Unauthorized")
    void getMeWithTamperedTokenReturns401() throws Exception {
        String tamperedToken = validAccessToken.substring(0, validAccessToken.length() - 5) + "ABCDE";

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + tamperedToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("PUT /api/v1/users/me with valid token should update profile details")
    void updateProfileSuccessfully() throws Exception {
        UpdateProfileRequest updateReq = new UpdateProfileRequest("Grace Hopper PhD", "EUR");

        mockMvc.perform(put("/api/v1/users/me")
                        .header("Authorization", "Bearer " + validAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Grace Hopper PhD"))
                .andExpect(jsonPath("$.data.currency").value("EUR"));
    }

    @Test
    @DisplayName("PUT /api/v1/users/me/password should change password when current password matches")
    void changePasswordSuccessfully() throws Exception {
        ChangePasswordRequest passwordReq = new ChangePasswordRequest("P@ssword123", "NewSecurePassword456#");

        mockMvc.perform(put("/api/v1/users/me/password")
                        .header("Authorization", "Bearer " + validAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PUT /api/v1/users/me/password should return 400 Bad Request when current password is incorrect")
    void changePasswordWithIncorrectCurrentPassword() throws Exception {
        ChangePasswordRequest passwordReq = new ChangePasswordRequest("WrongP@ssword123", "NewSecurePassword456#");

        mockMvc.perform(put("/api/v1/users/me/password")
                        .header("Authorization", "Bearer " + validAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Current password provided does not match"));
    }
}
