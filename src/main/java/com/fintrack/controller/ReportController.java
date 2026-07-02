package com.fintrack.controller;

import com.fintrack.service.AnalyticsService;
import com.fintrack.service.ReportService;
import com.fintrack.service.TransactionService;
import com.fintrack.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService      reportService;
    private final TransactionService transactionService;
    private final AnalyticsService   analyticsService;
    private final UserService        userService;

    public ReportController(ReportService reportService,
                            TransactionService transactionService,
                            AnalyticsService analyticsService,
                            UserService userService) {
        this.reportService      = reportService;
        this.transactionService = transactionService;
        this.analyticsService   = analyticsService;
        this.userService        = userService;
    }

    @GetMapping
    public String list(Authentication auth, Model model) {
        model.addAttribute("reports", reportService.findByUserId(currentUserId(auth)));
        return "reports";
    }

    @PostMapping("/monthly")
    public String generateMonthly(Authentication auth, RedirectAttributes ra) {
        reportService.generateMonthly(currentUserId(auth), LocalDate.now());
        ra.addFlashAttribute("success", "Месячный отчёт сформирован");
        return "redirect:/reports";
    }

    @PostMapping("/quarterly")
    public String generateQuarterly(Authentication auth, RedirectAttributes ra) {
        reportService.generateQuarterly(currentUserId(auth), LocalDate.now());
        ra.addFlashAttribute("success", "Квартальный отчёт сформирован");
        return "redirect:/reports";
    }

    @GetMapping("/preview")
    public String preview(
            Authentication auth,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Model model) {

        Long userId = currentUserId(auth);
        model.addAttribute("transactions",
                transactionService.findByUserIdAndDateBetween(userId, from, to));
        model.addAttribute("metrics",
                analyticsService.getMetricsForPeriod(userId, from, to));
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        return "reports-preview";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        reportService.deleteById(id);
        ra.addFlashAttribute("success", "Отчёт удалён");
        return "redirect:/reports";
    }

    private Long currentUserId(Authentication auth) {
        return userService.findByEmail(auth.getName()).getId();
    }
}
