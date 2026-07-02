package com.fintrack.controller;

import com.fintrack.service.AnalyticsService;
import com.fintrack.service.ReportService;
import com.fintrack.service.TransactionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private static final Long DEMO_USER_ID = 1L;

    private final ReportService      reportService;
    private final TransactionService transactionService;
    private final AnalyticsService   analyticsService;

    public ReportController(ReportService reportService,
                            TransactionService transactionService,
                            AnalyticsService analyticsService) {
        this.reportService      = reportService;
        this.transactionService = transactionService;
        this.analyticsService   = analyticsService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("reports", reportService.findByUserId(DEMO_USER_ID));
        return "reports";
    }

    @PostMapping("/monthly")
    public String generateMonthly(RedirectAttributes ra) {
        reportService.generateMonthly(DEMO_USER_ID, LocalDate.now());
        ra.addFlashAttribute("success", "Месячный отчёт сформирован");
        return "redirect:/reports";
    }

    @PostMapping("/quarterly")
    public String generateQuarterly(RedirectAttributes ra) {
        reportService.generateQuarterly(DEMO_USER_ID, LocalDate.now());
        ra.addFlashAttribute("success", "Квартальный отчёт сформирован");
        return "redirect:/reports";
    }

    /**
     * GET /reports/preview?from=2024-04-01&to=2024-06-30
     * HTML-предпросмотр отчёта перед скачиванием.
     * Кнопки "Скачать PDF/Excel" ведут на /api/reports/export — без JS.
     */
    @GetMapping("/preview")
    public String preview(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Model model) {

        model.addAttribute("transactions",
                transactionService.findByUserIdAndDateBetween(DEMO_USER_ID, from, to));
        model.addAttribute("metrics",
                analyticsService.getMetricsForPeriod(DEMO_USER_ID, from, to));
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
}
