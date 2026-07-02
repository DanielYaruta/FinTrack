package com.fintrack.model;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * @Enumerated(EnumType.STRING) — в БД хранится строка "INCOME" или "EXPENSE",
     *   а не числовой индекс (0, 1).
     *   Числовой индекс (EnumType.ORDINAL) — плохая практика: стоит переставить
     *   константы в enum — и все данные в БД становятся неверными.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    public Category() {}

    public Category(String name, TransactionType type) {
        this.name = name;
        this.type = type;
    }

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }
}
