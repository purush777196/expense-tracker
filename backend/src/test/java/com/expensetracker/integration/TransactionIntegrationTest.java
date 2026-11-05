package com.expensetracker.integration;

import com.expensetracker.auth.dto.RegisterRequest;
import com.expensetracker.category.entity.Category;
import com.expensetracker.category.entity.CategoryType;
import com.expensetracker.category.repository.CategoryRepository;
import com.expensetracker.transaction.dto.CreateTransactionRequest;
import com.expensetracker.transaction.dto.UpdateTransactionRequest;
import com.expensetracker.transaction.entity.PaymentMethod;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransactionIntegrationTest {

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

    private String userAToken;
    private String userBToken;
    private UUID expenseCatId;
    private UUID incomeCatId;

    @BeforeEach
    void setUp() throws Exception {
        transactionRepository.deleteAll();
        categoryRepository.deleteByIsSystemDefaultFalse();
        userRepository.deleteAll();

        // Register User A
        RegisterRequest userAReq = new RegisterRequest("Alice Finance", "alice@example.com", "Password123!", "USD");
        MvcResult resA = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userAReq)))
                .andExpect(status().isCreated())
                .andReturn();
        userAToken = objectMapper.readTree(resA.getResponse().getContentAsString()).path("data").path("accessToken").asText();

        // Register User B
        RegisterRequest userBReq = new RegisterRequest("Bob Saver", "bob@example.com", "Password123!", "USD");
        MvcResult resB = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userBReq)))
                .andExpect(status().isCreated())
                .andReturn();
        userBToken = objectMapper.readTree(resB.getResponse().getContentAsString()).path("data").path("accessToken").asText();

        // Get system categories
        Category groceries = categoryRepository.findByIsSystemDefaultTrue().stream()
                .filter(c -> c.getName().equals("Groceries") && c.getType() == CategoryType.EXPENSE)
                .findFirst()
                .orElseThrow();
        expenseCatId = groceries.getId();

        Category salary = categoryRepository.findByIsSystemDefaultTrue().stream()
                .filter(c -> c.getName().equals("Salary") && c.getType() == CategoryType.INCOME)
                .findFirst()
                .orElseThrow();
        incomeCatId = salary.getId();
    }

    @Test
    @DisplayName("Should create transaction and verify positive amount and category type matching")
    void shouldCreateTransactionsWithValidation() throws Exception {
        // Valid Expense
        CreateTransactionRequest validExpense = new CreateTransactionRequest(
                new BigDecimal("84.50"),
                CategoryType.EXPENSE,
                expenseCatId,
                Instant.now(),
                "Weekly organic groceries",
                "Whole Foods Market",
                PaymentMethod.CARD,
                "Fresh fruits & veggies",
                false,
                false
        );

        mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validExpense)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.amount").value(84.50))
                .andExpect(jsonPath("$.data.type").value("EXPENSE"))
                .andExpect(jsonPath("$.data.description").value("Weekly organic groceries"));

        // Reject non-positive amount
        CreateTransactionRequest zeroAmount = new CreateTransactionRequest(
                new BigDecimal("0.00"),
                CategoryType.EXPENSE,
                expenseCatId,
                Instant.now(),
                "Invalid zero amount",
                null,
                PaymentMethod.CASH,
                null,
                false,
                false
        );

        mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zeroAmount)))
                .andExpect(status().isBadRequest());

        // Reject category type mismatch: creating INCOME with EXPENSE category
        CreateTransactionRequest typeMismatch = new CreateTransactionRequest(
                new BigDecimal("1500.00"),
                CategoryType.INCOME,
                expenseCatId, // Groceries is EXPENSE!
                Instant.now(),
                "Bonus payout",
                null,
                PaymentMethod.BANK_TRANSFER,
                null,
                false,
                false
        );

        mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(typeMismatch)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("cannot be used for INCOME transactions")));
    }

    @Test
    @DisplayName("Should enforce strict cross-user data isolation")
    void shouldEnforceCrossUserDataIsolation() throws Exception {
        // Alice creates a transaction
        CreateTransactionRequest aliceTx = new CreateTransactionRequest(
                new BigDecimal("250.00"),
                CategoryType.EXPENSE,
                expenseCatId,
                Instant.now(),
                "Alice Private Dinner",
                "Luxury Bistro",
                PaymentMethod.CARD,
                "Confidential",
                false,
                false
        );

        MvcResult result = mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(aliceTx)))
                .andExpect(status().isCreated())
                .andReturn();

        String aliceTxId = objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asText();

        // Bob tries to GET Alice's transaction -> 404 Not Found
        mockMvc.perform(get("/api/v1/transactions/" + aliceTxId)
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isNotFound());

        // Bob tries to PUT Alice's transaction -> 404 Not Found
        UpdateTransactionRequest updateAttempt = new UpdateTransactionRequest(
                new BigDecimal("999.00"),
                CategoryType.EXPENSE,
                expenseCatId,
                Instant.now(),
                "Hacked description",
                null,
                PaymentMethod.CASH,
                null,
                false,
                false
        );
        mockMvc.perform(put("/api/v1/transactions/" + aliceTxId)
                        .header("Authorization", "Bearer " + userBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateAttempt)))
                .andExpect(status().isNotFound());

        // Bob tries to DELETE Alice's transaction -> 404 Not Found
        mockMvc.perform(delete("/api/v1/transactions/" + aliceTxId)
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isNotFound());

        // Bob's transactions list should be empty (0 items)
        mockMvc.perform(get("/api/v1/transactions")
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(0)));
    }

    @Test
    @DisplayName("Should test pagination, search, and type filtering")
    void shouldSupportPaginationAndFiltering() throws Exception {
        // Create 3 expenses and 1 income for Alice
        createTxHelper("Supermarket Run 1", new BigDecimal("50.00"), CategoryType.EXPENSE, expenseCatId);
        createTxHelper("Supermarket Run 2", new BigDecimal("75.00"), CategoryType.EXPENSE, expenseCatId);
        createTxHelper("Coffee Shop", new BigDecimal("12.50"), CategoryType.EXPENSE, expenseCatId);
        createTxHelper("Consulting Paycheck", new BigDecimal("3000.00"), CategoryType.INCOME, incomeCatId);

        // Filter by type: EXPENSE
        mockMvc.perform(get("/api/v1/transactions?type=EXPENSE")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.content", hasSize(3)));

        // Filter by search query: "Supermarket"
        mockMvc.perform(get("/api/v1/transactions?query=supermarket")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2));

        // Test pagination: page=0, size=2
        mockMvc.perform(get("/api/v1/transactions?size=2&page=0")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pageSize").value(2))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.isLast").value(false));
    }

    @Test
    @DisplayName("Should verify dashboard financial summary metrics")
    void shouldVerifyDashboardSummaryMetrics() throws Exception {
        createTxHelper("Client Fee", new BigDecimal("5000.00"), CategoryType.INCOME, incomeCatId);
        createTxHelper("Rent Payment", new BigDecimal("1500.00"), CategoryType.EXPENSE, expenseCatId);
        createTxHelper("Groceries Supplies", new BigDecimal("500.00"), CategoryType.EXPENSE, expenseCatId);

        // Income = 5000, Expense = 2000, Net Balance = 3000, Savings Rate = 60.00%
        mockMvc.perform(get("/api/v1/dashboard/summary")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalIncome").value(5000.00))
                .andExpect(jsonPath("$.data.totalExpense").value(2000.00))
                .andExpect(jsonPath("$.data.netBalance").value(3000.00))
                .andExpect(jsonPath("$.data.savingsRatePercent").value(60.00))
                .andExpect(jsonPath("$.data.transactionCount").value(3));

        // Category breakdown
        mockMvc.perform(get("/api/v1/dashboard/category-breakdown")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].amount").value(2000.00))
                .andExpect(jsonPath("$.data[0].percentage").value(100.00));

        // Spending trends
        mockMvc.perform(get("/api/v1/dashboard/spending-trend?months=3")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(3)));

        // Recent transactions
        mockMvc.perform(get("/api/v1/dashboard/recent-transactions?limit=2")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    private void createTxHelper(String desc, BigDecimal amount, CategoryType type, UUID catId) throws Exception {
        CreateTransactionRequest req = new CreateTransactionRequest(
                amount, type, catId, Instant.now(), desc, "Merchant", PaymentMethod.CARD, null, false, false
        );
        mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }
}
