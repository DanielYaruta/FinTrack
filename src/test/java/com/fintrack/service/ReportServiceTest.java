package com.fintrack.service;

import com.fintrack.dao.ReportDao;
import com.fintrack.model.Report;
import com.fintrack.model.ReportType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportDao reportDao;

    @InjectMocks
    private ReportService reportService;

    // --- resolveDateRange (MONTHLY) ---

    @Test
    void resolveDateRange_monthly_shouldReturnFirstAndLastDayOfMonth() {
        Report report = new Report("2024-05", ReportType.MONTHLY, LocalDateTime.now(), 1L);

        LocalDate[] range = reportService.resolveDateRange(report);

        assertThat(range[0]).isEqualTo(LocalDate.of(2024, 5, 1));
        assertThat(range[1]).isEqualTo(LocalDate.of(2024, 5, 31));
    }

    @Test
    void resolveDateRange_monthly_februaryLeapYear_shouldReturn29Days() {
        Report report = new Report("2024-02", ReportType.MONTHLY, LocalDateTime.now(), 1L);

        LocalDate[] range = reportService.resolveDateRange(report);

        assertThat(range[0]).isEqualTo(LocalDate.of(2024, 2, 1));
        assertThat(range[1]).isEqualTo(LocalDate.of(2024, 2, 29));
    }

    // --- resolveDateRange (QUARTERLY) ---

    @Test
    void resolveDateRange_q1_shouldReturnJanuaryToMarch() {
        Report report = new Report("2024-Q1", ReportType.QUARTERLY, LocalDateTime.now(), 1L);

        LocalDate[] range = reportService.resolveDateRange(report);

        assertThat(range[0]).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(range[1]).isEqualTo(LocalDate.of(2024, 3, 31));
    }

    @Test
    void resolveDateRange_q2_shouldReturnAprilToJune() {
        Report report = new Report("2024-Q2", ReportType.QUARTERLY, LocalDateTime.now(), 1L);

        LocalDate[] range = reportService.resolveDateRange(report);

        assertThat(range[0]).isEqualTo(LocalDate.of(2024, 4, 1));
        assertThat(range[1]).isEqualTo(LocalDate.of(2024, 6, 30));
    }

    @Test
    void resolveDateRange_q4_shouldReturnOctoberToDecember() {
        Report report = new Report("2024-Q4", ReportType.QUARTERLY, LocalDateTime.now(), 1L);

        LocalDate[] range = reportService.resolveDateRange(report);

        assertThat(range[0]).isEqualTo(LocalDate.of(2024, 10, 1));
        assertThat(range[1]).isEqualTo(LocalDate.of(2024, 12, 31));
    }

    // --- generateYearly ---

    @Test
    void generateYearly_shouldSaveReportWithYearPeriodAndYearlyType() {
        LocalDate date = LocalDate.of(2024, 6, 15);
        Report saved = new Report("2024", ReportType.YEARLY, LocalDateTime.now(), 1L);
        given(reportDao.save(any())).willReturn(saved);

        Report result = reportService.generateYearly(1L, date);

        assertThat(result.getPeriod()).isEqualTo("2024");
        assertThat(result.getType()).isEqualTo(ReportType.YEARLY);
    }

    // --- resolveDateRange (YEARLY) ---

    @Test
    void resolveDateRange_yearly_shouldReturnJanuaryToDecember() {
        Report report = new Report("2024", ReportType.YEARLY, LocalDateTime.now(), 1L);

        LocalDate[] range = reportService.resolveDateRange(report);

        assertThat(range[0]).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(range[1]).isEqualTo(LocalDate.of(2024, 12, 31));
    }
}
