-- =============================================================
-- V3 — Добавляем поле password_hash для аутентификации
-- =============================================================
--
-- Пароль для демо-пользователя (id=1): 'demo'
-- Хеш создан: new BCryptPasswordEncoder(10).encode("demo")
-- Для смены пароля: обновить значение ниже или сгенерировать новый хеш.

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255) NOT NULL DEFAULT '';

UPDATE users
SET password_hash = '$2a$10$2lZdhhffd/S4o3HX355kT.nTkW8Veo0StWro1SwFK5oesx7r0xdkK'
WHERE id = 1;
