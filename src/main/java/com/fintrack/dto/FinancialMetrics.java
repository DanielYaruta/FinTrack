package com.fintrack.dto;

import java.math.BigDecimal;

/**
 * record — компактный синтаксис Java 16+ для иммутабельных классов-носителей данных.
 * Компилятор автоматически генерирует: конструктор, геттеры, equals, hashCode, toString.
 * Идеально для DTO, которые только переносят данные и не имеют поведения.
 */
public record FinancialMetrics(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance,
        long transactionCount
) {}
