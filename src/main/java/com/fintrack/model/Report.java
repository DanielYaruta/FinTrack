package com.fintrack.model;

import java.time.LocalDateTime;

/**
 * Report — намеренно НЕ является JPA-сущностью (@Entity здесь нет).
 *
 * Это обычный POJO (Plain Old Java Object). Данные читаются и пишутся
 * через Spring JDBC (JdbcTemplate + RowMapper) вручную.
 *
 * Цель: показать разницу между JPA-подходом (автоматическим) и JDBC-подходом (ручным).
 * В реальных проектах JDBC используют там, где нужен сложный SQL или максимальный контроль.
 */
public class Report {

    private Long id;
    private String period;       // Например: "2024-05" или "2024-Q1"
    private ReportType type;     // MONTHLY или QUARTERLY
    private LocalDateTime generatedAt;
    private Long userId;         // Храним только ID, не объект User — JDBC не знает о связях JPA

    public Report() {}

    public Report(String period, ReportType type, LocalDateTime generatedAt, Long userId) {
        this.period = period;
        this.type = type;
        this.generatedAt = generatedAt;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public ReportType getType() { return type; }
    public void setType(ReportType type) { this.type = type; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
