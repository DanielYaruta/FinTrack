package com.fintrack.controller;

import com.fintrack.dto.FinancialMetrics;
import com.fintrack.dto.MonthlyMetrics;
import com.fintrack.model.User;
import com.fintrack.service.AnalyticsService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * После добавления Spring Security все незащищённые запросы → 302 на /login.
 *
 * @WithMockUser — помещает в SecurityContext фиктивного пользователя с заданным username.
 *   username = "demo@fintrack.com" → Authentication.getName() вернёт этот email.
 *   Реального обращения к базе данных НЕ происходит.
 *
 * @MockBean UserService нужен, потому что DashboardController вызывает
 *   userService.findByEmail(auth.getName()) для получения userId.
 */
@WebMvcTest(DashboardController.class)
@WithMockUser(username = "demo@fintrack.com")  // применяется ко всем тестам класса
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private UserService userService;

    @Test
    void root_shouldRedirectToDashboard() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    void dashboard_shouldReturn200WithCorrectViewAndModel() throws Exception {
        setupMocks();

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("metrics", "recent",
                        "chartMonths", "chartIncomes", "chartExpenses"));
    }

    @Test
    void dashboard_shouldPassMetricsToModel() throws Exception {
        FinancialMetrics metrics = new FinancialMetrics(
                BigDecimal.valueOf(50_000), BigDecimal.valueOf(30_000),
                BigDecimal.valueOf(20_000), 8L);

        User user = makeUser(1L);
        given(userService.findByEmail("demo@fintrack.com")).willReturn(user);
        given(analyticsService.getMetrics(1L)).willReturn(metrics);
        given(analyticsService.getMonthlyTrend(eq(1L), anyInt())).willReturn(List.of(
                new MonthlyMetrics("2026-06", BigDecimal.valueOf(50_000), BigDecimal.valueOf(30_000))
        ));
        given(transactionService.findAllByUserId(1L)).willReturn(List.of());

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("metrics", metrics));
    }

    // --- вспомогательные ---

    private void setupMocks() {
        User user = makeUser(1L);
        given(userService.findByEmail("demo@fintrack.com")).willReturn(user);
        given(analyticsService.getMetrics(anyLong())).willReturn(
                new FinancialMetrics(BigDecimal.valueOf(100_000), BigDecimal.valueOf(60_000),
                        BigDecimal.valueOf(40_000), 15L));
        given(analyticsService.getMonthlyTrend(anyLong(), anyInt())).willReturn(List.of(
                new MonthlyMetrics("2026-06", BigDecimal.TEN, BigDecimal.ONE)
        ));
        given(transactionService.findAllByUserId(anyLong())).willReturn(List.of());
    }

    private User makeUser(Long id) {
        User user = new User("Demo User", "demo@fintrack.com", LocalDate.now());
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
