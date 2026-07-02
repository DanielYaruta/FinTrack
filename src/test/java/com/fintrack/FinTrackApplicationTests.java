package com.fintrack;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke-тест: проверяет, что Spring-контекст поднимается без ошибок.
 * Запускать: mvn test (или зелёная кнопка рядом с классом в IDEA).
 */
@SpringBootTest
@ActiveProfiles("dev")
class FinTrackApplicationTests {

    @Test
    void contextLoads() {
        // Если контекст поднялся — тест зелёный.
        // Если какой-то бин не создаётся (неверный конфиг, нет БД) — тест упадёт с подробной ошибкой.
    }
}
