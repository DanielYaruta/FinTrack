-- =============================================================
-- V1 — Начальная схема базы данных
--
-- Flyway выполняет этот файл ровно один раз, запоминает в таблице
-- flyway_schema_history и никогда не запускает повторно.
-- Если нужно изменить схему — создаёшь V2__... файл.
-- =============================================================

CREATE TABLE users (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(100)  NOT NULL,
    email            VARCHAR(255)  NOT NULL UNIQUE,
    registration_date DATE         NOT NULL DEFAULT CURRENT_DATE
);

CREATE TABLE categories (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    -- CHECK гарантирует, что в БД попадут только допустимые значения,
    -- даже если приложение обойдёт JPA-валидацию.
    type VARCHAR(20)  NOT NULL CHECK (type IN ('INCOME', 'EXPENSE'))
);

CREATE TABLE transactions (
    id          BIGSERIAL PRIMARY KEY,
    amount      NUMERIC(15, 2) NOT NULL,
    type        VARCHAR(20)    NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    category_id BIGINT         REFERENCES categories(id) ON DELETE SET NULL,
    date        DATE           NOT NULL,
    description VARCHAR(500),
    user_id     BIGINT         NOT NULL REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE reports (
    id           BIGSERIAL PRIMARY KEY,
    period       VARCHAR(20)  NOT NULL,
    type         VARCHAR(20)  NOT NULL CHECK (type IN ('MONTHLY', 'QUARTERLY')),
    generated_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    user_id      BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE
);

-- Индексы для ускорения типичных запросов (фильтрация по user_id и date)
CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_date    ON transactions(date);
CREATE INDEX idx_reports_user_id      ON reports(user_id);
