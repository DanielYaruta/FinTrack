package com.fintrack.controller;

import com.fintrack.model.Transaction;
import com.fintrack.service.ExcelExportService;
import com.fintrack.service.PdfExportService;
import com.fintrack.service.TransactionService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * REST-контроллер для скачивания файлов.
 *
 * Ключевое отличие от обычных API-методов: мы не возвращаем объект для сериализации,
 * а пишем байты напрямую в HttpServletResponse.getOutputStream().
 *
 * Content-Disposition: attachment — браузер предложит сохранить файл,
 *   а не открывать его на странице.
 */
@RestController
@RequestMapping("/api/reports")
public class ApiReportController {

    private static final Long DEMO_USER_ID = 1L;

    private final TransactionService transactionService;
    private final PdfExportService   pdfExportService;
    private final ExcelExportService excelExportService;

    public ApiReportController(TransactionService transactionService,
                               PdfExportService pdfExportService,
                               ExcelExportService excelExportService) {
        this.transactionService  = transactionService;
        this.pdfExportService    = pdfExportService;
        this.excelExportService  = excelExportService;
    }

    /**
     * GET /api/reports/export?format=pdf&from=2024-04-01&to=2024-06-30
     * GET /api/reports/export?format=excel&from=2024-04-01&to=2024-06-30
     *
     * Браузер напрямую скачивает файл по этому URL.
     * Кнопки "Скачать" на странице предпросмотра ведут сюда через обычный <a href>.
     */
    @GetMapping("/export")
    public void export(
            @RequestParam String format,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletResponse response) throws IOException {

        List<Transaction> transactions =
                transactionService.findByUserIdAndDateBetween(DEMO_USER_ID, from, to);

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
}
