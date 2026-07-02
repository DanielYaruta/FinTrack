package com.fintrack.service;

import com.fintrack.model.Transaction;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReportExportService {

    private final PdfExportService   pdfExportService;
    private final ExcelExportService excelExportService;

    public ReportExportService(PdfExportService pdfExportService,
                               ExcelExportService excelExportService) {
        this.pdfExportService   = pdfExportService;
        this.excelExportService = excelExportService;
    }

    /**
     * Записывает экспорт в HttpServletResponse.
     * filenameBase — имя файла без расширения, например "fintrack-report-2024-05".
     */
    public void writeExport(String format, List<Transaction> transactions,
                            LocalDate from, LocalDate to,
                            String filenameBase, HttpServletResponse response) throws IOException {
        switch (format.toLowerCase()) {
            case "pdf" -> {
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition",
                        "attachment; filename=\"" + filenameBase + ".pdf\"");
                pdfExportService.export(transactions, from, to, response.getOutputStream());
            }
            case "excel" -> {
                response.setContentType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setHeader("Content-Disposition",
                        "attachment; filename=\"" + filenameBase + ".xlsx\"");
                excelExportService.export(transactions, from, to, response.getOutputStream());
            }
            default -> response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Неподдерживаемый формат: " + format + ". Используйте pdf или excel.");
        }
    }
}
