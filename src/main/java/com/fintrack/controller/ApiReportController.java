package com.fintrack.controller;

import com.fintrack.model.Transaction;
import com.fintrack.service.ReportExportService;
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

    private final TransactionService  transactionService;
    private final ReportExportService reportExportService;
    private final UserService         userService;

    public ApiReportController(TransactionService transactionService,
                               ReportExportService reportExportService,
                               UserService userService) {
        this.transactionService  = transactionService;
        this.reportExportService = reportExportService;
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

        reportExportService.writeExport(format, transactions, from, to,
                "fintrack-report-" + from + "-" + to, response);
    }

    private Long currentUserId(Authentication auth) {
        return userService.findByEmail(auth.getName()).getId();
    }
}
