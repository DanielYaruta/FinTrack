package com.fintrack.controller;

import com.fintrack.exception.ResourceNotFoundException;
import com.fintrack.model.Report;
import com.fintrack.model.ReportType;
import com.fintrack.model.User;
import com.fintrack.service.AnalyticsService;
import com.fintrack.service.ExcelExportService;
import com.fintrack.service.PdfExportService;
import com.fintrack.service.ReportExportService;
import com.fintrack.service.ReportService;
import com.fintrack.service.TransactionService;
import com.fintrack.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// ReportExportService подключается как реальный бин: его writeExport выставляет Content-Type,
// а PdfExportService/ExcelExportService остаются замоканными (пишут пустой поток).
@WebMvcTest(ReportController.class)
@Import(ReportExportService.class)
@WithMockUser(username = "demo@fintrack.com")
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private ReportService      reportService;
    @MockBean private TransactionService transactionService;
    @MockBean private AnalyticsService   analyticsService;
    @MockBean private PdfExportService   pdfExportService;
    @MockBean private ExcelExportService excelExportService;
    @MockBean private UserService        userService;

    // --- GET /reports/{id}/download ---

    @Test
    void download_pdf_shouldReturn200WithPdfContentType() throws Exception {
        setupUser();
        Report report = makeReport(1L, "2024-05", ReportType.MONTHLY);
        given(reportService.findById(1L)).willReturn(report);
        given(reportService.resolveDateRange(report))
                .willReturn(new LocalDate[]{LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 31)});
        given(transactionService.findByUserIdAndDateBetween(anyLong(), any(), any()))
                .willReturn(List.of());

        mockMvc.perform(get("/reports/1/download").param("format", "pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    void download_excel_shouldReturn200WithExcelContentType() throws Exception {
        setupUser();
        Report report = makeReport(2L, "2024-Q1", ReportType.QUARTERLY);
        given(reportService.findById(2L)).willReturn(report);
        given(reportService.resolveDateRange(report))
                .willReturn(new LocalDate[]{LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31)});
        given(transactionService.findByUserIdAndDateBetween(anyLong(), any(), any()))
                .willReturn(List.of());

        mockMvc.perform(get("/reports/2/download").param("format", "excel"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    void download_whenNotOwner_shouldReturn404() throws Exception {
        setupUser();
        // Отчёт принадлежит пользователю 99, а залогинен пользователь 1
        Report report = makeReport(3L, "2024-05", ReportType.MONTHLY);
        report.setUserId(99L);
        given(reportService.findById(3L)).willReturn(report);

        mockMvc.perform(get("/reports/3/download").param("format", "pdf"))
                .andExpect(status().isNotFound());
    }

    @Test
    void download_whenReportNotFound_shouldReturn404() throws Exception {
        setupUser();
        given(reportService.findById(99L))
                .willThrow(ResourceNotFoundException.of("Отчёт", 99L));

        mockMvc.perform(get("/reports/99/download").param("format", "pdf"))
                .andExpect(status().isNotFound());
    }

    // --- вспомогательные ---

    private void setupUser() {
        User user = new User("Demo", "demo@fintrack.com", LocalDate.now());
        ReflectionTestUtils.setField(user, "id", 1L);
        given(userService.findByEmail("demo@fintrack.com")).willReturn(user);
    }

    private Report makeReport(Long id, String period, ReportType type) {
        Report report = new Report(period, type, LocalDateTime.now(), 1L);
        report.setId(id);
        return report;
    }
}
