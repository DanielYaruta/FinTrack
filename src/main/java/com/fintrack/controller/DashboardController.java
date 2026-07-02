package com.fintrack.controller;

import com.fintrack.dto.FinancialMetrics;
import com.fintrack.dto.MonthlyMetrics;
import com.fintrack.model.Transaction;
import com.fintrack.service.AnalyticsService;
import com.fintrack.service.TransactionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Controller — возвращает имя Thymeleaf-шаблона (строку), а не JSON.
 * Именно этим отличается от @RestController, который сериализует
 * возвращаемый объект в JSON.
 *
 * TODO Stage 7: DEMO_USER_ID заменить на SecurityContextHolder.getContext()
 *               .getAuthentication().getName() → поиск пользователя по email.
 */
@Controller
public class DashboardController {

    private static final Long DEMO_USER_ID = 1L;

    private final AnalyticsService analyticsService;
    private final TransactionService transactionService;

    public DashboardController(AnalyticsService analyticsService,
                               TransactionService transactionService) {
        this.analyticsService = analyticsService;
        this.transactionService = transactionService;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        FinancialMetrics metrics = analyticsService.getMetrics(DEMO_USER_ID);
        List<MonthlyMetrics> trend = analyticsService.getMonthlyTrend(DEMO_USER_ID, 6);

        // Последние 5 транзакций для таблицы на дашборде
        List<Transaction> recent = transactionService.findAllByUserId(DEMO_USER_ID)
                .stream().limit(5).toList();

        // Раскладываем тренд в три отдельных списка — удобнее передавать в Chart.js
        List<String> chartMonths   = trend.stream().map(MonthlyMetrics::month).toList();
        List<BigDecimal> chartIncomes  = trend.stream().map(MonthlyMetrics::income).toList();
        List<BigDecimal> chartExpenses = trend.stream().map(MonthlyMetrics::expense).toList();

        model.addAttribute("metrics", metrics);
        model.addAttribute("recent", recent);
        model.addAttribute("chartMonths", chartMonths);
        model.addAttribute("chartIncomes", chartIncomes);
        model.addAttribute("chartExpenses", chartExpenses);

        return "dashboard";
    }
}
