package com.fintrack.controller;

import com.fintrack.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private static final Long DEMO_USER_ID = 1L;

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
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

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        reportService.deleteById(id);
        ra.addFlashAttribute("success", "Отчёт удалён");
        return "redirect:/reports";
    }
}
