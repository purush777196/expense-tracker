package com.expensetracker.integration;

import com.expensetracker.auth.dto.RegisterRequest;
import com.expensetracker.category.dto.CategoryRequest;
import com.expensetracker.category.entity.CategoryType;
import com.expensetracker.category.repository.CategoryRepository;
import com.expensetracker.transaction.repository.TransactionRepository;
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

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CategoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        transactionRepository.deleteAll();
        categoryRepository.deleteByIsSystemDefaultFalse();
        userRepository.deleteAll();

        RegisterRequest registerReq = new RegisterRequest("Category Tester", "cattester@example.com", "Password123!", "USD");
        MvcResult res = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode root = objectMapper.readTree(res.getResponse().getContentAsString());
        authToken = root.path("data").path("accessToken").asText();
    }

    @Test
    @DisplayName("GET /api/v1/categories should return system categories for authenticated user")
    void shouldReturnCategories() throws Exception {
        mockMvc.perform(get("/api/v1/categories")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(10))))
                .andExpect(jsonPath("$.data[?(@.name == 'Groceries')].type").value("EXPENSE"))
                .andExpect(jsonPath("$.data[?(@.name == 'Salary')].type").value("INCOME"));
    }

    @Test
    @DisplayName("POST /api/v1/categories should create custom category and block duplicates")
    void shouldCreateCustomCategoryAndBlockDuplicates() throws Exception {
        CategoryRequest req = new CategoryRequest("Online Subscriptions", CategoryType.EXPENSE, "#8B5CF6", "Tv");

        MvcResult createRes = mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Online Subscriptions"))
                .andExpect(jsonPath("$.data.isSystemDefault").value(false))
                .andReturn();

        // Duplicate creation attempt should be rejected with 409 Conflict
        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT & DELETE /api/v1/categories/{id} should update and delete custom category")
    void shouldUpdateAndDeleteCustomCategory() throws Exception {
        CategoryRequest req = new CategoryRequest("Gym Membership", CategoryType.EXPENSE, "#EC4899", "Activity");

        MvcResult createRes = mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode node = objectMapper.readTree(createRes.getResponse().getContentAsString());
        String catId = node.path("data").path("id").asText();

        // Update
        CategoryRequest updateReq = new CategoryRequest("Fitness & Gym", CategoryType.EXPENSE, "#10B981", "Activity");
        mockMvc.perform(put("/api/v1/categories/" + catId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Fitness & Gym"))
                .andExpect(jsonPath("$.data.colorHex").value("#10B981"));

        // Delete
        mockMvc.perform(delete("/api/v1/categories/" + catId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());

        // Verify it is gone
        mockMvc.perform(get("/api/v1/categories/" + catId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} should reject deleting system categories")
    void shouldRejectDeletingSystemCategory() throws Exception {
        MvcResult listRes = mockMvc.perform(get("/api/v1/categories")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode categories = objectMapper.readTree(listRes.getResponse().getContentAsString()).path("data");
        String systemCatId = null;
        for (JsonNode cat : categories) {
            if (cat.path("isSystemDefault").asBoolean()) {
                systemCatId = cat.path("id").asText();
                break;
            }
        }

        mockMvc.perform(delete("/api/v1/categories/" + systemCatId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("System default categories cannot be deleted")));
    }
}
