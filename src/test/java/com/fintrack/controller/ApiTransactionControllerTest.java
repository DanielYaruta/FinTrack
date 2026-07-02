package com.fintrack.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintrack.dto.TransactionDto;
import com.fintrack.exception.ResourceNotFoundException;
import com.fintrack.model.Transaction;
import com.fintrack.model.TransactionType;
import com.fintrack.model.User;
import com.fintrack.service.TransactionService;
import com.fintrack.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @WebMvcTest не загружает наш SecurityConfig автоматически — использует
 * дефолтную Spring Security конфигурацию, где CSRF включён для всех путей.
 *
 * Поэтому для POST/DELETE добавляем .with(csrf()) в тестах.
 * В продакшне наш SecurityConfig отключает CSRF для /api/** (AJAX-клиенты
 * защищены браузерной Same-Origin Policy).
 *
 * @WithMockUser — имитирует залогиненного пользователя без обращения к БД.
 */
@WebMvcTest(ApiTransactionController.class)
@WithMockUser(username = "demo@fintrack.com")
class ApiTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private UserService userService;

    // --- GET /api/transactions ---

    @Test
    void getAll_shouldReturnJsonArray() throws Exception {
        setupUser();
        given(transactionService.findAllByUserId(anyLong())).willReturn(List.of());

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getAll_withTransactions_shouldReturnNonEmptyArray() throws Exception {
        setupUser();
        List<Transaction> txs = List.of(
                makeTransaction(1L, BigDecimal.valueOf(5_000), TransactionType.EXPENSE),
                makeTransaction(2L, BigDecimal.valueOf(10_000), TransactionType.INCOME)
        );
        given(transactionService.findAllByUserId(anyLong())).willReturn(txs);

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value("EXPENSE"))
                .andExpect(jsonPath("$[1].type").value("INCOME"));
    }

    // --- GET /api/transactions/{id} ---

    @Test
    void getById_whenOwner_shouldReturn200() throws Exception {
        setupUser();
        Transaction tx = makeTransaction(42L, BigDecimal.valueOf(3_000), TransactionType.INCOME);
        given(transactionService.findByIdAndUserId(eq(42L), anyLong())).willReturn(tx);

        mockMvc.perform(get("/api/transactions/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.type").value("INCOME"));
    }

    @Test
    void getById_whenNotOwner_shouldReturn404() throws Exception {
        setupUser();
        given(transactionService.findByIdAndUserId(eq(42L), anyLong()))
                .willThrow(ResourceNotFoundException.of("Транзакция", 42L));

        mockMvc.perform(get("/api/transactions/42"))
                .andExpect(status().isNotFound());
    }

    // --- POST /api/transactions ---

    @Test
    void create_withValidJson_shouldReturn201() throws Exception {
        setupUser();
        TransactionDto dto = new TransactionDto();
        dto.setAmount(BigDecimal.valueOf(8_500));
        dto.setType(TransactionType.EXPENSE);
        dto.setDate(LocalDate.of(2024, 6, 20));

        Transaction saved = makeTransaction(10L, BigDecimal.valueOf(8_500), TransactionType.EXPENSE);
        given(transactionService.save(any(TransactionDto.class), anyLong())).willReturn(saved);

        mockMvc.perform(post("/api/transactions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.type").value("EXPENSE"));
    }

    @Test
    void create_withMissingAmount_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/transactions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\":\"EXPENSE\",\"date\":\"2024-06-15\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.amount").exists());
    }

    @Test
    void create_withNegativeAmount_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/transactions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":-500,\"type\":\"EXPENSE\",\"date\":\"2024-06-15\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.amount").exists());
    }

    // --- PUT /api/transactions/{id} ---

    @Test
    void update_withValidJson_shouldReturn200() throws Exception {
        setupUser();
        TransactionDto dto = new TransactionDto();
        dto.setAmount(BigDecimal.valueOf(6_000));
        dto.setType(TransactionType.INCOME);
        dto.setDate(LocalDate.of(2024, 6, 20));

        Transaction updated = makeTransaction(5L, BigDecimal.valueOf(6_000), TransactionType.INCOME);
        given(transactionService.update(eq(5L), any(TransactionDto.class), anyLong())).willReturn(updated);

        mockMvc.perform(put("/api/transactions/5")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.type").value("INCOME"));
    }

    @Test
    void update_whenNotOwner_shouldReturn404() throws Exception {
        setupUser();
        given(transactionService.update(eq(5L), any(), anyLong()))
                .willThrow(ResourceNotFoundException.of("Транзакция", 5L));

        mockMvc.perform(put("/api/transactions/5")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":1000,\"type\":\"INCOME\",\"date\":\"2024-06-15\"}"))
                .andExpect(status().isNotFound());
    }

    // --- DELETE /api/transactions/{id} ---

    @Test
    void delete_shouldReturn204() throws Exception {
        setupUser();

        mockMvc.perform(delete("/api/transactions/5")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(transactionService).deleteById(eq(5L), anyLong());
    }

    // --- вспомогательные ---

    private void setupUser() {
        User user = new User("Demo", "demo@fintrack.com", LocalDate.now());
        ReflectionTestUtils.setField(user, "id", 1L);
        given(userService.findByEmail("demo@fintrack.com")).willReturn(user);
    }

    private Transaction makeTransaction(Long id, BigDecimal amount, TransactionType type) {
        Transaction tx = new Transaction(amount, type, null, LocalDate.of(2024, 6, 15), null, null);
        ReflectionTestUtils.setField(tx, "id", id);
        return tx;
    }
}
