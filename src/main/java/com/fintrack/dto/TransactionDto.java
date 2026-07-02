package com.fintrack.dto;

import com.fintrack.model.TransactionType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO (Data Transfer Object) — объект для переноса данных между слоями.
 *
 * Зачем не использовать напрямую сущность Transaction?
 *   1. Безопасность: пользователь не должен иметь возможность задать поле user_id
 *      или id напрямую через форму — только разрешённые поля.
 *   2. Валидация: бизнес-правила формы могут отличаться от ограничений БД.
 *   3. Форма: в HTML-форме category приходит как categoryId (Long), а не объект.
 *
 * Аннотации Bean Validation (@NotNull, @Positive и т.д.) проверяются Spring,
 * когда контроллер получает данные формы с аннотацией @Valid.
 */
public class TransactionDto {

    @NotNull(message = "Сумма обязательна")
    @Positive(message = "Сумма должна быть больше нуля")
    @Digits(integer = 13, fraction = 2, message = "Максимум 13 цифр и 2 знака после запятой")
    private BigDecimal amount;

    @NotNull(message = "Тип транзакции обязателен")
    private TransactionType type;

    private Long categoryId;

    @NotNull(message = "Дата обязательна")
    @PastOrPresent(message = "Дата не может быть в будущем")
    private LocalDate date;

    @Size(max = 500, message = "Описание не более 500 символов")
    private String description;

    public TransactionDto() {}

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
