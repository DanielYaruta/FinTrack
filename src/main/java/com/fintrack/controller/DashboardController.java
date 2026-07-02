package com.fintrack.controller;

import com.fintrack.dto.FinancialMetrics;
import com.fintrack.dto.MonthlyMetrics;
import com.fintrack.model.Transaction;
import com.fintrack.service.AnalyticsService;
import com.fintrack.service.TransactionService;
import com.fintrack.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class DashboardController {

    private final AnalyticsService   analyticsService;
    private final TransactionService transactionService;
    private final UserService        userService;

    public DashboardController(AnalyticsService analyticsService,
                               TransactionService transactionService,
                               UserService userService) {
        this.analyticsService   = analyticsService;
        this.transactionService = transactionService;
        this.userService        = userService;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        Long userId = currentUserId(auth);

        FinancialMetrics metrics = analyticsService.getMetrics(userId);
        List<MonthlyMetrics> trend = analyticsService.getMonthlyTrend(userId, 6);

        List<Transaction> recent = transactionService.findAllByUserId(userId)
                .stream().limit(5).toList();

        List<String>     chartMonths   = trend.stream().map(MonthlyMetrics::month).toList();
        List<BigDecimal> chartIncomes  = trend.stream().map(MonthlyMetrics::income).toList();
        List<BigDecimal> chartExpenses = trend.stream().map(MonthlyMetrics::expense).toList();

        model.addAttribute("metrics", metrics);
        model.addAttribute("recent", recent);
        model.addAttribute("chartMonths", chartMonths);
        model.addAttribute("chartIncomes", chartIncomes);
        model.addAttribute("chartExpenses", chartExpenses);

        return "dashboard";
    }

    // Authentication.getName() возвращает email (то, что мы передали как username в UserDetails)
    private Long currentUserId(Authentication auth) {
        return userService.findByEmail(auth.getName()).getId();
    }
}
