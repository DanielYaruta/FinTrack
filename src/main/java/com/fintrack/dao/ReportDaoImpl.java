package com.fintrack.dao;

import com.fintrack.model.Report;
import com.fintrack.model.ReportType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * @Repository — говорит Spring: "это компонент слоя доступа к данным".
 *   Spring создаст бин и добавит обработку DataAccessException (перевод SQL-исключений
 *   в иерархию Spring-исключений — тебе не нужно ловить SQLException напрямую).
 *
 * JdbcTemplate — обёртка над JDBC, которая берёт на себя:
 *   - получение Connection из пула
 *   - создание PreparedStatement
 *   - закрытие ресурсов (try-finally)
 *   - трансляцию SQLException в RuntimeException
 *
 * Тебе остаётся только написать SQL и маппинг ResultSet → объект.
 */
@Repository
public class ReportDaoImpl implements ReportDao {

    private final JdbcTemplate jdbc;

    /**
     * Spring автоматически внедряет JdbcTemplate через конструктор.
     * JdbcTemplate сконфигурирован Spring Boot на основе DataSource из application.properties.
     */
    public ReportDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * RowMapper — функциональный интерфейс: "как превратить одну строку ResultSet в объект Report".
     * Вызывается для каждой строки результата запроса.
     */
    private final RowMapper<Report> rowMapper = (rs, rowNum) -> {
        Report report = new Report();
        report.setId(rs.getLong("id"));
        report.setPeriod(rs.getString("period"));
        report.setType(ReportType.valueOf(rs.getString("type")));
        report.setGeneratedAt(rs.getTimestamp("generated_at").toLocalDateTime());
        report.setUserId(rs.getLong("user_id"));
        return report;
    };

    @Override
    public Report save(Report report) {
        String sql = "INSERT INTO reports (period, type, generated_at, user_id) VALUES (?, ?, ?, ?)";

        /**
         * KeyHolder — держатель сгенерированного БД ключа.
         * После INSERT мы хотим узнать, какой id присвоила БД (BIGSERIAL).
         * Statement.RETURN_GENERATED_KEYS говорит JDBC-драйверу вернуть этот ключ.
         */
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, report.getPeriod());
            ps.setString(2, report.getType().name());
            ps.setTimestamp(3, Timestamp.valueOf(report.getGeneratedAt()));
            ps.setLong(4, report.getUserId());
            return ps;
        }, keyHolder);

        report.setId(keyHolder.getKey().longValue());
        return report;
    }

    @Override
    public Optional<Report> findById(Long id) {
        String sql = "SELECT * FROM reports WHERE id = ?";
        // query возвращает List — это безопаснее, чем queryForObject (не бросает исключение если не найдено)
        List<Report> results = jdbc.query(sql, rowMapper, id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<Report> findByUserId(Long userId) {
        String sql = "SELECT * FROM reports WHERE user_id = ? ORDER BY generated_at DESC";
        return jdbc.query(sql, rowMapper, userId);
    }

    @Override
    public void deleteById(Long id) {
        jdbc.update("DELETE FROM reports WHERE id = ?", id);
    }
}
