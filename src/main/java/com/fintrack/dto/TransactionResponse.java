package com.fintrack.dto;

import com.fintrack.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JSON-ответ для REST API.
 *
 * Почему не отдаём напрямую сущность Transaction?
 *   1. Transaction содержит поле user с паролем (в будущем) — случайно «вытечет» в API.
 *   2. Lazy-поля (category, user) вызывают LazyInitializationException при сериализации
 *      за пределами транзакции.
 *   3. Мы контролируем точно, какие поля и в каком виде попадают в JSON.
 *
 * Статический фабричный метод from() — маппинг сущности → DTO в одном месте.
 */
public record TransactionResponse(
        Long id,
        BigDecimal amount,
        String type,
        Long categoryId,
        String categoryName,
        LocalDate date,
        String description
) {
    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getAmount(),
                t.getType().name(),
                t.getCategory() != null ? t.getCategory().getId()   : null,
                t.getCategory() != null ? t.getCategory().getName() : null,
                t.getDate(),
                t.getDescription()
        );
    }
}
