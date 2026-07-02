package com.fintrack;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke-тест: проверяет, что Spring-контекст поднимается без ошибок.
 *
 * Требует запущенного PostgreSQL с базой fintrack_dev.
 * Запуск вручную: mvn test -Dtest=FinTrackApplicationTests
 */
@SpringBootTest
@ActiveProfiles("dev")
@Disabled("Интеграционный тест — требует запущенного PostgreSQL. Запускай вручную.")
class FinTrackApplicationTests {

    @Test
    void contextLoads() {
    }
}
