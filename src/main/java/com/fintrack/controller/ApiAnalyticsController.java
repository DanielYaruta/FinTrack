package com.fintrack.controller;

import com.fintrack.dto.FinancialMetrics;
import com.fintrack.dto.MonthlyMetrics;
import com.fintrack.service.AnalyticsService;
import com.fintrack.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class ApiAnalyticsController {

    private final AnalyticsService analyticsService;
    private final UserService      userService;

    public ApiAnalyticsController(AnalyticsService analyticsService,
                                  UserService userService) {
        this.analyticsService = analyticsService;
        this.userService      = userService;
    }

    @GetMapping("/metrics")
    public FinancialMetrics getMetrics(Authentication auth) {
        return analyticsService.getMetrics(currentUserId(auth));
    }

    @GetMapping("/trend")
    public List<MonthlyMetrics> getTrend(
            @RequestParam(defaultValue = "6") int months,
            Authentication auth) {
        return analyticsService.getMonthlyTrend(currentUserId(auth), months);
    }

    @GetMapping("/metrics/period")
    public FinancialMetrics getMetricsForPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication auth) {
        return analyticsService.getMetricsForPeriod(currentUserId(auth), from, to);
    }

    private Long currentUserId(Authentication auth) {
        return userService.findByEmail(auth.getName()).getId();
    }
}
