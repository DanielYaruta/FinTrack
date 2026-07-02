package com.fintrack.service;

import com.fintrack.dao.ReportDao;
import com.fintrack.exception.ResourceNotFoundException;
import com.fintrack.model.Report;
import com.fintrack.model.ReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ReportService работает через ReportDao (Spring JDBC), а не через JPA-репозиторий.
 * Это намеренное решение для демонстрации обоих подходов.
 *
 * Здесь нет @Transactional — ReportDaoImpl использует JdbcTemplate,
 * который сам управляет транзакциями. В будущем, если понадобится
 * атомарно создать Report + записать файл — добавим @Transactional.
 *
 * Генерация содержимого файла (PDF/Excel) — задача Stage 4.
 * Сейчас сервис только сохраняет метаданные отчёта в БД.
 */
@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final ReportDao reportDao;

    public ReportService(ReportDao reportDao) {
        this.reportDao = reportDao;
    }

    public List<Report> findByUserId(Long userId) {
        return reportDao.findByUserId(userId);
    }

    public Report findById(Long id) {
        return reportDao.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Отчёт", id));
    }

    /**
     * Создаёт запись о месячном отчёте.
     * @param userId  владелец отчёта
     * @param date    любая дата внутри нужного месяца (обычно берём "текущий месяц")
     */
    public Report generateMonthly(Long userId, LocalDate date) {
        String period = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        Report report = reportDao.save(new Report(period, ReportType.MONTHLY, LocalDateTime.now(), userId));
        log.info("Monthly report generated: period={}, userId={}", period, userId);
        return report;
    }

    /**
     * Квартальный отчёт.
     * period формируется как "2024-Q2" — квартал определяется по месяцу переданной даты.
     */
    public Report generateQuarterly(Long userId, LocalDate date) {
        int quarter = (date.getMonthValue() - 1) / 3 + 1;
        String period = date.getYear() + "-Q" + quarter;
        Report report = reportDao.save(new Report(period, ReportType.QUARTERLY, LocalDateTime.now(), userId));
        log.info("Quarterly report generated: period={}, userId={}", period, userId);
        return report;
    }

    public Report generateYearly(Long userId, LocalDate date) {
        String period = date.format(DateTimeFormatter.ofPattern("yyyy"));
        Report report = reportDao.save(new Report(period, ReportType.YEARLY, LocalDateTime.now(), userId));
        log.info("Yearly report generated: period={}, userId={}", period, userId);
        return report;
    }

    public void deleteById(Long id) {
        reportDao.deleteById(id);
    }

    /**
     * Разбирает поле period в диапазон дат [from, to].
     * MONTHLY  "2024-05"  → [2024-05-01, 2024-05-31]
     * QUARTERLY "2024-Q2" → [2024-04-01, 2024-06-30]
     * YEARLY   "2024"     → [2024-01-01, 2024-12-31]
     */
    public LocalDate[] resolveDateRange(Report report) {
        if (report.getType() == ReportType.MONTHLY) {
            YearMonth ym = YearMonth.parse(report.getPeriod(),
                    DateTimeFormatter.ofPattern("yyyy-MM"));
            return new LocalDate[]{ym.atDay(1), ym.atEndOfMonth()};
        }
        if (report.getType() == ReportType.YEARLY) {
            int year = Integer.parseInt(report.getPeriod());
            return new LocalDate[]{LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31)};
        }
        // QUARTERLY: "2024-Q2"
        String[] parts = report.getPeriod().split("-Q");
        int year    = Integer.parseInt(parts[0]);
        int quarter = Integer.parseInt(parts[1]);
        int firstMonth = (quarter - 1) * 3 + 1;
        LocalDate from = LocalDate.of(year, firstMonth, 1);
        LocalDate to   = YearMonth.of(year, quarter * 3).atEndOfMonth();
        return new LocalDate[]{from, to};
    }
}
