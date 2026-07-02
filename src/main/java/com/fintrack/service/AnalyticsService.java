package com.fintrack.service;

import com.fintrack.dto.FinancialMetrics;
import com.fintrack.dto.MonthlyMetrics;
import com.fintrack.model.Transaction;
import com.fintrack.model.TransactionType;
import com.fintrack.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final TransactionRepository transactionRepository;

    public AnalyticsService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Агрегированные метрики для дашборда.
     * Используем sumByUserIdAndType из репозитория (JPQL SUM-запрос),
     * а не загружаем все транзакции в память.
     */
    public FinancialMetrics getMetrics(Long userId) {
        BigDecimal income  = nullToZero(transactionRepository.sumByUserIdAndType(userId, TransactionType.INCOME));
        BigDecimal expense = nullToZero(transactionRepository.sumByUserIdAndType(userId, TransactionType.EXPENSE));
        BigDecimal balance = income.subtract(expense);
        long count = transactionRepository.countByUserId(userId);

        return new FinancialMetrics(income, expense, balance, count);
    }

    /**
     * Тренд за последние N месяцев — для графика Chart.js на дашборде.
     *
     * Алгоритм:
     *   1. Загружаем все транзакции за период одним запросом.
     *   2. Группируем в памяти по месяцу (Map<"yyyy-MM", List<Transaction>>).
     *   3. Для каждого месяца из диапазона считаем сумму Income и Expense.
     *
     * Шаг 2-3 в Java (а не в SQL) — осознанный компромисс: запрос простой,
     * данных за 6 мес. немного, зато логика читаема. Если бы месяцев было 100+,
     * лучше перенести GROUP BY в SQL.
     */
    public List<MonthlyMetrics> getMonthlyTrend(Long userId, int months) {
        LocalDate end   = LocalDate.now();
        LocalDate start = end.minusMonths(months - 1).withDayOfMonth(1);

        List<Transaction> txs = transactionRepository
                .findByUserIdAndDateBetweenOrderByDateDesc(userId, start, end);

        // Группируем по строке "yyyy-MM"
        Map<String, List<Transaction>> byMonth = txs.stream()
                .collect(Collectors.groupingBy(t -> t.getDate().format(MONTH_FMT)));

        // Строим список для каждого из N месяцев (включая пустые)
        return IntStream.range(0, months)
                .mapToObj(i -> end.minusMonths(months - 1 - i).withDayOfMonth(1))
                .map(monthStart -> {
                    String key = monthStart.format(MONTH_FMT);
                    List<Transaction> monthTxs = byMonth.getOrDefault(key, List.of());

                    BigDecimal income  = sumByType(monthTxs, TransactionType.INCOME);
                    BigDecimal expense = sumByType(monthTxs, TransactionType.EXPENSE);
                    return new MonthlyMetrics(key, income, expense);
                })
                .collect(Collectors.toList());
    }

    // --- Вспомогательные методы ---

    private BigDecimal sumByType(List<Transaction> txs, TransactionType type) {
        return txs.stream()
                .filter(t -> t.getType() == type)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal nullToZero(BigDecimal value) {
        // SUM() в SQL возвращает NULL если строк нет — защищаемся здесь
        return value != null ? value : BigDecimal.ZERO;
    }
}
