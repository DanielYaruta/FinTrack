# FinTrack — Personal Finance Tracker

Веб-приложение для учёта личных финансов: доходы, расходы, аналитика по периодам, тренды, экспорт отчётов в PDF и Excel.

# Стек

| Категория | Технологии |
|---|---|
| **Язык / платформа** | [![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/) [![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/) |
| **Фреймворк** | [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot) [![Spring MVC](https://img.shields.io/badge/Spring%20MVC-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://docs.spring.io/spring-framework/reference/web/webmvc.html) [![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-data-jpa) [![Spring JDBC](https://img.shields.io/badge/Spring%20JDBC-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://docs.spring.io/spring-framework/reference/data-access/jdbc.html) |
| **База данных** | [![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/) [![Flyway](https://img.shields.io/badge/Flyway-CC0200?style=for-the-badge&logo=flyway&logoColor=white)](https://flywaydb.org/) |
| **Безопасность** | [![Spring Security](https://img.shields.io/badge/Spring%20Security-6-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)](https://spring.io/projects/spring-security) [![BCrypt](https://img.shields.io/badge/BCrypt-4B0082?style=for-the-badge&logo=letsencrypt&logoColor=white)](https://en.wikipedia.org/wiki/Bcrypt) |
| **Шаблонизатор** | [![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.1-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)](https://www.thymeleaf.org/) |
| **Фронтенд** | [![Bootstrap](https://img.shields.io/badge/Bootstrap-5-7952B3?style=for-the-badge&logo=bootstrap&logoColor=white)](https://getbootstrap.com/) [![Chart.js](https://img.shields.io/badge/Chart.js-FF6384?style=for-the-badge&logo=chartdotjs&logoColor=white)](https://www.chartjs.org/) [![Bootstrap Icons](https://img.shields.io/badge/Bootstrap%20Icons-7952B3?style=for-the-badge&logo=bootstrap&logoColor=white)](https://icons.getbootstrap.com/) |
| **Экспорт** | [![iText](https://img.shields.io/badge/iText%207-PDF-E44D26?style=for-the-badge)](https://itextpdf.com/) [![Apache POI](https://img.shields.io/badge/Apache%20POI-Excel-D22128?style=for-the-badge&logo=apache&logoColor=white)](https://poi.apache.org/) |
| **Тесты** | [![JUnit 5](https://img.shields.io/badge/JUnit%205-25A162?style=for-the-badge&logo=junit5&logoColor=white)](https://junit.org/junit5/) [![Mockito](https://img.shields.io/badge/Mockito-78A641?style=for-the-badge)](https://site.mockito.org/) [![MockMvc](https://img.shields.io/badge/Spring%20MockMvc-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://docs.spring.io/spring-framework/reference/testing/spring-mvc-test-framework.html) |

## Быстрый старт

### 1. База данных

```bash
psql -U postgres -c "CREATE DATABASE fintrack_dev;"
```

### 2. Настройка подключения

Файл `src/main/resources/application-dev.properties` — замени под свои данные, если пользователь/пароль PostgreSQL отличается:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/fintrack_dev
spring.datasource.username=postgres
spring.datasource.password=postgres
```

### 3. Запуск

```bash
mvn spring-boot:run
```

Flyway автоматически создаст схему и заполнит тестовыми данными. Приложение доступно: **http://localhost:8080**

### Учётные данные для входа

| Поле | Значение |
|------|---------|
| Email | `demo@fintrack.com` |
| Пароль | `demo` |

## Сборка и запуск JAR

```bash
# Сборка
mvn clean package -DskipTests

# Запуск (dev-профиль, нужен запущенный PostgreSQL)
java -jar target/fintrack-0.0.1-SNAPSHOT.jar

# Запуск с prod-профилем (параметры БД через env-переменные)
export DB_URL=jdbc:postgresql://localhost:5432/fintrack_prod
export DB_USERNAME=postgres
export DB_PASSWORD=secret
java -jar target/fintrack-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## Функциональность

- **Аутентификация** — form login с BCrypt, защита всех маршрутов
- **Дашборд** — баланс, доходы/расходы, последние транзакции, график трендов за 6 месяцев
- **Транзакции** — создание, редактирование, удаление, фильтрация по датам
- **Аналитика** — метрики за период, разбивка по категориям
- **Категории** — управление категориями доходов и расходов
- **Профиль** — просмотр данных пользователя
- **Экспорт** — PDF и Excel отчёты за выбранный период
- **REST API** — JSON-эндпоинты для транзакций и аналитики (`/api/**`)

## Структура проекта

```
src/
├── main/
│   ├── java/com/fintrack/
│   │   ├── controller/     # MVC-контроллеры (Thymeleaf) и REST-контроллеры
│   │   ├── dao/            # JDBC-слой для Report (Spring JdbcTemplate)
│   │   ├── dto/            # DTO с Bean Validation аннотациями
│   │   ├── exception/      # ResourceNotFoundException, GlobalExceptionHandler
│   │   ├── model/          # JPA-сущности: User, Transaction, Category; POJO: Report
│   │   ├── repository/     # Spring Data JPA репозитории
│   │   ├── security/       # SecurityConfig, UserDetailsServiceImpl
│   │   └── service/        # Бизнес-логика, экспорт PDF/Excel
│   └── resources/
│       ├── db/migration/   # Flyway: V1 схема, V2 сиды, V3 пароль
│       ├── static/         # CSS (app.css), JS, images (favicon.svg)
│       └── templates/      # Thymeleaf-шаблоны + фрагменты
└── test/
    └── java/com/fintrack/
        ├── service/        # Unit-тесты сервисов (Mockito)
        └── controller/     # WebMvcTest тесты контроллеров
```

## Скриншоты

**Дашборд** — баланс, тренды, последние транзакции:
![Dashboard](docs/screenshots/dashboard.png)

**Транзакции** — список с формой добавления/редактирования:
![Transactions](docs/screenshots/transactions.png)

**Отчёты** — история с кнопками скачивания PDF/Excel:
![Reports](docs/screenshots/reports.png)

**Вход** — страница авторизации:
![Login](docs/screenshots/login.png)

## Тесты

```bash
# Запуск всех тестов
mvn test

# Только конкретный класс
mvn test -Dtest="TransactionServiceTest"
```

59 тестов: 30 unit-тестов сервисного слоя + 29 MockMvc-тестов контроллеров.
Плюс 1 smoke-тест (`@Disabled`) — требует запущенного PostgreSQL, запускается вручную.

## Профили Spring

| Профиль | Назначение | Активация |
|---------|-----------|-----------|
| `dev` | Локальная разработка, verbose SQL-лог | По умолчанию |
| `prod` | Продакшн, параметры БД из env-переменных | `--spring.profiles.active=prod` |

## Запуск через Docker

```bash
# Скопируй шаблон секретов и задай свои значения
cp .env.example .env
# отредактируй .env: DB_USERNAME, DB_PASSWORD, POSTGRES_DB

# Запусти PostgreSQL + приложение одной командой
docker compose up --build
```

Приложение поднимется на **http://localhost:8080**. Flyway накатит миграции автоматически.  
Первый запуск занимает ~2–3 минуты (скачивает образы и собирает JAR).

## Деплой в облако

Приложение упаковано в Docker-образ и готово к деплою — постоянного хостинга нет (портфельный проект).

Общие шаги для любого облачного провайдера (Railway, Render, Fly.io, GCP Cloud Run и др.):

1. **Регистр образов** — собери и загрузи образ в Docker Hub или другой реестр:
   ```bash
   docker build -t your-user/fintrack:latest .
   docker push your-user/fintrack:latest
   ```
2. **Managed PostgreSQL** — создай managed-базу у провайдера, получи строку подключения.
3. **Переменные окружения** — задай в панели провайдера:
   - `SPRING_PROFILES_ACTIVE=prod`
   - `DB_URL=jdbc:postgresql://<host>:5432/<db>`
   - `DB_USERNAME=...`
   - `DB_PASSWORD=...`
4. **Деплой контейнера** — укажи образ из реестра, порт `8080`, и запусти сервис.

Flyway накатит миграции при первом старте автоматически.
