package com.fintrack.dao;

import com.fintrack.model.Report;

import java.util.List;
import java.util.Optional;

/**
 * DAO-интерфейс для Report.
 *
 * Паттерн DAO (Data Access Object) — отделяет бизнес-логику от деталей работы с БД.
 * Интерфейс описывает "что умеет делать DAO", а реализация — "как именно".
 * Это позволяет в тестах подменить реализацию на фейковую, не трогая сервисы.
 */
public interface ReportDao {

    Report save(Report report);

    Optional<Report> findById(Long id);

    List<Report> findByUserId(Long userId);

    void deleteById(Long id);
}
