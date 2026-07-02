package com.fintrack.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * @Entity — JPA знает, что это класс, который нужно хранить в БД.
 * @Table(name = "users") — явно задаём имя таблицы, потому что "user"
 *   является зарезервированным словом в PostgreSQL.
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * @Id — первичный ключ.
     * @GeneratedValue(IDENTITY) — значение генерирует сама БД (BIGSERIAL/AUTO_INCREMENT).
     * Мы НЕ задаём id вручную при создании объекта — JPA сам проставит его после INSERT.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * unique = true добавляет UNIQUE-ограничение на уровне БД.
     * Flyway создаёт его явно в SQL, поэтому здесь это дублирование —
     * но оно полезно: Hibernate при validate проверяет схему и видит,
     * что ожидаем уникальность.
     */
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false)
    private LocalDate registrationDate;

    @Column(nullable = false)
    private String passwordHash = "";

    /**
     * OneToMany — у одного пользователя много транзакций.
     * mappedBy = "user" говорит JPA: "сторона-хозяин связи — поле user
     *   в классе Transaction, там и лежит foreign key".
     *
     * FetchType.LAZY — транзакции НЕ загружаются автоматически при загрузке User.
     *   Это важно: без LAZY каждый запрос User тянул бы все его транзакции.
     *   Данные будут загружены только если ты явно обратишься к getTransactions().
     *
     * cascade = CascadeType.ALL — операции (save, delete) каскадируются на транзакции.
     *   Удалил User — удалились все его транзакции.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions = new ArrayList<>();

    // --- Конструкторы ---

    public User() {}

    public User(String name, String email, LocalDate registrationDate) {
        this.name = name;
        this.email = email;
        this.registrationDate = registrationDate;
    }

    // --- Геттеры / сеттеры ---

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDate registrationDate) { this.registrationDate = registrationDate; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }
}
