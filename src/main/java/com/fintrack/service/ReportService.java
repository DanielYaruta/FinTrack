package com.fintrack.service;

import com.fintrack.dao.ReportDao;
import com.fintrack.exception.ResourceNotFoundException;
import com.fintrack.model.Report;
import com.fintrack.model.ReportType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        Report report = new Report(period, ReportType.MONTHLY, LocalDateTime.now(), userId);
        return reportDao.save(report);
    }

    /**
     * Квартальный отчёт.
     * period формируется как "2024-Q2" — квартал определяется по месяцу переданной даты.
     */
    public Report generateQuarterly(Long userId, LocalDate date) {
        int quarter = (date.getMonthValue() - 1) / 3 + 1;
        String period = date.getYear() + "-Q" + quarter;
        Report report = new Report(period, ReportType.QUARTERLY, LocalDateTime.now(), userId);
        return reportDao.save(report);
    }

    public void deleteById(Long id) {
        reportDao.deleteById(id);
    }
}
