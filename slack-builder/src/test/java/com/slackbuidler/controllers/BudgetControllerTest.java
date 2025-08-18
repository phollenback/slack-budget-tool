package com.slackbuidler.controllers;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.slackbuidler.models.BudgetCategoryModel;
import com.slackbuidler.models.BudgetModel;
import com.slackbuidler.models.BudgetSummaryDTO;
import com.slackbuidler.services.BudgetService;

@ExtendWith(MockitoExtension.class)
@DisplayName("BudgetController Tests")
class BudgetControllerTest {

    @Mock
    private BudgetService budgetService;

    @InjectMocks
    private BudgetController budgetController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(budgetController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Nested
    @DisplayName("GET /budgets/ - Get All Budgets")
    class GetBudgetsTests {

        @Test
        @DisplayName("should return all budgets for user successfully")
        void shouldReturnAllBudgetsForUserSuccessfully() throws Exception {
            // Given
            List<BudgetModel> budgets = Arrays.asList(
                createSampleBudget(1L, "Monthly Budget", 1000.0, 500.0),
                createSampleBudget(2L, "Vacation Fund", 2000.0, 0.0)
            );
            when(budgetService.getBudgetsByUserId(1)).thenReturn(budgets);

            // When & Then
            mockMvc.perform(get("/budgets/")
                    .param("userId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].name").value("Monthly Budget"))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[1].name").value("Vacation Fund"));

            verify(budgetService).getBudgetsByUserId(1);
        }

        @Test
        @DisplayName("should return bad request when userId is invalid")
        void shouldReturnBadRequestWhenUserIdIsInvalid() throws Exception {
            // Given
            when(budgetService.getBudgetsByUserId(-1))
                .thenThrow(new IllegalArgumentException("User ID must be positive"));

            // When & Then
            mockMvc.perform(get("/budgets/")
                    .param("userId", "-1"))
                    .andExpect(status().isBadRequest());

            verify(budgetService).getBudgetsByUserId(-1);
        }

        @Test
        @DisplayName("should return internal server error when service throws exception")
        void shouldReturnInternalServerErrorWhenServiceThrowsException() throws Exception {
            // Given
            when(budgetService.getBudgetsByUserId(1))
                .thenThrow(new RuntimeException("Database error"));

            // When & Then
            mockMvc.perform(get("/budgets/")
                    .param("userId", "1"))
                    .andExpect(status().isInternalServerError());

            verify(budgetService).getBudgetsByUserId(1);
        }

        @Test
        @DisplayName("should use default userId when not provided")
        void shouldUseDefaultUserIdWhenNotProvided() throws Exception {
            // Given
            List<BudgetModel> budgets = Arrays.asList(createSampleBudget(1L, "Default Budget", 1000.0, 0.0));
            when(budgetService.getBudgetsByUserId(1)).thenReturn(budgets);

            // When & Then
            mockMvc.perform(get("/budgets/"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());

            verify(budgetService).getBudgetsByUserId(1);
        }
    }

    @Nested
    @DisplayName("GET /budgets/{id} - Get Budget By ID")
    class GetBudgetByIdTests {

        @Test
        @DisplayName("should return budget when found")
        void shouldReturnBudgetWhenFound() throws Exception {
            // Given
            BudgetModel budget = createSampleBudget(1L, "Test Budget", 1000.0, 500.0);
            when(budgetService.getBudgetById(1L)).thenReturn(Optional.of(budget));

            // When & Then
            mockMvc.perform(get("/budgets/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Test Budget"));

            verify(budgetService).getBudgetById(1L);
        }

        @Test
        @DisplayName("should return not found when budget doesn't exist")
        void shouldReturnNotFoundWhenBudgetDoesntExist() throws Exception {
            // Given
            when(budgetService.getBudgetById(999L)).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(get("/budgets/999"))
                    .andExpect(status().isNotFound());

            verify(budgetService).getBudgetById(999L);
        }

        @Test
        @DisplayName("should return bad request when id is null")
        void shouldReturnBadRequestWhenIdIsNull() throws Exception {
            // When & Then
            mockMvc.perform(get("/budgets/null"))
                    .andExpect(status().isBadRequest());

            verify(budgetService, never()).getBudgetById(any());
        }

        @Test
        @DisplayName("should return bad request when id is negative")
        void shouldReturnBadRequestWhenIdIsNegative() throws Exception {
            // When & Then
            mockMvc.perform(get("/budgets/-1"))
                    .andExpect(status().isBadRequest());

            verify(budgetService, never()).getBudgetById(any());
        }
    }

    @Nested
    @DisplayName("POST /budgets/ - Create Budget")
    class CreateBudgetTests {

        @Test
        @DisplayName("should create budget successfully")
        void shouldCreateBudgetSuccessfully() throws Exception {
            // Given
            BudgetModel budgetToCreate = createSampleBudget(null, "New Budget", 1000.0, 0.0);
            BudgetModel createdBudget = createSampleBudget(1L, "New Budget", 1000.0, 0.0);
            when(budgetService.createBudget(any(BudgetModel.class))).thenReturn(createdBudget);

            // When & Then
            mockMvc.perform(post("/budgets/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(budgetToCreate)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("New Budget"));

            verify(budgetService).createBudget(any(BudgetModel.class));
        }

        @Test
        @DisplayName("should return bad request when budget is null")
        void shouldReturnBadRequestWhenBudgetIsNull() throws Exception {
            // When & Then
            mockMvc.perform(post("/budgets/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("null"))
                    .andExpect(status().isBadRequest());

            verify(budgetService, never()).createBudget(any());
        }

        @Test
        @DisplayName("should return bad request when service throws validation exception")
        void shouldReturnBadRequestWhenServiceThrowsValidationException() throws Exception {
            // Given
            BudgetModel invalidBudget = createSampleBudget(null, "", 1000.0, 0.0);
            when(budgetService.createBudget(any(BudgetModel.class)))
                .thenThrow(new IllegalArgumentException("Budget name cannot be empty"));

            // When & Then
            mockMvc.perform(post("/budgets/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidBudget)))
                    .andExpect(status().isBadRequest());

            verify(budgetService).createBudget(any(BudgetModel.class));
        }
    }

    @Nested
    @DisplayName("PUT /budgets/{id} - Update Budget")
    class UpdateBudgetTests {

        @Test
        @DisplayName("should update budget successfully")
        void shouldUpdateBudgetSuccessfully() throws Exception {
            // Given
            BudgetModel budgetToUpdate = createSampleBudget(1L, "Updated Budget", 1500.0, 500.0);
            when(budgetService.updateBudget(1L, budgetToUpdate)).thenReturn(budgetToUpdate);

            // When
            ResponseEntity<BudgetModel> response = budgetController.updateBudget(1L, budgetToUpdate);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getName()).isEqualTo("Updated Budget");

            verify(budgetService).updateBudget(1L, budgetToUpdate);
        }

        @Test
        @DisplayName("should return bad request when id is invalid")
        void shouldReturnBadRequestWhenIdIsInvalid() throws Exception {
            // Given
            BudgetModel budgetToUpdate = createSampleBudget(1L, "Updated Budget", 1500.0, 500.0);

            // When & Then
            mockMvc.perform(put("/budgets/-1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(budgetToUpdate)))
                    .andExpect(status().isBadRequest());

            verify(budgetService, never()).updateBudget(any(), any());
        }

        @Test
        @DisplayName("should return bad request when budget is null")
        void shouldReturnBadRequestWhenBudgetIsNull() throws Exception {
            // When & Then
            mockMvc.perform(put("/budgets/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("null"))
                    .andExpect(status().isBadRequest());

            verify(budgetService, never()).updateBudget(any(), any());
        }
    }

    @Nested
    @DisplayName("DELETE /budgets/{id} - Delete Budget")
    class DeleteBudgetTests {

        @Test
        @DisplayName("should delete budget successfully")
        void shouldDeleteBudgetSuccessfully() throws Exception {
            // Given
            when(budgetService.deleteBudget(1L)).thenReturn(true);

            // When & Then
            mockMvc.perform(delete("/budgets/1"))
                    .andExpect(status().isNoContent());

            verify(budgetService).deleteBudget(1L);
        }

        @Test
        @DisplayName("should return not found when budget doesn't exist")
        void shouldReturnNotFoundWhenBudgetDoesntExist() throws Exception {
            // Given
            when(budgetService.deleteBudget(999L)).thenReturn(false);

            // When & Then
            mockMvc.perform(delete("/budgets/999"))
                    .andExpect(status().isNotFound());

            verify(budgetService).deleteBudget(999L);
        }

        @Test
        @DisplayName("should return bad request when id is invalid")
        void shouldReturnBadRequestWhenIdIsInvalid() throws Exception {
            // When & Then
            mockMvc.perform(delete("/budgets/-1"))
                    .andExpect(status().isBadRequest());

            verify(budgetService, never()).deleteBudget(any());
        }
    }

    @Nested
    @DisplayName("GET /budgets/{budgetId}/categories - Get Budget Categories")
    class GetBudgetCategoriesTests {

        @Test
        @DisplayName("should return budget categories successfully")
        void shouldReturnBudgetCategoriesSuccessfully() throws Exception {
            // Given
            List<BudgetCategoryModel> categories = Arrays.asList(
                createSampleCategory(1L, 1L, "Food", 500.0, 200.0),
                createSampleCategory(2L, 1L, "Transport", 300.0, 150.0)
            );
            when(budgetService.getCategoriesByBudgetId(1L)).thenReturn(categories);

            // When & Then
            mockMvc.perform(get("/budgets/1/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].name").value("Food"))
                    .andExpect(jsonPath("$[1].name").value("Transport"));

            verify(budgetService).getCategoriesByBudgetId(1L);
        }

        @Test
        @DisplayName("should return bad request when budgetId is invalid")
        void shouldReturnBadRequestWhenBudgetIdIsInvalid() throws Exception {
            // When & Then
            mockMvc.perform(get("/budgets/-1/categories"))
                    .andExpect(status().isBadRequest());

            verify(budgetService, never()).getCategoriesByBudgetId(any());
        }
    }

    @Nested
    @DisplayName("POST /budgets/{budgetId}/categories - Create Category")
    class CreateCategoryTests {

        @Test
        @DisplayName("should create category successfully")
        void shouldCreateCategorySuccessfully() throws Exception {
            // Given
            BudgetCategoryModel categoryToCreate = createSampleCategory(null, 1L, "New Category", 400.0, 0.0);
            BudgetCategoryModel createdCategory = createSampleCategory(1L, 1L, "New Category", 400.0, 0.0);
            when(budgetService.createCategory(any(BudgetCategoryModel.class))).thenReturn(createdCategory);

            // When & Then
            mockMvc.perform(post("/budgets/1/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(categoryToCreate)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("New Category"));

            verify(budgetService).createCategory(any(BudgetCategoryModel.class));
        }

        @Test
        @DisplayName("should return bad request when budgetId is invalid")
        void shouldReturnBadRequestWhenBudgetIdIsInvalid() throws Exception {
            // Given
            BudgetCategoryModel categoryToCreate = createSampleCategory(null, 1L, "New Category", 400.0, 0.0);

            // When & Then
            mockMvc.perform(post("/budgets/-1/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(categoryToCreate)))
                    .andExpect(status().isBadRequest());

            verify(budgetService, never()).createCategory(any());
        }
    }

    @Nested
    @DisplayName("GET /budgets/summary - Get Budget Summaries")
    class GetBudgetSummariesTests {

        @Test
        @DisplayName("should return budget summaries successfully")
        void shouldReturnBudgetSummariesSuccessfully() throws Exception {
            // Given
            List<BudgetSummaryDTO> summaries = Arrays.asList(
                createSampleBudgetSummary(1L, "Budget 1", 1000.0, 500.0),
                createSampleBudgetSummary(2L, "Budget 2", 2000.0, 0.0)
            );
            when(budgetService.getBudgetSummariesByUserId(1)).thenReturn(summaries);

            // When & Then
            mockMvc.perform(get("/budgets/summary")
                    .param("userId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].name").value("Budget 1"))
                    .andExpect(jsonPath("$[1].name").value("Budget 2"));

            verify(budgetService).getBudgetSummariesByUserId(1);
        }
    }

    @Nested
    @DisplayName("GET /budgets/{id}/summary - Get Budget Summary By ID")
    class GetBudgetSummaryByIdTests {

        @Test
        @DisplayName("should return budget summary when found")
        void shouldReturnBudgetSummaryWhenFound() throws Exception {
            // Given
            BudgetSummaryDTO summary = createSampleBudgetSummary(1L, "Test Budget", 1000.0, 500.0);
            when(budgetService.getBudgetSummaryById(1L)).thenReturn(Optional.of(summary));

            // When & Then
            mockMvc.perform(get("/budgets/1/summary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Test Budget"));

            verify(budgetService).getBudgetSummaryById(1L);
        }

        @Test
        @DisplayName("should return not found when budget summary doesn't exist")
        void shouldReturnNotFoundWhenBudgetSummaryDoesntExist() throws Exception {
            // Given
            when(budgetService.getBudgetSummaryById(999L)).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(get("/budgets/999/summary"))
                    .andExpect(status().isNotFound());

            verify(budgetService).getBudgetSummaryById(999L);
        }
    }

    @Nested
    @DisplayName("GET /budgets/dashboard - Get Budget Dashboard")
    class GetBudgetDashboardTests {

        @Test
        @DisplayName("should return budget dashboard successfully")
        void shouldReturnBudgetDashboardSuccessfully() throws Exception {
            // Given
            List<BudgetSummaryDTO> summaries = Arrays.asList(
                createSampleBudgetSummary(1L, "Budget 1", 1000.0, 500.0),
                createSampleBudgetSummary(2L, "Budget 2", 2000.0, 0.0)
            );
            when(budgetService.getBudgetSummariesByUserId(1)).thenReturn(summaries);

            // When & Then
            mockMvc.perform(get("/budgets/dashboard")
                    .param("userId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.budgets").isArray())
                    .andExpect(jsonPath("$.summary").exists())
                    .andExpect(jsonPath("$.summary.totalBudgets").value(2));

            verify(budgetService).getBudgetSummariesByUserId(1);
        }
    }

    @Nested
    @DisplayName("Business Logic Endpoints")
    class BusinessLogicTests {

        @Test
        @DisplayName("should get over budget budgets successfully")
        void shouldGetOverBudgetBudgetsSuccessfully() throws Exception {
            // Given
            List<BudgetModel> overBudgetBudgets = Arrays.asList(
                createSampleBudget(1L, "Over Budget", 1000.0, 1200.0)
            );
            when(budgetService.getOverBudgetBudgets(1)).thenReturn(overBudgetBudgets);

            // When & Then
            mockMvc.perform(get("/budgets/over-budget")
                    .param("userId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].name").value("Over Budget"));

            verify(budgetService).getOverBudgetBudgets(1);
        }

        @Test
        @DisplayName("should get near limit budgets successfully")
        void shouldGetNearLimitBudgetsSuccessfully() throws Exception {
            // Given
            List<BudgetModel> nearLimitBudgets = Arrays.asList(
                createSampleBudget(1L, "Near Limit", 1000.0, 850.0)
            );
            when(budgetService.getNearLimitBudgets(1)).thenReturn(nearLimitBudgets);

            // When & Then
            mockMvc.perform(get("/budgets/near-limit")
                    .param("userId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].name").value("Near Limit"));

            verify(budgetService).getNearLimitBudgets(1);
        }

        @Test
        @DisplayName("should update budget totals successfully")
        void shouldUpdateBudgetTotalsSuccessfully() throws Exception {
            // Given
            doNothing().when(budgetService).updateBudgetTotals(1L);

            // When & Then
            mockMvc.perform(post("/budgets/1/update-totals"))
                    .andExpect(status().isOk());

            verify(budgetService).updateBudgetTotals(1L);
        }
    }

    // Helper methods to create test data
    private BudgetModel createSampleBudget(Long id, String name, Double planned, Double spent) {
        BudgetModel budget = new BudgetModel();
        budget.setId(id);
        budget.setName(name);
        budget.setUserId(1);
        budget.setStartDate("2025-01-01");
        budget.setEndDate("2025-01-31");
        budget.setTotalPlanned(planned);
        budget.setTotalSpent(spent);
        budget.setIsActive(true);
        budget.setIsRepeatable(false);
        budget.setRepeatInterval(null);
        budget.setRepeatCount(1);
        budget.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0, 0));
        budget.setUpdatedAt(LocalDateTime.of(2025, 1, 1, 10, 0, 0));
        return budget;
    }

    private BudgetCategoryModel createSampleCategory(Long id, Long budgetId, String name, Double planned, Double spent) {
        BudgetCategoryModel category = new BudgetCategoryModel();
        category.setId(id);
        category.setBudgetId(budgetId);
        category.setName(name);
        category.setPlannedAmount(planned);
        category.setSpentAmount(spent);
        category.setColor("#FF6384");
        category.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0, 0));
        category.setUpdatedAt(LocalDateTime.of(2025, 1, 1, 10, 0, 0));
        return category;
    }

    private BudgetSummaryDTO createSampleBudgetSummary(Long id, String name, Double planned, Double spent) {
        BudgetSummaryDTO summary = new BudgetSummaryDTO();
        summary.setId(id);
        summary.setName(name);
        summary.setUserId(1);
        summary.setStartDate("2025-01-01");
        summary.setEndDate("2025-01-31");
        summary.setTotalPlanned(planned);
        summary.setTotalSpent(spent);
        summary.setTotalRemaining(planned - spent);
        summary.setIsActive(true);
        summary.setIsRepeatable(false);
        summary.setRepeatInterval(null);
        summary.setRepeatCount(1);
        summary.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0, 0));
        summary.setUpdatedAt(LocalDateTime.of(2025, 1, 1, 10, 0, 0));
        summary.setSpentPercentage(planned > 0 ? (spent / planned) * 100 : 0);
        summary.setIsOverBudget(spent > planned);
        summary.setIsNearLimit(planned > 0 && (spent / planned) >= 0.8 && spent < planned);
        summary.setStatus(summary.getIsOverBudget() ? "over-budget" : 
                         summary.getIsNearLimit() ? "near-limit" : "on-track");
        summary.setCategories(Arrays.asList());
        summary.setTotalCategories(0);
        summary.setActiveCategories(0);
        summary.setOverBudgetCategories(0);
        summary.setNearLimitCategories(0);
        return summary;
    }
}
