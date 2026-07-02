package com.fintrack.dto;

import java.math.BigDecimal;

/**
 * Метрики за один календарный месяц — используется для построения графика тренда.
 * month — строка вида "2024-04" (год-месяц).
 */
public record MonthlyMetrics(
        String month,
        BigDecimal income,
        BigDecimal expense
) {}
