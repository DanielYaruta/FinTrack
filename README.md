# FinTrack — Personal Finance Tracker

Веб-приложение для учёта доходов и расходов с аналитикой и экспортом отчётов (PDF/Excel).

## Стек

- Java 17 · Spring Boot 3.2 · Spring MVC · Spring Data JPA · Spring JDBC
- PostgreSQL · Flyway · Thymeleaf · Bootstrap 5 · Chart.js
- iText 7 (PDF) · Apache POI (Excel)
- JUnit 5 · Mockito · Spring MockMvc

## Быстрый старт

```bash
# 1. Создай базу данных
psql -U postgres -c "CREATE DATABASE fintrack_dev;"

# 2. Настрой подключение (если у тебя другой пользователь/пароль)
# Отредактируй src/main/resources/application-dev.properties

# 3. Запусти приложение
mvn spring-boot:run

# Приложение доступно по адресу: http://localhost:8080
```

## Сборка в JAR

```bash
mvn clean package
java -jar target/fintrack-0.0.1-SNAPSHOT.jar
```

## Структура проекта

```
src/
├── main/
│   ├── java/com/fintrack/
│   │   ├── config/         # Конфигурационные классы Spring
│   │   ├── controller/     # MVC-контроллеры (Thymeleaf) и REST API
│   │   ├── dto/            # Data Transfer Objects (валидация входящих данных)
│   │   ├── model/          # JPA-сущности (User, Transaction, Category, Report)
│   │   ├── repository/     # Spring Data JPA репозитории
│   │   └── service/        # Бизнес-логика
│   └── resources/
│       ├── db/migration/   # SQL-миграции Flyway
│       ├── static/         # CSS, JS, изображения
│       └── templates/      # Thymeleaf HTML-шаблоны
└── test/                   # JUnit 5 + MockMvc тесты
```

## Прогресс разработки

- [x] Этап 0: Инициализация проекта
- [ ] Этап 1: Слой данных (JPA-сущности, репозитории, Flyway)
- [ ] Этап 2: Сервисный слой + бизнес-логика
- [ ] Этап 3: MVC-контроллеры и Thymeleaf-шаблоны
- [ ] Этап 4: REST API + экспорт PDF/Excel
- [ ] Этап 5: JS-слой (AJAX, валидация, уведомления)
- [ ] Этап 6: Тесты (Unit + Integration)
- [ ] Этап 7: Spring Security (аутентификация)
- [ ] Этап 8: Финальная документация и сборка
