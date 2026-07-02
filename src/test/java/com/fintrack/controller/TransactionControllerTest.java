package com.fintrack.controller;

import com.fintrack.model.Transaction;
import com.fintrack.model.TransactionType;
import com.fintrack.model.User;
import com.fintrack.service.CategoryService;
import com.fintrack.service.TransactionService;
import com.fintrack.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
 * После добавления Spring Security нужно:
 *   1. @WithMockUser — имитирует залогиненного пользователя
 *   2. .with(csrf()) для POST-запросов — добавляет CSRF-токен
 *      (Thymeleaf делает это автоматически в браузере, а в тестах нужно вручную)
 */
@WebMvcTest(TransactionController.class)
@WithMockUser(username = "demo@fintrack.com")
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private UserService userService;

    // --- GET /transactions ---

    @Test
    void list_shouldReturn200WithRequiredModelAttributes() throws Exception {
        setupUser();
        given(transactionService.findAllByUserId(anyLong())).willReturn(List.of());
        given(categoryService.findAll()).willReturn(List.of());

        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(view().name("transactions"))
                .andExpect(model().attributeExists("transactions", "transactionDto", "categories"));
    }

    @Test
    void list_withDateFilter_shouldCallFilteredQuery() throws Exception {
        setupUser();
        given(transactionService.findByUserIdAndDateBetween(anyLong(), any(), any()))
                .willReturn(List.of());
        given(categoryService.findAll()).willReturn(List.of());

        mockMvc.perform(get("/transactions")
                .param("from", "2024-06-01")
                .param("to",   "2024-06-30"))
                .andExpect(status().isOk());

        verify(transactionService).findByUserIdAndDateBetween(anyLong(), any(), any());
    }

    // --- POST /transactions (успех) ---

    @Test
    void create_withValidData_shouldRedirectWithSuccessFlash() throws Exception {
        setupUser();
        Transaction saved = new Transaction(BigDecimal.valueOf(5_000), TransactionType.EXPENSE,
                null, LocalDate.of(2024, 6, 15), null, null);
        given(transactionService.save(any(), anyLong())).willReturn(saved);

        mockMvc.perform(post("/transactions")
                // .with(csrf()) — добавляет CSRF-токен в POST-запрос
                .with(csrf())
                .param("amount",      "5000")
                .param("type",        "EXPENSE")
                .param("date",        "2024-06-15"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transactions"))
                .andExpect(flash().attributeExists("success"));
    }

    // --- POST /transactions (ошибки валидации) ---

    @Test
    void create_withNegativeAmount_shouldReturnFormWithErrors() throws Exception {
        setupUser();
        given(transactionService.findAllByUserId(anyLong())).willReturn(List.of());
        given(categoryService.findAll()).willReturn(List.of());

        mockMvc.perform(post("/transactions").with(csrf())
                .param("amount", "-100")
                .param("type",   "EXPENSE")
                .param("date",   "2024-06-15"))
                .andExpect(status().isOk())
                .andExpect(view().name("transactions"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("transactionDto", "amount"));
    }

    @Test
    void create_withMissingType_shouldReturnFormWithErrors() throws Exception {
        setupUser();
        given(transactionService.findAllByUserId(anyLong())).willReturn(List.of());
        given(categoryService.findAll()).willReturn(List.of());

        mockMvc.perform(post("/transactions").with(csrf())
                .param("amount", "1000")
                .param("date",   "2024-06-15"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("transactionDto", "type"));
    }

    @Test
    void create_withFutureDate_shouldReturnFormWithErrors() throws Exception {
        setupUser();
        given(transactionService.findAllByUserId(anyLong())).willReturn(List.of());
        given(categoryService.findAll()).willReturn(List.of());

        mockMvc.perform(post("/transactions").with(csrf())
                .param("amount", "1000")
                .param("type",   "INCOME")
                .param("date",   "2099-01-01"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("transactionDto", "date"));
    }

    // --- POST /transactions/{id}/delete ---

    @Test
    void delete_shouldRedirectWithSuccess() throws Exception {
        setupUser();

        mockMvc.perform(post("/transactions/1/delete").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transactions"))
                .andExpect(flash().attributeExists("success"));

        verify(transactionService).deleteById(eq(1L), anyLong());
    }

    // --- вспомогательные ---

    private void setupUser() {
        User user = new User("Demo", "demo@fintrack.com", LocalDate.now());
        ReflectionTestUtils.setField(user, "id", 1L);
        given(userService.findByEmail("demo@fintrack.com")).willReturn(user);
    }
}
