package com.fintrack.controller;

import com.fintrack.dto.FinancialMetrics;
import com.fintrack.dto.MonthlyMetrics;
import com.fintrack.service.AnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class ApiAnalyticsController {

    private static final Long DEMO_USER_ID = 1L;

    private final AnalyticsService analyticsService;

    public ApiAnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * GET /api/analytics/metrics — общие метрики пользователя.
     * Используется дашбордом через AJAX (Stage 5) для обновления без перезагрузки страницы.
     *
     * Пример ответа:
     * { "totalIncome": 308000, "totalExpense": 110700, "balance": 197300, "transactionCount": 20 }
     */
    @GetMapping("/metrics")
    public FinancialMetrics getMetrics() {
        return analyticsService.getMetrics(DEMO_USER_ID);
    }

    /**
     * GET /api/analytics/trend?months=6 — тренд по месяцам.
     * Используется Chart.js для построения графика.
     */
    @GetMapping("/trend")
    public List<MonthlyMetrics> getTrend(
            @RequestParam(defaultValue = "6") int months) {
        return analyticsService.getMonthlyTrend(DEMO_USER_ID, months);
    }

    /**
     * GET /api/analytics/metrics/period?from=2024-04-01&to=2024-06-30
     * Метрики за конкретный период — для страницы предпросмотра отчёта.
     */
    @GetMapping("/metrics/period")
    public FinancialMetrics getMetricsForPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return analyticsService.getMetricsForPeriod(DEMO_USER_ID, from, to);
    }
}
