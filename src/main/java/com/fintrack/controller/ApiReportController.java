package com.fintrack.controller;

import com.fintrack.model.Transaction;
import com.fintrack.service.ExcelExportService;
import com.fintrack.service.PdfExportService;
import com.fintrack.service.TransactionService;
import com.fintrack.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ApiReportController {

    private final TransactionService transactionService;
    private final PdfExportService   pdfExportService;
    private final ExcelExportService excelExportService;
    private final UserService        userService;

    public ApiReportController(TransactionService transactionService,
                               PdfExportService pdfExportService,
                               ExcelExportService excelExportService,
                               UserService userService) {
        this.transactionService  = transactionService;
        this.pdfExportService    = pdfExportService;
        this.excelExportService  = excelExportService;
        this.userService         = userService;
    }

    @GetMapping("/export")
    public void export(
            @RequestParam String format,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication auth,
            HttpServletResponse response) throws IOException {

        List<Transaction> transactions =
                transactionService.findByUserIdAndDateBetween(currentUserId(auth), from, to);

        String filename = "fintrack-report-" + from + "-" + to;

        switch (format.toLowerCase()) {
            case "pdf" -> {
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition",
                        "attachment; filename=\"" + filename + ".pdf\"");
                pdfExportService.export(transactions, from, to, response.getOutputStream());
            }
            case "excel" -> {
                response.setContentType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setHeader("Content-Disposition",
                        "attachment; filename=\"" + filename + ".xlsx\"");
                excelExportService.export(transactions, from, to, response.getOutputStream());
            }
            default -> response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Неподдерживаемый формат: " + format + ". Используйте pdf или excel.");
        }
    }

    private Long currentUserId(Authentication auth) {
        return userService.findByEmail(auth.getName()).getId();
    }
}
